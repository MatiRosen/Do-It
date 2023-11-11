package team.doit.do_it.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import team.doit.do_it.R
import team.doit.do_it.activities.MainActivity
import team.doit.do_it.databinding.FragmentInvestSelectStatusBinding
import team.doit.do_it.entities.InvestEntity
import team.doit.do_it.enums.InvestStatus

class InvestSelectStatusFragment : Fragment() {

    private var _binding : FragmentInvestSelectStatusBinding? = null
    private val binding get() = _binding!!

    private lateinit var v : View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInvestSelectStatusBinding.inflate(inflater, container, false)

        v = binding.root

        return v
    }

    override fun onStart() {
        super.onStart()
        val activity = activity as MainActivity
        activity.hideBottomNav()
        activity.removeMargins()
        safeAccessBinding {
            setupButtons()
        }
    }

    override fun onStop() {
        super.onStop()
        val activity = activity as MainActivity
        activity.showBottomNav()
        activity.showMargins()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupButtons() {
        binding.imgBtnInvestSelectStatusBack.setOnClickListener {
            this.findNavController().navigateUp()
        }

        binding.btnInvestSelectStatusAccept.setOnClickListener {
           if (!binding.checkInvestSelectStatusAcceptTerms.isChecked){
               sendConfirmation(
                   resources.getString(R.string.invest_select_status_attention),
                   resources.getString(R.string.invest_select_status_confirm_terms),
                   resources.getString(R.string.invest_select_status_button_accept_dialog),
                   ""
               ) {}

               return@setOnClickListener
           }

            sendConfirmation(
                resources.getString(R.string.invest_select_status_confirm_terms_title),
                resources.getString(R.string.invest_select_status_confirm_terms_message),
                resources.getString(R.string.invest_select_status_button_accept_dialog),
                resources.getString(R.string.invest_select_status_button_cancel_dialog)
            ) {
                this.findNavController().navigateUp()
                setInvestStatus(InvestStatus.ACCEPTED)
            }
        }

        binding.btnInvestSelectStatusReject.setOnClickListener {
            if (!binding.checkInvestSelectStatusAcceptTerms.isChecked){
                sendConfirmation(
                    resources.getString(R.string.invest_select_status_attention),
                    resources.getString(R.string.invest_select_status_confirm_terms),
                    resources.getString(R.string.invest_select_status_button_accept_dialog),
                    ""
                ) {}

                return@setOnClickListener
            }

            sendConfirmation(
                resources.getString(R.string.invest_select_status_reject_terms_title),
                resources.getString(R.string.invest_select_status_reject_terms_message),
                resources.getString(R.string.invest_select_status_button_accept_dialog),
                resources.getString(R.string.invest_select_status_button_cancel_dialog)
            ) {
                this.findNavController().navigateUp()
                setInvestStatus(InvestStatus.REJECTED)
            }
        }
    }

    private fun setInvestStatus(status: InvestStatus) {
        val args = InvestSelectStatusFragmentArgs.fromBundle(requireArguments())
        val invest = args.invest

        invest.status = status

        updateInvestOnDatabase(invest)
    }

    private fun updateInvestOnDatabase(invest: InvestEntity) {
        val filteredInvest = mutableMapOf(
            "uuid" to invest.uuid,
            "projectID" to invest.projectID,
            "investorEmail" to invest.investorEmail,
            "creatorEmail" to invest.creatorEmail,
            "status" to invest.status,
            "budgetInvest" to invest.budgetInvest,
            "date" to invest.date
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("inversiones")
            .document(invest.uuid)
            .set(filteredInvest)
            .addOnSuccessListener {
                safeAccessBinding {
                    Toast.makeText(context, resources.getString(R.string.invest_select_status_update_success), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                safeAccessBinding {
                    Toast.makeText(context, resources.getString(R.string.invest_select_status_update_error), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendConfirmation(title: String, message: String, accept: String, reject: String, action: () -> Unit){
        val alertDialogBuilder = AlertDialog.Builder(activity)

        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)

        alertDialogBuilder.setPositiveButton(accept) { _, _ ->
            action()
        }

        if (reject.isNotEmpty()) {
            alertDialogBuilder.setNegativeButton(reject) { _, _ ->
                Toast.makeText(context, resources.getString(R.string.invest_select_status_button_canceled_dialog), Toast.LENGTH_SHORT).show()
            }
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun safeAccessBinding(action : () -> Unit) {
        if (_binding != null && context != null) {
            action()
        }
    }
}