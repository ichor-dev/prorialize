package fyi.pauli.prolialize.serialization.types.primitives

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
		readByte: () -> Byte,
	): Int {
		var value = 0
		var position = 0
		var currentByte: Byte

		while (true) {
			currentByte = readByte()
			value = value or ((currentByte.toInt() and SEGMENT_BITS.toInt()) shl position)

			if ((currentByte.toInt() and CONTINUE_BIT.toInt()) == 0) break

			position += 7

			if (position >= 32) throw RuntimeException("VarInt is too big")
		}

		return value
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
		readByte: () -> Byte,
	): Long {
		var value: Long = 0
		var position = 0
		var currentByte: Byte

		while (true) {
			currentByte = readByte()
			value = value or ((currentByte.toInt() and SEGMENT_BITS.toInt()).toLong() shl position)

			if ((currentByte.toInt() and CONTINUE_BIT.toInt()) == 0) break

			position += 7

			if (position >= 64) throw RuntimeException("VarLong is too big")
		}

		return value
	}

	public inline fun writeVarLong(
		value: Long,
		writeByte: (Byte) -> Unit,
	) {
		var v = value
		do {
			var temp = (v and SEGMENT_BITS.toLong()).toByte()
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