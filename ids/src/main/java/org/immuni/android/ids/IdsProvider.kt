package org.immuni.android.ids

import org.immuni.android.ids.Ids.*
import android.content.Context
import java.util.*

/**
 * A provider of ids.
 */
interface IdsProvider {
    fun provideId(): Id
}

internal class IdsProviderImpl(val context: Context) : IdsProvider {

    /**
     * Provide an id generated as [UUID].
     */
    override fun provideId(): Id {
        return Id (
            id = UUID.randomUUID().toString(),
            creation = CreationType.justGenerated
        )
    }
}