package team.doit.do_it.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import team.doit.do_it.R
import team.doit.do_it.entities.CommentEntity
import team.doit.do_it.holders.CommentHolder
import team.doit.do_it.listeners.RecyclerViewCommentsListener

class CommentListAdapter(
    private val messageList: MutableList<CommentEntity>,
    private var listener: RecyclerViewCommentsListener

) : RecyclerView.Adapter<CommentHolder>(){

    private lateinit var userEmail: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)

        userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()

        return CommentHolder(view)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        val model = messageList[position]
        holder.setCommentAuthorName(model.userName)
        holder.setCommentContent(model.commentText)
        holder.setCommentUserImage(model.userImage, model.userEmail)
        holder.setCommentDate(model.commentDate)
        holder.getBtnDeleteComment()?.setOnClickListener {
            listener.onDeleteCommentClicked(model)
        }
        holder.getBtnEditComment()?.setOnClickListener {
            holder.editComment()
            listener.onEditCommentClicked(model)
        }
        holder.getBtnSaveComment()?.setOnClickListener {
            holder.saveComment()
            if(holder.getCommentContent().isNotBlank() || holder.getCommentContent().isNotEmpty()) {
                model.commentText = holder.getCommentContent()
                listener.onSavedCommentClicked(model)
            }
        }
        if(model.userEmail != userEmail) {
            holder.getBtnDeleteComment()?.visibility = ViewGroup.GONE
            holder.getBtnEditComment()?.visibility = ViewGroup.GONE
        }
    }
}