package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import team.doit.do_it.R


class ProjectCreationFragment : Fragment() {

    private lateinit var v : View
    private lateinit var btnSaveProject: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_project_creation, container, false)

        initializeVariables()

        return v
    }

    private fun initializeVariables() {
        btnSaveProject = v.findViewById(R.id.btn_project_creation_save)
    }

    override fun onStart() {
        super.onStart()

        // TODO: Enviar el proyecto a la base de datos
        btnSaveProject.setOnClickListener {
            val successMessage = R.string.project_creation_succeed.toString()
            Snackbar.make(v, successMessage, Snackbar.LENGTH_LONG).show()
            v.findNavController().navigateUp()
        }
    }
}