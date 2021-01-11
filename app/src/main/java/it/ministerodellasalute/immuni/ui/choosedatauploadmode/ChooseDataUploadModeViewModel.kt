package it.ministerodellasalute.immuni.ui.choosedatauploadmode

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.user.models.Region
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.KoinComponent

class ChooseDataUploadModeViewModel(
    val context: Context,
    val userManager: UserManager,
    val settingsManager: ConfigurationSettingsManager
) : ViewModel(),
    KoinComponent {
    val settings = settingsManager.settings.value

    private val _allowed_regions_self_upload = MutableLiveData<List<String>>()
    val allowedRegionsSelfUpload: LiveData<List<String>> = _allowed_regions_self_upload
    private val _region = MutableStateFlow<Region?>(null)
    val region: StateFlow<Region?> = _region

    init {
        userManager.user.value?.region?.let {
            _region.value = it
        }

        settings.allowed_regions_self_upload.let {
            _allowed_regions_self_upload.value = it
        }
    }
}
