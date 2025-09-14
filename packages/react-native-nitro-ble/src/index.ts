import { NitroModules } from "react-native-nitro-modules";
import type {
  Bluetooth as BluetoothSpec,
  BluetoothDevice,
} from "./specs/Bluetooth.nitro";

const Bluetooth = NitroModules.createHybridObject<BluetoothSpec>("Bluetooth");

export default Bluetooth;

export { useBluetoothPermissions } from "./permissions";
export { fromShort } from "./uuid";
export type { BluetoothDevice };
