package team.doit.do_it.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import team.doit.do_it.R
import team.doit.do_it.entities.ChatEntity
import team.doit.do_it.holders.ChatHolder
import team.doit.do_it.listeners.OnViewItemClickedListener
import java.util.Calendar
import java.util.Date

class ChatListAdapter(
    options: FirebaseRecyclerOptions<ChatEntity>,
    private val onItemClick: OnViewItemClickedListener
) : FirebaseRecyclerAdapter<ChatEntity, ChatHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)

        return ChatHolder(view)
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int, model: ChatEntity) {
        holder.setUserName(model.userName)
        val lastMessage = model.messages.lastOrNull()
        holder.setLastMessage(lastMessage?.message ?: "")
        holder.setLastMessageDate(lastMessage?.date ?: System.currentTimeMillis())
        holder.setUserImage(model.userImage, model.userEmail)
        holder.setWaiting(model.isWaiting)

        holder.getCardLayout().setOnClickListener {
            onItemClick.onViewItemDetail(model)
        }
    }
}