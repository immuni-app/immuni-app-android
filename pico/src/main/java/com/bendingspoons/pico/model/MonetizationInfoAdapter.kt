package com.bendingspoons.pico.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson

class MonetizationInfoAdapter {
    companion object {
        private val moshi = Moshi.Builder().build()
        private val adapter = moshi.adapter(MonetizationInfo::class.java)

        const val CUSTOM_FIELDS_FIELD = "custom_fields"
    }

    @ToJson
    fun toJson(info: MonetizationInfo): Map<String, Any> {
        val jsonMap = (adapter.toJsonValue(info) as Map<String, Any>).toMutableMap()
        jsonMap.putAll(info.customFields)
        jsonMap.remove(CUSTOM_FIELDS_FIELD)
        return jsonMap
    }

    @FromJson
    fun fromJson(infoMap: Object): MonetizationInfo? {
        val jsonMap = (infoMap as Map<String, Any>).toMutableMap()
        jsonMap.put(CUSTOM_FIELDS_FIELD, mapOf<String, Any>())
        val info = adapter.fromJsonValue(jsonMap)!!
        val baseFields = adapter.toJsonValue(info) as Map<String, Any>
        val nonBaseFields = jsonMap.apply {
            for (field in baseFields.keys) { remove(field) }
        }
        val updatedMap = mutableMapOf<String, Any>()
        updatedMap.putAll(baseFields)
        updatedMap.put(CUSTOM_FIELDS_FIELD, nonBaseFields)
        return adapter.fromJsonValue(updatedMap)
    }
}
