package com.bendingspoons.ascolto.ui.home

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.core.view.iterator
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.bendingspoons.ascolto.AscoltoActivity
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.toast
import com.bendingspoons.ascolto.ui.home.navigation.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.home_activity.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class HomeActivity : AscoltoActivity()  {

    private var currentNavController: LiveData<NavController>? = null
    private lateinit var viewModel: HomeSharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        viewModel = getViewModel()

        bottom_nav.itemIconTintList = null

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)

        val navGraphIds = listOf(R.navigation.home, R.navigation.family, R.navigation.settings)
        val menuItemsIds = listOf(R.id.home, R.id.family, R.id.settings)
        val defaultIconsIds = listOf(R.drawable.ic_tab_home, R.drawable.ic_tab_family, R.drawable.ic_tab_settings)
        val selectedIconsIds = listOf(R.drawable.ic_tab_home_selected, R.drawable.ic_tab_family_selected, R.drawable.ic_tab_settings_selected)

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            menuItemsIds = menuItemsIds,
            defaultIconsIds = defaultIconsIds,
            selectedIconsIds = selectedIconsIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent
        )

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, Observer { navController ->
            //setupActionBarWithNavController(navController)
        })
        currentNavController = controller

        // update icons
        bottom_nav.menu.iterator().forEach {
            if(it.isChecked) {
                it.setIcon(selectedIconsIds[menuItemsIds.indexOf(it.itemId)])
            } else {
                it.setIcon(defaultIconsIds[menuItemsIds.indexOf(it.itemId)])
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }
}
