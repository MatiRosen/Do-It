package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Filter.or
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import team.doit.do_it.R
import team.doit.do_it.adapters.InvestAdapter
import team.doit.do_it.databinding.FragmentMyInvestmentsBinding
import team.doit.do_it.entities.ChatEntity
import team.doit.do_it.entities.InvestEntity
import team.doit.do_it.holders.InvestHolder
import team.doit.do_it.listeners.OnBindViewHolderListener
import team.doit.do_it.listeners.OnInvestViewClickListener
import team.doit.do_it.listeners.OnViewItemClickedListener

class MyInvestmentsFragment : Fragment(), OnViewItemClickedListener<InvestEntity>, OnBindViewHolderListener<InvestHolder, InvestEntity>, OnInvestViewClickListener<InvestEntity> {

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

        if (item.creatorEmail == userEmail) {
            val navController = findNavController()
            val action = MyInvestmentsFragmentDirections.actionMyInvestmentsFragmentToInvestSelectStatusFragment(item)
            navController.navigate(action)
        } else {
            Toast.makeText(context, resources.getString(R.string.my_investments_not_creator), Toast.LENGTH_SHORT).show()
        }
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

    override fun onItemClick(item: InvestEntity) {
        binding.progressBarMyInvestments.visibility = View.VISIBLE
        binding.recyclerMyInvestments.visibility = View.GONE
        goToChat(item)
    }

    // TODO preguntar si está bien lo que hice o si mejor uso el adapter normal pasandole la lista
    // de inversiones ya creada
    private fun setupRecyclerView() {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email?: return

        val query = db.collection("inversiones")
            .where(
                or(
                    Filter.equalTo("creatorEmail", userEmail),
                    Filter.equalTo("investorEmail", userEmail)
                )
            )

        val config = PagingConfig(20, 10, false)

        val options = FirestorePagingOptions.Builder<InvestEntity>()
            .setLifecycleOwner(this)
            .setQuery(query, config, InvestEntity::class.java)
            .build()

        setupRecyclerViewSettings(binding.recyclerMyInvestments)

        investAdapter = InvestAdapter(options, this, this, this, resources)
        binding.recyclerMyInvestments.adapter = investAdapter

        query.get().addOnSuccessListener {
            if (it.isEmpty) {
                binding.progressBarMyInvestments.visibility = View.GONE
                Toast.makeText(context, resources.getString(R.string.my_investments_no_data), Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun goToChat(invest : InvestEntity){
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ownEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val otherUserEmail = if (ownEmail == invest.creatorEmail) invest.investorEmail else invest.creatorEmail

        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("email", otherUserEmail)
            .get()
            .addOnSuccessListener { documents ->
                safeAccessBinding {
                    if (!documents.isEmpty) {
                        val user = documents.documents[0]
                        val otherUserUUID = user.getString("uuid").toString()
                        val ref = Firebase.database.getReference("messages/${ownUserUUID}/${otherUserUUID}/messages")

                        ref.get().addOnCompleteListener {
                            if (it.isSuccessful) {
                                if (it.result?.children?.count() == 0) {
                                    createUserChat(ownUserUUID, user)
                                    return@addOnCompleteListener
                                }

                                openUserChat(ownUserUUID, otherUserUUID)
                            }
                        }
                    }
                }
            }
    }

    private fun createUserChat(ownUserUUID : String, user : DocumentSnapshot){
        val otherUserUUID = user.getString("uuid").toString()
        val otherUserEmail = user.getString("email").toString()
        val ref = Firebase.database.getReference("messages/${ownUserUUID}/${otherUserUUID}")
        ref.child("userName").setValue("${user.getString("nombre")} ${user.getString("apellido")}")
        ref.child("userImage").setValue(user.getString("imgPerfil"))
        ref.child("userEmail").setValue(user.getString("email"))
        ref.child("userUUID").setValue(otherUserUUID)
        val currentTime = System.currentTimeMillis()
        ref.child("lastMessageDate").setValue(-currentTime)

        val chat = ChatEntity(
            "${user.getString("nombre")} ${user.getString("apellido")}",
            otherUserEmail,
            user.getString("imgPerfil")!!,
            user.getString("uuid")!!,
            mutableListOf(),
            currentTime,
            false)

        val action = ProjectDetailInvestorFragmentDirections.actionProjectDetailInvestorFragmentToUserChatHome(chat)
        this.findNavController().navigate(action)
    }

    private fun openUserChat(ownUserUUID : String, otherUserUUID : String){
        val ref = Firebase.database.getReference("messages/$ownUserUUID/$otherUserUUID")

        ref.get().addOnSuccessListener {
            if (it.exists()) {
                val chat = ChatEntity(
                    it.child("userName").value.toString(),
                    it.child("userEmail").value.toString(),
                    it.child("userImage").value.toString(),
                    it.child("userUUID").value.toString(),
                    mutableListOf(),
                    it.child("lastMessageDate").value.toString().toLong(),
                    it.child("waiting").value.toString().toBoolean()
                )
                safeAccessBinding {
                    val action = MyInvestmentsFragmentDirections.actionMyInvestmentsFragmentToUserChat(chat)
                    this.findNavController().navigate(action)
                }
            }
        }
    }
}