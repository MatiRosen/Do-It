package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import team.doit.do_it.R
import team.doit.do_it.databinding.FragmentProjectCreationBinding
import team.doit.do_it.entities.ProjectEntity
import java.util.Date


class ProjectCreationFragment : Fragment() {

    private var _binding : FragmentProjectCreationBinding? = null
    private val binding get() = _binding!!
    private lateinit var v: View

    private val db = FirebaseFirestore.getInstance()

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

        binding.btnProjectCreationSave.setOnClickListener {
            saveProject()
        }
    }

    private fun saveProject(){
        val project = createProject() ?: return
        // TODO: Verificar si el usuario es premium. Si no lo es, no dejarle crear mas de 2 proyectos.

        // TODO: Hacer esto asincrono.
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
        val projectImg = "" // TODO Realizar el subido de imagenes.
        val projectDescription = binding.editTxtProjectCreationDescription.text.toString()
        val projectMinBudget = binding.editTxtProjectCreationMinBudget.text.toString().toDoubleOrNull() ?: 0.0
        val projectGoal = binding.editTxtProjectCreationGoal.text.toString().toDoubleOrNull() ?: 0.0

        if (projectTitle.isEmpty() || projectSubtitle.isEmpty() || projectCategory.isEmpty() || projectCategory.equals(resources.getStringArray(R.array.categories_array)) || projectDescription.isEmpty() || projectMinBudget < 0.0 || projectGoal <= 0.0){
            Snackbar.make(v, resources.getString(R.string.project_creation_failed_empty_fields), Snackbar.LENGTH_LONG).show()
            return null
        }

        return ProjectEntity(projectCreatorEmail, projectTitle, projectSubtitle, projectDescription, projectCategory, projectImg, projectMinBudget, projectGoal, Date())
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