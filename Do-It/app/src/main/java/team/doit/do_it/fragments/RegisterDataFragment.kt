package team.doit.do_it.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import team.doit.do_it.R
import team.doit.do_it.databinding.FragmentRegisterDataBinding
import team.doit.do_it.entities.UserEntity
import java.util.Calendar

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

        binding.txtRegisterDataDate.setOnClickListener {
            onClickBirthDatePicker(it)
        }

        startSpinner()
        btnRegister = binding.btnRegisterDataRegister
        btnRegister.setOnClickListener {
            initializeUser(email)
            if(isValidUser()) {
                register(email, password)
            }
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
            false,
            "",
            "")
    }

    private fun isValidUser(): Boolean {
        val propertiesToCheck = listOf(
            Pair(user.getFirstName(), resources.getString(R.string.register_name_error)),
            Pair(user.getSurname(), resources.getString(R.string.register_surname_error)),
            Pair(user.getBirthDate(), resources.getString(R.string.register_birth_date_error)),
            Pair(user.getTelephoneNumber(), resources.getString(R.string.register_phone_error)),
            Pair(user.getAddress(), resources.getString(R.string.register_address_error))
        )

        for ((property, errorMessage) in propertiesToCheck) {
            if (property.isNullOrEmpty() || property.isBlank()) {
                Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
                return false
            }
        }

        if (user.getTelephoneNumber().length != 10) {
            Toast.makeText(activity, resources.getString(R.string.register_phone_format_error), Toast.LENGTH_SHORT).show()
            return false
        }

        if(user.getGender() == resources.getString(R.string.register_gender)) {
            Toast.makeText(activity, resources.getString(R.string.register_gender_error), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun onClickBirthDatePicker(v: View) {
        val selectedCalendar = Calendar.getInstance()
        val year = selectedCalendar.get(Calendar.YEAR)
        val month = selectedCalendar.get(Calendar.MONTH)
        val day = selectedCalendar.get(Calendar.DAY_OF_MONTH)
        val listener = DatePickerDialog.OnDateSetListener{ datePicker, year, month, day ->
            binding.txtRegisterDataDate.setText("$day/${month+1}/$year")
        }

        DatePickerDialog(v.context, listener, year, month, day).show()
    }

    private fun register(email: String, password: String) {

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val dbRegister = FirebaseFirestore.getInstance()
                    user.setUUID(FirebaseAuth.getInstance().currentUser?.uid!!)
                    dbRegister.collection("usuarios").document(email).set(
                        hashMapOf(
                            "nombre" to user.getFirstName(),
                            "apellido" to user.getSurname(),
                            "email" to email,
                            "fechaNacimiento" to user.getBirthDate(),
                            "genero" to user.getGender(),
                            "telefono" to user.getTelephoneNumber(),
                            "direccion" to user.getAddress(),
                            "premium" to user.getIsPremium(),
                            "uuid" to user.getUUID(),
                        ))

                    validateEmail(email)
                    findNavController().navigate(R.id.action_registerDataFragment_to_loginFragment)
                }
                else Toast.makeText(activity, resources.getString(R.string.register_generic_error), Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateEmail(email: String) {
        FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
        //TODO: Cambiar el toast por un dialogo
            /*?.addOnCompleteListener {
                if(it.isSuccessful) {
                    Toast.makeText(v.context, resources.getString(R.string.register_email_sent), Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(v.context, resources.getString(R.string.register_email_sent_error), Toast.LENGTH_SHORT).show()
                }
            }*/
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