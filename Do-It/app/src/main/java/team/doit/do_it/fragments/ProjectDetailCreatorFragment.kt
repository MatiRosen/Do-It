package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import team.doit.do_it.R
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

        hideBottomNav()
        removeMargins()

        return v
    }

    override fun onStart() {
        super.onStart()

        setTexts()
        initializeButtons()
    }

    private fun initializeButtons() {
        binding.imgBtnProjectDetailCreatorBack.setOnClickListener {
            v.findNavController().navigateUp()
        }

        binding.imgBtnProjectDetailCreatorEdit.setOnClickListener {
            val project = ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project
            val action = ProjectDetailCreatorFragmentDirections.actionProjectDetailFragmentToEditProjectFragment(project)
            v.findNavController().navigate(action)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        showBottomNav()
        showMargins()
    }

    private fun setTexts() {
        val project = ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project

        binding.txtProjectDetailCreatorTitle.text = project.getTitle()
        binding.txtProjectDetailCreatorSubtitle.text = project.getSubtitle()
        binding.txtProjectDetailCreatorDescription.text = project.getDescription()
        binding.txtProjectDetailCreatorCategory.text = project.getCategory()
        binding.txtProjectDetailCreatorGoal.text = project.getGoal().toString()
    }

    private fun hideBottomNav() {
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE
    }

    private fun removeMargins() {
        requireActivity().findViewById<FragmentContainerView>(R.id.mainHost)
            .layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
            )
    }
    private fun showMargins() {
        val constraintSet = ConstraintSet()
        constraintSet.connect(R.id.mainHost, ConstraintSet.TOP, R.id.guidelineMainActivityHorizontal3, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.mainHost, ConstraintSet.BOTTOM, R.id.bottomNavigationView, ConstraintSet.TOP)
        constraintSet.connect(R.id.mainHost, ConstraintSet.START, R.id.guidelineMainActivityVertical2, ConstraintSet.END)
        constraintSet.connect(R.id.mainHost, ConstraintSet.END, R.id.guidelineMainActivityVertical98, ConstraintSet.START)


        constraintSet.applyTo(requireActivity().findViewById(R.id.frameLayoutMainActivity))
    }

    private fun showBottomNav() {
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}