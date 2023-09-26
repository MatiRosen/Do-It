package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import team.doit.do_it.R
class RegisterDataFragment : Fragment() {

    private lateinit var v : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_register_data, container, false)

        val spinnerGender: Spinner = v.findViewById(R.id.spn_register_data_gender)
        ArrayAdapter.createFromResource(
            v.context,
            R.array.genders,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerGender.adapter = adapter
        }

        return v
    }

    override fun onStart() {
        super.onStart()


    }
}