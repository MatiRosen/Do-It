package team.doit.do_it.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import team.doit.do_it.R
import team.doit.do_it.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navHostFragment : NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)

        initializeVariables()
    }

    private fun initializeVariables(){
        navHostFragment = supportFragmentManager.findFragmentById(R.id.mainHost) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(findViewById<BottomNavigationView>(R.id.bottomNavigationView), navController)
    }
}