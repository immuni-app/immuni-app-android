package org.ascolto.onlus.geocrowd19.android.ui.home.family.details.edit

import android.os.Bundle
import androidx.lifecycle.Observer
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.user_edit_age_group_activity.*
import org.ascolto.onlus.geocrowd19.android.AscoltoActivity
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.loading
import org.ascolto.onlus.geocrowd19.android.models.AgeGroup
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class EditAgeGroupActivity : AscoltoActivity() {
    private lateinit var viewModel: EditDetailsViewModel
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_edit_age_group_activity)
        setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))
        userId = intent?.extras?.getString("userId")!!
        viewModel = getViewModel { parametersOf(userId)}

        viewModel.navigateBack.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                finish()
            }
        })

        viewModel.user.observe(this, Observer {
            age_range_0_17.isChecked = false
            age_range_18_35.isChecked = false
            age_range_36_45.isChecked = false
            age_range_46_55.isChecked = false
            age_range_56_65.isChecked = false
            age_range_66_75.isChecked = false
            age_range_75.isChecked = false

            when(it.ageGroup) {
                AgeGroup.ZERO_SEVENTEEN -> age_range_0_17.isChecked = true
                AgeGroup.EIGHTEEN_THIRTYFIVE -> age_range_18_35.isChecked = true
                AgeGroup.THRITYSIX_FORTYFIVE -> age_range_36_45.isChecked = true
                AgeGroup.FORTYSIX_FIFTYFIVE -> age_range_46_55.isChecked = true
                AgeGroup.FIFTYSIX_SIXTYFIVE -> age_range_56_65.isChecked = true
                AgeGroup.SIXTYSIX_SEVENTYFIVE -> age_range_66_75.isChecked = true
                AgeGroup.MORE_THAN_SEVENTYFIVE -> age_range_75.isChecked = true
            }

        })

        viewModel.loading.observe(this, Observer {
            loading(it)
        })

        back.setOnClickListener { finish() }

        update.setOnClickListener {
            val ageGroup = when {
                age_range_0_17.isChecked -> AgeGroup.ZERO_SEVENTEEN
                age_range_18_35.isChecked -> AgeGroup.EIGHTEEN_THIRTYFIVE
                age_range_36_45.isChecked -> AgeGroup.THRITYSIX_FORTYFIVE
                age_range_46_55.isChecked -> AgeGroup.FORTYSIX_FIFTYFIVE
                age_range_56_65.isChecked -> AgeGroup.FIFTYSIX_SIXTYFIVE
                age_range_66_75.isChecked -> AgeGroup.SIXTYSIX_SEVENTYFIVE
                else -> AgeGroup.MORE_THAN_SEVENTYFIVE
            }

            val user = viewModel.user()
            user?.let {
                viewModel.updateUser(user.copy(ageGroup = ageGroup))
            }
        }
    }
}
