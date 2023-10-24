package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import team.doit.do_it.R
import team.doit.do_it.databinding.FragmentUserChatBinding

class UserChatFragment : Fragment() {

    private lateinit var v : View

    private var _binding : FragmentUserChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseDatabase
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserChatBinding.inflate(inflater, container, false)
        v = binding.root

        hideBottomNav()
        removeMargins()

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startButtons()
        setBindings()
    }

    override fun onStart() {
        super.onStart()

        startChat()
    }

    private fun startChat() {
        db = Firebase.database
        val ref = db.getReference("messages")
    }


    private fun startButtons() {
        binding.imgBtnUserChatBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setBindings() {


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

    private fun showBottomNav() {
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.VISIBLE
    }

    private fun showMargins() {
        val constraintSet = ConstraintSet()
        constraintSet.connect(R.id.mainHost, ConstraintSet.TOP, R.id.guidelineMainActivityHorizontal3, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.mainHost, ConstraintSet.BOTTOM, R.id.bottomNavigationView, ConstraintSet.TOP)
        constraintSet.connect(R.id.mainHost, ConstraintSet.START, R.id.guidelineMainActivityVertical2, ConstraintSet.END)
        constraintSet.connect(R.id.mainHost, ConstraintSet.END, R.id.guidelineMainActivityVertical98, ConstraintSet.START)


        constraintSet.applyTo(requireActivity().findViewById(R.id.frameLayoutMainActivity))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showBottomNav()
        showMargins()
        _binding = null
    }

}