package team.doit.do_it.fragments

import android.graphics.BitmapFactory
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
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import team.doit.do_it.R
import team.doit.do_it.databinding.FragmentProjectDetailInvestorBinding
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class ProjectDetailInvestorFragment : Fragment() {

    private var _binding : FragmentProjectDetailInvestorBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View

    private var projectImage : String = ""
    private var creatorEmail : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProjectDetailInvestorBinding.inflate(inflater, container, false)
        v = binding.root


        hideBottomNav()
        removeMargins()

        return v
    }

    override fun onStart() {
        super.onStart()

        setValues()

        binding.imgBtnProjectDetailInvestorBack.setOnClickListener {
            v.findNavController().navigateUp()
        }

        // TODO Hacer que cuando vas al perfil, puedas volver al proyecto.
        binding.linearLayoutProjectDetailInvestorData.setOnClickListener {
            val action = ProjectDetailInvestorFragmentDirections.actionProjectDetailInvestorFragmentToProfileFragment(creatorEmail)
            this.findNavController().navigate(action)
        }
    }

    private fun setValues() {
        val project = ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project
        creatorEmail = project.getCreatorEmail()
        binding.txtProjectDetailInvestorTitle.text = project.getTitle()
        binding.txtProjectDetailInvestorSubtitle.text = project.getSubtitle()
        binding.txtProjectDetailInvestorDescription.text = project.getDescription()
        binding.txtProjectDetailInvestorCategory.text = project.getCategory()

        val projectGoal = this.formatMoney(project.getGoal())
        val goalText = getString(R.string.project_detail_goal, projectGoal)
        binding.txtProjectDetailInvestorGoal.text = spannableText(goalText, goalText.indexOf(projectGoal[0]) - 1)

        val projectMinBudget = this.formatMoney(project.getMinBudget())
        val minBudgetText = getString(R.string.project_detail_min_budget, projectMinBudget)
        binding.txtProjectDetailInvestorMinBudget.text = spannableText(minBudgetText, minBudgetText.indexOf(projectMinBudget[0]) -1)

        projectImage = project.getImage()
        creatorEmail = project.getCreatorEmail()

        this.setCreatorData()
    }

    private fun setCreatorData() {
        binding.progressBarProjectDetailInvestor.visibility = View.VISIBLE
        binding.txtProjectDetailInvestorProfileName.visibility = View.GONE
        binding.imgProjectDetailInvestorProfileImage.visibility = View.GONE
        binding.imgBtnProjectDetailInvestorChat.visibility = View.GONE

        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("email", creatorEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.documents[0]
                    binding.txtProjectDetailInvestorProfileName.text = user.getString("nombre")
                    binding.progressBarProjectDetailInvestor.visibility = View.GONE
                    binding.txtProjectDetailInvestorProfileName.visibility = View.VISIBLE
                    binding.imgProjectDetailInvestorProfileImage.visibility = View.VISIBLE
                    binding.imgBtnProjectDetailInvestorChat.visibility = View.VISIBLE
                } else {
                    Toast.makeText(activity, resources.getString(R.string.project_detail_error), Toast.LENGTH_SHORT).show()
                    v.findNavController().navigateUp()
                }
                setImage()
            }
            .addOnFailureListener {
                Toast.makeText(activity, resources.getString(R.string.project_detail_error), Toast.LENGTH_SHORT).show()
                v.findNavController().navigateUp()
            }
    }

    private fun setImage() {
        var storageReference = FirebaseStorage.getInstance().reference.child("images/$creatorEmail/projects/$projectImage")
        var localFile = File.createTempFile("images", "jpg")
        storageReference.getFile(localFile)
            .addOnSuccessListener {
                var bitMap = BitmapFactory.decodeFile(localFile.absolutePath)
                binding.imgProjectDetailInvestorProjectImage.setImageBitmap(bitMap)
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