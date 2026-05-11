# KeyForge

KeyForge is a local-first Android password manager focused on encrypted on-device storage, simple vault management, and user ownership of private data.

All credential data is stored locally on the device. KeyForge does not use cloud sync, external servers, analytics, or tracking.

## Overview

Managing strong, unique passwords is difficult. KeyForge provides a private credential vault protected by a master password and backed by encrypted local storage.

The master password is never stored. It is used to derive key material for unlocking the vault, while credentials remain encrypted at rest inside the local Room database.

## Current Version

**Version:** 1.0.0

This is the first complete v1 build of KeyForge. It includes vault creation, master password login, encrypted credential storage, recovery key support, and a polished dark UI.

## Features

### Current v1 Features

- First-time vault setup
- Master password login
- Recovery key generation during setup
- Recovery-key-based password reset flow
- Add, view, edit, and delete credentials
- Encrypted local credential storage
- Password visibility toggle
- Consistent dark UI and shared app styling
- Local-only storage with no cloud dependency

## Security Model

KeyForge is designed around a local vault model.

### Master Password

- The master password is never stored.
- It is used only to derive key material for unlocking the vault.
- Incorrect passwords fail during vault-key unwrap/decryption.

### Key Derivation

- Key derivation uses Argon2id.
- Vault metadata stores the parameters needed to unlock the vault consistently.
- Salts and Argon2 parameters are stored locally as metadata, not as secrets.

### Vault Key

- Credentials are encrypted with a randomly generated vault key.
- The vault key is wrapped by key material derived from the master password.
- The same vault key is also wrapped through the recovery-key path.
- The unwrapped vault key only exists in memory while the vault is unlocked.

### Recovery Key

- KeyForge generates a recovery key during vault setup.
- The recovery key is shown once so the user can save it.
- The plaintext recovery key is not stored.
- A valid recovery key can unlock the vault key and allow the user to reset their master password.

### Credential Encryption

- Credential fields are serialized into a payload before storage.
- The payload is encrypted using AES-GCM.
- Each encrypted credential uses its own nonce.
- Room stores ciphertext and nonce data, not plaintext credential fields.

### Local Storage

- Data is stored using Room.
- Credential contents are encrypted at rest.
- Vault metadata contains salts, Argon2 parameters, and wrapped key material.
- KeyForge does not send vault data to a server.

## App Flow

1. First launch opens the vault setup screen.
2. The user creates a master password.
3. KeyForge generates and displays a recovery key.
4. The unlocked vault allows credentials to be added, viewed, edited, and deleted.
5. Restarting the app returns the vault to a locked state.
6. The user unlocks with the master password.
7. If the master password is forgotten, the recovery key can be used to reset it.

## Architecture

KeyForge uses a simple layered Android architecture:

- **UI:** Jetpack Compose and Material 3
- **State:** ViewModel and StateFlow
- **Database:** Room
- **Serialization:** Kotlinx Serialization
- **Crypto:** Argon2id and AES-GCM
- **Persistence:** Local-only encrypted storage

Important classes:

- `VaultManager` handles vault creation, unlock, lock, recovery unlock, and password reset.
- `CredentialCrypto` handles credential serialization, encryption, and decryption.
- `CredentialRepository` encrypts credentials before storage and decrypts them after reading.
- `VaultRepository` manages vault metadata persistence.
- `CredentialViewModel` exposes credential state to the unlocked UI.
- `VaultViewModel` manages setup, login, recovery, and reset-password state.

## Project Status

KeyForge v1 is complete as a portfolio-ready Android password manager prototype.

It demonstrates:

- Local encrypted storage
- Master-password-based vault unlock
- Recovery key support
- Clean separation between UI, repository, database, and crypto layers
- Compose-based Android UI
- Practical security-focused app architecture

This project is not currently published on the Play Store.

## Future Improvements

Possible future improvements include:

- Biometric unlock
- Auto-lock timer
- Manual lock button
- Secure backup and restore
- Search and filtering
- Password generator
- Autofill service support
- Import/export tools
- Security review and hardening before production release

## Developer Notes

KeyForge was built as a practical Android portfolio project with a focus on secure local data handling.

The main security goals are:

- Do not store the master password.
- Do not store plaintext credentials.
- Keep encrypted data local to the device.
- Keep the crypto and storage boundaries easy to review.
- Avoid unnecessary network or cloud dependencies.

## License

TBD