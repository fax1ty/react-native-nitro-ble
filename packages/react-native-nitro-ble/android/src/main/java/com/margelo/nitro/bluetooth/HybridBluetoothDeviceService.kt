package com.margelo.nitro.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import com.margelo.nitro.core.ArrayBuffer
import com.margelo.nitro.core.Promise

class HybridBluetoothDeviceService(
    private val gatt: BluetoothGatt,
    private val service: BluetoothGattService,
    private val pendingCharacteristicReadRequests: MutableList<Promise<ArrayBuffer>>,

    ) : HybridBluetoothDeviceServiceSpec() {


    override fun characteristic(uuid: String): Promise<HybridBluetoothDeviceServiceCharacteristicSpec> {
        val characteristic = service.characteristics.find { it.uuid.toString() == uuid }
        if (characteristic == null) return Promise.rejected(Exception("Characteristic not found"))

        return Promise.resolved(
            HybridBluetoothDeviceServiceCharacteristic(
                gatt,
                characteristic,
                pendingCharacteristicReadRequests
            )
        )
    }
}