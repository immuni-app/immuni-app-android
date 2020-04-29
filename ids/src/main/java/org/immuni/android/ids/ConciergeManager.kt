package org.immuni.android.ids

import org.immuni.android.ids.Concierge.*
import java.util.*

/**
 * Manage all the [Concierge.Id].
 *
 * @param storage used to store backuppable ids.
 * @param nonBackupStorage used to store non backuppable ids.
 * @param provider used to retrieve the ids.
 * @param appCustomIdProvider an app specific provider used to inject app specific ids.
 */
abstract class ConciergeManager(
    internal val storage: ConciergeStorage,
    internal val nonBackupStorage: ConciergeStorage,
    internal val provider: ConciergeProvider,
    internal val appCustomIdProvider: ConciergeCustomIdProvider
) {

    /**
     * Getter for a specific internal id.
     *
     * @param internalId: the type of the identifier
     * @return the id.
     */
    abstract fun internalId(internalId: InternalId): Id?

    /**
     * Getter for a specific custom id.
     *
     * @param internalId: the type of the identifier
     * @return the id.
     */
    abstract fun customId(name: String): Id?

    /**
     * Getter for all the ids.
     *
     * @return a set of ids.
     */
    abstract fun allIds(): Set<Id>

    /**
     * Internal utility method to forget the user.
     */
    abstract fun resetUserIds()

    /**
     * Register a custom id provider, that can be different from the app itself,
     * maybe another module or library.
     */
    abstract fun registerCustomIdProvider(provider: ConciergeCustomIdProvider)

    abstract var backupPersistentId: Id
    abstract var nonBackupPersistentId: Id

    protected abstract val customIds: Set<Id>
}

internal class ConciergeManagerImpl(
    storage: ConciergeStorage,
    nonBackupStorage: ConciergeStorage,
    provider: ConciergeProvider,
    appCustomIdProvider: ConciergeCustomIdProvider
) : ConciergeManager(storage, nonBackupStorage, provider, appCustomIdProvider) {

    private val customIdProviders = mutableSetOf<ConciergeCustomIdProvider>()
    override lateinit var backupPersistentId: Id
    override lateinit var nonBackupPersistentId: Id

    init {
        initializeInternalIds()
        registerCustomIdProvider(appCustomIdProvider)
    }

    override fun internalId(internalId: InternalId): Id? {
        return when (internalId.type) {
            InternalId.Type.BACKUP -> backupPersistentId
            InternalId.Type.NON_BACKUP -> nonBackupPersistentId
        }
    }

    override fun customId(name: String): Id? {
        return customIds.firstOrNull { it.name == name }
    }

    override fun resetUserIds() {
        backupPersistentId = Concierge.Id.Internal(
            Concierge.InternalId.BACKUP_PERSISTENT_ID, UUID.randomUUID().toString(), CreationType.readFromFile)
        nonBackupPersistentId = Concierge.Id.Internal(
            Concierge.InternalId.NON_BACKUP_PERSISTENT_ID, UUID.randomUUID().toString(), CreationType.readFromFile)

        storeBackupPersistentID()
        storeNonBackupPersistentId()
    }

    override fun allIds(): Set<Id> {
        return mutableSetOf<Id>().apply {
            add(backupPersistentId)
            add(nonBackupPersistentId)

            addAll(customIds)
        }
    }

    override fun registerCustomIdProvider(provider: ConciergeCustomIdProvider) {
        customIdProviders.add(provider)
    }

    override val customIds: Set<Id>
        get() {
            val customIds = customIdProviders.fold(setOf<Id>()) { set, provider ->
                set.union(provider.ids)
            }

            // verify custom ids don't contain internal ids
            val internalNames = InternalId.values().map { it.name }.toSet()
            val customNames = customIds.map { it.name }.toSet()
            require(internalNames.intersect(customNames).isEmpty()) {
                "Custom IDs cannot contain Internal IDs."
            }

            return customIds
        }

    private fun initializeInternalIds() {
        computeInternalIds()
    }

    private fun computeNonBackupPersistentId() {
        nonBackupPersistentId = nonBackupStorage.get(InternalId.NON_BACKUP_PERSISTENT_ID)
            ?: provider.provideNonBackupPersistentId()
        storeNonBackupPersistentId()
    }

    private fun computeInternalIds() {
        computeBackupPersistentID()
        computeNonBackupPersistentId()
    }

    private fun computeBackupPersistentID() {
        backupPersistentId =
            storage.get(InternalId.BACKUP_PERSISTENT_ID) ?: provider.provideBackupPersistentId()
        storeBackupPersistentID()
    }

    private fun storeBackupPersistentID() {
        storage.save(this.backupPersistentId)
    }

    private fun storeNonBackupPersistentId() {
        nonBackupStorage.save(this.nonBackupPersistentId)
    }
}
