package fyi.pauli.prolialize.exceptions

/**
 * @author btwonion
 * @since 11/11/2023
 *
 * Exception which will be thrown when an error occurs during decoding.
 */
internal class MinecraftProtocolDecodingException(message: String) : RuntimeException(message)