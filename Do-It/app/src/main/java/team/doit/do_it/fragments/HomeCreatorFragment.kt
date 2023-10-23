package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

    private lateinit var projectListAdapter: ProjectListAdapter

    interface OnUserFetchedListener {
        fun onUserFetched(user: DocumentSnapshot?)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeCreatorBinding.inflate(inflater, container, false)

        v = binding.root
        binding.progressBarHomeCreator.visibility = View.GONE
        binding.progressBarHomeCreatorBottom.visibility = View.GONE

        return v
    }

    override fun onStart() {
        super.onStart()
        setupButtons()
        setupRecyclerView()
        showMargins()
    }

    private fun showMargins() {
        val constraintSet = ConstraintSet()
        constraintSet.connect(R.id.mainHost, ConstraintSet.TOP, R.id.guidelineMainActivityHorizontal3, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.mainHost, ConstraintSet.BOTTOM, R.id.bottomNavigationView, ConstraintSet.TOP)
        constraintSet.connect(R.id.mainHost, ConstraintSet.START, R.id.guidelineMainActivityVertical2, ConstraintSet.END)
        constraintSet.connect(R.id.mainHost, ConstraintSet.END, R.id.guidelineMainActivityVertical98, ConstraintSet.START)


        constraintSet.applyTo(requireActivity().findViewById(R.id.frameLayoutMainActivity))
    }

    private fun setupButtons() {
        binding.btnHomeCreatorCreateProject.setOnClickListener {
            isPremiumUser { isPremium ->
                if(isPremium || projectListAdapter.itemCount < 2){
                    val action = HomeCreatorFragmentDirections.actionGlobalProjectCreationFragment()
                    this.findNavController().navigate(action)
                }
                else {
                    Toast.makeText(context, resources.getString(R.string.home_creation_premium_user), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.switchToHomeInvestor.setOnClickListener {
            val action = HomeCreatorFragmentDirections.actionHomeCreatorFragmentToHomeInversorFragment()
            v.findNavController().navigate(action)
        }
    }

    private fun isPremiumUser(action: (Boolean) -> Unit): Boolean {
        var isPremium = false

        val currentUser = FirebaseAuth.getInstance().currentUser
        getUser(currentUser?.email.toString(), object : ProfileFragment.OnUserFetchedListener {
            override fun onUserFetched(user: DocumentSnapshot?) {
                if (user != null) {
                    isPremium = user.getBoolean("premium")!!
                    action(isPremium)
                }
            }
        })
        return isPremium
    }

    private fun getUser(email: String, listener: ProfileFragment.OnUserFetchedListener) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    listener.onUserFetched(documents.documents[0])
                } else {
                    listener.onUserFetched(null)
                }
            }
            .addOnFailureListener {
                listener.onUserFetched(null)
            }
    }

    private fun setupRecyclerView() {
        val query = db.collection("ideas")
            .whereEqualTo("creatorEmail", FirebaseAuth.getInstance().currentUser?.email)

        val config = PagingConfig(20, 10, false)

        val options = FirestorePagingOptions.Builder<ProjectEntity>()
            .setLifecycleOwner(this)
            .setQuery(query, config, ProjectEntity::class.java)
            .build()

        setupRecyclerViewSettings(binding.recyclerHomeCreatorProjects)

        projectListAdapter = ProjectListAdapter(options, this)
        setupLoadStateSettings()

        projectListAdapter.startListening()
        binding.recyclerHomeCreatorProjects.adapter = projectListAdapter
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
                        binding.progressBarHomeCreator.visibility = View.VISIBLE
                        binding.recyclerHomeCreatorProjects.visibility = View.GONE
                    }
                    is LoadState.Error -> {
                        Toast.makeText(context, resources.getString(R.string.home_creation_get_project_failed) , Toast.LENGTH_SHORT).show()
                        binding.progressBarHomeCreator.visibility = View.GONE
                        binding.recyclerHomeCreatorProjects.visibility = View.VISIBLE
                    }
                    is LoadState.NotLoading -> {
                        binding.progressBarHomeCreator.visibility = View.GONE
                        binding.recyclerHomeCreatorProjects.visibility = View.VISIBLE
                    }
                }

                when(loadStates.append){
                    is LoadState.Loading -> {
                        changeBottomProgressBarVisibility(View.VISIBLE)
                    }
                    is LoadState.Error -> {
                        Toast.makeText(context, resources.getString(R.string.home_creation_get_project_failed) , Toast.LENGTH_SHORT).show()
                        changeBottomProgressBarVisibility(View.GONE)
                    }
                    is LoadState.NotLoading -> {
                        changeBottomProgressBarVisibility(View.GONE)
                    }
                }
            }
        }
    }

    // TODO rehacer este metodo y hacer que el boton de invertir se esconda al hacer scroll.
    private fun changeBottomProgressBarVisibility(visibility: Int) {
        binding.progressBarHomeCreatorBottom.visibility = visibility
        val startPadding = binding.recyclerHomeCreatorProjects.paddingStart
        val topPadding = binding.recyclerHomeCreatorProjects.paddingTop
        val endPadding = binding.recyclerHomeCreatorProjects.paddingEnd
        val bottomPadding = if (visibility == View.VISIBLE) 100 else 0
        binding.recyclerHomeCreatorProjects.setPadding(startPadding, topPadding, endPadding, bottomPadding)
    }

    override fun onViewItemDetail(project: ProjectEntity) {
        val action = HomeCreatorFragmentDirections.actionGlobalProjectDetailFragment(project)
        this.findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        binding.switchToHomeInvestor.isChecked = false
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