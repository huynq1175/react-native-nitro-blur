#include <jni.h>
#include "nitroblurOnLoad.hpp"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
  return margelo::nitro::nitroblur::initialize(vm);
}
