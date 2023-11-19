package fyi.pauli.prolialize.serialization.types.primitives

import fyi.pauli.prolialize.exceptions.MinecraftProtocolDecodingException
import fyi.pauli.prolialize.exceptions.MinecraftProtocolEncodingException
import fyi.pauli.prolialize.serialization.types.primitives.VarIntSerializer.readVarInt
import fyi.pauli.prolialize.serialization.types.primitives.VarIntSerializer.writeVarInt

/**
 * @author btwonion
 * @since 11/11/2023
 *
 * Class for de-/serializing Minecraft formatted strings.
 */
public object MinecraftStringEncoder {
    /**
     * The default max string length Minecraft uses.
     */
    public const val MAX_STRING_LENGTH: Int = 32767

    @ExperimentalStdlibApi
    public inline fun readString(
        maxLength: Int = MAX_STRING_LENGTH,
        readByte: () -> Byte,
        readBytes: (length: Int) -> ByteArray,
    ): String {
        val length: Int = readVarInt(readByte)
        return if (length > maxLength * 4) {
            throw MinecraftProtocolDecodingException("The received encoded string buffer length is longer than maximum allowed ($length > ${maxLength * 4})")
        } else if (length < 0) {
            throw MinecraftProtocolDecodingException("The received encoded string buffer length is less than zero! Weird string!")
        } else {
            val stringBuffer = readBytes(length).decodeToString()
            if (stringBuffer.length > maxLength) {
                throw MinecraftProtocolDecodingException("The received string length is longer than maximum allowed ($length > $maxLength)")
            } else {
                stringBuffer
            }
        }
    }

    public inline fun writeString(
        string: String, writeByte: (Byte) -> Unit, writeFully: (ByteArray) -> Unit
    ) {
        val bytes = string.encodeToByteArray()

        if (bytes.size > MAX_STRING_LENGTH) {
            throw MinecraftProtocolEncodingException("String too big (was ${bytes.size} bytes encoded, max $MAX_STRING_LENGTH)")
        } else {
            writeVarInt(bytes.size, writeByte)
            writeFully(bytes)
        }
    }
}