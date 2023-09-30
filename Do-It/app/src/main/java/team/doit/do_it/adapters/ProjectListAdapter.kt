package team.doit.do_it.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import team.doit.do_it.R
import team.doit.do_it.entities.ProjectEntity
import team.doit.do_it.holders.ProjectHolder
import team.doit.do_it.listeners.OnViewItemClickedListener

class ProjectListAdapter(
    private val projectList : MutableList<ProjectEntity>,
    private val onItemClick : OnViewItemClickedListener
) : RecyclerView.Adapter<ProjectHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project_creator, parent, false)

        return ProjectHolder(view)
    }

    override fun getItemCount(): Int {
        return projectList.size
    }

    override fun onBindViewHolder(holder: ProjectHolder, position: Int) {
        val project = projectList[position]

        holder.setProjectTitle(project.getTitle())
        holder.setProjectSubtitle(project.getSubtitle())
        holder.setProjectCategory(project.getCategory())
        holder.setProjectGoal(project.getGoal())
        holder.setProjectImage(project.getImage())


        holder.getCardLayout().setOnClickListener {
            onItemClick.onViewItemDetail(project)
        }
    }

}