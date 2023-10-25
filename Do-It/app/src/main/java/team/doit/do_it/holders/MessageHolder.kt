package team.doit.do_it.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import team.doit.do_it.R

class MessageHolder(view: View) : RecyclerView.ViewHolder(view){

    private var view: View

    init {
        this.view = view
    }

    fun setMessage(message: String) {
        val txt : TextView = view.findViewById(R.id.txtItemMessageMsg)
        txt.text = message
    }

    fun setSender(sender: String) {
        val card : View = view.findViewById(R.id.cardViewItemMessage)
        if (sender == FirebaseAuth.getInstance().currentUser?.uid) {
            card.setBackgroundColor(view.resources.getColor(R.color.green, null))
        } else {
            card.setBackgroundColor(view.resources.getColor(R.color.pantone, null))
        }
    }

    fun setDate(date: String) {
        val txt : TextView = view.findViewById(R.id.txtItemMessageDate)
        txt.text = date
    }
}