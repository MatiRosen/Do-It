package team.doit.do_it.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import team.doit.do_it.R
import team.doit.do_it.activities.MainActivity
import team.doit.do_it.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    companion object {
        lateinit var userEmail: String
        lateinit var providerSession: String
    }

    private lateinit var v : View

    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var loginButton: Button

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
            // TODO descomentar esto:
            login(v)
            // TODO borrar las siguientes 3 lineas:
            /*val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
            */

        }

        val btnTextRegister = binding.btnTxtLoginRegister
        btnTextRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun login(v: View) {
        mAuth.signInWithEmailAndPassword(binding.editTxtLoginEmail.text.toString(), binding.editTxtLoginPassword.text.toString())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    goHome(binding.editTxtLoginEmail.text.toString(), "email")
                    requireActivity().finish()
                }
                else Toast.makeText(activity, "Credenciales invalidas", Toast.LENGTH_SHORT).show()
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