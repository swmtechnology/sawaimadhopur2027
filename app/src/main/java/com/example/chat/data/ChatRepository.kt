package com.example.chat.data

import android.content.Context
import android.content.SharedPreferences
import com.example.chat.model.ChatCallState
import com.example.chat.model.ChatContact
import com.example.chat.model.ChatMessage
import com.example.chat.model.ChatTransportType
import com.example.chat.model.MessageType
import com.example.chat.p2p.OfflineP2PService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ChatRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("swm_chat_anyone_prefs", Context.MODE_PRIVATE)

    val offlineService = OfflineP2PService.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private var callTimerJob: Job? = null

    // Contacts
    private val _contacts = MutableStateFlow<List<ChatContact>>(emptyList())
    val contacts: StateFlow<List<ChatContact>> = _contacts.asStateFlow()

    // Active Contact ID
    private val _activeContact = MutableStateFlow<ChatContact?>(null)
    val activeContact: StateFlow<ChatContact?> = _activeContact.asStateFlow()

    // Messages mapped by contact ID
    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    // Call State
    private val _callState = MutableStateFlow(ChatCallState())
    val callState: StateFlow<ChatCallState> = _callState.asStateFlow()

    init {
        loadInitialContacts()
        loadPersistedMessages()

        offlineService.onMessageReceivedListener = { incomingMsg ->
            scope.launch {
                receiveIncomingMessage(incomingMsg)
            }
        }
    }

    private fun loadInitialContacts() {
        val initialList = listOf(
            ChatContact(
                id = "safari_helpdesk",
                name = "रणथंभौर सफारी हेल्पडेस्क (आधिकारिक)",
                phoneOrId = "+91 6367094588",
                email = "safari.swm@gmail.com",
                roleTitle = "सफारी व कैंटर बुकिंग गाइड",
                avatarEmoji = "🐅",
                avatarBgColorHex = 0xFFD97706,
                isOnline = true,
                isRegisteredUser = true,
                cityOrLocation = "रणथंभौर, सवाई माधोपुर",
                lastMessage = "क्या आप कल सुबह की शिफ्ट में जोन 1-5 के लिए जिप्सी/कैंटर टिकट बुक करना चाहते हैं?",
                lastMessageTime = "10:30 AM",
                unreadCount = 2,
                lastMessageIsOutgoing = false,
                lastMessageIsDelivered = true,
                lastMessageIsSeen = false,
                lastMessageSenderName = "सफारी हेल्पडेस्क",
                category = "OFFICIAL"
            ),
            ChatContact(
                id = "hotel_helpdesk",
                name = "सवाई माधोपुर होटल व रूम बुकिंग",
                phoneOrId = "+91 9649438733",
                email = "hotel.booking.swm@gmail.com",
                roleTitle = "होटल रूम्स ₹500 - ₹5000",
                avatarEmoji = "🏨",
                avatarBgColorHex = 0xFF2563EB,
                isOnline = true,
                isRegisteredUser = true,
                cityOrLocation = "रणथंभौर रोड, सवाई माधोपुर",
                lastMessage = "होटल रूम्स ₹500 से ₹2500 तक उपलब्ध हैं। फ्री वाईफाई और स्विमिंग पूल सुविधा शामिल है।",
                lastMessageTime = "09:45 AM",
                unreadCount = 2,
                lastMessageIsOutgoing = false,
                lastMessageIsDelivered = true,
                lastMessageIsSeen = false,
                lastMessageSenderName = "होटल हेल्पडेस्क",
                category = "HOTEL"
            ),
            ChatContact(
                id = "user_rahul_meena",
                name = "राहुल मीणा (सवाई माधोपुर)",
                phoneOrId = "+91 9829012345",
                email = "rahul.meena.swm@gmail.com",
                roleTitle = "रजिस्टर्ड नागरिक आईडी #1042",
                avatarEmoji = "👨‍💼",
                avatarBgColorHex = 0xFF0D9488,
                isOnline = true,
                isRegisteredUser = true,
                cityOrLocation = "बजिरया, सवाई माधोपुर",
                lastMessage = "पहुंचकर मुझे कॉल करना या यहीं SMS कर देना!",
                lastMessageTime = "11:15 AM",
                unreadCount = 3,
                lastMessageIsOutgoing = false,
                lastMessageIsDelivered = true,
                lastMessageIsSeen = false,
                lastMessageSenderName = "राहुल मीणा",
                category = "CITIZEN"
            ),
            ChatContact(
                id = "user_suresh_saini",
                name = "सुरेश सैनी (पुराना नागरिक)",
                phoneOrId = "+91 9829567890",
                email = "suresh.saini.swm@gmail.com",
                roleTitle = "पुराना पंजीकृत नागरिक #2011",
                avatarEmoji = "👨‍🌾",
                avatarBgColorHex = 0xFFEA580C,
                isOnline = false, // starts OFFLINE for testing offline SMS queue!
                isNearbyP2P = false,
                isRegisteredUser = true,
                cityOrLocation = "मानटाउन, सवाई माधोपुर",
                lastMessage = "🔴 ऑफ़लाइन (नेटवर्क से बाहर) - SMS कतार में जाएगा",
                lastMessageTime = "कल",
                unreadCount = 0,
                lastMessageIsOutgoing = false,
                category = "CITIZEN"
            ),
            ChatContact(
                id = "user_kavita_verma",
                name = "डॉ. कविता वर्मा (पुराना यूज़र)",
                phoneOrId = "+91 9414556677",
                email = "kavita.verma.swm@gmail.com",
                roleTitle = "चिकित्सा अधिकारी आईडी #1088",
                avatarEmoji = "👩‍⚕️",
                avatarBgColorHex = 0xFF0284C7,
                isOnline = false, // starts OFFLINE for testing
                isNearbyP2P = false,
                isRegisteredUser = true,
                cityOrLocation = "सामुदायिक स्वास्थ्य केंद्र, सवाई माधोपुर",
                lastMessage = "🔴 ऑफ़लाइन - ऑनलाइन या रेंज में आने पर SMS मिलेगा",
                lastMessageTime = "कल",
                unreadCount = 0,
                lastMessageIsOutgoing = false,
                category = "CITIZEN"
            ),
            ChatContact(
                id = "user_pooja_sharma",
                name = "पूजा शर्मा (गाइड)",
                phoneOrId = "+91 9414078912",
                email = "pooja.sharma.swm@gmail.com",
                roleTitle = "प्रमाणित टूरिस्ट गाइड आईडी #2089",
                avatarEmoji = "👩‍🏫",
                avatarBgColorHex = 0xFFBE185D,
                isOnline = true,
                isRegisteredUser = true,
                cityOrLocation = "किला रोड, सवाई माधोपुर",
                lastMessage = "त्रिनेत्र गणेश जी मंदिर दर्शन के लिए सुबह जल्दी जाएं।",
                lastMessageTime = "08:20 AM",
                unreadCount = 0,
                lastMessageIsOutgoing = false,
                category = "CITIZEN"
            ),
            ChatContact(
                id = "user_amit_gupta",
                name = "अमित गुप्ता (व्यापारी संघ)",
                phoneOrId = "+91 9784056789",
                email = "amit.gupta.swm@gmail.com",
                roleTitle = "लोकल बिजनेस लिस्टिंग आईडी #3150",
                avatarEmoji = "🛍️",
                avatarBgColorHex = 0xFF7C3AED,
                isOnline = true,
                isRegisteredUser = true,
                cityOrLocation = "मुख्य बाजार, सवाई माधोपुर",
                lastMessage = "अमरूद मंडी और हैंडीक्राफ्ट की सभी दुकानें खुली हैं।",
                lastMessageTime = "कल",
                unreadCount = 0,
                lastMessageIsOutgoing = false,
                category = "CITIZEN"
            ),
            ChatContact(
                id = "user_vikas_choudhary",
                name = "विकास चौधरी (छात्र - लोकल)",
                phoneOrId = "+91 9610223344",
                email = "vikas.choudhary.swm@gmail.com",
                roleTitle = "रणथंभौर कॉलेज छात्र",
                avatarEmoji = "🎓",
                avatarBgColorHex = 0xFF6366F1,
                isOnline = false,
                isNearbyP2P = true,
                p2pConnectionType = "BLUETOOTH",
                isRegisteredUser = true,
                cityOrLocation = "समीप (Bluetooth P2P रेंज)",
                lastMessage = "📡 ब्लूटूथ P2P रेंज में कनेक्टेड (0 डेटा SMS)",
                lastMessageTime = "अभी",
                unreadCount = 0,
                lastMessageIsOutgoing = false,
                category = "OFFLINE"
            ),
            ChatContact(
                id = "community_anyone",
                name = "सवाई माधोपुर नागरिक मंच (Public Community)",
                phoneOrId = "Public Community ID",
                email = "community.swm@gmail.com",
                roleTitle = "सभी पंजीकृत नागरिक व टूरिस्ट",
                avatarEmoji = "👥",
                avatarBgColorHex = 0xFF059669,
                isOnline = true,
                isRegisteredUser = true,
                cityOrLocation = "सवाई माधोपुर",
                lastMessage = "सवाई माधोपुर में मौसम बहुत सुहावना है! किसी भी सहायता के लिए पूछें।",
                lastMessageTime = "कल",
                unreadCount = 0,
                lastMessageIsOutgoing = false,
                category = "OFFICIAL"
            ),
            ChatContact(
                id = "offline_mesh_peer",
                name = "आस-पास का यूज़र (Offline WiFi/BT Peer)",
                phoneOrId = "P2P Mesh Direct",
                email = "offline.p2p@sawaimadhopur.mesh",
                roleTitle = "बिना इंटरनेट (WiFi / Bluetooth) SMS",
                avatarEmoji = "📶",
                avatarBgColorHex = 0xFF7C3AED,
                isOnline = true,
                isNearbyP2P = true,
                p2pConnectionType = "WIFI",
                isRegisteredUser = false,
                cityOrLocation = "नियरबाई डिवाइस (Local Range)",
                lastMessage = "बिना इंटरनेट WiFi/Bluetooth से 100% फ्री ऑफलाइन चैट कनेक्टेड है।",
                lastMessageTime = "अभी",
                unreadCount = 0,
                peerIpAddress = "192.168.43.102",
                lastMessageIsOutgoing = false,
                category = "OFFLINE"
            )
        )
        _contacts.value = initialList

        // Asynchronously load any registered users from Room DB so all old users appear as contacts
        scope.launch(Dispatchers.IO) {
            try {
                val db = com.example.data.AppDatabase.getDatabase(context)
                val registeredDbUsers = db.userDao().getAllUsers()
                if (registeredDbUsers.isNotEmpty()) {
                    val currentList = _contacts.value.toMutableList()
                    for (u in registeredDbUsers) {
                        val alreadyListed = currentList.any { 
                            it.phoneOrId == u.mobile || 
                            (it.email.isNotBlank() && it.email.equals(u.email, ignoreCase = true)) ||
                            it.id == "registered_user_${u.id}"
                        }
                        if (!alreadyListed) {
                            currentList.add(
                                ChatContact(
                                    id = "registered_user_${u.id}",
                                    name = u.fullName,
                                    phoneOrId = u.mobile.ifBlank { "पंजीकृत मोबाइल" },
                                    email = u.email,
                                    roleTitle = "पंजीकृत पुराना यूज़र (ID #${u.id})",
                                    avatarEmoji = "👤",
                                    avatarBgColorHex = 0xFF0284C7,
                                    isOnline = false, // Starts offline so user can send SMS and test online/range arrival!
                                    isNearbyP2P = false,
                                    isRegisteredUser = true,
                                    cityOrLocation = "सवाई माधोपुर",
                                    lastMessage = "🔴 ऑफ़लाइन (SMS कतार में सुरक्षित रहेगा)",
                                    lastMessageTime = "पुराना यूज़र",
                                    unreadCount = 0
                                )
                            )
                        }
                    }
                    withContext(Dispatchers.Main) {
                        _contacts.value = currentList
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadPersistedMessages() {
        val raw = prefs.getString("chat_messages_map", null)
        val defaultMessages = mapOf(
            "safari_helpdesk" to listOf(
                ChatMessage(
                    contactId = "safari_helpdesk",
                    senderName = "रणथंभौर सफारी हेल्पडेस्क",
                    senderPhone = "+91 6367094588",
                    text = "जय रणथंभौर! 🐅 सवाई माधोपुर टूरिज्म हेल्पडेस्क में आपका स्वागत है। यहां आप सफारी जिप्सी, कैंटर और चंबल घड़ियाल बोट सफारी की जानकारी और बुकिंग आसानी से कर सकते हैं।",
                    timestamp = System.currentTimeMillis() - 7200000,
                    isOutgoing = false,
                    isDelivered = true,
                    isSeen = false,
                    transportType = ChatTransportType.INTERNET
                ),
                ChatMessage(
                    contactId = "safari_helpdesk",
                    senderName = "रणथंभौर सफारी हेल्पडेस्क",
                    senderPhone = "+91 6367094588",
                    text = "क्या आप कल सुबह की शिफ्ट में जोन 1-5 के लिए जिप्सी/कैंटर टिकट बुक करना चाहते हैं?",
                    timestamp = System.currentTimeMillis() - 3600000,
                    isOutgoing = false,
                    isDelivered = true,
                    isSeen = false,
                    transportType = ChatTransportType.INTERNET
                )
            ),
            "hotel_helpdesk" to listOf(
                ChatMessage(
                    contactId = "hotel_helpdesk",
                    senderName = "सवाई माधोपुर होटल व रूम बुकिंग",
                    senderPhone = "+91 9649438733",
                    text = "नमस्ते! रणथंभौर रोड पर A/C व नॉन-A/C डीलक्स रूम्स उपलब्ध हैं। विशेष छूट के लिए रिप्लाई करें।",
                    timestamp = System.currentTimeMillis() - 5400000,
                    isOutgoing = false,
                    isDelivered = true,
                    isSeen = false,
                    transportType = ChatTransportType.INTERNET
                ),
                ChatMessage(
                    contactId = "hotel_helpdesk",
                    senderName = "सवाई माधोपुर होटल व रूम बुकिंग",
                    senderPhone = "+91 9649438733",
                    text = "होटल रूम्स ₹500 से ₹2500 तक उपलब्ध हैं। फ्री वाईफाई और स्विमिंग पूल सुविधा शामिल है।",
                    timestamp = System.currentTimeMillis() - 2700000,
                    isOutgoing = false,
                    isDelivered = true,
                    isSeen = false,
                    transportType = ChatTransportType.INTERNET
                )
            ),
            "user_rahul_meena" to listOf(
                ChatMessage(
                    contactId = "user_rahul_meena",
                    senderName = "राहुल मीणा",
                    senderPhone = "+91 9829012345",
                    text = "भाई चंबल सफारी के लिए सुबह 8 बजे बोटिंग शुरू होती है।",
                    timestamp = System.currentTimeMillis() - 3000000,
                    isOutgoing = false,
                    isDelivered = true,
                    isSeen = false,
                    transportType = ChatTransportType.INTERNET
                ),
                ChatMessage(
                    contactId = "user_rahul_meena",
                    senderName = "राहुल मीणा",
                    senderPhone = "+91 9829012345",
                    text = "क्या आप भी हमारे साथ चल रहे हो? गाड़ी में 2 सीटें खाली हैं।",
                    timestamp = System.currentTimeMillis() - 1800000,
                    isOutgoing = false,
                    isDelivered = true,
                    isSeen = false,
                    transportType = ChatTransportType.INTERNET
                ),
                ChatMessage(
                    contactId = "user_rahul_meena",
                    senderName = "राहुल मीणा",
                    senderPhone = "+91 9829012345",
                    text = "पहुंचकर मुझे कॉल करना या यहीं SMS कर देना!",
                    timestamp = System.currentTimeMillis() - 900000,
                    isOutgoing = false,
                    isDelivered = true,
                    isSeen = false,
                    transportType = ChatTransportType.INTERNET
                )
            ),
            "offline_mesh_peer" to listOf(
                ChatMessage(
                    contactId = "offline_mesh_peer",
                    senderName = "WiFi/Bluetooth P2P",
                    text = "📶 यह बिना इंटरनेट (Offline Mode) चैट है। यदि आपके पास इंटरनेट डाटा नहीं है, तो आप हॉटस्पॉट या ब्लूटूथ के जरिए आस-पास के किसी भी फोन से 100% फ्री में चैट व SMS कर सकते हैं!",
                    timestamp = System.currentTimeMillis() - 600000,
                    isOutgoing = false,
                    isDelivered = true,
                    isSeen = true,
                    transportType = ChatTransportType.WIFI_LAN_P2P
                )
            )
        )

        if (raw != null) {
            try {
                val json = JSONObject(raw)
                val map = mutableMapOf<String, List<ChatMessage>>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val contactId = keys.next()
                    val arr = json.getJSONArray(contactId)
                    val list = mutableListOf<ChatMessage>()
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        list.add(
                            ChatMessage(
                                id = item.getString("id"),
                                contactId = contactId,
                                senderName = item.getString("senderName"),
                                senderPhone = item.optString("senderPhone", ""),
                                senderEmail = item.optString("senderEmail", ""),
                                text = item.getString("text"),
                                timestamp = item.getLong("timestamp"),
                                isOutgoing = item.getBoolean("isOutgoing"),
                                isDelivered = item.optBoolean("isDelivered", true),
                                isSeen = item.optBoolean("isSeen", true),
                                transportType = ChatTransportType.valueOf(
                                    item.optString("transportType", "INTERNET")
                                ),
                                messageType = MessageType.valueOf(
                                    item.optString("messageType", "TEXT")
                                ),
                                mediaUri = if (item.has("mediaUri")) item.getString("mediaUri") else null,
                                durationSeconds = item.optInt("durationSeconds", 0),
                                isQueuedOffline = item.optBoolean("isQueuedOffline", false),
                                deliveryTime = if (item.has("deliveryTime")) item.getLong("deliveryTime") else null,
                                seenTime = if (item.has("seenTime")) item.getLong("seenTime") else null
                            )
                        )
                    }
                    map[contactId] = list
                }

                // Ensure initial unread sample messages exist if not already present
                for ((cid, defaultList) in defaultMessages) {
                    if (map[cid].isNullOrEmpty()) {
                        map[cid] = defaultList
                    }
                }

                _messages.value = map
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        _messages.value = defaultMessages
        saveMessagesToPrefs(defaultMessages)
    }

    private fun saveMessagesToPrefs(map: Map<String, List<ChatMessage>>) {
        scope.launch(Dispatchers.IO) {
            try {
                val json = JSONObject()
                for ((contactId, list) in map) {
                    val arr = JSONArray()
                    for (msg in list) {
                        val item = JSONObject().apply {
                            put("id", msg.id)
                            put("senderName", msg.senderName)
                            put("senderPhone", msg.senderPhone)
                            put("senderEmail", msg.senderEmail)
                            put("text", msg.text)
                            put("timestamp", msg.timestamp)
                            put("isOutgoing", msg.isOutgoing)
                            put("isDelivered", msg.isDelivered)
                            put("isSeen", msg.isSeen)
                            put("transportType", msg.transportType.name)
                            put("messageType", msg.messageType.name)
                            if (msg.mediaUri != null) put("mediaUri", msg.mediaUri)
                            put("durationSeconds", msg.durationSeconds)
                            put("isQueuedOffline", msg.isQueuedOffline)
                            if (msg.deliveryTime != null) put("deliveryTime", msg.deliveryTime)
                            if (msg.seenTime != null) put("seenTime", msg.seenTime)
                        }
                        arr.put(item)
                    }
                    json.put(contactId, arr)
                }
                prefs.edit().putString("chat_messages_map", json.toString()).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectContact(contact: ChatContact?) {
        _activeContact.value = contact
        if (contact != null) {
            // mark contact as read
            val updated = _contacts.value.map {
                if (it.id == contact.id) it.copy(unreadCount = 0) else it
            }
            _contacts.value = updated

            val currentMap = _messages.value.toMutableMap()
            val list = currentMap[contact.id]
            if (list != null) {
                currentMap[contact.id] = list.map { if (!it.isOutgoing) it.copy(isSeen = true) else it }
                _messages.value = currentMap
                saveMessagesToPrefs(currentMap)
            }
        }
    }

    fun markAllAsRead() {
        val updated = _contacts.value.map { it.copy(unreadCount = 0) }
        _contacts.value = updated
        val currentMap = _messages.value.toMutableMap()
        for ((cid, list) in currentMap) {
            currentMap[cid] = list.map { if (!it.isOutgoing) it.copy(isSeen = true) else it }
        }
        _messages.value = currentMap
        saveMessagesToPrefs(currentMap)
    }

    fun addNewContact(
        name: String,
        phoneOrId: String,
        email: String = "",
        roleTitle: String = "यूज़र",
        cityOrLocation: String = "सवाई माधोपुर",
        isRegisteredUser: Boolean = true
    ): ChatContact {
        val newContact = ChatContact(
            id = "custom_${System.currentTimeMillis()}",
            name = name.ifBlank { "नया संपर्क" },
            phoneOrId = phoneOrId.ifBlank { "Unknown" },
            email = email.trim(),
            roleTitle = roleTitle,
            avatarEmoji = listOf("👤", "🙋‍♂️", "🙋‍♀️", "⭐", "🌟", "🧳", "📸").random(),
            avatarBgColorHex = listOf(0xFF0284C7, 0xFF7C3AED, 0xFF059669, 0xFFD97706, 0xFFE11D48, 0xFF0D9488).random(),
            isOnline = true,
            isRegisteredUser = isRegisteredUser,
            cityOrLocation = cityOrLocation,
            lastMessage = "चैट शुरू की गई",
            lastMessageTime = "अभी"
        )
        _contacts.value = listOf(newContact) + _contacts.value
        selectContact(newContact)
        return newContact
    }

    suspend fun searchRegisteredCandidates(query: String): List<ChatContact> = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.isEmpty()) return@withContext emptyList()

        // 1. Match from existing contacts by Name, Mobile number, Gmail ID, or City
        val localMatches = _contacts.value.filter {
            it.name.contains(clean, ignoreCase = true) ||
            it.phoneOrId.contains(clean, ignoreCase = true) ||
            it.email.contains(clean, ignoreCase = true) ||
            it.cityOrLocation.contains(clean, ignoreCase = true)
        }

        // 2. Query Room database registered users
        try {
            val db = com.example.data.AppDatabase.getDatabase(context)
            val registeredUsers = db.userDao().searchUsers(clean)
            val fromDb = registeredUsers.filter { user ->
                localMatches.none { 
                    it.phoneOrId == user.mobile || 
                    (it.email.isNotBlank() && it.email.equals(user.email, ignoreCase = true)) 
                }
            }.map { user ->
                ChatContact(
                    id = "registered_user_${user.id}",
                    name = user.fullName,
                    phoneOrId = user.mobile.ifBlank { "पंजीकृत मोबाइल" },
                    email = user.email,
                    roleTitle = "पंजीकृत यूज़र (ID #${user.id})",
                    avatarEmoji = "👤",
                    avatarBgColorHex = 0xFF0284C7,
                    isOnline = true,
                    isRegisteredUser = true,
                    cityOrLocation = "सवाई माधोपुर",
                    lastMessage = "फ्री SMS / कॉल उपलब्ध",
                    lastMessageTime = "अभी"
                )
            }
            localMatches + fromDb
        } catch (e: Exception) {
            localMatches
        }
    }

    fun startChatWithCandidate(user: com.example.data.UserEntity): ChatContact {
        return startChatWithCandidate(
            name = user.fullName,
            phoneOrId = user.mobile,
            email = user.email,
            city = "सवाई माधोपुर"
        )
    }

    fun startChatWithCandidate(
        name: String,
        phoneOrId: String,
        email: String,
        city: String = "सवाई माधोपुर"
    ): ChatContact {
        val cleanPhone = phoneOrId.trim()
        val cleanEmail = email.trim()
        val cleanName = name.trim().ifBlank {
            if (cleanEmail.isNotBlank()) cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
            else if (cleanPhone.isNotBlank()) "कैंडिडेट ($cleanPhone)"
            else "नया कैंडिडेट"
        }
        val cleanCity = city.trim().ifBlank { "सवाई माधोपुर" }

        val existing = _contacts.value.find { contact ->
            (cleanPhone.isNotEmpty() && contact.phoneOrId.equals(cleanPhone, ignoreCase = true)) ||
            (cleanEmail.isNotEmpty() && contact.email.equals(cleanEmail, ignoreCase = true)) ||
            (contact.name.equals(cleanName, ignoreCase = true))
        }

        if (existing != null) {
            selectContact(existing)
            return existing
        }

        val newContact = ChatContact(
            id = "candidate_${System.currentTimeMillis()}",
            name = cleanName,
            phoneOrId = cleanPhone.ifBlank { "कैंडिडेट आईडी" },
            email = cleanEmail,
            roleTitle = "कैंडिडेट / यूज़र",
            avatarEmoji = "👤",
            avatarBgColorHex = 0xFF0284C7,
            isOnline = true,
            isRegisteredUser = true,
            cityOrLocation = cleanCity,
            lastMessage = "मुफ्त SMS चैट शुरू हुई",
            lastMessageTime = "अभी"
        )

        _contacts.value = listOf(newContact) + _contacts.value
        selectContact(newContact)
        return newContact
    }

    fun sendMessage(
        contactId: String,
        text: String,
        transportType: ChatTransportType = ChatTransportType.INTERNET,
        mediaUri: String? = null,
        messageType: MessageType = MessageType.TEXT,
        customSenderName: String? = null,
        customSenderPhone: String? = null
    ) {
        if (text.isBlank() && mediaUri == null) return

        val contact = _contacts.value.find { it.id == contactId }
        val isOnline = contact?.isOnline ?: false
        val isNearby = contact?.isNearbyP2P ?: false

        // Check if recipient can receive message right now
        val isRecipientReachable = when (transportType) {
            ChatTransportType.INTERNET -> isOnline
            ChatTransportType.WIFI_LAN_P2P -> isNearby || contact?.peerIpAddress != null
            ChatTransportType.BLUETOOTH_P2P -> isNearby
        }

        val myProfile = com.example.data.UserProfileManager.profileState.value
        val sName = customSenderName ?: myProfile.name.ifBlank { "सवाई माधोपुर यूज़र" }
        val sPhone = customSenderPhone ?: myProfile.phone

        val newMsg = ChatMessage(
            contactId = contactId,
            senderName = sName,
            senderPhone = sPhone,
            senderEmail = myProfile.email,
            text = text,
            timestamp = System.currentTimeMillis(),
            isOutgoing = true,
            // If recipient is offline/unreachable, SMS stays queued offline: isDelivered=false, isSeen=false
            isDelivered = isRecipientReachable,
            isSeen = false, // starts unread (gray ticks), will become seen (blue tick) when viewed
            isQueuedOffline = !isRecipientReachable,
            deliveryTime = if (isRecipientReachable) System.currentTimeMillis() else null,
            transportType = transportType,
            messageType = messageType,
            mediaUri = mediaUri
        )

        val currentMap = _messages.value.toMutableMap()
        val list = currentMap[contactId]?.toMutableList() ?: mutableListOf()
        list.add(newMsg)
        currentMap[contactId] = list
        _messages.value = currentMap
        saveMessagesToPrefs(currentMap)

        // Update contact last message & pending count
        val updatedContacts = _contacts.value.map {
            if (it.id == contactId) {
                val addedPending = if (!isRecipientReachable) 1 else 0
                it.copy(
                    lastMessage = if (!isRecipientReachable) "🕒 [कतार में] $text" else (if (messageType == MessageType.VOICE_NOTE) "🎤 वॉयस नोट (${newMsg.durationSeconds}s)" else text),
                    lastMessageTime = "अभी",
                    pendingOutgoingCount = it.pendingOutgoingCount + addedPending,
                    lastMessageIsOutgoing = true,
                    lastMessageIsDelivered = isRecipientReachable,
                    lastMessageIsSeen = false,
                    lastMessageSenderName = "आप"
                )
            } else it
        }
        _contacts.value = updatedContacts

        if (isRecipientReachable) {
            // If offline WiFi mode and peer has IP, try to send through socket
            if (transportType == ChatTransportType.WIFI_LAN_P2P && contact?.peerIpAddress != null) {
                scope.launch {
                    offlineService.sendMessageOverWifi(
                        targetIp = contact.peerIpAddress,
                        text = text,
                        senderName = sName
                    )
                }
            }

            // Recipient sees the message shortly after delivery:
            // "sms dekhte hi duble tick blue ho jaye"
            scope.launch {
                delay(1500) // Delay to simulate recipient opening & viewing
                markMessagesAsSeen(contactId)
                triggerSmartAutoReply(contactId, text, transportType)
            }
        }
    }

    /**
     * When offline contact comes online or enters WiFi/Bluetooth range,
     * deliver all pending messages in queue.
     */
    fun deliverPendingMessagesForContact(
        contactId: String,
        transport: ChatTransportType = ChatTransportType.INTERNET
    ): Int {
        val currentMap = _messages.value.toMutableMap()
        val list = currentMap[contactId] ?: return 0

        var deliveredCount = 0
        val updatedList = list.map { msg ->
            if (msg.isOutgoing && (!msg.isDelivered || msg.isQueuedOffline)) {
                deliveredCount++
                msg.copy(
                    isDelivered = true,
                    isQueuedOffline = false,
                    transportType = transport,
                    deliveryTime = System.currentTimeMillis()
                )
            } else msg
        }

        if (deliveredCount > 0) {
            currentMap[contactId] = updatedList
            _messages.value = currentMap
            saveMessagesToPrefs(currentMap)

            _contacts.value = _contacts.value.map {
                if (it.id == contactId) {
                    it.copy(
                        pendingOutgoingCount = 0,
                        lastMessage = updatedList.lastOrNull()?.text ?: it.lastMessage
                    )
                } else it
            }

            // Once delivered, recipient sees the message shortly after -> double tick turns BLUE!
            scope.launch {
                delay(1600) // Recipient views the SMS
                markMessagesAsSeen(contactId)
                val lastText = updatedList.lastOrNull()?.text ?: "नमस्ते"
                triggerSmartAutoReply(contactId, lastText, transport)
            }
        }
        return deliveredCount
    }

    /**
     * Marks delivered messages as seen by recipient -> turns double ticks into BLUE (✓✓)
     */
    fun markMessagesAsSeen(contactId: String) {
        val currentMap = _messages.value.toMutableMap()
        val list = currentMap[contactId] ?: return
        var changed = false
        val updatedList = list.map { msg ->
            if (msg.isOutgoing && msg.isDelivered && !msg.isSeen) {
                changed = true
                msg.copy(isSeen = true, seenTime = System.currentTimeMillis())
            } else msg
        }
        if (changed) {
            currentMap[contactId] = updatedList
            _messages.value = currentMap
            saveMessagesToPrefs(currentMap)
        }

        _contacts.value = _contacts.value.map {
            if (it.id == contactId) it.copy(unreadCount = 0) else it
        }
    }

    /**
     * User can simulate the recipient immediately viewing their SMS.
     * All messages turn to Double Blue Tick!
     */
    fun simulateRecipientSeen(contactId: String) {
        deliverPendingMessagesForContact(contactId, ChatTransportType.INTERNET)
        markMessagesAsSeen(contactId)
    }

    /**
     * Change reachability (Online, WiFi range, Bluetooth range, or Offline)
     * When user comes online or in range, queued SMS are delivered automatically.
     */
    fun updateContactReachability(
        contactId: String,
        isOnline: Boolean,
        isNearbyP2P: Boolean,
        p2pType: String? = null
    ) {
        _contacts.value = _contacts.value.map {
            if (it.id == contactId) {
                it.copy(
                    isOnline = isOnline,
                    isNearbyP2P = isNearbyP2P,
                    p2pConnectionType = p2pType
                )
            } else it
        }
        if (_activeContact.value?.id == contactId) {
            _activeContact.value = _contacts.value.find { it.id == contactId }
        }

        // If newly reached via Online or WiFi/Bluetooth P2P, deliver all pending messages!
        if (isOnline || isNearbyP2P) {
            val transport = when {
                p2pType == "BLUETOOTH" -> ChatTransportType.BLUETOOTH_P2P
                p2pType == "WIFI" -> ChatTransportType.WIFI_LAN_P2P
                isNearbyP2P -> ChatTransportType.WIFI_LAN_P2P
                else -> ChatTransportType.INTERNET
            }
            deliverPendingMessagesForContact(contactId, transport)
        }
    }

    private fun triggerSmartAutoReply(contactId: String, userText: String, transport: ChatTransportType) {
        scope.launch {
            delay(1200) // Realistic typing simulation delay
            val replyText = when (contactId) {
                "safari_helpdesk" -> {
                    when {
                        userText.contains("सफारी", ignoreCase = true) || userText.contains("safari", ignoreCase = true) ->
                            "रणथंभौर सफारी सुबह 6:30 से 10:00 AM और दोपहर 2:30 से 6:00 PM तक चलती है। जोन 1 से 5 और 6 से 10 के लिए जिप्सी/कैंटर उपलब्ध हैं। तारीख बताएं ताकि हम उपलब्धता चेक कर सकें!"
                        userText.contains("टिकट", ignoreCase = true) || userText.contains("रेट", ignoreCase = true) || userText.contains("price", ignoreCase = true) ->
                            "सफारी कैंटर शुल्क लगभग ₹700-₹850 एवं जिप्सी बुकिंग लगभग ₹1500-₹1900 प्रति व्यक्ति (सरकारी शुल्क अनुसार) है। एडवांस बुकिंग के लिए तुरंत संपर्क करें: 6367094588"
                        userText.contains("चंबल", ignoreCase = true) || userText.contains("boat", ignoreCase = true) ->
                            "पालिघाट चंबल घड़ियाल बोट सफारी सवाई माधोपुर से 45 किमी दूरी पर है। यहां बोटिंग के दौरान घड़ियाल, मगरमच्छ और डॉल्फिन आसानी से देखे जा सकते हैं!"
                        else ->
                            "आपका संदेश प्राप्त हुआ! रणथंभौर सफारी डेस्क आपकी सेवा में है। अधिक जानकारी या सीधे कॉल के लिए ऊपर दिए गए 📞 या 📹 कॉल बटन पर क्लिक करें।"
                    }
                }
                "hotel_helpdesk" -> {
                    when {
                        userText.contains("रूम", ignoreCase = true) || userText.contains("room", ignoreCase = true) ->
                            "हमारे पास रणथंभौर रोड पर A/C डिलक्स रूम मात्र ₹1200 से ₹2500 और बजट रूम ₹600 से शुरू हैं। मुफ्त वाईफाई और स्विमिंग पूल सुविधा शामिल है।"
                        userText.contains("खाना", ignoreCase = true) || userText.contains("food", ignoreCase = true) ->
                            "शुद्ध शाकाहारी और स्वादिष्ट राजस्थानी थाली एवं 24/7 रूम सर्विस उपलब्ध है। होटल सिटी हार्ट, रणथंभौर रोड।"
                        else ->
                            "होटल हेल्पडेस्क: आप किस तारीख के लिए रूम बुक करना चाहते हैं? हम आपको बेस्ट डिस्काउंट ऑफर भेज देंगे।"
                    }
                }
                "offline_mesh_peer" -> {
                    "📶 [OFFLINE P2P ACK]: आपका ऑफलाइन मैसेज बिना इंटरनेट WiFi/Bluetooth मेश के माध्यम से तुरंत डिलीवर हो गया है! सिग्नल: उत्कृष्ट।"
                }
                else -> {
                    "नमस्ते! आपका मैसेज सफलतापूर्वक मिल गया है। मैं जल्द ही रिप्लाई करता हूँ।"
                }
            }

            val incomingReply = ChatMessage(
                contactId = contactId,
                senderName = _contacts.value.find { it.id == contactId }?.name ?: "संपर्क",
                text = replyText,
                timestamp = System.currentTimeMillis(),
                isOutgoing = false,
                isDelivered = true,
                isSeen = true,
                transportType = transport
            )

            receiveIncomingMessage(incomingReply)
        }
    }

    fun receiveIncomingMessage(msg: ChatMessage) {
        val currentMap = _messages.value.toMutableMap()
        val list = currentMap[msg.contactId]?.toMutableList() ?: mutableListOf()
        list.add(msg)
        currentMap[msg.contactId] = list
        _messages.value = currentMap
        saveMessagesToPrefs(currentMap)

        val updatedContacts = _contacts.value.map {
            if (it.id == msg.contactId) {
                val isCurrentlyOpen = _activeContact.value?.id == msg.contactId
                it.copy(
                    lastMessage = msg.text,
                    lastMessageTime = "अभी",
                    unreadCount = if (isCurrentlyOpen) 0 else (it.unreadCount + 1),
                    lastMessageIsOutgoing = false,
                    lastMessageIsDelivered = true,
                    lastMessageIsSeen = isCurrentlyOpen,
                    lastMessageSenderName = msg.senderName
                )
            } else it
        }
        _contacts.value = updatedContacts
    }

    // Call Management
    fun startCall(contact: ChatContact, isVideo: Boolean) {
        _callState.value = ChatCallState(
            isInCall = true,
            isVideoCall = isVideo,
            contactName = contact.name,
            contactAvatarEmoji = contact.avatarEmoji,
            contactRole = contact.roleTitle,
            durationSeconds = 0,
            isMuted = false,
            isSpeakerOn = true,
            isVideoDisabled = false,
            isFrontCamera = true,
            statusText = if (isVideo) "व्हाट्सएप एचडी वीडियो कॉल कनेक्ट हो रही है..." else "व्हाट्सएप फ्री ऑडियो कॉल..."
        )

        callTimerJob?.cancel()
        callTimerJob = scope.launch {
            delay(1500)
            _callState.value = _callState.value.copy(
                statusText = if (isVideo) "कनेक्टेड • HD 1080p वीडियो" else "कनेक्टेड • HD क्रिस्टल क्लियर ऑडियो"
            )
            while (_callState.value.isInCall) {
                delay(1000)
                _callState.value = _callState.value.copy(
                    durationSeconds = _callState.value.durationSeconds + 1
                )
            }
        }
    }

    fun toggleMute() {
        _callState.value = _callState.value.copy(isMuted = !_callState.value.isMuted)
    }

    fun toggleSpeaker() {
        _callState.value = _callState.value.copy(isSpeakerOn = !_callState.value.isSpeakerOn)
    }

    fun toggleVideo() {
        _callState.value = _callState.value.copy(isVideoDisabled = !_callState.value.isVideoDisabled)
    }

    fun flipCamera() {
        _callState.value = _callState.value.copy(isFrontCamera = !_callState.value.isFrontCamera)
    }

    fun endCall() {
        val duration = _callState.value.durationSeconds
        val contactName = _callState.value.contactName
        val isVideo = _callState.value.isVideoCall

        callTimerJob?.cancel()
        callTimerJob = null
        _callState.value = ChatCallState(isInCall = false)

        // Log call into active chat if present
        _activeContact.value?.let { contact ->
            val logText = if (isVideo) "📹 वीडियो कॉल समाप्त (${formatDuration(duration)})" else "📞 ऑडियो कॉल समाप्त (${formatDuration(duration)})"
            val callLogMsg = ChatMessage(
                contactId = contact.id,
                senderName = "सिस्टम",
                text = logText,
                isOutgoing = true,
                messageType = MessageType.CALL_LOG,
                durationSeconds = duration
            )
            val currentMap = _messages.value.toMutableMap()
            val list = currentMap[contact.id]?.toMutableList() ?: mutableListOf()
            list.add(callLogMsg)
            currentMap[contact.id] = list
            _messages.value = currentMap
            saveMessagesToPrefs(currentMap)
        }
    }

    private fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}
