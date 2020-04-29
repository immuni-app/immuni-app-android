package org.immuni.android.ids

import org.immuni.android.extensions.utils.fromJson
import org.immuni.android.extensions.utils.toJson
import org.junit.Test

import org.junit.Assert.*

class IdsUnitTest {
    @Test
    fun `test correct fields are injected`() {

        val id = Ids.Id(ID_NAME, "1234", Ids.CreationType.justGenerated)

        assertEquals(ID_NAME, id.name)
        assertEquals("1234", id.id)
        assertEquals(Ids.CreationType.justGenerated, id.creation)
    }

    @Test
    fun `test id can be serialized`() {

        val id = Ids.Id(ID_NAME,  "1234", Ids.CreationType.justGenerated)

        assertEquals("""{"name":"$ID_NAME","id":"1234","creation":"just_generated"}""", toJson(id))
    }

    @Test
    fun `test id can be deserialized`() {
        val str = "{name:$ID_NAME,id:1234,creation:read_from_file}"
        val id = Ids.Id(ID_NAME, "1234", Ids.CreationType.readFromFile)
        assertEquals(id, fromJson<Ids.Id>(str, lenient = true))
    }
}
