# KeyForge 🔐

KeyForge is a local-first Android password manager focused on security, simplicity, and full user ownership.

All data is stored encrypted on-device. No cloud sync, no external servers, no tracking.

---

## 🚀 Overview

Managing strong, unique passwords is difficult. KeyForge solves this by providing a secure vault protected by a single master password.

Your data never leaves your device, and your master password is never stored.

---

## 🔐 Security Model

KeyForge uses modern cryptographic practices to protect user data:

- **Master Password**
    - Never stored or persisted
    - Used only to derive an encryption key

- **Key Derivation**
    - Argon2id with configurable parameters (memory, iterations, parallelism)

- **Encryption**
    - AES-GCM for authenticated encryption
    - Unique nonce per encrypted payload

- **Vault Verification**
    - Encrypted verifier blob confirms password correctness without storing the password

- **Data Storage**
    - All credentials are encrypted at rest
    - Database contains only ciphertext + metadata (no plaintext fields)

- **In-Memory Security**
    - Encryption key exists only while the vault is unlocked
    - Key is cleared when the vault is locked or app is closed

---

## 📱 Features

### Current (v1)

- 🔐 Master password setup and login
- 🧠 Argon2id-based key derivation
- 🔒 Fully encrypted credential storage
- 👁️ Password visibility toggle (secure UI handling)
- 📋 Add, edit, delete credentials
- 📂 Local-only storage (no cloud dependency)
- 🎨 Consistent dark UI with custom branding

---

## 🧭 App Flow

1. First launch → Create vault (set master password)
2. Vault created → credentials unlocked
3. App restart → vault locked
4. Enter password → unlock vault
5. Access credentials securely

---

## 🏗️ Architecture

- **UI**: Jetpack Compose (Material3)
- **State**: ViewModel + StateFlow
- **Database**: Room
- **Crypto Layer**:
    - Argon2id (Bouncy Castle)
    - AES-GCM encryption engine
- **Separation of Concerns**:
    - `VaultManager` → vault lifecycle + key handling
    - `CredentialCrypto` → encryption/decryption
    - `Repository` → data flow abstraction

---

## ⚠️ Important Notes

- There is currently **no password recovery**
- If you forget your master password, your data is permanently inaccessible
- This is intentional for security

---

## 🔮 Future Improvements

- 🔒 Auto-lock and manual lock controls
- 👆 Biometric unlock (fingerprint / face)
- 📦 Backup & restore (secure export)
- 🔍 Search optimization without exposing sensitive data
- 🧩 Modular expansion (potential LifeOS integration)

---

## 🧑‍💻 Developer Notes

KeyForge is actively under development and evolving toward a production-ready secure password manager.

Security decisions prioritize:
- No plaintext storage
- No unnecessary exposure of secrets
- Minimal attack surface

---

## 📜 License

TBD