package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import team.doit.do_it.R
import team.doit.do_it.databinding.FragmentLoginBinding
import team.doit.do_it.databinding.FragmentRegisterBinding
import kotlin.properties.Delegates

class RegisterFragment : Fragment() {

    private lateinit var v : View

    private var _binding : FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var continueButton: Button

    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private lateinit var etEmail : EditText
    private lateinit var etPassword : EditText

    private lateinit var termsAndConditionsCheckbox: CheckBox
    private lateinit var termsAndConditionsLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        v = binding.root
        return v
    }

    override fun onStart() {
        super.onStart()

        continueButton = binding.btnRegisterContinue
        continueButton.setOnClickListener {
            register()
        }
        termsAndConditionsLink = binding.textViewRegisterTermsAndConditionsLink
        termsAndConditionsLink.setOnClickListener {
            seeTermsAndConditions()
        }
    }

    private fun register() {
        etEmail = binding.editTxtRegisterEmail
        etPassword = binding.editTxtRegisterPassword
        termsAndConditionsCheckbox = binding.btnRegisterTermsAndConditionsCheckbox

        email = etEmail.text.toString()
        password = etPassword.text.toString()

        if(!email.isEmpty() && !password.isEmpty() && isEmailValid(email)){
            if(termsAndConditionsCheckbox.isChecked){
                val action = RegisterFragmentDirections.actionRegisterFragmentToRegisterDataFragment(email, password)
                findNavController().navigate(action)
            }
            else {
                Toast.makeText(activity, resources.getString(R.string.register_terms_and_conditions_unchecked), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(activity, resources.getString(R.string.register_wrong_format), Toast.LENGTH_SHORT).show()
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun seeTermsAndConditions() {
        val action = RegisterFragmentDirections.actionRegisterFragmentToTermsAndConditionsFragment()
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}