package com.margelo.nitro.bluetooth

import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import com.facebook.react.bridge.ReactContext

class ScanHandleScanCallback(
    private val context: ReactContext,
    private val bluetoothManager: BluetoothManager,
    private val devices: MutableMap<String, HybridBluetoothDevice>,
    private val onDeviceListeners: MutableList<(device: HybridBluetoothDeviceSpec) -> Unit>,
    private val onErrorListeners: MutableList<(error: Double) -> Unit>
) : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)

        if (result == null) return

        val address = result.device.address
        if (devices.containsKey(address)) return

        try {
            val device =
                HybridBluetoothDevice(
                    context,
                    bluetoothManager,
                    result.device,
                    result.device.name ?: ""
                )
            devices[address] = device

            for (callback in onDeviceListeners) {
                callback(device)
            }
        } catch (_: SecurityException) {
        }

    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)

        for (listener in onErrorListeners) {
            listener(errorCode.toDouble())
        }
    }
}