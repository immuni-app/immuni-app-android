package com.bendingspoons.concierge

import com.bendingspoons.concierge.Concierge.*
import android.content.Context
import java.util.*

/**
 * A provider of user ids.
 */
interface ConciergeProvider {
    fun provideBackupPersistentId(): Id
    fun provideNonBackupPersistentId(): Id
}

internal class ConciergeProviderImpl(val context: Context) : ConciergeProvider {

    /**
     * Provide a backup persistent id as [UUID].
     */
    override fun provideBackupPersistentId(): Id {
        return Id.Internal(
            InternalId.BACKUP_PERSISTENT_ID,
            UUID.randomUUID().toString(),
            CreationType.justGenerated
        )
    }

    /**
     * Provide a non backup persistent id as [UUID].
     */
    override fun provideNonBackupPersistentId(): Id {
        return Id.Internal(
            InternalId.NON_BACKUP_PERSISTENT_ID,
            UUID.randomUUID().toString(),
            CreationType.justGenerated
        )
    }
}