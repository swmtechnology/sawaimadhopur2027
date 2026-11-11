package com.example.chat.p2p

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.example.chat.model.ChatMessage
import com.example.chat.model.ChatTransportType
import com.example.chat.model.NearbyPeerDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.Collections

class OfflineP2PService(private val context: Context) {

    companion object {
        @Volatile
        private var instance: OfflineP2PService? = null

        fun getInstance(context: Context): OfflineP2PService {
            return instance ?: synchronized(this) {
                instance ?: OfflineP2PService(context.applicationContext).also { instance = it }
            }
        }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())
    private var serverSocket: ServerSocket? = null
    private var isServerRunning = false

    private val _localIpAddress = MutableStateFlow(getDeviceLocalIp())
    val localIpAddress: StateFlow<String> = _localIpAddress.asStateFlow()

    private val _isServerActive = MutableStateFlow(false)
    val isServerActive: StateFlow<Boolean> = _isServerActive.asStateFlow()

    private val _nearbyPeers = MutableStateFlow<List<NearbyPeerDevice>>(emptyList())
    val nearbyPeers: StateFlow<List<NearbyPeerDevice>> = _nearbyPeers.asStateFlow()

    var onMessageReceivedListener: ((ChatMessage) -> Unit)? = null

    init {
        startLocalWifiServer()
        refreshNearbyPeers()
    }

    /**
     * Finds the real device IP on local Wi-Fi or Hotspot network (e.g. 192.168.x.x or 10.x.x.x)
     */
    fun getDeviceLocalIp(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val host = addr.hostAddress ?: ""
                        if (host.isNotEmpty() && !host.startsWith("127.")) {
                            return host
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("OfflineP2P", "Error getting local IP: ${e.message}")
        }
        return "192.168.43.1 (हॉटस्पॉट डिफ़ॉल्ट)"
    }

    /**
     * Start TCP ServerSocket on port 8988 to receive messages over WiFi / Hotspot without Internet.
     * Uses SO_REUSEADDR and fallback ports to prevent EADDRINUSE errors.
     */
    fun startLocalWifiServer(port: Int = 8988) {
        if (isServerRunning && serverSocket?.isBound == true && !serverSocket!!.isClosed) return
        coroutineScope.launch {
            val candidatePorts = listOf(port, port + 1, port + 2, 0)
            var socketCreated: ServerSocket? = null
            for (p in candidatePorts) {
                try {
                    val s = ServerSocket()
                    s.reuseAddress = true
                    s.bind(InetSocketAddress(p))
                    socketCreated = s
                    Log.d("OfflineP2P", "WiFi Direct / Hotspot Server started on port ${s.localPort}, IP: ${_localIpAddress.value}")
                    break
                } catch (e: Exception) {
                    Log.d("OfflineP2P", "Port $p unavailable (${e.message}), trying alternative")
                }
            }

            if (socketCreated == null) {
                Log.w("OfflineP2P", "Could not bind server socket; another instance is active")
                _isServerActive.value = false
                return@launch
            }

            serverSocket = socketCreated
            isServerRunning = true
            _isServerActive.value = true
            _localIpAddress.value = getDeviceLocalIp()

            while (isServerRunning && serverSocket != null && !serverSocket!!.isClosed) {
                try {
                    val clientSocket = serverSocket!!.accept()
                    handleIncomingConnection(clientSocket)
                } catch (e: Exception) {
                    if (!isServerRunning) break
                }
            }
        }
    }

    fun stopLocalWifiServer() {
        isServerRunning = false
        _isServerActive.value = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
    }

    private fun handleIncomingConnection(socket: Socket) {
        coroutineScope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val rawPayload = reader.readLine()
                socket.close()

                if (!rawPayload.isNullOrBlank()) {
                    val json = JSONObject(rawPayload)
                    val senderName = json.optString("senderName", "आस-पास का यूज़र (WiFi Peer)")
                    val contactId = json.optString("contactId", "nearby_peer_wifi")
                    val text = json.optString("text", "")
                    val transportStr = json.optString("transport", "WIFI_LAN_P2P")

                    val transport = when (transportStr) {
                        "BLUETOOTH_P2P" -> ChatTransportType.BLUETOOTH_P2P
                        else -> ChatTransportType.WIFI_LAN_P2P
                    }

                    val incomingMsg = ChatMessage(
                        contactId = contactId,
                        senderName = senderName,
                        text = text,
                        timestamp = System.currentTimeMillis(),
                        isOutgoing = false,
                        isDelivered = true,
                        isSeen = true,
                        transportType = transport
                    )

                    withContext(Dispatchers.Main) {
                        onMessageReceivedListener?.invoke(incomingMsg)
                    }
                }
            } catch (e: Exception) {
                Log.e("OfflineP2P", "Error reading incoming socket: ${e.message}")
            }
        }
    }

    /**
     * Send message over local WiFi / Hotspot directly to peer IP address on port 8988
     */
    suspend fun sendMessageOverWifi(
        targetIp: String,
        text: String,
        senderName: String,
        port: Int = 8988
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val socket = Socket(targetIp, port)
            socket.soTimeout = 4000
            val payload = JSONObject().apply {
                put("senderName", senderName)
                put("text", text)
                put("timestamp", System.currentTimeMillis())
                put("transport", "WIFI_LAN_P2P")
            }

            val writer = PrintWriter(socket.getOutputStream(), true)
            writer.println(payload.toString())
            writer.flush()
            socket.close()
            true
        } catch (e: Exception) {
            Log.e("OfflineP2P", "Failed to send over WiFi to $targetIp: ${e.message}")
            false
        }
    }

    /**
     * Scan and refresh nearby WiFi & Bluetooth peers
     */
    fun refreshNearbyPeers() {
        coroutineScope.launch {
            val list = mutableListOf<NearbyPeerDevice>()
            val currentIp = getDeviceLocalIp()
            _localIpAddress.value = currentIp

            // 1. Local Wi-Fi Mesh Gateway Peer
            list.add(
                NearbyPeerDevice(
                    id = "wifi_hotspot_host",
                    name = "सवाई माधोपुर लोकल WiFi हॉटस्पॉट",
                    type = "WIFI_HOTSPOT",
                    addressOrIp = currentIp,
                    signalStrength = "100% फुल सिग्नल (0 डेटा यूसेज)",
                    isConnected = true
                )
            )

            // 2. Bluetooth Paired / Nearby Devices
            try {
                val btAdapter = BluetoothAdapter.getDefaultAdapter()
                if (btAdapter != null && btAdapter.isEnabled) {
                    val bonded = btAdapter.bondedDevices
                    if (!bonded.isNullOrEmpty()) {
                        for (device in bonded) {
                            list.add(
                                NearbyPeerDevice(
                                    id = "bt_${device.address}",
                                    name = device.name ?: "ब्लूटूथ डिवाइस",
                                    type = "BLUETOOTH",
                                    addressOrIp = device.address,
                                    signalStrength = "पेयर किया गया (Paired)",
                                    isConnected = false
                                )
                            )
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.d("OfflineP2P", "Bluetooth permission not yet granted: ${e.message}")
            }

            // 3. Nearby simulated community peers for instant testing in Sawai Madhopur
            list.add(
                NearbyPeerDevice(
                    id = "swm_nearby_peer_1",
                    name = "रणथंभौर सफारी गेट (लोकल मेश)",
                    type = "WIFI_HOTSPOT",
                    addressOrIp = "192.168.43.102",
                    signalStrength = "समीप (95% सिग्नल)",
                    isConnected = false
                )
            )
            list.add(
                NearbyPeerDevice(
                    id = "swm_nearby_peer_2",
                    name = "होटल व सिटी हार्ट P2P नोड",
                    type = "BLUETOOTH",
                    addressOrIp = "BT:88:42:AC:71",
                    signalStrength = "समीप (Bluetooth BLE)",
                    isConnected = false
                )
            )

            _nearbyPeers.value = list
        }
    }

    fun stopServer() {
        try {
            isServerRunning = false
            serverSocket?.close()
            serverSocket = null
            _isServerActive.value = false
        } catch (e: Exception) {
            Log.e("OfflineP2P", "Error stopping server: ${e.message}")
        }
    }
}
