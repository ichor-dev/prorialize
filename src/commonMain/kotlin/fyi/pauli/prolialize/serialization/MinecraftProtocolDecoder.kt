package fyi.pauli.prolialize.serialization

import fyi.pauli.prolialize.desc.ProtocolDesc
import fyi.pauli.prolialize.desc.extractEnumDescriptor
import fyi.pauli.prolialize.desc.extractProtocolDescriptor
import fyi.pauli.prolialize.desc.findEnumIndexByTag
import fyi.pauli.prolialize.exceptions.MinecraftProtocolDecodingException
import fyi.pauli.prolialize.serialization.types.Unprefixed
import fyi.pauli.prolialize.serialization.types.primitives.MinecraftEnumType
import fyi.pauli.prolialize.serialization.types.primitives.MinecraftNumberType
import fyi.pauli.prolialize.serialization.types.primitives.MinecraftStringEncoder.readString
import fyi.pauli.prolialize.serialization.types.primitives.VarIntSerializer.readVarInt
import fyi.pauli.prolialize.serialization.types.primitives.VarLongSerializer.readVarLong
import kotlinx.io.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.internal.TaggedDecoder


/**
 * @author btwonion
 * @since 11/11/2023
 *
 * Decoder for the Minecraft protocol format.
 */
@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
internal class MinecraftProtocolDecoder(private val input: Buffer) : TaggedDecoder<ProtocolDesc>() {
    private var currentIndex = 0
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (descriptor.elementsCount == currentIndex) DECODE_DONE
        else currentIndex++
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        return if (descriptor.annotations.map { it::class }.contains(Unprefixed::class)) -1
        else readVarInt { input.readByte() }
    }

    override fun decodeSequentially(): Boolean {
        return true
    }

    override fun decodeTaggedBoolean(
        tag: ProtocolDesc
    ): Boolean {
        return when (val i = input.readByte()) {
            0x00.toByte() -> false
            0x01.toByte() -> true
            else -> throw MinecraftProtocolDecodingException("Expected boolean value (0 or 1), found $i")
        }
    }

    override fun decodeTaggedByte(tag: ProtocolDesc): Byte {
        return when (tag.type) {
            MinecraftNumberType.UNSIGNED -> input.readUByte().toByte()
            else -> input.readByte()
        }
    }

    override fun decodeTaggedShort(tag: ProtocolDesc): Short {
        return when (tag.type) {
            MinecraftNumberType.UNSIGNED -> input.readUShort().toShort()
            else -> input.readShort()
        }
    }

    override fun decodeTaggedInt(tag: ProtocolDesc): Int {
        return when (tag.type) {
            MinecraftNumberType.DEFAULT -> input.readInt()
            MinecraftNumberType.VAR -> readVarInt { input.readByte() }
            MinecraftNumberType.UNSIGNED -> input.readUInt().toInt()
        }
    }

    override fun decodeTaggedLong(tag: ProtocolDesc): Long {
        return when (tag.type) {
            MinecraftNumberType.DEFAULT -> input.readLong()
            MinecraftNumberType.VAR -> readVarLong { input.readByte() }
            MinecraftNumberType.UNSIGNED -> input.readULong().toLong()
        }
    }

    override fun decodeTaggedFloat(tag: ProtocolDesc): Float {
        return input.readFloat()
    }

    override fun decodeTaggedDouble(tag: ProtocolDesc): Double {
        return input.readDouble()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun decodeTaggedString(tag: ProtocolDesc): String {
        return readString(readByte = { input.readByte() }) { length ->
            ByteArray(length) { input.readByte() }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun decodeTaggedEnum(tag: ProtocolDesc, enumDescriptor: SerialDescriptor): Int {
        val enumTag = extractEnumDescriptor(enumDescriptor)
        val ordinal = when (enumTag.type) {
            MinecraftEnumType.VAR_INT -> readVarInt { input.readByte() }
            MinecraftEnumType.BYTE -> input.readByte().toInt()
            MinecraftEnumType.UNSIGNED_BYTE -> input.readUByte().toInt()
            MinecraftEnumType.INT -> input.readInt()
            MinecraftEnumType.STRING -> enumDescriptor.getElementIndex(readString(readByte = { input.readByte() }) { length ->
                ByteArray(length) { input.readByte() }
            })
        }

        return findEnumIndexByTag(enumDescriptor, ordinal)
    }

    override fun SerialDescriptor.getTag(index: Int): ProtocolDesc {
        return extractProtocolDescriptor(this, index)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return when (descriptor.kind) {
            StructureKind.CLASS, StructureKind.LIST -> MinecraftProtocolDecoder(input)
            else -> MinecraftProtocolDecoder(input)
        }
    }
}