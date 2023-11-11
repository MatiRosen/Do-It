package team.doit.do_it.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import team.doit.do_it.R
import team.doit.do_it.enums.InvestStatus

class InvestHolder(view: View) : RecyclerView.ViewHolder(view){

    private var view: View

    init {
        this.view = view
    }

    fun setInvestProjectTitle(investProjectTitle: String) {
        val txt : TextView = view.findViewById(R.id.txtInvestItemProjectTitle)
        txt.text = investProjectTitle
    }

    fun setInvestBudget(investBudget: Double) {
        val txt : TextView = view.findViewById(R.id.txtInvestItemBudgetInfo)
        txt.text = investBudget.toString()
    }

    fun setInvestStatus(investStatus: InvestStatus) {
        val txt : TextView = view.findViewById(R.id.txtInvestItemStatus)
        txt.text = investStatus.getDescription()

        when (investStatus) {
            InvestStatus.ACCEPTED -> txt.setTextColor(view.resources.getColor(R.color.green, null))
            InvestStatus.PENDING -> txt.setTextColor(view.resources.getColor(R.color.yellow, null))
            InvestStatus.REJECTED -> txt.setTextColor(view.resources.getColor(R.color.pantone, null))
        }
    }

    fun setUserName(userName: String) {
        val txt : TextView = view.findViewById(R.id.txtInvestItemUserName)
        txt.text = userName
    }

    fun getCardLayout(): View {
        return view.findViewById(R.id.cardViewInvestItem)
    }

    fun getChatButton(): View {
        return view.findViewById(R.id.imgInvestItemChat)
    }
}