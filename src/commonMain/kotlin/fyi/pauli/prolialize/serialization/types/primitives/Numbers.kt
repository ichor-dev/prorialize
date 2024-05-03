package fyi.pauli.prolialize.serialization.types.primitives

import kotlin.experimental.and
import kotlin.experimental.or

/**
 * @author btwonion
 * @since 11/11/2023
 *
 * Enum to define the binary format of the number.
 */
public enum class MinecraftNumberType {
    DEFAULT, UNSIGNED, VAR
}

public const val SEGMENT_BITS: Byte = 0x7F.toByte() // 127
public const val CONTINUE_BIT: Byte = 0x80.toByte() // 128

/**
 * Class to de-/serialize VarInts.
 */
public object VarIntSerializer {
    public inline fun readVarInt(
        readByte: () -> Byte
    ): Int {
        var numRead = 0
        var result = 0
        var read: Byte
        do {
            read = readByte()
            val value = (read and SEGMENT_BITS).toInt()
            result = result or (value shl 7 * numRead)
            numRead++
            if (numRead > 5) {
                throw RuntimeException("VarInt is too big")
            }
        } while (read and CONTINUE_BIT != 0.toByte())
        return result
    }

    public inline fun writeVarInt(
        value: Int,
        writeByte: (Byte) -> Unit,
    ) {
        var v = value
        do {
            var temp = (v and SEGMENT_BITS.toInt()).toByte()
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            v = v ushr 7
            if (v != 0) {
                temp = temp or CONTINUE_BIT
            }
            writeByte(temp)
        } while (v != 0)
    }

    @Suppress("unused")
    public fun varIntBytesCount(
        value: Int,
    ): Int {
        var counter = 0
        writeVarInt(value) { counter++ }
        return counter
    }
}

/**
 * Class to de-/serialize VarLongs.
 */
public object VarLongSerializer {
    public inline fun readVarLong(
        readByte: () -> Byte
    ): Long {
        var result = 0L
        var shift = 0
        while (shift < 56) {
            val next = readByte()
            result = result or ((next.toInt() and 0x7F) shl shift).toLong()
            if (next >= 0) return result
            shift += 7
        }
        return result or ((readByte().toInt() and 0xFF) shl 56).toLong()
    }

    public inline fun writeVarLong(
        value: Long,
        writeByte: (Byte) -> Unit,
    ) {
        var v = value
        do {
            var temp = (v and SEGMENT_BITS.toLong()).toByte()
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            v = v ushr 7
            if (v != 0L) {
                temp = temp or CONTINUE_BIT
            }
            writeByte(temp)
        } while (v != 0L)
    }

    @Suppress("unused")
    public fun varLongBytesCount(
        value: Long,
    ): Int {
        var counter = 0
        writeVarLong(value) { counter++ }
        return counter
    }
}