package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
import team.doit.do_it.R
import team.doit.do_it.databinding.FragmentTermsAndConditionsBinding


class TermsAndConditionsFragment : Fragment() {

    private lateinit var v : View

    private lateinit var goBackButton: ImageButton

    private var _binding : FragmentTermsAndConditionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTermsAndConditionsBinding.inflate(inflater, container, false)
        v = binding.root
        return v
        //return inflater.inflate(R.layout.fragment_terms_and_conditions, container, false)
    }

    override fun onStart() {
        super.onStart()

        goBackButton = binding.imgBtnTermsAndConditionsBack
        goBackButton.setOnClickListener {
            val action = TermsAndConditionsFragmentDirections.actionTermsAndConditionsFragmentToRegisterFragment()
            findNavController().navigate(action)
        }
    }

    // TODO: Resolver tema de topnav con márgenes en fragment_terms_and_conditions
    // TODO: Resolver que al estar en Términos y Condiciones y regresar atrás a la primer pantalla de registracion, se deben mantener los datos ingresados en el mail y la contraseña
}