#include <jni.h>
#include <android/log.h>
#include <string>

#define LOG_TAG "BearStub"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global initialization flag
static bool g_initialized = false;

extern "C" {

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("🐻 BEAR-LOADER Native Library Loading...");
    
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        LOGE("❌ Failed to get JNI environment");
        return JNI_ERR;
    }
    
    LOGI("✅ BEAR-LOADER Native Library Loaded Successfully");
    g_initialized = true;
    
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved) {
    LOGI("🧹 BEAR-LOADER Native Library Unloading...");
    g_initialized = false;
}

// Basic native methods for compatibility
JNIEXPORT jboolean JNICALL
Java_com_happy_pro_utils_NativeUtils_isNativeLoaded(JNIEnv* env, jclass clazz) {
    return g_initialized ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_com_happy_pro_utils_NativeUtils_getNativeVersion(JNIEnv* env, jclass clazz) {
    return env->NewStringUTF("BEAR-LOADER 3.0.0");
}

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_utils_NativeUtils_initializeBear(JNIEnv* env, jclass clazz, jstring packageName) {
    const char* package = env->GetStringUTFChars(packageName, nullptr);
    LOGI("🎮 Initializing BEAR for package: %s", package);
    env->ReleaseStringUTFChars(packageName, package);
    
    // Stub implementation - always return success
    return JNI_TRUE;
}

// Stub implementations for compatibility
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_nativeInit(JNIEnv* env, jobject thiz) {
    LOGI("📱 FloatService native init (stub)");
}

JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_nativeDestroy(JNIEnv* env, jobject thiz) {
    LOGI("🧹 FloatService native destroy (stub)");
}

// Memory stub functions
JNIEXPORT jboolean JNICALL
Java_com_happy_pro_utils_Memory_initialize(JNIEnv* env, jclass clazz) {
    LOGI("💾 Memory initialize (stub)");
    return JNI_TRUE;
}

// Socket stub functions
JNIEXPORT jboolean JNICALL
Java_com_happy_pro_utils_Socket_connect(JNIEnv* env, jclass clazz, jstring host, jint port) {
    LOGI("🔌 Socket connect (stub)");
    return JNI_TRUE;
}

// Login stub functions
JNIEXPORT jboolean JNICALL
Java_com_happy_pro_utils_Login_authenticate(JNIEnv* env, jclass clazz, jstring key) {
    LOGI("🔐 Login authenticate (stub)");
    return JNI_TRUE;
}

} // extern "C" 