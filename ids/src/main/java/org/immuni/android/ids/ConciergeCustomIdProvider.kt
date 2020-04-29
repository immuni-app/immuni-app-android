package org.immuni.android.ids

import org.immuni.android.ids.Concierge

/**
 * Provides a set of custom [Concierge.Id].
 */
interface ConciergeCustomIdProvider {
    val ids: Set<Concierge.Id>
}
