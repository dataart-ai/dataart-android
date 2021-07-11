package com.datartproject.dataart.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.*

fun parseDateFromAtom(value: String): Date? {
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00", Locale.UK)
    df.timeZone = TimeZone.getTimeZone("UTC")
    return df.parse(value)
}

fun Date.formatAsAtom(): String {
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00", Locale.UK)
    df.timeZone = TimeZone.getTimeZone("UTC")
    return df.format(this)
}

object TimestampSerializer: KSerializer<Date> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("timestamp", PrimitiveKind.STRING)

    @Throws(Exception::class)
    override fun deserialize(decoder: Decoder): Date {
        return parseDateFromAtom(decoder.decodeString()) ?: throw Exception("received Date is invalid")
    }

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(value.formatAsAtom())
    }
}
