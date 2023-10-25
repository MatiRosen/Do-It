package team.doit.do_it.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.stripe.android.PaymentConfiguration
import team.doit.do_it.R
import team.doit.do_it.databinding.ActivityMainBinding

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
        MobileAds.initialize(this)
    }
}