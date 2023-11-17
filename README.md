# prorialize
> Kotlin.serialization library for the Minecraft protocol.

## Example
```kotlin
import kotlin.random.Random
import fyi.pauli.prolialize.MinecraftProtocol

data class TestClass(val randomInt: Int)

fun main() {
    // Initialization of Minecraft protocol format
    val protocol = MinecraftProtocol()
    
    val testClass = TestClass(Random.nextInt())
    
    // Decoded ByteArray from class
    val encoded = protocol.encodeToByteArray(testClass)
    
    // Encoded class from ByteArray
    val decodedClass = protocol.decodeFromByteArray<TestClass>(encoded)
}
```

## Docs
The Dokka docs can be found on [GitHub Pages](https://ichor-dev.github.io/prorialize/).