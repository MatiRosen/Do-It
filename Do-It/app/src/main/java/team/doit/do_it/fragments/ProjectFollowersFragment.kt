package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import team.doit.do_it.R
import team.doit.do_it.adapters.UserFollowerListAdapter
import team.doit.do_it.databinding.FragmentProjectFollowersBinding
import team.doit.do_it.entities.ChatEntity
import team.doit.do_it.entities.InvestEntity
import team.doit.do_it.entities.UserEntity
import team.doit.do_it.listeners.OnItemViewClickListener
import team.doit.do_it.listeners.OnViewItemClickedListener

class ProjectFollowersFragment : Fragment(), OnViewItemClickedListener<UserEntity>, OnItemViewClickListener<UserEntity>  {

    private var _binding: FragmentProjectFollowersBinding? = null
    private val binding get() = _binding!!

    private lateinit var v: View
    private val db = FirebaseFirestore.getInstance()

    private lateinit var userFollowerListAdapter : UserFollowerListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProjectFollowersBinding.inflate(inflater, container, false)
        v = binding.root

        return v
    }

    override fun onStart() {
        super.onStart()
        safeAccessBinding {
            setupRecyclerView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(item: UserEntity) {
        goToChat(item)
    }

    override fun onViewItemDetail(item: UserEntity) {
        TODO("Not yet implemented")
    }

    private fun setupRecyclerView(){
        val project = ProjectFollowersFragmentArgs.fromBundle(requireArguments()).project

        val query = db.collection("users").whereIn("email", project.followers)
        val config = PagingConfig(20, 10, false)

        val options = FirestorePagingOptions.Builder<UserEntity>()
            .setLifecycleOwner(this)
            .setQuery(query, config, UserEntity::class.java)
            .build()

        setupRecyclerViewSettings(binding.recyclerProjectFollowers)

        userFollowerListAdapter = UserFollowerListAdapter(options, this, this)
        binding.recyclerProjectFollowers.adapter = userFollowerListAdapter

        query.get().addOnSuccessListener {
            if (it.isEmpty) {
                binding.progressBarProjectFollowers.visibility = View.GONE
                Toast.makeText(context, resources.getString(R.string.project_followers_no_followers), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToChat(otherUser : UserEntity){
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val ref = Firebase.database.getReference("messages/${ownUserUUID}/${otherUser.uuid}/messages")
        ref.get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result?.children?.count() == 0) {
                    createUserChat(ownUserUUID, otherUser)
                    return@addOnCompleteListener
                }

                openUserChat(ownUserUUID, otherUser.uuid)
            }
        }

    }

    private fun createUserChat(ownUserUUID : String, user : UserEntity){
        val ref = Firebase.database.getReference("messages/${ownUserUUID}/${user}")
        ref.child("userName").setValue("${user.firstName} ${user.surname}")
        ref.child("userImage").setValue(user.userImage)
        ref.child("userEmail").setValue(user.email)
        ref.child("userUUID").setValue(user.uuid)
        val currentTime = System.currentTimeMillis()
        ref.child("lastMessageDate").setValue(-currentTime)

        val chat = ChatEntity(
            "${user.firstName} ${user.surname}",
            user.email,
            user.userImage,
            user.uuid,
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

    private fun setupRecyclerViewSettings(recycler : RecyclerView) {
        recycler.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        recycler.layoutManager = linearLayoutManager
    }

    private fun safeAccessBinding(action : () -> Unit){
        if(_binding != null && context != null){
            action()
        }
    }
}