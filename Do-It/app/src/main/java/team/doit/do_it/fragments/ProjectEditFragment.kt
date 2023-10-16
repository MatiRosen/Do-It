package team.doit.do_it.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import team.doit.do_it.databinding.FragmentProjectEditBinding

class ProjectEditFragment : Fragment() {

    private var _binding : FragmentProjectEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var v : View

    //TODO: actualizar mas campos, actualizar cuando se hace navigateup
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProjectEditBinding.inflate(inflater, container, false)
        v = binding.root
        return v
    }

    override fun onStart() {
        super.onStart()
        binding.imgBtnProjectEditBack.setOnClickListener {
            v.findNavController().navigateUp()
        }

        binding.imgProjectEditImage.setOnClickListener {
            Toast.makeText(activity, "WIP", Toast.LENGTH_SHORT).show()
        }

        binding.btnConfirmEditProject.setOnClickListener {
            updateProject(object : ProjectDetailCreatorFragment.OnProjectUpdatedListener {
                override fun onProjectUpdated(successful: Boolean) {
                    if (successful) {
                        safeAccesBinding {
                            Toast.makeText(activity, "Project updated", Toast.LENGTH_SHORT).show()
                            v.findNavController().navigateUp()
                        }
                    } else {
                        Toast.makeText(activity, "Error updating project", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        replaceData()
    }

    private fun safeAccesBinding(action : () -> Unit) {
        if (_binding != null) {
            action()
        }
    }

    private fun replaceData() {
        safeAccesBinding {
            binding.txtProjectEditTitle.setText(ProjectEditFragmentArgs.fromBundle(requireArguments()).project.title)
            binding.txtProjectEditDescription.setText(ProjectEditFragmentArgs.fromBundle(requireArguments()).project.description)
            binding.txtProjectEditSubtitle.setText(ProjectEditFragmentArgs.fromBundle(requireArguments()).project.subtitle)
        }
    }

    private fun updateProject(listener : ProjectDetailCreatorFragment.OnProjectUpdatedListener) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        currentUser?.let {

            val project = hashMapOf<String, Any>(
                "title" to binding.txtProjectEditTitle.text.toString(),
                "description" to binding.txtProjectEditDescription.text.toString(),
                "subtitle" to binding.txtProjectEditSubtitle.text.toString()
            )

            db.collection("ideas")
                .whereEqualTo("creatorEmail", currentUser?.email)
                .whereEqualTo("creationDate", ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project.creationDate)
                .get()
                .addOnSuccessListener {querySnapshot ->
                    for (document in querySnapshot.documents) {
                        db.collection("ideas").document(document.id)
                            .update(project)
                            .addOnSuccessListener {
                                Toast.makeText(activity, "Project updated", Toast.LENGTH_SHORT).show()
                                v.findNavController().navigateUp()
                            }
                            .addOnFailureListener {
                                Toast.makeText(activity, "Error updating project", Toast.LENGTH_SHORT).show()
                            }
                    }
                }


        }
    }
}