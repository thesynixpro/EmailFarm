# DotMail Gmail Inbox connection

DotMail can connect to a real Gmail account using Google OAuth and the Gmail API. The app requests **read-only** Gmail access (`gmail.readonly`) and does not ask for the Gmail password. The Inbox tab loads recent Inbox messages directly from Google.

## 1. Create the Google Cloud configuration

1. Create/select a Google Cloud project.
2. Enable the **Gmail API**.
3. Configure **Google Auth Platform / OAuth consent**.
4. Add the scope `https://www.googleapis.com/auth/gmail.readonly` to the app's requested data access.
5. Create an **Android OAuth client** for package name `com.dotmail.app`.
6. Add the SHA-1 certificate fingerprint of the certificate that signs the APK you will install.

Google requires the Android package name and signing-certificate SHA-1 for an Android OAuth client.

## 2. Make the GitHub Actions APK use a stable signing key

The workflow can use a stable debug keystore so the SHA-1 registered in Google Cloud stays the same between builds. Add these GitHub repository secrets:

- `DOTMAIL_DEBUG_KEYSTORE_BASE64` — base64 of your `.jks`/keystore file
- `DOTMAIL_DEBUG_KEYSTORE_PASSWORD`
- `DOTMAIL_DEBUG_KEY_ALIAS`
- `DOTMAIL_DEBUG_KEY_PASSWORD`

If these secrets are not configured, the workflow still builds a test APK using a temporary debug keystore, but Gmail OAuth may not match the SHA-1 registered in Google Cloud.

To get the SHA-1 of a keystore:

```bash
keytool -list -v -keystore your-keystore.jks -alias your-alias
```

## 3. Test connection

Install the APK, open **Inbox** or **Settings → Gmail inbox**, tap **Connect Gmail**, choose the account, and grant the requested read-only Gmail permission.

The app then calls Gmail's `users/me/profile` and Inbox `messages.list`/`messages.get` endpoints and displays recent sender, subject, date, and snippet data.

## Important privacy note

Gmail data is sensitive. DotMail uses the access token only in memory for the current app session and does not send mailbox contents to a DotMail server. If you publish the app for other people, follow Google's OAuth verification and Gmail API user-data policies for the requested scope.
