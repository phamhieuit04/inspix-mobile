package com.example.inspixmobile.data.source.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

/**
 * Accept both numeric and string IDs from mixed APIs.
 */
object FlexibleLongSerializer : KSerializer<Long?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleLongSerializer", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Long? {
        val jsonDecoder = decoder as? JsonDecoder ?: return null
        val element = jsonDecoder.decodeJsonElement() as? JsonPrimitive ?: return null

        if (element.isString) {
            val raw = element.content.trim()
            if (raw.isEmpty()) return null
            return raw.toLongOrNull() ?: stableLongFromString(raw)
        }

        return element.content.trim().toLongOrNull()
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Long?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeLong(value)
        }
    }

    private fun stableLongFromString(value: String): Long {
        return value.hashCode().toLong() and Long.MAX_VALUE
    }
}

