package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import team.doit.do_it.R
import team.doit.do_it.databinding.FragmentProjectCreationBinding
import team.doit.do_it.entities.ProjectEntity


class ProjectCreationFragment : Fragment() {

    private var _binding : FragmentProjectCreationBinding? = null
    private val binding get() = _binding!!
    private lateinit var v: View

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


    // TODO: Enviar el proyecto a la base de datos comprobando que nada es null ni vacio
    private fun saveProject(){
        val project = createProject()

        val successMessage = R.string.project_creation_succeed.toString() + ": " + project.toString()
        Snackbar.make(v, successMessage, Snackbar.LENGTH_LONG).show()
        v.findNavController().navigateUp()
    }

    private fun createProject() : ProjectEntity{
        val projectTitle = binding.editTxtProjectCreationTitle.text.toString()
        val projectSubtitle = binding.editTxtProjectCreationSubtitle.text.toString()
        val projectCategory = binding.spinnerProjectCreationCategory.selectedItem.toString()
        val projectImg = ""
        val projectDescription = binding.editTxtProjectCreationDescription.text.toString()
        val projectMinBudget = binding.editTxtProjectCreationMinBudget.text.toString().toDouble()
        val projectTotalBudget = binding.editTxtProjectCreationTotalBudget.text.toString().toDouble()

        return ProjectEntity(projectTitle, projectSubtitle, projectDescription, projectCategory, projectImg, projectMinBudget, projectTotalBudget)
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