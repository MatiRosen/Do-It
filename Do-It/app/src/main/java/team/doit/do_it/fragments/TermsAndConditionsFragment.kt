package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import team.doit.do_it.databinding.FragmentTermsAndConditionsBinding


class TermsAndConditionsFragment : Fragment() {

    private var _binding : FragmentTermsAndConditionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTermsAndConditionsBinding.inflate(inflater, container, false)
        v = binding.root
        return v
    }

    override fun onStart() {
        super.onStart()
        binding.btnTermsAndConditionsBack.setOnClickListener {
            v.findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}