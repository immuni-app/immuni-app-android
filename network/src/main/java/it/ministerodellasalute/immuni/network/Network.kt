/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.network

import android.content.Context
import it.ministerodellasalute.immuni.network.api.NetworkRetrofit
import kotlin.reflect.KClass

/**
 * Network module entry point.
 */
class Network(
    val context: Context,
    private val config: NetworkConfiguration
) {

    /**
     * Creates an instance of [apiClass]
     * using the [Network] and [NetworkConfiguration] config.
     */
    fun <T : Any> createServiceAPI(apiClass: KClass<T>): T {
        return NetworkRetrofit(context, config).retrofit.create(
            apiClass.java
        )
    }
}
