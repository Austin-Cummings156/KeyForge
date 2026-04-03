package com.example.keyforge.security

import com.example.keyforge.data.model.VaultMetadata
import com.example.keyforge.data.repository.VaultRepository
import java.security.SecureRandom

class VaultManager(
    private val vaultRepository: VaultRepository,
    private val argon2KeyDeriver: Argon2KeyDeriver = Argon2KeyDeriver(),
    private val cryptoEngine: CryptoEngine = CryptoEngine()
) {
    private val secureRandom = SecureRandom()

    private var activeVaultKey: ByteArray? = null

    companion object {
        private val VERIFIER_PLAINTEXT = "KEYFORGE_VAULT_OK".toByteArray(Charsets.UTF_8)

        private const val SALT_LENGTH_BYTES = 16
        private const val KEY_LENGTH_BYTES = 32

        private const val ARGON2_MEMORY_COST_KB = 65536
        private const val ARGON2_ITERATIONS = 3
        private const val ARGON2_PARALLELISM = 1
    }

    suspend fun doesVaultExist(): Boolean {
        return vaultRepository.doesVaultExist()
    }

    suspend fun createVault(masterPassword: CharArray): Result<Unit> {
        return runCatching {
            if (masterPassword.isEmpty()) {
                error("Master password cannot be blank.")
            }

            val existingVault = vaultRepository.getVaultMetadata()
            if (existingVault != null) {
                error("Vault already exists.")
            }

            val salt = ByteArray(SALT_LENGTH_BYTES).also { secureRandom.nextBytes(it) }

            val derivedKey = argon2KeyDeriver.deriveKey(
                password = masterPassword,
                salt = salt,
                memoryCostKb = ARGON2_MEMORY_COST_KB,
                iterations = ARGON2_ITERATIONS,
                parallelism = ARGON2_PARALLELISM,
                outputLengthBytes = KEY_LENGTH_BYTES
            )

            val encryptedVerifier = cryptoEngine.encrypt(
                plaintext = VERIFIER_PLAINTEXT,
                key = derivedKey
            )

            val metadata = VaultMetadata(
                salt = salt,
                verifierCiphertext = encryptedVerifier.ciphertext,
                verifierNonce = encryptedVerifier.nonce,
                argon2MemoryCostKb = ARGON2_MEMORY_COST_KB,
                argon2Iterations = ARGON2_ITERATIONS,
                argon2Parallelism = ARGON2_PARALLELISM
            )

            vaultRepository.saveVaultMetadata(metadata)
            setActiveVaultKey(derivedKey)
        }.also {
            masterPassword.fill('\u0000')
        }
    }

    suspend fun unlockVault(masterPassword: CharArray): Result<Unit> {
        return runCatching {
            val metadata = vaultRepository.getVaultMetadata()
                ?: error("Vault metadata not found.")

            val derivedKey = argon2KeyDeriver.deriveKey(
                password = masterPassword,
                salt = metadata.salt,
                memoryCostKb = metadata.argon2MemoryCostKb,
                iterations = metadata.argon2Iterations,
                parallelism = metadata.argon2Parallelism,
                outputLengthBytes = KEY_LENGTH_BYTES
            )

            val decryptedVerifier = cryptoEngine.decrypt(
                ciphertext = metadata.verifierCiphertext,
                key = derivedKey,
                nonce = metadata.verifierNonce
            )

            if (!decryptedVerifier.contentEquals(VERIFIER_PLAINTEXT)) {
                derivedKey.fill(0)
                error("Incorrect master password.")
            }

            setActiveVaultKey(derivedKey)
        }.also {
            masterPassword.fill('\u0000')
        }
    }

    fun lockVault() {
        activeVaultKey?.fill(0)
        activeVaultKey = null
    }

    fun isVaultUnlocked(): Boolean {
        return activeVaultKey != null
    }

    fun requireActiveVaultKey(): ByteArray {
        return activeVaultKey?.copyOf()
            ?: error("Vault is locked.")
    }

    private fun setActiveVaultKey(newKey: ByteArray) {
        activeVaultKey?.fill(0)
        activeVaultKey = newKey.copyOf()
    }
}