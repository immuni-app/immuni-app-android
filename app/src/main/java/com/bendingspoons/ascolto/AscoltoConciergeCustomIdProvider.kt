package com.bendingspoons.ascolto

import com.bendingspoons.concierge.Concierge
import com.bendingspoons.concierge.ConciergeCustomIdProvider

class AscoltoConciergeCustomIdProvider: ConciergeCustomIdProvider {
    override val ids: Set<Concierge.Id> = setOf()
}
