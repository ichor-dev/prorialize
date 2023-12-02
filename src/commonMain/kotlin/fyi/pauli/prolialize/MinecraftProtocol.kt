package fyi.pauli.prolialize

import fyi.pauli.prolialize.serialization.MinecraftProtocolDecoder
import fyi.pauli.prolialize.serialization.MinecraftProtocolEncoder
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * @author btwonion
 * @since 11/11/2023
 *
 * This is a kotlinx.serialization format supporting the Minecraft protocol.
 * For using specific number encoding annotate the specific field with @NumberType.
 * When using enums you can use the @EnumSerial annotation to specify the number, which should be encoded.
 */
public class MinecraftProtocol(
    public override var serializersModule: SerializersModule = EmptySerializersModule()
) : BinaryFormat {

    /**
     * This function is used to decode a serializable object from a Minecraft protocol formatted ByteArray.
     * @param deserializer specifies the serialization strategy to decode the object, by default MyObject.serializer()
     * @param bytes the input ByteArray the object should be read from
     * @returns the decoded object
     */
    @InternalSerializationApi
    public override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val decoder = MinecraftProtocolDecoder(Buffer().also { it.write(bytes) })

        return decoder.decodeSerializableValue(deserializer)
    }

    /**
     * This function is used to encode a serializable object to a Minecraft protocol formatted ByteArray.
     * @param serializer specifies the serialization strategy to encode the object, by default MyObject.serializer()
     * @param value the object, which should be transformed to a ByteArray
     * @returns the encoded ByteArray
     */
    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val buffer = Buffer()
        val encoder = MinecraftProtocolEncoder(buffer)
        encoder.encodeSerializableValue(serializer, value)

        return buffer.readByteArray()
    }
}