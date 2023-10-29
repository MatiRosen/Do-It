package team.doit.do_it.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import team.doit.do_it.R
import team.doit.do_it.activities.MainActivity
import team.doit.do_it.databinding.FragmentLoginBinding
import kotlin.properties.Delegates

class LoginFragment : Fragment() {
    companion object {
        lateinit var userEmail: String
        lateinit var providerSession: String
    }

    private lateinit var v : View

    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var loginButton: Button

    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()

    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        v = binding.root

        hideVerifyEmailButton()

        return v
    }

    override fun onStart() {
        super.onStart()

        mAuth = FirebaseAuth.getInstance()

        loginButton = binding.btnLoginLogin
        loginButton.setOnClickListener {
            login()
        }

        val btnTextRegister = binding.btnTxtLoginRegister
        btnTextRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        val btnForgotPassword = binding.txtLoginForgotPassword
        btnForgotPassword.setOnClickListener {
            forgotPassword()
        }

        val btnVerifyEmail = binding.btnLoginVerifyEmail
        btnVerifyEmail.setOnClickListener {
            mAuth.currentUser?.sendEmailVerification()
                ?.addOnCompleteListener {
                    if(it.isSuccessful) {
                        Toast.makeText(activity, resources.getString(R.string.register_email_sent), Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(activity, resources.getString(R.string.register_email_sent_error), Toast.LENGTH_SHORT).show()
                    }
                }
            hideVerifyEmailButton()
        }
    }

    private fun login() {
        email = binding.editTxtLoginEmail.text.toString()
        password = binding.editTxtLoginPassword.text.toString()

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(activity, resources.getString(R.string.login_empty_fields), Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    if(mAuth.currentUser?.isEmailVerified == true) {
                        goHome(email, "email")
                        requireActivity().finish()
                    }
                    else {
                        showVerifyEmailButton()
                        Toast.makeText(activity, resources.getString(R.string.login_email_not_verified), Toast.LENGTH_LONG).show()
                    }
                }
                else Toast.makeText(activity, resources.getString(R.string.login_invalid_credentials), Toast.LENGTH_SHORT).show()
            }
    }

    private fun forgotPassword() {
        resetPassword()
    }

    private fun resetPassword() {
        var email = binding.editTxtLoginEmail.text.toString()
        if(!TextUtils.isEmpty(email)) {
            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        Toast.makeText(activity, resources.getString(R.string.login_reset_password), Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(activity, resources.getString(R.string.login_reset_password_error), Toast.LENGTH_SHORT).show()
                    }
                }
        }
        else {
            Toast.makeText(activity, resources.getString(R.string.login_empty_email), Toast.LENGTH_SHORT).show()
        }
    }

    private fun goHome(email: String, provider: String) {
        userEmail = email
        providerSession = provider

        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)

        requireActivity().finish()
    }

    private fun hideVerifyEmailButton() {
        binding.btnLoginVerifyEmail.visibility = View.GONE
        binding.btnTxtLoginRegister.visibility = View.VISIBLE
        binding.txtLoginRegister.visibility = View.VISIBLE
    }

    private fun showVerifyEmailButton() {
        binding.btnLoginVerifyEmail.visibility = View.VISIBLE
        binding.btnTxtLoginRegister.visibility = View.GONE
        binding.txtLoginRegister.visibility = View.GONE
    }

    override fun onDestroyView() {
        hideVerifyEmailButton()
        super.onDestroyView()
        _binding = null
    }

}