package team.doit.do_it.holders

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import team.doit.do_it.R
import team.doit.do_it.enums.InvestStatus
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

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
        val decimalFormatSymbols = DecimalFormatSymbols()
        decimalFormatSymbols.groupingSeparator = '.'
        decimalFormatSymbols.decimalSeparator = ','
        val decimalFormat = DecimalFormat("#,###.00", decimalFormatSymbols)

        val budgetString = view.context.getString(R.string.currency) + decimalFormat.format(investBudget)
        txt.text = budgetString
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

    fun setUserName(userName: String, isOwner: Boolean) {
        val txt : TextView = view.findViewById(R.id.txtInvestItemUserName)
        val text = if (isOwner) {
            view.context.getString(R.string.my_investments_owner, userName)
        } else {
            view.context.getString(R.string.my_investments_investor, userName)
        }

        txt.text = text
    }

    fun getCardLayout(): View {
        return view.findViewById(R.id.cardViewInvestItem)
    }

    fun getChatButton(): View {
        return view.findViewById(R.id.imgInvestItemChat)
    }
}