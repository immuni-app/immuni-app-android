package org.immuni.android.testutils

import kotlin.coroutines.CoroutineContext
import org.immuni.android.util.CoroutineContextProvider

class TestCoroutineContextProvider(
    mainDispatcher: CoroutineContext,
    ioDispatcher: CoroutineContext
) : CoroutineContextProvider() {
    override val Main = mainDispatcher
    override val IO = ioDispatcher
}
