package org.immuni.android.config

import com.bendingspoons.concierge.Concierge
import com.bendingspoons.concierge.ConciergeCustomIdProvider
import org.koin.core.KoinComponent

class ImmuniConciergeCustomIdProvider: ConciergeCustomIdProvider, KoinComponent {

    override val ids: Set<Concierge.Id>
        get() {
            return setOf()
        }
}
