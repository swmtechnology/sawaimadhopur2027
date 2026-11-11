package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.OfflineRed
import com.example.ui.theme.RoyalAmberSecondary
import com.example.ui.theme.TerracottaPrimary

@Composable
fun AuthScreen(
    isOnline: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    guestTimeoutMessage: String? = null,
    wrongPasswordDetected: Boolean = false,
    wrongPasswordIdentifier: String = "",
    isResetPasswordOpen: Boolean = false,
    resetOtpSentCode: String? = null,
    resetOtpLoading: Boolean = false,
    resetError: String? = null,
    resetSuccess: String? = null,
    onOpenResetPassword: (identifier: String) -> Unit = {},
    onCloseResetPassword: () -> Unit = {},
    onRequestPasswordResetOtp: (identifier: String) -> Unit = {},
    onVerifyOtpAndResetPassword: (identifier: String, otp: String, newPass: String, onSuccess: (newPass: String) -> Unit) -> Unit = { _, _, _, _ -> },
    onDismissWrongPasswordNotice: () -> Unit = {},
    onLogin: (identifier: String, pass: String) -> Unit,
    onSignup: (name: String, email: String, mobile: String, pass: String) -> Unit,
    onGoogleSignInSuccess: (googleIdToken: String, displayName: String?, email: String?) -> Unit = { _, _, _ -> },
    onGuestContinue: () -> Unit,
    onClearError: () -> Unit,
    onBackToSplashGuide: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }

    // Login state
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Signup state: Name, Mobile Number, Email ID, and Password
    var signupFullName by remember { mutableStateOf("") }
    var signupMobile by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var signupConfirmPassword by remember { mutableStateOf("") }
    var signupPasswordVisible by remember { mutableStateOf(false) }
    var localValidationMsg by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .testTag("auth_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Guest timeout notification if redirected after 1 minute
            AnimatedVisibility(visible = guestTimeoutMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("guest_timeout_banner"),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Guest timeout",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "⏰ गेस्ट ट्रायल समाप्त (5 मिनट का समय पूरा हुआ)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFD97706)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = guestTimeoutMessage ?: "कृपया आगे की सभी सेवाओं के लिए अपना मोबाइल नंबर या Gmail ID से लॉगिन / साइनअप करें।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Button to revisit 4 splash screens
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                TextButton(
                    onClick = onBackToSplashGuide,
                    modifier = Modifier.testTag("back_to_splash_guide_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "View 4 Splash Screens",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "← Swipe 4 Splash Screens",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            // Offline alert pill if disconnected
            AnimatedVisibility(visible = !isOnline) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("auth_offline_banner"),
                    colors = CardDefaults.cardColors(containerColor = OfflineRed.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Offline indicator",
                            tint = OfflineRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No internet connection. Saved SQLite accounts work offline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OfflineRed
                        )
                    }
                }
            }

            // Glowing Glass Top Header Emblem
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            listOf(
                                TerracottaPrimary,
                                Color(0xFF991B1B)
                            )
                        )
                    )
                    .border(
                        2.dp,
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.6f), Color(0xFF38BDF8).copy(alpha = 0.6f))
                        ),
                        CircleShape
                    )
                    .testTag("auth_logo"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "Sawai Madhopur Emblem",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "सवाई माधोपुर पोर्टल",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White,
                modifier = Modifier.testTag("auth_title")
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF38BDF8).copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f))
            ) {
                Text(
                    text = "✨ प्रीमियम ग्लास इंटरफ़ेस • Ranthambore All-in-One",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xFF7DD3FC),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp).testTag("auth_subtitle")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Glassmorphism Auth Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.75f),
                border = BorderStroke(
                    1.5.dp,
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color(0xFF38BDF8).copy(alpha = 0.30f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    )
                ),
                shadowElevation = 24.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Modern Segmented Tab Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                            .padding(4.dp)
                            .testTag("auth_tabs"),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AuthTabPill(
                            title = "Log In",
                            isSelected = selectedTab == 0,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedTab = 0
                                localValidationMsg = null
                                onClearError()
                            },
                            testTag = "tab_login"
                        )
                        AuthTabPill(
                            title = "Sign Up",
                            isSelected = selectedTab == 1,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedTab = 1
                                localValidationMsg = null
                                onClearError()
                            },
                            testTag = "tab_signup"
                        )
                        AuthTabPill(
                            title = "रीसेट OTP",
                            isSelected = selectedTab == 2,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedTab = 2
                                localValidationMsg = null
                                onClearError()
                                onOpenResetPassword(loginIdentifier.trim())
                            },
                            testTag = "tab_forgot_password"
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Error display (from VM or local)
                    val activeError = errorMessage ?: localValidationMsg
                    AnimatedVisibility(visible = activeError != null) {
                        if (activeError != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .testTag("auth_error_card"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "Error icon",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = activeError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    if (selectedTab == 0) {
                        // LOGIN FORM (Without OTP - Email ID OR Mobile Number + Password)
                        OutlinedTextField(
                            value = loginIdentifier,
                            onValueChange = {
                                loginIdentifier = it
                                onClearError()
                            },
                            label = { Text("Email ID or Mobile Number") },
                            placeholder = { Text("e.g. name@gmail.com or 9876543210") },
                            leadingIcon = {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Email or Mobile Number")
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_identifier_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = authGlassFieldColors()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = {
                                loginPassword = it
                                onClearError()
                            },
                            label = { Text("Password") },
                            placeholder = { Text("Enter password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Password icon")
                            },
                            trailingIcon = {
                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                    Icon(
                                        imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                onLogin(loginIdentifier, loginPassword)
                            }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = authGlassFieldColors()
                        )

                        // Direct link to Reset Password via OTP
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TerracottaPrimary.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clickable {
                                    selectedTab = 2
                                    onOpenResetPassword(loginIdentifier.trim())
                                }
                                .testTag("forgot_password_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = TerracottaPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "पासवर्ड भूल गए? (Forgot Password / OTP)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = TerracottaPrimary
                                    )
                                }
                                Text(
                                    text = "नया पासवर्ड बनाएं →",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = TerracottaPrimary
                                )
                            }
                        }

                        // Dedicated Wrong Password Alert Card with direct OTP reset action
                        AnimatedVisibility(visible = wrongPasswordDetected) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                                    .testTag("wrong_password_alert_card"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Wrong Password",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "⚠️ गलत पासवर्ड दर्ज किया गया!",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFFB91C1C)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "अपने मोबाइल नंबर या Gmail ID पर OTP प्राप्त करके आसानी से नया पासवर्ड बनाएं। (केवल गलत पासवर्ड होने पर ही OTP लगेगा)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF7F1D1D)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            val target = loginIdentifier.ifBlank { wrongPasswordIdentifier }
                                            selectedTab = 2
                                            onOpenResetPassword(target)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp)
                                            .testTag("wrong_password_reset_action_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "OTP से नया पासवर्ड बनाएं (Reset Password)",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                onLogin(loginIdentifier, loginPassword)
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    "Log In (Without OTP)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                text = "  या / OR  ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                val clientId = try {
                                    context.getString(com.example.R.string.default_web_client_id)
                                } catch (e: Exception) {
                                    localValidationMsg = "Google Sign-In configuration missing"
                                    return@OutlinedButton
                                }
                                val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                                val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()
                                coroutineScope.launch {
                                    try {
                                        val result = credentialManager.getCredential(context as Activity, request)
                                        val credential = result.credential
                                        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                            val idToken = googleIdTokenCredential.idToken
                                            val displayName = googleIdTokenCredential.displayName
                                            val email = googleIdTokenCredential.id
                                            onGoogleSignInSuccess(idToken, displayName, email)
                                        }
                                    } catch (e: GetCredentialCancellationException) {
                                        Log.w("Auth", "Google Sign-In flow cancelled: ${e.message}", e)
                                    } catch (e: Exception) {
                                        Log.e("Auth", "Google Sign-In failed", e)
                                        localValidationMsg = e.localizedMessage ?: "Google Sign-In failed"
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("google_sign_in_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF4285F4))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Sign in with Google",
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign in with Google",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick fill helper for convenience
                        OutlinedButton(
                            onClick = {
                                loginIdentifier = "sawaimadhopur.help@gmail.com"
                                loginPassword = "123456"
                                onClearError()
                                onLogin("sawaimadhopur.help@gmail.com", "123456")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("quick_login_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Quick 1-Tap Demo Login",
                                style = MaterialTheme.typography.labelLarge,
                                color = TerracottaPrimary
                            )
                        }
                    } else if (selectedTab == 1) {
                        // SIGNUP FORM (Without OTP: Name, Mobile No, Gmail ID, Password)
                        OutlinedTextField(
                            value = signupFullName,
                            onValueChange = {
                                signupFullName = it
                                localValidationMsg = null
                            },
                            label = { Text("Full Name (नाम)") },
                            placeholder = { Text("e.g. Ramesh Sharma") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "Full Name")
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = authGlassFieldColors()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = signupMobile,
                            onValueChange = {
                                signupMobile = it.filter { ch -> ch.isDigit() }
                                localValidationMsg = null
                            },
                            label = { Text("Mobile No (मोबाइल नंबर)") },
                            placeholder = { Text("e.g. 9876543210") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = "Mobile Number")
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_mobile_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = authGlassFieldColors()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = signupEmail,
                            onValueChange = {
                                signupEmail = it
                                localValidationMsg = null
                            },
                            label = { Text("Gmail / Email ID") },
                            placeholder = { Text("e.g. user@gmail.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = "Email")
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_email_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = authGlassFieldColors()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = signupPassword,
                            onValueChange = {
                                signupPassword = it
                                localValidationMsg = null
                            },
                            label = { Text("Password (पासवर्ड)") },
                            placeholder = { Text("Min 4 characters") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Password")
                            },
                            trailingIcon = {
                                IconButton(onClick = { signupPasswordVisible = !signupPasswordVisible }) {
                                    Icon(
                                        imageVector = if (signupPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (signupPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = authGlassFieldColors()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = signupConfirmPassword,
                            onValueChange = {
                                signupConfirmPassword = it
                                localValidationMsg = null
                            },
                            label = { Text("Confirm Password (पासवर्ड दोबारा लिखें)") },
                            placeholder = { Text("Re-enter password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Confirm Password")
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                            }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_confirm_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = authGlassFieldColors()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                when {
                                    signupFullName.trim().isEmpty() -> {
                                        localValidationMsg = "कृपया अपना पूरा नाम दर्ज करें"
                                    }
                                    signupMobile.trim().isEmpty() -> {
                                        localValidationMsg = "कृपया अपना मोबाइल नंबर दर्ज करें"
                                    }
                                    signupMobile.trim().length < 10 -> {
                                        localValidationMsg = "कृपया 10 अंकों का मान्य मोबाइल नंबर दर्ज करें"
                                    }
                                    signupEmail.trim().isEmpty() -> {
                                        localValidationMsg = "कृपया अपना Gmail ID / ईमेल पता दर्ज करें"
                                    }
                                    !signupEmail.contains("@") || !signupEmail.contains(".") -> {
                                        localValidationMsg = "कृपया मान्य Gmail ID / ईमेल दर्ज करें"
                                    }
                                    signupPassword.length < 6 -> {
                                        localValidationMsg = "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए"
                                    }
                                    signupPassword != signupConfirmPassword -> {
                                        localValidationMsg = "पासवर्ड मेल नहीं खा रहे हैं"
                                    }
                                    else -> {
                                        onSignup(
                                            signupFullName.trim(),
                                            signupEmail.trim(),
                                            signupMobile.trim(),
                                            signupPassword
                                        )
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("signup_submit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    "Create Account (No OTP Needed)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (selectedTab == 2) {
                        // FORGOT PASSWORD / OTP RESET TAB (No dialog needed, direct in-card full UI)
                        ForgotPasswordResetContent(
                            initialIdentifier = if (wrongPasswordIdentifier.isNotBlank()) wrongPasswordIdentifier else loginIdentifier,
                            otpSentCode = resetOtpSentCode,
                            isLoading = resetOtpLoading,
                            errorMessage = resetError,
                            successMessage = resetSuccess,
                            onRequestOtp = onRequestPasswordResetOtp,
                            onVerifyAndReset = onVerifyOtpAndResetPassword,
                            onBackToLogin = {
                                selectedTab = 0
                                onClearError()
                            },
                            onPasswordChanged = { newIdent, newPass ->
                                if (newIdent.isNotBlank()) loginIdentifier = newIdent
                                loginPassword = newPass
                                selectedTab = 0
                                onClearError()
                                onDismissWrongPasswordNotice()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Guest option (5-minute full exploration trial)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGuestContinue() }
                    .testTag("guest_continue_button")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "गेस्ट मोड (5 मिनट तक बिना लॉगिन सभी सेवाएं देखें) →",
                        color = Color(0xFFFCD34D),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "सुरक्षित लॉगिन डेटा हमेशा सेव रहेगा • बिना लॉगिन 5 मिनट तक सभी सुविधाएं बिना रुकावट उपयोग करें",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )
        }

        // Dialog for resetting wrong / forgotten password via OTP
        ResetPasswordDialog(
            isOpen = isResetPasswordOpen,
            initialIdentifier = if (wrongPasswordIdentifier.isNotBlank()) wrongPasswordIdentifier else loginIdentifier,
            otpSentCode = resetOtpSentCode,
            isLoading = resetOtpLoading,
            errorMessage = resetError,
            successMessage = resetSuccess,
            onRequestOtp = onRequestPasswordResetOtp,
            onVerifyAndReset = onVerifyOtpAndResetPassword,
            onDismiss = onCloseResetPassword,
            onPasswordChanged = { newIdent, newPass ->
                if (newIdent.isNotBlank()) loginIdentifier = newIdent
                loginPassword = newPass
                onClearError()
                onDismissWrongPasswordNotice()
            }
        )
    }
}

@Composable
fun ResetPasswordDialog(
    isOpen: Boolean,
    initialIdentifier: String,
    otpSentCode: String?,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onRequestOtp: (identifier: String) -> Unit,
    onVerifyAndReset: (identifier: String, otp: String, newPass: String, onSuccess: (newPass: String) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onPasswordChanged: (identifier: String, newPass: String) -> Unit
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("reset_password_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "बंद करें (Close)",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ForgotPasswordResetContent(
                    initialIdentifier = initialIdentifier,
                    otpSentCode = otpSentCode,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    successMessage = successMessage,
                    onRequestOtp = onRequestOtp,
                    onVerifyAndReset = onVerifyAndReset,
                    onBackToLogin = onDismiss,
                    onPasswordChanged = onPasswordChanged
                )
            }
        }
    }
}

@Composable
fun AuthTabPill(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = if (isSelected) TerracottaPrimary else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        border = if (isSelected) BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.5f)) else null
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun authGlassFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFF38BDF8),
    unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
    focusedLabelColor = Color(0xFF38BDF8),
    unfocusedLabelColor = Color(0xFF94A3B8),
    focusedLeadingIconColor = Color(0xFF38BDF8),
    unfocusedLeadingIconColor = Color(0xFF94A3B8),
    focusedTrailingIconColor = Color(0xFF38BDF8),
    unfocusedTrailingIconColor = Color(0xFF94A3B8),
    focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.65f),
    unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.45f),
    cursorColor = Color(0xFF38BDF8)
)

