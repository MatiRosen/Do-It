package team.doit.do_it.listeners

import team.doit.do_it.entities.CommentEntity

interface RecyclerViewCommentsListener {
    fun onEditCommentClicked(comment: CommentEntity)
    fun onDeleteCommentClicked(comment: CommentEntity)
    fun onSavedCommentClicked(comment: CommentEntity)
}