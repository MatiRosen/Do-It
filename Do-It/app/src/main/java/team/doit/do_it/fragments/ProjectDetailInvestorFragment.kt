package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import team.doit.do_it.databinding.FragmentProjectDetailInvestorBinding

class ProjectDetailInvestorFragment : Fragment() {

    private var _binding : FragmentProjectDetailInvestorBinding? = null
    private val binding get() = _binding!!
    private var emailCreator : String = ""
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
        binding.btnGetInContactCreator.setOnClickListener {
            val action = ProjectDetailInvestorFragmentDirections.actionProjectDetailInvestorFragmentToProfileFragment(emailCreator)
            this.findNavController().navigate(action)
        }
    }

    private fun setTexts() {
        val project = ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project
        binding.txtProjectDetailInvestorTitle.text = project.getTitle()
        binding.txtProjectDetailInvestorSubtitle.text = project.getSubtitle()
        binding.txtProjectDetailInvestorDescription.text = project.getDescription()
        binding.txtProjectDetailInvestorCategory.text = project.getCategory()
        binding.txtProjectDetailInvestorGoal.text = project.getGoal().toString()
        binding.txtProjectDetailInvestorMinBudget.text = project.getMinBudget().toString()
        emailCreator = project.getCreatorEmail()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}