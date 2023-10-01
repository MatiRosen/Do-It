package team.doit.do_it.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import team.doit.do_it.R
import team.doit.do_it.activities.MainActivity
import team.doit.do_it.databinding.FragmentHomeCreatorBinding
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
        return v
    }

    override fun onStart() {
        super.onStart()

        mAuth = FirebaseAuth.getInstance()

        loginButton = binding.btnLoginLogin
        loginButton.setOnClickListener {
            login(v)
        }

        val btnTextRegister = binding.btnTxtLoginRegister
        btnTextRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun login(v: View) {
        email = binding.editTxtLoginEmail.text.toString()
        password = binding.editTxtLoginPassword.text.toString()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    goHome(email, "email")
                    requireActivity().finish()
                }
                else Toast.makeText(activity, resources.getString(R.string.login_invalid_credentials), Toast.LENGTH_SHORT).show()
            }
    }

    private fun goHome(email: String, provider: String) {
        userEmail = email
        providerSession = provider

        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)

        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}