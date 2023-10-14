package team.doit.do_it.fragments

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.net.toUri
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import team.doit.do_it.R
import team.doit.do_it.databinding.FragmentProjectDetailCreatorBinding
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


class ProjectDetailCreatorFragment : Fragment() {

    private var _binding : FragmentProjectDetailCreatorBinding? = null
    private val binding get() = _binding!!

    private lateinit var v : View

    private var projectImage : String = ""
    private var creatorEmail : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProjectDetailCreatorBinding.inflate(inflater, container, false)
        v = binding.root

        hideBottomNav()
        removeMargins()

        return v
    }

    override fun onStart() {
        super.onStart()

        setValues()
        initializeButtons()
    }

    private fun initializeButtons() {
        binding.imgBtnProjectDetailCreatorBack.setOnClickListener {
            v.findNavController().navigateUp()
        }

        val project = ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project

        binding.imgBtnProjectDetailCreatorEdit.setOnClickListener {
            val action = ProjectDetailCreatorFragmentDirections.actionProjectDetailFragmentToEditProjectFragment(project)
            v.findNavController().navigate(action)
        }
    }

    private fun setValues() {
        val project = ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project

        binding.txtProjectDetailCreatorTitle.text = project.title
        binding.txtProjectDetailCreatorSubtitle.text = project.subtitle
        binding.txtProjectDetailCreatorDescription.text = project.description
        binding.txtProjectDetailCreatorCategory.text = project.category

        val projectGoal = this.formatMoney(project.goal)
        val goalText = getString(R.string.project_detail_goal, projectGoal)
        binding.txtProjectDetailCreatorGoal.text = spannableText(goalText, goalText.indexOf(projectGoal[0]) - 1)

        val projectMinBudget = this.formatMoney(project.minBudget)
        val minBudgetText = getString(R.string.project_detail_min_budget, projectMinBudget)
        binding.txtProjectDetailCreatorMinBudget.text = spannableText(minBudgetText, minBudgetText.indexOf(projectMinBudget[0]) -1)

        projectImage = project.image
        creatorEmail = project.creatorEmail

        this.setCreatorData()
    }

    private fun setCreatorData() {
        val creatorEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        binding.progressBarProjectDetailCreator.visibility = View.VISIBLE
        binding.txtProjectDetailCreatorProfileName.visibility = View.GONE
        binding.imgProjectDetailCreatorProfileImage.visibility = View.GONE

        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("email", creatorEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.documents[0]
                    binding.txtProjectDetailCreatorProfileName.text = user.getString("nombre")
                    binding.progressBarProjectDetailCreator.visibility = View.GONE
                    binding.txtProjectDetailCreatorProfileName.visibility = View.VISIBLE
                    binding.imgProjectDetailCreatorProfileImage.visibility = View.VISIBLE
                } else {
                    Toast.makeText(activity, resources.getString(R.string.project_detail_error), Toast.LENGTH_SHORT).show()
                    v.findNavController().navigateUp()
                }
                setImage()
                setUserCreatorImage(creatorEmail)
            }
            .addOnFailureListener {
                Toast.makeText(activity, resources.getString(R.string.project_detail_error), Toast.LENGTH_SHORT).show()
                v.findNavController().navigateUp()
            }
    }

    private fun setImage() {
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$creatorEmail/projects/$projectImage")
        val localFile = File.createTempFile("images", "jpg")
        storageReference.getFile(localFile)
            .addOnSuccessListener {
                val bitMap = BitmapFactory.decodeFile(localFile.absolutePath)
                binding.imgProjectDetailCreatorProjectImage.setImageBitmap(bitMap)
            }
    }

    private fun setUserCreatorImage(creatorEmail: String) {
        getUser(creatorEmail, object : ProfileFragment.OnUserFetchedListener {
            override fun onUserFetched(user: DocumentSnapshot?) {
                if (user != null) {
                    val titleImg = user.getString("imgPerfil").toString()
                    val storageReference = FirebaseStorage.getInstance().reference.child("images/$creatorEmail/imgProfile/$titleImg")
                    val localFile = File.createTempFile("images", "jpg")

                    storageReference.getFile(localFile)
                        .addOnSuccessListener {
                            val bitMap = BitmapFactory.decodeFile(localFile.absolutePath)
                            binding.imgProjectDetailCreatorProfileImage.setImageBitmap(bitMap)
                        }
                } else {
                    Toast.makeText(activity, resources.getString(R.string.profile_dataUser_error), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun getUser(email: String, listener: ProfileFragment.OnUserFetchedListener) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.documents[0]
                    listener.onUserFetched(user)
                } else {
                    listener.onUserFetched(null)
                }
            }
            .addOnFailureListener {
                listener.onUserFetched(null)
            }
    }

    private fun formatMoney(money: Double): String {
        val decimalFormatSymbols = DecimalFormatSymbols()
        decimalFormatSymbols.groupingSeparator = '.'
        decimalFormatSymbols.decimalSeparator = ','
        val decimalFormat = DecimalFormat("#,###.00", decimalFormatSymbols)
        return decimalFormat.format(money)
    }

    private fun spannableText(text: String, index: Int): SpannableString {
        val spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.green, null)),
            index,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    private fun hideBottomNav() {
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE
    }

    private fun removeMargins() {
        requireActivity().findViewById<FragmentContainerView>(R.id.mainHost)
            .layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
            )
    }

    private fun showMargins() {
        val constraintSet = ConstraintSet()
        constraintSet.connect(R.id.mainHost, ConstraintSet.TOP, R.id.guidelineMainActivityHorizontal3, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.mainHost, ConstraintSet.BOTTOM, R.id.bottomNavigationView, ConstraintSet.TOP)
        constraintSet.connect(R.id.mainHost, ConstraintSet.START, R.id.guidelineMainActivityVertical2, ConstraintSet.END)
        constraintSet.connect(R.id.mainHost, ConstraintSet.END, R.id.guidelineMainActivityVertical98, ConstraintSet.START)


        constraintSet.applyTo(requireActivity().findViewById(R.id.frameLayoutMainActivity))
    }

    private fun showBottomNav() {
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showBottomNav()
        showMargins()
        _binding = null
    }
}