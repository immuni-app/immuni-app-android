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

package it.ministerodellasalute.immuni.ui.greencertificate

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.ExternalLinksHelper
import it.ministerodellasalute.immuni.extensions.utils.coloredClickable
import it.ministerodellasalute.immuni.extensions.utils.formatDateString
import it.ministerodellasalute.immuni.extensions.utils.formatDateTimeString
import it.ministerodellasalute.immuni.extensions.view.getColorCompat
import it.ministerodellasalute.immuni.logic.greencovidcertificate.DecodeData
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.user.models.GreenCertificateUser
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.green_certificate_more_details.*
import kotlinx.android.synthetic.main.green_certificate_more_details_exemption.*
import kotlinx.android.synthetic.main.green_certificate_more_details_recovery.*
import kotlinx.android.synthetic.main.green_certificate_more_details_test.*
import kotlinx.android.synthetic.main.green_certificate_more_details_vaccination.*
import kotlinx.android.synthetic.main.green_certificate_more_details_vaccination.countryVaccination
import kotlinx.android.synthetic.main.green_certificate_more_details_vaccination.validityVaccine
import org.koin.android.ext.android.get
import org.koin.core.KoinComponent

class MoreDetailGreenCertificate : PopupDialogFragment(), KoinComponent {

    lateinit var settingsManager: ConfigurationSettingsManager

    lateinit var greenCertificateDetail: GreenCertificateUser

    val format = SimpleDateFormat("yyyy-MM-dd")
    val formatDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private val molecolarTest = "LP6464-4"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val argument = navArgs<MoreDetailGreenCertificateArgs>()
        settingsManager = get()
        greenCertificateDetail = argument.value.greenCertificate
        setContentLayout(R.layout.green_certificate_more_details)

        setTitle(getString(R.string.green_pass_more_details_title))

        val vaccineFullyCompleted = settingsManager.settings.value.eudcc_expiration[Locale.getDefault().language]!!["vaccine_fully_completed"]
        val vaccineFirstDose = settingsManager.settings.value.eudcc_expiration[Locale.getDefault().language]!!["vaccine_first_dose"]
        val rapidTest = settingsManager.settings.value.eudcc_expiration[Locale.getDefault().language]!!["rapid_test"]
        val molecularTest = settingsManager.settings.value.eudcc_expiration[Locale.getDefault().language]!!["molecular_test"]
        val healingCertificate = settingsManager.settings.value.eudcc_expiration[Locale.getDefault().language]!!["healing_certificate"]
        val vaccineBooster = settingsManager.settings.value.eudcc_expiration[Locale.getDefault().language]!!["vaccine_booster"]
        val cbis = settingsManager.settings.value.eudcc_expiration[Locale.getDefault().language]!!["cbis"]

        setUI(
            vaccineFullyCompleted,
            vaccineFirstDose,
            molecularTest,
            rapidTest,
            healingCertificate,
            vaccineBooster,
            cbis
        )
    }

    private fun setUI(validUntilCompleteVaccine: String?, validUntilnotCompleteVaccine: String?, validUntilMolecularTest: String?, validUntilQuickTest: String?, healingCertificate: String?, vaccineBooster: String?, cbis: String?) {
        var isExemption = false
        when (true) {
            greenCertificateDetail.data?.vaccinations != null -> {
                // Inflate layout dynamically
                includeDynamicView(R.layout.green_certificate_more_details_vaccination)

                subHeading.text = getString(R.string.green_certificate_subHeading_vaccine)
                vaccineType.text = setTextOrDefault(
                    DecodeData.vpFromCode(
                        greenCertificateDetail.data?.vaccinations?.get(0)!!.vaccine
                    )?.let {
                        getString(it)
                    })
                denomVaccine.text =
                    setTextOrDefault(
                        DecodeData.mpFromCode(
                            greenCertificateDetail.data?.vaccinations?.get(
                                0
                            )!!.medicinalProduct
                        )?.let {
                            getString(it)
                        })
                producerVaccine.text =
                    setTextOrDefault(
                        DecodeData.maFromCode(
                            greenCertificateDetail.data?.vaccinations?.get(
                                0
                            )!!.manufacturer
                        )
                            ?.let { getString(it) })
                validityVaccine.text =
                    if (greenCertificateDetail.data?.vaccinations?.get(0)!!.doseNumber < greenCertificateDetail.data?.vaccinations?.get(0)!!.totalSeriesOfDoses) {
                        validUntilnotCompleteVaccine ?: getString(R.string.green_certificate_validity_vaccine_partial)
                    } else if (greenCertificateDetail.data?.vaccinations?.get(0)!!.doseNumber == greenCertificateDetail.data?.vaccinations?.get(0)!!.totalSeriesOfDoses && greenCertificateDetail.data?.vaccinations?.get(0)!!.totalSeriesOfDoses < 3) {
                        validUntilCompleteVaccine ?: getString(R.string.green_certificate_validity_vaccine_complete)
                    } else {
                        vaccineBooster ?: getString(R.string.green_certificate_validity_vaccine_booster)
                    }
                dosesNumber.text = String.format(
                    requireContext().getString(R.string.green_certificate_more_details_doses_number_text),
                    setTextOrDefault(greenCertificateDetail.data?.vaccinations?.get(0)!!.doseNumber.toString()),
                    setTextOrDefault(greenCertificateDetail.data?.vaccinations?.get(0)!!.totalSeriesOfDoses.toString())
                )
                lastAdministration.text =
                    setTextOrDefault(
                        convertDate(
                            greenCertificateDetail.data?.vaccinations?.get(0)!!.dateOfVaccination,
                            format
                        )
                    )
                countryVaccination.text =
                    setTextOrDefault(greenCertificateDetail.data?.vaccinations?.get(0)!!.countryOfVaccination)
                certificateIssuerLabel.text =
                    getText(R.string.green_certificate_certificate_issuer_vaccination)
                certificateIssuerLabelExemption.visibility = View.GONE
            }
            greenCertificateDetail.data?.tests != null -> {
                // Inflate layout dynamically
                includeDynamicView(R.layout.green_certificate_more_details_test)
                subHeading.text = getString(R.string.green_certificate_subHeading_test)
                diseaseLabel.text = getString(R.string.green_certificate_disease)
                testType.text =
                    setTextOrDefault(
                        DecodeData.ttFromCode(greenCertificateDetail.data?.tests?.get(0)!!.typeOfTest)
                            ?.let {
                                getString(
                                    it
                                )
                            })
                resultTest.text =
                    setTextOrDefault(
                        DecodeData.trFromCode(greenCertificateDetail.data?.tests?.get(0)!!.testResult)
                            ?.let {
                                getString(
                                    it
                                )
                            })

                if (greenCertificateDetail.data?.tests?.get(0)!!.typeOfTest == molecolarTest) {
                    ratNameTestLabelEng.visibility = View.GONE
                    ratNameTestLabel.visibility = View.GONE
                    ratNameTest.visibility = View.GONE
                    validityTest.text = validUntilMolecularTest ?: getString(R.string.green_certificate_validity_Moleculartest)
                } else {
                    ratNameTest.text = setTextOrDefault(greenCertificateDetail.data?.tests?.get(0)!!.testNameAndManufacturer)
                    ratNameTestLabelEng.visibility = View.VISIBLE
                    ratNameTestLabel.visibility = View.VISIBLE
                    ratNameTest.visibility = View.VISIBLE
                    validityTest.text = validUntilQuickTest ?: getString(R.string.green_certificate_validity_Quicktest)
                }
                dateTimeSampleCollection.text =
                    setTextOrDefault(
                        convertDate(
                            greenCertificateDetail.data?.tests?.get(0)!!.dateTimeOfCollection,
                            formatDateTime
                        )
                    )
                testingCentre.text =
                    setTextOrDefault(greenCertificateDetail.data?.tests?.get(0)!!.testingCentre)
                countryTest.text =
                    setTextOrDefault(greenCertificateDetail.data?.tests?.get(0)!!.countryOfVaccination)
                certificateIssuerLabel.text = getText(R.string.green_certificate_certificate_issuer)
                certificateIssuerLabelExemption.visibility = View.GONE
            }
            greenCertificateDetail.data?.recoveryStatements != null -> {
                // Inflate layout dynamically
                includeDynamicView(R.layout.green_certificate_more_details_recovery)

                subHeading.text = getString(R.string.green_certificate_subHeading_recovery)
                diseaseLabelEng.text = getString(R.string.green_certificate_disease_recovery_label)
                diseaseLabel.text = getString(R.string.green_certificate_disease_recovery)
                dateOfFirstPositiveResult.text =
                    setTextOrDefault(
                        convertDate(
                            greenCertificateDetail.data?.recoveryStatements?.get(
                                0
                            )!!.dateOfFirstPositiveTest,
                            format
                        )
                    )
                countryTestRecovery.text =
                    setTextOrDefault(greenCertificateDetail.data?.recoveryStatements?.get(0)!!.countryOfVaccination)
                certificateValidFrom.text =
                    setTextOrDefault(greenCertificateDetail.data?.recoveryStatements?.get(0)!!.certificateValidFrom)
                certificateValidUntil.text =
                    setTextOrDefault(greenCertificateDetail.data?.recoveryStatements?.get(0)!!.certificateValidUntil)
                certificateIssuerLabelEng.visibility = View.GONE
                certificateIssuerLabel.visibility = View.GONE
                entityIssuedCertificate.visibility = View.GONE
                validityHealing.text = when (greenCertificateDetail.fglTipoDgc) {
                    "cbis" -> cbis ?: getString(R.string.green_certificate_validity_healing_cbis)
                    else -> healingCertificate ?: getString(R.string.green_certificate_validity_healing)
                }
                certificateIssuerLabelExemption.visibility = View.GONE
            }
            greenCertificateDetail.data?.exemptions != null -> {
                isExemption = true
                flagEsenzione.visibility = View.VISIBLE
                question.text = getString(R.string.green_certificate_exemption_title)
                // Inflate layout dynamically
                includeDynamicView(R.layout.green_certificate_more_details_exemption)

                subHeadingExemption.text = getString(R.string.green_certificate_subHeading_exemption)
                certifying_physician_exemption.text = greenCertificateDetail.data?.exemptions?.get(0)!!.fiscalCode
                exemptionValidFrom.text = greenCertificateDetail.data?.exemptions?.get(0)!!.certificateValidFrom
                exemptionValidUntil.text = setTextOrDefault(greenCertificateDetail.data?.exemptions?.get(0)!!.certificateValidUntil)
                uniqueCodeExemption.text = greenCertificateDetail.data?.exemptions?.get(0)!!.uniqueVaccinationExemptionIdentifier
            }
        }

        entityIssuedCertificate.text =
            getString(R.string.green_certificate_certificate_issuer_const)

        if (isExemption) {
            certificateIssuerLabel.visibility = View.GONE
            certificateIssuerLabelExemption.visibility = View.VISIBLE
            certificateIssuerLabel.text = getString(R.string.green_certificate_certificate_issuer_exemption)
            setTitle(getString(R.string.green_certificate_exemption_title))
            textFooter.visibility = View.GONE
            europeRestrictionSite.visibility = View.GONE
            textFooterExemption.visibility = View.VISIBLE
            subHeading.visibility = View.GONE
            subHeadingExemption.visibility = View.VISIBLE
        } else {
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
    }

    private fun includeDynamicView(layout: Int) {
        val v = layoutInflater.inflate(layout, null)
        container.addView(
            v,
            5,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun convertDate(date: String?, pattern: SimpleDateFormat): String? {
        return if (date != null) {
            if (pattern == formatDateTime) {
                date.formatDateTimeString(date)
            } else {
                date.formatDateString(date)
            }
        } else {
            null
        }
    }

    private fun setTextOrDefault(text: String?): String {
        return if (text.isNullOrBlank()) {
            "---"
        } else {
            return text
        }
    }
}
