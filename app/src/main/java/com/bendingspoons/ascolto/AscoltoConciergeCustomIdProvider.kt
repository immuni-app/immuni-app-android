package com.bendingspoons.ascolto

import com.bendingspoons.ascolto.managers.GeolocationManager
import com.bendingspoons.concierge.Concierge
import com.bendingspoons.concierge.ConciergeCustomIdProvider
import org.koin.core.KoinComponent
import org.koin.core.inject

class AscoltoConciergeCustomIdProvider: ConciergeCustomIdProvider, KoinComponent {
    private val geolocationManager: GeolocationManager by inject()

    override val ids: Set<Concierge.Id>
        get() {
            val geoUniqId = geolocationManager.deviceId.value ?: return setOf()
            return setOf(Concierge.Id.Custom("geouniq", geoUniqId))
        }
}
