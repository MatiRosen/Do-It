package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import team.doit.do_it.R
import team.doit.do_it.adapters.ChatListAdapter
import team.doit.do_it.databinding.FragmentChatBinding
import team.doit.do_it.entities.ChatEntity
import team.doit.do_it.listeners.OnViewItemClickedListener

class ChatFragment : Fragment(), OnViewItemClickedListener<ChatEntity> {

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
        checkMessages()
    }

    private fun checkMessages(){
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.getReference("messages/$ownUserUUID").get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (data in it.result!!.children) {
                        val chat = data.getValue(ChatEntity::class.java)
                        if (chat != null && chat.isWaiting) {
                            return@addOnCompleteListener
                        }
                    }
                    safeActivityCall{
                        val mainActivity = requireActivity()
                        val bottomMenu = mainActivity.findViewById<BottomNavigationView>(R.id.bottomNavigationView).menu
                        bottomMenu.findItem(R.id.chat).icon = AppCompatResources.getDrawable(requireContext(), R.drawable.icon_chat)
                    }
                }
            }
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

    private fun safeActivityCall(action: () -> Unit) {
        try{
            if (!requireActivity().isFinishing && !requireActivity().isDestroyed ) {
                action()
            }
        } catch (_: IllegalStateException) {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        chatListAdapter.stopListening()
    }

    override fun onViewItemDetail(item: ChatEntity) {
        val action = ChatFragmentDirections.actionChatToUserChat(item)
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.getReference("messages/$ownUserUUID/${item.userUUID}/waiting").setValue(false)

        this.findNavController().navigate(action)
    }
}