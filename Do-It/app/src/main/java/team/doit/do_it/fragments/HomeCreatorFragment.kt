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

class HomeCreatorFragment : Fragment(), OnViewItemClickedListener<ProjectEntity> {

    private var _binding : FragmentHomeCreatorBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View

    private val db = FirebaseFirestore.getInstance()

    private lateinit var projectListAdapter: ProjectListAdapter

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

    private fun safeAccessBinding(action: () -> Unit) {
        if (_binding != null && context != null) {
            action()
        }
    }

    override fun onStart() {
        super.onStart()
        setupButtons()
        safeAccessBinding {
            setupRecyclerView()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.switchHomeCreatorToHomeInvestor.isChecked = false
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

        binding.switchHomeCreatorToHomeInvestor.setOnCheckedChangeListener() { _, isChecked ->
            if (isChecked) {
                val action = HomeCreatorFragmentDirections.actionHomeCreatorFragmentToHomeInversorFragment()
                v.findNavController().navigate(action)
            }
        }
    }

    private fun isPremiumUser(action: (Boolean) -> Unit): Boolean {
        var isPremium = false

        val currentUser = FirebaseAuth.getInstance().currentUser
        getUser(currentUser?.email.toString(), object : ProfileFragment.OnUserFetchedListener {
            override fun onUserFetched(user: DocumentSnapshot?) {
                if (user != null) {
                    isPremium = user.getBoolean("isPremium")!!
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

        val config = PagingConfig(3, 1, false)

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

    private fun changeBottomProgressBarVisibility(visibility: Int) {
        binding.progressBarHomeCreatorBottom.visibility = visibility
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.constraintLayoutHomeCreator)

        if (visibility == View.VISIBLE) {
            constraintSet.connect(
                R.id.recyclerHomeInvestorAllProjects,
                ConstraintSet.BOTTOM,
                R.id.progressBarHomeInvestorBottom,
                ConstraintSet.TOP
            )
        } else {
            constraintSet.connect(
                R.id.recyclerHomeInvestorAllProjects,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
        }

        constraintSet.applyTo(binding.constraintLayoutHomeCreator)
    }

    override fun onViewItemDetail(item: ProjectEntity) {
        val action = HomeCreatorFragmentDirections.actionGlobalProjectDetailFragment(item)
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