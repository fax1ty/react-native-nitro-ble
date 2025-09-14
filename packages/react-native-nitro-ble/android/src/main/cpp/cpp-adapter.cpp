#include <jni.h>
#include "NitroBluetoothOnLoad.hpp"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
  return margelo::nitro::bluetooth::initialize(vm);
}
