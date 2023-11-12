package team.doit.do_it.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import team.doit.do_it.R
import team.doit.do_it.entities.InvestEntity
import team.doit.do_it.enums.InvestStatus
import team.doit.do_it.holders.InvestHolder
import team.doit.do_it.listeners.OnBindViewHolderListener
import team.doit.do_it.listeners.OnItemViewClickListener
import team.doit.do_it.listeners.OnViewItemClickedListener

class InvestAdapter(
    options: FirestorePagingOptions<InvestEntity>,
    private val onItemClick: OnViewItemClickedListener<InvestEntity>,
    private val onButtonClickListener: OnItemViewClickListener<InvestEntity>,
    private val onBindViewHolderListener : OnBindViewHolderListener<InvestHolder, InvestEntity>,
    private val resources: Resources
) : FirestorePagingAdapter<InvestEntity, InvestHolder>(options) {

    private var loadedItemCount = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvestHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_invest, parent, false)

        return InvestHolder(view)
    }

    public override fun onBindViewHolder(holder: InvestHolder, position: Int, model: InvestEntity) {
        onBindViewHolderListener.onBindViewHolderSucceed(holder, model)
        holder.getCardLayout().visibility = ViewGroup.GONE
    }

    fun setInvestExtraData(holder: InvestHolder, model : InvestEntity){
        holder.setInvestProjectTitle(model.projectTitle)
        holder.setUserName(model.userName)
        holder.setInvestBudget(model.budgetInvest)
        holder.setInvestStatus(model.status)

        holder.getCardLayout().setOnClickListener {
            if (model.status == InvestStatus.PENDING) {
                onItemClick.onViewItemDetail(model)
            } else {
                Toast.makeText(
                    holder.getCardLayout().context,
                    resources.getString(R.string.my_investments_status_closed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        holder.getCardLayout().visibility = ViewGroup.VISIBLE

        holder.getChatButton().setOnClickListener {
            onButtonClickListener.onItemClick(model)
        }

        loadedItemCount++
    }

    fun isAllDataLoaded() : Boolean{
        return loadedItemCount == itemCount
    }
}