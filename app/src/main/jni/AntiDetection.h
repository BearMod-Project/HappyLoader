#ifndef BEAR_ANTI_DETECTION_H
#define BEAR_ANTI_DETECTION_H

#include <string>
#include <vector>
#include <memory>
#include <atomic>
#include <jni.h>

namespace AntiDetection {

// Detection types matching Java @IntDef
enum class DetectionType {
    DEBUGGER = 0,
    EMULATOR = 1,
    ROOT = 2,
    XPOSED = 3,
    FRIDA = 4,
    ANTICHEAT = 5,
    MEMORY_SCANNER = 6,
    HOOK_DETECTOR = 7
};

// Detection result structure
struct DetectionResult {
    DetectionType type;
    bool detected;
    std::string details;
    
    DetectionResult(DetectionType t, bool d, const std::string& det = "") 
        : type(t), detected(d), details(det) {}
};

// Forward declarations
class EnvironmentScanner;
class ProcessHiding;
class MemoryObfuscation;

/**
 * EnvironmentScanner - Comprehensive threat detection system
 */
class EnvironmentScanner {
public:
    // Individual detection methods
    bool detectDebugger();
    bool detectEmulator();
    bool detectRootAccess();
    bool detectXposedFramework();
    bool detectFridaInjection();
    bool detectAntiCheatSoftware();
    bool detectMemoryScanner();
    bool detectHookingFrameworks();
    
    // Comprehensive scanning
    std::vector<DetectionResult> performFullScan();
    DetectionResult scanSpecific(DetectionType type);
    bool isEnvironmentSafe();
    
    // Get scan history
    const std::vector<DetectionResult>& getScanHistory() const { return scan_history; }
    
private:
    std::vector<DetectionResult> scan_history;
};

/**
 * ProcessHiding - Process name and behavior obfuscation
 */
class ProcessHiding {
public:
    ProcessHiding();
    ~ProcessHiding() = default;
    
    bool enableProcessHiding();
    void disableProcessHiding();
    bool isHidingActive() const { return hiding_active; }
    
    void setFakeProcessName(const std::string& name);
    std::string getFakeProcessName() const;
    
private:
    bool hiding_active;
    std::vector<std::string> fake_names;
    std::string current_fake_name;
};

/**
 * MemoryObfuscation - Memory pattern obfuscation and protection
 */
class MemoryObfuscation {
public:
    MemoryObfuscation() = default;
    ~MemoryObfuscation() = default;
    
    bool enableObfuscation();
    void disableObfuscation();
    void cleanupObfuscation();
    
    bool isObfuscationActive() const { return obfuscation_active; }
    
private:
    bool obfuscation_active = false;
    std::vector<void*> obfuscated_regions;
};

/**
 * AntiDetectionManager - Main coordination class
 */
class AntiDetectionManager {
public:
    static AntiDetectionManager* getInstance();
    
    // Core lifecycle
    bool initialize();
    void shutdown();
    
    // Protection control
    bool enableProtection();
    void disableProtection();
    bool isProtectionActive() const { return protection_active.load(); }
    
    // Environment validation
    bool isEnvironmentSafe();
    std::vector<DetectionResult> performSecurityScan();
    
    // Detection reporting
    void reportDetection(DetectionType type, const std::string& details = "");
    int getDetectionCount() const { return detection_count.load(); }
    void resetDetectionCount() { detection_count.store(0); }
    
    // Component access
    EnvironmentScanner* getScanner() { return scanner.get(); }
    ProcessHiding* getProcessHider() { return process_hider.get(); }
    MemoryObfuscation* getMemoryObfuscator() { return memory_obfuscator.get(); }
    
    // Defensive measures
    void triggerDefensiveMeasures(DetectionType type);
    void triggerEmergencyCountermeasures();
    
    // Status reporting
    std::string getDetailedStatus() const;
    std::vector<DetectionResult> getDetectionHistory() const;
    
private:
    AntiDetectionManager();
    ~AntiDetectionManager() = default;
    
    static AntiDetectionManager* instance;
    
    // Components
    std::unique_ptr<EnvironmentScanner> scanner;
    std::unique_ptr<ProcessHiding> process_hider;
    std::unique_ptr<MemoryObfuscation> memory_obfuscator;
    
    // State tracking
    std::atomic<bool> protection_active{false};
    std::atomic<int> detection_count{0};
    std::vector<DetectionResult> detection_history;
    
    // Internal methods
    void handleCriticalDetection(DetectionType type);
    void updateDetectionHistory(const DetectionResult& result);
};

} // namespace AntiDetection

// JNI Bridge Functions
extern "C" {

// AntiDetectionManager JNI methods
JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_AntiDetectionManager_nativeInitialize(JNIEnv* env, jobject thiz);

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_AntiDetectionManager_nativeEnableProtection(JNIEnv* env, jobject thiz);

JNIEXPORT void JNICALL
Java_com_happy_pro_security_AntiDetectionManager_nativeDisableProtection(JNIEnv* env, jobject thiz);

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_AntiDetectionManager_nativeIsEnvironmentSafe(JNIEnv* env, jobject thiz);

JNIEXPORT jint JNICALL
Java_com_happy_pro_security_AntiDetectionManager_nativeGetDetectionCount(JNIEnv* env, jobject thiz);

JNIEXPORT jstring JNICALL
Java_com_happy_pro_security_AntiDetectionManager_nativeGetDetectionDetails(JNIEnv* env, jobject thiz);

JNIEXPORT void JNICALL
Java_com_happy_pro_security_AntiDetectionManager_nativeReportDetection(JNIEnv* env, jobject thiz, 
                                                                       jint detectionType, jstring details);

JNIEXPORT void JNICALL
Java_com_happy_pro_security_AntiDetectionManager_nativeTriggerDefensiveMeasures(JNIEnv* env, jobject thiz, 
                                                                                jint detectionType);

JNIEXPORT void JNICALL
Java_com_happy_pro_security_AntiDetectionManager_nativeResetDetectionState(JNIEnv* env, jobject thiz);

} // extern "C"

#endif // BEAR_ANTI_DETECTION_H 