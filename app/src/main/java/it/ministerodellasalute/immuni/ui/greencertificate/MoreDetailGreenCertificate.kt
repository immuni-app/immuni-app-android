package it.ministerodellasalute.immuni.ui.greencertificate

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.navigation.fragment.navArgs
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.ExternalLinksHelper
import it.ministerodellasalute.immuni.extensions.utils.coloredClickable
import it.ministerodellasalute.immuni.extensions.view.getColorCompat
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.user.models.GreenCertificateUser
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment
import kotlinx.android.synthetic.main.green_certificate_more_details.*
import org.koin.android.ext.android.get
import org.koin.core.KoinComponent

class MoreDetailGreenCertificate : PopupDialogFragment(), KoinComponent {

    lateinit var settingsManager: ConfigurationSettingsManager

    lateinit var greenCertificateDetail: GreenCertificateUser

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val argument = navArgs<MoreDetailGreenCertificateArgs>()
        settingsManager = get()
        greenCertificateDetail = argument.value.greenCertificate
        setContentLayout(R.layout.green_certificate_more_details)

        setTitle(getString(R.string.green_pass_more_details_title))

        setUI()
    }

    private fun setUI() {
        diseaseOrtargetAgent.text = greenCertificateDetail.targetAgent
        vaccineType.text = greenCertificateDetail.typeOfVaccine
        denomVaccine.text = greenCertificateDetail.vaccineName
        producerVaccine.text = greenCertificateDetail.vaccineProducer
        // da vedere dove arrivano il num totale di dosi

        dosesNumber.text = String.format(
            requireContext().getString(R.string.green_certificate_more_details_doses_number_text),
            greenCertificateDetail.dosesNumber,
            greenCertificateDetail.totalDosesNumber
        )
//        dosesNumber.text = getString(R.string.green_certificate_more_details_doses_number_text, greenCertificateDetail.dosesNumber, greenCertificateDetail.totalDosesNumber)
        lastAdministration.text = greenCertificateDetail.dateOfLastVaccination
        countryVaccination.text = greenCertificateDetail.perfomedCountryVaccination
        entityIssuedCertificate.text = greenCertificateDetail.entityIssuedCertificate

        val europeRestrictionUrl = getString(R.string.green_certificate_more_details_europe_restriction_url)
        europeRestrictionSite.text = "{$europeRestrictionUrl}".coloredClickable(
            color = requireContext().getColorCompat(R.color.colorPrimary),
            bold = true
        ) {
            ExternalLinksHelper.openLink(
                requireContext(),
                europeRestrictionUrl
            )
        }
        europeRestrictionSite.movementMethod = LinkMovementMethod.getInstance()
    }
}
