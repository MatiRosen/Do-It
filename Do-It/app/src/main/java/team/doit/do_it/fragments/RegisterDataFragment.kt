package team.doit.do_it.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import team.doit.do_it.R
import team.doit.do_it.activities.MainActivity
import team.doit.do_it.databinding.FragmentRegisterBinding
import team.doit.do_it.databinding.FragmentRegisterDataBinding
import team.doit.do_it.entities.UserEntity

class RegisterDataFragment : Fragment() {

    private lateinit var v : View

    private lateinit var spinnerGender : Spinner
    private lateinit var btnRegister : Button

    private var _binding : FragmentRegisterDataBinding? = null
    private val binding get() = _binding!!

    private lateinit var user : UserEntity

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterDataBinding.inflate(inflater, container, false)
        v = binding.root
        return v
    }

    override fun onStart() {
        super.onStart()

        val email = RegisterDataFragmentArgs.fromBundle(requireArguments()).email
        val password = RegisterDataFragmentArgs.fromBundle(requireArguments()).password

        startSpinner()
        btnRegister = binding.btnRegisterDataRegister
        btnRegister.setOnClickListener {
            initializeUser(email)
            register(email, password)
        }
    }

    private fun initializeUser(email: String) {
        user = UserEntity(
            binding.txtRegisterDataName.text.toString(),
            binding.txtRegisterDataSurname.text.toString(),
            email,
            binding.txtRegisterDataDate.text.toString(),
            spinnerGender.selectedItem.toString(),
            binding.txtRegisterDataPhone.text.toString(),
            binding.txtRegisterDataAddress.text.toString(),
            0)
    }

    private fun register(email: String, password: String) {

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    var dbRegister = FirebaseFirestore.getInstance()
                    dbRegister.collection("usuarios").document(email).set(
                        hashMapOf(
                            "nombre" to user.getFirstName(),
                            "apellido" to user.getSurname(),
                            "email" to email,
                            "fechaNacimiento" to user.getBirthDate(),
                            "genero" to user.getGender(),
                            "telefono" to user.getTelephoneNumber(),
                            "direccion" to user.getAddress(),
                            "premium" to user.getIsPremium().toInt()
                        ))

                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)

                    requireActivity().finish()
                }
                else Toast.makeText(activity, "Error, algo salió mal :(", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startSpinner() {
        spinnerGender = binding.spnRegisterDataGender
        val genders = resources.getStringArray(R.array.genders).toMutableList()
        val hint = resources.getString(R.string.register_gender)
        genders.add(0, hint)

        val adapter = object : ArrayAdapter<String>(v.context, android.R.layout.simple_list_item_activated_1, genders) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)

                if (position == 0) {
                    (view as TextView).setTextColor(resources.getColor(R.color.medium_gray, null))
                    view.setBackgroundColor(0)
                }
                return view
            }
        }
        spinnerGender.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}