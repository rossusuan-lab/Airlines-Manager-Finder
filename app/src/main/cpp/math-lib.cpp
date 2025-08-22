#include <jni.h>

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_app_MainActivity_addNumbers(JNIEnv *env, jobject, jint a, jint b) {
    return a + b;
}