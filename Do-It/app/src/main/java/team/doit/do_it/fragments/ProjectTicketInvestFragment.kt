package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import team.doit.do_it.R
import team.doit.do_it.activities.MainActivity
import team.doit.do_it.databinding.FragmentProjectTicketInvestBinding
import team.doit.do_it.entities.InvestEntity
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
        if (invest.getBudgetInvest() < minBudget){
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
            val projectTile = project.title
            val budget = binding.txtProjectInvestMountMoney.text.toString().toDoubleOrNull() ?: 0.0
            val estado = resources.getString(R.string.project_ticket_Invest_state)
            val invest = InvestEntity(investorEmail,creatorEmail, budget, projectTile.toString(),estado)
            if (validateInvest(invest,project.minBudget)){
                saveInvestToDatabase(invest)
            }
        }else{
            val Message = resources.getString(R.string.project_ticket_invest_not_checked_error)
            Snackbar.make(v, Message, Snackbar.LENGTH_LONG).show()
        }

    }
    private fun hideBottomInvest(){
        binding.btnProjectInvest.visibility = View.GONE
    }
    private fun saveInvestToDatabase(invest: InvestEntity){
        db.collection("inversiones")
            .add(invest)
            .addOnSuccessListener {
                safeAccessBinding {
                    showSuccessMessage(invest)
                    hideBottomInvest()
                }
            }
            .addOnFailureListener {
                safeAccessBinding {
                    Snackbar.make(v, resources.getString(R.string.project_ticket_Invest_failed), Snackbar.LENGTH_LONG).show()
                }
            }
    }
    private fun showSuccessMessage(invest: InvestEntity){
        val successMessage = resources.getString(R.string.project_ticket_invest_success)
        Snackbar.make(v, successMessage, Snackbar.LENGTH_LONG).show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}