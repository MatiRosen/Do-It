package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import team.doit.do_it.R
import team.doit.do_it.adapters.ProjectListAdapter
import team.doit.do_it.entities.ProjectEntity
import team.doit.do_it.listeners.OnViewItemClickedListener

class HomeCreatorFragment : Fragment(), OnViewItemClickedListener {

    private lateinit var v : View
    private lateinit var btnCreateProject: ImageButton

    private lateinit var recyclerProject : RecyclerView

    private var projectList : MutableList<ProjectEntity> = ArrayList()

    private lateinit var projectListAdapter: ProjectListAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_home_creator, container, false)
        iniliazeVariables()


        if (projectList.size == 0) {
            addProjects()
        }

        return v
    }

    private fun addProjects() {
        projectList.add(ProjectEntity("Proyecto 1", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation", "Categoría 1", "", 10000.0, 99000.0))
        projectList.add(ProjectEntity("Proyecto 2", "Descripción 2", "Categoría 2", "", 5000.0, 66000.0))
        projectList.add(ProjectEntity("Proyecto 3", "Descripción 3", "Categoría 3", "", 20000.0, 99000.0))
        projectList.add(ProjectEntity("Proyecto 4", "Descripción 4", "Categoría 4", "", 15000.0, 350000.0))
        projectList.add(ProjectEntity("Proyecto 5", "Descripción 5", "Categoría 5", "", 100000.0, 3000000.0))
    }

    private fun iniliazeVariables() {
        recyclerProject = v.findViewById(R.id.recycler_home_creator_projects)
        btnCreateProject = v.findViewById(R.id.btn_home_creator_create_project)
    }

    override fun onStart() {
        super.onStart()

        btnCreateProject.setOnClickListener {
            val action = HomeCreatorFragmentDirections.actionHomeCreatorToProjectCreationFragment()
            v.findNavController().navigate(action)
        }

        requireActivity()
        recyclerProject.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerProject.layoutManager = linearLayoutManager
        projectListAdapter = ProjectListAdapter(projectList, this)
        recyclerProject.adapter = projectListAdapter
    }

    override fun onViewItemDetail(project: ProjectEntity) {
        val action = HomeCreatorFragmentDirections.actionHomeCreatorToProjectDetail(project)
        this.findNavController().navigate(action)
    }
}