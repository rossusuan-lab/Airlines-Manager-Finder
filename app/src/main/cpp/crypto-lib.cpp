#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_app_MainActivity_encrypt(JNIEnv* env, jobject, jstring input) {
    const char* str = env->GetStringUTFChars(input, 0);
    std::string encrypted = std::string(str) + "_encrypted";
    env->ReleaseStringUTFChars(input, str);
    return env->NewStringUTF(encrypted.c_str());
}