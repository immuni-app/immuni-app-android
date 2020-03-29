package org.ascolto.onlus.geocrowd19.android.ui.home.family.details.edit

import android.os.Bundle
import androidx.lifecycle.Observer
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.user_edit_gender_activity.*
import org.ascolto.onlus.geocrowd19.android.AscoltoActivity
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.db.entity.Gender
import org.ascolto.onlus.geocrowd19.android.loading
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class EditGenderActivity : AscoltoActivity() {
    private lateinit var viewModel: EditDetailsViewModel
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_edit_gender_activity)
        setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))
        userId = intent?.extras?.getString("userId")!!
        viewModel = getViewModel { parametersOf(userId)}

        viewModel.navigateBack.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                finish()
            }
        })

        viewModel.user.observe(this, Observer {
            when(it.gender) {
                Gender.FEMALE -> {
                    female.isChecked = true
                    male.isChecked = false
                }
                Gender.MALE -> {
                    male.isChecked = true
                    female.isChecked = false
                }
            }

            pageTitle.text = when(it.isMain) {
                true -> applicationContext.resources.getString(R.string.onboarding_gender_title)
                false -> applicationContext.resources.getString(R.string.user_edit_gender_you_title)
            }
        })

        viewModel.loading.observe(this, Observer {
            loading(it)
        })

        back.setOnClickListener { finish() }

        update.setOnClickListener {
            val gender = when {
                female.isChecked -> Gender.FEMALE
                else -> Gender.MALE
            }

            val user = viewModel.user()
            user?.let {
                viewModel.updateUser(user.copy(gender = gender))
            }
        }
    }
}
