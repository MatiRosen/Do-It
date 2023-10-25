package team.doit.do_it.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import team.doit.do_it.R
import team.doit.do_it.entities.MessageEntity
import team.doit.do_it.holders.MessageHolder
import java.util.Date

class MessageListAdapter(
    options: FirebaseRecyclerOptions<MessageEntity>
) : FirebaseRecyclerAdapter<MessageEntity, MessageHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)

        return MessageHolder(view)
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int, model: MessageEntity) {
        holder.setMessage(model.message)
        holder.setSender(model.sender)
        holder.setDate(humanizeTime(model.date, holder))
    }

    private fun humanizeTime(time: Long, holder: MessageHolder): String {
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