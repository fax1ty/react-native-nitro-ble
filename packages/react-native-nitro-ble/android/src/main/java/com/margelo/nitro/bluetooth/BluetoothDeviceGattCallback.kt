package com.margelo.nitro.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.util.Log
import com.margelo.nitro.core.ArrayBuffer
import com.margelo.nitro.core.Promise
import java.nio.ByteBuffer.allocateDirect

class BluetoothDeviceGattCallback(
    private val pendingCharacteristicReadRequests: MutableList<Promise<ArrayBuffer>>,
    private val pendingServicesDiscoveryRequests: MutableList<Triple<BluetoothGatt, String, Promise<HybridBluetoothDeviceServiceSpec>>>,
    private val pendingConnectionStateChangeRequests: MutableList<Promise<Unit>>
) : BluetoothGattCallback() {
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)

        Log.i("NitroBluetooth", "New connection state: $newState with status $status")

        for (request in pendingConnectionStateChangeRequests) {
            if (newState == BluetoothProfile.STATE_CONNECTED) request.resolve(
                Unit
            )
            else request.reject(Exception("Connection failed with status $status"))

            pendingConnectionStateChangeRequests.remove(request)
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, value, status)

        for (request in pendingCharacteristicReadRequests) {
            val buffer = allocateDirect(value.size)
            buffer.put(value)
            buffer.flip()
            request.resolve(ArrayBuffer.wrap(buffer))
            pendingCharacteristicReadRequests.remove(request)
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)

        Log.i("NitroBluetooth", "New service discovered. Status: $status")

        for (triple in pendingServicesDiscoveryRequests) {
            val (requestGatt, uuid, request) = triple
            if (gatt != requestGatt) continue

            val service = gatt.services.find { it.uuid.toString() == uuid }
            if (service == null) return request.reject(Exception("Service not found"))

            pendingServicesDiscoveryRequests.remove(triple)

            request.resolve(
                HybridBluetoothDeviceService(
                    gatt, service, pendingCharacteristicReadRequests
                )
            )
        }
    }
}