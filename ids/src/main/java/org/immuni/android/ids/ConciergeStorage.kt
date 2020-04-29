package org.immuni.android.ids

import org.immuni.android.ids.Concierge.*
import android.content.Context
import org.immuni.android.base.storage.KVStorage
import java.io.File

/**
 * A store for user ids.
 */
interface ConciergeStorage {
    fun get(name: InternalId): Id?
    fun save(id: Id)
    fun clear()
}

/**
 * ConciergeStorage implementation that saves ids in the user shared preferences.
 * Shared preferences are backed-up if auto backup is active.
 */
internal class ConciergeStorageImpl(context: Context, encrypted: Boolean) :
    ConciergeStorage {

    val storage: KVStorage = KVStorage("ConciergeStorageImpl",context, encrypted = encrypted)

    override fun get(internalId: InternalId): Id? {
        val id = storage.load<String>(internalId.keyName) ?: return null
        return Id.Internal(internalId, id, CreationType.readFromFile)
    }

    override fun save(id: Id) {
        storage.save(id.name, id.id)
    }

    override fun clear() {
        storage.clear()
    }
}

/**
 * ConciergeStorage implementation that saves ids in the noBackupFilesDir.
 */
internal class ConciergeNonBackupStorageImpl(context: Context) :
    ConciergeStorage {

    private val nonBackupDir: File = context.noBackupFilesDir

    override fun get(internalId: InternalId): Id? {
        val file = File(nonBackupDir, "${internalId.keyName}.txt")
        val id = if (file.exists()) file.readText(Charsets.UTF_8) else return null
        return Id.Internal(internalId, id, CreationType.readFromFile)
    }

    override fun save(id: Id) {
        val file = File(nonBackupDir, "${id.name}.txt")
        file.writeText(id.id)
    }

    override fun clear() {}
}
