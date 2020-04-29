package org.immuni.android.ids

import android.content.Context
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Concierge lib.
 *
 * Manage the user ids.
 */
class Concierge {

    @JsonClass(generateAdapter = true)
    data class Id internal constructor(
        val name: String,
        val id: String,
        val creation: CreationType
    ) {
        companion object {
            fun Internal(internalId: InternalId, id: String, creation: CreationType): Id =
                Id(
                    name = internalId.keyName,
                    id = id,
                    creation = creation
                )

            fun Custom(name: String, id: String): Id =
                Id(
                    name = name,
                    id = id,
                    creation = CreationType.notApplicable
                )
        }
    }

    @JsonClass(generateAdapter = false)
    enum class CreationType {
        // Valid for all the custom IDs
        @Json(name = "not_applicable")
        notApplicable,
        // If the id has been generated in this session
        @Json(name = "just_generated")
        justGenerated,
        // If the id has been read from file this session
        @Json(name = "read_from_file")
        readFromFile
    }

    @JsonClass(generateAdapter = false)
    enum class InternalId(val keyName: String, val type: Type) {
        @Json(name = "backup_persistent_id")
        BACKUP_PERSISTENT_ID("backup_persistent_id",
            Type.BACKUP
        ),
        @Json(name = "non_backup_persistent_id")
        NON_BACKUP_PERSISTENT_ID("non_backup_persistent_id",
            Type.NON_BACKUP
        );

        enum class Type {
            BACKUP, NON_BACKUP
        }
    }

    companion object {
        fun Manager(
            context: Context,
            nonBackupStorage: ConciergeStorage = ConciergeNonBackupStorageImpl(
                context
            ),
            provider: ConciergeProvider = ConciergeProviderImpl(
                context
            ),
            appCustomIdProvider: ConciergeCustomIdProvider,
            encryptIds: Boolean
        ): ConciergeManager {
            return ConciergeManagerImpl(
                ConciergeStorageImpl(
                    context,
                    encryptIds
                ),
                nonBackupStorage,
                provider,
                appCustomIdProvider
            )
        }
    }
}
