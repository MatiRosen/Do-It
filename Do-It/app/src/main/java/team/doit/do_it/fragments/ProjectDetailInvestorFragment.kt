package team.doit.do_it.fragments

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import team.doit.do_it.R
import team.doit.do_it.databinding.FragmentProjectDetailInvestorBinding
import team.doit.do_it.entities.ChatEntity
import team.doit.do_it.entities.InvestEntity
import team.doit.do_it.entities.ProjectEntity
import java.lang.IllegalStateException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class ProjectDetailInvestorFragment : Fragment() {

    private var _binding : FragmentProjectDetailInvestorBinding? = null
    private val binding get() = _binding!!
    private lateinit var v : View

    private var projectImage : String = ""
    private var creatorEmail : String = ""
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProjectDetailInvestorBinding.inflate(inflater, container, false)
        v = binding.root


        hideBottomNav()
        removeMargins()
        return v
    }

    private fun safeAccessBinding(action: () -> Unit) {
        if (_binding != null && context != null) {
            action()
        }
    }

    override fun onStart() {
        super.onStart()

        setValues()
        setupButtons()
    }

    private fun setupButtons(){
        binding.imgBtnProjectDetailInvestorBack.setOnClickListener {
            v.findNavController().navigateUp()
        }

        binding.linearLayoutProjectDetailInvestorData.setOnClickListener {
            val action = ProjectDetailInvestorFragmentDirections.actionProjectDetailInvestorFragmentToProfileFragment(creatorEmail)
            this.findNavController().navigate(action)
        }

        binding.btnProjectDetailInvestmentContact.setOnClickListener {
            goToChat()
        }

        binding.imgBtnProjectDetailInvestorFollowProject.setOnClickListener {
            followProject()
        }

        binding.imgBtnProjectDetailInvestorChat.setOnClickListener {
            goToChat()
        }
    }

    private fun goToChat(){
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("email", creatorEmail)
            .get()
            .addOnSuccessListener { documents ->
                safeAccessBinding {
                    if (!documents.isEmpty) {
                        val user = documents.documents[0]
                        val otherUserUUID = user.getString("uuid").toString()
                        val ref = Firebase.database.getReference("messages/${ownUserUUID}/${otherUserUUID}/messages")

                        ref.get().addOnCompleteListener {
                            if (it.isSuccessful) {
                                if (it.result?.children?.count() == 0) {
                                    createUserChat(ownUserUUID, user)
                                    return@addOnCompleteListener
                                }

                                openUserChat(ownUserUUID, otherUserUUID)
                            }
                        }
                    }
                }
            }
    }

    private fun createUserChat(ownUserUUID : String, user : DocumentSnapshot){
        val otherUserUUID = user.getString("uuid").toString()
        val ref = Firebase.database.getReference("messages/${ownUserUUID}/${otherUserUUID}")
        ref.child("userName").setValue("${user.getString("nombre")} ${user.getString("apellido")}")
        ref.child("userImage").setValue(user.getString("imgPerfil"))
        ref.child("userEmail").setValue(user.getString("email"))
        ref.child("userUUID").setValue(otherUserUUID)
        val currentTime = System.currentTimeMillis()
        ref.child("lastMessageDate").setValue(-currentTime)

        val chat = ChatEntity(
            "${user.getString("nombre")} ${user.getString("apellido")}",
            creatorEmail,
            user.getString("imgPerfil")!!,
            user.getString("uuid")!!,
            mutableListOf(),
            currentTime,
            false)

        val action = ProjectDetailInvestorFragmentDirections.actionProjectDetailInvestorFragmentToUserChatHome(chat)
        this.findNavController().navigate(action)
    }

    private fun openUserChat(ownUserUUID : String, otherUserUUID : String){
        val ref = Firebase.database.getReference("messages/$ownUserUUID/$otherUserUUID")

        ref.get().addOnSuccessListener {
            if (it.exists()) {
                val chat = ChatEntity(
                    it.child("userName").value.toString(),
                    it.child("userEmail").value.toString(),
                    it.child("userImage").value.toString(),
                    it.child("userUUID").value.toString(),
                    mutableListOf(),
                    it.child("lastMessageDate").value.toString().toLong(),
                    it.child("waiting").value.toString().toBoolean()
                )

                val action = ProjectDetailInvestorFragmentDirections.actionProjectDetailInvestorFragmentToUserChatHome(chat)
                this.findNavController().navigate(action)
            }
        }
    }

    private fun followProject(){
        val project = ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project
        val investorEmail = FirebaseAuth.getInstance().currentUser?.email.toString()

        if (project.isFollowedBy(investorEmail)){
            binding.imgBtnProjectDetailInvestorFollowProject.setImageResource(R.drawable.icon_follow_project)
            project.removeFollower(investorEmail)
            binding.txtProjectDetailInvestorFollowers.text = project.followersCount.toString()
        }else{
            binding.imgBtnProjectDetailInvestorFollowProject.setImageResource(R.drawable.icon_check)
            project.addFollower(investorEmail)
            binding.txtProjectDetailInvestorFollowers.text = project.followersCount.toString()
        }

        updateProject(project, investorEmail)
    }

    private fun updateProject(project: ProjectEntity, investorEmail: String){
        db.collection("ideas")
            .whereEqualTo("creatorEmail", project.creatorEmail)
            .whereEqualTo("creationDate", project.creationDate)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val projectRef = documents.documents[0].reference
                    projectRef.update("followers", project.followers)
                    projectRef.update("followersCount", project.followersCount)
                        .addOnSuccessListener {
                            safeAccessBinding {
                                val message =
                                    if (project.isFollowedBy(investorEmail))
                                        resources.getString(R.string.project_detail_follow_success)
                                    else
                                        resources.getString(R.string.project_detail_unfollow_success)
                                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            safeAccessBinding {
                                val message =
                                    if (project.isFollowedBy(investorEmail))
                                        resources.getString(R.string.project_detail_follow_error)
                                    else
                                        resources.getString(R.string.project_detail_unfollow_error)
                                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    safeAccessBinding {
                        val message = if (project.isFollowedBy(investorEmail)) resources.getString(R.string.project_detail_follow_error) else resources.getString(R.string.project_detail_unfollow_error)
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                safeAccessBinding {
                    val message =
                        if (project.isFollowedBy(investorEmail))
                            resources.getString(R.string.project_detail_follow_error)
                        else
                            resources.getString(R.string.project_detail_unfollow_error)
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveInvest(){
        val invest = createInvest() ?: return
        saveInvestToDatabase(invest)
    }

    private fun saveInvestToDatabase(invest: InvestEntity){
        db.collection("inversiones")
            .add(invest)
            .addOnSuccessListener {
                safeAccessBinding {
                    showSuccessMessage(invest)
                    hideBottomInvest()
                }
            }
            .addOnFailureListener {
                safeAccessBinding {
                    Snackbar.make(v, resources.getString(R.string.project_creation_failed), Snackbar.LENGTH_LONG).show()
                }
            }
    }
    private fun showSuccessMessage(invest: InvestEntity){
        val successMessage = resources.getString(R.string.project_detail_invest_success)
        Snackbar.make(v, successMessage, Snackbar.LENGTH_LONG).show()
    }
    private fun createInvest():InvestEntity? {
        val project = ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project
        val investorEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        val creatorEmail = project.creatorEmail
        val projectTitle = project.title

        //val budget = binding.txtProjectDetailBudgetInvestment.text.toString().toDoubleOrNull() ?: 0.0
        //val estado = resources.getString(R.string.project_detail_estado_pendiente)
        //val invest = InvestEntity(investorEmail,creatorEmail, budget, projectTitle,estado)
        //return if (validateInvest(invest,project.minBudget)) invest else null
        return null
    }
    private fun validateInvest(invest:InvestEntity,minBudget:Double) : Boolean{
        if (invest.getBudgetInvest() < minBudget){
            val txtMinbudget = formatMoney(minBudget)
            Snackbar.make(v, resources.getString(R.string.project_detail_budget_error,txtMinbudget), Snackbar.LENGTH_LONG).show()
            return false
        }

    return true
    }
    private fun setValues() {
        val project = ProjectDetailCreatorFragmentArgs.fromBundle(requireArguments()).project

        binding.txtProjectDetailInvestorTitle.text = project.title
        binding.txtProjectDetailInvestorSubtitle.text = project.subtitle
        binding.txtProjectDetailInvestorDescription.text = project.description
        binding.txtProjectDetailInvestorCategory.text = project.category
        binding.txtProjectDetailInvestorFollowers.text = project.followersCount.toString()

        val projectGoal = this.formatMoney(project.goal)
        val goalText = getString(R.string.project_detail_goal, projectGoal)
        binding.txtProjectDetailInvestorGoal.text = spannableText(goalText, goalText.indexOf(projectGoal[0]) - 1)

        val projectMinBudget = this.formatMoney(project.minBudget)
        val minBudgetText = getString(R.string.project_detail_min_budget, projectMinBudget)
        binding.txtProjectDetailInvestorMinBudget.text = spannableText(minBudgetText, minBudgetText.indexOf(projectMinBudget[0]) -1)

        projectImage = project.image
        creatorEmail = project.creatorEmail
        showOrHideInvest(project.title)
        this.setCreatorData()

        if (project.isFollowedBy(FirebaseAuth.getInstance().currentUser?.email.toString())){
            binding.imgBtnProjectDetailInvestorFollowProject.setImageResource(R.drawable.icon_check)
        }else{
            binding.imgBtnProjectDetailInvestorFollowProject.setImageResource(R.drawable.icon_follow_project)
        }
    }

    private fun setCreatorData() {
        binding.progressBarProjectDetailInvestor.visibility = View.VISIBLE
        binding.txtProjectDetailInvestorProfileName.visibility = View.GONE
        binding.imgProjectDetailInvestorProfileImage.visibility = View.GONE
        binding.imgBtnProjectDetailInvestorChat.visibility = View.GONE

        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("usuarios")

        usersRef.whereEqualTo("email", creatorEmail)
            .get()
            .addOnSuccessListener { documents ->
                safeAccessBinding {
                    if (!documents.isEmpty) {
                        val user = documents.documents[0]
                        binding.txtProjectDetailInvestorProfileName.text = user.getString("nombre")
                        binding.progressBarProjectDetailInvestor.visibility = View.GONE
                        binding.txtProjectDetailInvestorProfileName.visibility = View.VISIBLE
                        binding.imgProjectDetailInvestorProfileImage.visibility = View.VISIBLE
                        binding.imgBtnProjectDetailInvestorChat.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(activity, resources.getString(R.string.project_detail_error), Toast.LENGTH_SHORT).show()
                        v.findNavController().navigateUp()
                    }
                    setImage()
                    setUserCreatorImage(creatorEmail)
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, resources.getString(R.string.project_detail_error), Toast.LENGTH_SHORT).show()
                v.findNavController().navigateUp()
            }
    }

    private fun setImage() {
        if (projectImage == "") {
            binding.imgProjectDetailInvestorProjectImage.setImageResource(R.drawable.img_not_img)
            return
        }

        safeAccessBinding {
            val storageReference = FirebaseStorage.getInstance().reference.child("images/$creatorEmail/projects/$projectImage")
            Glide.with(v.context)
                .load(storageReference)
                .placeholder(R.drawable.img_not_img)
                .error(R.drawable.img_not_img)
                .into(binding.imgProjectDetailInvestorProjectImage)
        }
    }

    private fun setUserCreatorImage(creatorEmail: String) {
        getUser(creatorEmail, object : ProfileFragment.OnUserFetchedListener {
            override fun onUserFetched(user: DocumentSnapshot?) {
                safeAccessBinding {
                    if (user != null) {
                        val titleImg = user.getString("imgPerfil").toString()
                        if (titleImg == "") {
                            binding.imgProjectDetailInvestorProfileImage.setImageResource(R.drawable.img_avatar)
                            return@safeAccessBinding
                        }

                        val storageReference = FirebaseStorage.getInstance().reference.child("images/$creatorEmail/imgProfile/$titleImg")

                        Glide.with(v.context)
                            .load(storageReference)
                            .placeholder(R.drawable.img_avatar)
                            .error(R.drawable.img_avatar)
                            .into(binding.imgProjectDetailInvestorProfileImage)
                    } else {
                        Toast.makeText(activity, resources.getString(R.string.profile_dataUser_error), Toast.LENGTH_SHORT).show()
                    }
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

    private fun formatMoney(money: Double): String {
        val decimalFormatSymbols = DecimalFormatSymbols()
        decimalFormatSymbols.groupingSeparator = '.'
        decimalFormatSymbols.decimalSeparator = ','
        val decimalFormat = DecimalFormat("#,###.00", decimalFormatSymbols)
        return decimalFormat.format(money)
    }

    private fun spannableText(text: String, index: Int): SpannableString {
        val spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.green, null)),
            index,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    private fun hideBottomNav() {
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE
    }

    private fun removeMargins() {
        requireActivity().findViewById<FragmentContainerView>(R.id.mainHost)
            .layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun showMargins() {
        val constraintSet = ConstraintSet()
        constraintSet.connect(R.id.mainHost, ConstraintSet.TOP, R.id.guidelineMainActivityHorizontal3, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.mainHost, ConstraintSet.BOTTOM, R.id.bottomNavigationView, ConstraintSet.TOP)
        constraintSet.connect(R.id.mainHost, ConstraintSet.START, R.id.guidelineMainActivityVertical2, ConstraintSet.END)
        constraintSet.connect(R.id.mainHost, ConstraintSet.END, R.id.guidelineMainActivityVertical98, ConstraintSet.START)


        constraintSet.applyTo(requireActivity().findViewById(R.id.frameLayoutMainActivity))
    }

    private fun showBottomNav() {
        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.VISIBLE
    }
    private fun showBottomInvest(){
        requireActivity().findViewById<View>(R.id.btnProjectDetailInvestmentContact).visibility = View.VISIBLE
        //requireActivity().findViewById<View>(R.id.txtProjectDetailBudgetInvestment).visibility = View.VISIBLE
    }
    private fun hideBottomInvest(){
        requireActivity().findViewById<View>(R.id.btnProjectDetailInvestmentContact).visibility = View.GONE
        //requireActivity().findViewById<View>(R.id.txtProjectDetailBudgetInvestment).visibility = View.GONE
    }
    private fun showOrHideInvest(projectTitle : String){
        val investorEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        val investmentsRef = db.collection("inversiones")
        investmentsRef.whereEqualTo("creatorEmail", creatorEmail)
            .whereEqualTo("investorEmail",investorEmail)
            .whereEqualTo("projectTitle",projectTitle)
            .get()
            .addOnSuccessListener { documents ->
                safeAccessBinding {
                    if (documents.isEmpty) {
                        // showBottomInvest()
                    } else {
                        hideBottomInvest()
                    }
                }

            }
            .addOnFailureListener {
                Toast.makeText(activity, resources.getString(R.string.project_detail_error), Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        hideBottomNav()
        removeMargins()
    }

    override fun onStop(){
        super.onStop()
        showBottomNav()
        showMargins()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}