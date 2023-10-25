package team.doit.do_it.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import team.doit.do_it.R

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

    fun setLastMessageDate(lastDate: String) {
        val txt : TextView = view.findViewById(R.id.txtItemChatLastTime)
        txt.text = lastDate
    }

    fun setUserImage(userImage: String, userEmail: String) {
        val imageView = view.findViewById<com.mikhaellopez.circularimageview.CircularImageView>(R.id.imgChatProfileImage)

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

    fun getCardLayout(): View {
        return view.findViewById(R.id.cardViewItemChat)
    }
}