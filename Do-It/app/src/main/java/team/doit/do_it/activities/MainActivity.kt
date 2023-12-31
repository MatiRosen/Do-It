package team.doit.do_it.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.stripe.android.PaymentConfiguration
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import team.doit.do_it.R
import team.doit.do_it.databinding.ActivityMainBinding
import team.doit.do_it.entities.ChatEntity
import team.doit.do_it.listeners.NotificationReceivedListener
import team.doit.do_it.services.MyFirebaseMessagingService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navHostFragment : NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)

        RxJavaPlugins.setErrorHandler { }

        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51O2Oj4F9ihC2ndyiZuDGqW3GAZOJ7clYNQDAS2V0SXz3ayk1hp0cA17DVM6NGyxA6z3rDY0a51TpFUsk2SBnzOa300ofWboxrL"
        )

        initializeVariables()
        saveFCMToken()

    }

    private fun initializeVariables(){
        navHostFragment = supportFragmentManager.findFragmentById(R.id.mainHost) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(findViewById<BottomNavigationView>(R.id.bottomNavigationView), navController)

        checkMessages()

        MyFirebaseMessagingService.notificationReceivedListener.setOnNotificationReceivedListener(object :
            NotificationReceivedListener.OnNotificationReceivedListener {
            override fun onNotificationReceived() {
                runOnUiThread {
                    val bottomMenu = findViewById<BottomNavigationView>(R.id.bottomNavigationView).menu
                    bottomMenu.findItem(R.id.chat).icon = AppCompatResources.getDrawable(this@MainActivity, R.drawable.icon_notif_chat)
                }
            }
        })

        MobileAds.initialize(this)
    }

    private fun checkMessages(){
        val db = FirebaseDatabase.getInstance()
        val ownUserUUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = db.getReference("messages/$ownUserUUID")

        ref.get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (data in it.result!!.children) {
                    val chat = data.getValue(ChatEntity::class.java)
                    if (chat != null && chat.isWaiting) {
                        val bottomMenu = findViewById<BottomNavigationView>(R.id.bottomNavigationView).menu
                        bottomMenu.findItem(R.id.chat).icon = AppCompatResources.getDrawable(this, R.drawable.icon_notif_chat)

                        return@addOnCompleteListener
                    }
                }
            }
        }
    }

    fun hideBottomNav() {
        findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE
    }

    fun removeMargins() {
        findViewById<FragmentContainerView>(R.id.mainHost)
            .layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    fun showBottomNav() {
       findViewById<View>(R.id.bottomNavigationView).visibility = View.VISIBLE
    }

    fun showMargins() {
        val constraintSet = ConstraintSet()
        constraintSet.connect(R.id.mainHost, ConstraintSet.TOP, R.id.guidelineMainActivityHorizontal3, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.mainHost, ConstraintSet.BOTTOM, R.id.bottomNavigationView, ConstraintSet.TOP)
        constraintSet.connect(R.id.mainHost, ConstraintSet.START, R.id.guidelineMainActivityVertical2, ConstraintSet.END)
        constraintSet.connect(R.id.mainHost, ConstraintSet.END, R.id.guidelineMainActivityVertical98, ConstraintSet.START)


        constraintSet.applyTo(findViewById(R.id.frameLayoutMainActivity))
    }

    private fun saveFCMToken() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result
                if (currentUser != null) {
                    db.collection("usuarios").document(currentUser.email.toString())
                        .update("fcmToken", token)
                }
            }
        }
    }

}