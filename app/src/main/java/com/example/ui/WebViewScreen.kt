package com.example.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.chat.data.ChatRepository
import com.example.chat.ui.ChatAnyoneScreen
import com.example.data.SessionEntity
import com.example.data.UserProfileData
import com.example.data.UserProfileManager
import com.example.ui.theme.OfflineAmber
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.TerracottaPrimary
import java.net.URLEncoder

private const val TARGET_URL = "https://sawaimadhopurapk.blogspot.com/"
private const val PRIMARY_PHONE = "6367094588"
private const val SECONDARY_PHONE = "9649438733"
private const val CONTACT_EMAIL = "Sawaimadhopur.help@gmail.com"
private const val CONTACT_ADDRESS = "124, City Heart Hotel & Restaurant, Ranthambore Road, Near Ramsingh Hospital, Sawai Madhopur, Raj .322001"
private const val INSTAGRAM_URL = "https://www.instagram.com/swmnews24/"
private const val FACEBOOK_URL = "https://www.facebook.com/SWMNEWS247/"
private const val YOUTUBE_URL = "https://www.youtube.com/@SWMNEWS24"
private const val WHATSAPP_CHANNEL_URL = "https://chat.whatsapp.com/Gaj5vtiGdrDF3E1QhOH5xs?s=cl&p=a&mlu=4&ilr=4"
private const val RATING_FEEDBACK_URL = "https://sawaimadhopurapp.blogspot.com/p/feedback.html"
private const val REFER_EARN_URL = "https://sawaimadhopurapp.blogspot.com/p/feedback.html"
private const val SHARE_APP_URL = "https://sawaimadhopuraapk.blogspot.com/"
private const val APP_SHARE_MESSAGE = "सवाई माधोपुर ऑल-इन-वन ऐप डाउनलोड करें! रणथंभौर सफारी, होटल बुकिंग, सरकारी नौकरी, रिजल्ट, न्यूज़ व स्थानीय सेवाएं: https://sawaimadhopuraapk.blogspot.com/"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    session: SessionEntity?,
    isOnline: Boolean,
    onLogout: () -> Unit,
    onRetryNetwork: () -> Unit,
    onRequireLogin: () -> Unit = {},
    onGuestTimeout: () -> Unit = {},
    onUpdateProfile: (name: String, mobile: String, email: String, city: String, imageUri: Uri?) -> Unit = { _, _, _, _, _ -> },
    onViewSplashGuide: () -> Unit = {},
    appThemeMode: String = "SYSTEM",
    onThemeChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isGuestUser = session == null || !session.isLoggedIn || session.username == "guest"

    // 5-minute (300 seconds) unrestricted guest exploration limit with live countdown
    var guestRemainingSeconds by remember { mutableIntStateOf(300) }

    LaunchedEffect(isGuestUser) {
        if (isGuestUser) {
            guestRemainingSeconds = 300
            while (guestRemainingSeconds > 0) {
                kotlinx.coroutines.delay(1000L)
                guestRemainingSeconds--
            }
            onGuestTimeout()
        }
    }

    var showGuestLoginPromptDialog by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var webViewKey by remember { mutableIntStateOf(0) }
    var pageProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var hasPageLoadError by remember { mutableStateOf(false) }

    // Navigation Drawer & Modals State
    val userProfile by UserProfileManager.profileState.collectAsStateWithLifecycle()
    var isRightMenuOpen by remember { mutableStateOf(false) }
    var showChatAnyoneSheet by remember { mutableStateOf(false) }
    val chatRepository = remember { ChatRepository(context) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showBusinessListingDialog by remember { mutableStateOf(false) }
    var showContactUsDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showReferDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showImageManagerSheet by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showAboutAppDialog by remember { mutableStateOf(false) }
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }

    // Intercept system back button
    BackHandler(enabled = showChatAnyoneSheet || isRightMenuOpen || canGoBack) {
        if (showChatAnyoneSheet) {
            showChatAnyoneSheet = false
        } else if (isRightMenuOpen) {
            isRightMenuOpen = false
        } else if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        }
    }

    // Refresh when internet restored
    LaunchedEffect(isOnline) {
        if (isOnline && hasPageLoadError) {
            hasPageLoadError = false
            if (webViewInstance != null) {
                webViewInstance?.reload()
            } else {
                webViewKey++
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("webview_screen_root"),
        topBar = {
            Column {
                // Professional App Bar: Clean App Name & Emblem, No browser buttons, Right Menu
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = if (canGoBack) 0.dp else 4.dp)
                        ) {
                            // Royal Emblem Badge for Sawai Madhopur
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFDC2626),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "SM",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "Sawai Madhopur",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        letterSpacing = 0.2.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "सवाई माधोपुर ऑल-इन-वन पोर्टल",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color(0xFFDC2626),
                                    maxLines = 1,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (canGoBack) {
                            IconButton(
                                onClick = {
                                    if (webViewInstance?.canGoBack() == true) {
                                        webViewInstance?.goBack()
                                    }
                                },
                                modifier = Modifier.testTag("webview_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Navigate Back"
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick Chat Anyone button (WhatsApp Free Chat & Call)
                        IconButton(
                            onClick = { showChatAnyoneSheet = true },
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("topbar_chat_anyone_button")
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF25D366).copy(alpha = 0.12f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "Chat Anyone",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // User requested: "Profile ke icon ki jagh Right side menu bar lgaa do"
                        IconButton(
                            onClick = { isRightMenuOpen = true },
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .testTag("right_side_menu_button")
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.08f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Right Side Menu Bar",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Top Screen Linear Page Loading Indicator
                AnimatedVisibility(
                    visible = isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (pageProgress > 0.05f && pageProgress < 1.0f) {
                        LinearProgressIndicator(
                            progress = { pageProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.5.dp)
                                .testTag("webview_progress_bar"),
                            color = TerracottaPrimary,
                            trackColor = TerracottaPrimary.copy(alpha = 0.2f)
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.5.dp)
                                .testTag("webview_progress_bar_indeterminate"),
                            color = TerracottaPrimary,
                            trackColor = TerracottaPrimary.copy(alpha = 0.2f)
                        )
                    }
                }

                // Offline Notice Banner
                AnimatedVisibility(visible = !isOnline) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("webview_offline_banner"),
                        color = OfflineAmber
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = "Offline indicator",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "इंटरनेट बंद है • ऑफ़लाइन मोड",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = {
                                        onRetryNetwork()
                                        if (webViewInstance != null) {
                                            webViewInstance?.reload()
                                        } else {
                                            webViewKey++
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("banner_retry_button")
                                ) {
                                    Text(
                                        "रीट्राई",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = OfflineAmber
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Button(
                                    onClick = { showChatAnyoneSheet = true },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("banner_offline_sms_button")
                                ) {
                                    Text(
                                        "📶 बिना इंटरनेट SMS",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Android WebView with resilient renderer crash recovery and lifecycle management
            key(webViewKey) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("embedded_webview"),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                cacheMode = if (isOnline) {
                                    WebSettings.LOAD_DEFAULT
                                } else {
                                    WebSettings.LOAD_CACHE_ELSE_NETWORK
                                }
                                userAgentString = "$userAgentString SawaiMadhopurApp/1.0"
                            }

                            setLayerType(android.view.View.LAYER_TYPE_NONE, null)

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                    canGoBack = view?.canGoBack() == true
                                    canGoForward = view?.canGoForward() == true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    canGoBack = view?.canGoBack() == true
                                    canGoForward = view?.canGoForward() == true
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    if (request?.isForMainFrame == true) {
                                        hasPageLoadError = true
                                        isLoading = false
                                    }
                                }

                                // Critical handler to prevent app crash on renderer process termination (aw_browser_terminator)
                                override fun onRenderProcessGone(
                                    view: WebView?,
                                    detail: RenderProcessGoneDetail?
                                ): Boolean {
                                    val didCrash = detail?.didCrash() == true
                                    android.util.Log.e("WebViewScreen", "WebView renderer process gone (didCrash=$didCrash). Gracefully recovering.")
                                    try {
                                        (view?.parent as? ViewGroup)?.removeView(view)
                                        view?.destroy()
                                    } catch (e: Exception) {
                                        android.util.Log.w("WebViewScreen", "Error destroying crashed WebView: ${e.message}")
                                    }
                                    webViewInstance = null
                                    hasPageLoadError = true
                                    isLoading = false
                                    return true // Return true to indicate the app handled the crash, preventing app termination
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    return handleCustomUrl(
                                        context = ctx,
                                        url = url,
                                        webView = view,
                                        isGuest = isGuestUser,
                                        onRequireLogin = {
                                            showGuestLoginPromptDialog = true
                                        }
                                    )
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    super.onProgressChanged(view, newProgress)
                                    pageProgress = newProgress / 100f
                                    if (newProgress >= 100) {
                                        isLoading = false
                                    }
                                }
                            }

                            loadUrl(TARGET_URL)
                            webViewInstance = this
                        }
                    },
                    update = { webView ->
                        webView.settings.cacheMode = if (isOnline) {
                            WebSettings.LOAD_DEFAULT
                        } else {
                            WebSettings.LOAD_CACHE_ELSE_NETWORK
                        }
                    },
                    onRelease = { webView ->
                        try {
                            webView.stopLoading()
                            webView.removeAllViews()
                            webView.destroy()
                        } catch (e: Exception) {
                            android.util.Log.w("WebViewScreen", "Error cleaning up WebView in onRelease: ${e.message}")
                        }
                    }
                )
            }

            // Offline Error View - When offline, completely block web view and show clean error message with Swm Infotech contact
            if (!isOnline || hasPageLoadError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A))
                        .padding(20.dp)
                        .testTag("webview_offline_fullscreen"),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = OfflineAmber.copy(alpha = 0.15f),
                                modifier = Modifier.size(68.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.WifiOff,
                                        contentDescription = "No Internet",
                                        tint = OfflineAmber,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "इंटरनेट कनेक्शन नहीं है (No Network)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "सवाई माधोपुर पोर्टल के लिए इंटरनेट कनेक्शन आवश्यक है। कृपया अपना मोबाइल डेटा या वाई-फाई ऑन करें।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Contact Swm Infotech card inside offline view
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF0F172A),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "📞 Contact Swm Infotech",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF38BDF8)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "तकनीकी सहायता व जानकारी के लिए संपर्क करें:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFCBD5E1),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { dialPhoneNumber(context, PRIMARY_PHONE) },
                                            modifier = Modifier.weight(1f).height(40.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("कॉल करें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                sendWhatsAppMessage(
                                                    context,
                                                    PRIMARY_PHONE,
                                                    "नमस्ते Swm Infotech, मुझे सवाई माधोपुर पोर्टल से जुड़ने में सहायता चाहिए।"
                                                )
                                            },
                                            modifier = Modifier.weight(1f).height(40.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Action buttons: Reconnect & Offline Chat
                            Button(
                                onClick = {
                                    onRetryNetwork()
                                    hasPageLoadError = false
                                    if (webViewInstance != null) {
                                        webViewInstance?.reload()
                                    } else {
                                        webViewKey++
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("fullscreen_retry_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("कनेक्शन जांचें और दोबारा जोड़ें", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = { showChatAnyoneSheet = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("fullscreen_offline_chat_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA78BFA)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA78BFA))
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = "Offline Chat")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("बिना इंटरनेट चैट (WiFi/Bluetooth SMS)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // RIGHT SIDE NAVIGATION DRAWER / MENU OVERLAY
            RightSideNavigationDrawer(
                isOpen = isRightMenuOpen,
                session = session,
                userProfile = userProfile,
                appThemeMode = appThemeMode,
                onThemeChange = onThemeChange,
                onDismiss = { isRightMenuOpen = false },
                onChatAnyoneClick = {
                    isRightMenuOpen = false
                    showChatAnyoneSheet = true
                },
                onProfileClick = {
                    isRightMenuOpen = false
                    showProfileDialog = true
                },
                onBusinessListingClick = {
                    isRightMenuOpen = false
                    showBusinessListingDialog = true
                },
                onContactUsClick = {
                    isRightMenuOpen = false
                    showContactUsDialog = true
                },
                onClearCacheClick = {
                    isRightMenuOpen = false
                    try {
                        webViewInstance?.clearCache(true)
                        context.cacheDir.deleteRecursively()
                        Toast.makeText(context, "कैश सफलतापूर्वक साफ़ कर दिया गया!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "कैश साफ़ किया गया", Toast.LENGTH_SHORT).show()
                    }
                },
                onNotificationSettingsClick = {
                    isRightMenuOpen = false
                    showNotificationSettingsDialog = true
                },
                onPrivacyPolicyClick = {
                    isRightMenuOpen = false
                    showPrivacyPolicyDialog = true
                },
                onTermsClick = {
                    isRightMenuOpen = false
                    showTermsDialog = true
                },
                onAboutAppClick = {
                    isRightMenuOpen = false
                    showAboutAppDialog = true
                },
                onRatingClick = {
                    isRightMenuOpen = false
                    showRatingDialog = true
                },
                onReferClick = {
                    isRightMenuOpen = false
                    showReferDialog = true
                },
                onShareClick = {
                    isRightMenuOpen = false
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, APP_SHARE_MESSAGE)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Sawai Madhopur App Share"))
                },
                onImageManagerClick = {
                    isRightMenuOpen = false
                    showImageManagerSheet = true
                },
                onCallClick = {
                    isRightMenuOpen = false
                    dialPhoneNumber(context, PRIMARY_PHONE)
                },
                onWhatsAppClick = {
                    isRightMenuOpen = false
                    sendWhatsAppMessage(context, PRIMARY_PHONE, "नमस्ते, मुझे सवाई माधोपुर ऐप के बारे में सहायता चाहिए।")
                },
                onLogoutClick = {
                    isRightMenuOpen = false
                    if (isGuestUser) {
                        onRequireLogin()
                    } else {
                        showLogoutConfirmDialog = true
                    }
                }
            )
        }
    }

    // MODAL DIALOGS
    if (showProfileDialog) {
        ProfileDetailsDialog(
            session = session,
            userProfile = userProfile,
            onDismiss = { showProfileDialog = false },
            onUpdateProfile = onUpdateProfile
        )
    }

    if (showBusinessListingDialog) {
        BusinessListingDialog(
            onDismiss = { showBusinessListingDialog = false }
        )
    }

    if (showContactUsDialog) {
        ContactUsDialog(
            onDismiss = { showContactUsDialog = false }
        )
    }

    if (showRatingDialog) {
        RatingDialog(
            onDismiss = { showRatingDialog = false }
        )
    }

    if (showReferDialog) {
        ReferAndEarnDialog(
            onDismiss = { showReferDialog = false }
        )
    }

    if (showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyPolicyDialog = false }
        )
    }

    if (showTermsDialog) {
        TermsAndConditionsDialog(
            onDismiss = { showTermsDialog = false }
        )
    }

    if (showAboutAppDialog) {
        AboutAppDialog(
            onDismiss = { showAboutAppDialog = false }
        )
    }

    if (showNotificationSettingsDialog) {
        NotificationSettingsDialog(
            onDismiss = { showNotificationSettingsDialog = false }
        )
    }

    // MANDATORY 10-Digit Mobile Number Requirement for Logged-In / Google Sign-In users
    val isRegisteredUser = session != null && session.isLoggedIn && session.username != "guest"
    val needsMobileCompletion = isRegisteredUser && (session.mobile.filter { it.isDigit() }.length < 10)
    if (needsMobileCompletion) {
        CompleteProfileMobileDialog(
            currentName = session?.fullName?.ifBlank { userProfile.name } ?: "",
            currentMobile = session?.mobile ?: "",
            onSave = { updatedName, updatedMobile ->
                onUpdateProfile(
                    updatedName,
                    updatedMobile,
                    session?.email?.ifBlank { userProfile.email } ?: "",
                    userProfile.city,
                    null
                )
            }
        )
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(
                    text = "लॉगआउट करें?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "क्या आप वास्तव में अपने अकाउंट से लॉगआउट करना चाहते हैं?",
                    color = Color(0xFFCBD5E1)
                )
            },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        UserProfileManager.clearOnLogout(context)
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("लॉगआउट", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("रद्द करें", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    if (showGuestLoginPromptDialog) {
        AlertDialog(
            onDismissRequest = { showGuestLoginPromptDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Login Required",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "लॉगिन अनिवार्य है",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "गेस्ट यूज़र केवल होम पेज देख सकते हैं। किसी भी लिंक या सेवा को खोलने के लिए मोबाइल नंबर या Gmail ID से लॉगिन करना आवश्यक है।",
                        color = Color(0xFFCBD5E1),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "बिना OTP के तुरंत लॉगिन / नया अकाउंट बनाएं!",
                        color = Color(0xFFFCD34D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                Button(
                    onClick = {
                        showGuestLoginPromptDialog = false
                        onRequireLogin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("अभी लॉगिन करें (Log In)", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGuestLoginPromptDialog = false }) {
                    Text("होम पेज पर रहें", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    if (showImageManagerSheet) {
        ImageManagerSheet(
            onDismiss = { showImageManagerSheet = false },
            onPreviewSplashScreens = {
                showImageManagerSheet = false
                onViewSplashGuide()
            }
        )
    }

        if (showChatAnyoneSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0B141A))
            ) {
                ChatAnyoneScreen(
                    repository = chatRepository,
                    onDismiss = { showChatAnyoneSheet = false }
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.stopLoading()
            webViewInstance?.destroy()
        }
    }
}

/**
 * Slide-in Right Side Navigation Drawer Menu
 */
@Composable
fun RightSideNavigationDrawer(
    isOpen: Boolean,
    session: SessionEntity?,
    userProfile: UserProfileData,
    appThemeMode: String = "SYSTEM",
    onThemeChange: (String) -> Unit = {},
    onDismiss: () -> Unit,
    onChatAnyoneClick: () -> Unit,
    onProfileClick: () -> Unit,
    onBusinessListingClick: () -> Unit,
    onContactUsClick: () -> Unit,
    onClearCacheClick: () -> Unit = {},
    onNotificationSettingsClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onAboutAppClick: () -> Unit = {},
    onRatingClick: () -> Unit,
    onReferClick: () -> Unit,
    onShareClick: () -> Unit,
    onImageManagerClick: () -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                shadowElevation = 20.dp,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Prevent clicks from closing drawer
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // Header Bar (Title + Close X Button)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDC2626),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("SM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "सवाई माधोपुर मेन्यू",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Menu",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SINGLE CONSOLIDATED PROFILE SECTION (PROFILE ID & SIGNUP DETAILS)
                    val displayProfileId = userProfile.profileId.ifBlank {
                        if (session != null && session.userId > 0) "SWM-${10000 + session.userId}" else "SWM-10042"
                    }
                    val displayFullName = userProfile.name.ifBlank { session?.fullName ?: "सवाई माधोपुर यूज़र" }
                    val displayMobile = userProfile.phone.ifBlank { session?.mobile ?: "" }
                    val displayEmail = userProfile.email.ifBlank { session?.email ?: "" }

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProfileClick() }
                            .testTag("drawer_single_profile_section")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0F172A))
                                        .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (userProfile.imagePath != null) {
                                        AsyncImage(
                                            model = userProfile.imagePath,
                                            contentDescription = "Profile Photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = displayFullName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Profile ID Chip
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF0284C7).copy(alpha = 0.25f),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "प्रोफ़ाइल ID: $displayProfileId",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }

                                    val contactSub = listOfNotNull(
                                        displayMobile.takeIf { it.isNotBlank() },
                                        displayEmail.takeIf { it.isNotBlank() }
                                    ).joinToString(" • ")

                                    if (contactSub.isNotBlank()) {
                                        Text(
                                            text = contactSub,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(OnlineGreen)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "साइनअप विवरण सुरक्षित",
                                        color = Color(0xFF86EFAC),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "बदलें / एडिट करें ✏️",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // CORE FEATURES SECTION (Profile is uniquely at the top, no duplicate item here)
                    Text(
                        text = "मुख्य सेवाएं",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // 0. Chat Anyone (चैट एनीवन - WhatsApp Free Chat, Video Calling & Offline SMS)
                    DrawerMenuItemRow(
                        icon = Icons.Default.Chat,
                        title = "Chat Anyone (चैट एनीवन)",
                        subtitle = "फ्री चैट, वीडियो/ऑडियो कॉलिंग व बिना इंटरनेट SMS",
                        iconColor = Color(0xFF25D366),
                        badgeText = "FREE CHAT",
                        onClick = onChatAnyoneClick,
                        testTag = "menu_chat_anyone_item"
                    )

                    // 1. Business Listing
                    DrawerMenuItemRow(
                        icon = Icons.Default.Storefront,
                        title = "Business Listing (बिजनेस लिस्टिंग)",
                        subtitle = "होटल, सफारी, दुकान या सेवा लिस्ट करें",
                        iconColor = Color(0xFFFBBF24),
                        onClick = onBusinessListingClick,
                        testTag = "menu_business_listing_item"
                    )

                    // 3. Contact Us
                    DrawerMenuItemRow(
                        icon = Icons.Default.SupportAgent,
                        title = "Contact Us (संपर्क करें)",
                        subtitle = "हेल्पलाइन, पता व कस्टमर सपोर्ट",
                        iconColor = Color(0xFF4ADE80),
                        onClick = onContactUsClick,
                        testTag = "menu_contact_us_item"
                    )

                    // 4. Change Images & Posters Section (Direct user requirement)
                    DrawerMenuItemRow(
                        icon = Icons.Default.Image,
                        title = "वीडियो व इमेज बदलें (Media Manager)",
                        subtitle = "स्प्लैश वीडियो, 4 स्प्लैश स्क्रीन व बैनर बदलें",
                        iconColor = Color(0xFFA78BFA),
                        badgeText = "NEW",
                        onClick = onImageManagerClick,
                        testTag = "menu_image_manager_item"
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // FOLLOW SOCIAL MEDIA ICONS
                    Text(
                        text = "Follow Social Media (सोशल मीडिया)",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Instagram
                        SocialMediaIconButton(
                            label = "Instagram",
                            color = Color(0xFFE1306C),
                            iconChar = "📸",
                            onClick = {
                                openExternalUrl(context, INSTAGRAM_URL)
                            }
                        )

                        // Facebook
                        SocialMediaIconButton(
                            label = "Facebook",
                            color = Color(0xFF1877F2),
                            iconChar = "📘",
                            onClick = {
                                openExternalUrl(context, FACEBOOK_URL)
                            }
                        )

                        // WhatsApp Group / Channel
                        SocialMediaIconButton(
                            label = "WA Group",
                            color = Color(0xFF25D366),
                            iconChar = "💬",
                            onClick = {
                                openExternalUrl(context, WHATSAPP_CHANNEL_URL)
                            }
                        )

                        // YouTube
                        SocialMediaIconButton(
                            label = "YouTube",
                            color = Color(0xFFFF0000),
                            iconChar = "▶️",
                            onClick = {
                                openExternalUrl(context, YOUTUBE_URL)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // NEW BUTTONS: Rating, Refer & Earn, Share
                    Text(
                        text = "अन्य उपयोगी विकल्प",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Rating Button
                    DrawerMenuItemRow(
                        icon = Icons.Default.Star,
                        title = "Rating (ऐप को 5★ रेटिंग दें)",
                        subtitle = "प्ले स्टोर पर अपना अनुभव साझा करें",
                        iconColor = Color(0xFFFBBF24),
                        onClick = onRatingClick,
                        testTag = "menu_rating_button"
                    )

                    // Refer & Earn Button
                    DrawerMenuItemRow(
                        icon = Icons.Default.CardGiftcard,
                        title = "Refer & Earn (रेफर और कमाएं)",
                        subtitle = "दोस्तों को जोड़ें और आकर्षक लाभ पाएं",
                        iconColor = Color(0xFFF43F5E),
                        onClick = onReferClick,
                        testTag = "menu_refer_earn_button"
                    )

                    // Share Button
                    DrawerMenuItemRow(
                        icon = Icons.Default.Share,
                        title = "Share (ऐप शेयर करें)",
                        subtitle = "मित्रों व परिवार को सवाई माधोपुर ऐप भेजें",
                        iconColor = Color(0xFF38BDF8),
                        onClick = onShareClick,
                        testTag = "menu_share_button"
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // APP THEME SELECTOR SECTION
                    Text(
                        text = "थीम व डिस्प्ले (App Theme)",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ThemeOptionChip(
                            label = "सिस्टम",
                            icon = Icons.Default.Brightness6,
                            isSelected = appThemeMode == "SYSTEM",
                            modifier = Modifier.weight(1f),
                            onClick = { onThemeChange("SYSTEM") },
                            testTag = "theme_system_chip"
                        )
                        ThemeOptionChip(
                            label = "लाइट",
                            icon = Icons.Default.LightMode,
                            isSelected = appThemeMode == "LIGHT",
                            modifier = Modifier.weight(1f),
                            onClick = { onThemeChange("LIGHT") },
                            testTag = "theme_light_chip"
                        )
                        ThemeOptionChip(
                            label = "डार्क",
                            icon = Icons.Default.DarkMode,
                            isSelected = appThemeMode == "DARK",
                            modifier = Modifier.weight(1f),
                            onClick = { onThemeChange("DARK") },
                            testTag = "theme_dark_chip"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // APP SETTINGS & LEGAL SECTION (User specification)
                    Text(
                        text = "ऐप सेटिंग्स व नीतियां (Settings & Legal)",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // 1. Clear Cache
                    DrawerMenuItemRow(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear Cache (कैश साफ़ करें)",
                        subtitle = "स्टोरेज खाली करें व ऐप स्मूथ चलाएं",
                        iconColor = Color(0xFFFB7185),
                        onClick = onClearCacheClick,
                        testTag = "menu_clear_cache_item"
                    )

                    // 2. Notification Settings
                    DrawerMenuItemRow(
                        icon = Icons.Default.Notifications,
                        title = "Notification Settings (नोटिफ़िकेशन)",
                        subtitle = "अलर्ट व ताज़ा अपडेट्स प्राथमिकता",
                        iconColor = Color(0xFF38BDF8),
                        onClick = onNotificationSettingsClick,
                        testTag = "menu_notifications_item"
                    )

                    // 3. Privacy Policy
                    DrawerMenuItemRow(
                        icon = Icons.Default.Security,
                        title = "Privacy Policy (गोपनीयता नीति)",
                        subtitle = "यूज़र डेटा व सुरक्षा नियम देखें",
                        iconColor = Color(0xFF34D399),
                        onClick = onPrivacyPolicyClick,
                        testTag = "menu_privacy_policy_item"
                    )

                    // 4. Terms and Conditions
                    DrawerMenuItemRow(
                        icon = Icons.Default.Description,
                        title = "Terms & Conditions (नियम एवं शर्तें)",
                        subtitle = "ऐप उपयोग की शर्तें व दिशानिर्देश",
                        iconColor = Color(0xFFFBBF24),
                        onClick = onTermsClick,
                        testTag = "menu_terms_conditions_item"
                    )

                    // 5. About App
                    DrawerMenuItemRow(
                        icon = Icons.Default.Info,
                        title = "About App (सवाई माधोपुर ऐप के बारे में)",
                        subtitle = "ऐप विवरण, उद्देश्य व प्रकाशक जानकारी",
                        iconColor = Color(0xFFA78BFA),
                        onClick = onAboutAppClick,
                        testTag = "menu_about_app_item"
                    )

                    // App Version Badge
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "सवाई माधोपुर ऑल-इन-वन",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "v1.0.0 (Production)",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // NICHE: Call Button, WhatsApp Button, and Text Logout Button
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Call Button
                        Button(
                            onClick = onCallClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("drawer_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "कॉल करें (+91 $PRIMARY_PHONE)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // WhatsApp Button
                        Button(
                            onClick = onWhatsAppClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("drawer_whatsapp_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WhatsApp (+91 $PRIMARY_PHONE)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val isGuest = session == null || !session.isLoggedIn || session.username == "guest"
                        if (isGuest) {
                            Button(
                                onClick = onLogoutClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("drawer_login_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "लॉगिन करें (Log In / Sign Up)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            // Text Log out Button
                            TextButton(
                                onClick = onLogoutClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("drawer_text_logout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "लॉगआउट (Log Out)",
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single Row inside Drawer
 */
@Composable
fun DrawerMenuItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    badgeText: String? = null,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = Color(0xFF1E293B).copy(alpha = 0.7f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF8B5CF6)
                        ) {
                            Text(
                                text = badgeText,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * Social media pill button
 */
@Composable
fun SocialMediaIconButton(
    label: String,
    color: Color,
    iconChar: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.18f))
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = iconChar, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Unified User Profile Dialog:
 * Shows Profile ID, Name, Mobile, Gmail, City, and Photo.
 * Auto-populated from signup and allows full editing and persistence.
 */
@Composable
fun ProfileDetailsDialog(
    session: SessionEntity?,
    userProfile: UserProfileData,
    onDismiss: () -> Unit,
    onUpdateProfile: (name: String, mobile: String, email: String, city: String, imageUri: Uri?) -> Unit = { _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val currentProfileId = remember(userProfile.profileId, session) {
        userProfile.profileId.ifBlank {
            if (session != null && session.userId > 0) "SWM-${10000 + session.userId}" else "SWM-10042"
        }
    }

    var name by remember { mutableStateOf(userProfile.name.ifBlank { session?.fullName ?: "" }) }
    var phone by remember { mutableStateOf(userProfile.phone.ifBlank { session?.mobile ?: "" }) }
    var email by remember { mutableStateOf(userProfile.email.ifBlank { session?.email ?: "" }) }
    var city by remember { mutableStateOf(userProfile.city.ifBlank { "सवाई माधोपुर" }) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentImagePath by remember { mutableStateOf(userProfile.imagePath) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "प्रोफ़ाइल आईडी व विवरण",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Profile ID Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile ID",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "आपकी प्रोफ़ाइल आईडी (Unique ID)",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = currentProfileId,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Profile ID", currentProfileId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "ID कॉपी की गई: $currentProfileId", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy ID",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Profile Image with change button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A))
                        .border(2.dp, Color(0xFF38BDF8), CircleShape)
                        .clickable {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (currentImagePath != null) {
                        AsyncImage(
                            model = currentImagePath,
                            contentDescription = "Saved Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "प्रोफ़ाइल फ़ोटो लगाएं / बदलें",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input fields
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("आपका नाम (Full Name) *", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("मोबाइल नंबर (Mobile No) *", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Gmail ID / ईमेल (Email Address)", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("शहर का नाम (City Name)", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Persistence notice card
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF16A34A).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF4ADE80),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "साइन अप करते समय बनाई गई जानकारी (नाम, मोबाइल, Gmail ID) आपकी प्रोफाइल में सुरक्षित रूप से सेव है। आप जब चाहें इसे यहाँ से बदल सकते हैं।",
                            color = Color(0xFF86EFAC),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save button
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "कृपया अपना नाम दर्ज करें", Toast.LENGTH_SHORT).show()
                        } else {
                            UserProfileManager.saveProfile(
                                context = context,
                                name = name,
                                phone = phone,
                                email = email,
                                city = city.ifBlank { "सवाई माधोपुर" },
                                newImageUri = selectedImageUri
                            )
                            onUpdateProfile(name, phone, email, city.ifBlank { "सवाई माधोपुर" }, selectedImageUri)
                            Toast.makeText(context, "प्रोफ़ाइल सफलतापूर्वक सुरक्षित हो गई!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("प्रोफ़ाइल अपडेट व सेव करें", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 12.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

/**
 * Business Listing Submission Dialog (Direct WhatsApp Sending)
 */
@Composable
fun BusinessListingDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var businessName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("होटल व स्टे") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val categories = listOf("होटल व स्टे", "रेस्टोरेंट / कैफे", "सफारी जिप्सी / टूर", "दुकान व शोरूम", "डॉक्टर / अस्पताल", "अन्य सेवा")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "बिजनेस लिस्टिंग फॉर्म",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "फॉर्म भरते ही विवरण सीधे WhatsApp पर भेजा जाएगा ताकि आपकी लिस्टिंग तुरंत सक्रिय हो सके।",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("बिजनेस / दुकान / होटल का नाम *", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category chips selector
                Text(text = "श्रेणी चुनें:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFFBBF24) else Color(0xFF0F172A),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("मालिक / संचालक का नाम", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("मोबाइल / WhatsApp नंबर *", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("स्थान / पता (सवाई माधोपुर)", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("सुविधाएं, उत्पाद या विशेष ऑफर", fontSize = 12.sp) },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Direct WhatsApp Submit Button
                Button(
                    onClick = {
                        if (businessName.isBlank() || phone.isBlank()) {
                            Toast.makeText(context, "कृपया बिजनेस का नाम व मोबाइल नंबर अवश्य भरें", Toast.LENGTH_SHORT).show()
                        } else {
                            val waMessage = buildString {
                                appendLine("📢 *सवाई माधोपुर ऐप - नई बिजनेस लिस्टिंग आवेदन*")
                                appendLine("━━━━━━━━━━━━━━━━━━━")
                                appendLine("🏪 *बिजनेस का नाम:* $businessName")
                                appendLine("🏷️ *श्रेणी:* $selectedCategory")
                                if (ownerName.isNotBlank()) appendLine("👤 *मालिक/संचालक:* $ownerName")
                                appendLine("📞 *मोबाइल/WhatsApp:* $phone")
                                if (address.isNotBlank()) appendLine("📍 *स्थान/पता:* $address")
                                if (description.isNotBlank()) appendLine("📝 *सुविधाएं/ऑफर:* $description")
                                appendLine("━━━━━━━━━━━━━━━━━━━")
                                appendLine("कृपया इस बिजनेस को सवाई माधोपुर पोर्टल पर लिस्ट करें।")
                            }
                            sendWhatsAppMessage(context, PRIMARY_PHONE, waMessage)
                            Toast.makeText(context, "WhatsApp पर विवरण भेजा जा रहा है...", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WhatsApp पर सीधे भेजें",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * Contact Us Dialog
 */
@Composable
fun ContactUsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint = Color(0xFF4ADE80),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Contact Us (संपर्क करें)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                ContactDetailCard(
                    icon = Icons.Default.Phone,
                    title = "हेल्पलाइन नंबर 1 (Call)",
                    value = "+91 $PRIMARY_PHONE",
                    onClick = {
                        dialPhoneNumber(context, PRIMARY_PHONE)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ContactDetailCard(
                    icon = Icons.Default.Phone,
                    title = "हेल्पलाइन नंबर 2 (Call)",
                    value = "+91 $SECONDARY_PHONE",
                    onClick = {
                        dialPhoneNumber(context, SECONDARY_PHONE)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ContactDetailCard(
                    icon = Icons.Default.Chat,
                    title = "व्हाट्सएप नंबर (WhatsApp)",
                    value = "+91 $PRIMARY_PHONE",
                    onClick = {
                        sendWhatsAppMessage(context, PRIMARY_PHONE, "नमस्ते, सवाई माधोपुर हेल्प डेस्क।")
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ContactDetailCard(
                    icon = Icons.Default.Email,
                    title = "आधिकारिक ईमेल (Mail)",
                    value = CONTACT_EMAIL,
                    onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$CONTACT_EMAIL")))
                        } catch (_: Exception) {}
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ContactDetailCard(
                    icon = Icons.Default.LocationOn,
                    title = "कार्यालय पता (Address)",
                    value = CONTACT_ADDRESS,
                    onClick = null
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            dialPhoneNumber(context, PRIMARY_PHONE)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("कॉल करें", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            sendWhatsAppMessage(context, PRIMARY_PHONE, "नमस्ते, मुझे सहायता चाहिए।")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ContactDetailCard(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)?
) {
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, color = Color(0xFF94A3B8), fontSize = 11.sp)
                Text(text = value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Rating Dialog
 */
@Composable
fun RatingDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedStars by remember { mutableIntStateOf(5) }
    var submitted by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ऐप को रेटिंग दें (Rating & Feedback)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "सवाई माधोपुर ऑल-इन-वन ऐप पर आपका अनुभव कैसा रहा?",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Star row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { selectedStars = i },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star $i",
                                tint = if (i <= selectedStars) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (submitted) {
                    Surface(
                        color = Color(0xFF16A34A).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "आपकी $selectedStars★ रेटिंग दर्ज हो गई है! धन्यवाद।",
                            color = Color(0xFF86EFAC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        submitted = true
                        Toast.makeText(context, "धन्यवाद! आपकी रेटिंग दर्ज हो गई।", Toast.LENGTH_SHORT).show()
                        openExternalUrl(context, RATING_FEEDBACK_URL)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("रेटिंग व फीडबैक फॉर्म खोलें", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("बंद करें", color = Color.White, fontWeight = FontWeight.Normal)
                }
            }
        }
    }
}

/**
 * Refer & Earn Dialog
 */
@Composable
fun ReferAndEarnDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val referralCode = "SAWAI2024"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF43F5E).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Refer & Earn (रेफर और कमाएं)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                Text(
                    text = "अपने दोस्तों व रिश्तेदारों को ऐप रेफर करें और होटल, सफारी व सेवाओं में विशेष डिस्काउंट व कैशबैक प्राप्त करें!",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Referral Code Box
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "आपका रेफरल कोड", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(
                                text = referralCode,
                                color = Color(0xFFFEF08A),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                letterSpacing = 2.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Referral Code", referralCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "रेफरल कोड कॉपी हो गया!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Code",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rewards page link button
                OutlinedButton(
                    onClick = {
                        openExternalUrl(context, REFER_EARN_URL)
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("रेफरल रिवार्ड्स व फीडबैक नियम देखें", color = Color(0xFFFF94A8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val shareText = "सवाई माधोपुर ऐप डाउनलोड करें और मेरा रेफरल कोड $referralCode उपयोग करें: $SHARE_APP_URL"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Referral"))
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("दोस्तों को शेयर करें", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFF38BDF8),
    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
    focusedLabelColor = Color(0xFF38BDF8),
    unfocusedLabelColor = Color(0xFF94A3B8)
)

private fun openExternalUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {}
}

private fun dialPhoneNumber(context: Context, phoneNumber: String) {
    try {
        val cleanNumber = phoneNumber.replace("+", "").replace("-", "").replace(" ", "").trim()
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber"))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "कॉल करने में असमर्थ", Toast.LENGTH_SHORT).show()
    }
}

private fun sendWhatsAppMessage(context: Context, phoneNumber: String, message: String = "") {
    try {
        val cleanNumber = phoneNumber.replace("+", "").replace("-", "").replace(" ", "").trim()
        val formattedNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
        val url = if (message.isNotBlank()) {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            "https://wa.me/$formattedNumber?text=$encodedMessage"
        } else {
            "https://wa.me/$formattedNumber"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "WhatsApp खोलने में असमर्थ", Toast.LENGTH_SHORT).show()
    }
}

private fun isHomePageUrl(url: String): Boolean {
    val clean = url.trim().lowercase().removeSuffix("/")
    return clean == "https://sawaimadhopurapk.blogspot.com" ||
            clean == "http://sawaimadhopurapk.blogspot.com" ||
            clean == "https://sawaimadhopurapk.blogspot.com/?m=1" ||
            clean == "http://sawaimadhopurapk.blogspot.com/?m=1" ||
            clean == "https://sawaimadhopurapk.blogspot.com/#" ||
            clean == "about:blank"
}

private fun handleCustomUrl(
    context: Context,
    url: String,
    webView: WebView?,
    isGuest: Boolean = false,
    onRequireLogin: () -> Unit = {}
): Boolean {
    val uri = Uri.parse(url)
    val scheme = uri.scheme?.lowercase()

    // During the 5-minute trial, guests can freely navigate all subpages and features without interruption

    return when {
        scheme == "tel" || scheme == "mailto" || scheme == "sms" || scheme == "whatsapp" -> {
            try {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            } catch (_: Exception) {}
            true
        }
        url.startsWith("intent:") -> {
            try {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                context.startActivity(intent)
            } catch (_: Exception) {}
            true
        }
        url.contains("sawaimadhopurapk.blogspot.com") || url.contains("blogspot.com") -> {
            false
        }
        else -> {
            false
        }
    }
}

@Composable
fun ThemeOptionChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = if (isSelected) Color(0xFF0284C7) else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) BorderStroke(1.dp, Color(0xFF38BDF8)) else null
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "गोपनीयता नीति (Privacy Policy)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "सवाई माधोपुर ऑल-इन-वन ऐप गोपनीयता नीति",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "अंतिम अपडेट: 2026 • सवाई माधोपुर डिजिटल पोर्टल",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    LegalSectionBlock(
                        title = "1. सूचना संग्रह (Information We Collect)",
                        body = "हम केवल आवश्यक खाता प्रमाणीकरण जानकारी जैसे आपका नाम, वैध 10-अंकों का मोबाइल नंबर और Google/Gmail ID एकत्र करते हैं। इसका उपयोग स्थानीय सेवाओं, हेल्पलाइन व सुरक्षित सत्र बनाए रखने हेतु होता है।"
                    )

                    LegalSectionBlock(
                        title = "2. डिवाइस अनुमतियां (Device Permissions)",
                        body = "ऐप केवल अनिवार्य कार्यप्रणाली के लिए अनुमतियां मांगता है:\n• इंटरनेट व नेटवर्क स्थिति: वेब पोर्टल व ताज़ा अपडेट्स हेतु।\n• सूचनाएं (Notifications): महत्वपूर्ण स्थानीय अलर्ट व अपडेट्स हेतु।\n• कैमरा/गैलरी: केवल प्रोफ़ाइल फोटो या बिजनेस लिस्टिंग पोस्टर अपलोड के लिए।"
                    )

                    LegalSectionBlock(
                        title = "3. डेटा सुरक्षा व गोपनीयता (Data Security)",
                        body = "आपका व्यक्तिगत डेटा कभी भी किसी तीसरे पक्ष को बेचा या व्यावसायिक रूप से साझा नहीं किया जाता। सभी डेटा ट्रांसमिशन SSL/HTTPS प्रोटोकॉल द्वारा एन्क्रिप्टेड होते हैं।"
                    )

                    LegalSectionBlock(
                        title = "4. बाहरी लिंक्स (External Links)",
                        body = "ऐप में सरकारी भर्ती, रणथंभौर सफारी, होटल बुकिंग या स्थानीय समाचार के आधिकारिक लिंक हो सकते हैं। बाहरी साइटों की अपनी नीतियां होती हैं।"
                    )

                    LegalSectionBlock(
                        title = "5. संपर्क सूत्र (Contact Us)",
                        body = "यदि आपके पास गोपनीयता नीति से संबंधित कोई प्रश्न है, तो हमसे संपर्क करें:\nईमेल: Sawaimadhopur.help@gmail.com\nहेल्पलाइन: +91 6367094588"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("स्वीकार है व बंद करें (Close)", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
            }
        }
    }
}

@Composable
fun TermsAndConditionsDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "नियम एवं शर्तें (Terms)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    LegalSectionBlock(
                        title = "1. सेवा की स्वीकृति (Acceptance of Terms)",
                        body = "सवाई माधोपुर ऑल-इन-वन ऐप का उपयोग करके आप इन नियमों और शर्तों का पूर्णतः पालन करने के लिए सहमत होते हैं।"
                    )

                    LegalSectionBlock(
                        title = "2. उपयुक्त उपयोग (Permitted Use)",
                        body = "यह ऐप सवाई माधोपुर वासियों और पर्यटकों को स्थानीय सूचनाएं, रणथंभौर सफारी दिशा-निर्देश, होटल, दुकान लिस्टिंग और आपातकालीन सहायता प्रदान करने के लिए है। किसी भी गैर-कानूनी गतिविधि के लिए इसका उपयोग वर्जित है।"
                    )

                    LegalSectionBlock(
                        title = "3. बिजनेस लिस्टिंग नियम (Business Listings)",
                        body = "उपयोगकर्ताओं द्वारा पोस्ट की गई व्यावसायिक जानकारी सत्य और सटीक होनी चाहिए। भ्रामक या अनुचित सामग्री पाए जाने पर लिस्टिंग तुरंत हटा दी जाएगी।"
                    )

                    LegalSectionBlock(
                        title = "4. बौद्धिक संपदा (Intellectual Property)",
                        body = "ऐप का डिज़ाइन, लेआउट, लोगो और स्थानीय संकलित डेटा सवाई माधोपुर डिजिटल मंच की संपत्ति हैं।"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("स्वीकार है (Accept & Close)", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
            }
        }
    }
}

@Composable
fun AboutAppDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDC2626),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("SM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "सवाई माधोपुर ऐप के बारे में",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "सवाई माधोपुर ऑल-इन-वन स्मार्ट सिटी व टूरिज्म ऐप",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "वर्जन: 1.0.0 (Production Build • Release 2026)",
                    color = Color(0xFF86EFAC),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "हमारा उद्देश्य सवाई माधोपुर जिले के नागरिकों एवं रणथंभौर आने वाले पर्यटकों को एक ही मंच पर संपूर्ण सुविधाएं उपलब्ध कराना है — जिसमें सफारी बुकिंग, होटल, स्थानीय दुकान व व्यापार लिस्टिंग, आपातकालीन नंबर, नौकरियां व ताज़ा समाचार शामिल हैं।",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("📍 मुख्य कार्यालय व संपर्क:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("124, City Heart Hotel & Restaurant, Ranthambore Road, Near Ramsingh Hospital, Sawai Madhopur, Raj. 322001", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("📞 हेल्पलाइन: +91 6367094588 / 9649438733", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("✉️ ईमेल: Sawaimadhopur.help@gmail.com", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("बंद करें (Close)", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
            }
        }
    }
}

@Composable
fun NotificationSettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var isNewsEnabled by remember { mutableStateOf(true) }
    var isSafariAlertsEnabled by remember { mutableStateOf(true) }
    var isBusinessAlertsEnabled by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "नोटिफ़िकेशन सेटिंग्स",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Text(
                        text = "अपनी प्राथमिकताओं के अनुसार अलर्ट्स चालू या बंद करें:",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    NotificationToggleRow(
                        title = "सवाई माधोपुर ताज़ा समाचार व अपडेट्स",
                        subtitle = "दैनिक मुख्य समाचार, मौसम व स्थानीय घटनाएं",
                        isChecked = isNewsEnabled,
                        onCheckedChange = { isNewsEnabled = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    NotificationToggleRow(
                        title = "रणथंभौर सफारी व पर्यटन अलर्ट",
                        subtitle = "ज़ोन बुकिंग, सफारी स्लॉट व गाइड सूचनाएं",
                        isChecked = isSafariAlertsEnabled,
                        onCheckedChange = { isSafariAlertsEnabled = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    NotificationToggleRow(
                        title = "बिजनेस व रोजगार नोटिफ़िकेशन",
                        subtitle = "नई दुकान लिस्टिंग, डिस्काउंट व सरकारी जॉब्स",
                        isChecked = isBusinessAlertsEnabled,
                        onCheckedChange = { isBusinessAlertsEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        Toast.makeText(context, "नोटिफ़िकेशन प्राथमिकताएं सहेज ली गईं!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("प्राथमिकताएं सेव करें (Save)", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
            }
        }
    }
}

@Composable
fun NotificationToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!isChecked) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            androidx.compose.material3.Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun CompleteProfileMobileDialog(
    currentName: String,
    currentMobile: String,
    onSave: (fullName: String, mobile: String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    var mobileInput by remember { mutableStateOf(currentMobile.filter { it.isDigit() }) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = {}, // Un-dismissible until valid 10-digit mobile is provided
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "मोबाइल नंबर दर्ज करें",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "सवाई माधोपुर ऐप की संपूर्ण सेवाओं और सुरक्षा के लिए 10 अंकों का वैध मोबाइल नंबर अनिवार्य है।",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFCA5A5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("पूरा नाम (Full Name)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("mandatory_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = dialogTextFieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = mobileInput,
                    onValueChange = {
                        val digits = it.filter { ch -> ch.isDigit() }
                        if (digits.length <= 10) {
                            mobileInput = digits
                            errorMessage = null
                        }
                    },
                    label = { Text("10 अंकों का मोबाइल नंबर") },
                    placeholder = { Text("उदा. 9876543210") },
                    leadingIcon = {
                        Text("+91 ", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("mandatory_mobile_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = dialogTextFieldColors()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val trimmedName = nameInput.trim()
                        val trimmedPhone = mobileInput.trim()
                        if (trimmedName.isBlank()) {
                            errorMessage = "कृपया अपना पूरा नाम दर्ज करें"
                        } else if (trimmedPhone.length != 10) {
                            errorMessage = "कृपया 10 अंकों का वैध मोबाइल नंबर दर्ज करें"
                        } else if (!trimmedPhone.startsWith("6") && !trimmedPhone.startsWith("7") &&
                            !trimmedPhone.startsWith("8") && !trimmedPhone.startsWith("9")) {
                            errorMessage = "मोबाइल नंबर 6, 7, 8 या 9 से शुरू होना चाहिए"
                        } else {
                            onSave(trimmedName, trimmedPhone)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("mandatory_mobile_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "सुरक्षित सहेजें व आगे बढ़ें →",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun LegalSectionBlock(title: String, body: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = body, color = Color(0xFFCBD5E1), fontSize = 12.sp, lineHeight = 18.sp)
    }
}

