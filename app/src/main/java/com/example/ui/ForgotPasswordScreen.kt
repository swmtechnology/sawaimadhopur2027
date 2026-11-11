package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.ui.theme.TerracottaPrimary
import kotlinx.coroutines.delay

/**
 * Visual 6-Digit OTP Box Grid displaying each digit in its own stylish cell
 */
@Composable
fun SixDigitOtpVisualGrid(
    otp: String,
    onCellClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCellClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 6) {
            val char = otp.getOrNull(i)?.toString() ?: ""
            val isCurrent = i == otp.length
            val isFilled = char.isNotEmpty()

            Box(
                modifier = Modifier
                    .size(46.dp, 52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            isFilled -> Color(0xFF2563EB).copy(alpha = 0.08f)
                            isCurrent -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                    .border(
                        width = if (isCurrent || isFilled) 2.dp else 1.dp,
                        color = when {
                            isFilled -> Color(0xFF2563EB)
                            isCurrent -> TerracottaPrimary
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        },
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char.ifEmpty { if (isCurrent) "|" else "•" },
                    fontSize = if (char.isNotEmpty()) 22.sp else 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = when {
                        char.isNotEmpty() -> Color(0xFF1E3A8A)
                        isCurrent -> TerracottaPrimary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    }
                )
            }
        }
    }
}

/**
 * Dedicated Forgot Password & OTP Reset View
 * Allows selecting Gmail ID OR 10-Digit Mobile Number, sending OTP, entering OTP in dedicated box,
 * and setting new password.
 */
@Composable
fun ForgotPasswordResetContent(
    initialIdentifier: String = "",
    otpSentCode: String?,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onRequestOtp: (identifier: String) -> Unit,
    onVerifyAndReset: (identifier: String, otp: String, newPass: String, onSuccess: (newPass: String) -> Unit) -> Unit,
    onBackToLogin: () -> Unit,
    onPasswordChanged: (identifier: String, newPass: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // 0 = Gmail ID, 1 = Mobile Number
    var selectedChannel by remember {
        mutableIntStateOf(if (initialIdentifier.contains("@")) 0 else 1)
    }
    var targetIdentifier by remember(initialIdentifier) { mutableStateOf(initialIdentifier) }
    var enteredOtp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var localValidation by remember { mutableStateOf<String?>(null) }

    // Resend countdown timer
    var resendCooldown by remember { mutableIntStateOf(0) }
    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000L)
            resendCooldown -= 1
        }
    }

    // Auto toast and notify when OTP code arrives
    LaunchedEffect(otpSentCode) {
        if (!otpSentCode.isNullOrBlank()) {
            resendCooldown = 45
            Toast.makeText(context, "🔔 आपका 6-अंकों का OTP कोड है: $otpSentCode", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("forgot_password_reset_content"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section Header
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = TerracottaPrimary.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(TerracottaPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "पासवर्ड रीसेट करें (Forgot Password)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Gmail ID या मोबाइल नंबर पर 6-अंकों का OTP प्राप्त करें",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Success Card if password was reset
        if (successMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .testTag("reset_success_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = successMessage,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF065F46)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onPasswordChanged(targetIdentifier.trim(), newPassword)
                            onBackToLogin()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("reset_success_login_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "नया पासवर्ड तैयार है → अब लॉगिन करें",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Active Error Display
        val activeError = errorMessage ?: localValidation
        if (activeError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("reset_error_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
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

        // 1. STEP 1: CHANNEL SELECTOR (Gmail ID OR Mobile Number)
        Text(
            text = "चरण 1: OTP कहाँ प्राप्त करना चाहते हैं?",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        TabRow(
            selectedTabIndex = selectedChannel,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = TerracottaPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .testTag("reset_channel_tabs")
        ) {
            Tab(
                selected = selectedChannel == 0,
                onClick = {
                    selectedChannel = 0
                    localValidation = null
                    if (!targetIdentifier.contains("@") && targetIdentifier.any { it.isDigit() }) {
                        targetIdentifier = ""
                    }
                },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "📧 Gmail ID",
                            fontWeight = if (selectedChannel == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_reset_channel_email")
            )
            Tab(
                selected = selectedChannel == 1,
                onClick = {
                    selectedChannel = 1
                    localValidation = null
                    if (targetIdentifier.contains("@")) {
                        targetIdentifier = ""
                    }
                },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "📱 मोबाइल नंबर",
                            fontWeight = if (selectedChannel == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_reset_channel_mobile")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Identifier TextField (Gmail or Mobile)
        OutlinedTextField(
            value = targetIdentifier,
            onValueChange = {
                targetIdentifier = it
                localValidation = null
                if (it.contains("@")) selectedChannel = 0
            },
            label = {
                Text(
                    if (selectedChannel == 0) "Gmail ID दर्ज करें (Enter Gmail) *" else "10 अंकों का मोबाइल नंबर दर्ज करें (Enter Mobile) *"
                )
            },
            placeholder = {
                Text(
                    if (selectedChannel == 0) "उदा. yourname@gmail.com" else "उदा. 9876543210"
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = if (selectedChannel == 0) Icons.Default.Email else Icons.Default.Phone,
                    contentDescription = null,
                    tint = TerracottaPrimary
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (selectedChannel == 0) KeyboardType.Email else KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_target_identifier_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TerracottaPrimary,
                focusedLabelColor = TerracottaPrimary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // SEND / RESEND OTP BUTTON
        Button(
            onClick = {
                focusManager.clearFocus()
                val clean = targetIdentifier.trim()
                if (clean.isBlank()) {
                    localValidation = if (selectedChannel == 0)
                        "कृपया अपनी Gmail ID दर्ज करें"
                    else
                        "कृपया अपना 10 अंकों का मोबाइल नंबर दर्ज करें"
                } else if (selectedChannel == 1 && clean.filter { it.isDigit() }.length < 10) {
                    localValidation = "कृपया पूरे 10 अंकों का मान्य मोबाइल नंबर दर्ज करें"
                } else {
                    localValidation = null
                    onRequestOtp(clean)
                }
            },
            enabled = !isLoading && resendCooldown == 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("btn_send_reset_otp"),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (isLoading && otpSentCode == null) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Icon(
                    imageVector = if (otpSentCode == null) Icons.Default.Send else Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        resendCooldown > 0 -> "पुनः भेजें ($resendCooldown सेकंड बाद)"
                        otpSentCode != null -> "OTP पुनः भेजें (Resend OTP Code)"
                        selectedChannel == 0 -> "Gmail पर OTP भेजें (Send OTP)"
                        else -> "मोबाइल पर OTP भेजें (Send OTP)"
                    },
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }

        // 2. INCOMING OTP NOTIFICATION BANNER (Instant Visibility)
        if (otpSentCode != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("incoming_otp_banner_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = BorderStroke(1.5.dp, Color(0xFF2563EB)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (selectedChannel == 0) Icons.Default.MarkEmailRead else Icons.Default.Sms,
                                contentDescription = null,
                                tint = Color(0xFF1D4ED8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedChannel == 0) "📩 Gmail इनबॉक्स संदेश:" else "💬 SMS संदेश प्राप्त हुआ:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E40AF)
                            )
                        }

                        // Auto-Fill Action
                        Button(
                            onClick = {
                                enteredOtp = otpSentCode
                                Toast.makeText(context, "OTP कोड स्वतः भर दिया गया!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.testTag("btn_autofill_otp")
                        ) {
                            Text("स्वतः भरें", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OTP कोड: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = otpSentCode,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF1E3A8A),
                            letterSpacing = 3.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(10 मिनट मान्य)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. STEP 2: DEDICATED OTP ENTER BOX (The User's core request: "otp likhne ka box")
        Text(
            text = "चरण 2: 6-अंकों का OTP कोड यहाँ दर्ज करें *",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        // Visual 6-Digit Grid
        SixDigitOtpVisualGrid(
            otp = enteredOtp,
            onCellClick = { /* Focus text field */ },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Numeric OTP Input Box
        OutlinedTextField(
            value = enteredOtp,
            onValueChange = {
                if (it.length <= 6) {
                    enteredOtp = it.filter { ch -> ch.isDigit() }
                    localValidation = null
                }
            },
            label = { Text("यहाँ 6-अंकों का OTP कोड लिखें (Enter OTP) *") },
            placeholder = { Text("उदा. 123456") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = Color(0xFF2563EB)
                )
            },
            trailingIcon = {
                if (otpSentCode != null && enteredOtp != otpSentCode) {
                    TextButton(onClick = { enteredOtp = otpSentCode }) {
                        Text(
                            text = "स्वतः भरें",
                            fontSize = 11.sp,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_otp_input_box"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2563EB),
                focusedLabelColor = Color(0xFF2563EB)
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. STEP 3: NEW PASSWORD & CONFIRM PASSWORD
        Text(
            text = "चरण 3: नया सुरक्षित पासवर्ड बनाएं *",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                localValidation = null
            },
            label = { Text("नया पासवर्ड (New Password) *") },
            placeholder = { Text("कम से कम 6 अक्षर") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                    Icon(
                        imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_new_password_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TerracottaPrimary,
                focusedLabelColor = TerracottaPrimary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                localValidation = null
            },
            label = { Text("नए पासवर्ड की पुनः पुष्टि करें (Confirm Password) *") },
            placeholder = { Text("Re-enter new password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_confirm_password_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TerracottaPrimary,
                focusedLabelColor = TerracottaPrimary
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 5. SAVE NEW PASSWORD BUTTON
        Button(
            onClick = {
                focusManager.clearFocus()
                val cleanId = targetIdentifier.trim()
                val cleanOtp = enteredOtp.trim()
                when {
                    cleanId.isBlank() -> {
                        localValidation = if (selectedChannel == 0)
                            "कृपया अपनी Gmail ID दर्ज करें"
                        else
                            "कृपया अपना मोबाइल नंबर दर्ज करें"
                    }
                    cleanOtp.length != 6 -> {
                        localValidation = if (otpSentCode == null)
                            "कृपया पहले ऊपर 'OTP भेजें' बटन दबाकर OTP प्राप्त करें"
                        else
                            "कृपया पूरे 6 अंकों का OTP कोड दर्ज करें"
                    }
                    newPassword.length < 6 -> {
                        localValidation = "नया पासवर्ड कम से कम 6 अक्षरों का होना चाहिए"
                    }
                    newPassword != confirmPassword -> {
                        localValidation = "दोनों पासवर्ड मेल नहीं खा रहे हैं। कृपया समान पासवर्ड दर्ज करें।"
                    }
                    else -> {
                        onVerifyAndReset(cleanId, cleanOtp, newPassword) { savedPass ->
                            onPasswordChanged(cleanId, savedPass)
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_save_new_password"),
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
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "नया पासवर्ड रीसेट व सेव करें (Reset Password)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Return to Login Tab
        OutlinedButton(
            onClick = onBackToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .testTag("btn_back_to_login"),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("← वापस लॉगिन पर जाएं (Back to Login)")
        }
    }
}
