package team.doit.do_it.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import team.doit.do_it.R
import team.doit.do_it.entities.ChatEntity
import team.doit.do_it.entities.UserEntity
import team.doit.do_it.holders.ChatHolder
import team.doit.do_it.listeners.OnViewItemClickedListener
import team.doit.do_it.repositories.UserRepository

class ChatListAdapter(
    options: FirebaseRecyclerOptions<ChatEntity>,
    private val onItemClick: OnViewItemClickedListener<ChatEntity>,
    private val userRepository: UserRepository
) : FirebaseRecyclerAdapter<ChatEntity, ChatHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)

        return ChatHolder(view)
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int, model: ChatEntity) {
        val lastMessage = model.messages.lastOrNull()
        holder.setLastMessage(lastMessage?.message ?: "")
        holder.setLastMessageDate(lastMessage?.date ?: System.currentTimeMillis())
        holder.setWaiting(model.isWaiting)

        val cardLayout = holder.getCardLayout()

        cardLayout.setOnClickListener {
            onItemClick.onViewItemDetail(model)
        }

        cardLayout.visibility = ViewGroup.GONE

        // Para no acceder a la base de datos desde el adapter usamos el userRepository.
        try {
            userRepository.getUser(model.userEmail).addOnSuccessListener {
                val user = it.toObject(UserEntity::class.java)
                if (user != null) {
                    model.userName = "${user.firstName} ${user.surname}"
                    model.userImage = user.userImage
                    holder.setUserName(model.userName)
                    holder.setUserImage(model.userImage, model.userEmail)
                }
                cardLayout.visibility = ViewGroup.VISIBLE
            }
        } catch (_: Exception) {

        }

    }
}