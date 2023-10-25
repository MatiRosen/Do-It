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
import com.google.firebase.database.FirebaseDatabase
import team.doit.do_it.adapters.ChatListAdapter
import team.doit.do_it.databinding.FragmentChatBinding
import team.doit.do_it.entities.ChatEntity
import team.doit.do_it.listeners.OnViewItemClickedListener

class ChatFragment : Fragment(), OnViewItemClickedListener {

    private var _binding : FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View

    private lateinit var db: FirebaseDatabase
    private lateinit var chatListAdapter : ChatListAdapter

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
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = db.getReference("messages/$ownUserUUID")

        val query = ref.orderByChild("date")
        val options = FirebaseRecyclerOptions.Builder<ChatEntity>()
            .setQuery(query, ChatEntity::class.java)
            .build()

        chatListAdapter = ChatListAdapter(options, this)
        binding.recyclerChats.adapter = chatListAdapter
        setupRecyclerViewSettings(binding.recyclerChats)
        binding.recyclerChats.visibility = View.VISIBLE
        binding.progressBarChat.visibility = View.GONE

        chatListAdapter.startListening()
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
        chatListAdapter.stopListening()
    }

    override fun onViewItemDetail(item: Any) {
        val chat = if (item is ChatEntity) item else return
        val action = ChatFragmentDirections.actionChatToUserChat(chat)
        this.findNavController().navigate(action)
    }
}