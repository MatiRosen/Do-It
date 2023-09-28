package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import team.doit.do_it.adapters.ProjectListAdapter
import team.doit.do_it.databinding.FragmentHomeCreatorBinding
import team.doit.do_it.entities.ProjectEntity
import team.doit.do_it.listeners.OnViewItemClickedListener

class HomeCreatorFragment : Fragment(), OnViewItemClickedListener {

    private var _binding : FragmentHomeCreatorBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View

    private var projectList : MutableList<ProjectEntity> = ArrayList()

    private lateinit var projectListAdapter: ProjectListAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeCreatorBinding.inflate(inflater, container, false)

        v = binding.root

        if (projectList.size == 0) {
            addProjects()
        }

        return v
    }

    private fun addProjects() {
        projectList.add(ProjectEntity("Proyecto 1 sobre la naturaleza y el arte y los museos :)", "Este proyecto trata sobre muchas cosas. Por ejemplo: Lorem Ipsum dolor sit amet, consectetur adipiscing elit", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation", "Naturaleza, arte, dinero y mas!", "", 10000.0, 99000.0))
        projectList.add(ProjectEntity("Proyecto 2", "Subtitulo proyecto 2", "Descripción 2", "Categoría 2", "", 5000.0, 66000.0))
        projectList.add(ProjectEntity("Proyecto 3", "Subtitulo proyecto 3", "Descripción 3", "Categoría 3", "", 20000.0, 99000.0))
        projectList.add(ProjectEntity("Proyecto 4", "Subtitulo proyecto 4", "Descripción 4", "Categoría 4", "", 15000.0, 350000.0))
        projectList.add(ProjectEntity("Proyecto 5", "Subtitulo proyecto 5", "Descripción 5", "Categoría 5", "", 100000.0, 3000000.0))
    }

    override fun onStart() {
        super.onStart()

        binding.btnHomeCreatorCreateProject.setOnClickListener {
            val action = HomeCreatorFragmentDirections.actionHomeCreatorFragmentToProjectCreationFragment()
            v.findNavController().navigate(action)
        }

        setupRecyclerView()
    }

    override fun onViewItemDetail(project: ProjectEntity) {
        val action = HomeCreatorFragmentDirections.actionHomeCreatorFragmentToProjectDetailFragment(project)
        this.findNavController().navigate(action)
    }

    private fun setupRecyclerView() {
        requireActivity()

        binding.recyclerHomeCreatorProjects.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerHomeCreatorProjects.layoutManager = linearLayoutManager
        projectListAdapter = ProjectListAdapter(projectList, this)
        binding.recyclerHomeCreatorProjects.adapter = projectListAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}