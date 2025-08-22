#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_app_MainActivity_stringFromNative(JNIEnv* env, jobject /* this */) {
    return env->NewStringUTF("Hello from native-lib!");
}