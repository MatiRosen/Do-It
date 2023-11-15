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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import team.doit.do_it.R
import team.doit.do_it.activities.MainActivity
import team.doit.do_it.databinding.FragmentProjectTicketInvestBinding
import team.doit.do_it.entities.InvestEntity
import team.doit.do_it.enums.InvestStatus
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Date

class ProjectTicketInvestFragment: Fragment() {

    companion object {
        private const val BACKEND_URL = "https://enchanting-sprout-agate.glitch.me"
    }

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

        safeAccessBinding {
            setupButtons()
        }
    }
    override fun onResume() {
        super.onResume()
        val activity = requireActivity() as MainActivity
        activity.hideBottomNav()
        activity.removeMargins()
    }

    override fun onStop() {
        super.onStop()
        val activity = requireActivity() as MainActivity
        activity.showBottomNav()
        activity.showMargins()
    }

    private fun setupButtons(){
        binding.btnProjectInvest.setOnClickListener {
            createInvest()
        }

        binding.imgBtnProjectDetailInvestBack.setOnClickListener {
            this.findNavController().navigateUp()
        }
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
    private fun validateInvest(invest:InvestEntity,minBudget:Double,goal:Double) : Boolean{
        if (invest.budgetInvest < minBudget){
            val txtMinBudget = formatMoney(minBudget)
            Snackbar.make(v, resources.getString(R.string.project_ticket_invest_budget_min_error,txtMinBudget), Snackbar.LENGTH_LONG).show()
            return false
        }
        if (invest.budgetInvest > goal){
            val txtMinBudget = formatMoney(minBudget)
            Snackbar.make(v, resources.getString(R.string.project_ticket_invest_budget_max_error,txtMinBudget), Snackbar.LENGTH_LONG).show()
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
            val status = InvestStatus.from(resources.getString(R.string.project_ticket_invest_state))

            val invest = InvestEntity("", creatorEmail, investorEmail, budget, projectID, status, "", "", Date())
            if (validateInvest(invest,project.minBudget,project.goal)){
                getOwnerToken(creatorEmail) { token ->
                    sendNotification(project.title, investorEmail, token)
                    saveInvestToDatabase(invest)
                }
            }
        }else{
            val message = resources.getString(R.string.project_ticket_invest_not_checked_error)
            Snackbar.make(v, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun sendNotification(projectTitle: String, investorEmail: String, token: String) {
        val url = "${BACKEND_URL}/send-notification"
        FirebaseFirestore
            .getInstance()
            .collection("usuarios")
            .document(investorEmail)
            .get()
            .addOnSuccessListener {
                safeAccessBinding {
                    val username = it.getString("firstName") + " " + it.getString("surname")
                    val title = resources.getString(R.string.project_new_ticket_title,projectTitle)
                    val message = resources.getString(R.string.project_new_ticket_message, username)

                    val mediaType = "application/json".toMediaType()
                    val json = "{\"title\":\"$title\", \"body\":\"$message\", \"token\":\"$token\", \"fromFragment\":\"ProjectTicketInvestFragment\"}"
                    val requestBody = json.toRequestBody(mediaType)

                    val req = Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()

                    OkHttpClient().newCall(req).enqueue(object: okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {}
                    })
                }
            }
    }

    private fun getOwnerToken(email: String,action: (token: String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.documents[0]
                    action(user.getString("fcmToken").toString())
                }
            }
    }

    private fun saveInvestToDatabase(invest: InvestEntity){
        val investMap = mutableMapOf(
            "creatorEmail" to invest.creatorEmail,
            "investorEmail" to invest.investorEmail,
            "budgetInvest" to invest.budgetInvest,
            "projectID" to invest.projectID,
            "status" to invest.status,
            "date" to invest.date
        )

        db.collection("inversiones")
            .add(investMap)
            .addOnSuccessListener {
                investMap["uuid"] = it.id
                db.collection("inversiones").document(it.id).set(investMap)
                safeAccessBinding {
                    showSuccessMessage()

                    this.findNavController().navigateUp()
                }
            }
            .addOnFailureListener {
                safeAccessBinding {
                    Snackbar.make(v, resources.getString(R.string.project_ticket_invest_failed), Snackbar.LENGTH_LONG).show()
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