package com.example.ui

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.theme.OfflineAmber
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RoyalAmberSecondary
import com.example.ui.theme.TerracottaPrimary
import kotlinx.coroutines.delay
import java.io.File

/**
 * Splash Screen that plays the authentic Ranthambore Tiger Safari video.
 * In accordance with user request, the video plays through completely
 * ("isko dekhne ke bad hi open hoo") and transitions directly to the main portal.
 */
@Composable
fun SplashScreen(
    isOnline: Boolean,
    onProceedToMain: () -> Unit = {},
    onViewPostersGuide: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isVideoPrepared by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    val totalDurationMs = 4320 // Exact duration of user's video (4.32 seconds)
    var videoProgress by remember { mutableFloatStateOf(0f) }
    var hasCompleted by remember { mutableStateOf(false) }

    // Fallback safety timeout so app never hangs if device cannot decode video
    LaunchedEffect(Unit) {
        delay(7000)
        if (!hasCompleted) {
            hasCompleted = true
            onProceedToMain()
        }
    }

    // Real-time progress updater
    LaunchedEffect(isVideoPrepared, isPlaying) {
        while (isPlaying && isVideoPrepared && !hasCompleted) {
            videoViewRef?.let { vv ->
                try {
                    val pos = vv.currentPosition
                    if (pos > 0) {
                        currentPositionMs = pos
                        videoProgress = (pos.toFloat() / totalDurationMs).coerceIn(0f, 1f)
                    }
                } catch (_: Exception) {}
            }
            delay(80)
        }
    }

    // Dynamic narration subtitles corresponding to the original video voiceover
    val (sceneHindi, sceneEnglish) = remember(currentPositionMs) {
        when {
            currentPositionMs < 2000 -> Pair(
                "रणथंभौर टाइगर सफारी का लुत्फ़ उठाइए...",
                "Experience Ranthambhore Tiger Safari"
            )
            currentPositionMs < 3500 -> Pair(
                "देखो, वहां टाइगर है! 🐅",
                "Look, Royal Bengal Tiger Spotted Ahead!"
            )
            else -> Pair(
                "यहां 1 से 10 ज़ोन्स में सफारी की जाती है! आप भी जरूर आइए",
                "Safari Across Zones 1-10 • Welcome to Sawai Madhopur"
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("splash_screen_root")
    ) {
        // Fullscreen Android VideoView for user's video playback
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("splash_video_view"),
            factory = { ctx ->
                VideoView(ctx).apply {
                    videoViewRef = this

                    // Determine video URI with priority:
                    // 1. Custom updated video in app files directory ("splash_safari_video.mp4")
                    // 2. User uploaded "videoplayback" in res/raw/
                    // 3. Bundled "splash_safari_video" in res/raw/
                    val customVideo = File(ctx.filesDir, "splash_safari_video.mp4")
                    val videoUri = if (customVideo.exists() && customVideo.length() > 0) {
                        Uri.fromFile(customVideo)
                    } else {
                        val videoPlaybackId = ctx.resources.getIdentifier("videoplayback", "raw", ctx.packageName)
                        val splashSafariId = ctx.resources.getIdentifier("splash_safari_video", "raw", ctx.packageName)
                        val resId = when {
                            videoPlaybackId != 0 -> videoPlaybackId
                            splashSafariId != 0 -> splashSafariId
                            else -> R.raw.videoplayback
                        }
                        Uri.parse("android.resource://${ctx.packageName}/$resId")
                    }
                    setVideoURI(videoUri)

                    setOnPreparedListener { mp ->
                        isVideoPrepared = true
                        mp.isLooping = false
                        val vol = if (isMuted) 0f else 1f
                        mp.setVolume(vol, vol)
                        start()
                        isPlaying = true
                    }

                    setOnCompletionListener {
                        isPlaying = false
                        if (!hasCompleted) {
                            hasCompleted = true
                            // User request: "isko dekhne ke bad hi open hoo"
                            onProceedToMain()
                        }
                    }

                    setOnErrorListener { _, _, _ ->
                        if (!hasCompleted) {
                            hasCompleted = true
                            onProceedToMain()
                        }
                        true
                    }
                }
            },
            update = { vv ->
                videoViewRef = vv
            }
        )

        // Subtle gradient overlay for top status bar and bottom subtitle readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Top Status Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Emblem Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.70f),
                border = BorderStroke(1.dp, RoyalAmberSecondary.copy(alpha = 0.8f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sawai Madhopur Safari",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Sound Toggle & Skip to Posters
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Black.copy(alpha = 0.70f),
                    border = BorderStroke(1.dp, RoyalAmberSecondary.copy(alpha = 0.8f))
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            hasCompleted = true
                            onProceedToMain()
                        },
                        modifier = Modifier.testTag("skip_to_posters_button")
                    ) {
                        Text(
                            text = "स्प्लैश पोस्टर (4 इमेज) ➔",
                            color = RoyalAmberSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        isMuted = !isMuted
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.70f))
                        .testTag("toggle_sound_button")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = if (isMuted) "Unmute Audio" else "Mute Audio",
                        tint = if (isMuted) Color.White.copy(alpha = 0.6f) else RoyalAmberSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Loading spinner if video is buffering
        if (!isVideoPrepared) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(44.dp)
                    .testTag("splash_loading_spinner"),
                color = RoyalAmberSecondary,
                strokeWidth = 3.dp
            )
        }

        // Bottom Subtitle & Transition Indicator
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glassmorphic Subtitle Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.88f),
                border = BorderStroke(1.5.dp, RoyalAmberSecondary.copy(alpha = 0.7f)),
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tag & Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDC2626)
                        ) {
                            Text(
                                text = "● RANTHAMBHORE SAFARI",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        val seconds = (currentPositionMs / 1000).coerceAtMost(4)
                        Text(
                            text = "00:0$seconds / 00:04",
                            color = Color(0xFFFDE68A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Hindi Narration Text
                    Text(
                        text = sceneHindi,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // English Translation
                    Text(
                        text = sceneEnglish,
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar representing video playback
                    LinearProgressIndicator(
                        progress = { videoProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = RoyalAmberSecondary,
                        trackColor = Color(0xFF334155),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "वीडियो समाप्त होने पर 4 इमेज स्प्लैश स्क्रीन खुलेंगी • सभी को स्वाइप करने पर ही मुख्य पेज खुलेगा",
                        color = Color(0xFFFDE68A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Connectivity Status indicator at bottom
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = if (isOnline) "Connected" else "No Internet",
                        tint = if (isOnline) OnlineGreen else OfflineAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOnline) "Internet Connected" else "Offline Mode",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}


