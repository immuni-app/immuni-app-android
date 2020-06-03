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

package it.ministerodellasalute.immuni.models

import it.ministerodellasalute.immuni.api.services.Faqs
import it.ministerodellasalute.immuni.extensions.utils.defaultMoshi
import it.ministerodellasalute.immuni.extensions.utils.fromJson
import it.ministerodellasalute.immuni.extensions.utils.loadJsonAsset
import org.junit.Test
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FaqsDeserializationTest {
    companion object {
        const val ASSET_BASE_PATH = "../app/src/main/assets/";
    }

    private fun readJsonFile(filename: String): String {
        val br = BufferedReader(InputStreamReader(FileInputStream(ASSET_BASE_PATH + filename)))
        val sb = StringBuilder()
        var line = br.readLine()
        while (line != null) {
            sb.append(line)
            line = br.readLine()
        }

        return sb.toString()
    }

    @Test
    fun `test default FAQ deserialization from JSON`() {
        val faqsEnJson = readJsonFile("faqs/faq-en.json")
        val faqsItJson = readJsonFile("faqs/faq-it.json")

        val faqsEn = defaultMoshi.fromJson<Faqs>(faqsEnJson)?.faqs
        val faqsIt = defaultMoshi.fromJson<Faqs>(faqsItJson)?.faqs

        assertNotNull(faqsEn)
        assertNotNull(faqsIt)
    }
}
