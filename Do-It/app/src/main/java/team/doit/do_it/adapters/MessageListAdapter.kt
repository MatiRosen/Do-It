package team.doit.do_it.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import team.doit.do_it.R
import team.doit.do_it.entities.MessageEntity
import team.doit.do_it.holders.MessageHolder
import java.util.Calendar
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
        holder.setMessage(model.message, model.sender)
        holder.setSender(model.sender)
        holder.setDate(model.date, model.sender)
        holder.setDay(model.date, isDateChange(position))
    }

    private fun isDateChange(position: Int): Boolean {
        if (position == 0) return true

        val currentCalendar = Calendar.getInstance()
        currentCalendar.time = Date(getItem(position).date)
        val previousCalendar = Calendar.getInstance()
        previousCalendar.time = Date(getItem(position - 1).date)

        return currentCalendar.get(Calendar.YEAR) != previousCalendar.get(Calendar.YEAR) ||
                currentCalendar.get(Calendar.MONTH) != previousCalendar.get(Calendar.MONTH) ||
                currentCalendar.get(Calendar.DAY_OF_MONTH) != previousCalendar.get(Calendar.DAY_OF_MONTH)
    }
}