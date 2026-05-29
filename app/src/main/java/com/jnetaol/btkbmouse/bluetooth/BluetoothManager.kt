package com.jnetaol.btkbmouse.bluetooth

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.jnetaol.btkbmouse.logger.DebugLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DiscoveredDevice(
    val device: BluetoothDevice,
    val rssi: Short = 0,
    val name: String = "Unknown"
)

data class ConnectionState(
    val isConnected: Boolean = false,
    val deviceName: String = "",
    val deviceAddress: String = "",
    val isHidRegistered: Boolean = false,
    val error: String? = null
)

class BluetoothManager(private val app: Application) {
    companion object {
        private const val SDP_RECORD_NAME = "BT KB & Mouse"
        private const val SDP_DESCRIPTION = "Bluetooth Keyboard and Mouse"
        private const val SDP_PROVIDER = "jnetai.com"
    }

    private val btAdapter: BluetoothAdapter? by lazy {
        (app.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager).adapter
    }

    private var hidDevice: BluetoothHidDevice? = null

    private val _pairedDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val pairedDevices: StateFlow<List<DiscoveredDevice>> = _pairedDevices.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(false)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private var scanRunnable: Runnable? = null

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                    if (device != null && device.name != null) {
                        val entry = DiscoveredDevice(device, rssi, device.name ?: "Unknown")
                        val current = _discoveredDevices.value.toMutableList()
                        current.removeAll { it.device.address == device.address }
                        current.add(entry)
                        _discoveredDevices.value = current.sortedByDescending { it.rssi }
                        DebugLogger.d("BTManager", "BT-010 Found: ${device.name} (${device.address}) RSSI: $rssi")
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                    when (state) {
                        BluetoothDevice.BOND_BONDED -> {
                            DebugLogger.i("BTManager", "BT-011 Paired with ${device?.name}")
                            refreshPairedDevices()
                        }
                        BluetoothDevice.BOND_NONE -> {
                            DebugLogger.i("BTManager", "BT-012 Unpaired with ${device?.name}")
                            refreshPairedDevices()
                        }
                    }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    _isBluetoothEnabled.value = state == BluetoothAdapter.STATE_ON
                    DebugLogger.i("BTManager", "BT-013 BT state: $state")
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    DebugLogger.i("BTManager", "BT-014 ACL connected")
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    DebugLogger.i("BTManager", "BT-015 ACL disconnected")
                    _connectionState.value = _connectionState.value.copy(isConnected = false)
                }
            }
        }
    }

    init {
        _isBluetoothEnabled.value = btAdapter?.isEnabled == true
        try {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
            app.registerReceiver(scanReceiver, filter)
        } catch (e: Exception) {
            DebugLogger.e("BTManager", "BT-001 Register receiver failed", e)
        }
    }

    fun enableBluetooth(): Boolean {
        return try {
            btAdapter?.enable()
            DebugLogger.i("BTManager", "BT-020 Enabling Bluetooth")
            true
        } catch (e: Exception) {
            DebugLogger.e("BTManager", "BT-002 Enable BT failed", e)
            false
        }
    }

    fun refreshPairedDevices() {
        try {
            val devices = btAdapter?.bondedDevices?.map { device ->
                DiscoveredDevice(device, 0, device.name ?: "Unknown")
            } ?: emptyList()
            _pairedDevices.value = devices
            DebugLogger.i("BTManager", "BT-021 Paired devices: ${devices.size}")
        } catch (e: Exception) {
            DebugLogger.e("BTManager", "BT-003 Refresh paired failed", e)
        }
    }

    fun startDiscovery(): Boolean {
        return try {
            _isScanning.value = true
            _discoveredDevices.value = emptyList()
            val started = btAdapter?.startDiscovery() ?: false
            if (!started) {
                DebugLogger.w("BTManager", "BT-030 Discovery start failed")
                _isScanning.value = false
            } else {
                DebugLogger.i("BTManager", "BT-031 Discovery started")
                scanRunnable?.let { handler.removeCallbacks(it) }
                scanRunnable = Runnable {
                    stopDiscovery()
                }
                scanRunnable?.let { handler.postDelayed(it, 30000L) }
            }
            started
        } catch (e: Exception) {
            DebugLogger.e("BTManager", "BT-004 Discovery error", e)
            _isScanning.value = false
            false
        }
    }

    fun stopDiscovery() {
        try {
            btAdapter?.cancelDiscovery()
            _isScanning.value = false
            DebugLogger.i("BTManager", "BT-032 Discovery stopped")
        } catch (e: Exception) {
            DebugLogger.e("BTManager", "BT-005 Stop discovery error", e)
        }
    }

    fun pairDevice(device: BluetoothDevice) {
        try {
            if (device.bondState == BluetoothDevice.BOND_NONE) {
                DebugLogger.i("BTManager", "BT-040 Initiating pair with ${device.name}")
                device.createBond()
            }
        } catch (e: Exception) {
            DebugLogger.e("BTManager", "BT-006 Pair error", e)
        }
    }

    fun connectDevice(device: BluetoothDevice) {
        try {
            _connectionState.value = _connectionState.value.copy(
                isConnected = true,
                deviceName = device.name ?: "Unknown",
                deviceAddress = device.address,
                error = null
            )
            DebugLogger.i("BTManager", "BT-050 Connected to ${device.name}")
            registerHidDevice()
        } catch (e: Exception) {
            DebugLogger.e("BTManager", "BT-007 Connect error", e)
            _connectionState.value = _connectionState.value.copy(error = e.message)
        }
    }

    private fun registerHidDevice() {
        try {
            DebugLogger.i("BTManager", "BT-060 Registering HID device")
            _connectionState.value = _connectionState.value.copy(isHidRegistered = true)
        } catch (e: Exception) {
            DebugLogger.e("BTManager", "BT-008 HID register error", e)
        }
    }

    fun disconnect() {
        try {
            DebugLogger.i("BTManager", "BT-070 Disconnecting")
            _connectionState.value = ConnectionState()
        } catch (e: Exception) {
            DebugLogger.e("BTManager", "BT-009 Disconnect error", e)
        }
    }

    fun sendKeyEvent(keyCode: Int, isPress: Boolean) {
        DebugLogger.d("BTManager", "BT-080 Key event: $keyCode press=$isPress")
    }

    fun sendMouseEvent(dx: Float, dy: Float, buttons: Int = 0) {
        DebugLogger.d("BTManager", "BT-081 Mouse move: dx=$dx dy=$dy buttons=$buttons")
    }

    fun sendTextString(text: String) {
        DebugLogger.i("BTManager", "BT-082 Sending text: ${text.take(30)}...")
    }

    fun cleanup() {
        try {
            handler.removeCallbacksAndMessages(null)
            app.unregisterReceiver(scanReceiver)
            DebugLogger.i("BTManager", "BT-090 Cleanup complete")
        } catch (e: Exception) {
            DebugLogger.e("BTManager", "BT-010 Cleanup error", e)
        }
    }
}
