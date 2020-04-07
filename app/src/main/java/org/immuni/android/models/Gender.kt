package org.immuni.android.models

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

fun Gender.iconResource(deviceId: String, userIndex: Int): Int {

    // main user always blue
    if(userIndex == 0) {
        return when(this) {
            Gender.MALE -> R.drawable.ic_male_blue
            Gender.FEMALE -> R.drawable.ic_female_blue
        }
    }

    val random = Random(deviceId.hashCode())
    val list = when (this) {
        Gender.MALE -> listOf(
            R.drawable.ic_male_pink,
            R.drawable.ic_male_yellow,
            R.drawable.ic_male_purple,
            R.drawable.ic_male_violet
        )
        Gender.FEMALE -> listOf(
            R.drawable.ic_female_pink,
            R.drawable.ic_female_yellow,
            R.drawable.ic_female_purple,
            R.drawable.ic_female_violet
        )
    }.shuffled(random)
    return list[userIndex % list.size]
}

fun colorResource(deviceId: String, userIndex: Int): Int {

    // main user always blue
    if(userIndex == 0) return R.color.profile_blue

    val random = Random(deviceId.hashCode())
    val list = listOf(
        R.color.profile_pink,
        R.color.profile_yellow,
        R.color.profile_purple,
        R.color.profile_violet
    ).shuffled(random)
    return list[userIndex % list.size]
}
