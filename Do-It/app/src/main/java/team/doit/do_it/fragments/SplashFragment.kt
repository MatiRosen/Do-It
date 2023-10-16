package team.doit.do_it.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import team.doit.do_it.activities.MainActivity
import team.doit.do_it.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private lateinit var v : View

    private var _binding : FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAuth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        v = binding.root

        return v
    }

    override fun onStart() {
        super.onStart()
        mAuth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            //checkUser()
            // TODO: Descomentamos esto y borramos el navigate cuando este terminado el logout
            v.findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToLoginFragment())
        }, 1500)
    }

    private fun checkUser() {
        if (mAuth.currentUser != null) {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        } else {
            val action = SplashFragmentDirections.actionSplashFragmentToLoginFragment()
            v.findNavController().navigate(action)
        }
    }
}