package team.doit.do_it.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import team.doit.do_it.R


class ProjectDetailFragment : Fragment() {

    private lateinit var v : View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_project_detail, container, false)

        return v
    }

    override fun onStart() {
        super.onStart()

        val project = ProjectDetailFragmentArgs.fromBundle(requireArguments()).project

        v.findViewById<TextView>(R.id.txtProjectDetailTitle).text = project.getTitle()
        // TODO terminar de implementar
    }
}