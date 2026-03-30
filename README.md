# KeyForge

**KeyForge** is an Android password manager designed around a simple idea:
store credentials securely using modern cryptographic practices while keeping all sensitive data **locally encrypted on the user's device**.

The goal of KeyForge is to provide a lightweight, transparent password manager that allows users to maintain strong, unique passwords without relying on external cloud services.

---

# Overview

Modern security advice encourages using unique, complex passwords for every service.
However, remembering dozens of credentials is unrealistic for most people.

KeyForge solves this by allowing users to securely store and manage credentials using a **single master password**.

All stored credentials are encrypted locally, meaning the application does **not require external servers or cloud storage** to function.

---

# Goals

KeyForge focuses on three core principles:

* **Security** – credentials are encrypted using modern cryptographic standards (currently in development)
* **Local Ownership** – user data remains on the user's device
* **Simplicity** – minimal interface focused on reliability and usability

---

# MVP Features

The first release of KeyForge will focus on core password manager functionality.

### Vault System

* Create encrypted credential vault
* Unlock vault with master password
* Secure vault key derivation

### Credential Storage

* Store usernames and passwords
* Add notes to credentials
* Edit and delete entries
* Local encrypted database

### Security

* Argon2id key derivation
* Encrypted credential storage
* Recovery key generation

---

# Planned Features

These features are not part of the initial release but may be added later.

### Security Improvements

* Biometric unlock
* Android Keystore integration
* Auto-lock vault after inactivity
* Clipboard clearing for copied passwords

### Usability

* Password generator
* Credential search
* Favorite credentials
* UI improvements

### Integration

* Android Autofill support
* Export encrypted backups
* Import existing password databases

---

# Tech Stack

KeyForge is built using modern Android development tools.

* **Language:** Kotlin
* **Platform:** Android
* **Database:** Room
* **Architecture:** MVVM
* **Encryption:** Argon2id + AES encryption
* **UI:** Jetpack Compose (or XML depending on final implementation)

---

# Project Status

KeyForge is currently in **early development**.

## Current Features

- Add, edit, and delete credentials
- View credentials in a structured list
- Detailed credential view screen
- Password visibility toggle (hide/reveal)
- Notes support with scroll handling
- Delete confirmation dialog
- Local storage using Room database
- MVVM architecture (ViewModel + Repository)
- Jetpack Compose UI
- Dark theme UI with consistent styling
- Floating action button for adding credentials
- In-app navigation between list, detail, and form screens
- System back button handling aligned with app navigation

## In Progress

- Encryption layer (AES)
- Argon2id key derivation
- Master password setup and unlock flow
- Vault lock/unlock state management

The current focus is transitioning KeyForge from a functional credential manager into a **secure password manager** by implementing proper encryption and authentication flows.

---

# Why This Project Exists

KeyForge was created as both a learning project and a practical tool.

The project explores:

* secure credential storage
* cryptographic best practices
* Android application architecture
* user-focused security design

---

# Disclaimer

KeyForge is an experimental project and should not yet be relied upon for storing critical credentials until the application has been thoroughly tested and audited.

---

# License

License information will be added once the project reaches a stable release.
