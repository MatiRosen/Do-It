package team.doit.do_it.holders

import android.view.View
import android.widget.ImageView
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

    fun getBtnEditComment() : ImageView? {
        return view.findViewById(R.id.btnViewItemCommentEdit)
    }

    fun getBtnDeleteComment() : ImageView? {
        return view.findViewById(R.id.btnViewItemCommentDelete)
    }

    fun getBtnSaveComment() : ImageView? {
        return view.findViewById(R.id.btnViewItemCommentSave)
    }

    fun editComment() {
        hideButtons()

        val txt : TextView = view.findViewById(R.id.textViewItemCommentContent)
        txt.isEnabled = true
    }

    fun saveComment() {
        val txt : TextView = view.findViewById(R.id.textViewItemCommentContent)
        txt.isEnabled = false

        setCommentContent(txt.text.toString())

        showButtons()
    }

    private fun hideButtons() {
        view.findViewById<ImageView>(R.id.btnViewItemCommentDelete).visibility = View.GONE
        view.findViewById<ImageView>(R.id.btnViewItemCommentEdit).visibility = View.GONE
        view.findViewById<ImageView>(R.id.btnViewItemCommentSave).visibility = View.VISIBLE
    }

    private fun showButtons() {
        view.findViewById<ImageView>(R.id.btnViewItemCommentDelete).visibility = View.VISIBLE
        view.findViewById<ImageView>(R.id.btnViewItemCommentEdit).visibility = View.VISIBLE
        view.findViewById<ImageView>(R.id.btnViewItemCommentSave).visibility = View.GONE
    }

    fun getCommentContent() : String {
        val txt : TextView = view.findViewById(R.id.textViewItemCommentContent)
        return txt.text.toString()
    }
}