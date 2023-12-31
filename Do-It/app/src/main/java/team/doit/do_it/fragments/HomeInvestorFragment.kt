package team.doit.do_it.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
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
import com.algolia.search.client.ClientSearch
import com.algolia.search.dsl.attributesToRetrieve
import com.algolia.search.dsl.settings
import com.algolia.search.helper.deserialize
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import kotlinx.serialization.Serializable


class HomeInvestorFragment : Fragment(), OnViewItemClickedListener<ProjectEntity> {

    private var _binding : FragmentHomeInvestorBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View
    private val db = FirebaseFirestore.getInstance()
    private lateinit var popularProjectListAdapter: ProjectListAdapter
    private lateinit var allProjectListAdapter: ProjectListAdapter
    private var interstitial: InterstitialAd? = null
    private val quantityClicksToShowAds: Int = 3
    private val applicationID = ApplicationID("6BNVZCZWQH")
    private val apiKey = APIKey("b3dd3d1872044a4f101752fa0fd0377e")
    private val algoliaClient = ClientSearch(applicationID, apiKey)
    private val index = algoliaClient.initIndex(indexName = IndexName("ideas"))
    private lateinit var results :List<SearchResult>
    @Serializable
    data class SearchResult(
        val title: String,
        val uuid: String
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeInvestorBinding.inflate(inflater, container, false)
        v = binding.root
        binding.progressBarHomeInvestor.visibility = View.GONE
        binding.progressBarHomeInvestorBottom.visibility = View.GONE
        startSpinner()
        return v
    }

    private fun safeAccessBinding(action: () -> Unit) {
        if (_binding != null && context != null) {
            action()
        }
    }

    override fun onStart() {
        super.onStart()
        safeAccessBinding {
            setupPopularProjectsRecyclerView()
            setupAllProjectsRecyclerView()
        }

        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        binding.switchHomeInvestorToHomeCreator.isChecked = true
    }

    private fun setupButtons() {
        binding.switchHomeInvestorToHomeCreator.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                val action = HomeInvestorFragmentDirections.actionHomeInvestorFragmentToHomeCreatorFragment()
                this.findNavController().navigate(action)
            }
        }

        binding.searchViewHomeInvestor.setOnClickListener{
            val phoneticSearch = binding.searchViewHomeInvestor.query.toString()
            if (phoneticSearch == ""){
                binding.txtHomeInvestorTitle.visibility = View.VISIBLE
                binding.recyclerHomeInvestorPopularProjects.visibility = View.VISIBLE
                Snackbar.make(v, resources.getString(R.string.home_investor_search_error), Snackbar.LENGTH_LONG).show()
                setupAllProjectsRecyclerView()
            }else{
                binding.txtHomeInvestorTitle.visibility = View.GONE
                binding.recyclerHomeInvestorPopularProjects.visibility = View.GONE
                filterAllProjectsByPhoneticSearch()
            }
            binding.spinnerHomeInvestorFilterCategory.setSelection(0)
        }

        binding.btnHomeInvestorFilterCategory.setOnClickListener {
            binding.txtHomeInvestorTitle.visibility = View.GONE
            binding.recyclerHomeInvestorPopularProjects.visibility = View.GONE
            val categoryFilter = binding.spinnerHomeInvestorFilterCategory.selectedItem.toString()

            if (categoryFilter == resources.getString(R.string.project_creation_project_category_hint)){
                binding.txtHomeInvestorTitle.visibility = View.VISIBLE
                binding.recyclerHomeInvestorPopularProjects.visibility = View.VISIBLE
                Snackbar.make(v, resources.getString(R.string.home_investor_filter_category_error), Snackbar.LENGTH_LONG).show()
                setupAllProjectsRecyclerView()
            }else{
                binding.searchViewHomeInvestor.setQuery("",false)
                filterAllProjectsByCategory()
            }
        }
    }

    private fun setupPopularProjectsRecyclerView() {
        val query = db.collection("ideas")
            .orderBy("followersCount", Query.Direction.DESCENDING)

        val config = PagingConfig(5,1,  false)

        val options = FirestorePagingOptions.Builder<ProjectEntity>()
            .setLifecycleOwner(this)
            .setQuery(query, config, ProjectEntity::class.java)
            .build()


        setupRecyclerViewSettings(binding.recyclerHomeInvestorPopularProjects, true)
        popularProjectListAdapter = ProjectListAdapter(options, this)

        popularProjectListAdapter.startListening()
        binding.recyclerHomeInvestorPopularProjects.adapter = popularProjectListAdapter
    }

    private fun setupAllProjectsRecyclerView() {
        val query = db.collection("ideas").orderBy("creationDate", Query.Direction.DESCENDING)

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
        val categorySearch = binding.spinnerHomeInvestorFilterCategory.selectedItem.toString()
        val query = db.collection("ideas").whereEqualTo("category", categorySearch)

        val config = PagingConfig(20, 10, false)
        val options = FirestorePagingOptions.Builder<ProjectEntity>()
            .setLifecycleOwner(this)
            .setQuery(query, config, ProjectEntity::class.java)
            .build()

        allProjectListAdapter.updateOptions(options)
        allProjectListAdapter.startListening()
        binding.recyclerHomeInvestorAllProjects.adapter = allProjectListAdapter
    }
    private fun filterAllProjectsByPhoneticSearch(){
        allProjectListAdapter.stopListening()
        val phoneticSearch = binding.searchViewHomeInvestor.query.toString()
        binding.progressBarHomeInvestor.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            searchAlgolia(phoneticSearch){
                val titleList:List<String> = results.map { result -> result.title }
                if(titleList.isEmpty()){
                    Toast.makeText(context, resources.getString(R.string.home_investor_filter_no_result_error), Toast.LENGTH_SHORT).show()
                    return@searchAlgolia
                }

                val query = db.collection("ideas")
                    .whereIn("title",titleList)
                    .orderBy("title",Query.Direction.DESCENDING)

                val config = PagingConfig(20, 10, false)
                val options = FirestorePagingOptions.Builder<ProjectEntity>()
                    .setLifecycleOwner(this@HomeInvestorFragment)
                    .setQuery(query, config, ProjectEntity::class.java)
                    .build()

                allProjectListAdapter.updateOptions(options)
                allProjectListAdapter.startListening()
                binding.recyclerHomeInvestorAllProjects.adapter = allProjectListAdapter
                binding.progressBarHomeInvestor.visibility = View.GONE
            }
        }
    }

    private fun setupRecyclerViewSettings(recycler : RecyclerView, isHorizontal : Boolean = false) {
        recycler.setHasFixedSize(true)
        val linearLayoutManager = if (isHorizontal) LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) else LinearLayoutManager(context)
        recycler.layoutManager = linearLayoutManager
    }

    private fun changeBottomProgressBarVisibility(visibility: Int) {
        binding.progressBarHomeInvestorBottom.visibility = visibility
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.constraintLayoutHomeInvestor)

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

        constraintSet.applyTo(binding.constraintLayoutHomeInvestor)
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

                when(loadStates.append){
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
                }
            }
        }
    }

    override fun onViewItemDetail(item: ProjectEntity) {
        val investorEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        val action = if (item.creatorEmail == investorEmail) {
            HomeInvestorFragmentDirections.actionGlobalProjectDetailFragment(item)
        } else {
            HomeInvestorFragmentDirections.actionGlobalProjectDetailInvestorFragment(item)
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
        binding.spinnerHomeInvestorFilterCategory.adapter = adapter
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
        val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()

        // TODO meter el id en un archivo de configuracion
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
                    val user = documents.documents[0].get("isPremium") as? Boolean
                    listener.onUserFetched(user)
                } else {
                    listener.onUserFetched(null)
                }
            }
            .addOnFailureListener {
                listener.onUserFetched(null)
            }
    }

    private suspend fun searchAlgolia(queryText: String,action: () -> Unit) {
        val settings = settings {
            attributesToRetrieve {
                +"title"
                +"uuid"
            }
        }
        index.setSettings(settings)
        val response = index.run {
            search(com.algolia.search.model.search.Query(queryText))
        }
        val resultsAlgolia: List<SearchResult> = response.hits.deserialize(SearchResult.serializer())
        results = resultsAlgolia
        action()
    }

    interface OnUserFetchedListener {
        fun onUserFetched(user: Boolean?)
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