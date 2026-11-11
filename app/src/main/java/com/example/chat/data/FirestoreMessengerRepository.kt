package com.example.chat.data

import android.content.Context
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.SessionEntity
import com.example.data.UserDao
import com.example.data.UserEntity
import com.example.data.UserProfileData
import com.example.data.UserProfileManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class ChatUser(
    val uid: String,
    val name: String,
    val mobile: String,
    val email: String,
    val avatarEmoji: String = "👤"
)

data class PrivateMessage(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val messageText: String,
    val timestamp: Long,
    val senderName: String,
    val receiverName: String,
    val senderMobile: String = "",
    val receiverMobile: String = "",
    val isPendingSync: Boolean = false,
    val isOutgoing: Boolean = false,
    val transport: String = "CLOUD" // "CLOUD" or "P2P"
)

data class ActiveChatSummary(
    val chatId: String,
    val otherUser: ChatUser,
    val lastMessageText: String,
    val lastTimestamp: Long,
    val lastSenderId: String,
    val isPendingSync: Boolean = false,
    val unreadCount: Int = 0
)

class FirestoreMessengerRepository(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.getString(com.example.R.string.firestore_database_id)
    )
    private val userDao: UserDao = AppDatabase.getDatabase(context).userDao()
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    // Google Nearby Connections manager for 100% offline P2P chatting via WiFi Direct & Bluetooth
    val nearbyManager: NearbyConnectionsManager = NearbyConnectionsManager(context)

    // In-memory & local cache of P2P messages (available even with zero internet)
    private val _localP2PMessages = MutableStateFlow<List<PrivateMessage>>(emptyList())
    val localP2PMessages = _localP2PMessages.asStateFlow()

    init {
        // Enable Firestore offline persistence
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            firestore.firestoreSettings = settings
            Log.d("FirestoreMessenger", "Firestore offline persistence successfully configured")
        } catch (e: Exception) {
            Log.w("FirestoreMessenger", "Firestore settings already applied or error: ${e.message}")
        }

        seedInitialUsers()
        listenToIncomingP2PMessages()
    }

    private fun listenToIncomingP2PMessages() {
        scope.launch {
            nearbyManager.incomingMessages.collect { incomingMsg ->
                recordP2PMessage(incomingMsg)
            }
        }
    }

    /**
     * Stores a P2P message locally and also records to Firestore cache.
     */
    fun recordP2PMessage(message: PrivateMessage) {
        scope.launch {
            // Update local memory list
            val current = _localP2PMessages.value
            if (current.none { it.id == message.id }) {
                _localP2PMessages.value = current + message
            }

            // Sync to Firestore messages collection (persisted offline in cache and pushed when online)
            try {
                val participants = listOf(message.senderId, message.receiverId)
                val sortedParticipants = participants.sorted()
                val chatId = "${sortedParticipants[0]}_${sortedParticipants[1]}"

                val messageData = hashMapOf<String, Any>(
                    "id" to message.id,
                    "senderId" to message.senderId,
                    "receiverId" to message.receiverId,
                    "messageText" to message.messageText,
                    "timestamp" to message.timestamp,
                    "senderName" to message.senderName,
                    "receiverName" to message.receiverName,
                    "senderMobile" to message.senderMobile,
                    "receiverMobile" to message.receiverMobile,
                    "participants" to participants,
                    "transport" to "P2P"
                )

                firestore.collection("messages").document(message.id).set(messageData, SetOptions.merge())

                val chatData = hashMapOf<String, Any>(
                    "chatId" to chatId,
                    "participants" to participants,
                    "lastMessageText" to message.messageText,
                    "lastTimestamp" to message.timestamp,
                    "lastSenderId" to message.senderId,
                    "user1Id" to sortedParticipants[0],
                    "user1Name" to (if (sortedParticipants[0] == message.senderId) message.senderName else message.receiverName),
                    "user1Mobile" to (if (sortedParticipants[0] == message.senderId) message.senderMobile else message.receiverMobile),
                    "user2Id" to sortedParticipants[1],
                    "user2Name" to (if (sortedParticipants[1] == message.senderId) message.senderName else message.receiverName),
                    "user2Mobile" to (if (sortedParticipants[1] == message.senderId) message.senderMobile else message.receiverMobile)
                )
                firestore.collection("chats").document(chatId).set(chatData, SetOptions.merge())
            } catch (e: Exception) {
                Log.w("FirestoreMessenger", "Failed to cache P2P message to Firestore: ${e.message}")
            }
        }
    }

    /**
     * Seeds sample directory users into local Room DB and Firestore 'users' collection
     * so that search by Name, Mobile, or Gmail ID works immediately out-of-the-box.
     */
    fun seedInitialUsers() {
        scope.launch {
            try {
                if (userDao.getUserCount() <= 1) {
                    val demoUsers = listOf(
                        UserEntity(
                            fullName = "राहुल शर्मा",
                            email = "rahul.sharma@gmail.com",
                            mobile = "9829012345",
                            username = "rahul_swm",
                            passwordHash = "demo"
                        ),
                        UserEntity(
                            fullName = "पूजा वर्मा",
                            email = "pooja.verma@gmail.com",
                            mobile = "9414056789",
                            username = "pooja_swm",
                            passwordHash = "demo"
                        ),
                        UserEntity(
                            fullName = "सवाई माधोपुर हेल्पडेस्क",
                            email = "sawaimadhopur.help@gmail.com",
                            mobile = "6367094588",
                            username = "swm_support",
                            passwordHash = "demo"
                        )
                    )
                    for (u in demoUsers) {
                        try {
                            userDao.insertUser(u)
                        } catch (_: Exception) {}
                    }
                }

                // Sync all users to Firestore 'users' collection
                val localAll = userDao.getAllUsers()
                for (u in localAll) {
                    val uid = if (u.mobile.length >= 10) "user_${u.mobile}" else "user_${u.id}"
                    val userMap = hashMapOf<String, Any>(
                        "uid" to uid,
                        "name" to u.fullName,
                        "mobile" to u.mobile,
                        "email" to u.email,
                        "createdAt" to System.currentTimeMillis()
                    )
                    firestore.collection("users").document(uid).set(userMap, SetOptions.merge())
                }
            } catch (e: Exception) {
                Log.w("FirestoreMessenger", "Error seeding initial users: ${e.message}")
            }
        }
    }

    /**
     * Resolves the current user ID reliably (Firebase Auth UID > Session User ID / Mobile / Email).
     */
    fun getCurrentUserId(session: SessionEntity?, profile: UserProfileData?): String {
        val fbUid = auth.currentUser?.uid
        if (!fbUid.isNullOrBlank()) return fbUid

        if (session != null && session.userId > 0) {
            val mobile = session.mobile.filter { it.isDigit() }
            if (mobile.length >= 10) return "user_$mobile"
            if (session.email.isNotBlank()) return "user_${session.email.replace("@", "_").replace(".", "_")}"
            return "user_${session.userId}"
        }

        if (profile != null && profile.profileId.isNotBlank() && profile.profileId != "SWM-GUEST") {
            return profile.profileId
        }

        return "user_guest"
    }

    /**
     * Resolves current user info as ChatUser.
     */
    fun getCurrentUser(session: SessionEntity?, profile: UserProfileData?): ChatUser {
        val uid = getCurrentUserId(session, profile)
        val name = session?.fullName?.ifBlank { null }
            ?: profile?.name?.ifBlank { null }
            ?: auth.currentUser?.displayName?.ifBlank { null }
            ?: "सवाई माधोपुर यूज़र"
        val mobile = session?.mobile?.ifBlank { null } ?: profile?.phone ?: ""
        val email = session?.email?.ifBlank { null } ?: profile?.email ?: auth.currentUser?.email ?: ""
        return ChatUser(
            uid = uid,
            name = name,
            mobile = mobile,
            email = email,
            avatarEmoji = "👤"
        )
    }

    /**
     * Automatically registers / updates the current user profile in Firestore 'users' collection
     * so that other users can search them by Name, Mobile, or Gmail ID.
     */
    fun syncCurrentUserToFirestore(session: SessionEntity?, profile: UserProfileData?) {
        scope.launch {
            try {
                val current = getCurrentUser(session, profile)
                if (current.uid == "user_guest") return@launch

                val userMap = hashMapOf<String, Any>(
                    "uid" to current.uid,
                    "name" to current.name,
                    "mobile" to current.mobile,
                    "email" to current.email,
                    "avatarEmoji" to current.avatarEmoji,
                    "lastActive" to System.currentTimeMillis()
                )

                firestore.collection("users")
                    .document(current.uid)
                    .set(userMap, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w("FirestoreMessenger", "Failed to sync user to Firestore: ${e.message}")
            }
        }
    }

    /**
     * Search 'users' collection in Firestore by Name, Mobile Number, or Gmail ID.
     * Also checks local Room database as fallback for offline or pre-registered users.
     */
    suspend fun searchUsers(
        query: String,
        currentUserId: String
    ): List<ChatUser> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return@withContext emptyList()

        val results = mutableMapOf<String, ChatUser>()

        // 1. Search Firestore 'users' collection
        try {
            val snapshot = firestore.collection("users").get().await()
            for (doc in snapshot.documents) {
                val uid = doc.getString("uid") ?: doc.id
                if (uid == currentUserId) continue

                val name = doc.getString("name") ?: ""
                val mobile = doc.getString("mobile") ?: ""
                val email = doc.getString("email") ?: ""
                val emoji = doc.getString("avatarEmoji") ?: "👤"

                val matchesName = name.contains(cleanQuery, ignoreCase = true)
                val matchesMobile = mobile.contains(cleanQuery.filter { it.isDigit() })
                val matchesEmail = email.contains(cleanQuery, ignoreCase = true)

                if (matchesName || matchesMobile || matchesEmail) {
                    results[uid] = ChatUser(
                        uid = uid,
                        name = name.ifBlank { "सवाई माधोपुर यूज़र" },
                        mobile = mobile,
                        email = email,
                        avatarEmoji = emoji
                    )
                }
            }
        } catch (e: Exception) {
            Log.w("FirestoreMessenger", "Firestore search error: ${e.message}")
        }

        // 2. Search local Room DB (offline support & registered users)
        try {
            val localUsers = userDao.searchUsers(cleanQuery)
            for (u in localUsers) {
                val uid = if (u.mobile.length >= 10) "user_${u.mobile}" else "user_${u.id}"
                if (uid == currentUserId) continue
                if (!results.containsKey(uid)) {
                    results[uid] = ChatUser(
                        uid = uid,
                        name = u.fullName.ifBlank { u.username },
                        mobile = u.mobile,
                        email = u.email,
                        avatarEmoji = "👤"
                    )
                }
            }
        } catch (e: Exception) {
            Log.w("FirestoreMessenger", "Room search error: ${e.message}")
        }

        results.values.toList()
    }

    /**
     * Sends a private 1-on-1 message.
     * Uses Firestore offline persistence so the message is saved locally and dispatched
     * as soon as internet connection is available!
     *
     * Database Structure:
     * Stores in 'messages' collection:
     * - senderId
     * - receiverId
     * - messageText
     * - timestamp
     * And in 'chats' collection to maintain the active conversation summary.
     */
    /**
     * Sends a private 1-on-1 message.
     * Supports both CLOUD (Firestore real-time & offline persistence)
     * and P2P (Google Nearby Connections via WiFi Direct / Bluetooth).
     */
    suspend fun sendMessage(
        sender: ChatUser,
        receiver: ChatUser,
        messageText: String,
        transport: String = "CLOUD" // "CLOUD" or "P2P"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanText = messageText.trim()
        if (cleanText.isEmpty()) return@withContext Result.failure(IllegalArgumentException("Message is empty"))

        val timestamp = System.currentTimeMillis()
        val messageId = "msg_${timestamp}_${(1000..9999).random()}"
        val participants = listOf(sender.uid, receiver.uid)
        val sortedParticipants = participants.sorted()
        val chatId = "${sortedParticipants[0]}_${sortedParticipants[1]}"

        val privateMessage = PrivateMessage(
            id = messageId,
            senderId = sender.uid,
            receiverId = receiver.uid,
            messageText = cleanText,
            timestamp = timestamp,
            senderName = sender.name,
            receiverName = receiver.name,
            senderMobile = sender.mobile,
            receiverMobile = receiver.mobile,
            isPendingSync = false,
            isOutgoing = true,
            transport = transport
        )

        // If user chose P2P (Free Chat), broadcast/send directly over Nearby Connections
        if (transport == "P2P") {
            try {
                nearbyManager.sendP2PMessage(privateMessage)
            } catch (e: Exception) {
                Log.w("FirestoreMessenger", "Nearby P2P send exception: ${e.message}")
            }
            recordP2PMessage(privateMessage)
        }

        val messageData = hashMapOf<String, Any>(
            "id" to messageId,
            "senderId" to sender.uid,
            "receiverId" to receiver.uid,
            "messageText" to cleanText,
            "timestamp" to timestamp,
            "senderName" to sender.name,
            "receiverName" to receiver.name,
            "senderMobile" to sender.mobile,
            "receiverMobile" to receiver.mobile,
            "senderEmail" to sender.email,
            "receiverEmail" to receiver.email,
            "participants" to participants,
            "transport" to transport
        )

        val chatData = hashMapOf<String, Any>(
            "chatId" to chatId,
            "participants" to participants,
            "lastMessageText" to cleanText,
            "lastTimestamp" to timestamp,
            "lastSenderId" to sender.uid,
            "user1Id" to sortedParticipants[0],
            "user1Name" to (if (sortedParticipants[0] == sender.uid) sender.name else receiver.name),
            "user1Mobile" to (if (sortedParticipants[0] == sender.uid) sender.mobile else receiver.mobile),
            "user1Email" to (if (sortedParticipants[0] == sender.uid) sender.email else receiver.email),
            "user2Id" to sortedParticipants[1],
            "user2Name" to (if (sortedParticipants[1] == sender.uid) sender.name else receiver.name),
            "user2Mobile" to (if (sortedParticipants[1] == sender.uid) sender.mobile else receiver.mobile),
            "user2Email" to (if (sortedParticipants[1] == sender.uid) sender.email else receiver.email)
        )

        try {
            // Write to Firestore (will save locally to offline cache if disconnected, and sync when online)
            firestore.collection("messages")
                .document(messageId)
                .set(messageData)

            firestore.collection("chats")
                .document(chatId)
                .set(chatData, SetOptions.merge())

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreMessenger", "Error persisting message to Firestore: ${e.message}", e)
            if (transport == "P2P") {
                // If sent via P2P successfully, consider operation a success
                Result.success(Unit)
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Real-time flow of active chat conversations where current user is a participant.
     * Merges Firestore chats with any offline P2P chats.
     * Clean Inbox: ONLY contains users with whom actual messages were exchanged.
     */
    fun getActiveChatsFlow(currentUserId: String): Flow<List<ActiveChatSummary>> {
        val firestoreFlow: Flow<List<ActiveChatSummary>> = callbackFlow {
            if (currentUserId.isBlank()) {
                trySend(emptyList())
                close()
                return@callbackFlow
            }

            val listener: ListenerRegistration = firestore.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirestoreMessenger", "Chats listener error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot == null) return@addSnapshotListener

                    val chatsList = mutableListOf<ActiveChatSummary>()
                    for (doc in snapshot.documents) {
                        val chatId = doc.id
                        val lastMessageText = doc.getString("lastMessageText") ?: ""
                        val lastTimestamp = doc.getLong("lastTimestamp") ?: 0L
                        val lastSenderId = doc.getString("lastSenderId") ?: ""
                        val isPending = doc.metadata.hasPendingWrites()

                        val user1Id = doc.getString("user1Id") ?: ""
                        val user2Id = doc.getString("user2Id") ?: ""

                        val otherId = if (user1Id == currentUserId) user2Id else user1Id
                        val otherName = if (user1Id == currentUserId) {
                            doc.getString("user2Name") ?: "यूज़र"
                        } else {
                            doc.getString("user1Name") ?: "यूज़र"
                        }
                        val otherMobile = if (user1Id == currentUserId) {
                            doc.getString("user2Mobile") ?: ""
                        } else {
                            doc.getString("user1Mobile") ?: ""
                        }
                        val otherEmail = if (user1Id == currentUserId) {
                            doc.getString("user2Email") ?: ""
                        } else {
                            doc.getString("user1Email") ?: ""
                        }

                        if (otherId.isNotBlank() && otherId != currentUserId) {
                            chatsList.add(
                                ActiveChatSummary(
                                    chatId = chatId,
                                    otherUser = ChatUser(
                                        uid = otherId,
                                        name = otherName,
                                        mobile = otherMobile,
                                        email = otherEmail
                                    ),
                                    lastMessageText = lastMessageText,
                                    lastTimestamp = lastTimestamp,
                                    lastSenderId = lastSenderId,
                                    isPendingSync = isPending
                                )
                            )
                        }
                    }

                    trySend(chatsList)
                }

            awaitClose {
                listener.remove()
            }
        }

        // Combine Firestore chats with local P2P messages to guarantee offline chats appear in Clean Inbox
        return combine(firestoreFlow, _localP2PMessages) { fsChats, p2pMsgs ->
            val chatMap = fsChats.associateBy { it.chatId }.toMutableMap()

            // Group P2P messages by conversation pair
            val p2pConversations = p2pMsgs.filter {
                it.senderId == currentUserId || it.receiverId == currentUserId
            }.groupBy {
                val pair = listOf(it.senderId, it.receiverId).sorted()
                "${pair[0]}_${pair[1]}"
            }

            for ((chatId, msgs) in p2pConversations) {
                val lastP2PMsg = msgs.maxByOrNull { it.timestamp } ?: continue
                val existing = chatMap[chatId]
                if (existing == null || lastP2PMsg.timestamp > existing.lastTimestamp) {
                    val otherId = if (lastP2PMsg.senderId == currentUserId) lastP2PMsg.receiverId else lastP2PMsg.senderId
                    val otherName = if (lastP2PMsg.senderId == currentUserId) lastP2PMsg.receiverName else lastP2PMsg.senderName
                    val otherMobile = if (lastP2PMsg.senderId == currentUserId) lastP2PMsg.receiverMobile else lastP2PMsg.senderMobile

                    chatMap[chatId] = ActiveChatSummary(
                        chatId = chatId,
                        otherUser = ChatUser(
                            uid = otherId,
                            name = otherName.ifBlank { "Nearby User" },
                            mobile = otherMobile,
                            email = ""
                        ),
                        lastMessageText = lastP2PMsg.messageText,
                        lastTimestamp = lastP2PMsg.timestamp,
                        lastSenderId = lastP2PMsg.senderId,
                        isPendingSync = false
                    )
                }
            }

            chatMap.values.sortedByDescending { it.lastTimestamp }
        }
    }

    /**
     * Real-time flow of 1-on-1 messages between current user and target user.
     * Combines Firestore messages (with offline persistence) and offline P2P messages.
     */
    fun getMessagesFlow(currentUserId: String, otherUserId: String): Flow<List<PrivateMessage>> {
        val firestoreFlow: Flow<List<PrivateMessage>> = callbackFlow {
            val listener: ListenerRegistration = firestore.collection("messages")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirestoreMessenger", "Messages listener error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot == null) return@addSnapshotListener

                    val messages = mutableListOf<PrivateMessage>()
                    for (doc in snapshot.documents) {
                        val senderId = doc.getString("senderId") ?: ""
                        val receiverId = doc.getString("receiverId") ?: ""

                        val isBetweenTheseTwo = (senderId == currentUserId && receiverId == otherUserId) ||
                                (senderId == otherUserId && receiverId == currentUserId)

                        if (isBetweenTheseTwo) {
                            val messageText = doc.getString("messageText") ?: ""
                            val timestamp = doc.getLong("timestamp") ?: 0L
                            val senderName = doc.getString("senderName") ?: ""
                            val receiverName = doc.getString("receiverName") ?: ""
                            val senderMobile = doc.getString("senderMobile") ?: ""
                            val receiverMobile = doc.getString("receiverMobile") ?: ""
                            val isPending = doc.metadata.hasPendingWrites()
                            val transport = doc.getString("transport") ?: "CLOUD"

                            messages.add(
                                PrivateMessage(
                                    id = doc.id,
                                    senderId = senderId,
                                    receiverId = receiverId,
                                    messageText = messageText,
                                    timestamp = timestamp,
                                    senderName = senderName,
                                    receiverName = receiverName,
                                    senderMobile = senderMobile,
                                    receiverMobile = receiverMobile,
                                    isPendingSync = isPending,
                                    isOutgoing = (senderId == currentUserId),
                                    transport = transport
                                )
                            )
                        }
                    }

                    trySend(messages)
                }

            awaitClose {
                listener.remove()
            }
        }

        return combine(firestoreFlow, _localP2PMessages) { fsList, p2pList ->
            val relevantP2P = p2pList.filter {
                (it.senderId == currentUserId && it.receiverId == otherUserId) ||
                        (it.senderId == otherUserId && it.receiverId == currentUserId)
            }.map {
                it.copy(isOutgoing = (it.senderId == currentUserId))
            }

            // Merge and de-duplicate by ID
            val combinedMap = linkedMapOf<String, PrivateMessage>()
            for (msg in fsList) {
                combinedMap[msg.id] = msg
            }
            for (msg in relevantP2P) {
                // If not in Firestore yet, or preserve P2P badge
                val existing = combinedMap[msg.id]
                if (existing == null) {
                    combinedMap[msg.id] = msg
                } else if (msg.transport == "P2P") {
                    combinedMap[msg.id] = existing.copy(transport = "P2P")
                }
            }

            combinedMap.values.sortedBy { it.timestamp }
        }
    }
}
