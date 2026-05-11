package com.example.keyforge.security

import com.example.keyforge.data.model.VaultMetadata
import com.example.keyforge.data.repository.VaultRepository
import java.security.SecureRandom

/**
 * Owns the vault lifecycle and protects the active vault key.
 *
 * KeyForge does not store the user's master password. Instead, the master
 * password derives a key with Argon2id, and that derived key unwraps the random
 * vault key stored in [VaultMetadata]. The same vault key is also wrapped by a
 * recovery-key-derived key so the user can reset their master password without
 * exposing plaintext credentials.
 *
 * The unwrapped vault key only lives in memory while the vault is unlocked.
 */
class VaultManager(
    private val vaultRepository: VaultRepository,
    private val argon2KeyDeriver: Argon2KeyDeriver = Argon2KeyDeriver(),
    private val cryptoEngine: CryptoEngine = CryptoEngine()
) {
    private val secureRandom = SecureRandom()

    private var activeVaultKey: ByteArray? = null

    /**
     * Security parameters for v1 vault creation.
     *
     * These values are stored in vault metadata so existing vaults can be unlocked
     * with the same Argon2 settings even if defaults change in the future.
     */
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

    /**
     * Creates a new vault and returns the generated recovery key.
     *
     * A random vault key is generated once, then wrapped twice: once with the
     * master-password-derived key and once with the recovery-key-derived key.
     * Only the wrapped vault key material and salts are stored in Room.
     */
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

    /**
     * Unlocks the vault with the user's master password.
     *
     * The supplied password derives a key that attempts to decrypt the wrapped
     * vault key. If decryption fails, the password is rejected without revealing
     * whether any individual field was correct.
     */
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

    /**
     * Unlocks the vault through the recovery key path.
     *
     * A valid recovery key unwraps the same vault key used by the master password.
     * The UI then moves into a reset-password state so the user can create a new
     * master password.
     */
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

    /**
     * Re-wraps the active vault key with a new master-password-derived key.
     *
     * This does not re-encrypt every credential. Credentials remain encrypted with
     * the vault key; only the metadata used to unlock that vault key is updated.
     */
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

    /**
     * Clears the in-memory vault key and returns the vault to a locked state.
     */
    fun lockVault() {
        activeVaultKey?.fill(0)
        activeVaultKey = null
    }

    fun isVaultUnlocked(): Boolean {
        return activeVaultKey != null
    }

    /**
     * Returns a defensive copy of the active vault key.
     *
     * Throws if the vault is locked. Callers should treat the returned key as
     * sensitive and avoid retaining it longer than needed.
     */
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