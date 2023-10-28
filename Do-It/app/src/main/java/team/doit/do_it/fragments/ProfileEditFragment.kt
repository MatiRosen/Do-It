package team.doit.do_it.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
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
    private lateinit var spinnerGender : Spinner

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

        startSpinner()

        binding.btnConfirmEditProfile.setOnClickListener {
            if(isValidUser()) {
                updateUser(object : ProfileFragment.OnUserUpdatedListener {
                    override fun onUserUpdated(successful: Boolean) {
                        if (successful) {
                            safeAccessBinding {
                                Toast.makeText(activity, resources.getString(R.string.profile_editUser_complete), Toast.LENGTH_SHORT).show()
                                v.findNavController().navigateUp()
                            }

                        } else {
                            Toast.makeText(activity, resources.getString(R.string.profile_dataUser_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        }

        binding.txtEditProfileDeleteAccount.setOnClickListener {
            deleteAccountConfirm()
        }

        binding.imgCloseEditProfile.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.editImgEditProfileCircular.setOnClickListener {
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
                    binding.editImgEditProfileCircular.setImageURI(selectedImageFromGallery)
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
                        binding.editTextEditProfileName.text = editableFactory.newEditable(user.getString("nombre") ?: "")
                        binding.editTextEditProfileSurname.text = editableFactory.newEditable(user.getString("apellido") ?: "")
                        binding.editTextEditProfileEmail.text = editableFactory.newEditable(user.getString("email") ?: "")
                        binding.editTextEditProfilePhone.text = editableFactory.newEditable(user.getString("telefono") ?: "")
                        spinnerGender.setSelection(getGenderIndex(user.getString("genero")))
                        binding.editTextEditProfileAddress.text = editableFactory.newEditable(user.getString("direccion") ?: "")
                        setImage(currentUser?.email.toString(), user.getString("imgPerfil").toString())
                    }
                } else {
                    Toast.makeText(activity, resources.getString(R.string.profile_dataUser_error), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setImage(creatorEmail: String, titleImg: String) {

        Handler(Looper.getMainLooper()).postDelayed({
            safeAccessBinding {
                if (titleImg == "") {
                    binding.editImgEditProfileCircular.setImageResource(R.drawable.img_avatar)
                    return@safeAccessBinding
                }
                val storageReference = FirebaseStorage.getInstance().reference.child("images/$creatorEmail/imgProfile/$titleImg")

                Glide.with(v.context)
                    .load(storageReference)
                    .placeholder(R.drawable.img_avatar)
                    .error(R.drawable.img_avatar)
                    .into(binding.editImgEditProfileCircular)
            }
        }, 500)

    }

    private fun startSpinner() {
        spinnerGender = binding.editSpinnerEditProfileGender
        val genders = resources.getStringArray(R.array.genders).toMutableList()
        val hint = resources.getString(R.string.register_gender)
        genders.add(0, hint)

        val adapter = object : ArrayAdapter<String>(v.context, android.R.layout.simple_list_item_activated_1, genders) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)

                if (position == 0) {
                    (view as TextView).setTextColor(resources.getColor(R.color.medium_gray, null))
                    view.setBackgroundColor(0)
                }
                return view
            }
        }
        spinnerGender.adapter = adapter
    }

    private fun getGenderIndex(gender: String?): Int {
        var index = 1

        if (gender == "Mujer") {
            index = 2
        }

        if (gender == "Otro") {
            index = 3
        }

        return index
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

    private fun isValidUser(): Boolean {
        val propertiesToCheck = listOf(
            Pair(binding.editTextEditProfileName.text.toString(), resources.getString(R.string.register_name_error)),
            Pair(binding.editTextEditProfileSurname.text.toString(), resources.getString(R.string.register_surname_error)),
            Pair(binding.editTextEditProfilePhone.text.toString(), resources.getString(R.string.register_phone_error)),
            Pair(binding.editTextEditProfileAddress.text.toString(), resources.getString(R.string.register_address_error))
        )

        for ((property, errorMessage) in propertiesToCheck) {
            if (property.isEmpty() || property.isBlank()) {
                Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
                return false
            }
        }

        if (binding.editTextEditProfilePhone.text.toString().length != 10) {
            Toast.makeText(activity, resources.getString(R.string.register_phone_format_error), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
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
                        var imgUrl = userDoc.getString("imgPerfil").toString()

                        if(selectedImage != null) {
                            deleteProfileImage(user)
                            imgUrl = uploadImage(user.email.toString())
                        }

                        val updatedData = hashMapOf<String, Any>(
                            "nombre" to binding.editTextEditProfileName.text.toString(),
                            "apellido" to binding.editTextEditProfileSurname.text.toString(),
                            "telefono" to binding.editTextEditProfilePhone.text.toString(),
                            "genero" to spinnerGender.selectedItem.toString(),
                            "direccion" to binding.editTextEditProfileAddress.text.toString(),
                            "imgPerfil" to imgUrl
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

    private fun deleteProfileImage(user: FirebaseUser) {
        val creatorEmail = user.email.toString()
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$creatorEmail/imgProfile")

        storageReference.listAll()
            .addOnSuccessListener { listResult ->
                if (listResult.items.isNotEmpty()) {
                    listResult.items[0].delete()
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
            deleteProfileImage(user)
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