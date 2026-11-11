package com.example.chat.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chat.model.ChatContact

/**
 * Contact Us Directory View - Displays all contacts in a clean, categorized sequence
 * allowing immediate direct SMS, audio calls, and video calls to anyone.
 */
@Composable
fun ContactUsDirectoryView(
    contacts: List<ChatContact>,
    onSelectContact: (ChatContact) -> Unit,
    onStartCall: (ChatContact, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val filteredContacts = remember(contacts, searchQuery, selectedCategory) {
        contacts.filter { contact ->
            val matchesQuery = searchQuery.isBlank() ||
                    contact.name.contains(searchQuery, ignoreCase = true) ||
                    contact.phoneOrId.contains(searchQuery, ignoreCase = true) ||
                    contact.email.contains(searchQuery, ignoreCase = true) ||
                    contact.roleTitle.contains(searchQuery, ignoreCase = true) ||
                    contact.cityOrLocation.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategory) {
                "OFFICIAL" -> contact.category == "OFFICIAL" || contact.id.contains("helpdesk") || contact.id.contains("community")
                "HOTEL" -> contact.category == "HOTEL" || contact.id.contains("hotel")
                "CITIZEN" -> contact.category == "CITIZEN" || contact.isRegisteredUser
                "OFFLINE" -> contact.category == "OFFLINE" || contact.isNearbyP2P
                else -> true
            }

            matchesQuery && matchesCategory
        }
    }

    // Partition contacts into sequential groups for clear hierarchy
    val officialContacts = remember(filteredContacts) {
        filteredContacts.filter { it.category == "OFFICIAL" || it.id.contains("safari") || it.id.contains("community") }
    }
    val hotelContacts = remember(filteredContacts) {
        filteredContacts.filter { it.category == "HOTEL" || it.id.contains("hotel") }
    }
    val citizenContacts = remember(filteredContacts) {
        filteredContacts.filter { (it.category == "CITIZEN" || it.isRegisteredUser) && !it.id.contains("safari") && !it.id.contains("community") && !it.id.contains("hotel") }
    }
    val offlineContacts = remember(filteredContacts) {
        filteredContacts.filter { it.category == "OFFLINE" || it.isNearbyP2P }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B141A))
            .testTag("contact_us_directory_view")
    ) {
        // Top Header Banner
        Surface(
            color = Color(0xFF1F2C34),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "👥 Contact Us (संपर्क डायरेक्ट्री)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "किसी को भी डायरेक्ट SMS या कॉल करें • 100% फ्री सेवा",
                            fontSize = 12.sp,
                            color = Color(0xFF8696A0)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF25D366).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF25D366))
                    ) {
                        Text(
                            text = "${contacts.size} संपर्क",
                            color = Color(0xFF25D366),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "नाम, मोबाइल नं., ईमेल या विभाग खोजें...",
                            color = Color(0xFF8696A0),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF8696A0),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF8696A0),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A3942),
                        unfocusedContainerColor = Color(0xFF2A3942),
                        focusedBorderColor = Color(0xFF25D366),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("contact_directory_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val categories = listOf(
                        "ALL" to "सभी संपर्क",
                        "OFFICIAL" to "🏛️ हेल्पडेस्क",
                        "HOTEL" to "🏨 होटल व रूम",
                        "CITIZEN" to "👤 नागरिक",
                        "OFFLINE" to "📶 बिना इंटरनेट"
                    )
                    items(categories) { (key, label) ->
                        val isSelected = selectedCategory == key
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFF25D366) else Color(0xFF2A3942),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF3B4A54)),
                            modifier = Modifier.clickable { selectedCategory = key }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else Color(0xFFE9EDEF),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Contact List grouped in sequence
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (filteredContacts.isEmpty()) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp)
                    ) {
                        Text("🔍", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "कोई संपर्क नहीं मिला",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "कृपया दूसरा नाम या नंबर खोजें",
                            color = Color(0xFF8696A0),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Section 1: Official & Helpdesk Contacts
            if (officialContacts.isNotEmpty() && (selectedCategory == "ALL" || selectedCategory == "OFFICIAL")) {
                item {
                    SectionHeader(
                        title = "🏛️ आधिकारिक हेल्पडेस्क एवं आपातकालीन सेवाएं",
                        count = officialContacts.size,
                        color = Color(0xFFD97706)
                    )
                }
                items(officialContacts, key = { "off_${it.id}" }) { contact ->
                    ContactDirectoryCard(
                        contact = contact,
                        onDirectSms = { onSelectContact(contact) },
                        onAudioCall = { onStartCall(contact, false) },
                        onVideoCall = { onStartCall(contact, true) }
                    )
                }
            }

            // Section 2: Hotels & Accommodations
            if (hotelContacts.isNotEmpty() && (selectedCategory == "ALL" || selectedCategory == "HOTEL")) {
                item {
                    SectionHeader(
                        title = "🏨 होटल, रिसॉर्ट व आवास सेवाएं",
                        count = hotelContacts.size,
                        color = Color(0xFF2563EB)
                    )
                }
                items(hotelContacts, key = { "hot_${it.id}" }) { contact ->
                    ContactDirectoryCard(
                        contact = contact,
                        onDirectSms = { onSelectContact(contact) },
                        onAudioCall = { onStartCall(contact, false) },
                        onVideoCall = { onStartCall(contact, true) }
                    )
                }
            }

            // Section 3: Registered Citizens & Candidates
            if (citizenContacts.isNotEmpty() && (selectedCategory == "ALL" || selectedCategory == "CITIZEN")) {
                item {
                    SectionHeader(
                        title = "👤 पंजीकृत नागरिक व स्थानीय गाइड",
                        count = citizenContacts.size,
                        color = Color(0xFF0D9488)
                    )
                }
                items(citizenContacts, key = { "cit_${it.id}" }) { contact ->
                    ContactDirectoryCard(
                        contact = contact,
                        onDirectSms = { onSelectContact(contact) },
                        onAudioCall = { onStartCall(contact, false) },
                        onVideoCall = { onStartCall(contact, true) }
                    )
                }
            }

            // Section 4: Offline P2P Contacts
            if (offlineContacts.isNotEmpty() && (selectedCategory == "ALL" || selectedCategory == "OFFLINE")) {
                item {
                    SectionHeader(
                        title = "📶 बिना इंटरनेट आस-पास के यूज़र्स (WiFi / Bluetooth)",
                        count = offlineContacts.size,
                        color = Color(0xFF7C3AED)
                    )
                }
                items(offlineContacts, key = { "mesh_${it.id}" }) { contact ->
                    ContactDirectoryCard(
                        contact = contact,
                        onDirectSms = { onSelectContact(contact) },
                        onAudioCall = { onStartCall(contact, false) },
                        onVideoCall = { onStartCall(contact, true) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE9EDEF)
            )
        }
        Text(
            text = "$count",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8696A0)
        )
    }
}

@Composable
private fun ContactDirectoryCard(
    contact: ChatContact,
    onDirectSms: () -> Unit,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1F2C34),
        border = BorderStroke(
            1.dp,
            if (contact.unreadCount > 0) Color(0xFF25D366) else Color(0xFF2A3942)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDirectSms() }
            .testTag("contact_card_${contact.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Row: Avatar, Name, Status, and Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar with Status Dot
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(contact.avatarBgColorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = contact.avatarEmoji, fontSize = 22.sp)
                    }

                    // Online/Offline status dot
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(13.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1F2C34))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    when {
                                        contact.isOnline -> Color(0xFF25D366)
                                        contact.isNearbyP2P -> Color(0xFFA78BFA)
                                        else -> Color(0xFF94A3B8)
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name and Role
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = contact.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (contact.isRegisteredUser) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color(0xFF53BDEB),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Text(
                        text = contact.roleTitle,
                        color = Color(0xFF8696A0),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Unread Count Badge if any
                if (contact.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF25D366))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${contact.unreadCount} SMS",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details Row: Phone, Email, Location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (contact.phoneOrId.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color(0xFF8696A0),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = contact.phoneOrId,
                            color = Color(0xFFE9EDEF),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF8696A0),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = contact.cityOrLocation,
                        color = Color(0xFF8696A0),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: Direct SMS, Audio Call, Video Call
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Primary: Direct SMS Button
                Button(
                    onClick = onDirectSms,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("direct_sms_button_${contact.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "SMS",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "💬 डायरेक्ट SMS भेजें",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Audio Call Button
                IconButton(
                    onClick = onAudioCall,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A3942))
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Audio Call",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Video Call Button
                IconButton(
                    onClick = onVideoCall,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A3942))
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Video Call",
                        tint = Color(0xFF53BDEB),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
