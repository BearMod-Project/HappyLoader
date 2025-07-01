#ifndef STEALTH_OPERATIONS_H
#define STEALTH_OPERATIONS_H

#include <memory>
#include <vector>
#include <mutex>
#include <atomic>
#include <jni.h>

namespace StealthOps {

// Threat level enumeration
enum class ThreatLevel {
    NONE = 0,
    LOW = 1,
    MEDIUM = 2,
    HIGH = 3,
    CRITICAL = 4
};

// Operation type enumeration
enum class OperationType {
    ESP_OVERLAY = 0,
    MEMORY_HACK = 1,
    AIMBOT = 2,
    WALLHACK = 3,
    SPEED_HACK = 4
};

/**
 * ThreatAssessment - Advanced threat detection and assessment system
 */
class ThreatAssessment {
public:
    ThreatAssessment() : current_threat_level(ThreatLevel::NONE) {}
    
    // Individual threat checks
    bool checkDebuggerPresence();
    bool checkEmulatorEnvironment();
    bool checkRootAccess();
    bool checkAntiCheatPresence();
    bool checkMemoryIntegrity();
    
    // Advanced SignKill detection methods
    bool checkSignatureBypassAttempts();
    bool checkPackageManagerIntegrity();
    bool checkHookingFrameworks();
    
    // Comprehensive assessment
    ThreatLevel performAssessment();
    
    // Operation safety validation
    bool isOperationSafe(OperationType op_type) const;
    
    // Current threat level
    ThreatLevel getCurrentThreatLevel() const { return current_threat_level.load(); }
    
private:
    std::atomic<ThreatLevel> current_threat_level;
    mutable std::mutex assessment_mutex;
};

/**
 * MemoryProtection - Advanced memory region protection system
 */
class MemoryProtection {
public:
    MemoryProtection() = default;
    
    // Memory protection operations
    bool protectRegion(void* address, size_t size);
    bool unprotectRegion(void* address, size_t size);
    void cleanupAllRegions();
    
    // Protection status
    bool isRegionProtected(void* address) const;
    size_t getProtectedRegionCount() const { return protected_regions.size(); }
    
private:
    std::vector<void*> protected_regions;
    mutable std::mutex protection_mutex;
};

/**
 * StealthController - Main controller for stealth operations
 */
class StealthController {
public:
    static StealthController* getInstance();
    
    // Lifecycle management
    bool initialize();
    void shutdown();
    
    // Stealth mode control
    bool enableStealthMode();
    void disableStealthMode();
    bool isStealthModeActive() const { return stealth_active.load(); }
    
    // Operation validation
    bool isOperationAllowed(OperationType op_type);
    ThreatLevel getCurrentThreatLevel() const;
    
    // Memory protection interface
    bool protectMemoryRegion(void* address, size_t size);
    bool unprotectMemoryRegion(void* address, size_t size);
    
    // Emergency controls
    void triggerEmergencyShutdown();
    bool isEmergencyShutdownActive() const { return emergency_shutdown.load(); }
    
    // Static utility methods
    static bool validateEnvironment();
    static bool performSecurityCheck();
    
    // Component access
    ThreatAssessment* getThreatAssessment() const { return threat_assessor.get(); }
    MemoryProtection* getMemoryProtection() const { return memory_protector.get(); }
    
private:
    StealthController();
    static StealthController* instance;
    
    // Components
    std::unique_ptr<ThreatAssessment> threat_assessor;
    std::unique_ptr<MemoryProtection> memory_protector;
    
    // State
    std::atomic<bool> stealth_active{false};
    std::atomic<bool> emergency_shutdown{false};
};

} // namespace StealthOps

// JNI Bridge Functions
extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_StealthManager_nativeInitialize(JNIEnv* env, jclass clazz);

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_StealthManager_nativeEnableStealthMode(JNIEnv* env, jclass clazz);

JNIEXPORT void JNICALL
Java_com_happy_pro_security_StealthManager_nativeDisableStealthMode(JNIEnv* env, jclass clazz);

JNIEXPORT jint JNICALL
Java_com_happy_pro_security_StealthManager_nativeGetThreatLevel(JNIEnv* env, jclass clazz);

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_StealthManager_nativeIsOperationSafe(JNIEnv* env, 
                                                               jclass clazz, 
                                                               jint operation_type);

JNIEXPORT void JNICALL
Java_com_happy_pro_security_StealthManager_nativeTriggerEmergencyShutdown(JNIEnv* env, 
                                                                        jclass clazz);

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_StealthManager_nativeValidateEnvironment(JNIEnv* env, jclass clazz);

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_StealthManager_nativePerformSecurityCheck(JNIEnv* env, jclass clazz);

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_StealthManager_nativeProtectMemoryRegion(JNIEnv* env, 
                                                                    jclass clazz,
                                                                    jlong address, 
                                                                    jint size);

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_StealthManager_nativeUnprotectMemoryRegion(JNIEnv* env, 
                                                                      jclass clazz,
                                                                      jlong address, 
                                                                      jint size);

} // extern "C"

#endif // STEALTH_OPERATIONS_H 