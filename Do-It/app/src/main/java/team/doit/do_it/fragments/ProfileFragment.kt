package team.doit.do_it.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import team.doit.do_it.R
import team.doit.do_it.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View

    interface OnUserFetchedListener {
        fun onUserFetched(userDoc: DocumentSnapshot?)
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

        showProgressBar()
        replaceData()

        return v
    }

    private fun safeAccessBinding(action: () -> Unit) {
        if (_binding != null && context != null) {
            action()
        }
    }

    override fun onStart() {
        super.onStart()

        binding.imgProfileEditIcon.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragmentMain_to_profileEditFragment)
        }
        binding.imgBtnProfileCreatorBack.setOnClickListener{
            v.findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showProgressBar() {
        binding.progressBarProfile.visibility = View.VISIBLE
        binding.scrollViewProfileInfo.visibility = View.GONE
        binding.imgProfileCircular.visibility = View.GONE
        binding.imgBtnProfileCreatorBack.visibility = View.GONE
        binding.imgProfileLogo.visibility = View.GONE
        binding.imgProfileEditIcon.visibility = View.GONE
    }

    private fun hideProgressBar(isOwnProfile : Boolean) {
        binding.progressBarProfile.visibility = View.GONE
        binding.scrollViewProfileInfo.visibility = View.VISIBLE
        binding.imgProfileCircular.visibility = View.VISIBLE
        binding.imgProfileLogo.visibility = View.VISIBLE
        if (isOwnProfile) {
            binding.imgProfileEditIcon.visibility = View.VISIBLE
        } else {
            binding.imgBtnProfileCreatorBack.visibility = View.VISIBLE
        }
    }

    private fun replaceData() {
        val creatorEmail = ProfileFragmentArgs.fromBundle(requireArguments()).CreatorEmail
        val currentUser = FirebaseAuth.getInstance().currentUser
        var userEmail = currentUser?.email.toString()

        if (creatorEmail != " ") {
            userEmail = creatorEmail
        }

        getUser(userEmail, object : OnUserFetchedListener {
            override fun onUserFetched(userDoc: DocumentSnapshot?) {
                safeAccessBinding {
                    if (userDoc != null) {
                        val firstName = userDoc.getString("firstName") ?: ""
                        val surname = userDoc.getString("surname") ?: ""
                        binding.txtProfileName.text = getString(R.string.profile_name_format, firstName, surname)
                        binding.txtProfileEmail.text = userDoc.getString("email")
                        binding.txtProfilePhone.text = userDoc.getString("telephoneNumber")
                        binding.txtProfileGender.text = userDoc.getString("gender")
                        binding.txtProfileAddress.text = userDoc.getString("address")
                        setImage(userEmail, userDoc.getString("userImage").toString())
                        hideProgressBar(creatorEmail == " ")
                    } else {
                        Toast.makeText(activity, resources.getString(R.string.profile_dataUser_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun setImage(creatorEmail: String, titleImg: String) {
        safeAccessBinding {
            if (titleImg == "") {
                binding.imgProfileCircular.setImageResource(R.drawable.img_avatar)
                return@safeAccessBinding
            }
            val storageReference = FirebaseStorage.getInstance().reference.child("images/$creatorEmail/imgProfile/$titleImg")

            Glide.with(v.context)
                .load(storageReference)
                .placeholder(R.drawable.img_avatar)
                .error(R.drawable.img_avatar)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.imgProfileCircular)
        }
    }

    private fun getUser(email: String, listener: OnUserFetchedListener) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                safeAccessBinding {
                    if (!documents.isEmpty) {
                        val user = documents.documents[0]
                        listener.onUserFetched(user)
                    } else {
                        listener.onUserFetched(null)
                    }
                }
            }
            .addOnFailureListener {
                listener.onUserFetched(null)
            }
    }

}