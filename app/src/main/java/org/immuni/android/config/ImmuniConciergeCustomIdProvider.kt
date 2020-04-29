package org.immuni.android.config

import org.immuni.android.ids.Concierge
import org.immuni.android.ids.ConciergeCustomIdProvider
import org.koin.core.KoinComponent

class ImmuniConciergeCustomIdProvider: ConciergeCustomIdProvider, KoinComponent {

    override val ids: Set<Concierge.Id>
        get() {
            return setOf()
        }
}
