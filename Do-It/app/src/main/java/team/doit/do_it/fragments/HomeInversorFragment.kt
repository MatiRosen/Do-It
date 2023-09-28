package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import team.doit.do_it.databinding.FragmentHomeInversorBinding

class HomeInversorFragment : Fragment() {

    private var _binding : FragmentHomeInversorBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeInversorBinding.inflate(inflater, container, false)
        v = binding.root

        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}