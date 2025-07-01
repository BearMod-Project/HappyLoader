#ifndef BEAR_STEALTH_CONTAINER_H
#define BEAR_STEALTH_CONTAINER_H

#include <memory>
#include <vector>
#include <string>
#include <atomic>
#include <jni.h>
#include <android/log.h>
#include <sys/system_properties.h>

namespace StealthContainer {

// Protection levels for the container
enum class ProtectionLevel {
    BASIC = 0,      // Basic protection
    ENHANCED = 1,   // Enhanced protection with stealth
    KERNEL = 2      // Kernel-level protection (requires root)
};

/**
 * ProtectedFunction - Protects individual functions by copying to secure memory
 */
class ProtectedFunction {
public:
    ProtectedFunction(void* func_ptr, size_t size);
    ~ProtectedFunction();
    
    // Protection control
    bool protect();
    bool unprotect();
    bool isProtected() const { return is_protected; }
    
    // Memory access
    void* getProtectedPointer() const { return protected_ptr; }
    void* getOriginalPointer() const { return original_ptr; }
    size_t getSize() const { return function_size; }
    
private:
    void* original_ptr;
    void* protected_ptr;
    size_t function_size;
    bool is_protected;
};

/**
 * Container - Main secure execution environment
 */
class Container {
public:
    static Container* getInstance();
    
    // Lifecycle management
    bool initialize(ProtectionLevel level = ProtectionLevel::ENHANCED);
    void shutdown();
    
    // Function protection
    bool protectFunction(void* func_ptr, size_t size, const std::string& name = "");
    
    // Game-specific protection
    bool protectESPOverlay();
    bool protectMemoryHacks();
    bool protectFloatingServices();
    
    // Security validation
    bool performSecurityCheck();
    
    // Stealth operations
    void enableStealthMode();
    void disableStealthMode();
    
    // Detection methods
    bool detectDebugger();
    bool detectEmulator();
    bool detectHooks();
    
    // Protection status
    ProtectionLevel getCurrentLevel() const { return current_level; }
    bool isKernelModeAvailable() const { return kernel_mode_available; }
    size_t getProtectedFunctionCount() const { return protected_functions.size(); }
    
    // Advanced features
    bool enableKernelMode();
    void disableKernelMode();
    void obfuscateMemory();
    
private:
    Container();
    ~Container() = default;
    
    static Container* instance;
    
    // Configuration
    ProtectionLevel current_level;
    bool kernel_mode_available;
    
    // Protected functions
    std::vector<std::unique_ptr<ProtectedFunction>> protected_functions;
    
    // Internal methods
    void clearProtectedFunctions();
    bool validateEnvironment();
    void setupSecurityMeasures();
};

} // namespace StealthContainer

// JNI Bridge Functions
extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_stealth_StealthContainer_nativeInitialize(JNIEnv* env,
                                                           jclass clazz,
                                                           jint protection_level);

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_stealth_StealthContainer_nativeProtectGameFunctions(JNIEnv* env,
                                                                       jclass clazz);

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_stealth_StealthContainer_nativePerformSecurityCheck(JNIEnv* env,
                                                                       jclass clazz);

JNIEXPORT void JNICALL
Java_com_happy_pro_stealth_StealthContainer_nativeShutdown(JNIEnv* env,
                                                          jclass clazz);

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_stealth_StealthContainer_nativeProtectFunction(JNIEnv* env,
                                                                 jclass clazz,
                                                                 jlong function_ptr,
                                                                 jint size,
                                                                 jstring name);

JNIEXPORT jint JNICALL
Java_com_happy_pro_stealth_StealthContainer_nativeGetProtectionLevel(JNIEnv* env,
                                                                    jclass clazz);

JNIEXPORT jstring JNICALL
Java_com_happy_pro_stealth_StealthContainer_nativeGetContainerStatus(JNIEnv* env,
                                                                    jclass clazz);

} // extern "C"

#endif // BEAR_STEALTH_CONTAINER_H 