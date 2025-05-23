package com.example.signify.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.signify.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

open class AuthViewModel(
    @SuppressLint("StaticFieldLeak") private val context: Context,
    private val oneTapClient: SignInClient
) : ViewModel() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    open val authState: LiveData<AuthState> = _authState


    init {
        checkAuthStatus()
    }

    fun setAuthState(state: AuthState) {
        _authState.value = state
    }

    private fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun loginUser(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "Login successful")
                    _authState.value = AuthState.Authenticated
                } else {
                    Log.e("AuthViewModel", "Login failed: ${task.exception?.message}")
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }.addOnFailureListener {
                Log.e("AuthViewModel", "Exception: ${it.message}")
                _authState.value = AuthState.Error(it.message ?: "Something went wrong")
            }
    }


    fun createUser(email: String, password: String, name: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty.")
            return
        } else if (name.isEmpty()) {
            _authState.value = AuthState.Error("Name can't be empty.")
            return
        }

        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val user = auth.currentUser

                    if (userId != null && user != null) {
                        val profileUpdates = userProfileChangeRequest {
                            displayName = name
                        }

                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    val userMap = hashMapOf(
                                        "email" to email,
                                        "name" to name
                                    )
                                    firestore.collection("users").document(userId)
                                        .set(userMap)
                                        .addOnSuccessListener {
                                            _authState.value = AuthState.Authenticated
                                        }
                                        .addOnFailureListener { exception ->
                                            _authState.value = AuthState.Error("Failed to create user profile: ${exception.message}")
                                        }
                                } else {
                                    _authState.value = AuthState.Error("Failed to update display name: ${updateTask.exception?.message}")
                                }
                            }
                    } else {
                        _authState.value = AuthState.Error("User ID is null.")
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong.")
                }
            }
    }

    // Google SignIn
    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            ).setAutoSelectEnabled(true)
            .build()
    }

    suspend fun signIn(): IntentSender? {
        _authState.value = AuthState.Loading
        val request = buildSignInRequest()
        val result = try {
            oneTapClient.beginSignIn(request).await()
        } catch (e: Exception) {
            e.printStackTrace()
            _authState.value = AuthState.Error("Sign-in failed: ${e.message}")
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent: Intent){
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val googleIdToken = credential.googleIdToken
            val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
            // Firebase Authentication
            val user = auth.signInWithCredential(googleCredentials).await().user
            if (auth.currentUser?.uid != null) {
                val userMap = user?.run {
                    hashMapOf(
                        "email" to email,
                        "name" to displayName
                    )
                }
                firestore.collection("users").document(auth.currentUser!!.uid)
                    .set(userMap!!)
                    .addOnSuccessListener {
                        _authState.value = AuthState.Authenticated
                    }
                    .addOnFailureListener { exception ->
                        _authState.value = AuthState.Error("Failed to create user profile: ${exception.message}")
                    }
            }

        } catch (e: ApiException) {
            _authState.value = AuthState.Error("Google Sign-In failed: ${e.statusCode} - ${e.message}")
            SignInResult(
                data = null,
                errorMessage = "Google Sign-In failed: ${e.statusCode} - ${e.message}"
            )
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Sign-in failed: ${e.message}")
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String,
    val userName: String?,
    val userEmail: String,
    val profilePicture: String?
)
