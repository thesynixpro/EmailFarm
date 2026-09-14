# DotMail

DotMail is a local Android utility for generating Gmail dot-placement variations and organizing them for testing.

## What it does

- Accepts a `@gmail.com` address.
- Removes existing dots from the username before generation.
- Generates every valid dot-placement pattern up to the configured safety limit.
- Never creates leading, trailing, or consecutive dots.
- Optionally includes the original address.
- Copies individual or multiple results.
- Favorites persist locally with DataStore.
- Generation history persists locally with DataStore.
- Supports search, selection, bulk copy, bulk save, and delete.
- Supports System / Light / Dark appearance and dynamic colors on supported Android versions.

For a username with `n` characters, the theoretical number of patterns is `2^(n-1)`, including the original no-dot form. DotMail applies a configurable result limit so very long usernames cannot create an excessive in-memory list.

## Privacy

No Gmail login, password, network permission, account creation, CAPTCHA automation, or verification bypass is used. The app is a local address-variation utility.

Gmail dot variations refer to the same underlying Gmail mailbox for supported personal Gmail addresses; they are not separate Gmail accounts.

## Build

- Android Gradle Plugin 9.4.0
- Gradle 9.6.0
- Java 17
- Kotlin/Compose compiler plugin 2.3.21
- Jetpack Compose BOM 2026.08.00
- Material 3 1.4.0 line via BOM
- Navigation Compose 2.10.1
- DataStore Preferences 1.2.0
- compileSdk / targetSdk 37
