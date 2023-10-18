package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import team.doit.do_it.R
import team.doit.do_it.databinding.FragmentLoginBinding
import team.doit.do_it.databinding.FragmentOptionsBinding

class OptionsFragment : Fragment() {
    private lateinit var v : View


    private var _binding : FragmentOptionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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

        binding.btnPro.setOnClickListener {
            val action = OptionsFragmentDirections.actionOptionsToPremiumFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}