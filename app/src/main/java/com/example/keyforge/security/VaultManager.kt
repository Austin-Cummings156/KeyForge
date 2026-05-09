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
        private const val SALT_LENGTH_BYTES = 16
        private const val KEY_LENGTH_BYTES = 32
        private const val RECOVERY_KEY_BYTES = 20

        private const val ARGON2_MEMORY_COST_KB = 65536
        private const val ARGON2_ITERATIONS = 3
        private const val ARGON2_PARALLELISM = 1

        private const val RECOVERY_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }

    suspend fun doesVaultExist(): Boolean {
        return vaultRepository.doesVaultExist()
    }

    suspend fun createVault(masterPassword: CharArray): Result<String> {
        return runCatching {
            if (masterPassword.isEmpty()) {
                error("Master password cannot be blank.")
            }

            val existingVault = vaultRepository.getVaultMetadata()
            if (existingVault != null) {
                error("Vault already exists.")
            }

            val vaultKey = generateRandomBytes(KEY_LENGTH_BYTES)

            val passwordSalt = generateRandomBytes(SALT_LENGTH_BYTES)
            val recoverySalt = generateRandomBytes(SALT_LENGTH_BYTES)

            val passwordDerivedKey = deriveKey(
                secret = masterPassword,
                salt = passwordSalt
            )

            val recoveryKey = generateRecoveryKey()
            val recoveryDerivedKey = deriveKey(
                secret = recoveryKey.toCharArray(),
                salt = recoverySalt
            )

            val passwordWrappedVaultKey = cryptoEngine.encrypt(
                plaintext = vaultKey,
                key = passwordDerivedKey
            )

            val recoveryWrappedVaultKey = cryptoEngine.encrypt(
                plaintext = vaultKey,
                key = recoveryDerivedKey
            )

            val metadata = VaultMetadata(
                passwordSalt = passwordSalt,
                passwordWrappedVaultKey = passwordWrappedVaultKey.ciphertext,
                passwordWrappedVaultKeyNonce = passwordWrappedVaultKey.nonce,
                recoverySalt = recoverySalt,
                recoveryWrappedVaultKey = recoveryWrappedVaultKey.ciphertext,
                recoveryWrappedVaultKeyNonce = recoveryWrappedVaultKey.nonce,
                argon2MemoryCostKb = ARGON2_MEMORY_COST_KB,
                argon2Iterations = ARGON2_ITERATIONS,
                argon2Parallelism = ARGON2_PARALLELISM
            )

            vaultRepository.saveVaultMetadata(metadata)
            setActiveVaultKey(vaultKey)

            passwordDerivedKey.fill(0)
            recoveryDerivedKey.fill(0)

            recoveryKey
        }.also {
            masterPassword.fill('\u0000')
        }
    }

    suspend fun unlockVault(masterPassword: CharArray): Result<Unit> {
        return runCatching {
            val metadata = vaultRepository.getVaultMetadata()
                ?: error("Vault metadata not found.")

            val passwordDerivedKey = deriveKey(
                secret = masterPassword,
                salt = metadata.passwordSalt,
                memoryCostKb = metadata.argon2MemoryCostKb,
                iterations = metadata.argon2Iterations,
                parallelism = metadata.argon2Parallelism
            )

            val unwrappedVaultKey = try {
                cryptoEngine.decrypt(
                    ciphertext = metadata.passwordWrappedVaultKey,
                    key = passwordDerivedKey,
                    nonce = metadata.passwordWrappedVaultKeyNonce
                )
            } catch (_: Exception) {
                passwordDerivedKey.fill(0)
                error("Incorrect master password.")
            }

            passwordDerivedKey.fill(0)
            setActiveVaultKey(unwrappedVaultKey)
        }.also {
            masterPassword.fill('\u0000')
        }
    }

    suspend fun unlockWithRecoveryKey(recoveryKey: CharArray): Result<Unit> {
        return runCatching {
            val metadata = vaultRepository.getVaultMetadata()
                ?: error("Vault metadata not found.")

            val recoveryDerivedKey = deriveKey(
                secret = recoveryKey,
                salt = metadata.recoverySalt,
                memoryCostKb = metadata.argon2MemoryCostKb,
                iterations = metadata.argon2Iterations,
                parallelism = metadata.argon2Parallelism
            )

            val unwrappedVaultKey = try {
                cryptoEngine.decrypt(
                    ciphertext = metadata.recoveryWrappedVaultKey,
                    key = recoveryDerivedKey,
                    nonce = metadata.recoveryWrappedVaultKeyNonce
                )
            } catch (_: Exception) {
                recoveryDerivedKey.fill(0)
                error("Incorrect recovery key.")
            }

            recoveryDerivedKey.fill(0)
            setActiveVaultKey(unwrappedVaultKey)
        }.also {
            recoveryKey.fill('\u0000')
        }
    }

    suspend fun resetMasterPassword(newMasterPassword: CharArray): Result<Unit> {
        return runCatching {
            if (newMasterPassword.isEmpty()) {
                error("Master password cannot be blank.")
            }

            val currentMetadata = vaultRepository.getVaultMetadata()
                ?: error("Vault metadata not found.")

            val vaultKey = activeVaultKey?.copyOf()
                ?: error("Vault must be unlocked before resetting the password.")

            val newPasswordSalt = generateRandomBytes(SALT_LENGTH_BYTES)

            val newPasswordDerivedKey = deriveKey(
                secret = newMasterPassword,
                salt = newPasswordSalt,
                memoryCostKb = currentMetadata.argon2MemoryCostKb,
                iterations = currentMetadata.argon2Iterations,
                parallelism = currentMetadata.argon2Parallelism
            )

            val newPasswordWrappedVaultKey = cryptoEngine.encrypt(
                plaintext = vaultKey,
                key = newPasswordDerivedKey
            )

            val updatedMetadata = currentMetadata.copy(
                passwordSalt = newPasswordSalt,
                passwordWrappedVaultKey = newPasswordWrappedVaultKey.ciphertext,
                passwordWrappedVaultKeyNonce = newPasswordWrappedVaultKey.nonce
            )

            vaultRepository.saveVaultMetadata(updatedMetadata)

            newPasswordDerivedKey.fill(0)
            vaultKey.fill(0)
        }.also {
            newMasterPassword.fill('\u0000')
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

    private fun deriveKey(
        secret: CharArray,
        salt: ByteArray,
        memoryCostKb: Int = ARGON2_MEMORY_COST_KB,
        iterations: Int = ARGON2_ITERATIONS,
        parallelism: Int = ARGON2_PARALLELISM
    ): ByteArray {
        return argon2KeyDeriver.deriveKey(
            password = secret,
            salt = salt,
            memoryCostKb = memoryCostKb,
            iterations = iterations,
            parallelism = parallelism,
            outputLengthBytes = KEY_LENGTH_BYTES
        )
    }

    private fun setActiveVaultKey(newKey: ByteArray) {
        activeVaultKey?.fill(0)
        activeVaultKey = newKey.copyOf()
        newKey.fill(0)
    }

    private fun generateRandomBytes(size: Int): ByteArray {
        return ByteArray(size).also { secureRandom.nextBytes(it) }
    }

    private fun generateRecoveryKey(): String {
        val raw = generateRandomBytes(RECOVERY_KEY_BYTES)
        return raw.joinToString("") { byte ->
            RECOVERY_ALPHABET[byte.toInt().and(0xFF) % RECOVERY_ALPHABET.length].toString()
        }.chunked(4).joinToString("-")
    }
}