package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import team.doit.do_it.databinding.FragmentEditProjectBinding

class EditProjectFragment : Fragment() {

    private var _binding : FragmentEditProjectBinding? = null
    private val binding get() = _binding!!

    private lateinit var v : View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProjectBinding.inflate(inflater, container, false)
        v = binding.root
        return v
    }

    override fun onStart() {
        super.onStart()
        binding.imgBtnProjectEditCreatorBack.setOnClickListener {
            v.findNavController().navigateUp()
        }
    }
}