package team.doit.do_it.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.mikhaellopez.circularimageview.CircularImageView
import team.doit.do_it.R

class CommentHolder(view: View) : RecyclerView.ViewHolder(view){

    private var view: View

    init {
        this.view = view
    }

    fun setCommentAuthorName(authorName: String) {
        val txt : TextView = view.findViewById(R.id.textViewItemCommentAuthor)
        txt.text = authorName
    }

    fun setCommentContent(commentContent: String) {
        val txt : TextView = view.findViewById(R.id.textViewItemCommentContent)
        txt.text = commentContent
    }

    fun setCommentUserImage(userImage: String, userEmail: String) {
        val imageView = view.findViewById<CircularImageView>(R.id.imgItemCommentProfileImage)

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

    fun setCommentDate(commentDate: String) {
        val txt : TextView = view.findViewById(R.id.textViewItemCommentDate)
        txt.text = commentDate
    }

}