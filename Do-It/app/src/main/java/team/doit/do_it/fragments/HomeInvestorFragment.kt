package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import team.doit.do_it.R
import team.doit.do_it.adapters.ProjectListAdapter
import team.doit.do_it.databinding.FragmentHomeInvestorBinding
import team.doit.do_it.entities.ProjectEntity
import team.doit.do_it.listeners.OnViewItemClickedListener
import java.util.Date


class HomeInvestorFragment : Fragment(), OnViewItemClickedListener {

    private var _binding : FragmentHomeInvestorBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View
    private val db = FirebaseFirestore.getInstance()
    private var popularProjectList : MutableList<ProjectEntity> = ArrayList()
    private var allProjectList : MutableList<ProjectEntity> = ArrayList()

    private lateinit var projectListAdapter: ProjectListAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeInvestorBinding.inflate(inflater, container, false)
        v = binding.root

        binding.progressBarHomeInvestor.visibility = View.GONE

        return v
    }
    private fun addProjects() {

        binding.recyclerHomeInvestorProjects.visibility = View.GONE
        binding.txtHomeInvestorSubTitle.visibility = View.GONE
        binding.txtHomeInvestorTitle.visibility = View.GONE
        binding.searchViewHomeInvestor.visibility = View.GONE
        binding.switchToHomeCreator.visibility = View.GONE
        binding.progressBarHomeInvestor.visibility = View.VISIBLE

        // TODO: Hacer esto asincrono. (no se si ya lo es)
        db.collection("ideas")
            .orderBy("followersCount", Query.Direction.DESCENDING).limit(5)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.id != "xyz") {
                        popularProjectList.add(
                            ProjectEntity(
                                document.data["creatorEmail"] as String,
                                document.data["title"] as String,
                                document.data["subtitle"] as String,
                                document.data["description"] as String,
                                document.data["category"] as String,
                                document.data["image"] as String,
                                document.data["minBudget"] as Double,
                                document.data["goal"] as Double,
                                (document.data["visitorsCount"] as Long).toInt(),
                                (document.data["followersCount"] as Long).toInt(),
                                (document.data["creationDate"] as Timestamp).toDate()
                            )
                        )
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, resources.getString(R.string.home_investor_get_project_failed), Toast.LENGTH_SHORT).show()
            }

        db.collection("ideas")
            .orderBy("creationDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                    for (document in result) {
                        if (document.id != "xyz") {
                            allProjectList.add(
                                ProjectEntity(
                                    document.data["creatorEmail"] as String,
                                    document.data["title"] as String,
                                    document.data["subtitle"] as String,
                                    document.data["description"] as String,
                                    document.data["category"] as String,
                                    document.data["image"] as String,
                                    document.data["minBudget"] as Double,
                                    document.data["goal"] as Double,
                                    (document.data["visitorsCount"] as Long).toInt(),
                                    (document.data["followersCount"] as Long).toInt(),
                                    (document.data["creationDate"] as Timestamp).toDate()
                                )
                            )
                        }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(activity, resources.getString(R.string.home_investor_get_project_failed), Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                binding.progressBarHomeInvestor.visibility = View.GONE
                binding.recyclerHomeInvestorProjects.visibility = View.VISIBLE
                binding.txtHomeInvestorTitle.visibility = View.VISIBLE
                binding.txtHomeInvestorSubTitle.visibility = View.VISIBLE
                binding.searchViewHomeInvestor.visibility = View.VISIBLE
                binding.switchToHomeCreator.visibility = View.VISIBLE

            }
    }
    override fun onStart() {
        super.onStart()
        setupRecyclerView()

        binding.switchToHomeCreator.setOnClickListener {
            var action = HomeInvestorFragmentDirections.actionHomeInvestorFragmentToHomeCreatorFragment()
            this.findNavController().navigate(action)
        }

    }

    override fun onResume() {
        super.onResume()

        // TODO: Cambiarlo para hacer que solo se agreguen los nuevos proyectos, no todos.
        allProjectList.clear()
        popularProjectList.clear()
        addProjects()
    }

    override fun onViewItemDetail(project: ProjectEntity) {
        val action = HomeInvestorFragmentDirections.actionGlobalProjectDetailInvestorFragment(project)
        this.findNavController().navigate(action)
    }
    private fun setupRecyclerView() {
        requireActivity()

        binding.recyclerHomeInvestorProjects.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerHomeInvestorProjects.layoutManager = linearLayoutManager
        projectListAdapter = ProjectListAdapter(popularProjectList, this)
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