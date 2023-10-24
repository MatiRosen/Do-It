package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import team.doit.do_it.adapters.ChatListAdapter
import team.doit.do_it.databinding.FragmentChatBinding
import team.doit.do_it.entities.ChatEntity
import team.doit.do_it.listeners.OnViewItemClickedListener
import java.util.Date

class ChatFragment : Fragment(), OnViewItemClickedListener {

    private var _binding : FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View

    private lateinit var db: FirebaseDatabase

    private lateinit var concatAdapter : ConcatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        v = binding.root
        db = FirebaseDatabase.getInstance()
        binding.recyclerChats.visibility = View.GONE

        return v
    }

    override fun onStart() {
        super.onStart()
        setupButtons()
        setupRecyclerView()
    }

    private fun setupButtons() {

    }

    private fun setupRecyclerView() {
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = db.getReference("messages/$ownUserUUID")

        concatAdapter = ConcatAdapter()
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val references = mutableListOf<DatabaseReference>()
                for (user in snapshot.children) {
                    val userUUID = user.key ?: continue
                    references.add(db.getReference("messages/$ownUserUUID/$userUUID"))
                }

                for (reference in references) {
                    val query = reference.orderByChild("date").limitToLast(1)

                    val options = FirebaseRecyclerOptions.Builder<ChatEntity>()
                        .setQuery(query, ChatEntity::class.java)
                        .build()

                    val chatListAdapter = ChatListAdapter(options, this@ChatFragment)
                    concatAdapter.addAdapter(chatListAdapter)

                }

                binding.recyclerChats.adapter = concatAdapter
                setupRecyclerViewSettings(binding.recyclerChats)
                binding.recyclerChats.visibility = View.VISIBLE
                binding.progressBarChat.visibility = View.GONE

                concatAdapter.adapters.forEach { adapter ->
                    if (adapter is ChatListAdapter) {
                        adapter.startListening()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBarChat.visibility = View.GONE
                binding.recyclerChats.visibility = View.VISIBLE
            }
        })
    }

    private fun setupRecyclerViewSettings(recycler : RecyclerView) {
        recycler.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        recycler.layoutManager = linearLayoutManager
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        concatAdapter.adapters.forEach { adapter ->
            if (adapter is ChatListAdapter) {
                adapter.stopListening()
            }
        }
    }

    override fun onViewItemDetail(item: Any) {
        val chat = if (item is ChatEntity) item else return
        val action = ChatFragmentDirections.actionChatToUserChat(chat)
        this.findNavController().navigate(action)
    }
}