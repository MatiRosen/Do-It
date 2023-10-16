package team.doit.do_it.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import team.doit.do_it.R
import team.doit.do_it.activities.LoginActivity
import team.doit.do_it.databinding.FragmentProfileEditBinding
import java.io.File
import java.util.Date

class ProfileEditFragment : Fragment() {

    private var _binding : FragmentProfileEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View

    private var selectedImage: Uri? = null
    private var photoFile: File? = null
    private var lastImage: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        v = binding.root

        replaceData()

        return v
    }

    private fun safeAccessBinding(action: () -> Unit) {
        if (_binding != null) {
            action()
        }
    }

    override fun onStart() {
        super.onStart()

        binding.btnConfirmEditProfile.setOnClickListener {
            updateUser(object : ProfileFragment.OnUserUpdatedListener {
                override fun onUserUpdated(successful: Boolean) {
                    if (successful) {
                        safeAccessBinding {
                            Toast.makeText(activity, resources.getString(R.string.profile_editUser_complete), Toast.LENGTH_SHORT).show()

                            //TODO: al editar el perfil, te devuelve al perfil pero no siempre carga la foto que acabas de modificar
                            replaceData()
                        }

                    } else {
                        Toast.makeText(activity, resources.getString(R.string.profile_dataUser_error), Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        binding.txtDeleteAccount.setOnClickListener {
            deleteAccountConfirm()
        }

        binding.imgCloseEditProfile.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.imgProfileCircular.setOnClickListener {
            pickImage()
        }
    }

    private fun pickImage() {
        photoFile = pickImageFromGallery()
    }

    private fun pickImageFromGallery() : File?{
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
        return selectedImage?.let { it.path?.let { it1 -> File(it1) } }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if(data != null) run {
                val selectedImageFromGallery: Uri? = data.data
                selectedImage = selectedImageFromGallery
                if (selectedImageFromGallery != null) {
                    binding.imgProfileCircular.setImageURI(selectedImageFromGallery)
                }
            }
        }
    }

    private fun uploadImage(projectCreatorEmail: String): String {
        var fileName = projectCreatorEmail + "-imgProfile-" + Date().time.toString()

        val storeReference = FirebaseStorage.getInstance().getReference("images/$projectCreatorEmail/imgProfile/$fileName")
        storeReference.putFile(selectedImage!!)
            .addOnSuccessListener {
                lastImage = "images/$projectCreatorEmail/imgProfile/$fileName"
            }.addOnFailureListener {
                Snackbar.make(v, resources.getString(R.string.project_creation_image_upload_failed), Snackbar.LENGTH_LONG).show()
                fileName = ""
            }
        return fileName
    }

    private fun replaceData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val editableFactory = Editable.Factory.getInstance()

        getUser(currentUser?.email.toString(), object : ProfileFragment.OnUserFetchedListener {
            override fun onUserFetched(user: DocumentSnapshot?) {
                if (user != null) {
                    safeAccessBinding {
                        binding.editTextProfileName.text = editableFactory.newEditable(user.getString("nombre") ?: "")
                        binding.editTextProfileSurname.text = editableFactory.newEditable(user.getString("apellido") ?: "")
                        binding.editTextProfileEmail.text = editableFactory.newEditable(user.getString("email") ?: "")
                        binding.editTextProfilePhone.text = editableFactory.newEditable(user.getString("telefono") ?: "")
                        binding.editTextProfileGender.text = editableFactory.newEditable(user.getString("genero") ?: "")
                        binding.editTextProfileAddress.text = editableFactory.newEditable(user.getString("direccion") ?: "")
                        setImage(currentUser?.email.toString(), user.getString("imgPerfil").toString())
                    }
                } else {
                    Toast.makeText(activity, resources.getString(R.string.profile_dataUser_error), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setImage(creatorEmail: String, titleImg: String) {
        if (titleImg == "") {
            binding.imgProfileCircular.setImageResource(R.drawable.img_avatar)
            return
        }
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$creatorEmail/imgProfile/$titleImg")

        Glide.with(v.context)
            .load(storageReference)
            .placeholder(R.drawable.img_avatar)
            .error(R.drawable.img_avatar)
            .into(binding.imgProfileCircular)

    }

    private fun getUser(email: String, listener: ProfileFragment.OnUserFetchedListener) {
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

    private fun updateUser(listener: ProfileFragment.OnUserUpdatedListener) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        currentUser?.let { user ->
            getUser(user.email.toString(), object : ProfileFragment.OnUserFetchedListener {
                override fun onUserFetched(userDoc: DocumentSnapshot?) {
                    userDoc?.let { userSnapshot ->
                        val userId = userSnapshot.id
                        val usersRef = db.collection("usuarios").document(userId)

                        deleteProfileImages(user)

                        val updatedData = hashMapOf<String, Any>(
                            "nombre" to binding.editTextProfileName.text.toString(),
                            "apellido" to binding.editTextProfileSurname.text.toString(),
                            "email" to binding.editTextProfileEmail.text.toString(),
                            "telefono" to binding.editTextProfilePhone.text.toString(),
                            "genero" to binding.editTextProfileGender.text.toString(),
                            "direccion" to binding.editTextProfileAddress.text.toString(),
                            "imgPerfil" to if(selectedImage != null) uploadImage(user.email.toString()) else userDoc.getString("imgPerfil").toString()
                        )
                        // TODO para que se actualice la foto al volver atrás, el problema está aca!!

                        usersRef.update(updatedData)
                            .addOnSuccessListener {
                                listener.onUserUpdated(true)
                            }
                            .addOnFailureListener {
                                listener.onUserUpdated(false)
                            }
                    } ?: run {
                        Toast.makeText(activity, resources.getString(R.string.profile_dataUser_error), Toast.LENGTH_SHORT).show()
                        listener.onUserUpdated(false)
                    }
                }
            })
        }
    }

    private fun deleteProfileImages(user: FirebaseUser) {
        val creatorEmail = user.email.toString()
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$creatorEmail/imgProfile")

        storageReference.listAll()
            .addOnSuccessListener { listResult ->
                for (item in listResult.items) {
                    item.delete()
                        .addOnFailureListener {
                            resources.getString(R.string.profile_deleteImages_error)
                        }
                }
            }
    }

    private fun deleteAccount() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        currentUser?.let { user ->
            deleteIdeas(db, user)
            deleteUsers(db, user)
            deleteIdeasImages(user)
            deleteProfileImages(user)
            deleteUserAccount(user)
        }
    }

    private fun deleteIdeas(db: FirebaseFirestore, user: FirebaseUser) {
        db.collection("ideas")
            .whereEqualTo("creatorEmail", user.email.toString())
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    db.collection("ideas").document(document.id)
                        .delete()
                        .addOnFailureListener {
                            handleDeleteFailure()
                        }
                }
            }
    }

    private fun deleteUsers(db: FirebaseFirestore, user: FirebaseUser) {
        db.collection("usuarios")
            .whereEqualTo("email", user.email.toString())
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    db.collection("usuarios").document(document.id)
                        .delete()
                        .addOnFailureListener {
                            handleDeleteFailure()
                        }
                }
            }
    }

    private fun deleteIdeasImages(user: FirebaseUser) {
        val creatorEmail = user.email.toString()
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$creatorEmail/projects")

        storageReference.listAll()
            .addOnSuccessListener { listResult ->
                for (item in listResult.items) {
                    item.delete()
                        .addOnFailureListener {
                            resources.getString(R.string.profile_deleteImages_error)
                        }
                }
            }
    }

    private fun deleteUserAccount(user: FirebaseUser) {
        user.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.profile_deleteAccount_complete),
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(activity, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    handleDeleteFailure()
                }
            }
    }

    private fun handleDeleteFailure() {
        Toast.makeText(
            activity,
            resources.getString(R.string.profile_deleteAccount_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun deleteAccountConfirm() {
        val alertDialogBuilder = AlertDialog.Builder(activity)

        alertDialogBuilder.setTitle(resources.getString(R.string.profile_deleteAccount_title))
        alertDialogBuilder.setMessage(resources.getString(R.string.profile_deleteAccount_message))

        alertDialogBuilder.setPositiveButton(resources.getString(R.string.profile_deleteAccount_accept)) { _, _ ->
            deleteAccount()
        }

        alertDialogBuilder.setNegativeButton(resources.getString(R.string.profile_deleteAccount_decline)) { _, _ ->

        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}