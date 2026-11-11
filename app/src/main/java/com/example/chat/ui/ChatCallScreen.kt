package com.example.chat.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chat.model.ChatCallState

@Composable
fun ChatCallScreen(
    callState: ChatCallState,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onVideoToggle: () -> Unit,
    onFlipCamera: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationText = formatCallDuration(callState.durationSeconds)

    if (callState.isVideoCall) {
        VideoCallContent(
            callState = callState,
            durationText = durationText,
            onMuteToggle = onMuteToggle,
            onSpeakerToggle = onSpeakerToggle,
            onVideoToggle = onVideoToggle,
            onFlipCamera = onFlipCamera,
            onEndCall = onEndCall,
            modifier = modifier
        )
    } else {
        AudioCallContent(
            callState = callState,
            durationText = durationText,
            onMuteToggle = onMuteToggle,
            onSpeakerToggle = onSpeakerToggle,
            onEndCall = onEndCall,
            modifier = modifier
        )
    }
}

@Composable
private fun VideoCallContent(
    callState: ChatCallState,
    durationText: String,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onVideoToggle: () -> Unit,
    onFlipCamera: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("chat_video_call_screen")
    ) {
        // Full screen Live Local Camera Preview
        CameraPreviewView(
            isFrontCamera = callState.isFrontCamera,
            isVideoDisabled = callState.isVideoDisabled,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient Shading at Top and Bottom for readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        // Top Info Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF22C55E),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "एंड-टू-एंड एन्क्रिप्टेड HD वीडियो",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = callState.contactName,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (callState.durationSeconds > 0) durationText else callState.statusText,
                color = Color(0xFF4ADE80),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Floating Remote Peer PiP Box (Top Right)
        Surface(
            modifier = Modifier
                .padding(top = 110.dp, end = 16.dp)
                .size(width = 110.dp, height = 150.dp)
                .align(Alignment.TopEnd),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B),
            shadowElevation = 10.dp,
            border = BorderStroke(2.dp, Color(0xFF22C55E).copy(alpha = 0.8f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F766E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = callState.contactAvatarEmoji,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "HD 1080p",
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "कनेक्टेड",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Bottom Call Controls Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 28.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flip Camera
                CallControlButton(
                    icon = Icons.Default.Cameraswitch,
                    label = "कैमरा फ्लिप",
                    isActive = false,
                    onClick = onFlipCamera,
                    testTag = "call_btn_flip_camera"
                )

                // Video Toggle
                CallControlButton(
                    icon = if (callState.isVideoDisabled) Icons.Default.VideocamOff else Icons.Default.Videocam,
                    label = if (callState.isVideoDisabled) "वीडियो ऑन" else "वीडियो ऑफ",
                    isActive = callState.isVideoDisabled,
                    activeBg = Color(0xFFEF4444),
                    onClick = onVideoToggle,
                    testTag = "call_btn_toggle_video"
                )

                // Mic Mute Toggle
                CallControlButton(
                    icon = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = if (callState.isMuted) "अनम्यूट" else "म्यूट",
                    isActive = callState.isMuted,
                    activeBg = Color(0xFFEF4444),
                    onClick = onMuteToggle,
                    testTag = "call_btn_mute"
                )

                // Speaker Toggle
                CallControlButton(
                    icon = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    label = "स्पीकर",
                    isActive = callState.isSpeakerOn,
                    activeBg = Color(0xFF25D366),
                    onClick = onSpeakerToggle,
                    testTag = "call_btn_speaker"
                )

                // End Call
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDC2626))
                        .clickable { onEndCall() }
                        .testTag("call_btn_end"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "कॉल समाप्त करें",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioCallContent(
    callState: ChatCallState,
    durationText: String,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulsating animation for audio ripple
    val infiniteTransition = rememberInfiniteTransition(label = "audio_ripple")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF111B21)) // WhatsApp Call dark background
            .testTag("chat_audio_call_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Security + Contact Info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "व्हाट्सएप एंड-टू-एंड एन्क्रिप्टेड ऑडियो कॉल",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = callState.contactName,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (callState.contactRole.isNotBlank()) {
                    Text(
                        text = callState.contactRole,
                        color = Color(0xFF38BDF8),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = if (callState.durationSeconds > 0) durationText else callState.statusText,
                    color = Color(0xFF25D366),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Center Pulsating Avatar Visualizer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Outer Ripple Ring
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF25D366).copy(alpha = 0.3f), CircleShape)
                )

                // Middle Ripple Ring
                Box(
                    modifier = Modifier
                        .size(175.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF25D366).copy(alpha = 0.12f))
                )

                // Center Avatar
                Box(
                    modifier = Modifier
                        .size(125.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF075E54)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = callState.contactAvatarEmoji,
                        fontSize = 62.sp
                    )
                }
            }

            // Bottom Audio Controls Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speaker
                CallControlButton(
                    icon = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    label = "स्पीकर",
                    isActive = callState.isSpeakerOn,
                    activeBg = Color(0xFF25D366),
                    onClick = onSpeakerToggle,
                    testTag = "call_audio_btn_speaker"
                )

                // End Call Button (Large Red)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDC2626))
                        .clickable { onEndCall() }
                        .testTag("call_audio_btn_end"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "कॉल समाप्त करें",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Mute
                CallControlButton(
                    icon = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = if (callState.isMuted) "अनम्यूट" else "म्यूट",
                    isActive = callState.isMuted,
                    activeBg = Color(0xFFEF4444),
                    onClick = onMuteToggle,
                    testTag = "call_audio_btn_mute"
                )
            }
        }
    }
}

@Composable
private fun CallControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeBg: Color = Color(0xFF25D366),
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (isActive) activeBg else Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatCallDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
