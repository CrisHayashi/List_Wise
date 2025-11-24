package com.example.list_wise

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Pega o NavHostFragment do layout
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Configura o BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

        // Toolbar customizada (TextView)
        val titleView = findViewById<TextView>(R.id.txtToolbarTitle)

        // Atualiza o título conforme o fragment atual
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val newTitle = when (destination.id) {
                R.id.dashboardFragment -> getString(R.string.title_dashboard)
                R.id.listFragment -> getString(R.string.title_list)
                R.id.historicFragment -> getString(R.string.title_historic)
                R.id.storeLocatorFragment -> getString(R.string.title_store_locator)
                R.id.settingsFragment -> getString(R.string.title_settings)
                R.id.purchasedListFragment -> getString(R.string.title_purchased_list)
                else -> getString(R.string.app_name) // fallback
            }
            titleView.text = newTitle
        }
    }

    override fun onSupportNavigateUp(): Boolean {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            return navController.navigateUp() || super.onSupportNavigateUp()
    }
}