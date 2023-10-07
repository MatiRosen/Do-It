package team.doit.do_it.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import team.doit.do_it.R
import team.doit.do_it.extensions.setMaxLinesForEllipsizing
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

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
        val txt: TextView = view.findViewById(R.id.txtItemProjectCreatorSubtitle)
        txt.text = projectSubtitle

        txt.setMaxLinesForEllipsizing(true)
    }


    fun setProjectCategory(projectCategory: String) {
        val txt : TextView = view.findViewById(R.id.txtItemProjectCreatorCategory)
        txt.text = projectCategory
    }

    fun setProjectGoal(projectTotalBudget: Double) {
        val decimalFormatSymbols = DecimalFormatSymbols()
        decimalFormatSymbols.groupingSeparator = '.'
        decimalFormatSymbols.decimalSeparator = ','
        val decimalFormat = DecimalFormat("#,###.00", decimalFormatSymbols)

        val txt : TextView = view.findViewById(R.id.txtItemProjectCeatorTotalBudget)
        val budgetString = view.context.getString(R.string.currency) + decimalFormat.format(projectTotalBudget)
        txt.text = budgetString
    }

    fun setProjectImage(projectImage: String) {
        // TODO averiguar como se hace...
    }

    fun getCardLayout(): View {
        return view.findViewById(R.id.cardViewHomeCreatorProject)
    }
}