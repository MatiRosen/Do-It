package team.doit.do_it.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import team.doit.do_it.databinding.FragmentProfileBinding
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import team.doit.do_it.R
import java.io.File

class ProfileFragment : Fragment() {

    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View

    interface OnUserFetchedListener {
        fun onUserFetched(user: DocumentSnapshot?)
    }

    interface OnUserUpdatedListener {
        fun onUserUpdated(successful: Boolean)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        v = binding.root

        replaceData()

        return v
    }

    override fun onStart() {
        super.onStart()

        binding.imgProfileEditIcon.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragmentMain_to_profileEditFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun replaceData() {
        val creatorEmail = ProfileFragmentArgs.fromBundle(requireArguments()).CreatorEmail
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = if (creatorEmail == " "){
            currentUser?.email.toString()
        }else{
            creatorEmail
        }
        getUser(userEmail, object : OnUserFetchedListener {
            override fun onUserFetched(user: DocumentSnapshot?) {
                if (user != null) {
                    binding.txtProfileName.text = "${user.getString("nombre")} ${user.getString("apellido")}"
                    binding.txtProfileEmail.text = user.getString("email")
                    binding.txtProfilePhone.text = user.getString("telefono")
                    binding.txtProfileGender.text = user.getString("genero")
                    binding.txtProfileAddress.text = user.getString("direccion")
                    setImage(userEmail, user.getString("imgPerfil").toString())
                } else {
                    Toast.makeText(activity, resources.getString(R.string.profile_dataUser_error), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setImage(creatorEmail: String, titleImg: String) {
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$creatorEmail/imgProfile/$titleImg")
        val localFile = File.createTempFile("images", "jpg")
        storageReference.getFile(localFile)
            .addOnSuccessListener {
                val bitMap = BitmapFactory.decodeFile(localFile.absolutePath)
                binding.imgProfileCircular.setImageBitmap(bitMap)
            }
    }

    private fun getUser(email: String, listener: OnUserFetchedListener) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.documents[0]
                    listener.onUserFetched(user)
                } else {
                    listener.onUserFetched(null)
                }
            }
            .addOnFailureListener {
                listener.onUserFetched(null)
            }
    }

}