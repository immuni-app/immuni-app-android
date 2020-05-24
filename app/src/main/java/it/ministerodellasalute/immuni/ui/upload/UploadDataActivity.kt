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

package it.ministerodellasalute.immuni.ui.upload

import android.os.Bundle
import androidx.navigation.findNavController
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.ui.ImmuniActivity
import org.koin.androidx.viewmodel.ext.android.getViewModel

class UploadDataActivity : ImmuniActivity() {

    private lateinit var viewModel: UploadViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightStatusBar(resources.getColor(R.color.background_darker))
        viewModel = getViewModel()

        setContentView(R.layout.nav_host_activity)
        findNavController(R.id.nav_host_fragment).setGraph(R.navigation.upload, intent.extras)
    }
}
