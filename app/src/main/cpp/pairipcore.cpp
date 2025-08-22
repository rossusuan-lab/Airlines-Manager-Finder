// pairipcore.cpp
#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_proteksi_Security_check(JNIEnv* env, jobject /* this */) {
    std::string key = "Pa1r1P-C0r3-Pr0t3ct";
    return env->NewStringUTF(key.c_str());
}