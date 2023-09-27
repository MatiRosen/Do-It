package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import team.doit.do_it.R
import team.doit.do_it.entities.ProjectEntity


class ProjectCreationFragment : Fragment() {

    private lateinit var v : View
    private lateinit var btnSaveProject: View
    private lateinit var spinnerCategory : Spinner
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_project_creation, container, false)

        initializeVariables()

        return v
    }

    private fun initializeVariables() {
        btnSaveProject = v.findViewById(R.id.btnProjectCreationSave)
        spinnerCategory = v.findViewById(R.id.spinnerProjectCreationCategory)


        startSpinner()
    }

    override fun onStart() {
        super.onStart()

        btnSaveProject.setOnClickListener {
            val projectTitle = v.findViewById<EditText>(R.id.editTxtProjectCreationTitle).text.toString()
            val projectSubtitle = v.findViewById<EditText>(R.id.editTxtProjectCreationSubtitle).text.toString()
            val projectCategory = spinnerCategory.selectedItem.toString()
            val projectImg = ""
            val projectDescription = v.findViewById<EditText>(R.id.editTxtProjectCreationDescription).text.toString()
            val projectMinBudget = v.findViewById<EditText>(R.id.editTxtProjectCreationMinBudget).text.toString().toDouble()
            val projectTotalBudget = v.findViewById<EditText>(R.id.editTxtProjectCreationTotalBudget).text.toString().toDouble()

            val project = ProjectEntity(projectTitle, projectSubtitle, projectDescription, projectCategory, projectImg, projectMinBudget, projectTotalBudget)
            // TODO: Enviar el proyecto a la base de datos comprobando que nada es null ni vacio
            val successMessage = R.string.project_creation_succeed.toString() + ": " + project.toString()
            Snackbar.make(v, successMessage, Snackbar.LENGTH_LONG).show()
            v.findNavController().navigateUp()
        }
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

        spinnerCategory.adapter = adapter
    }
}