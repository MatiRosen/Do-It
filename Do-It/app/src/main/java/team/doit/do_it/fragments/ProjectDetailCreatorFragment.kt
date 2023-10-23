package team.doit.do_it.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import team.doit.do_it.R
import team.doit.do_it.databinding.FragmentProjectDetailCreatorBinding
import team.doit.do_it.entities.ProjectEntity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


class ProjectDetailCreatorFragment : Fragment() {

    private var _binding : FragmentProjectDetailCreatorBinding? = null
    private val binding get() = _binding!!

    private lateinit var v : View

    private lateinit var project: ProjectEntity

    private var projectImage : String = ""
    private var creatorEmail : String = ""

    interface OnProjectUpdatedListener {
        fun onProjectUpdated(successful: Boolean)
    }

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

    private fun safeAccessBinding(action: () -> Unit) {
        if (_binding != null) {
            action()
        }
    }

    override fun onStart() {
        super.onStart()

        project = ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project

        setValues()
        initializeButtons()
    }



    override fun onResume() {
        super.onResume()

        val db = FirebaseFirestore.getInstance()
        val query = db.collection("ideas")
            .whereEqualTo("creatorEmail", FirebaseAuth.getInstance().currentUser?.email)
            .whereEqualTo("creationDate", project.creationDate)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val p = documents.documents[0]
                    project.title = p.getString("title").toString()
                    project.subtitle = p.getString("subtitle").toString()
                    project.description = p.getString("description").toString()
                    project.category = p.getString("category").toString()
                    project.goal = p.getDouble("goal")!!
                    project.minBudget = p.getDouble("minBudget")!!
                    project.image = p.getString("image").toString()

                    setValues()
                } else {
                    Toast.makeText(activity, resources.getString(R.string.project_detail_error), Toast.LENGTH_SHORT).show()
                    v.findNavController().navigateUp()
                }
            }
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

        binding.imgBtnProjectDetailCreatorTrash.setOnClickListener {
            deleteProjectConfirm()
        }
    }

    private fun setValues() {
        binding.txtProjectDetailCreatorTitle.text = project.title
        binding.txtProjectDetailCreatorSubtitle.text = project.subtitle
        binding.txtProjectDetailCreatorDescription.text = project.description
        binding.txtProjectDetailCreatorCategory.text = project.category
        binding.txtProjectDetailCreatorFollowers.text = project.followersCount.toString()

        val projectGoal = this.formatMoney(project.goal)
        val goalText = getString(R.string.project_detail_goal, projectGoal)
        binding.txtProjectDetailCreatorGoal.text = spannableText(goalText, goalText.indexOf(projectGoal[0]) - 1)

        val projectMinBudget = this.formatMoney(project.minBudget)
        val minBudgetText = getString(R.string.project_detail_min_budget, projectMinBudget)
        binding.txtProjectDetailCreatorMinBudget.text = spannableText(minBudgetText, minBudgetText.indexOf(projectMinBudget[0]) -1)

        projectImage = project.image
        creatorEmail = project.creatorEmail

        this.setCreatorData()
        setImage()
    }

    private fun setCreatorData() {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        binding.progressBarProjectDetailCreator.visibility = View.VISIBLE
        binding.txtProjectDetailCreatorProfileName.visibility = View.GONE
        binding.imgProjectDetailCreatorProfileImage.visibility = View.GONE

        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                safeAccessBinding {
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
                    setUserCreatorImage(userEmail)
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, resources.getString(R.string.project_detail_error), Toast.LENGTH_SHORT).show()
                v.findNavController().navigateUp()
            }
    }

    private fun setImage() {
        if (projectImage == "") {
            binding.imgProjectDetailCreatorProjectImage.setImageResource(R.drawable.img_not_img)
            return
        }
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$creatorEmail/projects/$projectImage")

        safeAccessBinding {
            Glide.with(v.context)
                .load(storageReference)
                .placeholder(R.drawable.img_not_img)
                .error(R.drawable.img_not_img)
                .into(binding.imgProjectDetailCreatorProjectImage)
        }
    }

    private fun setUserCreatorImage(creatorEmail: String) {
        getUser(creatorEmail, object : ProfileFragment.OnUserFetchedListener {
            override fun onUserFetched(user: DocumentSnapshot?) {
                if (user != null) {
                    val titleImg = user.getString("imgPerfil").toString()
                    if (titleImg == "") {
                        binding.imgProjectDetailCreatorProfileImage.setImageResource(R.drawable.img_avatar)
                        return
                    }

                    val storageReference = FirebaseStorage.getInstance().reference.child("images/$creatorEmail/imgProfile/$titleImg")

                    safeAccessBinding {
                        Glide.with(v.context)
                            .load(storageReference)
                            .placeholder(R.drawable.img_avatar)
                            .error(R.drawable.img_avatar)
                            .into(binding.imgProjectDetailCreatorProfileImage)
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
                safeAccessBinding {
                    if (!documents.isEmpty) {
                        val user = documents.documents[0]
                        listener.onUserFetched(user)
                    } else {
                        listener.onUserFetched(null)
                    }
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



    private fun showBottomNav() {
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showBottomNav()
        _binding = null
    }

    private fun deleteProjectConfirm() {
        val alertDialogBuilder = AlertDialog.Builder(activity)

        alertDialogBuilder.setTitle(resources.getString(R.string.project_detail_delete_title))

        if(ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project.hasFollowers())
            alertDialogBuilder.setMessage(resources.getString(R.string.project_detail_noDeletable_message))
        else {
            alertDialogBuilder.setMessage(resources.getString(R.string.project_detail_delete_message))
            alertDialogBuilder.setPositiveButton(resources.getString(R.string.project_detail_delete_accept)) { _, _ ->
                deleteProject()
            }
        }

        alertDialogBuilder.setNegativeButton(resources.getString(R.string.project_detail_delete_decline)) { _, _ ->

        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun deleteProject() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        currentUser?.let {
            db.collection("ideas")
                .whereEqualTo("creatorEmail", currentUser?.email)
                .whereEqualTo("creationDate", ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project.creationDate)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        db.collection("ideas").document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                v.findNavController().navigateUp()
                            }
                            .addOnFailureListener {
                                handleDeleteFailure()
                            }
                    }
                }
        }
    }

    private fun handleDeleteFailure() {
        Toast.makeText(activity, resources.getString(R.string.project_detail_delete_error), Toast.LENGTH_SHORT).show()
    }


}

















