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

        // NavHostFragment
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Deixa o NavigationUI sincronizar seleção x destino
        // (ele adiciona o listener de destinationChanged pra marcar o item certo)
        bottomNavigationView.setupWithNavController(navController)

        // Agora controlamos o QUE acontece quando clica em cada item
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboardFragment -> {
                    // Volta pro Dashboard "raiz"
                    navController.popBackStack(R.id.dashboardFragment, false)
                    if (navController.currentDestination?.id != R.id.dashboardFragment) {
                        navController.navigate(R.id.dashboardFragment)
                    }
                    true
                }

                R.id.historicFragment -> {
                    navController.popBackStack(R.id.historicFragment, false)
                    if (navController.currentDestination?.id != R.id.historicFragment) {
                        navController.navigate(R.id.historicFragment)
                    }
                    true
                }

                R.id.storeLocatorFragment -> {
                    navController.popBackStack(R.id.storeLocatorFragment, false)
                    if (navController.currentDestination?.id != R.id.storeLocatorFragment) {
                        navController.navigate(R.id.storeLocatorFragment)
                    }
                    true
                }

                R.id.settingsFragment -> {
                    navController.popBackStack(R.id.settingsFragment, false)
                    if (navController.currentDestination?.id != R.id.settingsFragment) {
                        navController.navigate(R.id.settingsFragment)
                    }
                    true
                }

                else -> false
            }
        }

        // (Opcional) Não precisamos de comportamento extra em re-seleção,
        // então podemos deixar sem setOnItemReselectedListener.

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
                else -> getString(R.string.app_name)
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

