package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import team.doit.do_it.R
import team.doit.do_it.databinding.FragmentFollowingProjectBinding

class FollowingProjectFragment : Fragment() {

    private var _binding: FragmentFollowingProjectBinding? = null
    private val binding get() = _binding!!

    private lateinit var v : View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFollowingProjectBinding.inflate(inflater, container, false)
        v = binding.root
        return v
    }

    override fun onStart() {
        super.onStart()

        overrideBackButton()
    }

    private fun overrideBackButton() { // TODO hacerle acordar a juli que meta esto mismo en el fragment de premium
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = findNavController()
                navController.navigate(R.id.action_followingProjectFragment_to_options)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}