package it.ministerodellasalute.immuni.ui.greencertificate

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.ExternalLinksHelper
import it.ministerodellasalute.immuni.extensions.utils.coloredClickable
import it.ministerodellasalute.immuni.extensions.utils.isoDateString
import it.ministerodellasalute.immuni.extensions.view.getColorCompat
import it.ministerodellasalute.immuni.logic.greencovidcertificate.enum.DecodeData
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.user.models.GreenCertificateUser
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment
import java.text.SimpleDateFormat
import kotlinx.android.synthetic.main.green_certificate_more_details.*
import kotlinx.android.synthetic.main.green_certificate_more_details_recovery.*
import kotlinx.android.synthetic.main.green_certificate_more_details_test.*
import kotlinx.android.synthetic.main.green_certificate_more_details_vaccination.*
import org.koin.android.ext.android.get
import org.koin.core.KoinComponent

class MoreDetailGreenCertificate : PopupDialogFragment(), KoinComponent {

    lateinit var settingsManager: ConfigurationSettingsManager

    lateinit var greenCertificateDetail: GreenCertificateUser

    val format = SimpleDateFormat("yyyy-MM-dd")

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
        when (true) {
            greenCertificateDetail.data?.vaccinations != null -> {
                // Inflate layout dynamically
                includeDynamicView(R.layout.green_certificate_more_details_vaccination)

                subHeading.text = getString(R.string.green_certificate_subHeading_vaccine)
                vaccineType.text = DecodeData.maFromCode(greenCertificateDetail.data?.vaccinations?.get(0)!!.manufacturer)?.let {
                        getString(it)
                    } ?: "-"
                denomVaccine.text =
                    DecodeData.vpFromCode(greenCertificateDetail.data?.vaccinations?.get(0)!!.vaccine)?.let {
                        getString(it)
                    } ?: "-"
                producerVaccine.text =
                    DecodeData.mpFromCode(greenCertificateDetail.data?.vaccinations?.get(0)!!.medicinalProduct)
                        ?.let { getString(it) } ?: "-"
                dosesNumber.text = String.format(
                    requireContext().getString(R.string.green_certificate_more_details_doses_number_text),
                    greenCertificateDetail.data?.vaccinations?.get(0)!!.doseNumber,
                    greenCertificateDetail.data?.vaccinations?.get(0)!!.totalSeriesOfDoses
                )
                lastAdministration.text =
                    convertDate(greenCertificateDetail.data?.vaccinations?.get(0)!!.dateOfVaccination) ?: "-"
                countryVaccination.text =
                    greenCertificateDetail.data?.vaccinations?.get(0)!!.countryOfVaccination
                entityIssuedCertificate.text =
                    greenCertificateDetail.data?.vaccinations?.get(0)!!.certificateIssuer
            }
            greenCertificateDetail.data?.tests != null -> {
                // Inflate layout dynamically
                includeDynamicView(R.layout.green_certificate_more_details_test)

                subHeading.text = getString(R.string.green_certificate_subHeading_test)
                testType.text =
                    DecodeData.ttFromCode(greenCertificateDetail.data?.tests?.get(0)!!.typeOfTest)?.let {
                        getString(
                            it
                        )
                    } ?: "-"
                resultTest.text =
                    DecodeData.trFromCode(greenCertificateDetail.data?.tests?.get(0)!!.testResult)?.let {
                        getString(
                            it
                        )
                    } ?: "-"

                if (greenCertificateDetail.data?.tests?.get(0)!!.testName != null) {
                    nameNaa.text = greenCertificateDetail.data?.tests?.get(0)!!.testName ?: "-"
                    nameNaaLabel.visibility = View.VISIBLE
                    nameNaa.visibility = View.VISIBLE
                    naaNameTestLabelEng.visibility = View.VISIBLE
                } else {
                    ratNameTest.text =
                        greenCertificateDetail.data?.tests?.get(0)!!.testNameAndManufacturer ?: "-"
                    ratNameTestLabelEng.visibility = View.VISIBLE
                    ratNameTestLabel.visibility = View.VISIBLE
                    ratNameTest.visibility = View.VISIBLE
                }
                dateTimeSampleCollection.text =
                    convertDate(greenCertificateDetail.data?.tests?.get(0)!!.dateTimeOfCollection) ?: "-"
                dateTimeTestResult.text =
                    convertDate(greenCertificateDetail.data?.tests?.get(0)!!.dateTimeOfTestResult) ?: "-"
                testingCentre.text = greenCertificateDetail.data?.tests?.get(0)!!.testingCentre
                countryTest.text = greenCertificateDetail.data?.tests?.get(0)!!.countryOfVaccination
                entityIssuedCertificate.text =
                    greenCertificateDetail.data?.tests?.get(0)!!.certificateIssuer
            }
            greenCertificateDetail.data?.recoveryStatements != null -> {
                // Inflate layout dynamically
                includeDynamicView(R.layout.green_certificate_more_details_recovery)

                subHeading.text = getString(R.string.green_certificate_subHeading_recovery)
                dateOfFirstPositiveResult.text =
                    convertDate(greenCertificateDetail.data?.recoveryStatements?.get(0)!!.dateOfFirstPositiveTest) ?: "-"
                countryTestRecovery.text =
                    greenCertificateDetail.data?.recoveryStatements?.get(0)!!.countryOfVaccination
                certificateValidFrom.text =
                    greenCertificateDetail.data?.recoveryStatements?.get(0)!!.certificateValidFrom
                certificateValidUntil.text =
                    greenCertificateDetail.data?.recoveryStatements?.get(0)!!.certificateValidUntil
                entityIssuedCertificate.text =
                    greenCertificateDetail.data?.recoveryStatements?.get(0)!!.certificateIssuer
            }
        }

        val europeRestrictionUrl =
            getString(R.string.green_certificate_more_details_europe_restriction_url)
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

    private fun includeDynamicView(layout: Int) {
        val v = layoutInflater.inflate(layout, null)
        container.addView(
            v,
            4,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun convertDate(date: String?): String? {
        return if (date != null) {
            format.parse(date)!!.isoDateString
        } else {
            null
        }
    }
}
