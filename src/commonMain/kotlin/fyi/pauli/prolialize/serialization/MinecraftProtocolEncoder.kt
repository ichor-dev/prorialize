package fyi.pauli.prolialize.serialization

import fyi.pauli.prolialize.desc.ProtocolDesc
import fyi.pauli.prolialize.desc.extractEnumDescriptor
import fyi.pauli.prolialize.desc.extractEnumElementDescriptor
import fyi.pauli.prolialize.desc.extractProtocolDescriptor
import fyi.pauli.prolialize.serialization.types.Unprefixed
import fyi.pauli.prolialize.serialization.types.primitives.MinecraftEnumType
import fyi.pauli.prolialize.serialization.types.primitives.MinecraftNumberType
import fyi.pauli.prolialize.serialization.types.primitives.MinecraftStringEncoder.writeString
import fyi.pauli.prolialize.serialization.types.primitives.VarIntSerializer.writeVarInt
import fyi.pauli.prolialize.serialization.types.primitives.VarLongSerializer
import kotlinx.io.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.internal.TaggedEncoder

/**
 * @author btwonion
 * @since 11/11/2023
 *
 * Encoder for the Minecraft protocol format.
 */
@OptIn(InternalSerializationApi::class)
internal class MinecraftProtocolEncoder(
    private val output: Buffer
) : TaggedEncoder<ProtocolDesc>() {

    override fun SerialDescriptor.getTag(index: Int): ProtocolDesc {
        return extractProtocolDescriptor(this@getTag, index)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        if (!descriptor.annotations.map { it::class }
                .contains(Unprefixed::class)) writeVarInt(collectionSize) { output.writeByte(it) }
        return super.beginCollection(descriptor, collectionSize)
    }

    override fun encodeTaggedInt(tag: ProtocolDesc, value: Int) {
        when (tag.type) {
            MinecraftNumberType.DEFAULT -> output.writeInt(value)
            MinecraftNumberType.VAR -> writeVarInt(value) { output.writeByte(it) }
            MinecraftNumberType.UNSIGNED -> output.writeUInt(value.toUInt())
        }
    }

    override fun encodeTaggedByte(tag: ProtocolDesc, value: Byte) {
        when (tag.type) {
            MinecraftNumberType.UNSIGNED -> output.writeUByte(value.toUByte())
            else -> output.writeByte(value)
        }
    }

    override fun encodeTaggedShort(tag: ProtocolDesc, value: Short) {
        when (tag.type) {
            MinecraftNumberType.UNSIGNED -> output.writeUShort(value.toUShort())
            else -> output.writeShort(value)
        }
    }

    override fun encodeTaggedLong(tag: ProtocolDesc, value: Long) {
        when (tag.type) {
            MinecraftNumberType.DEFAULT -> output.writeLong(value)
            MinecraftNumberType.VAR -> VarLongSerializer.writeVarLong(value) { output.writeByte(it) }
            MinecraftNumberType.UNSIGNED -> output.writeULong(value.toULong())
        }
    }

    override fun encodeTaggedFloat(tag: ProtocolDesc, value: Float) {
        output.writeFloat(value)
    }

    override fun encodeTaggedDouble(tag: ProtocolDesc, value: Double) {
        output.writeDouble(value)
    }

    override fun encodeTaggedBoolean(tag: ProtocolDesc, value: Boolean) {
        output.writeByte(if (value) 0x01 else 0x00)
    }

    override fun encodeTaggedString(tag: ProtocolDesc, value: String) {
        writeString(value, { output.writeByte(it) }) { output.write(it) }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun encodeTaggedEnum(tag: ProtocolDesc, enumDescriptor: SerialDescriptor, ordinal: Int) {
        val enumDesc = extractEnumElementDescriptor(enumDescriptor, ordinal)

        when (extractEnumDescriptor(enumDescriptor).type) {
            MinecraftEnumType.VAR_INT -> writeVarInt(enumDesc.ordinal) { output.writeByte(it) }
            MinecraftEnumType.BYTE -> output.writeByte(enumDesc.ordinal.toByte())
            MinecraftEnumType.UNSIGNED_BYTE -> output.writeUByte(enumDesc.ordinal.toUByte())
            MinecraftEnumType.INT -> output.writeInt(enumDesc.ordinal)
            MinecraftEnumType.STRING -> writeString(
                enumDescriptor.getElementName(ordinal),
                { output.writeByte(it) }) { output.write(it) }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return when (descriptor.kind) {
            StructureKind.CLASS, StructureKind.LIST -> MinecraftProtocolEncoder(output)
            else -> super.beginStructure(descriptor)
        }
    }
}