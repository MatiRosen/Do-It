package team.doit.do_it.holders

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.auth.FirebaseAuth
import team.doit.do_it.R
import java.util.Calendar

class MessageHolder(view: View) : RecyclerView.ViewHolder(view){

    private var view: View

    init {
        this.view = view
    }

    fun setMessage(message: String, sender: String) {
        if (sender == FirebaseAuth.getInstance().currentUser?.uid) {
            view.findViewById<TextView>(R.id.txtItemMessageRightMsg).text = message
        } else {
            view.findViewById<TextView>(R.id.txtItemMessageLeftMsg).text = message
        }
    }

    fun setSender(sender: String) {
        if (sender == FirebaseAuth.getInstance().currentUser?.uid) {
            view.findViewById<LinearLayout>(R.id.linearLayoutItemMessageLeft).visibility = View.GONE
            view.findViewById<FlexboxLayout>(R.id.linearLayoutItemMessageRight).visibility = View.VISIBLE
        } else {
            view.findViewById<FlexboxLayout>(R.id.linearLayoutItemMessageRight).visibility = View.GONE
            view.findViewById<LinearLayout>(R.id.linearLayoutItemMessageLeft).visibility = View.VISIBLE
        }
    }

    fun setDate(date: Long, sender: String) {
        if (sender == FirebaseAuth.getInstance().currentUser?.uid) {
            view.findViewById<TextView>(R.id.txtItemMessageRightDate).text = humanizeTime(date)
        } else {
            view.findViewById<TextView>(R.id.txtItemMessageLeftDate).text = humanizeTime(date)
        }
    }

    fun setDay(date: Long, isDateChange: Boolean) {
        if (isDateChange) {
            view.findViewById<TextView>(R.id.txtItemMessageCenterDate).text = getDate(date)
            view.findViewById<LinearLayout>(R.id.linearLayoutItemMessageCenter).visibility = View.VISIBLE
        } else {
            view.findViewById<LinearLayout>(R.id.linearLayoutItemMessageCenter).visibility = View.GONE
        }
    }

    private fun humanizeTime(time: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = if (calendar.get(Calendar.MINUTE) < 10) "0${calendar.get(Calendar.MINUTE)}" else calendar.get(Calendar.MINUTE)
        val amPm = if (hour < 12) "AM" else "PM"
        return "${hour%12}:$minute $amPm"
    }

    private fun getDate(date: Long) : String{
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date

        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_MONTH, -1)

        return when {
            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) -> view.resources.getString(R.string.today)
            calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == yesterday.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == yesterday.get(Calendar.DAY_OF_MONTH) -> view.resources.getString(R.string.yesterday)
            else -> "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
        }
    }
}