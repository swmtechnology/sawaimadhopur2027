package com.example.chat.data

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Peer representation for Google Nearby Connections
 */
data class NearbyPeer(
    val endpointId: String,
    val userId: String,
    val name: String,
    val mobile: String = "",
    val isConnected: Boolean = false
)

/**
 * Manages Google Nearby Connections API for 100% offline peer-to-peer messaging
 * using WiFi Direct and Bluetooth.
 */
class NearbyConnectionsManager(private val context: Context) {

    companion object {
        private const val TAG = "NearbyP2PManager"
        const val SERVICE_ID = "com.example.swm.nearbychat"
        val STRATEGY: Strategy = Strategy.P2P_CLUSTER
    }

    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context.applicationContext)
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    // State flows for Compose UI
    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    // Discovered nearby endpoints awaiting connection
    private val _discoveredPeers = MutableStateFlow<Map<String, NearbyPeer>>(emptyMap())
    val discoveredPeers: StateFlow<Map<String, NearbyPeer>> = _discoveredPeers.asStateFlow()

    // Fully connected endpoints
    private val _connectedPeers = MutableStateFlow<Map<String, NearbyPeer>>(emptyMap())
    val connectedPeers: StateFlow<Map<String, NearbyPeer>> = _connectedPeers.asStateFlow()

    // Incoming messages stream
    private val _incomingMessages = MutableSharedFlow<PrivateMessage>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<PrivateMessage> = _incomingMessages.asSharedFlow()

    private var currentUser: ChatUser? = null

    // Endpoint Discovery Callback
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "Endpoint found: $endpointId (${info.endpointName})")
            val peer = parseEndpointInfo(endpointId, info.endpointName, isConnected = false)
            _discoveredPeers.value = _discoveredPeers.value + (endpointId to peer)

            // Automatically request connection to establish offline mesh
            currentUser?.let { localUser ->
                val localNamePayload = formatEndpointName(localUser)
                connectionsClient.requestConnection(localNamePayload, endpointId, connectionLifecycleCallback)
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Request connection failed for $endpointId: ${e.message}")
                    }
            }
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "Endpoint lost: $endpointId")
            _discoveredPeers.value = _discoveredPeers.value - endpointId
            _connectedPeers.value = _connectedPeers.value - endpointId
        }
    }

    // Payload Callback for incoming messages
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                payload.asBytes()?.let { bytes ->
                    try {
                        val jsonStr = String(bytes, Charsets.UTF_8)
                        val json = JSONObject(jsonStr)
                        if (json.optString("type") == "CHAT_MSG") {
                            val msg = PrivateMessage(
                                id = json.optString("id", "p2p_${System.currentTimeMillis()}"),
                                senderId = json.optString("senderId"),
                                receiverId = json.optString("receiverId"),
                                messageText = json.optString("messageText"),
                                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                                senderName = json.optString("senderName", "Nearby User"),
                                receiverName = json.optString("receiverName", ""),
                                senderMobile = json.optString("senderMobile", ""),
                                receiverMobile = json.optString("receiverMobile", ""),
                                isPendingSync = false,
                                isOutgoing = false,
                                transport = "P2P"
                            )
                            Log.d(TAG, "P2P message received from $endpointId: ${msg.messageText}")
                            scope.launch {
                                _incomingMessages.emit(msg)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse incoming payload: ${e.message}", e)
                    }
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                Log.d(TAG, "Payload transfer to $endpointId succeeded")
            }
        }
    }

    // Connection Lifecycle Callback
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(TAG, "Connection initiated by $endpointId (${info.endpointName})")
            // Automatically accept connection for zero-click offline chatting
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener {
                    val peer = parseEndpointInfo(endpointId, info.endpointName, isConnected = false)
                    _discoveredPeers.value = _discoveredPeers.value + (endpointId to peer)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Accept connection failed for $endpointId: ${e.message}")
                }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                Log.d(TAG, "Connected to $endpointId successfully!")
                val existing = _discoveredPeers.value[endpointId]
                val connectedPeer = existing?.copy(isConnected = true) ?: NearbyPeer(
                    endpointId = endpointId,
                    userId = endpointId,
                    name = "Nearby User",
                    isConnected = true
                )
                _connectedPeers.value = _connectedPeers.value + (endpointId to connectedPeer)
            } else {
                Log.w(TAG, "Connection to $endpointId failed: ${result.status.statusCode}")
                _connectedPeers.value = _connectedPeers.value - endpointId
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "Disconnected from $endpointId")
            _connectedPeers.value = _connectedPeers.value - endpointId
            _discoveredPeers.value = _discoveredPeers.value - endpointId
        }
    }

    /**
     * Starts advertising and discovery for offline P2P messaging.
     */
    fun startP2P(user: ChatUser) {
        currentUser = user
        startAdvertising(user)
        startDiscovery()
    }

    /**
     * Starts advertising this device to nearby users
     */
    private fun startAdvertising(user: ChatUser) {
        val endpointName = formatEndpointName(user)
        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(STRATEGY)
            .build()

        connectionsClient.startAdvertising(
            endpointName,
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener {
            Log.d(TAG, "Advertising started successfully as '$endpointName'")
            _isAdvertising.value = true
        }.addOnFailureListener { e ->
            Log.w(TAG, "Failed to start advertising: ${e.message}")
            _isAdvertising.value = false
        }
    }

    /**
     * Starts discovering other nearby devices
     */
    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(STRATEGY)
            .build()

        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnSuccessListener {
            Log.d(TAG, "Discovery started successfully")
            _isDiscovering.value = true
        }.addOnFailureListener { e ->
            Log.w(TAG, "Failed to start discovery: ${e.message}")
            _isDiscovering.value = false
        }
    }

    /**
     * Stops advertising, discovery, and disconnects all endpoints.
     */
    fun stopAll() {
        try {
            connectionsClient.stopAdvertising()
            connectionsClient.stopDiscovery()
            connectionsClient.stopAllEndpoints()
            _isAdvertising.value = false
            _isDiscovering.value = false
            _connectedPeers.value = emptyMap()
            _discoveredPeers.value = emptyMap()
            Log.d(TAG, "Nearby connections stopped")
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping nearby connections: ${e.message}")
        }
    }

    /**
     * Sends a private message via Offline P2P (WiFi Direct / Bluetooth).
     * Automatically resolves the connected endpoint for the target user,
     * or broadcasts to all connected peers if specific endpoint is not mapped.
     */
    fun sendP2PMessage(message: PrivateMessage): Boolean {
        val connectedMap = _connectedPeers.value
        if (connectedMap.isEmpty()) {
            Log.w(TAG, "Cannot send P2P message: No connected peers")
            return false
        }

        val json = JSONObject().apply {
            put("type", "CHAT_MSG")
            put("id", message.id)
            put("senderId", message.senderId)
            put("receiverId", message.receiverId)
            put("messageText", message.messageText)
            put("timestamp", message.timestamp)
            put("senderName", message.senderName)
            put("receiverName", message.receiverName)
            put("senderMobile", message.senderMobile)
            put("receiverMobile", message.receiverMobile)
        }

        val payload = Payload.fromBytes(json.toString().toByteArray(Charsets.UTF_8))

        // Find endpoint for target receiver
        val targetEndpoint = connectedMap.values.firstOrNull {
            it.userId == message.receiverId ||
                    (it.mobile.isNotBlank() && it.mobile == message.receiverMobile)
        }

        return if (targetEndpoint != null) {
            connectionsClient.sendPayload(targetEndpoint.endpointId, payload)
            Log.d(TAG, "Sent P2P payload directly to target endpoint: ${targetEndpoint.endpointId}")
            true
        } else {
            // Send to all connected peers in cluster mesh
            val endpointIds = connectedMap.keys.toList()
            connectionsClient.sendPayload(endpointIds, payload)
            Log.d(TAG, "Sent P2P payload to ${endpointIds.size} connected peers")
            true
        }
    }

    /**
     * Checks if target receiver is currently connected via Offline P2P
     */
    fun isUserConnectedP2P(userId: String, mobile: String): Boolean {
        return _connectedPeers.value.values.any {
            it.userId == userId || (mobile.isNotBlank() && it.mobile == mobile)
        }
    }

    private fun formatEndpointName(user: ChatUser): String {
        // Encode user details: uid|name|mobile
        val cleanName = user.name.replace("|", "_")
        val cleanMobile = user.mobile.replace("|", "_")
        return "${user.uid}|$cleanName|$cleanMobile"
    }

    private fun parseEndpointInfo(endpointId: String, endpointName: String, isConnected: Boolean): NearbyPeer {
        return try {
            val parts = endpointName.split("|")
            if (parts.size >= 3) {
                NearbyPeer(
                    endpointId = endpointId,
                    userId = parts[0],
                    name = parts[1],
                    mobile = parts[2],
                    isConnected = isConnected
                )
            } else {
                NearbyPeer(
                    endpointId = endpointId,
                    userId = endpointId,
                    name = endpointName.ifBlank { "Nearby User" },
                    isConnected = isConnected
                )
            }
        } catch (_: Exception) {
            NearbyPeer(
                endpointId = endpointId,
                userId = endpointId,
                name = endpointName.ifBlank { "Nearby User" },
                isConnected = isConnected
            )
        }
    }
}
