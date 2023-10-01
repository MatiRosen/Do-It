package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import team.doit.do_it.databinding.FragmentProfileBinding
import com.google.firebase.firestore.*
import team.doit.do_it.R

class ProfileFragment : Fragment() {

    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View

    interface OnUserFetchedListener {
        fun onUserFetched(user: DocumentSnapshot?)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        v = binding.root

        replaceData()

        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun replaceData() {
        val name = binding.txtProfileName
        val mail = binding.txtProfileEmail
        val phone = binding.txtProfilePhone
        val gender = binding.txtProfileGender
        val address = binding.txtProfileAddress

        val currentUser = FirebaseAuth.getInstance().currentUser

        getUser(currentUser?.email.toString(), object : OnUserFetchedListener {
            override fun onUserFetched(user: DocumentSnapshot?) {
                if (user != null) {
                    //TODO: add profile photo
                    name.text = "${user.getString("nombre")} ${user.getString("apellido")}"
                    mail.text = user.getString("email")
                    phone.text = user.getString("telefono")
                    gender.text = user.getString("genero")
                    address.text = user.getString("direccion")
                } else {
                    Toast.makeText(activity, resources.getString(R.string.profile_dataUser_error), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun getUser(email: String, listener: OnUserFetchedListener) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.documents[0]
                    println(user)
                    listener.onUserFetched(user)
                } else {
                    listener.onUserFetched(null)
                }
            }
            .addOnFailureListener {
                listener.onUserFetched(null)
            }
    }

}