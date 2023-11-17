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

private const val SEGMENT_BITS = 0x7F.toByte() // 127
private const val CONTINUE_BIT = 0x80.toByte() // 128

/**
 * Internal class to de-/serialize VarInts.
 */
internal object VarIntSerializer {
    internal inline fun readVarInt(
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

    internal inline fun writeVarInt(
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
    fun varIntBytesCount(
        value: Int,
    ): Int {
        var counter = 0
        writeVarInt(value) { counter++ }
        return counter
    }
}

/**
 * Internal class to de-/serialize VarLongs.
 */
internal object VarLongSerializer {
    internal inline fun readVarLong(
        readByte: () -> Byte
    ): Long {
        var numRead = 0
        var result = 0L
        var read: Byte
        do {
            read = readByte()
            val value = (read and SEGMENT_BITS).toLong()
            result = result or (value shl 7 * numRead)
            numRead++
            if (numRead > 5) {
                throw RuntimeException("VarLong is too big")
            }
        } while (read and CONTINUE_BIT != 0.toByte())
        return result
    }

    internal inline fun writeVarLong(
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
    fun varLongBytesCount(
        value: Long,
    ): Int {
        var counter = 0
        writeVarLong(value) { counter++ }
        return counter
    }
}