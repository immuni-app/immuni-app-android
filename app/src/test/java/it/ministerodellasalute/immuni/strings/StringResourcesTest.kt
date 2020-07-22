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

package it.ministerodellasalute.immuni.strings

import it.ministerodellasalute.immuni.testutils.getStringPluralValueByName
import it.ministerodellasalute.immuni.testutils.getStringValueByName
import it.ministerodellasalute.immuni.testutils.readXml
import kotlin.test.assertTrue
import org.junit.Test

class StringResourcesTest {
    companion object {
        private const val ASSET_BASE_PATH = "../app/src/main/res"
        private val LOCALES = listOf("", "-it", "-es", "-fr", "-de")
    }

    @Test
    fun `test string placeholders`() {
        containsMatch("privacy_checkbox_read", "[{](.+)[}]".toRegex()) // {privacy notice...}
        containsMatch("privacy_tos_read", "[{](.+)[}]".toRegex()) // {privacy notice...}
        containsMatch("home_protection_not_active", "[{](.+)[}]".toRegex()) // {privacy notice...}
        containsMatch("home_protection_active", "[{](.+)[}]".toRegex()) // {privacy notice...}
        containsMatch("suggestions_risk_third_message_android", "[{](.+)[}]".toRegex()) // {privacy notice...}

        containsAll("settings_app_version", listOf("%s", "%d")) // Immuni v%s (%d)
        containsAll("suggestions_risk_with_date_subtitle", listOf("%s")) // The day %s you...
        containsAll("support_phone_description_android", listOf("%1\$s", "%2\$s")) // Number active from  %s to %s...

        pluralContainsAll("upload_data_verify_loading_button_seconds", listOf("one", "other"), listOf("%d")) // Wait %d seconds
        pluralContainsAll("upload_data_verify_loading_button_minutes", listOf("one", "other"), listOf("%d")) // Wait %d minutes

        containsAll("support_info_item_lastencheck_date_android", listOf("%1\$s", "%2\$s")) // %s at %s
    }

    private fun containsMatch(name: String, regex: Regex) {
        LOCALES.forEach { locale ->
            val document = readXml("$ASSET_BASE_PATH/values$locale/strings.xml")
            val value = getStringValueByName(document, name).first()

            regex.findAll(value).forEach {
                println(it.value)
            }

            assertTrue(regex.containsMatchIn(value))
        }
    }

    private fun containsAll(name: String, list: List<String>) {
        LOCALES.forEach { locale ->
            val document = readXml("$ASSET_BASE_PATH/values$locale/strings.xml")
            val value = getStringValueByName(document, name).first()

            list.forEach { text ->
                println("$value $text")
                assertTrue(value.contains(text))
            }
        }
    }

    private fun pluralContainsAll(name: String, quantities: List<String>, list: List<String>) {
        LOCALES.forEach { locale ->
            val document = readXml("$ASSET_BASE_PATH/values$locale/strings.xml")

            quantities.forEach { quantity ->
                val value = getStringPluralValueByName(document, name, quantity).first()

                list.forEach { text ->
                    println("$value $text")
                    assertTrue(value.contains(text))
                }
            }
        }
    }
}
