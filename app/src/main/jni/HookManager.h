#pragma once

#include <cstdint>
#include <string>
#include <jni.h>
#include <android/log.h>

/**
 * @brief Manager class for hooking functions
 * 
 * This class provides a unified interface for hooking functions
 * using various hooking libraries (Dobby, Substrate, etc.)
 */
class HookManager {
public:
    /**
     * @brief Initialize the hook manager
     * 
     * @return true if initialization was successful
     * @return false if initialization failed
     */
    static bool initialize();

    /**
     * @brief Hook a function
     * 
     * @param target Pointer to the target function
     * @param replacement Pointer to the replacement function
     * @param original Pointer to store the original function
     * @return true if hooking was successful
     * @return false if hooking failed
     */
    static bool hookFunction(void* target, void* replacement, void** original);

    /**
     * @brief Hook a function by name
     * 
     * @param libraryName Name of the library containing the function
     * @param functionName Name of the function to hook
     * @param replacement Pointer to the replacement function
     * @param original Pointer to store the original function
     * @return true if hooking was successful
     * @return false if hooking failed
     */
    static bool hookFunctionByName(const std::string& libraryName, 
                                  const std::string& functionName,
                                  void* replacement,
                                  void** original);

    /**
     * @brief Unhook a function
     * 
     * @param target Pointer to the target function
     * @return true if unhooking was successful
     * @return false if unhooking failed
     */
    static bool unhookFunction(void* target);

    // Static member for tracking initialization state
    static bool initialized;
    
private:
    // Private constructor to prevent instantiation
    HookManager() = delete;
};

// ============ JNI Function Declarations ============

extern "C" {
    
    // Core initialization
    JNIEXPORT jboolean JNICALL
    Java_com_happy_pro_hooks_HookManager_nativeInitializeHooks(JNIEnv *env, jobject thiz, jboolean isRoot);
    
    // Security functions
    JNIEXPORT void JNICALL
    Java_com_happy_pro_hooks_HookManager_nativeEnableAntiDetection(JNIEnv *env, jobject thiz);
    
    JNIEXPORT void JNICALL
    Java_com_happy_pro_hooks_HookManager_nativeEnableMemoryProtection(JNIEnv *env, jobject thiz);
    
    JNIEXPORT jboolean JNICALL
    Java_com_happy_pro_hooks_HookManager_nativeEnableStealthMode(JNIEnv *env, jobject thiz);
    
    // Subsystem initialization
    JNIEXPORT jboolean JNICALL
    Java_com_happy_pro_hooks_HookManager_nativeInitializeESPHooks(JNIEnv *env, jobject thiz);
    
    JNIEXPORT jboolean JNICALL
    Java_com_happy_pro_hooks_HookManager_nativeInitializeAimbotHooks(JNIEnv *env, jobject thiz);
    
    JNIEXPORT jboolean JNICALL
    Java_com_happy_pro_hooks_HookManager_nativeInitializeMemoryHooks(JNIEnv *env, jobject thiz);
    
    // Control functions
    JNIEXPORT jboolean JNICALL
    Java_com_happy_pro_hooks_HookManager_nativeSetESPEnabled(JNIEnv *env, jobject thiz, jboolean enabled);
    
    JNIEXPORT jboolean JNICALL
    Java_com_happy_pro_hooks_HookManager_nativeSetAimbotEnabled(JNIEnv *env, jobject thiz, jboolean enabled);
    
    JNIEXPORT jboolean JNICALL
    Java_com_happy_pro_hooks_HookManager_nativeSetMemoryHacksEnabled(JNIEnv *env, jobject thiz, jboolean enabled);
    
    // Emergency & cleanup
    JNIEXPORT void JNICALL
    Java_com_happy_pro_hooks_HookManager_nativeEmergencyDisable(JNIEnv *env, jobject thiz);
    
    JNIEXPORT jboolean JNICALL
    Java_com_happy_pro_hooks_HookManager_nativeEnableAdvancedStealth(JNIEnv *env, jobject thiz, jboolean enabled);
    
    JNIEXPORT void JNICALL
    Java_com_happy_pro_hooks_HookManager_nativeCleanup(JNIEnv *env, jobject thiz);
} 