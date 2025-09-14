package com.margelo.nitro.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import com.facebook.react.bridge.ReactContext
import com.margelo.nitro.NitroModules
import com.margelo.nitro.core.Promise

class HybridBluetooth : HybridBluetoothSpec() {
    private val context: ReactContext = NitroModules.applicationContext
        ?: throw IllegalStateException("applicationContext not available")
    private var bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val devices = mutableMapOf<String, HybridBluetoothDevice>()

    override val state: AdapterState
        get() {
            return when (bluetoothManager.adapter.state) {
                BluetoothAdapter.STATE_OFF -> AdapterState.DISABLED
                BluetoothAdapter.STATE_TURNING_ON -> AdapterState.TURNING_ON
                BluetoothAdapter.STATE_ON -> AdapterState.ENABLED
                BluetoothAdapter.STATE_TURNING_OFF -> AdapterState.TURNING_OFF
                else -> {
                    throw IllegalStateException("Unknown Bluetooth adapter state")
                }
            }
        }

    override fun scan(): Promise<HybridScanHandleSpec> {
        val onDeviceListeners = mutableListOf<(device: HybridBluetoothDeviceSpec) -> Unit>()
        val onErrorListeners = mutableListOf<(error: Double) -> Unit>()

        val scanCallback =
            ScanHandleScanCallback(context, bluetoothManager, devices, onDeviceListeners, onErrorListeners)
        val scanHandle =
            HybridScanHandle(bluetoothManager, scanCallback, onDeviceListeners, onErrorListeners)

        try {
            bluetoothManager.adapter.bluetoothLeScanner.startScan(scanCallback)
        } catch (e: SecurityException) {
            return Promise.rejected(
                Exception(
                    "Missing scan permissions", e
                )
            )
        }

        return Promise.resolved(scanHandle)
    }

    override fun devices(): Array<HybridBluetoothDeviceSpec> {
        return devices.values.toTypedArray()
    }
}
