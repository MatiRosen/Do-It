package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import team.doit.do_it.R
import team.doit.do_it.adapters.ProjectListAdapter
import team.doit.do_it.databinding.FragmentHomeCreatorBinding
import team.doit.do_it.entities.ProjectEntity
import team.doit.do_it.listeners.OnViewItemClickedListener

class HomeCreatorFragment : Fragment(), OnViewItemClickedListener {

    private var _binding : FragmentHomeCreatorBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View

    private val db = FirebaseFirestore.getInstance()
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
        // TODO: Hacer esto asincrono. (no se si ya lo es)
        db.collection("ideas")
            .whereEqualTo("creatorEmail", FirebaseAuth.getInstance().currentUser?.email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document.id != "xyz") {
                        projectList.add(
                            ProjectEntity(
                                document.data["creatorEmail"] as String,
                                document.data["title"] as String,
                                document.data["subtitle"] as String,
                                document.data["description"] as String,
                                document.data["category"] as String,
                                document.data["image"] as String,
                                (document.getLong("goal") as Long).toDouble(),
                                (document.getLong("minBudget") as Long).toDouble()
                            )
                        )
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, resources.getString(R.string.home_creation_get_project_failed) , Toast.LENGTH_SHORT).show()
            }
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