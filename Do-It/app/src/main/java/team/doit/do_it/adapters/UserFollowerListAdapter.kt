package team.doit.do_it.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import team.doit.do_it.R
import team.doit.do_it.entities.UserEntity
import team.doit.do_it.holders.UserFollowerHolder
import team.doit.do_it.listeners.OnItemViewClickListener
import team.doit.do_it.listeners.OnViewItemClickedListener

class UserFollowerListAdapter(
    options : FirestorePagingOptions<UserEntity>,
    private val onItemViewClick: OnViewItemClickedListener<UserEntity>,
    private val onItemClick: OnItemViewClickListener<UserEntity>
) : FirestorePagingAdapter<UserEntity, UserFollowerHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserFollowerHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_follower, parent, false)

        return UserFollowerHolder(view)
    }

    override fun onBindViewHolder(holder: UserFollowerHolder, position: Int, model: UserEntity) {
        holder.setUserName(model.firstName + " " + model.surname)
        holder.setUserImage(model.userImage, model.email)

        holder.getCardLayout().setOnClickListener {
            onItemViewClick.onViewItemDetail(model)
        }

        holder.getChatButton().setOnClickListener {
            onItemClick.onItemClick(model)
        }
    }
}