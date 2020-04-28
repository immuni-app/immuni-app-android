package org.immuni.android.testutils

import org.immuni.android.util.CoroutineContextProvider
import kotlin.coroutines.CoroutineContext

class TestCoroutineContextProvider(
    mainDispatcher: CoroutineContext,
    ioDispatcher: CoroutineContext): CoroutineContextProvider() {
    override val Main = mainDispatcher
    override val IO = ioDispatcher
}