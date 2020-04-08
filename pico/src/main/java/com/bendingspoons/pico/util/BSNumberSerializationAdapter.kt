package com.bendingspoons.pico.util

import com.squareup.moshi.*

/*
 When dealing with unknown types, for example Map<String, Any> JSON knows during serialization the
 actual types of the objects (Integer vs Double for example). But when JSON try to deserialize them
 there is no way it can know the type. The result is that some Integer can be read as Double.
 When you send them through Retrofit your integer can be serialized with decimal numbers and this
 maybe not what you want: [1.0, 2.0, 3.0] vs [1, 2, 3].

 This adapter serialized every number as Integer if its value is the same as its integer value.

 Why we need it: before the PicoEvents are first stored in Room (serialized) and then
 read (deserialized) and sent to Retrofit.
 */

val BS_NUMBER_JSON_SERIALIZATION_ADAPTER = object : JsonAdapter<Double>() {

    @ToJson
    override fun toJson(writer: JsonWriter, double: Double?) {
        if(double?.toDouble() == double?.toInt()?.toDouble()) writer.value(double?.toInt())
        else writer.value(double?.toDouble())
    }

    @FromJson
    override fun fromJson(reader: JsonReader): Double? {
        return reader.nextDouble()
    }
}