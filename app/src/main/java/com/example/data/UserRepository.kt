package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class WrongPasswordException(
    override val message: String,
    val identifier: String
) : Exception(message)

data class OtpResetState(
    val identifier: String,
    val userId: Long,
    val otpCode: String,
    val expiresAt: Long
)

class UserRepository(
    private val userDao: UserDao,
    private val firebaseAuthService: FirebaseAuthService = FirebaseAuthService()
) {

    private var currentPasswordResetOtp: OtpResetState? = null

    val activeSession: Flow<SessionEntity?> = userDao.getActiveSession()

    suspend fun getActiveSessionOnce(): SessionEntity? = withContext(Dispatchers.IO) {
        userDao.getActiveSessionOnce()
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun seedInitialData() = withContext(Dispatchers.IO) {
        if (userDao.getUserCount() == 0) {
            val defaultUser = UserEntity(
                fullName = "Sawai Madhopur User",
                email = "sawaimadhopur.help@gmail.com",
                mobile = "6367094588",
                username = "sawaimadhopur",
                passwordHash = hashPassword("123456")
            )
            userDao.insertUser(defaultUser)
        }
    }

    /**
     * Login flow:
     * - Checks local SQLite/Room database first for instant offline-ready access and saved accounts.
     * - If not found locally and Firebase is configured, queries Firebase Auth / Firestore.
     * - Gracefully handles unconfigured/invalid Firebase API keys without throwing errors to the user.
     */
    suspend fun login(identifier: String, password: String): Result<SessionEntity> = withContext(Dispatchers.IO) {
        val cleanIdentifier = identifier.trim()
        if (cleanIdentifier.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("कृपया अपना ईमेल आईडी या मोबाइल नंबर दर्ज करें"))
        }
        if (password.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("कृपया अपना पासवर्ड दर्ज करें"))
        }

        val hash = hashPassword(password)

        // 1. Check local database first (Room DB has all registered users and instant offline-ready access)
        var localUser = userDao.findUserByIdentifier(cleanIdentifier)
        if (localUser == null && !cleanIdentifier.contains("@")) {
            val digits = cleanIdentifier.filter { it.isDigit() }
            if (digits.isNotEmpty()) {
                localUser = userDao.findUserByMobile(digits)
            }
        }
        if (localUser == null && cleanIdentifier.contains("@")) {
            localUser = userDao.findUserByEmail(cleanIdentifier.lowercase())
        }

        if (localUser != null) {
            if (localUser.passwordHash == hash) {
                val session = SessionEntity(
                    userId = localUser.id,
                    fullName = localUser.fullName,
                    email = localUser.email,
                    mobile = localUser.mobile,
                    username = localUser.username,
                    isLoggedIn = true,
                    loginTimestamp = System.currentTimeMillis()
                )
                userDao.saveSession(session)
                return@withContext Result.success(session)
            } else {
                return@withContext Result.failure(
                    WrongPasswordException("गलत पासवर्ड दर्ज किया गया है। आप नीचे OTP द्वारा नया पासवर्ड बना सकते हैं।", cleanIdentifier)
                )
            }
        }

        // 2. If not found locally, and Firebase is configured with a valid API key, try Firebase
        if (firebaseAuthService.isFirebaseAvailable()) {
            val firebaseResult = firebaseAuthService.findUserInFirestore(cleanIdentifier)
            if (firebaseResult.isSuccess) {
                val profile = firebaseResult.getOrNull() ?: emptyMap()
                val resolvedName = (profile["name"] as? String)?.ifBlank { null }
                    ?: cleanIdentifier.substringBefore("@").replaceFirstChar { it.uppercase() }
                val resolvedEmail = (profile["email"] as? String) ?: cleanIdentifier
                val resolvedMobile = (profile["mobile"] as? String)
                    ?: if (!cleanIdentifier.contains("@")) cleanIdentifier.filter { it.isDigit() } else ""

                // Sync with local Room database
                var dbUser = userDao.findUserByEmail(resolvedEmail)
                if (dbUser == null && resolvedMobile.isNotEmpty()) {
                    dbUser = userDao.findUserByMobile(resolvedMobile)
                }

                val userId = if (dbUser != null) {
                    dbUser.id
                } else {
                    userDao.insertUser(
                        UserEntity(
                            fullName = resolvedName,
                            email = resolvedEmail,
                            mobile = resolvedMobile,
                            username = resolvedEmail.substringBefore("@"),
                            passwordHash = hash
                        )
                    )
                }

                val session = SessionEntity(
                    userId = userId,
                    fullName = resolvedName,
                    email = resolvedEmail,
                    mobile = resolvedMobile,
                    username = resolvedEmail.substringBefore("@"),
                    isLoggedIn = true,
                    loginTimestamp = System.currentTimeMillis()
                )
                userDao.saveSession(session)
                return@withContext Result.success(session)
            } else {
                val errMsg = firebaseResult.exceptionOrNull()?.message ?: ""
                if (errMsg.contains("wrong-password", ignoreCase = true) ||
                    errMsg.contains("INVALID_PASSWORD", ignoreCase = true) ||
                    errMsg.contains("invalid-credential", ignoreCase = true) ||
                    errMsg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true)
                ) {
                    return@withContext Result.failure(
                        WrongPasswordException("गलत पासवर्ड दर्ज किया गया है। आप नीचे OTP द्वारा नया पासवर्ड बना सकते हैं।", cleanIdentifier)
                    )
                }
            }
        }

        // If local user was not found and Firebase is not available / has invalid API key:
        Result.failure(
            IllegalArgumentException("इस ईमेल आईडी या मोबाइल नंबर से कोई अकाउंट नहीं मिला। कृपया पहले 'साइन अप (Sign Up)' करें।")
        )
    }

    suspend fun loginWithGoogle(
        googleIdToken: String,
        displayName: String?,
        email: String?
    ): Result<SessionEntity> = withContext(Dispatchers.IO) {
        val fbResult = firebaseAuthService.signInWithGoogleIdToken(googleIdToken)
        val fbUser = fbResult.getOrNull()

        val resolvedName = displayName ?: fbUser?.displayName ?: "Google User"
        val resolvedEmail = email ?: fbUser?.email ?: "user@gmail.com"
        val resolvedMobile = fbUser?.phoneNumber ?: ""

        var dbUser = userDao.findUserByEmail(resolvedEmail)
        val userId = if (dbUser != null) {
            dbUser.id
        } else {
            userDao.insertUser(
                UserEntity(
                    fullName = resolvedName,
                    email = resolvedEmail,
                    mobile = resolvedMobile,
                    username = resolvedEmail.substringBefore("@"),
                    passwordHash = ""
                )
            )
        }

        val session = SessionEntity(
            userId = userId,
            fullName = resolvedName,
            email = resolvedEmail,
            mobile = resolvedMobile,
            username = resolvedEmail.substringBefore("@"),
            isLoggedIn = true,
            loginTimestamp = System.currentTimeMillis()
        )
        userDao.saveSession(session)
        Result.success(session)
    }

    /**
     * Signup flow:
     * - Collects Name, Mobile Number, Email ID, and Password.
     * - Checks for existing user in local Room database.
     * - If Firebase has a valid API key, creates user in Firebase Auth & Firestore.
     * - If Firebase is unconfigured or returns invalid API key, registers directly in local Room database.
     * - Saves active session for immediate UI access and offline persistence.
     */
    suspend fun signup(
        fullName: String,
        email: String,
        mobile: String,
        password: String
    ): Result<SessionEntity> = withContext(Dispatchers.IO) {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanMobile = mobile.trim().filter { it.isDigit() }

        if (cleanName.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("कृपया अपना पूरा नाम दर्ज करें"))
        }
        if (cleanMobile.length < 10) {
            return@withContext Result.failure(IllegalArgumentException("कृपया 10 अंकों का मान्य मोबाइल नंबर दर्ज करें"))
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return@withContext Result.failure(IllegalArgumentException("कृपया मान्य Gmail ID / ईमेल पता दर्ज करें"))
        }
        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("पासवर्ड कम से कम 6 अक्षरों का होना चाहिए"))
        }

        // Check if user already registered locally
        val existingByEmail = userDao.findUserByEmail(cleanEmail)
        if (existingByEmail != null) {
            return@withContext Result.failure(IllegalArgumentException("यह Gmail ID पहले से पंजीकृत है। कृपया लॉगिन करें।"))
        }
        val existingByMobile = userDao.findUserByMobile(cleanMobile)
        if (existingByMobile != null) {
            return@withContext Result.failure(IllegalArgumentException("यह मोबाइल नंबर पहले से पंजीकृत है। कृपया सीधे लॉगिन करें।"))
        }

        val username = cleanEmail.substringBefore("@")
        val passwordHash = hashPassword(password)

        // Helper to register locally in SQLite/Room and create active session
        suspend fun registerLocally(): SessionEntity {
            val newUser = UserEntity(
                fullName = cleanName,
                email = cleanEmail,
                mobile = cleanMobile,
                username = username,
                passwordHash = passwordHash
            )
            val newId = userDao.insertUser(newUser)
            val session = SessionEntity(
                userId = newId,
                fullName = cleanName,
                email = cleanEmail,
                mobile = cleanMobile,
                username = username,
                isLoggedIn = true,
                loginTimestamp = System.currentTimeMillis()
            )
            userDao.saveSession(session)
            return session
        }

        // Try Firebase if properly configured with a valid API key
        if (firebaseAuthService.isFirebaseAvailable()) {
            val firebaseResult = firebaseAuthService.signup(
                name = cleanName,
                mobileNumber = cleanMobile,
                emailId = cleanEmail,
                password = password
            )

            if (firebaseResult.isSuccess) {
                val session = registerLocally()
                return@withContext Result.success(session)
            }

            val errMsg = firebaseResult.exceptionOrNull()?.message ?: ""
            // If Firebase failed specifically due to invalid API key or configuration, fall back seamlessly to local DB
            if (errMsg.contains("FIREBASE_API_KEY_INVALID", ignoreCase = true) ||
                errMsg.contains("API key", ignoreCase = true) ||
                errMsg.contains("API_KEY", ignoreCase = true) ||
                errMsg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ||
                errMsg.contains("network", ignoreCase = true)
            ) {
                val session = registerLocally()
                return@withContext Result.success(session)
            } else {
                return@withContext Result.failure(
                    firebaseResult.exceptionOrNull() ?: IllegalArgumentException("साइन अप असफल रहा। कृपया पुनः प्रयास करें।")
                )
            }
        } else {
            // Firebase API key is unconfigured / dummy -> register locally in Room directly
            val session = registerLocally()
            return@withContext Result.success(session)
        }
    }

    suspend fun continueAsGuest(): SessionEntity = withContext(Dispatchers.IO) {
        val guestSession = SessionEntity(
            userId = 0,
            fullName = "Guest Explorer",
            email = "guest@sawaimadhopur.portal",
            mobile = "",
            username = "guest",
            isLoggedIn = false,
            loginTimestamp = System.currentTimeMillis()
        )
        userDao.saveSession(guestSession)
        guestSession
    }

    suspend fun updateProfile(
        fullName: String,
        mobile: String,
        email: String,
        city: String = "सवाई माधोपुर"
    ): SessionEntity? = withContext(Dispatchers.IO) {
        val session = userDao.getActiveSessionOnce() ?: return@withContext null
        val updatedSession = session.copy(
            fullName = fullName.trim(),
            mobile = mobile.trim(),
            email = email.trim()
        )
        userDao.saveSession(updatedSession)
        if (session.userId > 0) {
            userDao.updateUser(session.userId, fullName.trim(), mobile.trim(), email.trim())
        }
        updatedSession
    }

    suspend fun searchUsers(query: String): List<UserEntity> = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.isEmpty()) return@withContext emptyList()
        userDao.searchUsers(clean)
    }

    suspend fun getAllRegisteredUsers(): List<UserEntity> = withContext(Dispatchers.IO) {
        userDao.getAllUsers()
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        firebaseAuthService.signOut()
        userDao.clearSession()
    }

    /**
     * Generates and sends OTP ONLY when user needs to reset a wrong or forgotten password.
     * Supports both Gmail ID and 10-digit Mobile Number.
     * Normal registration and regular login NEVER use OTP.
     */
    suspend fun sendPasswordResetOtp(identifier: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanId = identifier.trim()
        if (cleanId.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("कृपया अपना मोबाइल नंबर या Gmail ID दर्ज करें"))
        }

        val isEmail = cleanId.contains("@")
        val digitsOnly = cleanId.filter { it.isDigit() }

        // Check if user exists in database
        var user = userDao.findUserByIdentifier(cleanId)
        if (user == null && !isEmail && digitsOnly.isNotEmpty()) {
            user = userDao.findUserByMobile(digitsOnly)
            if (user == null && digitsOnly.length >= 10) {
                user = userDao.findUserByMobile(digitsOnly.takeLast(10))
            }
        }
        if (user == null && isEmail) {
            user = userDao.findUserByEmail(cleanId.lowercase())
        }

        val otp = (100000..999999).random().toString()
        val expiresAt = System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutes valid
        currentPasswordResetOtp = OtpResetState(
            identifier = cleanId,
            userId = user?.id ?: -1L,
            otpCode = otp,
            expiresAt = expiresAt
        )

        // Return generated OTP code
        Result.success(otp)
    }

    /**
     * Verifies the 6-digit OTP and resets the password for the account.
     * If user exists, updates their password hash.
     * If account didn't exist yet, registers a new account with the new password.
     */
    suspend fun resetPasswordWithOtp(
        identifier: String,
        enteredOtp: String,
        newPass: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanId = identifier.trim()
        val cleanOtp = enteredOtp.trim()
        val cleanPass = newPass.trim()

        val pending = currentPasswordResetOtp
        if (pending == null) {
            return@withContext Result.failure(IllegalArgumentException("कृपया पहले 'OTP भेजें' बटन दबाकर OTP प्राप्त करें"))
        }

        if (System.currentTimeMillis() > pending.expiresAt) {
            currentPasswordResetOtp = null
            return@withContext Result.failure(IllegalArgumentException("OTP की समय सीमा समाप्त (Expired) हो चुकी है। कृपया पुनः OTP भेजें।"))
        }

        if (pending.otpCode != cleanOtp) {
            return@withContext Result.failure(IllegalArgumentException("गलत OTP दर्ज किया गया है। कृपया सही 6-अंकों का OTP लिखें।"))
        }

        if (cleanPass.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("नया पासवर्ड कम से कम 6 अक्षरों का होना चाहिए"))
        }

        val newHash = hashPassword(cleanPass)

        if (pending.userId != -1L) {
            userDao.updatePassword(pending.userId, newHash)
        } else {
            val isEmail = cleanId.contains("@")
            val digits = cleanId.filter { it.isDigit() }
            val mobileStr = if (!isEmail) (if (digits.length >= 10) digits.takeLast(10) else digits) else ""
            val emailStr = if (isEmail) cleanId.lowercase() else (if (mobileStr.isNotBlank()) "$mobileStr@user.local" else "user@sawaimadhopur.in")
            val fallbackName = if (isEmail) cleanId.substringBefore("@") else "User $mobileStr"

            // Check if user was added concurrently
            val existing = if (isEmail) userDao.findUserByEmail(emailStr) else userDao.findUserByMobile(mobileStr)
            if (existing != null) {
                userDao.updatePassword(existing.id, newHash)
            } else {
                userDao.insertUser(
                    UserEntity(
                        fullName = fallbackName,
                        email = emailStr,
                        mobile = mobileStr,
                        username = cleanId,
                        passwordHash = newHash
                    )
                )
            }
        }

        currentPasswordResetOtp = null
        Result.success(Unit)
    }
}

