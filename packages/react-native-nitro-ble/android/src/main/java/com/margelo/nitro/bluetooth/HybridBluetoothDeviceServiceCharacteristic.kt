package com.margelo.nitro.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.margelo.nitro.core.ArrayBuffer
import com.margelo.nitro.core.Promise

class HybridBluetoothDeviceServiceCharacteristic(
    private val gatt: BluetoothGatt,
    private val characteristic: BluetoothGattCharacteristic,
    private val pendingCharacteristicReadRequests: MutableList<Promise<ArrayBuffer>>
) : HybridBluetoothDeviceServiceCharacteristicSpec() {
    override fun read(): Promise<ArrayBuffer> {
        try {
            gatt.readCharacteristic(characteristic)
            val promise = Promise<ArrayBuffer>()
            pendingCharacteristicReadRequests.add(promise)
            return promise
        } catch (e: SecurityException) {
            return Promise.rejected(
                Exception(
                    "Missing connect permissions", e
                )
            )
        }
    }

    override fun write(value: ArrayBuffer): Promise<Unit> {
        val arrayBuffer = value.getBuffer(false).array()

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(
                    characteristic,
                    arrayBuffer,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
            } else {
                characteristic.value = arrayBuffer
                gatt.writeCharacteristic(
                    characteristic,
                )
            }

            return Promise.resolved(Unit)
        } catch (e: SecurityException) {
            return Promise.rejected(
                Exception(
                    "Missing connect permissions", e
                )
            )
        }
    }
}