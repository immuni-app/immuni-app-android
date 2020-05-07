package org.immuni.android.network

import org.immuni.android.network.api.NetworkRetrofit
import kotlin.reflect.KClass

/**
 * Network module entry point.
 */
class Network(
    private val config: NetworkConfiguration
) {

    /**
     * Creates an instace of [apiClass]
     * using the [Network] and [NetworkConfiguration] config.
     */
    fun <T : Any> createServiceAPI(apiClass: KClass<T>): T {
        return NetworkRetrofit(config).retrofit.create(
            apiClass.java
        )
    }
}
