package relsys.eu.myarchsandbox

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import relsys.eu.myarchsandbox.lifecycle.MainActivityLifecycleObserver
import relsys.eu.myarchsandbox.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivityLifecycleObserver(lifecycle)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment.newInstance())
                .commitNow()
        }
    }

}
