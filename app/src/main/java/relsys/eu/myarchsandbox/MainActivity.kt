package relsys.eu.myarchsandbox

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import relsys.eu.myarchsandbox.lifecycle.MainActivityLifecycleObserver
import relsys.eu.myarchsandbox.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivityLifecycleObserver(lifecycle)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)
        mainNavigation.setupWithNavController(navController)

        val appBarConfiguration = AppBarConfiguration(setOf(R.id.home_fragment, R.id.dashboard_fragment,
            R.id.notifications_fragment))
        toolbar.setupWithNavController(navController, appBarConfiguration)
    }

}
