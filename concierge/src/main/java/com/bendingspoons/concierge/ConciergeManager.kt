package com.bendingspoons.concierge

import com.bendingspoons.concierge.Concierge.*
import com.bendingspoons.concierge.Concierge.Companion.NULL_AAID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

abstract class ConciergeManager(
    internal val storage: ConciergeStorage,
    internal val nonBackupStorage: ConciergeStorage,
    internal val provider: ConciergeProvider,
    internal val appCustomIdProvider: ConciergeCustomIdProvider
) {

    /// Getter for a specific internal id.
    ///
    /// - parameters internalId: the type of the identifier
    /// - returns: the id
    abstract fun internalId(internalId: InternalId): Id?

    /// Getter for a specific custom id.
    ///
    /// - parameters name: the name of the identifier
    /// - returns: the id
    abstract fun customId(name: String): Id?

    /// Getter for all the ids
    ///
    /// - returns: a list of id
    abstract fun allIds(): Set<Id>

    /// Internal utility method to forget the user
    /// and allow Oracle to segment the user from scratch.
    ///
    abstract fun resetUserIds()

    abstract fun registerCustomIdProvider(provider: ConciergeCustomIdProvider)

    abstract var aaid: Id
    abstract var backupPersistentId: Id
    abstract var nonBackupPersistentId: Id
    // this id is not stored, but always get from the system
    abstract var androidId: Id?

    protected abstract val customIds: Set<Id>
}

internal class ConciergeManagerImpl(
    storage: ConciergeStorage,
    nonBackupStorage: ConciergeStorage,
    provider: ConciergeProvider,
    appCustomIdProvider: ConciergeCustomIdProvider
) : ConciergeManager(storage, nonBackupStorage, provider, appCustomIdProvider) {

    private val customIdProviders = mutableSetOf<ConciergeCustomIdProvider>()
    override var aaid: Id = NULL_AAID
    override var androidId: Id? = provider.provideAndroidId()
    override lateinit var backupPersistentId: Id
    override lateinit var nonBackupPersistentId: Id

    init {
        initializeInternalIds()
        registerCustomIdProvider(appCustomIdProvider)
    }

    override fun internalId(internalId: InternalId): Id? {
        return if (internalId == InternalId.AAID) aaid
        else if (internalId == InternalId.ANDROID_ID) androidId
        else when (internalId.type) {
            InternalId.Type.BACKUP -> backupPersistentId
            InternalId.Type.NON_BACKUP -> nonBackupPersistentId
        }
    }

    override fun customId(name: String): Id? {
        return customIds.firstOrNull { it.name == name }
    }

    override fun resetUserIds() {
        backupPersistentId = Concierge.Id.Internal(Concierge.InternalId.BACKUP_PERSISTENT_ID, UUID.randomUUID().toString(), CreationType.readFromFile)
        nonBackupPersistentId = Concierge.Id.Internal(Concierge.InternalId.NON_BACKUP_PERSISTENT_ID, UUID.randomUUID().toString(), CreationType.readFromFile)

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

    // function that initialize the ids and store them to the disk
    private fun initializeInternalIds() {
        // Compute the ids and store them
        computeInternalIds()
    }

    private fun computeNonBackupPersistentId() {
        nonBackupPersistentId = nonBackupStorage.get(InternalId.NON_BACKUP_PERSISTENT_ID)
            ?: provider.provideNonBackupPersistentId()
        storeNonBackupPersistentId()
    }

    private fun computeInternalIds() {
        // Immediately get previously stored AAID, but retrieve and store the updated one
        computeStoredAAIDSync()
        // AAID must be retrieved not from the Main thread, async
        GlobalScope.launch(Dispatchers.IO) {
            computeAAID()
        }

        computeBackupPersistentID()
        computeNonBackupPersistentId()
    }

    private fun computeBackupPersistentID() {
        backupPersistentId =
            storage.get(InternalId.BACKUP_PERSISTENT_ID) ?: provider.provideBackupPersistentId()
        storeBackupPersistentID()
    }

    // AAID cannot be retrieved synchronously, so when we already have it, set it immediately
    private fun computeStoredAAIDSync() {
        val aaid = storage.get(InternalId.AAID)
        if (aaid != null) {
            this.aaid = aaid
        }
    }

    private fun computeAAID() {
        val aaid = provider.provideAAID()
        this.aaid = aaid ?: NULL_AAID
        storeAAID()
    }

    private fun storeBackupPersistentID() {
        storage.save(this.backupPersistentId)
    }

    private fun storeAAID() {
        if (aaid != NULL_AAID) {
            storage.save(aaid)
        }
    }

    private fun storeNonBackupPersistentId() {
        nonBackupStorage.save(this.nonBackupPersistentId)
    }
}
