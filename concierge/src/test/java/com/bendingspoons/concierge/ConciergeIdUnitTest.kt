package com.bendingspoons.concierge

import com.bendingspoons.base.utils.fromJson
import com.bendingspoons.base.utils.toJson
import org.junit.Test

import org.junit.Assert.*

class ConciergeIdUnitTest {
    @Test
    fun `test correct fields are injected`() {

        val id = Concierge.Id.Internal(Concierge.InternalId.BACKUP_PERSISTENT_ID, "1234", Concierge.CreationType.justGenerated)

        assertEquals(Concierge.InternalId.BACKUP_PERSISTENT_ID.keyName, id.name)
        assertEquals("1234", id.id)
        assertEquals(Concierge.CreationType.justGenerated, id.creation)
    }

    @Test
    fun `test id can be serialized`() {

        val id = Concierge.Id.Internal(Concierge.InternalId.BACKUP_PERSISTENT_ID,  "1234", Concierge.CreationType.justGenerated)

        assertEquals("""{"name":"backup_persistent_id","id":"1234","creation":"just_generated"}""", toJson(id))
    }

    @Test
    fun `test id can be deserialized`() {
        val str = "{name:backup_persistent_id,id:1234,creation:read_from_file}"
        val id = Concierge.Id.Internal(Concierge.InternalId.BACKUP_PERSISTENT_ID, "1234", Concierge.CreationType.readFromFile)
        assertEquals(id, fromJson<Concierge.Id>(str, lenient = true))
    }
}
