package org.immuni.android.analytics.db.converter

import androidx.room.TypeConverter
import org.immuni.android.analytics.model.PicoEvent
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

class PicoEventConverter {
    companion object {
        val adapter: JsonAdapter<PicoEvent> = {
            val moshi = Moshi.Builder().build()
            moshi.adapter(PicoEvent::class.java).nullSafe().serializeNulls()
        }()
    }

    @TypeConverter
    fun toPicoEvent(json: String): PicoEvent? {
        return adapter.fromJson(json)
    }

    @TypeConverter
    fun toJson(event: PicoEvent): String {
        return adapter.toJson(event)
    }
}
