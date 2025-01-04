package com.tweener.alarmee.serializer

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * @author Vivien Mahe
 * @since 03/01/2025
 */
class ColorSerializer : KSerializer<Color> {

    override val descriptor =
        PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Color =
        Color(decoder.decodeInt())

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeInt(value.hashCode())
    }
}
