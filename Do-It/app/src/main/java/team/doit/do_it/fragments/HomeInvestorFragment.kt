package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

    private lateinit var popularProjectListAdapter: ProjectListAdapter
    private lateinit var allProjectListAdapter: ProjectListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeInvestorBinding.inflate(inflater, container, false)
        v = binding.root

        binding.progressBarHomeInvestor.visibility = View.GONE
        binding.progressBarHomeInvestorTop.visibility = View.GONE
        binding.progressBarHomeInvestorBottom.visibility = View.GONE

        return v
    }

    override fun onStart() {
        super.onStart()
        setupPopularProjectsRecyclerView()
        setupAllProjectsRecyclerView()
        setupButtons()
    }

    private fun setupButtons() {
        binding.switchToHomeCreator.setOnClickListener {
            val action = HomeInvestorFragmentDirections.actionHomeInvestorFragmentToHomeCreatorFragment()
            this.findNavController().navigate(action)
        }
    }

    private fun setupPopularProjectsRecyclerView() {
        // TODO fijarse por que trae mas de 5.
        val query = db.collection("ideas")
            .orderBy("followersCount", Query.Direction.DESCENDING).limit(5)

        val config = PagingConfig(5,1,  false)

        val options = FirestorePagingOptions.Builder<ProjectEntity>()
            .setLifecycleOwner(this)
            .setQuery(query, config, ProjectEntity::class.java)
            .build()


        setupRecyclerViewSettings(binding.recyclerHomeInvestorPopularProjects, true)
        popularProjectListAdapter = ProjectListAdapter(options, this)
        //setupPopularLoadStateSettings()

        popularProjectListAdapter.startListening()
        binding.recyclerHomeInvestorPopularProjects.adapter = popularProjectListAdapter
    }

    private fun setupAllProjectsRecyclerView() {
        val query = db.collection("ideas")
            .orderBy("creationDate", Query.Direction.DESCENDING)

        val config = PagingConfig(20, 10, false)

        val options = FirestorePagingOptions.Builder<ProjectEntity>()
            .setLifecycleOwner(this)
            .setQuery(query, config, ProjectEntity::class.java)
            .build()

        setupRecyclerViewSettings(binding.recyclerHomeInvestorAllProjects)
        allProjectListAdapter = ProjectListAdapter(options, this)
        setupAllLoadStateSettings()

        allProjectListAdapter.startListening()
        binding.recyclerHomeInvestorAllProjects.adapter = allProjectListAdapter
    }

    private fun setupRecyclerViewSettings(recycler : RecyclerView, isHorizontal : Boolean = false) {
        recycler.setHasFixedSize(true)
        val linearLayoutManager = if (isHorizontal) LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) else LinearLayoutManager(context)
        recycler.layoutManager = linearLayoutManager
    }

    // TODO rehacer los metodos de load state settings para que se vean mejor los progress bar
    private fun setupPopularLoadStateSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            popularProjectListAdapter.loadStateFlow.collectLatest { loadStates ->
                when(loadStates.refresh){
                    is LoadState.Loading -> {
                        binding.progressBarHomeInvestor.visibility = View.VISIBLE
                    }
                    is LoadState.NotLoading -> {
                        binding.progressBarHomeInvestor.visibility = View.GONE
                    }
                    is LoadState.Error -> {
                        Toast.makeText(context, resources.getString(R.string.home_investor_get_projects_failed), Toast.LENGTH_SHORT).show()
                    }
                }

                /*when(loadStates.append){
                    is LoadState.Loading -> {
                        changeTopProgressBarVisibility(View.VISIBLE)
                    }
                    is LoadState.Error -> {
                        Toast.makeText(context, resources.getString(R.string.home_investor_get_projects_failed), Toast.LENGTH_SHORT).show()
                        changeTopProgressBarVisibility(View.GONE)
                    }
                    is LoadState.NotLoading -> {
                        changeTopProgressBarVisibility(View.GONE)
                    }
                }*/
            }
        }
    }

    private fun setupAllLoadStateSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            allProjectListAdapter.loadStateFlow.collectLatest { loadStates ->
                when(loadStates.refresh){
                    is LoadState.Loading -> {
                        binding.progressBarHomeInvestor.visibility = View.VISIBLE
                    }
                    is LoadState.NotLoading -> {
                        binding.progressBarHomeInvestor.visibility = View.GONE
                    }
                    is LoadState.Error -> {
                        Toast.makeText(context, resources.getString(R.string.home_investor_get_projects_failed), Toast.LENGTH_SHORT).show()
                    }
                }

                /*when(loadStates.append){
                    is LoadState.Loading -> {
                        changeBottomProgressBarVisibility(View.VISIBLE)
                    }
                    is LoadState.Error -> {
                        Toast.makeText(context, resources.getString(R.string.home_investor_get_projects_failed), Toast.LENGTH_SHORT).show()
                        changeBottomProgressBarVisibility(View.GONE)
                    }
                    is LoadState.NotLoading -> {
                        changeBottomProgressBarVisibility(View.GONE)
                    }
                }*/
            }
        }
    }

    private fun changeBottomProgressBarVisibility(visibility: Int) {
        binding.progressBarHomeInvestorBottom.visibility = visibility
        val startPadding = binding.recyclerHomeInvestorAllProjects.paddingStart
        val topPadding = binding.recyclerHomeInvestorAllProjects.paddingTop
        val endPadding = binding.recyclerHomeInvestorAllProjects.paddingEnd
        val bottomPadding = if (visibility == View.VISIBLE) binding.guidelineHomeInvestorHorizontal95.bottom else 0
        binding.recyclerHomeInvestorAllProjects.setPadding(startPadding, topPadding, endPadding, bottomPadding)
    }

    private fun changeTopProgressBarVisibility(visibility: Int) {
        binding.progressBarHomeInvestorTop.visibility = visibility
        val startPadding = binding.recyclerHomeInvestorPopularProjects.paddingStart
        val topPadding = binding.recyclerHomeInvestorPopularProjects.paddingTop
        val endPadding = if (visibility == View.VISIBLE) binding.progressBarHomeInvestorTop.width else 0
        val bottomPadding = binding.recyclerHomeInvestorPopularProjects.paddingBottom
        binding.recyclerHomeInvestorPopularProjects.setPadding(startPadding, topPadding, endPadding, bottomPadding)
    }

    override fun onViewItemDetail(project: ProjectEntity) {
        val action = HomeInvestorFragmentDirections.actionGlobalProjectDetailInvestorFragment(project)
        this.findNavController().navigate(action)
    }

    override fun onStop() {
        super.onStop()
        popularProjectListAdapter.stopListening()
        allProjectListAdapter.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}