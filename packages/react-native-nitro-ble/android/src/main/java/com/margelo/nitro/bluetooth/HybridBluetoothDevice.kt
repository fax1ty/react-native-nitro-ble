package com.margelo.nitro.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.util.Log
import com.facebook.react.bridge.ReactContext
import com.margelo.nitro.core.ArrayBuffer
import com.margelo.nitro.core.Promise

class HybridBluetoothDevice(
    private val context: ReactContext,
    private val bluetoothManager: BluetoothManager,
    private val device: BluetoothDevice,
    override val knownName: String,
) : HybridBluetoothDeviceSpec() {
    override val address: String = device.address

    private var gatt: BluetoothGatt? = null
    override val state: BluetoothDeviceState
        get() {
            try {
                val connected = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
                return when (connected.contains(device)) {
                    true -> BluetoothDeviceState.CONNECTED
                    false -> BluetoothDeviceState.DISCONNECTED
                }
            } catch (e: SecurityException) {
                return BluetoothDeviceState.DISCONNECTED
            }
        }

    private val pendingCharacteristicReadRequests = mutableListOf<Promise<ArrayBuffer>>()
    private val pendingServicesDiscoveryRequests =
        mutableListOf<Triple<BluetoothGatt, String, Promise<HybridBluetoothDeviceServiceSpec>>>()
    private val pendingConnectionStateChangeRequests = mutableListOf<Promise<Unit>>()

    override fun connect(): Promise<Unit> {
        try {
            if (state == BluetoothDeviceState.CONNECTED) return Promise.resolved(Unit)

            if (gatt != null) {
                gatt!!.close()
                gatt = null
            }

            gatt = device.connectGatt(
                context, false, BluetoothDeviceGattCallback(
                    pendingCharacteristicReadRequests,
                    pendingServicesDiscoveryRequests,
                    pendingConnectionStateChangeRequests
                )
            )
            val promise = Promise<Unit>()
            pendingConnectionStateChangeRequests.add(promise)
            return promise
        } catch (e: SecurityException) {
            return Promise.rejected(
                Exception(
                    "Missing connect permissions", e
                )
            )
        }
    }

    override fun disconnect(): Promise<Unit> {
        if (gatt == null) return Promise.rejected(Exception("Can't disconnect, not connected"))

        try {
            gatt!!.close()
            gatt = null
            return Promise.resolved(Unit)
        } catch (e: SecurityException) {
            return Promise.rejected(
                Exception(
                    "Missing connect permissions", e
                )
            )
        }
    }

    override fun service(uuid: String): Promise<HybridBluetoothDeviceServiceSpec> {
        if (gatt == null) {
            Log.e("NitroBluetooth", "Received service call before GATT is connected!")
            return Promise.rejected(Exception("Can't get service, not connected"))
        }

        val service = gatt!!.services.find { it.uuid.toString() == uuid }

        try {
            if (service == null) {
                Log.i("NitroBluetooth", "Discovering services...")
                gatt!!.discoverServices()
                val promise = Promise<HybridBluetoothDeviceServiceSpec>()
                pendingServicesDiscoveryRequests.add(Triple(gatt!!, uuid, promise))
                return promise
            } else {
                Log.i("NitroBluetooth", "Service is already discovered. Creating new Hybrid object")
                return Promise.resolved(
                    HybridBluetoothDeviceService(
                        gatt!!, service, pendingCharacteristicReadRequests
                    )
                )
            }
        } catch (e: SecurityException) {
            return Promise.rejected(
                Exception(
                    "Missing connect permissions", e
                )
            )
        }
    }


    override fun dispose() {
        super.dispose()

        try {
            if (gatt != null) gatt!!.close()
        } catch (_: SecurityException) {
        }
    }
}