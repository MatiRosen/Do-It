package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.snapshots
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.count
import team.doit.do_it.R
import team.doit.do_it.adapters.MessageListAdapter
import team.doit.do_it.databinding.FragmentUserChatBinding
import team.doit.do_it.entities.ChatEntity
import team.doit.do_it.entities.MessageEntity

class UserChatFragment : Fragment() {

    private lateinit var v : View

    private var _binding : FragmentUserChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseDatabase
    private lateinit var chat : ChatEntity
    private lateinit var messageListAdapter : MessageListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserChatBinding.inflate(inflater, container, false)
        v = binding.root
        chat = UserChatFragmentArgs.fromBundle(requireArguments()).chat
        db = Firebase.database

        hideBottomNav()
        removeMargins()

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startButtons()
        setBindings()
    }

    override fun onStart() {
        super.onStart()

        startChat()
    }

    private fun safeAccessBinding(action: () -> Unit) {
        if (_binding != null) {
            action()
        }
    }

    private fun startChat() {
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val ref = db.getReference("messages/${ownUserUUID}").child("${chat.userUUID}/messages")
        val query = ref.orderByChild("date")

        val options = FirebaseRecyclerOptions.Builder<MessageEntity>()
            .setQuery(query, MessageEntity::class.java)
            .build()

        messageListAdapter = MessageListAdapter(options)
        binding.recyclerViewUserChat.adapter = messageListAdapter
        setupRecyclerViewSettings(binding.recyclerViewUserChat)
        //binding.recyclerViewUserChat.scrollToPosition(messageListAdapter.itemCount - 1)
        messageListAdapter.startListening()

        messageListAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.progressBaUserChat.visibility = View.GONE
            }
        })

        ref.get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result?.children?.count() == 0) {
                    binding.progressBaUserChat.visibility = View.GONE
                }
            }
        }
    }

    private fun setupRecyclerViewSettings(recycler : RecyclerView) {
        recycler.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        recycler.layoutManager = linearLayoutManager
    }

    private fun startButtons() {
        binding.imgBtnUserChatBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.imgBtnUserChatSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val message = binding.editTxtUserChatMessage.text.toString()
        val otherUserUUID = chat.userUUID

        if (message == "") return

        saveMessageOnDatabase(ownUserUUID, otherUserUUID, message, ownUserUUID)

        saveMessageOnDatabase(otherUserUUID, ownUserUUID, message, ownUserUUID)
    }

    private fun saveMessageOnDatabase(ownUserUUID : String, otherUserUUID : String, message : String, sender : String) {
        val ref = db.getReference("messages/${ownUserUUID}/${otherUserUUID}/messages")

        ref.get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result?.children?.count() == 0) {
                    createUserChat(ownUserUUID, otherUserUUID, message, sender)
                    return@addOnCompleteListener
                }

                val lastMessageKey = it.result?.children?.last()?.key?.toInt() ?: 0
                val currentTime = System.currentTimeMillis()
                ref.child("${lastMessageKey + 1}").setValue(MessageEntity(message, sender, currentTime))
                db.getReference("messages/${ownUserUUID}/${otherUserUUID}/lastMessageDate").setValue(-currentTime)

                binding.editTxtUserChatMessage.text.clear()
            }
        }
    }

    private fun createUserChat(ownUserUUID: String, otherUserUUID: String, message: String, sender: String){
        val ref = db.getReference("messages/${ownUserUUID}/${otherUserUUID}")
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("uuid", otherUserUUID)
            .get()
            .addOnSuccessListener { documents ->
                safeAccessBinding {
                    if (!documents.isEmpty) {
                        val user = documents.documents[0]
                        ref.child("userName").setValue("${user.getString("nombre")} ${user.getString("apellido")}")
                        ref.child("userImage").setValue(user.getString("imgPerfil"))
                        ref.child("userEmail").setValue(user.getString("email"))
                        ref.child("userUUID").setValue(otherUserUUID)
                        val currentTime = System.currentTimeMillis()
                        ref.child("messages").child("0").setValue(MessageEntity(message, sender, currentTime))
                        ref.child("lastMessageDate").setValue(-currentTime)
                        binding.editTxtUserChatMessage.text.clear()
                    }
                }
            }
    }

    private fun setBindings() {
        binding.txtProjectDetailCreatorProfileName.text = chat.userName

        setUserImage()
    }

    private fun setUserImage() {
        val imageView = binding.imgProjectDetailCreatorProfileImage
        val userImage = chat.userImage
        val userEmail = chat.userEmail

        if (userImage == "") {
            imageView.setImageResource(R.drawable.img_avatar)
            return
        }

        val storageReference = FirebaseStorage.getInstance().reference
            .child("images/$userEmail/imgProfile/$userImage")


        Glide.with(this)
            .load(storageReference)
            .placeholder(R.drawable.img_avatar)
            .error(R.drawable.img_avatar)
            .into(imageView)
    }

    private fun hideBottomNav() {
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE
    }

    private fun removeMargins() {
        requireActivity().findViewById<FragmentContainerView>(R.id.mainHost)
            .layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun showBottomNav() {
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.VISIBLE
    }

    private fun showMargins() {
        val constraintSet = ConstraintSet()
        constraintSet.connect(R.id.mainHost, ConstraintSet.TOP, R.id.guidelineMainActivityHorizontal3, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.mainHost, ConstraintSet.BOTTOM, R.id.bottomNavigationView, ConstraintSet.TOP)
        constraintSet.connect(R.id.mainHost, ConstraintSet.START, R.id.guidelineMainActivityVertical2, ConstraintSet.END)
        constraintSet.connect(R.id.mainHost, ConstraintSet.END, R.id.guidelineMainActivityVertical98, ConstraintSet.START)


        constraintSet.applyTo(requireActivity().findViewById(R.id.frameLayoutMainActivity))
    }

    private fun deleteChatIfNoMessages() {
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = db.getReference("messages/${ownUserUUID}/${chat.userUUID}/messages")

        ref.get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result?.children?.count() == 0) {
                    ref.parent?.removeValue()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideBottomNav()
        removeMargins()
    }

    override fun onStop() {
        super.onStop()
        showBottomNav()
        showMargins()
        deleteChatIfNoMessages()
        messageListAdapter.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}