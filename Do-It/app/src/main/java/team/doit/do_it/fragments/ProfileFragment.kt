package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import team.doit.do_it.databinding.FragmentProfileBinding
import android.widget.TextView



class ProfileFragment : Fragment() {

    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View

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
        val name = v.findViewById<TextView>(team.doit.do_it.R.id.txtProfileName)
        val mail = v.findViewById<TextView>(team.doit.do_it.R.id.txtProfileEmail)
        val phone = v.findViewById<TextView>(team.doit.do_it.R.id.txtProfilePhone)
        val gender = v.findViewById<TextView>(team.doit.do_it.R.id.txtProfileGender)
        val address = v.findViewById<TextView>(team.doit.do_it.R.id.txtProfileAddress)

        val user = FirebaseAuth.getInstance().currentUser

        //TODO: replace data with user information
        if (user != null) {
            name.text = user.displayName.toString()
            mail.text = user.email.toString()
            phone.text = user.phoneNumber.toString()
            gender.text = "Género"
            address.text = "Dirección"
        }

    }
}