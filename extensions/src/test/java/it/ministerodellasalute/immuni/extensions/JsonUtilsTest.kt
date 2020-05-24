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

package it.ministerodellasalute.immuni.extensions

import it.ministerodellasalute.immuni.extensions.utils.*
import junit.framework.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonUtilsTest {

    @Test
    fun `toJson serialize an object correctly`() {
        val person = Person(
            name = "Marco",
            surname = "Rossi",
            age = 36,
            pets = mutableListOf(
                Pet(
                    name = "Lucky",
                    age = 4
                ),
                Pet(
                    name = "Lilly",
                    age = 12
                )
            )
        )
        val json = defaultMoshi.toJson(person)
        assertEquals("{\"name\":\"Marco\",\"age\":36,\"surname\":\"Rossi\",\"pets\":[{\"name\":\"Lucky\",\"age\":4},{\"name\":\"Lilly\",\"age\":12}]}", json)
    }

    @Test
    fun `fromJson deserialize an json correctly`() {
        val json = "{\"name\":\"Marco\",\"age\":36,\"surname\":\"Rossi\",\"pets\":[{\"name\":\"Lucky\",\"age\":4},{\"name\":\"Lilly\",\"age\":12}]}"
        val person = Person(
            name = "Marco",
            surname = "Rossi",
            age = 36,
            pets = mutableListOf(
                Pet(
                    name = "Lucky",
                    age = 4
                ),
                Pet(
                    name = "Lilly",
                    age = 12
                )
            )
        )
        val obj = defaultMoshi.fromJson(Person::class, json)
        assertTrue(person == obj)
    }
}

private data class Person(
    val name: String,
    val age: Int,
    val surname: String,
    val pets: MutableList<Pet>
)

private data class Pet(
    val name: String,
    val age: Int
)
