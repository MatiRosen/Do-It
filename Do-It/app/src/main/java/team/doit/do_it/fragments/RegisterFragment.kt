package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import team.doit.do_it.R

class RegisterFragment : Fragment() {

    private lateinit var v : View

    private lateinit var continueButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_register, container, false)
        return v
    }

    override fun onStart() {
        super.onStart()

        continueButton = v.findViewById<Button>(R.id.btn_register_continue)
        continueButton.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_registerDataFragment)
        }
    }
}