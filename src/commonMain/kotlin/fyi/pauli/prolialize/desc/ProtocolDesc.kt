package fyi.pauli.prolialize.desc

import fyi.pauli.prolialize.serialization.types.primitives.MinecraftEnumType
import fyi.pauli.prolialize.serialization.types.primitives.MinecraftNumberType

/**
 * @author btwonion
 * @since 11/11/2023
 *
 * Description objects used while en-/decoding.
 */
internal data class ProtocolDesc(
    val type: MinecraftNumberType,
    val maxStringLength: Int
)

internal data class ProtocolEnumDesc(
    val type: MinecraftEnumType,
    val stringMaxLength: Int
)

internal data class ProtocolEnumElementDesc(
    val ordinal: Int
)