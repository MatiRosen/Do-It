package team.doit.do_it.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import team.doit.do_it.R
import team.doit.do_it.adapters.ProjectListAdapter
import team.doit.do_it.databinding.FragmentHomeInvestorBinding
import team.doit.do_it.entities.ProjectEntity
import team.doit.do_it.listeners.OnViewItemClickedListener


class HomeInvestorFragment : Fragment(), OnViewItemClickedListener {

    private var _binding : FragmentHomeInvestorBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View
    private val db = FirebaseFirestore.getInstance()
    private var projectList : MutableList<ProjectEntity> = ArrayList()
    private var allProjectList : MutableList<ProjectEntity> = ArrayList()

    private lateinit var projectListAdapter: ProjectListAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeInvestorBinding.inflate(inflater, container, false)
        v = binding.root
        if (projectList.size == 0) {
            addProjects()
        }
        return v
    }
    private fun addProjects() {
        allProjectList.add(ProjectEntity("Proyecto 1 sobre la naturaleza y el arte y los museos :)", "Este proyecto trata sobre muchas cosas. Por ejemplo: Lorem Ipsum dolor sit amet, consectetur adipiscing elit", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation", "Naturaleza, arte, dinero y mas!", "", 10000.0, 99000.0))
        allProjectList.add(ProjectEntity("Proyecto 2", "Subtitulo proyecto 2", "Descripción 2", "Categoría 2", "", 5000.0, 66000.0))
        allProjectList.add(ProjectEntity("Proyecto 3", "Subtitulo proyecto 3", "Descripción 3", "Categoría 3", "", 20000.0, 99000.0))
        allProjectList.add(ProjectEntity("Proyecto 4", "Subtitulo proyecto 4", "Descripción 4", "Categoría 4", "", 15000.0, 350000.0))
        allProjectList.add(ProjectEntity("Proyecto 5", "Subtitulo proyecto 5", "Descripción 5", "Categoría 5", "", 100000.0, 3000000.0))

        // TODO: Hacer esto asincrono. (no se si ya lo es)
        db.collection("ideas")
            .get()
            .addOnSuccessListener { result ->
                    for (document in result) {
                        //Log.d(TAG, "${document.id} => ${document.data}")
                        if (document.id != "xyz") {
                            val title = document.data["title"] as String
                            val subtitle = document.data["subtitle"] as String
                            val description = document.data["description"] as String
                            val image = document.data["image"] as String
                            val category = document.data["category"] as String
                            val goal = document.getLong("goal") as Long
                            val minBudget = document.getLong("minBudget") as Long
                            projectList.add(
                                ProjectEntity(
                                    title,
                                    subtitle,
                                    description,
                                    category,
                                    image,
                                    minBudget.toDouble(),
                                    goal.toDouble()
                                )
                            )
                        }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(activity, R.string.home_investor_get_project_failed, Toast.LENGTH_SHORT).show()
            }
    }
    override fun onStart() {
        super.onStart()
        setupRecyclerView()
    }
    override fun onViewItemDetail(project: ProjectEntity) {
        val action = HomeInvestorFragmentDirections.actionHomeInvestorFragmentToProjectDetailFragment(project)
        this.findNavController().navigate(action)
    }
    private fun setupRecyclerView() {
        requireActivity()

        binding.recyclerHomeInvestorProjects.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerHomeInvestorProjects.layoutManager = linearLayoutManager
        projectListAdapter = ProjectListAdapter(projectList, this)
        binding.recyclerHomeInvestorProjects.adapter = projectListAdapter

        binding.recyclerHomeInvestorAllProjects.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerHomeInvestorAllProjects.layoutManager = linearLayoutManager
        projectListAdapter = ProjectListAdapter(allProjectList, this)
        binding.recyclerHomeInvestorAllProjects.adapter = projectListAdapter

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}