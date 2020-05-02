package org.immuni.android.extensions.extensions

import junit.framework.Assert.assertEquals
import org.immuni.android.extensions.utils.fromJson
import org.immuni.android.extensions.utils.round
import org.immuni.android.extensions.utils.toJson
import org.junit.Before
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
        val json = toJson(person)
        assertEquals("{\"name\":\"Marco\",\"age\":36,\"surname\":\"Rossi\",\"pets\":[{\"name\":\"Lucky\",\"age\":4},{\"name\":\"Lilly\",\"age\":12}]}", json)
    }

    @Test
    fun `fromson deserialize an json correctly`() {
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
        val obj = fromJson<Person>(json)
        assertEquals(person, obj)
    }
}

private class Person(
    val name: String,
    val age: Int,
    val surname: String,
    val pets: MutableList<Pet>
)

private class Pet(
    val name: String,
    val age: Int
)