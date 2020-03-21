package org.ascolto.onlus.geocrowd19.android.db.entity

import com.squareup.moshi.Json

enum class Gender(val id: String) {
    @Json(name = "M") MALE("M"),
    @Json(name = "F") FEMALE("F");

    companion object {
        fun fromId(id: String): Gender = values().first { it.id == id }
    }
}
