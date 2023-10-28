package team.doit.do_it.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
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

    private var interstitial: InterstitialAd? = null
    private val quantityClicksToShowAds: Int = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeInvestorBinding.inflate(inflater, container, false)
        v = binding.root
        binding.progressBarHomeInvestor.visibility = View.GONE
        binding.progressBarHomeInvestorTop.visibility = View.GONE
        binding.progressBarHomeInvestorBottom.visibility = View.GONE
        startSpinner()
        return v
    }

    override fun onStart() {
        super.onStart()
        setupPopularProjectsRecyclerView()
        setupAllProjectsRecyclerView()
        setupButtons()
    }

    private fun setupButtons() {
        binding.switchToHomeCreator.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                val action = HomeInvestorFragmentDirections.actionHomeInvestorFragmentToHomeCreatorFragment()
                this.findNavController().navigate(action)
            }
        }

        binding.searchViewHomeInvestor.setOnClickListener{
            binding.txtHomeInvestorTitle.visibility = View.GONE
            binding.recyclerHomeInvestorPopularProjects.visibility = View.GONE
            filterAllProjectsByfoneticSearch()
            binding.spinnerFilterCategory.setSelection(0)
        }
        binding.btnFiltrarCategoria.setOnClickListener {
            binding.txtHomeInvestorTitle.visibility = View.GONE
            binding.recyclerHomeInvestorPopularProjects.visibility = View.GONE
            val categoryfilter = binding.spinnerFilterCategory.selectedItem.toString()
            if (categoryfilter == resources.getString(R.string.project_creation_project_category_hint)){
                binding.txtHomeInvestorTitle.visibility = View.VISIBLE
                binding.recyclerHomeInvestorPopularProjects.visibility = View.VISIBLE
                Snackbar.make(v, resources.getString(R.string.project_creation_category_error), Snackbar.LENGTH_LONG).show()
                setupAllProjectsRecyclerView()
            }else{
                binding.searchViewHomeInvestor.setQuery("",false)
                filterAllProjectsByCategory()
            }
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

        var query = db.collection("ideas").orderBy("creationDate", Query.Direction.DESCENDING)

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
    private fun filterAllProjectsByCategory(){
        allProjectListAdapter.stopListening()
        val categorySearch = binding.spinnerFilterCategory.selectedItem.toString()
        var query = db.collection("ideas").whereEqualTo("category", categorySearch)

        val config = PagingConfig(20, 10, false)
        val options = FirestorePagingOptions.Builder<ProjectEntity>()
            .setLifecycleOwner(this)
            .setQuery(query, config, ProjectEntity::class.java)
            .build()

        allProjectListAdapter.updateOptions(options)
        allProjectListAdapter.startListening()
        binding.recyclerHomeInvestorAllProjects.adapter = allProjectListAdapter
    }
    private fun filterAllProjectsByfoneticSearch(){
        allProjectListAdapter.stopListening()
        val foneticSearch = binding.searchViewHomeInvestor.query.toString()

        var query = db.collection("ideas") .orderBy("title")
            .whereGreaterThanOrEqualTo("title",foneticSearch)
            
            .whereLessThanOrEqualTo("title",foneticSearch+'\uf8ff')

        val config = PagingConfig(20, 10, false)
        val options = FirestorePagingOptions.Builder<ProjectEntity>()
            .setLifecycleOwner(this)
            .setQuery(query, config, ProjectEntity::class.java)
            .build()

        allProjectListAdapter.updateOptions(options)
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

    override fun onViewItemDetail(item: Any) {
        val project = if (item is ProjectEntity) item else return

        val investorEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        val action = if (project.creatorEmail == investorEmail) {
            HomeInvestorFragmentDirections.actionGlobalProjectDetailFragment(project)
        } else {
            HomeInvestorFragmentDirections.actionGlobalProjectDetailInvestorFragment(project)
        }

        isUserPremium(investorEmail, object : OnUserFetchedListener {
            override fun onUserFetched(user: Boolean?) {
                if (user == false) {
                    if(context?.let { getClicksCounter(it) }!! % quantityClicksToShowAds == 0) {
                        showAds()
                    } else {
                        context?.let { updateClicksCounter(it) }
                    }
                }
            }
        })

        this.findNavController().navigate(action)
    }
    private fun startSpinner(){
        val categoryList = resources.getStringArray(R.array.categories_array).toMutableList()
        val hint = resources.getString(R.string.project_creation_project_category_hint)
        categoryList.add(0, hint)
        val adapter = object : ArrayAdapter<String>(v.context, android.R.layout.simple_list_item_activated_1, categoryList){
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                return view
            }
        }
        binding.spinnerFilterCategory.adapter = adapter
    }

    private fun showAds() {
        if(interstitial != null){
            activity?.let { interstitial!!.show(it) }
            interstitial = null
        } else{
            context?.let { updateClicksCounter(it) }
            initAds()
        }
    }

    private fun initAds() {
        var adRequest = com.google.android.gms.ads.AdRequest.Builder().build()

        InterstitialAd.load(
            v.context,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback(){
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitial = interstitialAd
                    showAds()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    interstitial = null

                }
            }
        )
    }

    private fun getClicksCounter(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("MyCounterPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("counter", 0)
    }

    private fun updateClicksCounter(context: Context) {
        val retrievedCounter = getClicksCounter(context)
        val counter = retrievedCounter + 1
        val sharedPreferences = context.getSharedPreferences("MyCounterPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("counter", counter)
        editor.apply()
    }

    private fun isUserPremium(email: String, listener: OnUserFetchedListener) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.documents[0].get("premium") as? Boolean
                    listener.onUserFetched(user)
                } else {
                    listener.onUserFetched(null)
                }
            }
            .addOnFailureListener {
                listener.onUserFetched(null)
            }
    }

    interface OnUserFetchedListener {
        fun onUserFetched(user: Boolean?)
    }

    private fun showBottomNav() {
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        showBottomNav()
        binding.switchToHomeCreator.isChecked = true
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