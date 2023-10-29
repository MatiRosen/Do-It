package team.doit.do_it.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.mikhaellopez.circularimageview.CircularImageView
import team.doit.do_it.R
import java.util.Calendar

class ChatHolder(view: View) : RecyclerView.ViewHolder(view) {

    private var view: View

    init {
        this.view = view
    }

    fun setUserName(userName: String) {
        val txt : TextView = view.findViewById(R.id.txtItemChatUserName)
        txt.text = userName
    }

    fun setLastMessage(lastMessage: String) {
        val txt : TextView = view.findViewById(R.id.txtItemChatLastMessage)
        txt.text = lastMessage
    }

    fun setLastMessageDate(lastDate: Long) {
        val txt : TextView = view.findViewById(R.id.txtItemChatLastTime)
        txt.text = humanizeTime(lastDate)
    }

    fun setUserImage(userImage: String, userEmail: String) {
        val imageView = view.findViewById<CircularImageView>(R.id.imgItemChatProfileImage)

        if (userImage == "") {
            imageView.setImageResource(R.drawable.img_avatar)
            return
        }

        val storageReference = FirebaseStorage.getInstance().reference
            .child("images/$userEmail/imgProfile/$userImage")


        Glide.with(view.context)
            .load(storageReference)
            .placeholder(R.drawable.img_avatar)
            .error(R.drawable.img_avatar)
            .into(imageView)

    }

    fun setWaiting(isWaiting: Boolean) {
        val img : ImageView = view.findViewById(R.id.imgItemChatNewMessage)
        if (isWaiting) {
            img.visibility = View.VISIBLE
            view.findViewById<CircularImageView>(R.id.imgItemChatProfileImage).borderColor = view.resources.getColor(R.color.pantone, null)
            view.findViewById<TextView>(R.id.txtItemChatUserName).setTextColor(view.resources.getColor(R.color.pantone, null))
        } else {
            img.visibility = View.GONE
        }
    }

    fun getCardLayout(): View {
        return view.findViewById(R.id.cardViewItemChat)
    }

    private fun humanizeTime(date: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date

        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_MONTH, -1)

        return when {
            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) -> {
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = if (calendar.get(Calendar.MINUTE) < 10) "0${calendar.get(Calendar.MINUTE)}" else calendar.get(Calendar.MINUTE)
                val amPm = if (hour < 12) "AM" else "PM"

                "${hour%12}:$minute $amPm"
            }
            calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == yesterday.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == yesterday.get(Calendar.DAY_OF_MONTH) -> {
                view.resources.getString(R.string.yesterday)
            }
            else -> {
                "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
            }
        }

    }
}