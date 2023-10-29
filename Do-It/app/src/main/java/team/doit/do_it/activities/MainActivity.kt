package team.doit.do_it.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.get
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.stripe.android.PaymentConfiguration
import team.doit.do_it.R
import team.doit.do_it.databinding.ActivityMainBinding
import team.doit.do_it.entities.ChatEntity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navHostFragment : NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)

        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51O2Oj4F9ihC2ndyiZuDGqW3GAZOJ7clYNQDAS2V0SXz3ayk1hp0cA17DVM6NGyxA6z3rDY0a51TpFUsk2SBnzOa300ofWboxrL"
        )

        initializeVariables()

    }

    private fun initializeVariables(){
        navHostFragment = supportFragmentManager.findFragmentById(R.id.mainHost) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(findViewById<BottomNavigationView>(R.id.bottomNavigationView), navController)
        // TODO ver FCM de firebase para resolver esto.
        //checkMessages()
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
}