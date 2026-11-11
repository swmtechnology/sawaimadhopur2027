package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SessionEntity
import com.example.data.UserProfileManager
import com.example.data.UserRepository
import com.example.network.NetworkMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class Screen {
    data object Splash : Screen()
    data object Onboarding : Screen()
    data object Auth : Screen()
    data object WebView : Screen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = UserRepository(database.userDao())
    val networkMonitor = NetworkMonitor(application)

    val isOnline: StateFlow<Boolean> = networkMonitor.isConnected

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _activeSession = MutableStateFlow<SessionEntity?>(null)
    val activeSession: StateFlow<SessionEntity?> = _activeSession.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _guestExpiredNotice = MutableStateFlow<String?>(null)
    val guestExpiredNotice: StateFlow<String?> = _guestExpiredNotice.asStateFlow()

    // Wrong password / Forgot password state
    private val _wrongPasswordDetected = MutableStateFlow(false)
    val wrongPasswordDetected: StateFlow<Boolean> = _wrongPasswordDetected.asStateFlow()

    private val _wrongPasswordIdentifier = MutableStateFlow("")
    val wrongPasswordIdentifier: StateFlow<String> = _wrongPasswordIdentifier.asStateFlow()

    private val _isResetPasswordOpen = MutableStateFlow(false)
    val isResetPasswordOpen: StateFlow<Boolean> = _isResetPasswordOpen.asStateFlow()

    private val _resetOtpSentCode = MutableStateFlow<String?>(null)
    val resetOtpSentCode: StateFlow<String?> = _resetOtpSentCode.asStateFlow()

    private val _resetOtpLoading = MutableStateFlow(false)
    val resetOtpLoading: StateFlow<Boolean> = _resetOtpLoading.asStateFlow()

    private val _resetError = MutableStateFlow<String?>(null)
    val resetError: StateFlow<String?> = _resetError.asStateFlow()

    private val _resetSuccess = MutableStateFlow<String?>(null)
    val resetSuccess: StateFlow<String?> = _resetSuccess.asStateFlow()

    private val prefs = application.getSharedPreferences("app_theme_prefs", android.content.Context.MODE_PRIVATE)
    private val _appThemeMode = MutableStateFlow(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
    val appThemeMode: StateFlow<String> = _appThemeMode.asStateFlow()

    fun setAppThemeMode(mode: String) {
        _appThemeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    init {
        UserProfileManager.init(application)

        viewModelScope.launch {
            repository.seedInitialData()
        }

        viewModelScope.launch {
            repository.activeSession.collect { session ->
                _activeSession.value = session
                if (session != null && session.isLoggedIn && session.username != "guest") {
                    UserProfileManager.syncWithSession(application, session)
                }
            }
        }
    }

    fun onSplashVideoCompleted() {
        // User request: "videoke bd imge space screen pr aani chahiye"
        // After video finishes, navigate to the image splash screen
        _currentScreen.value = Screen.Onboarding
    }

    fun onFinishOnboarding() {
        viewModelScope.launch {
            val session = repository.getActiveSessionOnce()
            if (session != null && session.isLoggedIn) {
                _currentScreen.value = Screen.WebView
            } else {
                val guest = repository.continueAsGuest()
                _activeSession.value = guest
                _currentScreen.value = Screen.WebView
            }
        }
    }

    fun login(identifier: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            _wrongPasswordDetected.value = false
            val result = repository.login(identifier, pass)
            result.fold(
                onSuccess = { session ->
                    _wrongPasswordDetected.value = false
                    _wrongPasswordIdentifier.value = ""
                    UserProfileManager.syncWithSession(getApplication(), session)
                    _activeSession.value = session
                    _currentScreen.value = Screen.WebView
                },
                onFailure = { error ->
                    if (error is com.example.data.WrongPasswordException) {
                        _wrongPasswordDetected.value = true
                        _wrongPasswordIdentifier.value = identifier.trim()
                        _authError.value = "⚠️ गलत पासवर्ड दर्ज किया गया है! आप नीचे 'OTP से नया पासवर्ड बनाएं' द्वारा तुरंत नया पासवर्ड बना सकते हैं।"
                    } else {
                        _wrongPasswordDetected.value = false
                        val rawMsg = error.message ?: ""
                        _authError.value = if (rawMsg.contains("API key", ignoreCase = true) ||
                            rawMsg.contains("API_KEY", ignoreCase = true) ||
                            rawMsg.contains("FIREBASE", ignoreCase = true)
                        ) {
                            "अकाउंट नहीं मिला या पासवर्ड गलत है। कृपया पहले साइन अप करें।"
                        } else {
                            rawMsg.ifBlank { "लॉगिन असफल रहा। कृपया विवरण जांचें।" }
                        }
                    }
                }
            )
            _isAuthLoading.value = false
        }
    }

    fun loginWithGoogle(idToken: String, displayName: String?, email: String?) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            _wrongPasswordDetected.value = false
            val result = repository.loginWithGoogle(idToken, displayName, email)
            result.fold(
                onSuccess = { session ->
                    UserProfileManager.syncWithSession(getApplication(), session)
                    _activeSession.value = session
                    _currentScreen.value = Screen.WebView
                },
                onFailure = { error ->
                    _authError.value = error.localizedMessage ?: "Google Sign-In failed"
                }
            )
            _isAuthLoading.value = false
        }
    }

    fun openResetPassword(defaultTarget: String = "") {
        _resetError.value = null
        _resetSuccess.value = null
        _resetOtpSentCode.value = null
        if (defaultTarget.isNotBlank()) {
            _wrongPasswordIdentifier.value = defaultTarget
        }
        _isResetPasswordOpen.value = true
    }

    fun closeResetPassword() {
        _isResetPasswordOpen.value = false
        _resetOtpSentCode.value = null
        _resetError.value = null
    }

    fun dismissWrongPasswordNotice() {
        _wrongPasswordDetected.value = false
    }

    fun requestPasswordResetOtp(identifier: String) {
        viewModelScope.launch {
            _resetOtpLoading.value = true
            _resetError.value = null
            _resetSuccess.value = null
            val result = repository.sendPasswordResetOtp(identifier)
            result.fold(
                onSuccess = { otp ->
                    _resetOtpSentCode.value = otp
                    _resetSuccess.value = "OTP आपके $identifier पर भेज दिया गया है!"
                },
                onFailure = { err ->
                    _resetError.value = err.message ?: "OTP भेजने में त्रुटि हुई।"
                }
            )
            _resetOtpLoading.value = false
        }
    }

    fun verifyOtpAndResetPassword(
        identifier: String,
        otp: String,
        newPass: String,
        onSuccess: (newPass: String) -> Unit
    ) {
        viewModelScope.launch {
            _resetOtpLoading.value = true
            _resetError.value = null
            val result = repository.resetPasswordWithOtp(identifier, otp, newPass)
            result.fold(
                onSuccess = {
                    _resetSuccess.value = "पासवर्ड सफलतापूर्वक बदल दिया गया है!"
                    _wrongPasswordDetected.value = false
                    _resetOtpSentCode.value = null
                    onSuccess(newPass)
                },
                onFailure = { err ->
                    _resetError.value = err.message ?: "पासवर्ड रीसेट करने में त्रुटि।"
                }
            )
            _resetOtpLoading.value = false
        }
    }

    fun signup(fullName: String, email: String, mobile: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val result = repository.signup(fullName, email, mobile, pass)
            result.fold(
                onSuccess = { session ->
                    UserProfileManager.syncWithSession(getApplication(), session)
                    _activeSession.value = session
                    _currentScreen.value = Screen.WebView
                },
                onFailure = { error ->
                    val rawMsg = error.message ?: ""
                    _authError.value = if (rawMsg.contains("API key", ignoreCase = true) ||
                        rawMsg.contains("API_KEY", ignoreCase = true) ||
                        rawMsg.contains("FIREBASE", ignoreCase = true)
                    ) {
                        "साइन अप विफल रहा। कृपया विवरण जांचें।"
                    } else {
                        rawMsg.ifBlank { "साइन अप असफल रहा। कृपया पुनः प्रयास करें।" }
                    }
                }
            )
            _isAuthLoading.value = false
        }
    }

    fun updateUserProfile(
        fullName: String,
        mobile: String,
        email: String,
        city: String,
        newImageUri: android.net.Uri? = null
    ) {
        viewModelScope.launch {
            UserProfileManager.saveProfile(
                context = getApplication(),
                name = fullName,
                phone = mobile,
                email = email,
                city = city,
                newImageUri = newImageUri
            )
            val updated = repository.updateProfile(fullName, mobile, email, city)
            if (updated != null) {
                _activeSession.value = updated
            }
        }
    }

    fun onGuestTimeout() {
        _guestExpiredNotice.value = "गेस्ट मोड की 5 मिनट की समय सीमा समाप्त हो गई है। आगे की सेवाओं के लिए कृपया लॉगिन या साइन अप करें।"
        _currentScreen.value = Screen.Auth
    }

    fun clearGuestExpiredNotice() {
        _guestExpiredNotice.value = null
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            _isAuthLoading.value = true
            val guest = repository.continueAsGuest()
            _activeSession.value = guest
            _currentScreen.value = Screen.WebView
            _isAuthLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            UserProfileManager.clearOnLogout(getApplication())
            _activeSession.value = null
            _currentScreen.value = Screen.Onboarding
        }
    }

    fun navigateToOnboarding() {
        _currentScreen.value = Screen.Onboarding
    }

    fun navigateToAuth() {
        _currentScreen.value = Screen.Auth
    }

    fun navigateToWebView() {
        _currentScreen.value = Screen.WebView
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun retryConnection(): Boolean {
        return networkMonitor.recheckConnection()
    }

    override fun onCleared() {
        super.onCleared()
        networkMonitor.unregister()
    }
}
