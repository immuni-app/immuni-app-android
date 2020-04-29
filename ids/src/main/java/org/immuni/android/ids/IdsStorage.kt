package org.immuni.android.ids

import org.immuni.android.ids.Ids.*
import android.content.Context
import org.immuni.android.extensions.storage.KVStorage

/**
 * A store for user ids.
 */
interface IdsStorage {
    fun get(name: IdName): Id?
    fun save(id: Id)
    fun clear()
}

/**
 * [IdsStorage] implementation that saves ids in the user shared preferences.
 */
internal class IdsStorageImpl(context: Context, encrypted: Boolean) :
    IdsStorage {

    val storage: KVStorage = KVStorage("ConciergeStorageImpl",context, encrypted = encrypted)

    override fun get(name: IdName): Id? {
        val id = storage.load<String>(name) ?: return null
        return Id(name, id, CreationType.readFromFile)
    }

    override fun save(id: Id) {
        storage.save(id.name, id.id)
    }

    override fun clear() {
        storage.clear()
    }
}