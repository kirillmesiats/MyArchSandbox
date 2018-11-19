package relsys.eu.myarchsandbox

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.activity_main.*
import relsys.eu.myarchsandbox.lifecycle.MainActivityLifecycleObserver
import relsys.eu.myarchsandbox.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivityLifecycleObserver(lifecycle)
        setContentView(R.layout.activity_main)

        mainNavigation.setOnNavigationItemSelectedListener { menuItem ->
            val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
            when (menuItem.itemId) {
                R.id.navigation_home -> navController.navigate(R.id.homeFragment)
                R.id.navigation_dashboard -> navController.navigate(R.id.dashboardFragment)
                R.id.navigation_notifications -> navController.navigate(R.id.notificationsFragment)
                else -> {}
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

}
