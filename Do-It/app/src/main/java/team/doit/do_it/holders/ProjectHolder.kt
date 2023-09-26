package team.doit.do_it.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import team.doit.do_it.R

class ProjectHolder(view: View) : RecyclerView.ViewHolder(view){

    private var view: View

    init {
        this.view = view
    }

    fun setProjectTitle(projectTitle: String) {
        val txt : TextView = view.findViewById(R.id.txt_item_project_creator_title)
        txt.text = projectTitle
    }

    fun setProjectDescription(projectDescription: String) {
        val txt : TextView = view.findViewById(R.id.txt_item_project_creator_description)
        txt.text = projectDescription
    }

    fun setProjectCategory(projectCategory: String) {
        val txt : TextView = view.findViewById(R.id.txt_item_project_creator_category)
        txt.text = projectCategory
    }

    fun setProjectTotalBudget(projectTotalBudget: Double) {
        val txt : TextView = view.findViewById(R.id.txt_item_project_creator_total_budget)
        txt.text = projectTotalBudget.toString()
    }

    fun setProjectImage(projectImage: String) {
        // TODO averiguar como se hace...
    }

    fun getCardLayout(): View {
        return view.findViewById(R.id.card_view_home_creator_project)
    }
}