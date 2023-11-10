package team.doit.do_it.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import team.doit.do_it.R
import team.doit.do_it.entities.ProjectEntity
import team.doit.do_it.holders.ProjectHolder
import team.doit.do_it.listeners.OnViewItemClickedListener

class ProjectListAdapter(
    options : FirestorePagingOptions<ProjectEntity>,
    private val onItemClick : OnViewItemClickedListener<ProjectEntity>
) : FirestorePagingAdapter<ProjectEntity, ProjectHolder>(options){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project_creator, parent, false)

        return ProjectHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectHolder, position: Int, model: ProjectEntity) {
        holder.setProjectTitle(model.title)
        holder.setProjectSubtitle(model.subtitle)
        holder.setProjectCategory(model.category)
        holder.setProjectGoal(model.goal)
        holder.setProjectImage(model.image, model.creatorEmail)


        holder.getCardLayout().setOnClickListener {
            onItemClick.onViewItemDetail(model)
        }
    }
}