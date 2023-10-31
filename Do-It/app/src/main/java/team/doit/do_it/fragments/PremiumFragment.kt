package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import team.doit.do_it.R
import team.doit.do_it.activities.MainActivity
import team.doit.do_it.databinding.FragmentPremiumBinding
import java.io.IOException

class PremiumFragment : Fragment() {

    companion object {
        private const val BACKEND_URL = "https://enchanting-sprout-agate.glitch.me"
    }

    private lateinit var v : View

    private var _binding : FragmentPremiumBinding? = null
    private val binding get() = _binding!!

    private lateinit var paymentIntentClientSecret: String
    private lateinit var paymentSheet: PaymentSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        fetchPaymentIntent()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPremiumBinding.inflate(inflater, container, false)
        v = binding.root

        showProgressBar()

        return v
    }

    private fun showProgressBar() {
        binding.progressBarPremium.visibility = View.VISIBLE
        binding.btnPremiumBuy.visibility = View.GONE
    }

    private fun hideProgressBar() {
        binding.progressBarPremium.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        overrideBackButton()

        isPremiumUser { isPremium ->
            if(isPremium){
                binding.btnPremiumBuy.visibility = View.GONE
            } else binding.btnPremiumBuy.visibility = View.VISIBLE
            hideProgressBar()
        }

        setupButtons()

        val activity = requireActivity() as MainActivity
        activity.removeMargins()
    }

    private fun setupButtons(){
        binding.imgBtnPremiumBack.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_premiumFragment_to_options)
        }

        binding.btnPremiumBuy.setOnClickListener{
            onPayClicked(it)
        }
    }

    private fun fetchPaymentIntent() {
        val url = "$BACKEND_URL/create-payment-intent"

        val shoppingCartContent = ""

        val mediaType = "application/json; charset=utf-8".toMediaType()

        val body = shoppingCartContent.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        OkHttpClient()
            .newCall(request)
            .enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    safeAccessBinding {
                        showAlert(resources.getString(R.string.failLoadData), "Error: $e")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    safeAccessBinding {
                        if (!response.isSuccessful) {
                            showAlert(resources.getString(R.string.failLoadPage), "Error: $response")
                        } else {
                            val responseData = response.body?.string()
                            val responseJson = responseData?.let { JSONObject(it) } ?: JSONObject()
                            paymentIntentClientSecret = responseJson.getString("clientSecret")
                            safeAccessBinding {
                                activity?.runOnUiThread {
                                    binding.btnPremiumBuy.isEnabled = true
                                }
                            }
                        }
                    }
                }
            })
    }

    private fun overrideBackButton() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = findNavController()
                navController.navigate(R.id.action_premiumFragment_to_options)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun safeAccessBinding(action: () -> Unit) {
        if (_binding != null && context != null) {
            action()
        }
    }

    private fun showAlert(title: String, message: String? = null) {
        activity?.runOnUiThread {
            val builder = AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
            builder.setPositiveButton("Ok", null)
            builder.create().show()
        }
    }

    private fun showToast(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(activity,  message, Toast.LENGTH_LONG).show()
        }
    }

    private fun onPayClicked(view: View) {
        val configuration = PaymentSheet.Configuration("Example, Inc.")
        isPremiumUser { isPremium ->
            if(!isPremium){
                paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration)
            }
            else {
                Toast.makeText(context, resources.getString(R.string.alreadyPremium), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isPremiumUser(action: (Boolean) -> Unit): Boolean {
        var isPremium = false

        val currentUser = FirebaseAuth.getInstance().currentUser
        getUser(currentUser?.email.toString(), object : ProfileFragment.OnUserFetchedListener {
            override fun onUserFetched(user: DocumentSnapshot?) {
                safeAccessBinding {
                    if (user != null) {
                        isPremium = user.getBoolean("premium")!!
                        action(isPremium)
                    }
                }
            }
        })
        return isPremium
    }

    private fun onPaymentSheetResult(paymentResult: PaymentSheetResult) {
        when (paymentResult) {
            is PaymentSheetResult.Completed -> {
                showToast(resources.getString(R.string.paymentSuccess))
                upgradeClient {
                    this.findNavController().navigateUp()
                }
            }
            is PaymentSheetResult.Failed -> {
                showAlert(resources.getString(R.string.paymentFailed), paymentResult.error.localizedMessage)
            }

            else -> {}
        }
    }

    private fun upgradeClient(action: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        getUser(currentUser?.email.toString(), object : ProfileFragment.OnUserFetchedListener {
            override fun onUserFetched(user: DocumentSnapshot?) {
                user?.reference?.update("premium", true)
                action(true)
            }
        })
    }

    private fun getUser(email: String, listener: ProfileFragment.OnUserFetchedListener) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    listener.onUserFetched(documents.documents[0])
                } else {
                    listener.onUserFetched(null)
                }
            }
            .addOnFailureListener {
                listener.onUserFetched(null)
            }
    }

    override fun onStop() {
        super.onStop()

        val activity = requireActivity() as MainActivity
        activity.showMargins()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}