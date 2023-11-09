package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Filter.or
import com.google.firebase.firestore.FirebaseFirestore
import team.doit.do_it.R
import team.doit.do_it.adapters.InvestAdapter
import team.doit.do_it.databinding.FragmentMyInvestmentsBinding
import team.doit.do_it.entities.InvestEntity
import team.doit.do_it.holders.InvestHolder
import team.doit.do_it.listeners.OnBindViewHolderListener
import team.doit.do_it.listeners.OnViewItemClickedListener

class MyInvestmentsFragment : Fragment(), OnViewItemClickedListener<InvestEntity>, OnBindViewHolderListener<InvestHolder, InvestEntity> {

    private var _binding : FragmentMyInvestmentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var v : View
    private val db = FirebaseFirestore.getInstance()

    private lateinit var investAdapter : InvestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyInvestmentsBinding.inflate(inflater, container, false)

        v = binding.root

        return v
    }

    override fun onStart(){
        super.onStart()
        overrideBackButton()
        binding.progressBarMyInvestments.visibility = View.VISIBLE
        safeAccessBinding {
            setupRecyclerView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewItemDetail(item: InvestEntity) {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email?: return

        /*if (invest.creatorEmail == userEmail) {
            val navController = findNavController()
            //val action = InvestFragmentDirections.actionInvestFragmentToInvestDetailFragment(invest)
            //navController.navigate(action)
        } else {
            Toast.makeText(context, resources.getString(R.string.invest_not_creator), Toast.LENGTH_SHORT).show()
        }*/
    }

    override fun onBindViewHolderSucceed(holder: InvestHolder, item: InvestEntity) {
        db.collection("ideas")
            .document(item.projectID)
            .get()
            .addOnSuccessListener { projectDoc ->
                item.projectTitle = projectDoc.getString("title") ?: ""

                val creatorEmail = projectDoc.getString("creatorEmail") ?: return@addOnSuccessListener
                val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return@addOnSuccessListener
                val otherUserEmail = if (userEmail == creatorEmail) item.investorEmail else creatorEmail

                db.collection("usuarios")
                    .document(otherUserEmail)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        safeAccessBinding {
                            item.userName = "${userDoc.getString("nombre") ?: ""} ${userDoc.getString("apellido") ?: ""}"

                            investAdapter.setInvestExtraData(holder, item)
                            if (investAdapter.isAllDataLoaded()) {
                                binding.progressBarMyInvestments.visibility = View.GONE
                            }
                        }
                    }
            }
    }

    // TODO preguntar si está bien lo que hice o si mejor uso el adapter normal pasandole la lista
    // de inversiones ya creada
    private fun setupRecyclerView() {
        val query = FirebaseAuth.getInstance().currentUser?.email?.let {
            db.collection("inversiones")
                .where(
                    or(
                        Filter.equalTo("creatorEmail", it),
                        Filter.equalTo("investorEmail", it))
                )
        }

        val config = PagingConfig(20, 10, false)

        val options = FirestorePagingOptions.Builder<InvestEntity>()
            .setLifecycleOwner(this)
            .setQuery(query!!, config, InvestEntity::class.java)
            .build()

        setupRecyclerViewSettings(binding.recyclerMyInvestments)

        investAdapter = InvestAdapter(options, this, this, resources)
        binding.recyclerMyInvestments.adapter = investAdapter
    }

    private fun setupRecyclerViewSettings(recycler : RecyclerView) {
        recycler.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        recycler.layoutManager = linearLayoutManager
    }

    private fun safeAccessBinding(action: () -> Unit) {
        if (_binding != null && context != null) {
            action()
        }
    }

    private fun overrideBackButton() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = findNavController()
                navController.navigate(R.id.action_myInvestmentsFragment_to_options)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }
}