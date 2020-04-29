package org.immuni.android.ids

import org.immuni.android.ids.Ids.*
import java.util.*

/**
 * Manage all the [Ids.Id].
 *
 * @param storage used to store backuppable ids.
 * @param nonBackupStorage used to store non backuppable ids.
 * @param provider used to retrieve the ids.
 * @param appCustomIdProvider an app specific provider used to inject app specific ids.
 */
abstract class IdsManager(
    internal val storage: IdsStorage,
    internal val nonBackupStorage: IdsStorage,
    internal val provider: IdsProvider,
    internal val appCustomIdProvider: CustomIdProvider
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
    abstract fun registerCustomIdProvider(provider: CustomIdProvider)

    abstract var backupPersistentId: Id
    abstract var nonBackupPersistentId: Id

    protected abstract val customIds: Set<Id>
}

internal class IdsManagerImpl(
    storage: IdsStorage,
    nonBackupStorage: IdsStorage,
    provider: IdsProvider,
    appCustomIdProvider: CustomIdProvider
) : IdsManager(storage, nonBackupStorage, provider, appCustomIdProvider) {

    private val customIdProviders = mutableSetOf<CustomIdProvider>()
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
        backupPersistentId = Ids.Id.Internal(
            Ids.InternalId.BACKUP_PERSISTENT_ID, UUID.randomUUID().toString(), CreationType.readFromFile)
        nonBackupPersistentId = Ids.Id.Internal(
            Ids.InternalId.NON_BACKUP_PERSISTENT_ID, UUID.randomUUID().toString(), CreationType.readFromFile)

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

    override fun registerCustomIdProvider(provider: CustomIdProvider) {
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
