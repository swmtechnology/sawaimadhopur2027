package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Definition of an image that can be customized by the user.
 */
data class ConfigurableImageItem(
    val id: String,
    val fileName: String,
    val titleHindi: String,
    val subtitle: String,
    val defaultDrawableRes: Int,
    val categoryBadge: String,
    val badgeColor: Color
)

val CONFIGURABLE_IMAGES = listOf(
    ConfigurableImageItem(
        id = "splash_1",
        fileName = "splash_1.png",
        titleHindi = "स्प्लैश 1: रणथंभौर सफारी व बोट बुकिंग",
        subtitle = "टाइगर सफारी, जिप्सी, कैंटर व चंबल घड़ियाल बोट टूर पोस्टर",
        defaultDrawableRes = R.drawable.splash_1,
        categoryBadge = "पोस्टर 1/4",
        badgeColor = Color(0xFF16A34A)
    ),
    ConfigurableImageItem(
        id = "splash_2",
        fileName = "splash_2.png",
        titleHindi = "स्प्लैश 2: होटल व रूम बुकिंग",
        subtitle = "मात्र ₹500 से ₹5000, A/C व नॉन-A/C, स्विमिंग पूल पोस्टर",
        defaultDrawableRes = R.drawable.splash_2,
        categoryBadge = "पोस्टर 2/4",
        badgeColor = Color(0xFF0284C7)
    ),
    ConfigurableImageItem(
        id = "splash_3",
        fileName = "splash_3.png",
        titleHindi = "स्प्लैश 3: सवाई माधोपुर फ़ूड व स्टे",
        subtitle = "रेस्टोरेंट, वेज/नॉन-वेज, 24/7 होम डिलीवरी पोस्टर",
        defaultDrawableRes = R.drawable.splash_3,
        categoryBadge = "पोस्टर 3/4",
        badgeColor = Color(0xFFEA580C)
    ),
    ConfigurableImageItem(
        id = "splash_4",
        fileName = "splash_4.png",
        titleHindi = "स्प्लैश 4: ऑल-इन-वन मुख्य ऐप पोर्टल",
        subtitle = "सरकारी नौकरी, एग्जाम, एडमिट कार्ड, बुक्स व न्यूज़ पोस्टर",
        defaultDrawableRes = R.drawable.splash_4,
        categoryBadge = "पोस्टर 4/4",
        badgeColor = Color(0xFF2563EB)
    ),
    ConfigurableImageItem(
        id = "app_banner",
        fileName = "app_banner.png",
        titleHindi = "ऐप मुख्य हेडर बैनर",
        subtitle = "ऐप के अंदर दिखने वाला मुख्य प्रचार बैनर पोस्टर",
        defaultDrawableRes = R.drawable.splash_4,
        categoryBadge = "ऐप बैनर",
        badgeColor = Color(0xFF9333EA)
    )
)

/**
 * Bottom Sheet / Dialog allowing users to change any image (Splash 1 to 4 & Banners)
 * via direct Image URL or Gallery photo selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageManagerSheet(
    onDismiss: () -> Unit,
    onPreviewSplashScreens: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var refreshKey by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(44.dp)
                    .height(4.dp)
            ) {}
        },
        modifier = Modifier.testTag("image_manager_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "वीडियो, इमेज व पोस्टर मैनेजर",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "स्प्लैश वीडियो, 4 स्प्लैश स्क्रीन व बैनर का लिंक बदलें",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notice Box
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "यहाँ स्प्लैश वीडियो (MP4) या किसी भी पोस्टर/इमेज का नया लिंक पेस्ट करें या गैलरी से चुनें। स्प्लैश स्क्रीन तुरंत नए वीडियो/पोस्टर से अपडेट हो जाएगी।",
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preview Splash Screens Button
            Button(
                onClick = {
                    onDismiss()
                    onPreviewSplashScreens()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("preview_splash_flow_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "स्प्लैश पोस्टर चलाकर देखें (4 इमेज फ्लो)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(10.dp))

            // List of configurable items (Video + Images)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Splash Video Manager
                item(key = "splash_video_card") {
                    ConfigurableVideoCard(
                        refreshKey = refreshKey,
                        onVideoUpdated = {
                            refreshKey++
                            Toast.makeText(
                                context,
                                "स्प्लैश वीडियो सफलतापूर्वक अपडेट हो गया!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                // Section 2: Splash Posters and Banners
                items(CONFIGURABLE_IMAGES, key = { it.id }) { item ->
                    ConfigurableImageCard(
                        item = item,
                        refreshKey = refreshKey,
                        onImageUpdated = {
                            refreshKey++
                            Toast.makeText(
                                context,
                                "${item.categoryBadge} इमेज सफलतापूर्वक अपडेट हो गई!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Card for an individual configurable image with URL input, Gallery Picker, and Reset options.
 */
@Composable
fun ConfigurableImageCard(
    item: ConfigurableImageItem,
    refreshKey: Int,
    onImageUpdated: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("image_prefs", Context.MODE_PRIVATE) }

    var urlInput by remember(item.id) {
        mutableStateOf(prefs.getString("url_${item.id}", "") ?: "")
    }
    var isDownloading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val file = remember(item.id, refreshKey) {
        File(context.filesDir, item.fileName)
    }
    val hasCustomImage = file.exists() && file.length() > 0

    val currentBitmap = remember(item.id, refreshKey) {
        if (hasCustomImage) {
            try {
                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        } else null
    }

    // Photo picker for selecting an image from the device gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isDownloading = true
                errorMessage = null
                try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    prefs.edit().putString("url_${item.id}", "gallery_upload").apply()
                    onImageUpdated()
                } catch (e: Exception) {
                    errorMessage = "इमेज सेव करने में त्रुटि: ${e.localizedMessage}"
                } finally {
                    isDownloading = false
                }
            }
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("configurable_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Title and Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = item.badgeColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.categoryBadge,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = if (hasCustomImage) Color(0xFF16A34A).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (hasCustomImage) Color(0xFF22C55E) else Color(0xFF94A3B8))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasCustomImage) "कस्टम इमेज सक्रिय" else "डिफ़ॉल्ट इमेज",
                            color = if (hasCustomImage) Color(0xFF86EFAC) else Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.titleHindi,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = item.subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Thumbnail Preview Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentBitmap != null) {
                        Image(
                            bitmap = currentBitmap,
                            contentDescription = item.titleHindi,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = item.defaultDrawableRes),
                            contentDescription = item.titleHindi,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (hasCustomImage) "सफलतापूर्वक सेव्ड फ़ाइल: ${item.fileName}" else "मूल डिफ़ॉल्ट पोस्टर सक्रिय है",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "नीचे नया लिंक डालें या सीधे गैलरी से फोटो जोड़ें",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }

                if (hasCustomImage) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    if (file.exists()) file.delete()
                                    prefs.edit().remove("url_${item.id}").apply()
                                    urlInput = ""
                                    onImageUpdated()
                                } catch (_: Exception) {}
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("reset_image_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("रीसेट", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // URL Input Field
            OutlinedTextField(
                value = urlInput,
                onValueChange = {
                    urlInput = it
                    errorMessage = null
                },
                label = { Text("नया इमेज URL (Link)", fontSize = 12.sp) },
                placeholder = { Text("https://example.com/poster.jpg", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                    focusedLabelColor = Color(0xFF38BDF8),
                    unfocusedLabelColor = Color(0xFF94A3B8)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("url_input_${item.id}")
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFF87171),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Save URL & Pick from Gallery
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save from URL button
                Button(
                    onClick = {
                        val trimmedUrl = urlInput.trim()
                        if (trimmedUrl.isEmpty() || !trimmedUrl.startsWith("http")) {
                            errorMessage = "कृपया वैध इमेज लिंक (http/https) डालें"
                            return@Button
                        }
                        coroutineScope.launch {
                            isDownloading = true
                            errorMessage = null
                            try {
                                val success = downloadAndSaveImage(context, trimmedUrl, file)
                                if (success) {
                                    prefs.edit().putString("url_${item.id}", trimmedUrl).apply()
                                    onImageUpdated()
                                } else {
                                    errorMessage = "इमेज डाउनलोड नहीं हो सकी। लिंक जांचें।"
                                }
                            } catch (e: Exception) {
                                errorMessage = "त्रुटि: ${e.localizedMessage}"
                            } finally {
                                isDownloading = false
                            }
                        }
                    },
                    enabled = !isDownloading,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("download_button_${item.id}")
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("लिंक सेव करें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Pick from Gallery button
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isDownloading,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFEF08A).copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFEF08A)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gallery_picker_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("गैलरी से चुनें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Card for configuring the Opening Splash Screen Video.
 * Allows user to pick a video from Gallery or enter a direct MP4 URL,
 * as well as view status and reset to default.
 */
@Composable
fun ConfigurableVideoCard(
    refreshKey: Int,
    onVideoUpdated: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("image_prefs", Context.MODE_PRIVATE) }

    var videoUrlInput by remember {
        mutableStateOf(prefs.getString("url_splash_video", "") ?: "")
    }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val videoFile = remember(refreshKey) {
        File(context.filesDir, "splash_safari_video.mp4")
    }
    val hasCustomVideo = videoFile.exists() && videoFile.length() > 0

    // Video picker launcher for gallery MP4 selection
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isProcessing = true
                errorMessage = null
                try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(videoFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    prefs.edit().putString("url_splash_video", "gallery_video").apply()
                    onVideoUpdated()
                } catch (e: Exception) {
                    errorMessage = "वीडियो सेव करने में त्रुटि: ${e.localizedMessage}"
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("configurable_card_splash_video")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row with Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFD97706),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "स्प्लैश स्क्रीन वीडियो",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = if (hasCustomVideo) Color(0xFF16A34A).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (hasCustomVideo) Color(0xFF22C55E) else Color(0xFF94A3B8))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasCustomVideo) "कस्टम वीडियो सक्रिय (${videoFile.length() / 1024} KB)" else "raw/videoplayback वीडियो सक्रिय",
                            color = if (hasCustomVideo) Color(0xFF86EFAC) else Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "ओपनिंग स्प्लैश वीडियो (Splash Video)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Text(
                text = "ऐप खुलते ही चलने वाला मुख्य वीडियो। यहाँ से आप नया वीडियो चुन सकते हैं या raw फ़ोल्डर का वीडियो चलेगा।",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // URL input
            OutlinedTextField(
                value = videoUrlInput,
                onValueChange = {
                    videoUrlInput = it
                    errorMessage = null
                },
                label = { Text("ऑनलाइन वीडियो MP4 डायरेक्ट लिंक", fontSize = 12.sp) },
                placeholder = { Text("https://example.com/safari_video.mp4", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("video_url_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFF59E0B),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedLabelColor = Color(0xFFF59E0B),
                    unfocusedLabelColor = Color(0xFF94A3B8)
                )
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFEF4444),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save from URL button
                Button(
                    onClick = {
                        val trimmed = videoUrlInput.trim()
                        if (trimmed.isNotBlank()) {
                            coroutineScope.launch {
                                isProcessing = true
                                errorMessage = null
                                val success = downloadAndSaveImage(context, trimmed, videoFile)
                                if (success) {
                                    prefs.edit().putString("url_splash_video", trimmed).apply()
                                    onVideoUpdated()
                                } else {
                                    errorMessage = "वीडियो डाउनलोड नहीं हो सका। कृपया लिंक जांचें।"
                                }
                                isProcessing = false
                            }
                        }
                    },
                    enabled = !isProcessing && videoUrlInput.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("video_save_url_btn")
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("लिंक सेव करें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Pick from Gallery button
                OutlinedButton(
                    onClick = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    enabled = !isProcessing,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFEF08A).copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFEF08A)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("video_gallery_picker_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("फ़ोन से वीडियो चुनें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (hasCustomVideo) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        if (videoFile.exists()) {
                            videoFile.delete()
                        }
                        prefs.edit().remove("url_splash_video").apply()
                        videoUrlInput = ""
                        onVideoUpdated()
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("डिफ़ॉल्ट raw/videoplayback पर रीसेट करें", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Downloads image bytes from URL and saves to target file.
 */
private suspend fun downloadAndSaveImage(
    context: Context,
    urlString: String,
    targetFile: File
): Boolean = withContext(Dispatchers.IO) {
    try {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 12000
        conn.readTimeout = 15000
        conn.instanceFollowRedirects = true
        conn.requestMethod = "GET"
        conn.connect()

        if (conn.responseCode in 200..299) {
            val bytes = conn.inputStream.use { it.readBytes() }
            // Verify valid image data
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                targetFile.parentFile?.mkdirs()
                FileOutputStream(targetFile).use { out ->
                    out.write(bytes)
                    out.flush()
                }
                return@withContext true
            }
        }
        false
    } catch (_: Exception) {
        false
    }
}
