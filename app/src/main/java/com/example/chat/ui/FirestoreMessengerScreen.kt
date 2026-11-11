package com.example.chat.ui

import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chat.data.ActiveChatSummary
import com.example.chat.data.ChatUser
import com.example.chat.data.FirestoreMessengerRepository
import com.example.chat.data.PrivateMessage
import com.example.data.SessionEntity
import com.example.data.UserProfileData
import com.example.ui.theme.TerracottaPrimary
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FirestoreMessengerScreen(
    repository: FirestoreMessengerRepository,
    session: SessionEntity?,
    userProfile: UserProfileData?,
    isOnline: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentUser = remember(session, userProfile) {
        repository.getCurrentUser(session, userProfile)
    }

    // Sync current user to Firestore 'users' collection on open
    LaunchedEffect(currentUser.uid) {
        repository.syncCurrentUserToFirestore(session, userProfile)
    }

    // Nearby Connections required permissions for Android 13+ and older
    val requiredNearbyPermissions = remember {
        val perms = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(android.Manifest.permission.BLUETOOTH_SCAN)
            perms.add(android.Manifest.permission.BLUETOOTH_ADVERTISE)
            perms.add(android.Manifest.permission.BLUETOOTH_CONNECT)
            perms.add(android.Manifest.permission.NEARBY_WIFI_DEVICES)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(android.Manifest.permission.BLUETOOTH_SCAN)
            perms.add(android.Manifest.permission.BLUETOOTH_ADVERTISE)
            perms.add(android.Manifest.permission.BLUETOOTH_CONNECT)
            perms.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            perms.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        perms
    }

    val nearbyPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        repository.nearbyManager.startP2P(currentUser)
    }

    // Launch Nearby P2P advertising & discovery automatically so offline users connect instantly
    LaunchedEffect(currentUser.uid) {
        val hasAll = requiredNearbyPermissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
        if (hasAll) {
            repository.nearbyManager.startP2P(currentUser)
        } else {
            nearbyPermissionLauncher.launch(requiredNearbyPermissions.toTypedArray())
        }
    }

    // Active 1-on-1 Chat target (null means we are in the Inbox Screen)
    var activeChatUser by remember { mutableStateOf<ChatUser?>(null) }

    // Intercept hardware back button: If inside a chat, return to inbox; otherwise dismiss messenger
    BackHandler {
        if (activeChatUser != null) {
            activeChatUser = null
        } else {
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("messenger_screen_root")
    ) {
        if (activeChatUser == null) {
            // 1. INBOX SCREEN (Chat List & Search)
            MessengerInboxView(
                repository = repository,
                currentUser = currentUser,
                isOnline = isOnline,
                onSelectUser = { targetUser ->
                    activeChatUser = targetUser
                },
                onDismiss = onDismiss
            )
        } else {
            // 2. PERSONAL CHAT SCREEN (Real-time 1-on-1 conversation)
            PersonalChatView(
                repository = repository,
                currentUser = currentUser,
                receiver = activeChatUser!!,
                isOnline = isOnline,
                onBack = {
                    activeChatUser = null
                }
            )
        }
    }
}

/**
 * 1. INBOX SCREEN (Chat List)
 * - Clean UI that ONLY shows people the current user has active chats with.
 * - Removed all unnecessary clutter (no fake hotels, no safari clutter, no P2P call spam).
 * - Live search across Firestore 'users' collection by Name, Mobile Number, or Gmail ID.
 * - Tapping on searched user opens direct chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerInboxView(
    repository: FirestoreMessengerRepository,
    currentUser: ChatUser,
    isOnline: Boolean,
    onSelectUser: (ChatUser) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Real-time active chats from Firestore
    val activeChatsFlow = remember(currentUser.uid) {
        repository.getActiveChatsFlow(currentUser.uid)
    }
    val activeChats by activeChatsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    // Active P2P peers discovered / connected via Google Nearby Connections
    val connectedPeers by repository.nearbyManager.connectedPeers.collectAsStateWithLifecycle()

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<ChatUser>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    // Search 'users' collection when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().length >= 2) {
            isSearching = true
            try {
                searchResults = repository.searchUsers(searchQuery, currentUser.uid)
            } catch (e: Exception) {
                searchResults = emptyList()
            } finally {
                isSearching = false
            }
        } else {
            searchResults = emptyList()
            isSearching = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0F172A),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "💬 मैसेन्जर (Messenger)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            )
                        }
                        Text(
                            text = "आपका खाता: ${currentUser.name} (${if (currentUser.mobile.isNotBlank()) currentUser.mobile else currentUser.email})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_messenger_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "बंद करें (Close)",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Nearby P2P Status Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (connectedPeers.isNotEmpty()) Color(0xFF065F46) else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (connectedPeers.isNotEmpty()) Color(0xFF10B981) else Color(0xFF334155)),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("p2p_mesh_status_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = "P2P Free Chat",
                                tint = if (connectedPeers.isNotEmpty()) Color(0xFF34D399) else Color(0xFF94A3B8),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (connectedPeers.isNotEmpty()) "P2P (${connectedPeers.size})" else "P2P मेश",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (connectedPeers.isNotEmpty()) Color(0xFF34D399) else Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Cloud Connectivity Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOnline) Color(0xFF0F766E) else Color(0xFF78350F),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("offline_sync_status_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = if (isOnline) "Online" else "Offline",
                                tint = if (isOnline) Color(0xFF5EEAD4) else Color(0xFFFCD34D),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOnline) "ऑनलाइन" else "ऑफलाइन सिंक",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOnline) Color(0xFF5EEAD4) else Color(0xFFFCD34D),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E293B)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    searchFocusRequester.requestFocus()
                },
                containerColor = Color(0xFF0D9488),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("start_chat_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonSearch,
                        contentDescription = "Start Chat"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "नया चैट",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar for searching 'users' collection by Name, Mobile, or Gmail ID
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(searchFocusRequester)
                            .testTag("inbox_search_field"),
                        placeholder = {
                            Text(
                                "नाम, 10-अंकों का मोबाइल या Gmail खोजें...",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // If Search Query is Active: Show Matching Users from 'users' collection
            if (searchQuery.isNotBlank()) {
                Text(
                    text = "🔍 खोज परिणाम (सर्च 'users' कलेक्शन):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF38BDF8)
                    ),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                )

                if (isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "उपयोगकर्ता खोजे जा रहे हैं...",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }
                } else if (searchResults.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "इस विवरण से कोई यूज़र नहीं मिला",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "कृपया सही नाम, 10 अंकों का मोबाइल नंबर या पूरा Gmail ID टाइप करें।",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults, key = { it.uid }) { user ->
                            SearchedUserItemCard(
                                user = user,
                                onStartChat = {
                                    searchQuery = ""
                                    onSelectUser(user)
                                }
                            )
                        }
                    }
                }
            } else {
                // If NO Search Query: Show ONLY Active 1-on-1 Chats with current user
                if (activeChats.isEmpty()) {
                    // Clean Empty State
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF0D9488).copy(alpha = 0.2f),
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = "No Chats",
                                            tint = Color(0xFF2DD4BF),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "अभी कोई एक्टिव चैट नहीं है",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "केवल उन लोगों की सूची यहाँ दिखेगी जिनसे आपकी बातचीत हुई है।\n\nसवाई माधोपुर के किसी भी नागरिक को संदेश भेजने के लिए ऊपर सर्च बार से खोजें या नीचे दिए गए बटन पर टैप करें।",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        lineHeight = 18.sp
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        searchFocusRequester.requestFocus()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("नया चैट शुरू करें (Start Chat)")
                                }
                            }
                        }
                    }
                } else {
                    // Active Conversations List
                    Text(
                        text = "सक्रिय बातचीत (Active 1-on-1 Chats):",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(activeChats, key = { it.chatId }) { chat ->
                            ActiveChatItemCard(
                                chat = chat,
                                currentUserId = currentUser.uid,
                                onClick = {
                                    onSelectUser(chat.otherUser)
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
 * 2. PERSONAL CHAT SCREEN (Real-time 1-on-1 Chat)
 * - Real-time Firestore snapshot listener on 'messages' collection.
 * - Offline persistence enabled: messages send and save locally while offline,
 *   and sync automatically to receiver when internet is back.
 * - Displays message history clearly, allowing receiver to see who messaged them and reply easily.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalChatView(
    repository: FirestoreMessengerRepository,
    currentUser: ChatUser,
    receiver: ChatUser,
    isOnline: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Real-time messages between currentUser and receiver
    val messagesFlow = remember(currentUser.uid, receiver.uid) {
        repository.getMessagesFlow(currentUser.uid, receiver.uid)
    }
    val messages by messagesFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    // Nearby Connections P2P peers
    val connectedPeers by repository.nearbyManager.connectedPeers.collectAsStateWithLifecycle()
    val isPeerConnected = remember(connectedPeers, receiver.uid, receiver.mobile) {
        connectedPeers.values.any { peer ->
            peer.userId == receiver.uid ||
                    (receiver.mobile.isNotBlank() && peer.mobile == receiver.mobile) ||
                    (peer.name.isNotBlank() && peer.name.equals(receiver.name, ignoreCase = true))
        }
    }

    // Transport mode selection: AUTO, P2P, or CLOUD
    var selectedTransportMode by remember { mutableStateOf("AUTO") }

    var inputMessageText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0B141A), // Modern messaging dark canvas
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Receiver Avatar
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0D9488),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = receiver.name.take(1).uppercase().ifBlank { "👤" },
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = receiver.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (receiver.mobile.isNotBlank()) receiver.mobile else receiver.email,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPeerConnected) {
                                        "📡 P2P कनेक्टेड"
                                    } else if (isOnline) {
                                        "🟢 रियल-टाइम क्लाउड"
                                    } else {
                                        "📶 ऑफलाइन ऑटो-सिंक"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isPeerConnected) Color(0xFF34D399) else if (isOnline) Color(0xFF5EEAD4) else Color(0xFFFCD34D),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_to_inbox_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to inbox",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // P2P Status Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPeerConnected) Color(0xFF065F46) else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (isPeerConnected) Color(0xFF10B981) else Color(0xFF334155)),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("chat_p2p_status_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = "P2P status",
                                tint = if (isPeerConnected) Color(0xFF34D399) else Color(0xFF94A3B8),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isPeerConnected) "P2P कनेक्टेड" else "P2P रेडी",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPeerConnected) Color(0xFF34D399) else Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E293B)
                )
            )
        },
        bottomBar = {
            // Message Input Bar anchored at bottom with IME padding
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding(),
                color = Color(0xFF1E293B),
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column {
                    // Transport Selection Bar (Auto / Offline P2P / Cloud)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // AUTO Mode Chip
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedTransportMode == "AUTO") Color(0xFF0D9488) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (selectedTransportMode == "AUTO") Color(0xFF14B8A6) else Color(0xFF334155)),
                                modifier = Modifier
                                    .clickable { selectedTransportMode = "AUTO" }
                                    .testTag("transport_mode_auto")
                            ) {
                                Text(
                                    text = "⚡ ऑटो",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTransportMode == "AUTO") Color.White else Color(0xFF94A3B8)
                                    )
                                )
                            }

                            // P2P Mode Chip (Google Nearby Connections Free Chat)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedTransportMode == "P2P") Color(0xFF065F46) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (selectedTransportMode == "P2P") Color(0xFF10B981) else Color(0xFF334155)),
                                modifier = Modifier
                                    .clickable { selectedTransportMode = "P2P" }
                                    .testTag("transport_mode_p2p")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bluetooth,
                                        contentDescription = "P2P",
                                        tint = if (selectedTransportMode == "P2P") Color(0xFF34D399) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "📡 ऑफलाइन P2P",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedTransportMode == "P2P") Color(0xFF34D399) else Color(0xFF94A3B8)
                                        )
                                    )
                                }
                            }

                            // Cloud Mode Chip (Firestore)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedTransportMode == "CLOUD") Color(0xFF1E3A8A) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (selectedTransportMode == "CLOUD") Color(0xFF3B82F6) else Color(0xFF334155)),
                                modifier = Modifier
                                    .clickable { selectedTransportMode = "CLOUD" }
                                    .testTag("transport_mode_cloud")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cloud,
                                        contentDescription = "Cloud",
                                        tint = if (selectedTransportMode == "CLOUD") Color(0xFF93C5FD) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "☁️ क्लाउड",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedTransportMode == "CLOUD") Color(0xFF93C5FD) else Color(0xFF94A3B8)
                                        )
                                    )
                                }
                            }
                        }

                        // Info tagline
                        Text(
                            text = when (selectedTransportMode) {
                                "P2P" -> "मुफ़्त P2P"
                                "CLOUD" -> "क्लाउड"
                                else -> if (isPeerConnected) "P2P वरीयता" else if (isOnline) "क्लाउड" else "ऑफलाइन"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = Color(0xFF64748B)
                            )
                        )
                    }

                    if (!isOnline && selectedTransportMode != "P2P") {
                        // Inform user that messages are safely saved offline and will auto-sync
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF78350F).copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Offline queue",
                                tint = Color(0xFFFCD34D),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ऑफलाइन मोड: संदेश फोन में सुरक्षित रहेंगे और इंटरनेट आते ही स्वतः पहुंचेंगे।",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFDE68A),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputMessageText,
                            onValueChange = { inputMessageText = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("message_input_field"),
                            placeholder = {
                                Text(
                                    if (selectedTransportMode == "P2P") "P2P संदेश लिखें (Free Chat)..." else "मैसेज लिखें... (Type a message)",
                                    color = Color(0xFF64748B),
                                    fontSize = 14.sp
                                )
                            },
                            maxLines = 4,
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedBorderColor = if (selectedTransportMode == "P2P") Color(0xFF10B981) else Color(0xFF0D9488),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                val textToSend = inputMessageText.trim()
                                if (textToSend.isNotBlank()) {
                                    inputMessageText = ""
                                    val effectiveTransport = when (selectedTransportMode) {
                                        "P2P" -> "P2P"
                                        "CLOUD" -> "CLOUD"
                                        else -> if (isPeerConnected || !isOnline) "P2P" else "CLOUD"
                                    }
                                    scope.launch {
                                        repository.sendMessage(
                                            sender = currentUser,
                                            receiver = receiver,
                                            messageText = textToSend,
                                            transport = effectiveTransport
                                        )
                                    }
                                }
                            },
                            enabled = inputMessageText.isNotBlank(),
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    if (inputMessageText.isNotBlank()) {
                                        if (selectedTransportMode == "P2P") Color(0xFF059669) else Color(0xFF0D9488)
                                    } else {
                                        Color(0xFF334155)
                                    }
                                )
                                .testTag("send_message_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (inputMessageText.isNotBlank()) Color.White else Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👋 ${receiver.name} के साथ 1-on-1 चैट",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "यह एक सुरक्षित और निजी बातचीत है। आप ऑफलाइन होने पर भी संदेश भेज सकते हैं, जो इंटरनेट कनेक्ट होते ही प्राप्तकर्ता को डिलीवर हो जाएगा।",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        isOutgoing = msg.isOutgoing,
                        timeFormatted = timeFormatter.format(Date(msg.timestamp))
                    )
                }
            }
        }
    }
}

/**
 * Message Bubble UI
 * - Outgoing: Terracotta/teal container aligned to End
 * - Incoming: Dark slate card aligned to Start, clearly displaying sender's name so receiver sees who messaged them
 * - Shows Pending sync clock if sent while offline, and double checkmark once synced!
 */
@Composable
fun MessageBubble(
    message: PrivateMessage,
    isOutgoing: Boolean,
    timeFormatted: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOutgoing) 16.dp else 4.dp,
                bottomEnd = if (isOutgoing) 4.dp else 16.dp
            ),
            color = if (isOutgoing) Color(0xFF0F766E) else Color(0xFF1E293B),
            border = if (isOutgoing) null else BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.widthIn(min = 80.dp, max = 290.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // For incoming message: Display sender's name clearly so receiver knows who wrote it
                if (!isOutgoing && message.senderName.isNotBlank()) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = message.messageText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Transport indicator badge (Cloud vs Offline P2P)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (message.transport == "P2P") {
                            Color(0xFF065F46).copy(alpha = 0.8f)
                        } else {
                            Color(0xFF1E3A8A).copy(alpha = 0.6f)
                        },
                        modifier = Modifier.padding(end = 5.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (message.transport == "P2P") Icons.Default.Bluetooth else Icons.Default.Cloud,
                                contentDescription = if (message.transport == "P2P") "P2P Free Chat" else "Cloud Firestore",
                                tint = if (message.transport == "P2P") Color(0xFF34D399) else Color(0xFF93C5FD),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (message.transport == "P2P") "P2P" else "Cloud",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (message.transport == "P2P") Color(0xFF34D399) else Color(0xFF93C5FD)
                                )
                            )
                        }
                    }

                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isOutgoing) Color(0xFFCCFBF1) else Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    )

                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        if (message.isPendingSync) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Offline Pending Sync",
                                tint = Color(0xFFFCD34D),
                                modifier = Modifier.size(12.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered / Synced",
                                tint = Color(0xFF5EEAD4),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card for an active conversation in the Inbox list
 */
@Composable
fun ActiveChatItemCard(
    chat: ActiveChatSummary,
    currentUserId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(chat.lastTimestamp) {
        if (chat.lastTimestamp > 0) timeFormatter.format(Date(chat.lastTimestamp)) else ""
    }

    val isUserLastSender = chat.lastSenderId == currentUserId

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("active_chat_item_${chat.chatId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                shape = CircleShape,
                color = Color(0xFF0D9488),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = chat.otherUser.name.take(1).uppercase().ifBlank { "👤" },
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.otherUser.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (formattedTime.isNotBlank()) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUserLastSender) "आप: ${chat.lastMessageText}" else chat.lastMessageText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isUserLastSender) Color(0xFF94A3B8) else Color(0xFFE2E8F0),
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (isUserLastSender) {
                        Spacer(modifier = Modifier.width(6.dp))
                        if (chat.isPendingSync) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Offline Pending",
                                tint = Color(0xFFFCD34D),
                                modifier = Modifier.size(13.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Synced",
                                tint = Color(0xFF2DD4BF),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card for searched user from 'users' collection
 */
@Composable
fun SearchedUserItemCard(
    user: ChatUser,
    onStartChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onStartChat)
            .testTag("search_user_result_${user.uid}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF0D9488).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF0284C7),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.name.take(1).uppercase().ifBlank { "👤" },
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                )
                if (user.mobile.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.mobile,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
                if (user.email.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Button(
                onClick = onStartChat,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Chat",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "चैट करें",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
