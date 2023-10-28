package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.snapshots
import kotlinx.coroutines.flow.map
import team.doit.do_it.R
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

        return v
    }

    override fun onStart() {
        super.onStart()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = db.getReference("messages/$ownUserUUID")

        val query = ref.orderByChild("lastMessageDate")

        val options = FirebaseRecyclerOptions.Builder<ChatEntity>()
            .setQuery(query, ChatEntity::class.java)
            .build()

        chatListAdapter = ChatListAdapter(options, this)
        binding.recyclerChats.adapter = chatListAdapter
        setupRecyclerViewSettings(binding.recyclerChats)
        chatListAdapter.startListening()

        chatListAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.progressBarChat.visibility = View.GONE
            }
        })

        ref.get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result?.children?.count() == 0) {
                    binding.progressBarChat.visibility = View.GONE
                }
            }
        }
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
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.getReference("messages/$ownUserUUID/${chat.userUUID}/waiting").setValue(false)
        // TODO ver FCM de firebase para resolver cambiar el icono.
        this.findNavController().navigate(action)
    }
}