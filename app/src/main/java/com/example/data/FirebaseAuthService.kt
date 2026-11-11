package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Firebase Auth & Firestore Service
 * Handles:
 * 1. Google Sign-In with Jetpack Credential Manager
 * 2. Community Directory profile syncing in Firestore 'users' collection
 */
class FirebaseAuthService(
    context: Context? = null
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = try {
        val appContext = context?.applicationContext
            ?: FirebaseApp.getInstance().applicationContext
        val dbId = appContext.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    } catch (_: Exception) {
        val app = FirebaseApp.getInstance()
        val dbId = app.applicationContext.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(app, dbId)
    }

    val currentUser: FirebaseUser?
        get() = try {
            if (isFirebaseAvailable()) auth.currentUser else null
        } catch (e: Exception) {
            null
        }

    /**
     * Checks if Firebase is configured with a valid, non-placeholder project and API key.
     */
    fun isFirebaseAvailable(): Boolean {
        return try {
            val app = FirebaseApp.getInstance()
            val apiKey = app.options.apiKey ?: ""
            val projectId = app.options.projectId ?: ""
            apiKey.isNotBlank() &&
                    !apiKey.contains("remixed", ignoreCase = true) &&
                    !apiKey.contains("example", ignoreCase = true) &&
                    !projectId.contains("remixed", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Authenticates with Firebase using a Google ID token from Jetpack Credential Manager.
     */
    suspend fun signInWithGoogleIdToken(idToken: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
                ?: return@withContext Result.failure(IllegalStateException("No Firebase user from Google credential"))

            // Sync user profile to Firestore 'users' collection
            val userData = hashMapOf<String, Any>(
                "uid" to user.uid,
                "name" to (user.displayName ?: "User"),
                "email" to (user.email ?: ""),
                "mobile" to (user.phoneNumber ?: ""),
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(user.uid)
                .set(userData, SetOptions.merge())
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs up / records a new user with Name, Mobile Number, Email ID.
     * Saves to Firestore 'users' collection using UID.
     */
    suspend fun signup(
        name: String,
        mobileNumber: String,
        emailId: String,
        password: String
    ): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable()) {
            return@withContext Result.failure(Exception("FIREBASE_API_KEY_INVALID"))
        }
        try {
            val cleanName = name.trim()
            val cleanEmail = emailId.trim().lowercase()
            val cleanMobile = mobileNumber.trim().filter { it.isDigit() }

            if (cleanName.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("कृपया अपना पूरा नाम दर्ज करें"))
            }
            if (cleanMobile.length < 10) {
                return@withContext Result.failure(IllegalArgumentException("कृपया 10 अंकों का मान्य मोबाइल नंबर दर्ज करें"))
            }
            if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
                return@withContext Result.failure(IllegalArgumentException("कृपया मान्य ईमेल / Gmail ID दर्ज करें"))
            }
            if (password.length < 6) {
                return@withContext Result.failure(IllegalArgumentException("पासवर्ड कम से कम 6 अक्षरों का होना चाहिए"))
            }

            val uid = "user_$cleanMobile"
            val userData = hashMapOf<String, Any>(
                "uid" to uid,
                "name" to cleanName,
                "mobile" to cleanMobile,
                "email" to cleanEmail,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(uid)
                .set(userData, SetOptions.merge())
                .await()

            Result.success(userData)
        } catch (e: Exception) {
            val friendlyMsg = mapFirebaseException(e)
            Result.failure(Exception(friendlyMsg, e))
        }
    }

    /**
     * Resolves user profile by Email ID or Mobile Number from Firestore 'users' collection.
     */
    suspend fun findUserInFirestore(
        emailOrMobile: String
    ): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable()) {
            return@withContext Result.failure(Exception("FIREBASE_API_KEY_INVALID"))
        }
        try {
            val cleanInput = emailOrMobile.trim()
            if (cleanInput.isEmpty()) {
                return@withContext Result.failure(
                    IllegalArgumentException("कृपया अपना ईमेल आईडी या मोबाइल नंबर दर्ज करें")
                )
            }

            val snapshot = if (cleanInput.contains("@")) {
                firestore.collection("users")
                    .whereEqualTo("email", cleanInput.lowercase())
                    .limit(1)
                    .get()
                    .await()
            } else {
                val digitsOnly = cleanInput.filter { it.isDigit() }
                firestore.collection("users")
                    .whereEqualTo("mobile", digitsOnly)
                    .limit(1)
                    .get()
                    .await()
            }

            if (snapshot.isEmpty) {
                return@withContext Result.failure(
                    IllegalArgumentException("इस विवरण से कोई अकाउंट नहीं मिला।")
                )
            }

            val doc = snapshot.documents.first()
            val data = doc.data ?: emptyMap()
            Result.success(data)
        } catch (e: Exception) {
            val friendlyMsg = mapFirebaseException(e)
            Result.failure(Exception(friendlyMsg, e))
        }
    }

    /**
     * Fetches user profile from Firestore 'users' collection for the given UID.
     */
    suspend fun getUserProfile(uid: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) doc.data else null
        } catch (e: Exception) {
            null
        }
    }

    fun signOut() {
        auth.signOut()
    }

    private fun mapFirebaseException(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("API key", ignoreCase = true) ||
                    msg.contains("API_KEY", ignoreCase = true) ||
                    msg.contains("api key is not valid", ignoreCase = true) ||
                    msg.contains("invalid api key", ignoreCase = true) ->
                "FIREBASE_API_KEY_INVALID"
            msg.contains("The email address is already in use", ignoreCase = true) ||
                    msg.contains("EMAIL_EXISTS", ignoreCase = true) ->
                "यह ईमेल पहले से पंजीकृत है। कृपया लॉग इन करें।"
            msg.contains("There is no user record", ignoreCase = true) ||
                    msg.contains("USER_NOT_FOUND", ignoreCase = true) ->
                "कोई अकाउंट नहीं मिला। कृपया विवरण जांचें या साइन अप करें।"
            msg.contains("badly formatted", ignoreCase = true) ||
                    msg.contains("INVALID_EMAIL", ignoreCase = true) ->
                "ईमेल का प्रारूप अमान्य है।"
            msg.contains("network error", ignoreCase = true) ||
                    msg.contains("network", ignoreCase = true) ->
                "नेटवर्क कनेक्शन त्रुटि। कृपया इंटरनेट जांचें।"
            msg.contains("blocked all requests", ignoreCase = true) ->
                "अत्यधिक प्रयासों के कारण ब्लॉक किया गया है। कृपया कुछ देर बाद पुनः प्रयास करें।"
            msg.contains("PERMISSION_DENIED", ignoreCase = true) ->
                "Firestore Permission Denied: कृपया Firestore Database Rules जांचें।"
            else -> msg.ifBlank { "प्रमाणीकरण विफल रहा।" }
        }
    }
}
