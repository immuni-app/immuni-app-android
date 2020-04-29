package org.immuni.android.ids

import android.content.Context
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

typealias IdName = String

internal const val ID_NAME = "unique_id"

/**
 * Ids lib.
 *
 * Manage the app ids.
 */
class Ids(
    context: Context,
    encryptIds: Boolean,
    storage: IdsStorage = IdsStorageImpl(context, encryptIds),
    provider: IdsProvider = IdsProviderImpl(context)
) {

    val manager: IdsManager = IdsManagerImpl(storage, provider)

    @JsonClass(generateAdapter = true)
    data class Id (
        val name: IdName = ID_NAME,
        val id: String,
        val creation: CreationType
    )

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
}
