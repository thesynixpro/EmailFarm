# Email Alias Sandbox

A small Kotlin Android app for prototyping email-dependent flows without using Gmail or creating real disposable mailboxes.

## Features
- Generates random test addresses under the reserved `example.test` domain.
- Copies the current test address to the clipboard.
- Shows a 15-minute local expiry timer.
- Includes a mock inbox and a sample verification email.
- No network permission and no Gmail integration.

## Build
Open the project in a recent Android Studio release and sync Gradle.

Tooling versions are pinned in the Gradle files. The project targets Android API 37.

## Important
`example.test` is a reserved testing domain. The addresses are placeholders only; they do not receive real mail.
