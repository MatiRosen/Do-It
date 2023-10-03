package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import team.doit.do_it.databinding.FragmentProjectDetailCreatorBinding


class ProjectDetailCreatorFragment : Fragment() {

    private var _binding : FragmentProjectDetailCreatorBinding? = null
    private val binding get() = _binding!!

    private lateinit var v : View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProjectDetailCreatorBinding.inflate(inflater, container, false)
        v = binding.root

        return v
    }

    override fun onStart() {
        super.onStart()

        setTexts()
    }

    private fun setTexts() {
        val project = ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project

        binding.txtProjectDetailCreatorTitle.text = project.getTitle()
        binding.txtProjectDetailCreatorSubtitle.text = project.getSubtitle()
        binding.txtProjectDetailCreatorDescription.text = project.getDescription()
        binding.txtProjectDetailCreatorCategory.text = project.getCategory()
        binding.txtProjectDetailCreatorGoal.text = project.getGoal().toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}