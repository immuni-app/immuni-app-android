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

package it.ministerodellasalute.immuni.logic.greencovidcertificate

import it.ministerodellasalute.immuni.R

object DecodeData {

    // TEST DATA
    private val tt = mapOf(
        "LP6464-4" to R.string.green_certificate_molecolar_test_label,
        "LP217198-3" to R.string.green_certificate_rapid_test_label
    )

    private val tr = mapOf(
        "260415000" to R.string.green_certificate_negative_label,
        "260373001" to R.string.green_certificate_positive_label
    )

    // VACCINE DATA
    private val vp = mapOf(
        "1119349007" to R.string.green_certificate_mrna_vaccine_label,
        "1119305005" to R.string.green_certificate_antigen_vaccine_label,
        "J07BX03" to R.string.green_certificate_covid19_vaccine_label
    )

    private val ma = mapOf(
        "ORG-100001699" to R.string.green_certificate_astrazeneca,
        "ORG-100030215" to R.string.green_certificate_biontech,
        "ORG-100001417" to R.string.green_certificate_janssen,
        "ORG-100031184" to R.string.green_certificate_moderna,
        "ORG-100006270" to R.string.green_certificate_curevac_ag,
        "ORG-100013793" to R.string.green_certificate_cansino,
        "ORG-100020693" to R.string.green_certificate_china_sinopharm,
        "ORG-100010771" to R.string.green_certificate_sinopharm_weigida,
        "ORG-100024420" to R.string.green_certificate_sinopharm_zhijun,
        "ORG-100032020" to R.string.green_certificate_novavax_cz,
        "Gamaleya-Research-Institute" to R.string.green_certificate_gamaleya,
        "Vector-Institute" to R.string.green_certificate_vector_institute,
        "Sinovac-Biotech" to R.string.green_certificate_sinovac,
        "Bharat-Biotech" to R.string.green_certificate_bharat
    )

    private val mp = mapOf(
        "ORG-100001699" to R.string.green_certificate_astrazeneca,
        "ORG-100030215" to R.string.green_certificate_biontech,
        "ORG-100001417" to R.string.green_certificate_janssen,
        "ORG-100031184" to R.string.green_certificate_moderna,
        "ORG-100006270" to R.string.green_certificate_curevac_ag,
        "ORG-100013793" to R.string.green_certificate_cansino,
        "ORG-100020693" to R.string.green_certificate_china_sinopharm,
        "ORG-100010771" to R.string.green_certificate_sinopharm_weigida,
        "ORG-100024420" to R.string.green_certificate_sinopharm_zhijun,
        "ORG-100032020" to R.string.green_certificate_novavax_cz,
        "Gamaleya-Research-Institute" to R.string.green_certificate_gamaleya,
        "Vector-Institute" to R.string.green_certificate_vector_institute,
        "Sinovac-Biotech" to R.string.green_certificate_sinovac,
        "Bharat-Biotech" to R.string.green_certificate_bharat,
        "EU/1/20/1528" to R.string.green_certificate_vac_comirnaty,
        "EU/1/20/1507" to R.string.green_certificate_vac_moderna,
        "EU/1/21/1529" to R.string.green_certificate_vac_vaxzevria,
        "EU/1/20/1525" to R.string.green_certificate_vac_janssen,
        "CVnCoV" to R.string.green_certificate_vac_cvncov,
        "Sputnik-V" to R.string.green_certificate_vac_sputnik_v,
        "Convidecia" to R.string.green_certificate_vac_convidecia,
        "EpiVacCorona" to R.string.green_certificate_vac_epi_vac_corona,
        "BBIBP-CorV" to R.string.green_certificate_vac_bbibp_corv,
        "Inactivated-SARS-CoV-2-Vero-Cell" to R.string.green_certificate_vac_vero_cell,
        "CoronaVac" to R.string.green_certificate_vac_corona_vac,
        "Covaxin" to R.string.green_certificate_vac_covaxin
    )

    fun ttFromCode(code: String): Int? {
        return if (tt.containsKey(code)) {
            tt.getValue(code)
        } else {
            null
        }
    }

    fun trFromCode(code: String): Int? {
        return if (tr.containsKey(code)) {
            tr.getValue(code)
        } else {
            null
        }
    }

    fun vpFromCode(code: String): Int? {
        return if (vp.containsKey(code)) {
            vp.getValue(code)
        } else {
            null
        }
    }

    fun maFromCode(code: String): Int? {
        return if (ma.containsKey(code)) {
            ma.getValue(code)
        } else {
            null
        }
    }

    fun mpFromCode(code: String): Int? {
        return if (mp.containsKey(code)) {
            mp.getValue(code)
        } else {
            null
        }
    }
}
