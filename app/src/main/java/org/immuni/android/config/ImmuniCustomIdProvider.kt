package org.immuni.android.config

import org.immuni.android.ids.Ids
import org.immuni.android.ids.CustomIdProvider
import org.koin.core.KoinComponent

class ImmuniCustomIdProvider: CustomIdProvider, KoinComponent {

    override val ids: Set<Ids.Id>
        get() {
            return setOf()
        }
}
