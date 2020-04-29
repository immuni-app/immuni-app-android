package org.immuni.android.analytics.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson

/**
 * Adapter that merges PicoUser's additionalInfo map into info map.
 */
class PicoUserAdapter {
    companion object {
        private val moshi = Moshi.Builder().build()
        private val adapter = moshi.adapter(PicoUser::class.java)

        const val INFO_FIELD = "info"
        const val ADDITIONAL_INFO_FIELD = "additional_info"
    }

    @ToJson
    fun toJson(picoUser: PicoUser): Map<String, Any> {
        val jsonMap = (adapter.toJsonValue(picoUser) as Map<String, Any>).toMutableMap()
        val info = (jsonMap[INFO_FIELD] as Map<String, Any>).toMutableMap()
        val additionalInfo = jsonMap[ADDITIONAL_INFO_FIELD] as Map<String, Any>
        info.putAll(additionalInfo)
        jsonMap[INFO_FIELD] = info
        jsonMap.remove(ADDITIONAL_INFO_FIELD)
        return jsonMap
    }

    @FromJson
    fun fromJson(picoUserMap: Object): PicoUser? {
        val jsonMap = (picoUserMap as Map<String, Any>).toMutableMap()
        jsonMap?.put(ADDITIONAL_INFO_FIELD, mapOf<String, Any>())
        val infoMap = jsonMap?.get(INFO_FIELD) as Map<String, Any>
        val additionalInfoMap = infoMap.toMutableMap()
        val user = adapter.fromJsonValue(jsonMap)!!
        val baseUserMap = adapter.toJsonValue(user) as Map<String, Any>
        val baseInfoMap = baseUserMap?.get(INFO_FIELD) as Map<String, Any>
        for (key in baseInfoMap.keys) {
            additionalInfoMap.remove(key)
        }
        jsonMap[ADDITIONAL_INFO_FIELD] = additionalInfoMap
        return adapter.fromJsonValue(jsonMap)
    }
}
