package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import team.doit.do_it.R
import team.doit.do_it.databinding.FragmentOptionsBinding
import team.doit.do_it.databinding.FragmentPremiumBinding

class PremiumFragment : Fragment() {
    private lateinit var v : View


    private var _binding : FragmentPremiumBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPremiumBinding.inflate(inflater, container, false)
        v = binding.root

        removeMargins()

        return v
    }

    override fun onStart() {
        super.onStart()

        binding.imgBtnPremiumBack.setOnClickListener {
            v.findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showMargins()
        _binding = null
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
}