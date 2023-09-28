package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import team.doit.do_it.databinding.FragmentProjectDetailBinding


class ProjectDetailFragment : Fragment() {

    private var _binding : FragmentProjectDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProjectDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val project = ProjectDetailFragmentArgs.fromBundle(requireArguments()).project

        binding.txtProjectDetailTitle.text = project.getTitle()
        binding.txtProjectDetailSubtitle.text = project.getSubtitle()
        binding.txtProjectDetailDescription.text = project.getDescription()
        binding.txtProjectDetailCategory.text = project.getCategory()
        binding.txtProjectDetailTotalBudget.text = project.getTotalBudget().toString()
        // TODO terminar de implementar
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}