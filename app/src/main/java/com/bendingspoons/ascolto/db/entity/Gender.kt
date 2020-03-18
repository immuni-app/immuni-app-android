package com.bendingspoons.ascolto.db.entity

enum class Gender(val id: String) {
    MALE("male"),
    FEMALE("female");

    companion object {
        fun fromId(id: String): Gender = values().first { it.id == id }
    }
}
