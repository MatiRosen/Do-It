package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import team.doit.do_it.databinding.FragmentProjectDetailInvestorBinding

class ProjectDetailInvestorFragment : Fragment() {

    private var _binding : FragmentProjectDetailInvestorBinding? = null
    private val binding get() = _binding!!

    private lateinit var v : View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProjectDetailInvestorBinding.inflate(inflater, container, false)
        v = binding.root

        return v
    }

    override fun onStart() {
        super.onStart()

        setTexts()
    }

    private fun setTexts() {
        val project = ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}