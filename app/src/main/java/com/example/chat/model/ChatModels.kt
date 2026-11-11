package com.example.chat.model

enum class ChatTransportType(val label: String, val badgeColorHex: Long) {
    INTERNET("ऑनलाइन (Internet)", 0xFF10B981),
    WIFI_LAN_P2P("बिना इंटरनेट (WiFi Hotspot)", 0xFF3B82F6),
    BLUETOOTH_P2P("बिना इंटरनेट (Bluetooth P2P)", 0xFF8B5CF6)
}

enum class MessageType {
    TEXT,
    IMAGE,
    VOICE_NOTE,
    CALL_LOG,
    SYSTEM
}

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString() + "_" + (1000..9999).random(),
    val contactId: String,
    val senderName: String,
    val senderPhone: String = "",
    val senderEmail: String = "",
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean = true,
    val isDelivered: Boolean = true,
    val isSeen: Boolean = true,
    val transportType: ChatTransportType = ChatTransportType.INTERNET,
    val messageType: MessageType = MessageType.TEXT,
    val mediaUri: String? = null,
    val durationSeconds: Int = 0,
    val isQueuedOffline: Boolean = false,
    val deliveryTime: Long? = null,
    val seenTime: Long? = null
)

data class ChatContact(
    val id: String,
    val name: String,
    val phoneOrId: String,
    val email: String = "",
    val roleTitle: String = "यूज़र",
    val avatarEmoji: String = "👤",
    val avatarBgColorHex: Long = 0xFF0D9488,
    val isOnline: Boolean = true,
    val isNearbyP2P: Boolean = false,
    val p2pConnectionType: String? = null, // "WIFI", "BLUETOOTH", null
    val isRegisteredUser: Boolean = true,
    val cityOrLocation: String = "सवाई माधोपुर",
    val lastMessage: String = "",
    val lastMessageTime: String = "अभी",
    val unreadCount: Int = 0,
    val peerIpAddress: String? = null,
    val pendingOutgoingCount: Int = 0,
    val lastMessageIsOutgoing: Boolean = false,
    val lastMessageIsDelivered: Boolean = true,
    val lastMessageIsSeen: Boolean = true,
    val lastMessageSenderName: String = "",
    val category: String = "CITIZEN" // "OFFICIAL", "HOTEL", "CITIZEN", "OFFLINE"
)

data class NearbyPeerDevice(
    val id: String,
    val name: String,
    val type: String, // "WIFI_HOTSPOT" or "BLUETOOTH"
    val addressOrIp: String,
    val signalStrength: String = "उत्कृष्ट (Strong)",
    val isConnected: Boolean = false
)

data class ChatCallState(
    val isInCall: Boolean = false,
    val isVideoCall: Boolean = false,
    val contactName: String = "",
    val contactAvatarEmoji: String = "👤",
    val contactRole: String = "",
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isVideoDisabled: Boolean = false,
    val isFrontCamera: Boolean = true,
    val statusText: String = "कॉल कनेक्ट हो रही है..."
)
