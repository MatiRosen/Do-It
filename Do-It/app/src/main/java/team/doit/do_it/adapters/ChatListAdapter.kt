package team.doit.do_it.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import team.doit.do_it.R
import team.doit.do_it.entities.ChatEntity
import team.doit.do_it.holders.ChatHolder
import team.doit.do_it.listeners.OnViewItemClickedListener
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
        holder.setLastMessageDate(humanizeTime(lastMessage?.date ?: System.currentTimeMillis(), holder))
        holder.setUserImage(model.userImage, model.userEmail)

        holder.getCardLayout().setOnClickListener {
            onItemClick.onViewItemDetail(model)
        }
    }

    private fun humanizeTime(time: Long, holder: ChatHolder): String {
        if (time == 0L) return ""
        val diff = Date().time - time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        val resources = holder.itemView.resources

        return when {
            days > 0 -> "$days ${if (days == 1L) resources.getString(R.string.day).lowercase() else resources.getString(R.string.days).lowercase()}"
            hours > 0 -> "$hours ${if (hours == 1L) resources.getString(R.string.hour).lowercase() else resources.getString(R.string.hours).lowercase()}"
            minutes > 0 -> "$minutes ${if (minutes == 1L) resources.getString(R.string.minute).lowercase() else resources.getString(R.string.minutes).lowercase()}"
            else -> resources.getString(R.string.now)
        }

    }
}