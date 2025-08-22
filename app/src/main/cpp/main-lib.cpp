#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_app_MainActivity_stringLength(JNIEnv* env, jobject, jstring input) {
    const char* str = env->GetStringUTFChars(input, nullptr);
    int len = strlen(str);
    env->ReleaseStringUTFChars(input, str);
    return len;
}