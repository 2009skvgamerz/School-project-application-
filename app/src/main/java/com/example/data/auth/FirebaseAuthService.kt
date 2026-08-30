package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.model.User
import com.example.model.UserRole
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class FirebaseAuthService(private val context: Context) {

  companion object {
    private const val TAG = "FirebaseAuthService"
  }

  private val credentialManager: CredentialManager by lazy {
    CredentialManager.create(context)
  }

  private val auth: FirebaseAuth? by lazy {
    try {
      if (FirebaseApp.getApps(context).isNotEmpty()) {
        FirebaseAuth.getInstance()
      } else {
        null
      }
    } catch (e: Exception) {
      Log.w(TAG, "Firebase Auth not available: ${e.message}")
      null
    }
  }

  val currentUser: FirebaseUser?
    get() = auth?.currentUser

  /**
   * Observe Firebase auth state changes as a Kotlin Flow
   */
  fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
    val firebaseAuth = auth
    if (firebaseAuth == null) {
      trySend(null)
      close()
      return@callbackFlow
    }

    val listener = FirebaseAuth.AuthStateListener { authInstance ->
      trySend(authInstance.currentUser)
    }

    firebaseAuth.addAuthStateListener(listener)
    awaitClose {
      firebaseAuth.removeAuthStateListener(listener)
    }
  }

  /**
   * Initiates Google Sign-In using modern Android CredentialManager and GetSignInWithGoogleOption.
   * On success, signs in with Firebase Auth and returns a mapped School User.
   */
  suspend fun signInWithGoogle(
    activityContext: Context,
    preferredRole: UserRole = UserRole.STUDENT,
    customServerClientId: String? = null
  ): Result<User> {
    return try {
      val clientId = customServerClientId
        ?: getWebClientId(activityContext)
        ?: "7rvxzvgj3osc7vezagm3bh.apps.googleusercontent.com"

      val googleIdOption = GetSignInWithGoogleOption.Builder(clientId)
        .build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val result = credentialManager.getCredential(
        request = request,
        context = activityContext
      )

      val credential = result.credential
      if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
      ) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken

        val firebaseAuth = auth
        if (firebaseAuth != null) {
          val authCredential = GoogleAuthProvider.getCredential(idToken, null)
          val authResult = firebaseAuth.signInWithCredential(authCredential).await()
          val firebaseUser = authResult.user

          val user = User(
            id = firebaseUser?.uid ?: "usr_${UUID.randomUUID().toString().take(8)}",
            username = firebaseUser?.email?.substringBefore("@") ?: googleIdTokenCredential.displayName ?: "google_user",
            fullName = firebaseUser?.displayName ?: googleIdTokenCredential.displayName ?: "Google User",
            email = firebaseUser?.email ?: googleIdTokenCredential.id,
            role = preferredRole,
            avatarUrl = firebaseUser?.photoUrl?.toString() ?: "",
            designation = "${preferredRole.displayName} - Google Authenticated"
          )
          Result.success(user)
        } else {
          // Offline / Local fallback if Firebase services are running locally without cloud credentials
          val user = User(
            id = "google_${UUID.randomUUID().toString().take(8)}",
            username = googleIdTokenCredential.id.substringBefore("@"),
            fullName = googleIdTokenCredential.displayName ?: "Google User",
            email = googleIdTokenCredential.id,
            role = preferredRole,
            avatarUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: "",
            designation = "${preferredRole.displayName} - Google Authenticated"
          )
          Result.success(user)
        }
      } else {
        Result.failure(IllegalStateException("Unexpected credential type: ${credential.javaClass.name}"))
      }
    } catch (e: GetCredentialCancellationException) {
      Result.failure(Exception("Sign in was cancelled."))
    } catch (e: GetCredentialException) {
      Log.e(TAG, "Credential Manager error: ${e.message}", e)
      Result.failure(Exception("Google Sign-In failed: ${e.message}"))
    } catch (e: Exception) {
      Log.e(TAG, "Google Auth error: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Signs in with Email and Password using Firebase Auth
   */
  suspend fun signInWithEmailPassword(
    email: String,
    password: String,
    role: UserRole
  ): Result<User> {
    val firebaseAuth = auth
      ?: return Result.failure(IllegalStateException("Firebase Auth is not initialized."))

    return try {
      val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
      val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user is null")

      val user = User(
        id = firebaseUser.uid,
        username = firebaseUser.email?.substringBefore("@") ?: "user",
        fullName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "School Member",
        email = firebaseUser.email ?: email,
        role = role,
        avatarUrl = firebaseUser.photoUrl?.toString() ?: "",
        designation = "${role.displayName} - Verified Portal Access"
      )
      Result.success(user)
    } catch (e: Exception) {
      Log.e(TAG, "Firebase email sign in error: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Creates a new user with Email and Password using Firebase Auth
   */
  suspend fun signUpWithEmailPassword(
    email: String,
    password: String,
    fullName: String,
    role: UserRole
  ): Result<User> {
    val firebaseAuth = auth
      ?: return Result.failure(IllegalStateException("Firebase Auth is not initialized."))

    return try {
      val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
      val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user is null")

      // Update display name
      val profileUpdates = UserProfileChangeRequest.Builder()
        .setDisplayName(fullName)
        .build()
      firebaseUser.updateProfile(profileUpdates).await()

      val user = User(
        id = firebaseUser.uid,
        username = email.substringBefore("@"),
        fullName = fullName,
        email = email,
        role = role,
        designation = "${role.displayName} - Registered"
      )
      Result.success(user)
    } catch (e: Exception) {
      Log.e(TAG, "Firebase sign up error: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Signs out of Firebase Auth and clears stored credential state
   */
  suspend fun signOut() {
    try {
      auth?.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    } catch (e: Exception) {
      Log.w(TAG, "Error during sign out: ${e.message}")
    }
  }

  private fun getWebClientId(context: Context): String? {
    return try {
      val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
      if (resId != 0) {
        val str = context.getString(resId)
        if (str.isNotBlank() && !str.contains("YOUR_")) str else null
      } else {
        null
      }
    } catch (e: Exception) {
      null
    }
  }
}
