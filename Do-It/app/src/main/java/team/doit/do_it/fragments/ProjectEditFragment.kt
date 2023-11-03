package team.doit.do_it.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import team.doit.do_it.R
import team.doit.do_it.activities.MainActivity
import team.doit.do_it.databinding.FragmentProjectEditBinding
import java.io.File
import java.util.Date

class ProjectEditFragment : Fragment() {

    private var _binding : FragmentProjectEditBinding? = null

    private var selectedImage: Uri? = null
    private var photoFile: File? = null
    private var lastImage: String = ""
    private val binding get() = _binding!!

    private lateinit var v : View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProjectEditBinding.inflate(inflater, container, false)
        v = binding.root

        startSpinner()

        return v
    }

    override fun onStart() {
        super.onStart()

        setupButtons()
        replaceData()

        val activity = requireActivity() as MainActivity
        activity.hideBottomNav()
        activity.removeMargins()
    }

    private fun setupButtons(){
        binding.imgBtnProjectEditBack.setOnClickListener {
            v.findNavController().navigateUp()
        }

        binding.imgProjectEditImage.setOnClickListener{
            pickImage()
        }

        binding.btnEditProjectConfirm.setOnClickListener {
            updateProject(object : ProjectDetailCreatorFragment.OnProjectUpdatedListener {
                override fun onProjectUpdated(successful: Boolean) {
                    if (successful) {
                        safeAccessBinding {
                            Toast.makeText(activity, "Project updated", Toast.LENGTH_SHORT).show()
                            v.findNavController().navigateUp()
                        }
                    } else {
                        safeAccessBinding {
                            Toast.makeText(activity, "Error updating project", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }


    private fun pickImage() {
        photoFile = pickImageFromGallery()
    }

    private fun pickImageFromGallery() : File?{
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
        return selectedImage?.let { it.path?.let { it1 -> File(it1) } }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if(data != null) run {
                val selectedImageFromGallery: Uri? = data.data
                selectedImage = selectedImageFromGallery
                if (selectedImageFromGallery != null) {
                    binding.imgProjectEditImage.setImageURI(selectedImageFromGallery)
                }
            }
        }
    }

    private fun setImage(creatorEmail: String, titleImg: String) {
        if (titleImg == "") {
            binding.imgProjectEditImage.setImageResource(R.drawable.img_not_img)
            return
        }
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$creatorEmail/projects/$titleImg")

        Glide.with(v.context)
            .load(storageReference)
            .placeholder(R.drawable.img_not_img)
            .error(R.drawable.img_not_img)
            .into(binding.imgProjectEditImage)

    }

    private fun uploadImage(projectCreatorEmail: String, projectTitle: String): String {
        var fileName = "$projectCreatorEmail-$projectTitle" + "-" + Date().time.toString()

        val storeReference = FirebaseStorage.getInstance().getReference("images/$projectCreatorEmail/projects/$fileName")
        storeReference.putFile(selectedImage!!)
            .addOnSuccessListener {
                lastImage = "images/$projectCreatorEmail/projects/$fileName"
            }.addOnFailureListener {
                safeAccessBinding {
                    Snackbar.make(v, resources.getString(R.string.project_creation_image_upload_failed), Snackbar.LENGTH_LONG).show()
                }
                fileName = ""
            }
        return fileName
    }

    private fun safeAccessBinding(action : () -> Unit) {
        if (_binding != null && context != null) {
            action()
        }
    }

    private fun replaceData() {
        safeAccessBinding {
            binding.editTxtProjectEditTitle.setText(ProjectEditFragmentArgs.fromBundle(requireArguments()).project.title)
            binding.editTxtProjectEditDescription.setText(ProjectEditFragmentArgs.fromBundle(requireArguments()).project.description)
            binding.editTxtProjectEditSubtitle.setText(ProjectEditFragmentArgs.fromBundle(requireArguments()).project.subtitle)
            binding.editTxtProjectEditGoal.setText(ProjectEditFragmentArgs.fromBundle(requireArguments()).project.goal.toString())
            binding.editTxtProjectEditMinBudget.setText(ProjectEditFragmentArgs.fromBundle(requireArguments()).project.minBudget.toString())
            setImage(ProjectEditFragmentArgs.fromBundle(requireArguments()).project.creatorEmail, ProjectEditFragmentArgs.fromBundle(requireArguments()).project.image)
        }
    }

    private fun updateProject(listener : ProjectDetailCreatorFragment.OnProjectUpdatedListener) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        currentUser?.let {

            val project = hashMapOf<String, Any>(
                "title" to binding.editTxtProjectEditTitle.text.toString(),
                "description" to binding.editTxtProjectEditDescription.text.toString(),
                "subtitle" to binding.editTxtProjectEditSubtitle.text.toString(),
                "goal" to binding.editTxtProjectEditGoal.text.toString().toDouble().toInt(),
                "minBudget" to binding.editTxtProjectEditMinBudget.text.toString().toDouble().toInt(),
                "category" to binding.spinnerProjectEditCategory.selectedItem.toString(),
                "image" to if(selectedImage != null) uploadImage(currentUser.email.toString(), binding.editTxtProjectEditTitle.text.toString()) else ProjectEditFragmentArgs.fromBundle(requireArguments()).project.image
            )

            if(selectedImage != null){
                    val storageReference = FirebaseStorage.getInstance().reference.child("images/${currentUser.email.toString()}/projects")

                    storageReference.listAll()
                        .addOnSuccessListener { listResult ->
                            if (listResult.items.isNotEmpty()) {
                                listResult.items.find { x -> x.name == ProjectEditFragmentArgs.fromBundle(requireArguments()).project.image }?.delete()
                                    ?.addOnFailureListener() {
                                        resources.getString(R.string.project_deleteImage_error)
                                    }
                            }
                        }
            }

            if(validateFields(project)) {
                db.collection("ideas")
                    .whereEqualTo("creatorEmail", currentUser?.email)
                    .whereEqualTo("creationDate", ProjectEditFragmentArgs.fromBundle(requireArguments()).project.creationDate)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            db.collection("ideas").document(document.id)
                                .update(project)
                                .addOnSuccessListener {
                                    safeAccessBinding {
                                        Toast.makeText(
                                            activity,
                                            resources.getString(R.string.project_edit_succeed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        v.findNavController().navigateUp()
                                    }
                                }
                                .addOnFailureListener {
                                   safeAccessBinding {
                                       Toast.makeText(
                                           activity,
                                           resources.getString(R.string.project_edit_failed),
                                           Toast.LENGTH_SHORT
                                       ).show()
                                   }

                                }
                        }
                    }
            }
        }
    }

    private fun validateFields(project: HashMap<String, Any>) : Boolean{
        val fieldsToCheck = listOf(
            Pair(project["title"], resources.getString(R.string.project_edit_title_error)),
            Pair(project["subtitle"], resources.getString(R.string.project_edit_subtitle_error)),
            Pair(project["description"], resources.getString(R.string.project_edit_description_error)),
            Pair(project["minBudget"], resources.getString(R.string.project_edit_min_budget_error)),
            Pair(project["goal"], resources.getString(R.string.project_edit_goal_error))
        )

        for ((property, errorMessage) in fieldsToCheck){
            if (property.toString().isEmpty()){
                Snackbar.make(v, errorMessage, Snackbar.LENGTH_LONG).show()
                return false
            }
        }

        if (project["minBudget"].toString().toInt() < 0.0){
            Snackbar.make(v, resources.getString(R.string.project_creation_min_budget_error), Snackbar.LENGTH_LONG).show()
            return false
        }

        if (project["goal"].toString().toInt() <= 0.0){
            Snackbar.make(v, resources.getString(R.string.project_creation_goal_error), Snackbar.LENGTH_LONG).show()
            return false
        }

        if (project["goal"].toString().toInt() < project["minBudget"].toString().toInt()){
            Snackbar.make(v, resources.getString(R.string.project_creation_min_budget_higher_than_goal_error), Snackbar.LENGTH_LONG).show()
            return false
        }


        return true
    }


    private fun startSpinner(){
        val categoryList = resources.getStringArray(R.array.categories_array).toMutableList()
        val hint = resources.getString(R.string.project_creation_project_category_hint)
        categoryList.add(0, hint)

        val selected = ProjectEditFragmentArgs.fromBundle(requireArguments()).project.category
        val index = categoryList.indexOf(selected)

        val adapter = object : ArrayAdapter<String>(v.context, android.R.layout.simple_list_item_activated_1, categoryList){
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)

                if (position == 0) {
                    (view as TextView).setTextColor(resources.getColor(R.color.medium_gray, null))
                    view.setBackgroundColor(0)
                }

                return view
            }

        }

        binding.spinnerProjectEditCategory.adapter = adapter

        if (index != -1) {
            binding.spinnerProjectEditCategory.setSelection(index)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        val activity = requireActivity() as MainActivity
        activity.showBottomNav()
        activity.showMargins()
    }
}