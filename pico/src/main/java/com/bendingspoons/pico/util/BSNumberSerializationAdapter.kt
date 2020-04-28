package com.bendingspoons.pico.util

import com.squareup.moshi.*

/*
 When dealing with serialization/deserialization of untyped objects – i.e. Map<String, Any> –
 the Json serializer knows at the time of serialization the actual types of its values
 (Int vs Double for example). But when it comes to deserializing them there is no way
 it can tell that integers are in fact Int rather than Double, so it deserializes them as Double.
 If they are then to be serialized again, they'd be re-serialized ad Double, rather than Int.

 This adapter prevents that by serializes integer Double numbers as Int.

 Why we need it: PicoEvents are first serialized and stored in Room and then
 read and deserialized and sent with Retrofit, which serializes them again.
 */
val BS_NUMBER_JSON_SERIALIZATION_ADAPTER = object : JsonAdapter<Double>() {
    @ToJson
    override fun toJson(writer: JsonWriter, double: Double?) {
        if(double == double?.toInt()?.toDouble()) writer.value(double?.toInt())
        else writer.value(double)
    }

    @FromJson
    override fun fromJson(reader: JsonReader): Double? {
        return reader.nextDouble()
    }
}
