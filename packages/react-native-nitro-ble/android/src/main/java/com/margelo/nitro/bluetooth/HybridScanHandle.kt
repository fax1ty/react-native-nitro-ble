package com.margelo.nitro.bluetooth

import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import com.margelo.nitro.core.Promise

class HybridScanHandle(
    private val bluetoothManager: BluetoothManager,
    private val scanCallback: ScanCallback,
    private val onDeviceListeners: MutableList<(device: HybridBluetoothDeviceSpec) -> Unit>,
    private val onErrorListeners: MutableList<(error: Double) -> Unit>
) : HybridScanHandleSpec() {
    override fun stop(): Promise<Unit> {
        try {
            bluetoothManager.adapter.bluetoothLeScanner.stopScan(scanCallback)
            return Promise.resolved(Unit)
        } catch (e: SecurityException) {
            return Promise.rejected(Exception("Missing scan permissions", e))
        }
    }

    override fun onDevice(callback: (device: HybridBluetoothDeviceSpec) -> Unit): () -> Unit {
        onDeviceListeners.add(callback)
        return { onDeviceListeners.remove(callback) }
    }

    override fun onError(callback: (error: Double) -> Unit): () -> Unit {
        onErrorListeners.add(callback)
        return { onErrorListeners.remove(callback) }
    }
}