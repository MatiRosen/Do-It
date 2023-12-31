package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import team.doit.do_it.R
import team.doit.do_it.adapters.ProjectListAdapter
import team.doit.do_it.databinding.FragmentFollowingProjectBinding
import team.doit.do_it.entities.ProjectEntity
import team.doit.do_it.listeners.OnViewItemClickedListener

class FollowingProjectFragment : Fragment(), OnViewItemClickedListener<ProjectEntity> {

    private var _binding: FragmentFollowingProjectBinding? = null
    private val binding get() = _binding!!

    private lateinit var v : View
    private val db = FirebaseFirestore.getInstance()

    private lateinit var projectListAdapter: ProjectListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFollowingProjectBinding.inflate(inflater, container, false)
        v = binding.root
        binding.progressBarFollowingProject.visibility = View.GONE
        binding.progressBarFollowingProjectBottom.visibility = View.GONE

        return v
    }

    override fun onStart() {
        super.onStart()
        overrideBackButton()
        safeAccessBinding {
            setupRecyclerView()
        }
    }

    private fun safeAccessBinding(action: () -> Unit) {
        if (_binding != null && context != null) {
            action()
        }
    }

    private fun setupRecyclerView() {
        val query = FirebaseAuth.getInstance().currentUser?.email?.let {
            db.collection("ideas").whereArrayContains("followers", it)
        }

        val config = PagingConfig(20, 10, false)

        val options = FirestorePagingOptions.Builder<ProjectEntity>()
            .setLifecycleOwner(this)
            .setQuery(query!!, config, ProjectEntity::class.java)
            .build()

        setupRecyclerViewSettings(binding.recyclerFollowingProjects)

        projectListAdapter = ProjectListAdapter(options, this)
        setupLoadStateSettings()

        projectListAdapter.startListening()
        binding.recyclerFollowingProjects.adapter = projectListAdapter
    }

    private fun setupRecyclerViewSettings(recycler : RecyclerView) {
        recycler.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        recycler.layoutManager = linearLayoutManager
    }

    private fun setupLoadStateSettings(){
        viewLifecycleOwner.lifecycleScope.launch {
            projectListAdapter.loadStateFlow.collectLatest { loadStates ->
                when(loadStates.refresh){
                    is LoadState.Loading -> {
                        binding.progressBarFollowingProject.visibility = View.VISIBLE
                        binding.recyclerFollowingProjects.visibility = View.GONE
                    }
                    is LoadState.Error -> {
                        Toast.makeText(context, resources.getString(R.string.following_project_get_project_failed) , Toast.LENGTH_SHORT).show()
                        binding.progressBarFollowingProject.visibility = View.GONE
                        binding.recyclerFollowingProjects.visibility = View.VISIBLE
                    }
                    is LoadState.NotLoading -> {
                        binding.progressBarFollowingProject.visibility = View.GONE
                        binding.recyclerFollowingProjects.visibility = View.VISIBLE
                    }
                }

                when(loadStates.append){
                    is LoadState.Loading -> {
                        binding.progressBarFollowingProjectBottom.visibility = View.VISIBLE
                    }
                    is LoadState.Error -> {
                        Toast.makeText(context, resources.getString(R.string.following_project_get_project_failed) , Toast.LENGTH_SHORT).show()
                        binding.progressBarFollowingProjectBottom.visibility = View.GONE
                    }
                    is LoadState.NotLoading -> {
                        binding.progressBarFollowingProjectBottom.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun overrideBackButton() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = findNavController()
                navController.navigate(R.id.action_followingProjectFragment_to_options)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewItemDetail(item: ProjectEntity) {
        val action = FollowingProjectFragmentDirections
            .actionFollowingProjectFragmentToProjectDetailInvestorFragment(item)
        this.findNavController().navigate(action)
    }

    override fun onStop() {
        super.onStop()
        projectListAdapter.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}