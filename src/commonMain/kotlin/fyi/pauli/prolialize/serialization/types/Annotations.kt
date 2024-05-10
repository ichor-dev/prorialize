@file:OptIn(ExperimentalSerializationApi::class)

package fyi.pauli.prolialize.serialization.types

import fyi.pauli.prolialize.serialization.types.primitives.MinecraftEnumType
import fyi.pauli.prolialize.serialization.types.primitives.MinecraftNumberType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * @author btwonion
 * @since 11/11/2023
 */
/**
 * Annotation to specify the binary encoding of the number.
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class NumberType(
	val type: MinecraftNumberType = MinecraftNumberType.DEFAULT,
)

/**
 * Annotation to specify the max string length of a specific string.
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
public annotation class StringLength(
	val maxLength: Int,
)

/**
 * Annotation to specify the encoding of an enum.
 */
@SerialInfo
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
public annotation class EnumType(
	val type: MinecraftEnumType = MinecraftEnumType.VAR_INT,
)

/**
 * Annotation to specify serial number of the targeted enum entry.
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class EnumSerial(
	val ordinal: Int,
)

/**
 * Annotation to indicate that this collection should not be prefixed by a length VarInt.
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class Unprefixed