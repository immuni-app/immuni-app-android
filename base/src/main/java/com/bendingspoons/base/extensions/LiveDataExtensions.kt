package com.bendingspoons.base.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <T, K, R> LiveData<T>.combineWith(
    liveData: LiveData<K>,
    block: (T?, K?) -> R?
): MediatorLiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(this) { t ->
        val r = block.invoke(t, liveData.value)
        if (r != null) {
            result.value = r
        }
    }
    result.addSource(liveData) { k ->
        val r = block.invoke(this.value, k)
        if (r != null) {
            result.value = r
        }
    }
    return result
}
