import { useState } from "react";
import { Button, View } from "react-native";
import { Text } from "react-native-gesture-handler";
import Bluetooth, {
  BluetoothDevice,
  useBluetoothPermissions,
  fromShort,
} from "react-native-nitro-ble";

export default function Index() {
  const [granted, requestBluetoothPermissions] = useBluetoothPermissions();

  const [devices, setDevices] = useState<BluetoothDevice[]>([]);

  const getState = async () => {
    if (Bluetooth.state !== "enabled") return;

    if (!granted) {
      const granted = await requestBluetoothPermissions();
      if (!granted) return;
    }

    const scan = await Bluetooth.scan();

    scan.onDevice((device) => {
      setDevices((v) => [...v, device]);
    });

    setTimeout(async () => {
      await scan.stop();
    }, 5000);
  };

  const connect = async (device: BluetoothDevice) => {
    try {
      await device.connect();

      const deviceInformation = await device.service(fromShort("180a"));
      const modelNumberString = await deviceInformation.characteristic(
        fromShort("2a24")
      );
      const buffer = await modelNumberString.read();
      console.log(new TextDecoder().decode(buffer));

      await device.disconnect();
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <View
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
        gap: 20,
      }}
    >
      <Button onPress={getState} title="Scan devices!" />

      {devices.map((device) => (
        <Text key={device.address} onPress={() => connect(device)}>
          {device.knownName || "Unknown"} - {device.address}
        </Text>
      ))}
    </View>
  );
}
