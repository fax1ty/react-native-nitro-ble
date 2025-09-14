<div align="center">
  <h1 align="center">Bluetooth</h1>
</div>

<br/>

Why have only `TurboModule` version of something when you can also use modern _(read: unstable)_ technology written in `Nitro`?

## Features

- Get characteristics as **ArrayBuffers**
- Auto discovery with easier API

> [!WARNING]
>
> This is a very unstable. Use it at your **own risk**. At the moment, only the Android support is available. The iOS will be added in 0.0.2.

## Installation

### React Native

```sh
npm install react-native-nitro-ble react-native-nitro-modules
cd ios && pod install
```

### Expo

```sh
npx expo install react-native-nitro-ble react-native-nitro-modules
npx expo prebuild
```

## Usage

### Check for bluetooth adapter state

```ts
import Bluetooth from "react-native-nitro-ble";
Bluetooth.state === "enabled";
```

### Reuqest permissions

```tsx
import { useBluetoothPermissions } from "react-native-nitro-ble";
function ReactComponent() {
  const [granted, requestBluetoothPermissions] = useBluetoothPermissions();

  const getPermissions = () => {
    if (!granted) {
      const granted = await requestBluetoothPermissions();
      if (!granted) return;
    }
  };
}
```

> [!WARNING]
>
> Most likely, this API will be considered for moving to an asynchronous approach.

### Start scan

```ts
const scan = await Bluetooth.scan();
```

#### Listen for new devices

```ts
scan.onDevice((device) => {
  // New device is here!
});
```

The current behavior is not to re-discover devices that have already been discovered. You can safely all devices to your array.

#### Get devices

```ts
Bluetooth.devices();
```

> [!NOTE]
>
> Devices are created in their initial states. Before you can use them, you need to connect them!

#### Stop the scan

```ts
await scan.stop();
```

### Connect to device

```ts
await device.connect();
```

Consider wrapping this call in a try/catch block, as it may throw an error related to the connection. We are listening for the connection state change event in this function, so we don't need to worry about premature data transmission requests.

### Get service

```ts
import { fromShort } from "react-native-nitro-ble";
const service = await device.service(fromShort("180a"));
```

`fromShort` is a small utility that makes your short UUID compatible with the long form. If GATT services have not been discovered for the current device yet, it will first discover all services and characteristics. There is no need to worry about this.

### Get characteristic

```ts
const characteristic = await service.characteristic(fromShort("2a24"));
```

As mentioned before, you can obtain the desired characteristics without any worries. If they don't exist, you will just get an error.

#### Read value

```ts
const buffer = await characteristic.read(); // -> ArrayBuffer
const text = new TextDecoder().decode(buffer);
```

> [!NOTE]
>
> For now, we are performing a single memory copy of the system's data. I will revisit this when I have new ideas.  
> Most likely, I will also provide a more convenient interface for automatically decoding text values. Something like `characteristic.read('utf-8')`

### Disconnect from device

```ts
await device.disconnect();
```

## Note

In most cases, the disposal of memory and internal objects is already handled by the library. You can safely rely on the garbage collector (GC) and simply drop values when they are no longer needed.
