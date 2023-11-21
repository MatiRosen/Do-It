package team.doit.do_it.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import team.doit.do_it.R
import team.doit.do_it.activities.LoginActivity
import team.doit.do_it.databinding.FragmentOptionsBinding

class OptionsFragment : Fragment() {
    private lateinit var v : View


    private var _binding : FragmentOptionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOptionsBinding.inflate(inflater, container, false)
        v = binding.root

        return v
    }

    override fun onStart() {
        super.onStart()
        mAuth = FirebaseAuth.getInstance()

        setButtons()
    }

    private fun setButtons() {
        binding.btnOptionsPro.setOnClickListener {
            val action = OptionsFragmentDirections.actionOptionsToPremiumFragment()
            findNavController().navigate(action)
        }

        binding.btnOptionsLogout.setOnClickListener {
            confirmLogout()
        }

        binding.btnOptionsFollowingProjects.setOnClickListener {
            val action = OptionsFragmentDirections.actionOptionsToFollowingProjectFragment()
            findNavController().navigate(action)
        }

        binding.btnOptionsInvests.setOnClickListener {
            val action = OptionsFragmentDirections.actionOptionsToMyInvestmentsFragment()
            findNavController().navigate(action)
        }
    }

    private fun confirmLogout(){
        val alertDialogBuilder = AlertDialog.Builder(activity)

        alertDialogBuilder.setTitle(resources.getString(R.string.option_logout))
        alertDialogBuilder.setMessage(resources.getString(R.string.option_logout_confirmation))

        alertDialogBuilder.setPositiveButton(resources.getString(R.string.option_logout_positive)) { _, _ ->
            logout()
        }

        alertDialogBuilder.setNegativeButton(resources.getString(R.string.option_logout_negative)){
                _, _ ->
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun logout() {
        removeFCMToken{
            mAuth.signOut()

            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)

            Toast.makeText(activity, resources.getString(R.string.option_logout_success), Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }

    private fun removeFCMToken(action: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = mAuth.currentUser

        db.collection("usuarios").document(currentUser?.email.toString())
            .update("fcmToken", "")
            .addOnFailureListener { action() }
            .addOnSuccessListener { action() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
