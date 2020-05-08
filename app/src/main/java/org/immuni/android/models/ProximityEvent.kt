package org.immuni.android.models

import java.util.*

data class ProximityEvent(val date: Date = Date(), val btId: String, val txPower: Int, val rssi: Int)
