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
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import team.doit.do_it.R
import team.doit.do_it.activities.MainActivity
import team.doit.do_it.adapters.CommentListAdapter
import team.doit.do_it.databinding.FragmentProjectDetailCreatorBinding
import team.doit.do_it.entities.CommentEntity
import team.doit.do_it.entities.ProjectEntity
import team.doit.do_it.listeners.RecyclerViewCommentsListener
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


class ProjectDetailCreatorFragment : Fragment() {

    private var _binding : FragmentProjectDetailCreatorBinding? = null
    private val binding get() = _binding!!

    private lateinit var v : View

    private lateinit var project: ProjectEntity

    private var projectImage : String = ""
    private var creatorEmail : String = ""

    private lateinit var listener : RecyclerViewCommentsListener
    private val db = FirebaseFirestore.getInstance()

    interface OnProjectUpdatedListener {
        fun onProjectUpdated(successful: Boolean)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProjectDetailCreatorBinding.inflate(inflater, container, false)
        v = binding.root

        return v
    }

    private fun safeAccessBinding(action: () -> Unit) {
        if (_binding != null && context != null) {
            action()
        }
    }

    private fun safeActivityCall(action: () -> Unit) {
        try{
            if (activity != null && !requireActivity().isFinishing && !requireActivity().isDestroyed) {
                action()
            }
        } catch (_: IllegalStateException) {
        }
    }

    override fun onStart() {
        super.onStart()

        project = ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project

        setupAllCommentsRecyclerView()
        setValues()
        initializeButtons()
    }

    override fun onResume() {
        super.onResume()

        val db = FirebaseFirestore.getInstance()
        db.collection("ideas")
            .whereEqualTo("creatorEmail", FirebaseAuth.getInstance().currentUser?.email)
            .whereEqualTo("creationDate", project.creationDate)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    safeAccessBinding {
                        val p = documents.documents[0]
                        project.title = p.getString("title").toString()
                        project.subtitle = p.getString("subtitle").toString()
                        project.description = p.getString("description").toString()
                        project.category = p.getString("category").toString()
                        project.goal = p.getDouble("goal")!!
                        project.minBudget = p.getDouble("minBudget")!!
                        project.image = p.getString("image").toString()

                        setValues()
                    }
                } else {
                    safeAccessBinding {
                        Toast.makeText(activity, resources.getString(R.string.project_detail_error), Toast.LENGTH_SHORT).show()
                        v.findNavController().navigateUp()
                    }
                }
            }

        val activity = requireActivity() as MainActivity
        activity.hideBottomNav()
        activity.removeMargins()
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

        binding.txtProjectDetailCreatorFollowers.setOnClickListener {
            if (!project.hasFollowers()){
                Toast.makeText(context, resources.getString(R.string.project_followers_no_followers), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val action = ProjectDetailCreatorFragmentDirections.actionProjectDetailFragmentToProjectFollowersFragment(project)
            v.findNavController().navigate(action)
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
                        binding.txtProjectDetailCreatorProfileName.text = user.getString("firstName")
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
                safeAccessBinding {
                    Toast.makeText(activity, resources.getString(R.string.project_detail_error), Toast.LENGTH_SHORT).show()
                    v.findNavController().navigateUp()
                }
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
                safeAccessBinding {
                    if (user != null) {
                        val titleImg = user.getString("userImage").toString()
                        if (titleImg == "") {
                            binding.imgProjectDetailCreatorProfileImage.setImageResource(R.drawable.img_avatar)
                            return@safeAccessBinding
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
        if (money == 0.0) {
            return "0${decimalFormatSymbols.decimalSeparator}00"
        } else {
            val decimalFormat = DecimalFormat("#,###.00", decimalFormatSymbols)
            return decimalFormat.format(money)
        }
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

    private fun deleteProjectConfirm() {
        FirebaseFirestore.getInstance()
            .collection("inversiones")
            .whereEqualTo("projectID", project.uuid)
            .whereNotEqualTo("status", "REJECTED")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    safeActivityCall {
                        val alertDialogBuilder = AlertDialog.Builder(activity)

                        safeAccessBinding {
                            alertDialogBuilder.setTitle(resources.getString(R.string.project_detail_delete_title))

                            if(ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project.hasFollowers())
                                alertDialogBuilder.setMessage(resources.getString(R.string.project_detail_noDeletable_message_followers))
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
                    }
                } else {
                    safeAccessBinding {
                        Toast.makeText(context, resources.getString(R.string.project_detail_noDeletable_message_investors), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener{
                safeAccessBinding {
                    Toast.makeText(context, resources.getString(R.string.project_detail_delete_error), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun deleteProject() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        val project = ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project

        currentUser?.let {
            db.collection("ideas")
                .whereEqualTo("creatorEmail", currentUser?.email)
                .whereEqualTo("creationDate", project.creationDate)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        db.collection("ideas").document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                deleteProjectImage(currentUser.email.toString(), ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project.image)
                                deleteInvestments(project.uuid)
                                safeAccessBinding {
                                    v.findNavController().navigateUp()
                                }
                            }
                            .addOnFailureListener {
                                safeAccessBinding {
                                    handleDeleteFailure()
                                }
                            }
                    }
                }
        }
    }

    private fun deleteInvestments(projectID: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("inversiones")
            .whereEqualTo("projectID", projectID)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    db.collection("inversiones").document(document.id)
                        .delete()
                        .addOnFailureListener {
                            safeAccessBinding {
                                handleDeleteFailure()
                            }
                        }
                }
            }
    }

    private fun deleteProjectImage(mail: String, imgName: String) {
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$mail/projects")

        storageReference.listAll()
            .addOnSuccessListener { listResult ->
                if (listResult.items.isNotEmpty()) {
                    listResult.items.find { x -> x.name == imgName }?.delete()
                        ?.addOnFailureListener() {
                            resources.getString(R.string.project_deleteImage_error)
                        }
                }
            }
    }

    private fun setupAllCommentsRecyclerView() {
        val project = ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project

        binding.recyclerProjectDetailCreatorComments.setHasFixedSize(true)
        val linearLayout = LinearLayoutManager(context)
        binding.recyclerProjectDetailCreatorComments.layoutManager = linearLayout
        setListener()
        val commentAdapter = CommentListAdapter(project.comments, listener)
        binding.recyclerProjectDetailCreatorComments.adapter = commentAdapter
    }

    private fun setListener() {
        listener = object : RecyclerViewCommentsListener {
            override fun onDeleteCommentClicked(comment: CommentEntity) { }

            override fun onSavedCommentClicked(comment: CommentEntity) { }

            override fun onEditCommentClicked(comment: CommentEntity) { }
        }
    }

    private fun handleDeleteFailure() {
        Toast.makeText(activity, resources.getString(R.string.project_detail_delete_error), Toast.LENGTH_SHORT).show()
    }

    override fun onStop(){
        super.onStop()
        val activity = requireActivity() as MainActivity
        activity.showMargins()
        activity.showBottomNav()
    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}