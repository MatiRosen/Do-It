package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import team.doit.do_it.R
import kotlin.properties.Delegates

class RegisterFragment : Fragment() {

    private lateinit var v : View

    private lateinit var continueButton: Button

    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private lateinit var etEmail : EditText
    private lateinit var etPassword : EditText

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_register, container, false)
        return v
    }

    override fun onStart() {
        super.onStart()

        continueButton = v.findViewById<Button>(R.id.btnRegisterContinue)
        continueButton.setOnClickListener {
            //register(v)
            //TODO: Descomentar la línea de arriba y borrar la de abajo!
            findNavController().navigate(R.id.action_registerFragment_to_registerDataFragment)
        }
    }

    private fun register(v: View) {
        etEmail = v.findViewById(R.id.editTxtRegisterEmail)
        etPassword = v.findViewById(R.id.editTxtRegisterPassword)

        email = etEmail.text.toString()
        password = etPassword.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    var dbRegister = FirebaseFirestore.getInstance()
                    dbRegister.collection("usuarios").document(email).set(
                        hashMapOf(
                            "email" to email
                        ))

                    findNavController().navigate(R.id.action_registerFragment_to_registerDataFragment)
                }
                else Toast.makeText(activity, "Error, algo salió mal :(", Toast.LENGTH_SHORT).show()
            }
    }
}