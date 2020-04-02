package org.immuni.android

import org.immuni.android.managers.PermissionsManager
import com.bendingspoons.concierge.Concierge
import com.bendingspoons.concierge.ConciergeCustomIdProvider
import org.koin.core.KoinComponent
import org.koin.core.inject

class ImmuniConciergeCustomIdProvider: ConciergeCustomIdProvider, KoinComponent {
    private val permissionsManager: PermissionsManager by inject()

    override val ids: Set<Concierge.Id>
        get() {
            return setOf()
        }
}
