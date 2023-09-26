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
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import team.doit.do_it.R
import team.doit.do_it.activities.MainActivity
import kotlin.properties.Delegates

class LoginFragment : Fragment() {
    companion object {
        lateinit var userEmail: String
        lateinit var providerSession: String
    }

    private lateinit var v : View

    private lateinit var loginButton: Button

    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private lateinit var etEmail : EditText
    private lateinit var etPassword : EditText

    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_login, container, false)
        return v
    }

    override fun onStart() {
        super.onStart()

        etEmail = v.findViewById(R.id.edit_txt_login_email)
        etPassword = v.findViewById(R.id.edit_txt_login_password)
        mAuth = FirebaseAuth.getInstance()

        loginButton = v.findViewById<Button>(R.id.btn_login_login)


        loginButton.setOnClickListener {
            //login(v)
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

        val btnTextRegister = v.findViewById<TextView>(R.id.btn_txt_login_register)
        btnTextRegister.setOnClickListener {
            //register(v)
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun login(v: View) {
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    goHome(email, "email")
                }
            }
    }

    private fun register(v: View) {
        etEmail = v.findViewById(R.id.edit_txt_login_email)
        etPassword = v.findViewById(R.id.edit_txt_login_password)

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(etEmail.text.toString(), etPassword.text.toString())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    goHome(etEmail.text.toString(), "email")
                }
            }
    }

    private fun goHome(email: String, provider: String) {
        userEmail = email
        providerSession = provider

        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
    }
}