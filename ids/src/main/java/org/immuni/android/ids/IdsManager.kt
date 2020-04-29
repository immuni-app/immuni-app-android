package org.immuni.android.ids

import org.immuni.android.ids.Ids.*
import java.util.*

/**
 * Manage all the [Ids.Id].
 *
 * @param storage used to store ids.
 * @param provider used to retrieve the ids.
 */
abstract class IdsManager(
    internal val storage: IdsStorage,
    internal val provider: IdsProvider
) {

    /**
     * Internal utility method to forget the user.
     */
    abstract fun resetUserIds()

    abstract var id: Id
}

internal class IdsManagerImpl(
    storage: IdsStorage,
    provider: IdsProvider
) : IdsManager(storage, provider) {

    override lateinit var id: Id

    init {
        initializeIds()
    }

    override fun resetUserIds() {
        id = Id(id = UUID.randomUUID().toString(), creation = CreationType.readFromFile)

        storeId()
    }

    private fun initializeIds() {
        id = storage.get(ID_NAME) ?: provider.provideId()
        storeId()
    }

    private fun storeId() {
        storage.save(this.id)
    }
}
