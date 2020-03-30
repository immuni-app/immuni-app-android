package org.immuni.android.db.entity

import android.content.Context
import com.squareup.moshi.Json
import org.immuni.android.R
import kotlin.random.Random

enum class Gender(val id: String) {
    @Json(name = "M") MALE("M"),
    @Json(name = "F") FEMALE("F");

    companion object {
        fun fromId(id: String): Gender = values().first { it.id == id }
    }
}

fun Gender.iconResource(context: Context, deviceId: String, userIndex: Int): Int {
    val random = Random(deviceId.hashCode())
    val list = when (this) {
        Gender.MALE -> listOf(
            R.drawable.ic_male_purple,
            R.drawable.ic_male_blue,
            R.drawable.ic_male_violet,
            R.drawable.ic_male_pink,
            R.drawable.ic_male_yellow
        )
        Gender.FEMALE -> listOf(
            R.drawable.ic_female_purple,
            R.drawable.ic_female_blue,
            R.drawable.ic_female_violet,
            R.drawable.ic_female_pink,
            R.drawable.ic_female_yellow
        )
    }.shuffled(random)
    return list[userIndex % list.size]
}
