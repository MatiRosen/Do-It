package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import team.doit.do_it.R
import team.doit.do_it.activities.MainActivity
import team.doit.do_it.databinding.FragmentProjectTicketInvestBinding
import team.doit.do_it.entities.InvestEntity
import team.doit.do_it.enums.InvestStatus
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class ProjectTicketInvestFragment: Fragment() {
    private var _binding : FragmentProjectTicketInvestBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View

    private val db = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProjectTicketInvestBinding.inflate(inflater, container, false)
        v = binding.root
        return v
    }
    override fun onStart() {
        super.onStart()
        binding.btnProjectInvest.setOnClickListener {
            createInvest()
        }

    }
    override fun onResume() {
        super.onResume()
        val activity = requireActivity() as MainActivity
        activity.hideBottomNav()
    }
    private fun safeAccessBinding(action: () -> Unit) {
        if (_binding != null && context != null) {
            action()
        }
    }

    private fun formatMoney(money: Double): String {
        val decimalFormatSymbols = DecimalFormatSymbols()
        decimalFormatSymbols.groupingSeparator = '.'
        decimalFormatSymbols.decimalSeparator = ','
        val decimalFormat = DecimalFormat("#,###.00", decimalFormatSymbols)
        return decimalFormat.format(money)
    }
    private fun validateInvest(invest:InvestEntity,minBudget:Double) : Boolean{
        if (invest.budgetInvest < minBudget){
            val txtMinBudget = formatMoney(minBudget)
            Snackbar.make(v, resources.getString(R.string.project_ticket_budget_error,txtMinBudget), Snackbar.LENGTH_LONG).show()
            return false
        }

        return true
    }
    private fun createInvest() {
        if(binding.checkProjectInvestAcceptAffidavit.isChecked){
            val project = ProjectTicketInvestFragmentArgs.fromBundle(requireArguments()).project
            val investorEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
            val creatorEmail = project.creatorEmail
            val projectID = project.uuid
            val budget = binding.txtProjectInvestMountMoney.text.toString().toDoubleOrNull() ?: 0.0
            val status = InvestStatus.from(resources.getString(R.string.project_ticket_Invest_state))

            val invest = InvestEntity(creatorEmail, investorEmail, budget, projectID, status, "", "")
            if (validateInvest(invest,project.minBudget)){
                saveInvestToDatabase(invest)
            }
        }else{
            val message = resources.getString(R.string.project_ticket_invest_not_checked_error)
            Snackbar.make(v, message, Snackbar.LENGTH_LONG).show()
        }

    }
    private fun hideBottomInvest(){
        binding.btnProjectInvest.visibility = View.GONE
    }
    private fun saveInvestToDatabase(invest: InvestEntity){
        val investMap = mapOf(
            "creatorEmail" to invest.creatorEmail,
            "investorEmail" to invest.investorEmail,
            "budgetInvest" to invest.budgetInvest,
            "projectID" to invest.projectID,
            "status" to invest.status
        )

        db.collection("inversiones")
            .add(investMap)
            .addOnSuccessListener {
                safeAccessBinding {
                    showSuccessMessage()
                    hideBottomInvest()
                }
            }
            .addOnFailureListener {
                safeAccessBinding {
                    Snackbar.make(v, resources.getString(R.string.project_ticket_Invest_failed), Snackbar.LENGTH_LONG).show()
                }
            }
    }
    private fun showSuccessMessage(){
        val successMessage = resources.getString(R.string.project_ticket_invest_success)
        Snackbar.make(v, successMessage, Snackbar.LENGTH_LONG).show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}