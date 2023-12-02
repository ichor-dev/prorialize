@file:OptIn(ExperimentalSerializationApi::class)

package fyi.pauli.prolialize.desc

import fyi.pauli.prolialize.serialization.types.EnumSerial
import fyi.pauli.prolialize.serialization.types.EnumType
import fyi.pauli.prolialize.serialization.types.NumberType
import fyi.pauli.prolialize.serialization.types.StringLength
import fyi.pauli.prolialize.serialization.types.primitives.MinecraftEnumType
import fyi.pauli.prolialize.serialization.types.primitives.MinecraftNumberType
import fyi.pauli.prolialize.serialization.types.primitives.MinecraftStringEncoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor

/**
 * @author btwonion
 * @since 11/11/2023
 *
 * This file is used to extract property descriptors used by the de-/encoder.
 */
internal fun extractProtocolDescriptor(
    descriptor: SerialDescriptor, index: Int
): ProtocolDesc {
    val format = descriptor.findElementAnnotation<NumberType>(index)?.type ?: MinecraftNumberType.VAR
    val maxStringLength =
        descriptor.findElementAnnotation<StringLength>(index)?.maxLength ?: MinecraftStringEncoder.MAX_STRING_LENGTH
    return ProtocolDesc(format, maxStringLength)
}


internal fun extractEnumDescriptor(
    descriptor: SerialDescriptor
): ProtocolEnumDesc {
    val format = descriptor.findEntityAnnotation<EnumType>()?.type ?: MinecraftEnumType.VAR_INT
    val stringMaxLength =
        descriptor.findEntityAnnotation<StringLength>()?.maxLength ?: MinecraftStringEncoder.MAX_STRING_LENGTH
    return ProtocolEnumDesc(format, stringMaxLength)
}

internal fun extractEnumElementDescriptor(
    descriptor: SerialDescriptor, index: Int
): ProtocolEnumElementDesc {
    val ordinal = descriptor.findElementAnnotation<EnumSerial>(index)?.ordinal ?: index

    return ProtocolEnumElementDesc(ordinal)
}

private inline fun <reified A : Annotation> SerialDescriptor.findEntityAnnotation(): A? {
    return annotations.find { it is A } as A?
}

private inline fun <reified A : Annotation> SerialDescriptor.findElementAnnotation(
    elementIndex: Int
): A? {
    return getElementAnnotations(elementIndex).find { it is A } as A?
}

internal fun findEnumIndexByTag(
    descriptor: SerialDescriptor, serialOrdinal: Int
): Int = (0..< descriptor.elementsCount).firstOrNull {
    extractEnumElementDescriptor(descriptor, it).ordinal == serialOrdinal
} ?: -1