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
        val txt : TextView = view.findViewById(R.id.txtItemProjectCreatorTitle)
        txt.text = projectTitle
    }

    fun setProjectSubtitle(projectSubtitle: String) {
        val txt : TextView = view.findViewById(R.id.txtItemProjectCreatorSubtitle)
        txt.text = projectSubtitle
    }

    fun setProjectCategory(projectCategory: String) {
        val txt : TextView = view.findViewById(R.id.txtItemProjectCreatorCategory)
        txt.text = projectCategory
    }

    fun setProjectTotalBudget(projectTotalBudget: Double) {
        val txt : TextView = view.findViewById(R.id.txtItemProjectCeatorTotalBudget)
        val budgetString = view.context.getString(R.string.currency) + projectTotalBudget.toString()
        txt.text = budgetString
    }

    fun setProjectImage(projectImage: String) {
        // TODO averiguar como se hace...
    }

    fun getCardLayout(): View {
        return view.findViewById(R.id.cardViewHomeCreatorProject)
    }
}