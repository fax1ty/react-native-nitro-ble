import type { HybridObject } from "react-native-nitro-modules";

export type AdapterState =
  | "disabled"
  | "turning-on"
  | "enabled"
  | "turning-off";

export interface Bluetooth
  extends HybridObject<{ android: "kotlin"; ios: "swift" }> {
  readonly state: AdapterState;

  scan(): Promise<ScanHandle>;
  devices(): BluetoothDevice[];
}

export interface ScanHandle
  extends HybridObject<{ android: "kotlin"; ios: "swift" }> {
  stop(): Promise<void>;

  // Events
  onDevice(callback: (device: BluetoothDevice) => void): () => void;
  onError(callback: (error: number) => void): () => void;
}

type BluetoothDeviceState = "disconnected" | "connected";

export interface BluetoothDevice
  extends HybridObject<{ android: "kotlin"; ios: "swift" }> {
  readonly address: string;
  readonly knownName: string;
  readonly state: BluetoothDeviceState;

  connect(): Promise<void>;
  disconnect(): Promise<void>;
  service(uuid: string): Promise<BluetoothDeviceService>;
}

export interface BluetoothDeviceService
  extends HybridObject<{ android: "kotlin"; ios: "swift" }> {
  characteristic(uuid: string): Promise<BluetoothDeviceServiceCharacteristic>;
}

export interface BluetoothDeviceServiceCharacteristic
  extends HybridObject<{ android: "kotlin"; ios: "swift" }> {
  read(): Promise<ArrayBuffer>;
  write(value: ArrayBuffer): Promise<void>;
}
