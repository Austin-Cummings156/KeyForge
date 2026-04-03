package com.example.keyforge.security

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

class Argon2KeyDeriver {

    fun deriveKey(
        password: CharArray,
        salt: ByteArray,
        memoryCostKb: Int,
        iterations: Int,
        parallelism: Int,
        outputLengthBytes: Int = 32
    ): ByteArray {
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withSalt(salt)
            .withMemoryAsKB(memoryCostKb)
            .withIterations(iterations)
            .withParallelism(parallelism)

        val generator = Argon2BytesGenerator()
        generator.init(builder.build())

        val output = ByteArray(outputLengthBytes)
        generator.generateBytes(password, output)

        return output
    }
}