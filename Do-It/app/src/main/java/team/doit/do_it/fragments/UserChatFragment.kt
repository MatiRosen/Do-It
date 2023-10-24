package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
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

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Firebase.database
        val myRef = db.getReference("messages").child("mati")
        
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}