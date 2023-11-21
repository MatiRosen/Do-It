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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
        val date = SimpleDateFormat("dd/MM/yy", Locale.US).parse(binding.txtRegisterDataDate.text.toString())?: Date()
        user = UserEntity(
            binding.txtRegisterDataName.text.toString(),
            binding.txtRegisterDataSurname.text.toString(),
            email,
            date,
            spinnerGender.selectedItem.toString(),
            binding.txtRegisterDataPhone.text.toString(),
            binding.txtRegisterDataAddress.text.toString(),
            false,
            "",
            "",
            "")
    }

    private fun isValidUser(): Boolean {
        val propertiesToCheck = listOf(
            Pair(user.firstName, resources.getString(R.string.register_name_error)),
            Pair(user.surname, resources.getString(R.string.register_surname_error)),
            Pair(user.birthDate, resources.getString(R.string.register_birth_date_error)),
            Pair(user.telephoneNumber, resources.getString(R.string.register_phone_error)),
            Pair(user.address, resources.getString(R.string.register_address_error))
        )

        for ((property, errorMessage) in propertiesToCheck) {
            if (property.toString().isEmpty()) {
                Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
                return false
            }
        }

        if (user.telephoneNumber.length != 10) {
            Toast.makeText(activity, resources.getString(R.string.register_phone_format_error), Toast.LENGTH_SHORT).show()
            return false
        }

        if(user.gender == resources.getString(R.string.register_gender)) {
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
        val listener = DatePickerDialog.OnDateSetListener{
                _, year, month, day ->
            binding.txtRegisterDataDate.setText("$day/${month+1}/$year")
        }

        DatePickerDialog(v.context, listener, year, month, day).show()
    }

    private fun register(email: String, password: String) {

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val dbRegister = FirebaseFirestore.getInstance()
                    user.uuid = FirebaseAuth.getInstance().currentUser?.uid!!
                    dbRegister.collection("usuarios").document(email).set(
                        hashMapOf(
                            "firstName" to user.firstName,
                            "surname" to user.surname,
                            "email" to email,
                            "birthDate" to user.birthDate,
                            "gender" to user.gender,
                            "telephoneNumber" to user.telephoneNumber,
                            "address" to user.address,
                            "isPremium" to user.isPremium,
                            "uuid" to user.uuid,
                            "fcmToken" to user.fcmToken,
                            "userImage" to user.userImage
                        ))

                    validateEmail(email)
                    findNavController().navigate(R.id.action_registerDataFragment_to_loginFragment)
                }
                else Toast.makeText(activity, resources.getString(R.string.register_generic_error), Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateEmail(email: String) {
        FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
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