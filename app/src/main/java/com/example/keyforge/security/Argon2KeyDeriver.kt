package com.example.keyforge.security

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

/**
 * Derives encryption keys from user-provided secrets using Argon2id.
 *
 * Argon2id is used for both the master password and recovery key paths. The
 * caller supplies the salt and work parameters so the values stored in
 * [VaultMetadata] can be reused when unlocking an existing vault.
 */
class Argon2KeyDeriver {

    /**
     * Derives a fixed-length key from a password-like secret.
     *
     * @param password The user-provided secret. The caller is responsible for
     * clearing this CharArray after use.
     * @param salt Unique random salt stored with the vault metadata.
     * @param memoryCostKb Argon2 memory cost in KiB.
     * @param iterations Argon2 iteration count.
     * @param parallelism Argon2 parallelism setting.
     * @param outputLengthBytes Length of the derived key. Defaults to 32 bytes for AES-256.
     *
     * @return A derived key suitable for wrapping or unwrapping the vault key.
     */
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