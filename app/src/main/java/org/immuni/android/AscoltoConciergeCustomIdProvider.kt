package org.immuni.android

import org.immuni.android.managers.GeolocationManager
import com.bendingspoons.concierge.Concierge
import com.bendingspoons.concierge.ConciergeCustomIdProvider
import org.koin.core.KoinComponent
import org.koin.core.inject

class AscoltoConciergeCustomIdProvider: ConciergeCustomIdProvider, KoinComponent {
    private val geolocationManager: GeolocationManager by inject()

    override val ids: Set<Concierge.Id>
        get() {
            return setOf()
        }
}
