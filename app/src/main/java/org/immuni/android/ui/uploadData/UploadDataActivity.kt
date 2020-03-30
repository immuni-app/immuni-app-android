package org.immuni.android.ui.uploadData

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import org.immuni.android.AscoltoActivity
import org.immuni.android.R
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class UploadDataActivity : AscoltoActivity() {
    private lateinit var viewModel: UploadDataViewModel
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_data_activity)
        setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))
        userId = intent?.extras?.getString("userId")!!
        viewModel = getViewModel { parametersOf(userId)}

        findNavController(R.id.nav_host_fragment).setGraph(R.navigation.upload_data)
    }
}
