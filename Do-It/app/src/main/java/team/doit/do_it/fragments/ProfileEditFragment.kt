package team.doit.do_it.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import team.doit.do_it.R
import team.doit.do_it.activities.LoginActivity
import team.doit.do_it.databinding.FragmentProfileEditBinding

class ProfileEditFragment : Fragment() {

    private var _binding : FragmentProfileEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        v = binding.root

        replaceData()

        return v
    }

    override fun onStart() {
        super.onStart()

        binding.btnConfirmEditProfile.setOnClickListener {
            updateUser(object : ProfileFragment.OnUserUpdatedListener {
                override fun onUserUpdated(successful: Boolean) {
                    if (successful) {
                        Toast.makeText(activity, resources.getString(R.string.profile_editUser_complete), Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
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
    }

    private fun replaceData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val editableFactory = Editable.Factory.getInstance()

        getUser(currentUser?.email.toString(), object : ProfileFragment.OnUserFetchedListener {
            override fun onUserFetched(user: DocumentSnapshot?) {
                if (user != null) {
                    //TODO: add profile photo
                    binding.editTextProfileName.text = editableFactory.newEditable(user.getString("nombre") ?: "")
                    binding.editTextProfileSurname.text = editableFactory.newEditable(user.getString("apellido") ?: "")
                    binding.editTextProfileEmail.text = editableFactory.newEditable(user.getString("email") ?: "")
                    binding.editTextProfilePhone.text = editableFactory.newEditable(user.getString("telefono") ?: "")
                    binding.editTextProfileGender.text = editableFactory.newEditable(user.getString("genero") ?: "")
                    binding.editTextProfileAddress.text = editableFactory.newEditable(user.getString("direccion") ?: "")
                } else {
                    Toast.makeText(activity, resources.getString(R.string.profile_dataUser_error), Toast.LENGTH_SHORT).show()
                }
            }
        })
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

                        val updatedData = hashMapOf<String, Any>(
                            "nombre" to binding.editTextProfileName.text.toString(),
                            "apellido" to binding.editTextProfileSurname.text.toString(),
                            "email" to binding.editTextProfileEmail.text.toString(),
                            "telefono" to binding.editTextProfilePhone.text.toString(),
                            "genero" to binding.editTextProfileGender.text.toString(),
                            "direccion" to binding.editTextProfileAddress.text.toString()
                        )

                        usersRef.update(updatedData)
                            .addOnSuccessListener {
                                listener.onUserUpdated(true)
                            }
                            .addOnFailureListener { exception ->
                                println(exception)
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

    private fun deleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser

        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity, resources.getString(R.string.profile_deleteAccount_complete), Toast.LENGTH_SHORT).show()
                    val intent = Intent(activity, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(activity, resources.getString(R.string.profile_deleteAccount_error), Toast.LENGTH_SHORT).show()
                }
            }
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

}