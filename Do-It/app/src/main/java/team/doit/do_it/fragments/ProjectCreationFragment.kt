package team.doit.do_it.fragments

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import team.doit.do_it.R
import team.doit.do_it.databinding.FragmentProjectCreationBinding
import team.doit.do_it.entities.ProjectEntity
import java.io.File
import java.util.Date


class ProjectCreationFragment : Fragment() {
    private var _binding : FragmentProjectCreationBinding? = null
    private val binding get() = _binding!!
    private lateinit var v: View

    private val db = FirebaseFirestore.getInstance()

    private var selectedImage: Uri? = null
    private var photoFile: File? = null
    private var lastImage: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProjectCreationBinding.inflate(inflater, container, false)

        v = binding.root
        startSpinner()

        return v
    }

    override fun onStart() {
        super.onStart()

        binding.btnProjectCreationAddImage.setOnClickListener {
            pickImage()
        }

        binding.btnProjectCreationSave.setOnClickListener {
            saveProject()
        }
    }

    private fun pickImage() {
        photoFile = pickImageFromGallery()
    }

    private fun pickImageFromGallery() : File?{
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
        return selectedImage?.let { File(it.path) }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if(data != null) run {
                val selectedImageFromGallery: Uri? = data.data
                selectedImage = selectedImageFromGallery
            }
        }
    }

    private fun saveProject(){
        val project = createProject() ?: return
        // TODO: Verificar si el usuario es premium. Si no lo es, no dejarle crear mas de 2 proyectos.

        saveProjectToDatabase(project)
        /*db.collection("ideas")
            .whereEqualTo("title", project.getTitle())
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty){
                    saveProjectToDatabase(project)
                } else {
                    Snackbar.make(v, resources.getString(R.string.project_creation_failed_title_exists), Snackbar.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Snackbar.make(v, resources.getString(R.string.project_creation_failed), Snackbar.LENGTH_LONG).show()
            }
        */
    }

    private fun saveProjectToDatabase(project: ProjectEntity){
        db.collection("ideas")
            .add(project)
            .addOnSuccessListener {
                showSuccessMessage(project)
                v.findNavController().popBackStack()
            }
            .addOnFailureListener {
                Snackbar.make(v, resources.getString(R.string.project_creation_failed), Snackbar.LENGTH_LONG).show()
            }
    }

    private fun showSuccessMessage(project: ProjectEntity){
        val successMessage = resources.getString(R.string.project_creation_succeed) + ": " + project.toString()
        Snackbar.make(v, successMessage, Snackbar.LENGTH_LONG).show()
    }

    private fun createProject() : ProjectEntity?{
        val projectCreatorEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        val projectTitle = binding.editTxtProjectCreationTitle.text.toString()
        val projectSubtitle = binding.editTxtProjectCreationSubtitle.text.toString()
        val projectCategory = binding.spinnerProjectCreationCategory.selectedItem.toString()
        val projectImg = if(selectedImage != null) uploadImage(projectCreatorEmail, projectTitle) else ""
        val projectDescription = binding.editTxtProjectCreationDescription.text.toString()
        val projectMinBudget = binding.editTxtProjectCreationMinBudget.text.toString().toDoubleOrNull() ?: 0.0
        val projectGoal = binding.editTxtProjectCreationGoal.text.toString().toDoubleOrNull() ?: 0.0

        val project = ProjectEntity(projectCreatorEmail, projectTitle, projectSubtitle, projectDescription, projectCategory, projectImg, projectMinBudget, projectGoal, 0, 0, Date())

        return if (validateFields(project)) project else null
    }

    private fun uploadImage(projectCreatorEmail: String, projectTitle: String): String {
        var fileName = "$projectCreatorEmail-$projectTitle" + "-" + Date().time.toString()

        val storeReference = FirebaseStorage.getInstance().getReference("images/$projectCreatorEmail/projects/$fileName")
        storeReference.putFile(selectedImage!!)
            .addOnSuccessListener {
                lastImage = "images/$projectCreatorEmail/projects/$fileName"
            }.addOnFailureListener {
                Snackbar.make(v, resources.getString(R.string.project_creation_image_upload_failed), Snackbar.LENGTH_LONG).show()
                fileName = ""
            }
        return fileName
    }

    private fun validateFields(project: ProjectEntity) : Boolean{
        val fieldsToCheck = listOf(
            Pair(project.title, resources.getString(R.string.project_creation_title_error)),
            Pair(project.subtitle, resources.getString(R.string.project_creation_subtitle_error)),
            Pair(project.description, resources.getString(R.string.project_creation_description_error)),
            Pair(project.category, resources.getString(R.string.project_creation_category_error)),
            Pair(project.image, resources.getString(R.string.project_creation_image_error)),
            Pair(project.minBudget, resources.getString(R.string.project_creation_min_budget_error)),
            Pair(project.goal, resources.getString(R.string.project_creation_goal_error))
        )

        for ((property, errorMessage) in fieldsToCheck){
            if (property.toString().isEmpty()){
                Snackbar.make(v, errorMessage, Snackbar.LENGTH_LONG).show()
                return false
            }
        }

        if (project.category == resources.getString(R.string.project_creation_project_category_hint)){
            Snackbar.make(v, resources.getString(R.string.project_creation_category_error), Snackbar.LENGTH_LONG).show()
            return false
        }

        if (project.minBudget < 0.0){
            Snackbar.make(v, resources.getString(R.string.project_creation_min_budget_error), Snackbar.LENGTH_LONG).show()
            return false
        }

        if (project.goal <= 0.0){
            Snackbar.make(v, resources.getString(R.string.project_creation_goal_error), Snackbar.LENGTH_LONG).show()
            return false
        }

        if (project.goal < project.minBudget){
            Snackbar.make(v, resources.getString(R.string.project_creation_min_budget_higher_than_goal_error), Snackbar.LENGTH_LONG).show()
            return false
        }


        return true
    }

    private fun startSpinner(){
        val categoryList = resources.getStringArray(R.array.categories_array).toMutableList()
        val hint = resources.getString(R.string.project_creation_project_category_hint)
        categoryList.add(0, hint)

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

        binding.spinnerProjectCreationCategory.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}