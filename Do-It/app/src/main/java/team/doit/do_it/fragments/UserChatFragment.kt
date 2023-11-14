package team.doit.do_it.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import team.doit.do_it.R
import team.doit.do_it.activities.MainActivity
import team.doit.do_it.adapters.MessageListAdapter
import team.doit.do_it.databinding.FragmentUserChatBinding
import team.doit.do_it.entities.ChatEntity
import team.doit.do_it.entities.MessageEntity
import team.doit.do_it.listeners.InsetsWithKeyboardCallback

class UserChatFragment : Fragment() {

    companion object {
        private const val BACKEND_URL = "https://enchanting-sprout-agate.glitch.me"
    }

    private lateinit var v : View

    private var _binding : FragmentUserChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseDatabase
    private lateinit var chat : ChatEntity
    private lateinit var messageListAdapter : MessageListAdapter
    private lateinit var insetsWithKeyboardCallback : InsetsWithKeyboardCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserChatBinding.inflate(inflater, container, false)
        v = binding.root
        chat = UserChatFragmentArgs.fromBundle(requireArguments()).chat
        db = Firebase.database

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startButtons()
        setBindings()

        val rootActivity = requireActivity().findViewById<ConstraintLayout>(R.id.frameLayoutMainActivity)
        insetsWithKeyboardCallback = InsetsWithKeyboardCallback(requireActivity().window, rootActivity)
        ViewCompat.setOnApplyWindowInsetsListener(rootActivity, insetsWithKeyboardCallback)
    }

    override fun onStart() {
        super.onStart()

        startChat()
    }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity() as MainActivity
        activity.hideBottomNav()
        activity.removeMargins()
    }

    private fun safeAccessBinding(action: () -> Unit) {
        if (_binding != null && context != null) {
            action()
        }
    }


    private fun startChat() {
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val ref = db.getReference("messages/${ownUserUUID}").child("${chat.userUUID}/messages")
        val query = ref.orderByChild("date")

        val options = FirebaseRecyclerOptions.Builder<MessageEntity>()
            .setLifecycleOwner(this)
            .setQuery(query, MessageEntity::class.java)
            .build()

        messageListAdapter = MessageListAdapter(options)
        binding.recyclerViewUserChat.adapter = messageListAdapter
        setupRecyclerViewSettings(binding.recyclerViewUserChat)

        messageListAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.progressBarUserChat.visibility = View.GONE
                binding.recyclerViewUserChat.scrollToPosition(messageListAdapter.itemCount - 1)
            }
        })

        ref.get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result?.children?.count() == 0) {
                    binding.progressBarUserChat.visibility = View.GONE
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
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigateUp()
            }, 100)

        }

        binding.imgBtnUserChatSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        var message = binding.editTxtUserChatMessage.text.toString()
        val otherUserUUID = chat.userUUID

        if (message == "") return

        message = message.trim()

        saveMessageOnDatabase(ownUserUUID, otherUserUUID, message, ownUserUUID)
        saveMessageOnDatabase(otherUserUUID, ownUserUUID, message, ownUserUUID)
        db.getReference("messages/${otherUserUUID}/${ownUserUUID}/waiting").setValue(true)

        getOtherUserToken(otherUserUUID){
            sendNotification(message, it)
        }
    }

    private fun sendNotification(message: String, token: String) {
        val url = "$BACKEND_URL/send-notification"

        val title = resources.getString(R.string.chat_new_message_notif)

        val mediaType = "application/json".toMediaType()
        val json = "{\"title\":\"$title\", \"body\":\"$message\", \"token\":\"$token\", \"fromFragment\":\"UserChatFragment\"}"
        val requestBody = json.toRequestBody(mediaType)

        val req = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        OkHttpClient().newCall(req).enqueue(object: okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {}
        })
    }

    private fun getOtherUserToken(uuid: String, action: (token: String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("uuid", uuid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.documents[0]
                    action(user.getString("fcmToken").toString())
                }
            }
    }

    private fun saveMessageOnDatabase(ownUserUUID : String, otherUserUUID : String, message : String, sender : String) {
        val ref = db.getReference("messages/${ownUserUUID}/${otherUserUUID}")

        ref.child("messages").get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result?.children?.count() == 0) {
                    createUserChat(ownUserUUID, otherUserUUID, message, sender)
                    return@addOnCompleteListener
                }

                val lastMessageKey = it.result?.children?.last()?.key?.toInt() ?: 0
                val currentTime = System.currentTimeMillis()
                ref.child("messages/${lastMessageKey + 1}").setValue(MessageEntity(message, sender, currentTime))
                ref.child("lastMessageDate").setValue(-currentTime)

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
                        ref.child("userName").setValue("${user.getString("firstName")} ${user.getString("surname")}")
                        ref.child("userImage").setValue(user.getString("userImage"))
                        ref.child("userEmail").setValue(user.getString("email"))
                        ref.child("userUUID").setValue(otherUserUUID)
                        val currentTime = System.currentTimeMillis()
                        ref.child("messages").child("0").setValue(MessageEntity(message, sender, currentTime))
                        ref.child("lastMessageDate").setValue(-currentTime)
                        ref.child("waiting").setValue(true)
                        binding.editTxtUserChatMessage.text.clear()
                    }
                }
            }
    }

    private fun setBindings() {
        binding.txtUserChatProfileName.text = chat.userName

        setUserImage()
    }

    private fun setUserImage() {
        val imageView = binding.imgUserChatProfileImage
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



    override fun onStop() {
        super.onStop()
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.getReference("messages/${ownUserUUID}/${chat.userUUID}/waiting").setValue(false)
        val activity = requireActivity() as MainActivity
        activity.showBottomNav()
        activity.showMargins()
        deleteChatIfNoMessages()
    }

    override fun onDestroy() {
        super.onDestroy()

        insetsWithKeyboardCallback.removeListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}