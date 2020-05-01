package org.immuni.android.util

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Coroutines contexts provider.
 * Useful during unit tests to change contexts in an easy way.
 */
open class CoroutineContextProvider {
    open val Main: CoroutineContext by lazy { Dispatchers.Main }
    open val IO: CoroutineContext by lazy { Dispatchers.IO }
    open val Unconfined: CoroutineContext by lazy { Dispatchers.Unconfined }
    open val Default: CoroutineContext by lazy { Dispatchers.Default }
}