/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.ui.main

import android.os.Bundle
import androidx.core.view.iterator
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.ui.ImmuniActivity
import it.ministerodellasalute.immuni.ui.main.navigation.setupWithNavController
import kotlinx.android.synthetic.main.home_activity.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity : ImmuniActivity() {

    private var currentNavController: LiveData<NavController>? = null
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        viewModel = getViewModel()

        bottom_nav.itemIconTintList = null

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
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

        val navGraphIds = listOf(R.navigation.home, R.navigation.settings)
        val menuItemsIds = listOf(R.id.home_nav, R.id.settings_nav)
        val defaultIconsIds =
            listOf(R.drawable.ic_home_unselected, R.drawable.ic_settings_unselected)
        val selectedIconsIds = listOf(R.drawable.ic_home_selected, R.drawable.ic_settings_selected)

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
            // setupActionBarWithNavController(navController)
        })
        currentNavController = controller

        // update icons
        bottom_nav.menu.iterator().forEach {
            if (it.isChecked) {
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
