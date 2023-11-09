package team.doit.do_it.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import team.doit.do_it.R
import team.doit.do_it.entities.CommentEntity
import team.doit.do_it.holders.CommentHolder

class CommentListAdapter(
    private val messageList: MutableList<CommentEntity>
) : RecyclerView.Adapter<CommentHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)

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
    }
}