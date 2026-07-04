package com.example.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

sealed class GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult()
    object NotConfigured : GoogleSignInResult()
    object Cancelled : GoogleSignInResult()
    data class Failure(val message: String) : GoogleSignInResult()
}

/**
 * Requests a Google ID token via Credential Manager. Returns [GoogleSignInResult.NotConfigured]
 * without touching Credential Manager if `default_web_client_id` doesn't exist yet — that
 * resource is only generated once the "Google" sign-in provider is enabled in the Firebase
 * Console and google-services.json is re-synced, so this keeps the app safe to build and run
 * before that one-time setup is done.
 */
suspend fun requestGoogleIdToken(context: Context): GoogleSignInResult {
    val webClientIdRes = context.resources.getIdentifier(
        "default_web_client_id", "string", context.packageName
    )
    if (webClientIdRes == 0) return GoogleSignInResult.NotConfigured
    val serverClientId = context.getString(webClientIdRes)

    val option = GetSignInWithGoogleOption.Builder(serverClientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

    return try {
        val response = CredentialManager.create(context).getCredential(context, request)
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleSignInResult.Success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
        } else {
            GoogleSignInResult.Failure("Unexpected credential type returned.")
        }
    } catch (e: GetCredentialCancellationException) {
        GoogleSignInResult.Cancelled
    } catch (e: GetCredentialException) {
        GoogleSignInResult.Failure(e.message ?: "Google Sign-In failed.")
    } catch (e: GoogleIdTokenParsingException) {
        GoogleSignInResult.Failure("Couldn't read the Google credential.")
    }
}
