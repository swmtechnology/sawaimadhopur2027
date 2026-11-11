package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AuthScreen
import com.example.ui.MainViewModel
import com.example.ui.OnboardingSplashScreen
import com.example.ui.Screen
import com.example.ui.SplashScreen
import com.example.ui.WebViewScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val appThemeMode by viewModel.appThemeMode.collectAsStateWithLifecycle()
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDark = when (appThemeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemDark
            }

            MyApplicationTheme(darkTheme = isDark) {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
                val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
                val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
                val authError by viewModel.authError.collectAsStateWithLifecycle()
                val guestNotice by viewModel.guestExpiredNotice.collectAsStateWithLifecycle()
                val wrongPasswordDetected by viewModel.wrongPasswordDetected.collectAsStateWithLifecycle()
                val wrongPasswordIdentifier by viewModel.wrongPasswordIdentifier.collectAsStateWithLifecycle()
                val isResetPasswordOpen by viewModel.isResetPasswordOpen.collectAsStateWithLifecycle()
                val resetOtpSentCode by viewModel.resetOtpSentCode.collectAsStateWithLifecycle()
                val resetOtpLoading by viewModel.resetOtpLoading.collectAsStateWithLifecycle()
                val resetError by viewModel.resetError.collectAsStateWithLifecycle()
                val resetSuccess by viewModel.resetSuccess.collectAsStateWithLifecycle()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Crossfade(
                            targetState = currentScreen,
                            animationSpec = tween(durationMillis = 400),
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                Screen.Splash -> {
                                    SplashScreen(
                                        isOnline = isOnline,
                                        onProceedToMain = {
                                            viewModel.onSplashVideoCompleted()
                                        },
                                        onViewPostersGuide = {
                                            viewModel.navigateToOnboarding()
                                        }
                                    )
                                }
                                Screen.Onboarding -> {
                                    OnboardingSplashScreen(
                                        onFinishOnboarding = {
                                            viewModel.onFinishOnboarding()
                                        }
                                    )
                                }
                                Screen.Auth -> {
                                    AuthScreen(
                                        isOnline = isOnline,
                                        isLoading = isAuthLoading,
                                        errorMessage = authError,
                                        guestTimeoutMessage = guestNotice,
                                        wrongPasswordDetected = wrongPasswordDetected,
                                        wrongPasswordIdentifier = wrongPasswordIdentifier,
                                        isResetPasswordOpen = isResetPasswordOpen,
                                        resetOtpSentCode = resetOtpSentCode,
                                        resetOtpLoading = resetOtpLoading,
                                        resetError = resetError,
                                        resetSuccess = resetSuccess,
                                        onOpenResetPassword = { identifier ->
                                            viewModel.openResetPassword(identifier)
                                        },
                                        onCloseResetPassword = {
                                            viewModel.closeResetPassword()
                                        },
                                        onRequestPasswordResetOtp = { identifier ->
                                            viewModel.requestPasswordResetOtp(identifier)
                                        },
                                        onVerifyOtpAndResetPassword = { identifier, otp, newPass, onSuccess ->
                                            viewModel.verifyOtpAndResetPassword(identifier, otp, newPass, onSuccess)
                                        },
                                        onDismissWrongPasswordNotice = {
                                            viewModel.dismissWrongPasswordNotice()
                                        },
                                        onLogin = { identifier, pass ->
                                            viewModel.clearGuestExpiredNotice()
                                            viewModel.login(identifier, pass)
                                        },
                                        onSignup = { name, email, mobile, pass ->
                                            viewModel.clearGuestExpiredNotice()
                                            viewModel.signup(name, email, mobile, pass)
                                        },
                                        onGoogleSignInSuccess = { idToken, name, email ->
                                            viewModel.clearGuestExpiredNotice()
                                            viewModel.loginWithGoogle(idToken, name, email)
                                        },
                                        onGuestContinue = {
                                            viewModel.clearGuestExpiredNotice()
                                            viewModel.continueAsGuest()
                                        },
                                        onClearError = {
                                            viewModel.clearAuthError()
                                            viewModel.clearGuestExpiredNotice()
                                            viewModel.dismissWrongPasswordNotice()
                                        },
                                        onBackToSplashGuide = {
                                            viewModel.navigateToOnboarding()
                                        }
                                    )
                                }
                                Screen.WebView -> {
                                    WebViewScreen(
                                        session = activeSession,
                                        isOnline = isOnline,
                                        onLogout = {
                                            viewModel.logout()
                                        },
                                        onRetryNetwork = {
                                            viewModel.retryConnection()
                                        },
                                        onRequireLogin = {
                                            viewModel.navigateToAuth()
                                        },
                                        onGuestTimeout = {
                                            viewModel.onGuestTimeout()
                                        },
                                        onUpdateProfile = { name, mobile, email, city, imageUri ->
                                            viewModel.updateUserProfile(name, mobile, email, city, imageUri)
                                        },
                                        onViewSplashGuide = {
                                            viewModel.navigateToOnboarding()
                                        },
                                        appThemeMode = appThemeMode,
                                        onThemeChange = { mode ->
                                            viewModel.setAppThemeMode(mode)
                                        }
                                    )
                                }
                            }
                        }

                        // Persistent "No Internet" Banner docked at the bottom across all views
                        AnimatedVisibility(
                            visible = !isOnline,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300)),
                            exit = slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300)),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            PersistentNoInternetBanner(
                                onRetry = {
                                    viewModel.retryConnection()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Persistent "No Internet" Banner with immediate retry affordance and high-contrast styling.
 */
@Composable
fun PersistentNoInternetBanner(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("persistent_no_internet_banner"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFDC2626), // Vivid crimson warning
        shadowElevation = 10.dp,
        border = BorderStroke(1.5.dp, Color(0xFFFCA5A5).copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "No Internet Connection",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "No Internet Connection",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Check Wi-Fi or mobile data",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFFDC2626)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("no_internet_retry_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry Connection",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Retry",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// Retained for backwards-compatibility and tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Sawai Madhopur")
    }
}
