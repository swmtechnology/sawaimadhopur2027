package com.example.ui

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import java.io.File
import kotlinx.coroutines.launch

/**
 * Metadata for the 4 Mandatory Splash Screen Posters.
 * User requirement:
 * "video ke bad 4 iamge spce screen aaye jinko one by one swipe krna pde tab jaker aage bade ...
 * in imge ko hi add krna hai inko dekhe bina main page dikhai na de"
 */
data class SplashPosterItem(
    val id: Int,
    val titleHindi: String,
    val titleEnglish: String,
    val subText: String,
    val drawableRes: Int,
    val accentColor: Color,
    val tagHindi: String
)

val SPLASH_POSTERS = listOf(
    SplashPosterItem(
        id = 1,
        titleHindi = "रणथंभौर सफारी एवं चंबल घड़ियाल वोट बुकिंग",
        titleEnglish = "Ranthambore Safari & Chambal Gadiyal Boat Booking",
        subText = "जिप्सी / कैंटर सफारी बुकिंग • चंबल वाइल्डलाइफ बोट टूर",
        drawableRes = R.drawable.splash_1,
        accentColor = Color(0xFF16A34A),
        tagHindi = "पोस्टर 1/4 • सफारी बुकिंग"
    ),
    SplashPosterItem(
        id = 2,
        titleHindi = "होटल एवं रूम बुकिंग (मात्र ₹500 से ₹5000)",
        titleEnglish = "Hotel / Room Booking • ₹500 to ₹5000",
        subText = "A/C व नॉन-A/C रूम्स • स्विमिंग पूल • स्वादिष्ट राजस्थानी भोजन",
        drawableRes = R.drawable.splash_2,
        accentColor = Color(0xFF0284C7),
        tagHindi = "पोस्टर 2/4 • होटल बुकिंग"
    ),
    SplashPosterItem(
        id = 3,
        titleHindi = "सवाई माधोपुर फ़ूड एवं स्टे ऐप",
        titleEnglish = "Sawai Madhopur Food & Stay App",
        subText = "वेज एवं नॉन-वेज रेस्टोरेंट • 24/7 होम डिलीवरी • स्विगी/ज़ोमैटो पार्टनर",
        drawableRes = R.drawable.splash_3,
        accentColor = Color(0xFFEA580C),
        tagHindi = "पोस्टर 3/4 • फ़ूड एवं डिलीवरी"
    ),
    SplashPosterItem(
        id = 4,
        titleHindi = "सवाई माधोपुर ऑल इन वन ऐप - सरकारी नौकरी व न्यूज",
        titleEnglish = "Sawai Madhopur All-in-One App",
        subText = "Govt. Job • Exam • Admit Card • Result • NCERT Books • News",
        drawableRes = R.drawable.splash_4,
        accentColor = Color(0xFF2563EB),
        tagHindi = "पोस्टर 4/4 • मुख्य ऐप पोर्टल"
    )
)

/**
 * Fullscreen Swipe-Based 4-Image Splash Screen Flow.
 * Users MUST swipe through all 4 images sequentially before they can enter the main portal.
 * No skip button is provided to ensure full compliance with the user's explicit directive.
 */
@Composable
fun OnboardingSplashScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { SPLASH_POSTERS.size })
    val coroutineScope = rememberCoroutineScope()
    var fullScreenPoster by remember { mutableStateOf<SplashPosterItem?>(null) }

    // Pulsing animation for the final action button on the 4th image
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080D1A))
            .testTag("onboarding_screen")
    ) {
        // Horizontal Pager displaying the 4 Full-Screen Image Splash Screens
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("splash_images_pager")
        ) { pageIndex ->
            val poster = SPLASH_POSTERS[pageIndex]
            FullScreenSplashPosterPage(
                poster = poster,
                pageIndex = pageIndex,
                totalPages = SPLASH_POSTERS.size,
                onZoomClick = { fullScreenPoster = poster }
            )
        }

        // Top Status Header (Image Progress Counter + Informative Pill, NO SKIP BUTTON)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Pill: Current Screen Badge
            val currentPoster = SPLASH_POSTERS[pagerState.currentPage]
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, currentPoster.accentColor.copy(alpha = 0.8f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(currentPoster.accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "स्प्लैश इमेज ${pagerState.currentPage + 1} / ${SPLASH_POSTERS.size}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Right Pill: Direct swipe instruction (Reinforces sequence requirement)
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Swipe,
                        contentDescription = "Swipe to proceed",
                        tint = Color(0xFFFEF08A),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (pagerState.currentPage < 3) "आगे स्वाइप करें" else "अंतिम पोस्टर",
                        color = Color(0xFFFEF08A),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Bottom Navigation Bar with Indicators, Back button, and Next/Finish button
        Surface(
            color = Color(0xFF0A0F1D).copy(alpha = 0.95f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            shadowElevation = 16.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dot Indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(SPLASH_POSTERS.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val widthAnim = animateDpAsState(
                            targetValue = if (isSelected) 32.dp else 10.dp,
                            label = "dot_width"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(widthAnim.value)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) SPLASH_POSTERS[index].accentColor
                                    else Color.White.copy(alpha = 0.25f)
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }

                // Action Navigation Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button (shown only on page 2, 3, 4)
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.35f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.testTag("splash_prev_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "पिछला",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("पिछला", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        // Informational placeholder on page 1
                        Text(
                            text = "1 / 4 • स्वाइप करें ➔",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Next / Enter Main Page Button
                    if (pagerState.currentPage < SPLASH_POSTERS.size - 1) {
                        // On pages 1, 2, 3: Next button to guide sequential swipe
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SPLASH_POSTERS[pagerState.currentPage].accentColor
                            ),
                            modifier = Modifier.testTag("splash_next_button")
                        ) {
                            Text(
                                "अगला (${pagerState.currentPage + 2}/4)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        // ONLY on the 4th (last) page: User has seen all 4 images!
                        // Unlocks the main page with a prominent, vibrant call-to-action
                        Button(
                            onClick = onFinishOnboarding,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A)
                            ),
                            modifier = Modifier
                                .scale(pulseScale)
                                .testTag("splash_finish_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFFFEF08A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "मुख्य पेज खोलें ➔",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Fullscreen Tap-to-Zoom Modal if user clicks on poster
        fullScreenPoster?.let { poster ->
            FullScreenPosterViewerDialog(
                poster = poster,
                onDismiss = { fullScreenPoster = null }
            )
        }
    }
}

/**
 * Individual Full-Screen Poster Page for the HorizontalPager.
 * Displays the image as the central hero, with crisp aspect-fit rendering,
 * decorative framing, and tap-to-zoom affordance.
 */
@Composable
fun FullScreenSplashPosterPage(
    poster: SplashPosterItem,
    pageIndex: Int,
    totalPages: Int,
    onZoomClick: () -> Unit
) {
    val context = LocalContext.current

    // Check if custom image exists in local storage
    val diskBitmap = remember(poster.id) {
        val candidates = listOf(
            File(context.filesDir, "splash_${poster.id}.png"),
            File(context.filesDir, "splash_${poster.id}.jpg"),
            File(context.getExternalFilesDir(null), "splash_${poster.id}.png"),
            File(context.getExternalFilesDir(null), "splash_${poster.id}.jpg")
        )
        candidates.firstOrNull { it.exists() && it.length() > 0 }?.let { file ->
            try {
                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 56.dp, bottom = 90.dp)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Card framing the poster image
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onZoomClick() }
                .testTag("poster_card_${poster.id}"),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF030712),
            shadowElevation = 12.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Brush.verticalGradient(
                    listOf(
                        poster.accentColor.copy(alpha = 0.7f),
                        Color.White.copy(alpha = 0.1f),
                        poster.accentColor.copy(alpha = 0.5f)
                    )
                )
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (diskBitmap != null) {
                    Image(
                        bitmap = diskBitmap,
                        contentDescription = poster.titleHindi,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("splash_poster_image_${poster.id}")
                    )
                } else {
                    Image(
                        painter = painterResource(id = poster.drawableRes),
                        contentDescription = poster.titleHindi,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("splash_poster_image_${poster.id}")
                    )
                }

                // Tap to Zoom hint pill at bottom right
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(topStart = 14.dp),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Zoom Poster",
                            tint = Color(0xFFFEF08A),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ज़ूम करें",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Interactive Zoom Dialog with Pinch-to-Zoom & Pan.
 */
@Composable
fun FullScreenPosterViewerDialog(
    poster: SplashPosterItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val diskBitmap = remember(poster.id) {
        val candidates = listOf(
            File(context.filesDir, "splash_${poster.id}.png"),
            File(context.filesDir, "splash_${poster.id}.jpg"),
            File(context.getExternalFilesDir(null), "splash_${poster.id}.png"),
            File(context.getExternalFilesDir(null), "splash_${poster.id}.jpg")
        )
        candidates.firstOrNull { it.exists() }?.let { file ->
            try {
                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Interactive Zoomable / Pannable Image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (diskBitmap != null) {
                    Image(
                        bitmap = diskBitmap,
                        contentDescription = poster.titleHindi,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            )
                    )
                } else {
                    Image(
                        painter = painterResource(id = poster.drawableRes),
                        contentDescription = poster.titleHindi,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            )
                    )
                }
            }

            // Top Dismiss & Title Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.80f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, poster.accentColor)
                ) {
                    Text(
                        text = poster.tagHindi,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.80f))
                        .testTag("dismiss_zoom_dialog")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Bottom Reset Zoom Button (when zoomed in)
            if (scale > 1.05f) {
                Surface(
                    color = Color.Black.copy(alpha = 0.80f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .clickable {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Reset Zoom",
                            tint = Color(0xFFFEF08A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ज़ूम रीसेट करें",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
