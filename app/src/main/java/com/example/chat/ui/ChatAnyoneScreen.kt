package com.example.chat.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PermScanWifi
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chat.data.ChatRepository
import com.example.chat.model.ChatCallState
import com.example.chat.model.ChatContact
import com.example.chat.model.ChatMessage
import com.example.chat.model.ChatTransportType
import com.example.chat.model.MessageType
import com.example.chat.model.NearbyPeerDevice
import com.example.data.UserEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAnyoneScreen(
    repository: ChatRepository,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val contacts by repository.contacts.collectAsStateWithLifecycle()
    val activeContact by repository.activeContact.collectAsStateWithLifecycle()
    val messagesMap by repository.messages.collectAsStateWithLifecycle()
    val callState by repository.callState.collectAsStateWithLifecycle()

    val localIp by repository.offlineService.localIpAddress.collectAsStateWithLifecycle()
    val isServerActive by repository.offlineService.isServerActive.collectAsStateWithLifecycle()
    val nearbyPeers by repository.offlineService.nearbyPeers.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var isNewContactDialogOpen by remember { mutableStateOf(false) }

    val totalUnread = remember(contacts) { contacts.sumOf { it.unreadCount } }

    // Search query state for finding registered users & candidates
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var foundCandidates by remember { mutableStateOf<List<ChatContact>>(emptyList()) }
    var isSearchingCandidates by remember { mutableStateOf(false) }

    // Live search across database by name, mobile, or Gmail ID
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            isSearchingCandidates = true
            try {
                foundCandidates = repository.searchRegisteredCandidates(searchQuery)
            } catch (e: Exception) {
                foundCandidates = emptyList()
            } finally {
                isSearchingCandidates = false
            }
        } else {
            foundCandidates = emptyList()
            isSearchingCandidates = false
        }
    }

    // Filter contacts based on search query
    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) {
            contacts
        } else {
            contacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true) ||
                contact.phoneOrId.contains(searchQuery, ignoreCase = true) ||
                contact.roleTitle.contains(searchQuery, ignoreCase = true) ||
                contact.cityOrLocation.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Offline mode toggle
    var isOfflineModeActive by remember { mutableStateOf(false) }

    BackHandler {
        if (activeContact != null) {
            repository.selectContact(null)
        } else {
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B141A)) // WhatsApp Dark Background
            .testTag("chat_anyone_main_screen")
    ) {
        if (activeContact != null) {
            // Full 1-on-1 Active Chat Conversation View
            ActiveConversationScreen(
                contact = activeContact!!,
                messages = messagesMap[activeContact!!.id] ?: emptyList(),
                isOfflineMode = isOfflineModeActive,
                onBack = { repository.selectContact(null) },
                onSendMessage = { text, transport ->
                    repository.sendMessage(
                        contactId = activeContact!!.id,
                        text = text,
                        transportType = transport
                    )
                },
                onUpdateReachability = { isOnline, isNearby, p2pType ->
                    repository.updateContactReachability(
                        contactId = activeContact!!.id,
                        isOnline = isOnline,
                        isNearbyP2P = isNearby,
                        p2pType = p2pType
                    )
                },
                onSimulateRecipientSeen = {
                    repository.simulateRecipientSeen(activeContact!!.id)
                },
                onStartAudioCall = {
                    repository.startCall(activeContact!!, isVideo = false)
                },
                onStartVideoCall = {
                    repository.startCall(activeContact!!, isVideo = true)
                }
            )
        } else {
            // Home Screen: Top App Bar + Tabs (All Chats, Offline SMS, Calls)
            Scaffold(
                containerColor = Color(0xFF0B141A),
                topBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1F2C34)) // WhatsApp Bar Dark
                    ) {
                        TopAppBar(
                            title = {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Chat Anyone",
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF25D366))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "FREE",
                                                color = Color.Black,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                        if (totalUnread > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFF25D366),
                                                modifier = Modifier.clickable { selectedTab = 0 }
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.NotificationsActive,
                                                        contentDescription = null,
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "$totalUnread SMS",
                                                        color = Color.Black,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Text(
                                        text = "व्हाट्सएप चैट, वीडियो कॉलिंग व बिना इंटरनेट SMS",
                                        color = Color(0xFF8696A0),
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.testTag("chat_close_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "बंद करें",
                                        tint = Color.White
                                    )
                                }
                            },
                            actions = {
                                // Contact Us direct button
                                IconButton(
                                    onClick = { selectedTab = 1 },
                                    modifier = Modifier.testTag("chat_contact_us_top_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContactPhone,
                                        contentDescription = "Contact Us",
                                        tint = if (selectedTab == 1) Color(0xFF25D366) else Color.White
                                    )
                                }

                                // Search Icon Button for finding any registered user
                                IconButton(
                                    onClick = { isSearchActive = !isSearchActive },
                                    modifier = Modifier.testTag("chat_search_btn")
                                ) {
                                    Icon(
                                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                        contentDescription = "यूज़र खोजें",
                                        tint = Color.White
                                    )
                                }

                                // Toggle Online vs Offline P2P Mode
                                Surface(
                                    onClick = {
                                        isOfflineModeActive = !isOfflineModeActive
                                        if (isOfflineModeActive) {
                                            Toast.makeText(
                                                context,
                                                "बिना इंटरनेट मोड चालू: WiFi/Bluetooth P2P",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "ऑनलाइन मोड चालू (Internet)",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isOfflineModeActive) Color(0xFF7C3AED) else Color(0xFF005C4B),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isOfflineModeActive) Icons.Default.WifiTethering else Icons.Default.Wifi,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isOfflineModeActive) "बिना इंटरनेट" else "ऑनलाइन",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF1F2C34)
                            )
                        )

                        // Search bar below header if active
                        AnimatedVisibility(visible = isSearchActive) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1F2C34))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            text = "नाम, मोबाइल नंबर या Gmail ID से खोजें...",
                                            color = Color(0xFF8696A0),
                                            fontSize = 13.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = Color(0xFF25D366)
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "हटाएं",
                                                    tint = Color(0xFF8696A0),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("chat_user_search_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(24.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF121B22),
                                        unfocusedContainerColor = Color(0xFF121B22),
                                        focusedBorderColor = Color(0xFF25D366),
                                        unfocusedBorderColor = Color(0xFF2A3942)
                                    )
                                )
                            }
                        }

                        // 4 Primary Tabs (Chats with SMS badge, Contact Us directory, Offline SMS, Calls)
                        PrimaryTabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color(0xFF1F2C34),
                            contentColor = Color(0xFF25D366),
                            indicator = {
                                TabRowDefaults.PrimaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(selectedTab),
                                    color = Color(0xFF25D366),
                                    height = 3.dp
                                )
                            }
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (searchQuery.isNotBlank()) "खोज (${filteredContacts.size})" else "चैट्स",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (selectedTab == 0) Color(0xFF25D366) else Color(0xFF8696A0)
                                        )
                                        if (totalUnread > 0) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF25D366))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "$totalUnread",
                                                    color = Color.Black,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ContactPhone,
                                            contentDescription = null,
                                            tint = if (selectedTab == 1) Color(0xFF25D366) else Color(0xFF8696A0),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Contact Us",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (selectedTab == 1) Color(0xFF25D366) else Color(0xFF8696A0)
                                        )
                                    }
                                }
                            )
                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.WifiTethering,
                                            contentDescription = null,
                                            tint = if (selectedTab == 2) Color(0xFF25D366) else Color(0xFF8696A0),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "ऑफलाइन SMS",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (selectedTab == 2) Color(0xFF25D366) else Color(0xFF8696A0)
                                        )
                                    }
                                }
                            )
                            Tab(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                text = {
                                    Text(
                                        text = "कॉल्स",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (selectedTab == 3) Color(0xFF25D366) else Color(0xFF8696A0)
                                    )
                                }
                            )
                        }
                    }
                },
                floatingActionButton = {
                    when (selectedTab) {
                        0 -> {
                            FloatingActionButton(
                                onClick = { isNewContactDialogOpen = true },
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White,
                                modifier = Modifier.testTag("chat_fab_new_contact")
                            ) {
                                Icon(imageVector = Icons.Default.Chat, contentDescription = "नया चैट")
                            }
                        }
                        1 -> {
                            FloatingActionButton(
                                onClick = { isNewContactDialogOpen = true },
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White,
                                modifier = Modifier.testTag("chat_fab_new_contact_dir")
                            ) {
                                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "नया संपर्क")
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
                    when (selectedTab) {
                        0 -> {
                            // All Chats List / Search Results with Unread Notification Banner at the top
                            ChatListView(
                                contacts = filteredContacts,
                                registeredCandidates = foundCandidates,
                                isSearchingCandidates = isSearchingCandidates,
                                searchQuery = searchQuery,
                                totalUnread = totalUnread,
                                onClearSearch = { searchQuery = "" },
                                onContactClick = { contact ->
                                    repository.selectContact(contact)
                                },
                                onCandidateClick = { candidate ->
                                    repository.selectContact(candidate)
                                    Toast.makeText(context, "${candidate.name} के साथ चैट शुरू की गई", Toast.LENGTH_SHORT).show()
                                },
                                onAudioCall = { contact ->
                                    repository.startCall(contact, isVideo = false)
                                },
                                onVideoCall = { contact ->
                                    repository.startCall(contact, isVideo = true)
                                },
                                onOpenContactUs = { selectedTab = 1 },
                                onMarkAllRead = { repository.markAllAsRead() }
                            )
                        }
                        1 -> {
                            // Contact Us Directory View in clean, structured sequence
                            ContactUsDirectoryView(
                                contacts = contacts,
                                onSelectContact = { contact ->
                                    repository.selectContact(contact)
                                },
                                onStartCall = { contact, isVideo ->
                                    repository.startCall(contact, isVideo)
                                }
                            )
                        }
                        2 -> {
                            // Offline WiFi / Bluetooth P2P SMS Screen
                            OfflineP2PScreen(
                                localIp = localIp,
                                isServerActive = isServerActive,
                                nearbyPeers = nearbyPeers,
                                onRefreshPeers = {
                                    repository.offlineService.refreshNearbyPeers()
                                    Toast.makeText(context, "आस-पास के डिवाइस स्कैन किए जा रहे हैं...", Toast.LENGTH_SHORT).show()
                                },
                                onSendDirectOfflineMessage = { targetIp, text ->
                                    scope.launch {
                                        val success = repository.offlineService.sendMessageOverWifi(
                                            targetIp = targetIp,
                                            text = text,
                                            senderName = "सवाई माधोपुर यूज़र"
                                        )
                                        if (success) {
                                            Toast.makeText(context, "बिना इंटरनेट SMS सफलतापूर्वक डिलीवर हुआ! (0 Data)", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "मैसेज भेजा गया (लोकल मेश कतार में सुरक्षित)", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                        3 -> {
                            // Calls History / Quick Dial View
                            CallsHistoryView(
                                contacts = contacts,
                                onStartAudioCall = { contact ->
                                    repository.startCall(contact, isVideo = false)
                                },
                                onStartVideoCall = { contact ->
                                    repository.startCall(contact, isVideo = true)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Add New Contact Dialog
        if (isNewContactDialogOpen) {
            AddNewContactDialog(
                onDismiss = { isNewContactDialogOpen = false },
                onAdd = { name, phone, role, city, isRegistered ->
                    repository.addNewContact(
                        name = name,
                        phoneOrId = phone,
                        roleTitle = role,
                        cityOrLocation = city,
                        isRegisteredUser = isRegistered
                    )
                    isNewContactDialogOpen = false
                }
            )
        }

        // Full Screen Video/Audio Call Overlay if active
        if (callState.isInCall) {
            ChatCallScreen(
                callState = callState,
                onMuteToggle = { repository.toggleMute() },
                onSpeakerToggle = { repository.toggleSpeaker() },
                onVideoToggle = { repository.toggleVideo() },
                onFlipCamera = { repository.flipCamera() },
                onEndCall = { repository.endCall() }
            )
        }
    }
}

/**
 * 1-on-1 WhatsApp Active Chat Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveConversationScreen(
    contact: ChatContact,
    messages: List<ChatMessage>,
    isOfflineMode: Boolean,
    onBack: () -> Unit,
    onSendMessage: (String, ChatTransportType) -> Unit,
    onUpdateReachability: (isOnline: Boolean, isNearbyP2P: Boolean, p2pType: String?) -> Unit = { _, _, _ -> },
    onSimulateRecipientSeen: () -> Unit = {},
    onStartAudioCall: () -> Unit,
    onStartVideoCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var selectedTransport by remember {
        mutableStateOf(if (isOfflineMode) ChatTransportType.WIFI_LAN_P2P else ChatTransportType.INTERNET)
    }
    var showEmojiPicker by remember { mutableStateOf(false) }
    val popularEmojis = remember { listOf("😊", "👍", "❤️", "🐅", "🙏", "🔥", "🎉", "✈️", "🏨", "📍", "🤝", "✅") }
    val context = LocalContext.current

    LaunchedEffect(isOfflineMode) {
        selectedTransport = if (isOfflineMode) ChatTransportType.WIFI_LAN_P2P else ChatTransportType.INTERNET
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = Color(0xFF0B141A),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(contact.avatarBgColorHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = contact.avatarEmoji, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = contact.name,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (contact.isRegisteredUser) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "रजिस्टर्ड यूजर",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                contact.isOnline -> Color(0xFF25D366)
                                                contact.isNearbyP2P -> Color(0xFF38BDF8)
                                                else -> Color(0xFFEF4444)
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when {
                                        selectedTransport == ChatTransportType.WIFI_LAN_P2P -> "📶 WiFi Direct P2P (0 डेटा)"
                                        selectedTransport == ChatTransportType.BLUETOOTH_P2P -> "📡 ब्लूटूथ मेश (0 डेटा)"
                                        contact.isOnline -> "🟢 ऑनलाइन • ${contact.cityOrLocation}"
                                        contact.isNearbyP2P -> "📶 WiFi/BT रेंज में • ${contact.cityOrLocation}"
                                        else -> "🔴 ऑफ़लाइन (SMS कतार में सुरक्षित) • ${contact.cityOrLocation}"
                                    },
                                    color = when {
                                        contact.isOnline -> Color(0xFF25D366)
                                        contact.isNearbyP2P -> Color(0xFF38BDF8)
                                        else -> Color(0xFFEF4444)
                                    },
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "वापस",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Video Call Button
                    IconButton(
                        onClick = onStartVideoCall,
                        modifier = Modifier.testTag("chat_btn_video_call")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "वीडियो कॉल",
                            tint = Color(0xFF25D366)
                        )
                    }
                    // Audio Call Button
                    IconButton(
                        onClick = onStartAudioCall,
                        modifier = Modifier.testTag("chat_btn_audio_call")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "ऑडियो कॉल",
                            tint = Color(0xFF25D366)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1F2C34))
            )
        },
        bottomBar = {
            val density = LocalDensity.current
            val isImeVisible = WindowInsets.ime.getBottom(density) > 0
            // Lift text box ~110.dp (~3.5-4 cm) above navigation buttons when keyboard is closed
            // and adapt to 10.dp when keyboard is open with imePadding()
            val bottomClearance = if (isImeVisible) 10.dp else 110.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F2C34))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .imePadding()
                    .padding(bottom = bottomClearance)
            ) {
                // Transport Mode Selector Bar (Internet, WiFi Direct P2P, Bluetooth Mesh)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "माध्यम (Channel):",
                        color = Color(0xFF8696A0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Internet option
                        Surface(
                            onClick = { selectedTransport = ChatTransportType.INTERNET },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedTransport == ChatTransportType.INTERNET) Color(0xFF00A884) else Color(0xFF2A3942),
                            border = BorderStroke(1.dp, if (selectedTransport == ChatTransportType.INTERNET) Color(0xFF25D366) else Color.Transparent)
                        ) {
                            Text(
                                text = "🌐 इंटरनेट",
                                color = if (selectedTransport == ChatTransportType.INTERNET) Color.White else Color(0xFF8696A0),
                                fontSize = 11.sp,
                                fontWeight = if (selectedTransport == ChatTransportType.INTERNET) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        // WiFi P2P option
                        Surface(
                            onClick = { selectedTransport = ChatTransportType.WIFI_LAN_P2P },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedTransport == ChatTransportType.WIFI_LAN_P2P) Color(0xFF0284C7) else Color(0xFF2A3942),
                            border = BorderStroke(1.dp, if (selectedTransport == ChatTransportType.WIFI_LAN_P2P) Color(0xFF38BDF8) else Color.Transparent)
                        ) {
                            Text(
                                text = "📶 WiFi P2P",
                                color = if (selectedTransport == ChatTransportType.WIFI_LAN_P2P) Color.White else Color(0xFF8696A0),
                                fontSize = 11.sp,
                                fontWeight = if (selectedTransport == ChatTransportType.WIFI_LAN_P2P) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        // Bluetooth P2P option
                        Surface(
                            onClick = { selectedTransport = ChatTransportType.BLUETOOTH_P2P },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedTransport == ChatTransportType.BLUETOOTH_P2P) Color(0xFF6366F1) else Color(0xFF2A3942),
                            border = BorderStroke(1.dp, if (selectedTransport == ChatTransportType.BLUETOOTH_P2P) Color(0xFF818CF8) else Color.Transparent)
                        ) {
                            Text(
                                text = "📡 ब्लूटूथ",
                                color = if (selectedTransport == ChatTransportType.BLUETOOTH_P2P) Color.White else Color(0xFF8696A0),
                                fontSize = 11.sp,
                                fontWeight = if (selectedTransport == ChatTransportType.BLUETOOTH_P2P) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Quick Suggestion Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val suggestions = listOf(
                        "🐅 सफारी बुकिंग का क्या रेट है?",
                        "🏨 रूम की उपलब्धता बताएं",
                        "📶 बिना इंटरनेट टेस्ट SMS",
                        "📍 रणथंभौर फोर्ट टाइमिंग",
                        "👋 नमस्ते, कैसे हैं आप?"
                    )
                    for (chip in suggestions) {
                        Surface(
                            onClick = {
                                onSendMessage(chip, selectedTransport)
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF2A3942),
                            border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = chip,
                                color = Color(0xFFE9EDEF),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Emoji Quick Picker Row (Toggleable)
                AnimatedVisibility(visible = showEmojiPicker) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .background(Color(0xFF202C33))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (emoji in popularEmojis) {
                            Text(
                                text = emoji,
                                fontSize = 22.sp,
                                modifier = Modifier
                                    .clickable { inputText += emoji }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                // WhatsApp Style Input Bar with Custom Text Box & Send Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // WhatsApp-like pill container for Emoji + Custom Input Box
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 50.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF2A3942))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji Toggle Button
                        IconButton(
                            onClick = { showEmojiPicker = !showEmojiPicker },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SentimentSatisfiedAlt,
                                contentDescription = "इमोजी",
                                tint = if (showEmojiPicker) Color(0xFF25D366) else Color(0xFF8696A0),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Fully custom free-form text input field (type whatever user wants)
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    text = when (selectedTransport) {
                                        ChatTransportType.WIFI_LAN_P2P -> "बिना इंटरनेट SMS (WiFi P2P)..."
                                        ChatTransportType.BLUETOOTH_P2P -> "बिना सिम SMS (Bluetooth)..."
                                        else -> "मैसेज लिखें... (WhatsApp Style)"
                                    },
                                    color = Color(0xFF8696A0),
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_textfield"),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            maxLines = 4
                        )

                        // Clear or quick action
                        if (inputText.isNotEmpty()) {
                            IconButton(
                                onClick = { inputText = "" },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "साफ़ करें",
                                    tint = Color(0xFF8696A0),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    if (inputText.isNotBlank()) {
                        // WhatsApp style Circular Send Button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    when (selectedTransport) {
                                        ChatTransportType.WIFI_LAN_P2P -> Color(0xFF0284C7)
                                        ChatTransportType.BLUETOOTH_P2P -> Color(0xFF6366F1)
                                        else -> Color(0xFF25D366)
                                    }
                                )
                                .clickable {
                                    val textToSend = inputText.trim()
                                    if (textToSend.isNotEmpty()) {
                                        onSendMessage(textToSend, selectedTransport)
                                        inputText = ""
                                        showEmojiPicker = false
                                    }
                                }
                                .testTag("chat_btn_send"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "भेजें",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        // Voice Note Simulation Button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00A884))
                                .clickable {
                                    Toast.makeText(context, "वॉयस नोट भेजा गया (00:04s)", Toast.LENGTH_SHORT).show()
                                    onSendMessage("🎤 [ऑडियो संदेश 00:04s]", selectedTransport)
                                }
                                .testTag("chat_btn_mic"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "वॉयस नोट",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Recipient Connectivity & Delivery Control Panel
            item {
                val pendingCount = messages.count { it.isOutgoing && (!it.isDelivered || it.isQueuedOffline) }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF182229),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                contact.isOnline -> Color(0xFF25D366)
                                                contact.isNearbyP2P -> Color(0xFF38BDF8)
                                                else -> Color(0xFFEF4444)
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        contact.isOnline -> "🟢 ${contact.name} ऑनलाइन है"
                                        contact.isNearbyP2P -> "📶 ${contact.name} WiFi/BT रेंज में है"
                                        else -> "🔴 ${contact.name} ऑफ़लाइन है"
                                    },
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (pendingCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B))
                                ) {
                                    Text(
                                        text = "⏳ $pendingCount SMS कतार में",
                                        color = Color(0xFFF59E0B),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "यूज़र कनेक्टिविटी सिम्युलेटर (डिलीवरी और डबल टिक टेस्ट):",
                            color = Color(0xFF8696A0),
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Set Offline
                            Surface(
                                onClick = {
                                    onUpdateReachability(false, false, null)
                                    Toast.makeText(context, "${contact.name} अब ऑफ़लाइन है। भेजे गए SMS कतार में रहेंगे।", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (!contact.isOnline && !contact.isNearbyP2P) Color(0xFFEF4444).copy(alpha = 0.25f) else Color(0xFF2A3942),
                                border = BorderStroke(1.dp, if (!contact.isOnline && !contact.isNearbyP2P) Color(0xFFEF4444) else Color.Transparent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "🔴 ऑफ़लाइन",
                                    color = if (!contact.isOnline && !contact.isNearbyP2P) Color(0xFFFCA5A5) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }

                            // Bring Online -> Deliver Pending
                            Surface(
                                onClick = {
                                    onUpdateReachability(true, false, null)
                                    Toast.makeText(context, "🟢 ${contact.name} ऑनलाइन आया! सभी लंबित SMS डिलीवर हो गए।", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (contact.isOnline) Color(0xFF00A884).copy(alpha = 0.25f) else Color(0xFF2A3942),
                                border = BorderStroke(1.dp, if (contact.isOnline) Color(0xFF25D366) else Color.Transparent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "🟢 ऑनलाइन",
                                    color = if (contact.isOnline) Color(0xFF86EFAC) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }

                            // Bring in WiFi / Bluetooth range -> Deliver Pending
                            Surface(
                                onClick = {
                                    onUpdateReachability(false, true, "WIFI")
                                    Toast.makeText(context, "📶 ${contact.name} WiFi/BT रेंज में आया! SMS P2P से डिलीवर हुए।", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (contact.isNearbyP2P) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF2A3942),
                                border = BorderStroke(1.dp, if (contact.isNearbyP2P) Color(0xFF38BDF8) else Color.Transparent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "📶 WiFi/BT रेंज",
                                    color = if (contact.isNearbyP2P) Color(0xFF7DD3FC) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Seen action: double tick turns blue!
                        Surface(
                            onClick = {
                                onSimulateRecipientSeen()
                                Toast.makeText(context, "✓✓ ${contact.name} ने SMS देख लिया! डबल टिक नीला (Blue) हो गया।", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF005C4B),
                            border = BorderStroke(1.dp, Color(0xFF53BDEB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    tint = Color(0xFF53BDEB),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "👁️ SMS देखते ही डबल टिक ब्लू (✓✓) करें",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Mode Banner
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isOfflineMode) Color(0xFF4C1D95) else Color(0xFF182229),
                    border = BorderStroke(1.dp, if (isOfflineMode) Color(0xFFA78BFA) else Color(0xFF005C4B))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isOfflineMode) Icons.Default.WifiTethering else Icons.Default.Wifi,
                            contentDescription = null,
                            tint = if (isOfflineMode) Color(0xFFC4B5FD) else Color(0xFF25D366),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOfflineMode) "बिना इंटरनेट मोड चालू है: संदेश सीधे लोकल हॉटस्पॉट/ब्लूटूथ द्वारा भेजे जाएंगे।" else "व्हाट्सएप फ्री चैट: एंड-टू-एंड एन्क्रिप्टेड संदेश व फ्री कॉलिंग।",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                MessageBubble(message = msg)
            }
        }
    }
}

/**
 * Single Message Bubble with WhatsApp Styling
 */
@Composable
fun MessageBubble(message: ChatMessage) {
    val isOutgoing = message.isOutgoing
    val alignment = if (isOutgoing) Alignment.End else Alignment.Start
    val bubbleBg = if (isOutgoing) Color(0xFF005C4B) else Color(0xFF202C33)

    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (message.messageType == MessageType.CALL_LOG) {
            // Call Log Pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF182229),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = message.text,
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        } else {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (isOutgoing) 14.dp else 2.dp,
                    bottomEnd = if (isOutgoing) 2.dp else 14.dp
                ),
                color = bubbleBg,
                shadowElevation = 2.dp,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (!isOutgoing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = message.senderName,
                                color = Color(0xFF25D366),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (message.senderPhone.isNotBlank()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${message.senderPhone})",
                                    color = Color(0xFF8696A0),
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Text(
                        text = message.text,
                        color = Color(0xFFE9EDEF),
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        // Transport Badge
                        if (message.transportType != ChatTransportType.INTERNET) {
                            Text(
                                text = when (message.transportType) {
                                    ChatTransportType.WIFI_LAN_P2P -> "📶 WiFi P2P"
                                    ChatTransportType.BLUETOOTH_P2P -> "📡 ब्लूटूथ"
                                    else -> "📶 P2P"
                                },
                                color = Color(0xFF38BDF8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Text(
                            text = timeFormatted,
                            color = Color(0xFF8696A0),
                            fontSize = 10.sp
                        )

                        if (isOutgoing) {
                            Spacer(modifier = Modifier.width(4.dp))
                            when {
                                !message.isDelivered || message.isQueuedOffline -> {
                                    // Pending in offline queue
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "कतार में",
                                            color = Color(0xFFF59E0B),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = "लंबित (कतार में)",
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                                !message.isSeen -> {
                                    // Delivered to recipient, not viewed yet: Double Gray Tick
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "डिलीवर हो गया (पहुंच गया)",
                                        tint = Color(0xFF8696A0),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                else -> {
                                    // Seen by recipient: Double Blue Tick!
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "देखा गया (डबल टिक ब्लू)",
                                        tint = Color(0xFF53BDEB), // WhatsApp Cyan Blue
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modern notification banner pinned at the top when receiver has incoming SMS
 * Shows exact notification: "🔔 7 नए SMS प्राप्त हुए!", with sender chips and "सब पढ़ें ✓✓" action
 */
@Composable
fun UnreadSmsNotificationBanner(
    totalUnread: Int,
    contacts: List<ChatContact>,
    onSelectContact: (ChatContact) -> Unit,
    onMarkAllRead: () -> Unit
) {
    val unreadContacts = remember(contacts) { contacts.filter { it.unreadCount > 0 } }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF003B2E), // Rich WhatsApp Green
        border = BorderStroke(1.5.dp, Color(0xFF25D366)),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("chat_unread_notification_banner")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .background(Color(0xFF25D366)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "🔔 $totalUnread नए SMS प्राप्त हुए!",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${unreadContacts.size} संपर्कों ने आपको SMS भेजा है",
                            color = Color(0xFF25D366),
                            fontSize = 11.sp
                        )
                    }
                }

                // Quick Mark All As Read
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1F2C34),
                    border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { onMarkAllRead() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "सब पढ़ें",
                            color = Color(0xFF25D366),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Unread sender quick chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(unreadContacts, key = { "banner_${it.id}" }) { contact ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1F2C34),
                        border = BorderStroke(1.dp, Color(0xFF25D366)),
                        modifier = Modifier.clickable { onSelectContact(contact) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = contact.avatarEmoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = contact.name.split(" ").firstOrNull() ?: contact.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF25D366))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "${contact.unreadCount}",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
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
 * Contact list view with user search support, verified registration badges, and offline tags
 */
@Composable
fun ChatListView(
    contacts: List<ChatContact>,
    registeredCandidates: List<ChatContact> = emptyList(),
    isSearchingCandidates: Boolean = false,
    searchQuery: String = "",
    totalUnread: Int = 0,
    onClearSearch: () -> Unit = {},
    onContactClick: (ChatContact) -> Unit,
    onCandidateClick: (ChatContact) -> Unit = {},
    onAudioCall: (ChatContact) -> Unit,
    onVideoCall: (ChatContact) -> Unit,
    onOpenContactUs: () -> Unit = {},
    onMarkAllRead: () -> Unit = {}
) {
    val totalFound = contacts.size + registeredCandidates.size

    // Sort contacts: receiver sees unread incoming SMS at the top, in sequence!
    val sortedContacts = remember(contacts) {
        contacts.sortedWith(
            compareByDescending<ChatContact> { it.unreadCount > 0 }
                .thenByDescending { it.unreadCount }
        )
    }

    if (contacts.isEmpty() && registeredCandidates.isEmpty() && !isSearchingCandidates) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1F2C34)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = Color(0xFF8696A0),
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (searchQuery.isNotBlank()) "कोई कैंडिडेट या यूज़र नहीं मिला" else "कोई चैट उपलब्ध नहीं है",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (searchQuery.isNotBlank())
                        "\"$searchQuery\" नाम, मोबाइल नंबर या Gmail ID से कोई खाता नहीं मिला। सही जानकारी दर्ज करें।"
                    else
                        "नया चैट शुरू करने के लिए नीचे दिए गए + बटन पर टैप करें या ऊपर सर्च करें।",
                    color = Color(0xFF8696A0),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                if (searchQuery.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onClearSearch,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Text("सभी चैट्स देखें", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // 1. TOP NOTIFICATION BANNER IF UNREAD MESSAGES EXIST
            if (totalUnread > 0 && searchQuery.isBlank()) {
                item {
                    UnreadSmsNotificationBanner(
                        totalUnread = totalUnread,
                        contacts = contacts,
                        onSelectContact = onContactClick,
                        onMarkAllRead = onMarkAllRead
                    )
                }
            }

            // 2. QUICK SHORTCUT BAR (Contact Us Directory access & status)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1F2C34),
                        border = BorderStroke(1.dp, Color(0xFF25D366)),
                        modifier = Modifier.clickable { onOpenContactUs() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "👥 Contact Us सूची (डायरेक्ट SMS)",
                                color = Color(0xFF25D366),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (totalUnread > 0) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF25D366).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF25D366))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color(0xFF25D366),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$totalUnread नए SMS",
                                    color = Color(0xFF25D366),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Search banner if filtering
            if (searchQuery.isNotBlank()) {
                item {
                    Surface(
                        color = Color(0xFF1F2C34),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🔍 खोज: \"$searchQuery\"",
                                    color = Color(0xFF25D366),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "($totalFound मिले)",
                                    color = Color(0xFF8696A0),
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "हटाएं",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { onClearSearch() }
                            )
                        }
                    }
                }
            }

            // REGISTERED CANDIDATES SECTION (Search by name, mobile number, or Gmail ID)
            if (registeredCandidates.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF005C4B).copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF25D366),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "रजिस्टर्ड कैंडिडेट्स व यूज़र्स (${registeredCandidates.size}) - फ्री SMS व चैट",
                                    color = Color(0xFF25D366),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                items(registeredCandidates, key = { "candidate_${it.id}" }) { candidate ->
                    CandidateChatRow(
                        candidate = candidate,
                        onClick = { onCandidateClick(candidate) }
                    )
                }

                if (sortedContacts.isNotEmpty()) {
                    item {
                        Text(
                            text = "मौजूदा बातचीत (Recent Chats)",
                            color = Color(0xFF8696A0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // RENDER ALL CONTACTS - SORTED WITH UNREAD INCOMING SMS AT TOP
            items(sortedContacts, key = { it.id }) { contact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (contact.unreadCount > 0) Color(0xFF005C4B).copy(alpha = 0.12f)
                            else Color.Transparent
                        )
                        .clickable { onContactClick(contact) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar with online / P2P / offline status dot
                    Box(modifier = Modifier.size(50.dp)) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(contact.avatarBgColorHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = contact.avatarEmoji, fontSize = 24.sp)
                        }

                        // Status dot at bottom right
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        contact.isOnline -> Color(0xFF25D366)
                                        contact.isNearbyP2P -> Color(0xFF38BDF8)
                                        else -> Color(0xFFEF4444)
                                    }
                                )
                                .border(2.dp, Color(0xFF0B141A), CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Name, badges & subtitle
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(
                                    text = contact.name,
                                    color = Color(0xFFE9EDEF),
                                    fontSize = 15.sp,
                                    fontWeight = if (contact.unreadCount > 0) FontWeight.ExtraBold else FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (contact.isRegisteredUser) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "रजिस्टर्ड यूज़र",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Text(
                                text = contact.lastMessageTime,
                                color = if (contact.unreadCount > 0) Color(0xFF25D366) else Color(0xFF8696A0),
                                fontSize = 12.sp,
                                fontWeight = if (contact.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        // Category / Location Info tag + Status indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when {
                                    contact.isOnline -> "🟢 ऑनलाइन"
                                    contact.isNearbyP2P -> "📶 WiFi/BT रेंज"
                                    else -> "🔴 ऑफ़लाइन"
                                },
                                color = when {
                                    contact.isOnline -> Color(0xFF25D366)
                                    contact.isNearbyP2P -> Color(0xFF38BDF8)
                                    else -> Color(0xFFEF4444)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "• ${contact.roleTitle}",
                                color = Color(0xFF8696A0),
                                fontSize = 11.sp
                            )
                            if (contact.cityOrLocation.isNotBlank()) {
                                Text(
                                    text = " • ${contact.cityOrLocation}",
                                    color = Color(0xFF8696A0),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Modern Messenger preview: clearly shows WHO sent message + tick indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (contact.lastMessageIsOutgoing) {
                                    Icon(
                                        imageVector = when {
                                            contact.pendingOutgoingCount > 0 -> Icons.Default.Schedule
                                            contact.lastMessageIsSeen -> Icons.Default.DoneAll
                                            contact.lastMessageIsDelivered -> Icons.Default.DoneAll
                                            else -> Icons.Default.Done
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            contact.pendingOutgoingCount > 0 -> Color(0xFFF59E0B)
                                            contact.lastMessageIsSeen -> Color(0xFF53BDEB)
                                            else -> Color(0xFF8696A0)
                                        },
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "आप: ${contact.lastMessage}",
                                        color = Color(0xFF8696A0),
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    val sender = contact.lastMessageSenderName.ifBlank {
                                        contact.name.split(" ").firstOrNull() ?: contact.name
                                    }
                                    Text(
                                        text = "👤 $sender: ${contact.lastMessage}",
                                        color = if (contact.unreadCount > 0) Color(0xFFE9EDEF) else Color(0xFF8696A0),
                                        fontWeight = if (contact.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (contact.pendingOutgoingCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = "⏳ ${contact.pendingOutgoingCount} कतार में",
                                        color = Color(0xFFF59E0B),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Notification badge showing exact number of unread SMS (e.g. 7 SMS)
                            if (contact.unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0xFF25D366))
                                        .padding(horizontal = 7.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${contact.unreadCount}",
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    // Direct Call Shortcuts
                    IconButton(onClick = { onAudioCall(contact) }) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "कॉल",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { onVideoCall(contact) }) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "वीडियो कॉल",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.06f),
                    modifier = Modifier.padding(start = 78.dp)
                )
            }
        }
    }
}

/**
 * Candidate search result row allowing instant 1-tap Free SMS / Chat
 * Searchable by Full Name, Mobile Number, or Gmail ID
 */
@Composable
fun CandidateChatRow(
    candidate: ChatContact,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(candidate.avatarBgColorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = candidate.avatarEmoji.ifBlank { "👤" },
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = candidate.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "रजिस्टर्ड कैंडिडेट",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                val infoList = listOfNotNull(
                    candidate.phoneOrId.takeIf { it.isNotBlank() },
                    candidate.email.takeIf { it.isNotBlank() }
                )
                if (infoList.isNotEmpty()) {
                    Text(
                        text = infoList.joinToString(" • "),
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "${candidate.cityOrLocation} • फ्री SMS व चैट",
                    color = Color(0xFF8696A0),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SMS करें",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Offline WiFi / Bluetooth P2P SMS Screen
 * Allows users to send direct messages without Internet by using local Hotspot / WiFi Socket & Bluetooth
 */
@Composable
fun OfflineP2PScreen(
    localIp: String,
    isServerActive: Boolean,
    nearbyPeers: List<NearbyPeerDevice>,
    onRefreshPeers: () -> Unit,
    onSendDirectOfflineMessage: (String, String) -> Unit
) {
    var targetIpInput by remember { mutableStateOf("192.168.43.102") }
    var offlineMessageText by remember { mutableStateOf("नमस्ते! यह बिना इंटरनेट WiFi/Bluetooth से भेजा गया 100% फ्री SMS है।") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card: Real Local IP and Server Socket
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2C34)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, Color(0xFF25D366).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiTethering,
                            contentDescription = null,
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "बिना इंटरनेट (Offline P2P SMS) इंजन",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isServerActive) "🟢 लोकल सॉकेट सर्वर चालू (Port 8988)" else "लोकल सर्वर लोड हो रहा है...",
                            color = Color(0xFF4ADE80),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "आपका डिवाइस लोकल IP:", color = Color(0xFF8696A0), fontSize = 11.sp)
                        Text(text = localIp, color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "डेटा खपत (Internet Usage):", color = Color(0xFF8696A0), fontSize = 11.sp)
                        Text(text = "0 MB (100% मुफ़्त)", color = Color(0xFF25D366), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Direct Offline SMS Sender Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF182229)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "बिना इंटरनेट सीधा SMS भेजें (Send Offline SMS)",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "हॉटस्पॉट या वाईफाई से जुड़े दूसरे फोन का IP दर्ज करें",
                    color = Color(0xFF8696A0),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetIpInput,
                    onValueChange = { targetIpInput = it },
                    label = { Text("Peer IP / Address (e.g. 192.168.43.102)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF8696A0)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = offlineMessageText,
                    onValueChange = { offlineMessageText = it },
                    label = { Text("संदेश (Message Text)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF25D366),
                        unfocusedBorderColor = Color(0xFF8696A0)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (targetIpInput.isNotBlank() && offlineMessageText.isNotBlank()) {
                            onSendDirectOfflineMessage(targetIpInput.trim(), offlineMessageText.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "बिना इंटरनेट SMS भेजें (Send Offline)",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Nearby Devices Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "आस-पास के डिवाइस (Nearby WiFi & BT Peers)",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onRefreshPeers) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color(0xFF25D366),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "स्कैन करें", color = Color(0xFF25D366), fontSize = 12.sp)
            }
        }

        nearbyPeers.forEach { peer ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2C34)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (peer.type == "WIFI_HOTSPOT") Color(0xFF0284C7) else Color(0xFF7C3AED)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (peer.type == "WIFI_HOTSPOT") Icons.Default.Wifi else Icons.Default.Bluetooth,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = peer.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${peer.addressOrIp} • ${peer.signalStrength}",
                            color = Color(0xFF8696A0),
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            targetIpInput = peer.addressOrIp
                            Toast.makeText(context, "${peer.name} चुना गया", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A3942)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "चुनें", color = Color(0xFF25D366), fontSize = 12.sp)
                    }
                }
            }
        }

        // Helpful Step-by-Step Hindi Guide
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF132028)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💡 बिना इंटरनेट कैसे बात करें? (3 आसान स्टेप्स):",
                    color = Color(0xFFFEF08A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. एक फोन में पर्सनल हॉटस्पॉट ऑन करें (मोबाइल इंटरनेट डेटा बंद भी रख सकते हैं)।",
                    color = Color(0xFFE9EDEF),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "2. दूसरे फोन को उस हॉटस्पॉट वाई-फाई से कनेक्ट करें।",
                    color = Color(0xFFE9EDEF),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "3. दोनों फोन में सवाई माधोपुर ऐप खोलकर 'Chat Anyone' में जाएं — बिना किसी सिम या इंटरनेट बैलेंस के 100% फ्री में अनलिमिटेड चैट व SMS करें!",
                    color = Color(0xFFE9EDEF),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Calls History & Quick Audio/Video dialer view
 */
@Composable
fun CallsHistoryView(
    contacts: List<ChatContact>,
    onStartAudioCall: (ChatContact) -> Unit,
    onStartVideoCall: (ChatContact) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "व्हाट्सएप फ्री कॉलिंग (Free Audio & Video Calls)",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "किसी भी संपर्क को एक क्लिक में HD ऑडियो या वीडियो कॉल करें",
                color = Color(0xFF8696A0),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(contacts, key = { it.id }) { contact ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2C34)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(contact.avatarBgColorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = contact.avatarEmoji, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contact.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${contact.roleTitle} • ${contact.phoneOrId}",
                            color = Color(0xFF8696A0),
                            fontSize = 11.sp
                        )
                    }

                    // Audio Call
                    IconButton(
                        onClick = { onStartAudioCall(contact) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366).copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Audio Call",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Video Call
                    IconButton(
                        onClick = { onStartVideoCall(contact) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Add New Contact Dialog with City and Registered User options
 */
@Composable
fun AddNewContactDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phoneOrId by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("नागरिक / पर्यटक") }
    var city by remember { mutableStateOf("सवाई माधोपुर") }
    var isRegistered by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F2C34),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = Color(0xFF25D366),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "नया चैट संपर्क / यूज़र जोड़ें",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("यूज़र का नाम (Full Name)") },
                    placeholder = { Text("उदा. राहुल शर्मा, अमित मीणा") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF25D366),
                        unfocusedBorderColor = Color(0xFF8696A0)
                    )
                )

                OutlinedTextField(
                    value = phoneOrId,
                    onValueChange = { phoneOrId = it },
                    label = { Text("मोबाइल नंबर या यूज़र ID") },
                    placeholder = { Text("+91 9876543210 या IP") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF25D366),
                        unfocusedBorderColor = Color(0xFF8696A0)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("शहर / स्थान") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF25D366),
                            unfocusedBorderColor = Color(0xFF8696A0)
                        )
                    )

                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("श्रेणी") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF25D366),
                            unfocusedBorderColor = Color(0xFF8696A0)
                        )
                    )
                }

                // Is Registered User Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF121B22))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ऐप रजिस्टर्ड सदस्य ID",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "यदि यूज़र ने ऐप में आईडी बनाई है",
                            color = Color(0xFF8696A0),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isRegistered,
                        onCheckedChange = { isRegistered = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF25D366)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name.trim(), phoneOrId.trim(), role.trim(), city.trim(), isRegistered)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Text("चैट शुरू करें", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें", color = Color(0xFF8696A0))
            }
        }
    )
}
