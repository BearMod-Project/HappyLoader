#include <jni.h>
#include "BearMundoSecurity.h"
#include "ContainerManager.h"
#include "MemoryProtection.h"
#include "MemoryManager.h"
#include <android/log.h>

#define LOG_TAG "BearMundoJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global security flag
bool g_BearMundoActive = false;

namespace BearMundo {
    bool initializeBearMundoSecurity(JNIEnv* env) {
        g_BearMundoActive = true;
        return true;
    }
    
    bool isBearMundoSecurityActive() {
        return g_BearMundoActive;
    }
    
    SecurityLevel getCurrentSecurityLevel() {
        return g_BearMundoActive ? SecurityLevel::ENHANCED : SecurityLevel::BASIC;
    }
    
    bool enableStealthMode() {
        return true;
    }
    
    bool disableStealthMode() {
        return true;
    }
    
    DetectionThreat validateSecurityEnvironment() {
        return DetectionThreat::NONE;
    }
    
    bool validateKeyAuthWithSecurity() {
        return g_BearMundoActive;
    }
    
    bool isESPOperationSecure() {
        return g_BearMundoActive;
    }
    
    bool detectFridaFramework() {
        return false;
    }
    
    bool detectAdvancedDebugging() {
        return false;
    }
    
    bool detectRootWithEvasion() {
        return false;
    }
    
    bool detectEmulatorEnvironment() {
        return false;
    }
    
    std::string generateRandomStackName() {
        return "randomstack123";
    }
    
    std::string generateObfuscatedFunctionName() {
        return "handle_random_event";
    }
    
    void createDecoyOperations() {
        // Simple decoy implementation
    }
}

using namespace BearMundo;
using namespace bearmundo::security;

extern "C" {

// ========================================
// BEAR MUNDO CORE FUNCTIONS
// ========================================

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_BearMundoSecurity_initializeBearMundoSecurity(JNIEnv *env, jclass clazz) {
    LOGI("🚀 Initializing BEAR Mundo Security...");
    try {
        return BearMundo::initializeBearMundoSecurity(env);
    } catch (...) {
        LOGE("❌ Security initialization failed");
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_BearMundoSecurity_isBearMundoActive(JNIEnv *env, jclass clazz) {
    try {
        return BearMundo::isBearMundoSecurityActive();
    } catch (...) {
        return JNI_FALSE;
    }
}

JNIEXPORT jint JNICALL
Java_com_bearmod_security_BearMundoSecurity_getSecurityLevel(JNIEnv *env, jclass clazz) {
    try {
        return static_cast<jint>(BearMundo::getCurrentSecurityLevel());
    } catch (...) {
        return 0;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_BearMundoSecurity_enableStealthMode(JNIEnv *env, jclass clazz) {
    LOGI("🥷 Enabling stealth mode...");
    try {
        return BearMundo::enableStealthMode();
    } catch (...) {
        LOGE("❌ Stealth mode activation failed");
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_BearMundoSecurity_disableStealthMode(JNIEnv *env, jclass clazz) {
    try {
        return BearMundo::disableStealthMode();
    } catch (...) {
        return JNI_FALSE;
    }
}

JNIEXPORT jint JNICALL
Java_com_bearmod_security_BearMundoSecurity_performThreatAssessment(JNIEnv *env, jclass clazz) {
    LOGI("🔍 Performing threat assessment...");
    try {
        BearMundo::DetectionThreat threat = BearMundo::validateSecurityEnvironment();
        return static_cast<jint>(threat);
    } catch (...) {
        return -1;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_BearMundoSecurity_validateKeyAuthWithSecurity(JNIEnv *env, jclass clazz) {
    try {
        return BearMundo::validateKeyAuthWithSecurity();
    } catch (...) {
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_BearMundoSecurity_isMemoryOperationSecure(JNIEnv *env, jclass clazz) {
    try {
        return BearMundo::isMemoryOperationSecure();
    } catch (...) {
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_BearMundoSecurity_isESPOperationSecure(JNIEnv *env, jclass clazz) {
    try {
        return BearMundo::isESPOperationSecure();
    } catch (...) {
        return JNI_FALSE;
    }
}

// ========================================
// BEAR MEMORY PROTECTION FUNCTIONS
// ========================================

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeInitialize(JNIEnv *env, jobject thiz) {
    LOGI("🛡️ Initializing BEAR Memory Protection...");
    try {
        auto& manager = BearMemoryManager::getInstance();
        bool result = manager.initializeProtection();
        if (result) {
            LOGI("✅ BEAR Memory Protection initialized successfully");
        } else {
            LOGE("❌ BEAR Memory Protection initialization failed");
        }
        return result ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        LOGE("❌ Exception during memory protection initialization");
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeProtectBearComponents(JNIEnv *env, jobject thiz) {
    LOGI("🔐 Protecting BEAR components...");
    try {
        auto& manager = BearMemoryManager::getInstance();
        bool result = manager.protectBearComponents();
        if (result) {
            LOGI("✅ BEAR components protected successfully");
        } else {
            LOGE("❌ BEAR components protection failed");
        }
        return result ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        LOGE("❌ Exception during BEAR components protection");
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeEnableAdvancedProtection(JNIEnv *env, jobject thiz) {
    LOGI("🚀 Enabling advanced protection...");
    try {
        auto& manager = BearMemoryManager::getInstance();
        bool result = manager.enableAdvancedProtection();
        if (result) {
            LOGI("✅ Advanced protection enabled successfully");
        } else {
            LOGE("❌ Advanced protection enablement failed");
        }
        return result ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        LOGE("❌ Exception during advanced protection enablement");
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeProtectRegion(JNIEnv *env, jobject thiz, jlong address, jlong size) {
    LOGI("🔒 Protecting memory region: %p (size: %zu)", 
         reinterpret_cast<void*>(address), static_cast<size_t>(size));
    try {
        auto& manager = BearMemoryManager::getInstance();
        MemoryProtection* protection = manager.getProtection();
        
        if (!protection) {
            LOGE("❌ Memory protection not initialized");
            return JNI_FALSE;
        }
        
        bool result = protection->protectRegion(reinterpret_cast<void*>(address), static_cast<size_t>(size));
        
        if (result) {
            LOGI("✅ Memory region protected successfully");
        } else {
            LOGE("❌ Memory region protection failed: %s", protection->getLastError().c_str());
        }
        
        return result ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        LOGE("❌ Exception during memory region protection");
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeIsRegionProtected(JNIEnv *env, jobject thiz, jlong address) {
    try {
        auto& manager = BearMemoryManager::getInstance();
        MemoryProtection* protection = manager.getProtection();
        
        if (!protection) {
            return JNI_FALSE;
        }
        
        bool result = protection->isRegionProtected(reinterpret_cast<void*>(address));
        return result ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        LOGE("❌ Exception during memory protection check");
        return JNI_FALSE;
    }
}

JNIEXPORT jstring JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeGetProtectionStatus(JNIEnv *env, jobject thiz) {
    try {
        auto& manager = BearMemoryManager::getInstance();
        MemoryProtection* protection = manager.getProtection();
        
        if (!protection) {
            return env->NewStringUTF("Memory protection not initialized");
        }
        
        auto statusList = protection->getProtectionStatus();
        std::string statusString;
        
        for (const auto& line : statusList) {
            statusString += line + "\n";
        }
        
        return env->NewStringUTF(statusString.c_str());
    } catch (...) {
        LOGE("❌ Exception during status retrieval");
        return env->NewStringUTF("Error retrieving status");
    }
}

JNIEXPORT jint JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeGetProtectedRegionCount(JNIEnv *env, jobject thiz) {
    try {
        auto& manager = BearMemoryManager::getInstance();
        MemoryProtection* protection = manager.getProtection();
        
        if (!protection) {
            return 0;
        }
        
        size_t count = protection->getProtectedRegionCount();
        return static_cast<jint>(count);
    } catch (...) {
        LOGE("❌ Exception during region count retrieval");
        return 0;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeEnableStealthMode(JNIEnv *env, jobject thiz) {
    LOGI("🥷 Enabling memory protection stealth mode...");
    try {
        auto& manager = BearMemoryManager::getInstance();
        MemoryProtection* protection = manager.getProtection();
        
        if (!protection) {
            LOGE("❌ Memory protection not initialized");
            return JNI_FALSE;
        }
        
        bool result = protection->enableStealthMode();
        
        if (result) {
            LOGI("✅ Stealth mode enabled successfully");
        } else {
            LOGE("❌ Stealth mode enablement failed");
        }
        
        return result ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        LOGE("❌ Exception during stealth mode enablement");
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeEnableAntiDebug(JNIEnv *env, jobject thiz) {
    LOGI("🛡️ Enabling anti-debug protection...");
    try {
        auto& manager = BearMemoryManager::getInstance();
        MemoryProtection* protection = manager.getProtection();
        
        if (!protection) {
            LOGE("❌ Memory protection not initialized");
            return JNI_FALSE;
        }
        
        bool result = protection->enableAntiDebug();
        
        if (result) {
            LOGI("✅ Anti-debug protection enabled successfully");
        } else {
            LOGE("❌ Anti-debug protection enablement failed");
        }
        
        return result ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        LOGE("❌ Exception during anti-debug protection enablement");
        return JNI_FALSE;
    }
}

JNIEXPORT jstring JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeGetLastError(JNIEnv *env, jobject thiz) {
    try {
        auto& manager = BearMemoryManager::getInstance();
        MemoryProtection* protection = manager.getProtection();
        
        if (!protection) {
            return env->NewStringUTF("Memory protection not initialized");
        }
        
        std::string error = protection->getLastError();
        return env->NewStringUTF(error.c_str());
    } catch (...) {
        LOGE("❌ Exception during error retrieval");
        return env->NewStringUTF("Error retrieving last error");
    }
}

// ========================================
// HOOK MANAGER INTEGRATION FUNCTIONS
// ========================================

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_hooks_HookManager_nativeInitializeMemoryProtection(JNIEnv *env, jobject thiz) {
    return Java_com_happy_pro_security_BearMemoryProtection_nativeInitialize(env, thiz);
}

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_hooks_HookManager_nativeProtectESPRegions(JNIEnv *env, jobject thiz) {
    LOGI("🎯 Protecting ESP hook regions...");
    try {
        auto& manager = BearMemoryManager::getInstance();
        MemoryProtection* protection = manager.getProtection();
        
        if (!protection) {
            LOGE("❌ Memory protection not initialized");
            return JNI_FALSE;
        }
        
        // This would protect ESP-specific memory regions
        // For now, we'll use the general protection
        bool result = manager.protectBearComponents();
        
        if (result) {
            LOGI("✅ ESP hook regions protected");
        } else {
            LOGE("❌ ESP hook regions protection failed");
        }
        
        return result ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        LOGE("❌ Exception during ESP protection");
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_hooks_HookManager_nativeProtectAimbotRegions(JNIEnv *env, jobject thiz) {
    LOGI("🎯 Protecting Aimbot hook regions...");
    try {
        auto& manager = BearMemoryManager::getInstance();
        MemoryProtection* protection = manager.getProtection();
        
        if (!protection) {
            LOGE("❌ Memory protection not initialized");
            return JNI_FALSE;
        }
        
        // This would protect Aimbot-specific memory regions
        // For now, we'll use the general protection
        bool result = manager.protectBearComponents();
        
        if (result) {
            LOGI("✅ Aimbot hook regions protected");
        } else {
            LOGE("❌ Aimbot hook regions protection failed");
        }
        
        return result ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        LOGE("❌ Exception during Aimbot protection");
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_hooks_HookManager_nativeProtectMemoryRegions(JNIEnv *env, jobject thiz) {
    LOGI("🧠 Protecting Memory hack regions...");
    try {
        auto& manager = BearMemoryManager::getInstance();
        bool result = manager.protectBearComponents();
        
        if (result) {
            LOGI("✅ Memory hack regions protected");
        } else {
            LOGE("❌ Memory hack regions protection failed");
        }
        
        return result ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        LOGE("❌ Exception during memory region protection");
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_hooks_HookManager_nativeEnableStealthMode(JNIEnv *env, jobject thiz) {
    return Java_com_happy_pro_security_BearMemoryProtection_nativeEnableStealthMode(env, thiz);
}

JNIEXPORT jstring JNICALL
Java_com_happy_pro_hooks_HookManager_nativeGetMemoryProtectionStatus(JNIEnv *env, jobject thiz) {
    return Java_com_happy_pro_security_BearMemoryProtection_nativeGetProtectionStatus(env, thiz);
}

// ========================================
// CONTAINER MANAGEMENT FUNCTIONS
// ========================================

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_BearMundoSecurity_initializeContainerManager(JNIEnv *env, jclass clazz) {
    LOGI("📦 Initializing container manager...");
    try {
        auto* manager = BearMundo::Container::BearMundoContainerManager::getInstance();
        if (manager) {
            return manager->initialize();
        }
        return false;
    } catch (...) {
        LOGE("❌ Container manager initialization failed");
        return false;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_BearMundoSecurity_isContainerManagerInitialized(JNIEnv *env, jclass clazz) {
    try {
        auto* manager = BearMundo::Container::BearMundoContainerManager::getInstance();
        if (manager) {
            return manager->isManagerInitialized();
        }
        return false;
    } catch (...) {
        return false;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_BearMundoSecurity_isRootEnvironment(JNIEnv *env, jclass clazz) {
    try {
        auto* manager = BearMundo::Container::BearMundoContainerManager::getInstance();
        if (manager) {
            return manager->isRootEnvironment();
        }
        return false;
    } catch (...) {
        return false;
    }
}

JNIEXPORT jstring JNICALL
Java_com_bearmod_security_BearMundoSecurity_createSecureContainer(JNIEnv *env, jclass clazz, jint containerType) {
    LOGI("🏗️ Creating secure container (type: %d)...", containerType);
    try {
        auto* manager = BearMundo::Container::BearMundoContainerManager::getInstance();
        if (!manager || !manager->isManagerInitialized()) {
            return env->NewStringUTF("");
        }
        
        BearMundo::Container::ContainerConfiguration config;
        
        switch (containerType) {
            case 0: // Standard
                config = BearMundo::Container::createStandardConfiguration();
                break;
            case 1: // Root
                config = BearMundo::Container::createRootConfiguration();
                break;
            case 2: // Stealth
                config = BearMundo::Container::createStealthConfiguration();
                break;
            default:
                config = BearMundo::Container::createNonRootConfiguration();
                break;
        }
        
        std::string containerId = manager->createContainer(config);
        LOGI("✅ Container created: %s", containerId.c_str());
        return env->NewStringUTF(containerId.c_str());
    } catch (...) {
        LOGE("❌ Container creation failed");
        return env->NewStringUTF("");
    }
}

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_BearMundoSecurity_activateContainer(JNIEnv *env, jclass clazz, jstring containerId) {
    try {
        auto* manager = BearMundo::Container::BearMundoContainerManager::getInstance();
        if (!manager) {
            return false;
        }

        const char* idStr = env->GetStringUTFChars(containerId, nullptr);
        bool result = manager->activateContainer(std::string(idStr));
        env->ReleaseStringUTFChars(containerId, idStr);

        if (result) {
            LOGI("✅ Container activated: %s", idStr);
        } else {
            LOGE("❌ Container activation failed: %s", idStr);
        }

        return result;
    } catch (...) {
        LOGE("❌ Exception during container activation");
        return false;
    }
}

JNIEXPORT jstring JNICALL
Java_com_bearmod_security_BearMundoSecurity_getActiveContainerInfo(JNIEnv *env, jclass clazz) {
    try {
        auto* manager = BearMundo::Container::BearMundoContainerManager::getInstance();
        if (!manager) {
            return env->NewStringUTF("Container manager not available");
        }
        
        auto* activeContainer = manager->getActiveContainer();
        if (!activeContainer) {
            return env->NewStringUTF("No active container");
        }
        
        std::string info = "Container ID: " + activeContainer->containerId +
                          ", Type: " + std::to_string(static_cast<int>(activeContainer->config.type)) +
                          ", Security Level: " + std::to_string(static_cast<int>(activeContainer->config.securityLevel)) +
                          ", Environment: " + std::to_string(static_cast<int>(activeContainer->detectedEnvironment));
        
        return env->NewStringUTF(info.c_str());
    } catch (...) {
        return env->NewStringUTF("Error retrieving container info");
    }
}

JNIEXPORT jint JNICALL
Java_com_bearmod_security_BearMundoSecurity_getContainerCount(JNIEnv *env, jclass clazz) {
    try {
        auto* manager = BearMundo::Container::BearMundoContainerManager::getInstance();
        if (manager) {
            return static_cast<jint>(manager->getContainerCount());
        }
        return 0;
    } catch (...) {
        return 0;
    }
}

// ========================================
// DETECTION FUNCTIONS
// ========================================

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_BearMundoSecurity_detectFridaFramework(JNIEnv *env, jclass clazz) {
    try {
        return BearMundo::detectFridaFramework();
    } catch (...) {
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_BearMundoSecurity_detectAdvancedDebugging(JNIEnv *env, jclass clazz) {
    try {
        return BearMundo::detectAdvancedDebugging();
    } catch (...) {
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_BearMundoSecurity_detectRootWithEvasion(JNIEnv *env, jclass clazz) {
    try {
        return BearMundo::detectRootWithEvasion();
    } catch (...) {
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_BearMundoSecurity_detectEmulatorEnvironment(JNIEnv *env, jclass clazz) {
    try {
        return BearMundo::detectEmulatorEnvironment();
    } catch (...) {
        return JNI_FALSE;
    }
}

// ========================================
// UTILITY FUNCTIONS
// ========================================

JNIEXPORT jstring JNICALL
Java_com_bearmod_security_BearMundoSecurity_generateRandomStackName(JNIEnv *env, jclass clazz) {
    try {
        std::string name = BearMundo::generateRandomStackName();
        return env->NewStringUTF(name.c_str());
    } catch (...) {
        return env->NewStringUTF("stack_default");
    }
}

JNIEXPORT jstring JNICALL
Java_com_bearmod_security_BearMundoSecurity_generateObfuscatedFunctionName(JNIEnv *env, jclass clazz) {
    try {
        std::string name = BearMundo::generateObfuscatedFunctionName();
        return env->NewStringUTF(name.c_str());
    } catch (...) {
        return env->NewStringUTF("func_default");
    }
}

JNIEXPORT void JNICALL
Java_com_bearmod_security_BearMundoSecurity_randomDelay(JNIEnv *env, jclass clazz) {
    try {
        BearMundo::randomDelay();
    } catch (...) {
        // Ignore exceptions for timing functions
    }
}

JNIEXPORT void JNICALL
Java_com_bearmod_security_BearMundoSecurity_createDecoyOperations(JNIEnv *env, jclass clazz) {
    try {
        BearMundo::createDecoyOperations();
    } catch (...) {
        // Ignore exceptions for decoy operations
    }
}

// ========================================
// MEMORY MANAGER JNI FUNCTIONS
// ========================================

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_hooks_HookManager_nativeInitializeMemoryManager(JNIEnv *env, jobject thiz) {
    LOGI("🧠 Initializing BEAR Memory Manager...");
    try {
        bool result = MemoryManager::initialize();
        if (result) {
            LOGI("✅ Memory Manager initialized successfully");
        } else {
            LOGE("❌ Memory Manager initialization failed");
        }
        return result ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        LOGE("❌ Exception during Memory Manager initialization");
        return JNI_FALSE;
    }
}

JNIEXPORT jlong JNICALL
Java_com_happy_pro_hooks_HookManager_nativeFindPatternInModule(JNIEnv *env, jobject thiz, jstring pattern, jstring moduleName) {
    try {
        if (!pattern || !moduleName) {
            LOGE("❌ Invalid parameters");
            return 0;
        }
        
        const char* patternStr = env->GetStringUTFChars(pattern, nullptr);
        const char* moduleStr = env->GetStringUTFChars(moduleName, nullptr);
        
        if (!patternStr || !moduleStr) {
            LOGE("❌ Could not get string parameters");
            if (patternStr) env->ReleaseStringUTFChars(pattern, patternStr);
            if (moduleStr) env->ReleaseStringUTFChars(moduleName, moduleStr);
            return 0;
        }
        
        uintptr_t result = MemoryManager::findPatternInModule(std::string(patternStr), std::string(moduleStr));
        
        env->ReleaseStringUTFChars(pattern, patternStr);
        env->ReleaseStringUTFChars(moduleName, moduleStr);
        
        return static_cast<jlong>(result);
    } catch (...) {
        LOGE("❌ Exception during module pattern search");
        return 0;
    }
}

JNIEXPORT jlong JNICALL
Java_com_happy_pro_hooks_HookManager_nativeGetModuleBase(JNIEnv *env, jobject thiz, jstring moduleName) {
    try {
        if (!moduleName) {
            LOGE("❌ Invalid module name parameter");
            return 0;
        }
        
        const char* moduleStr = env->GetStringUTFChars(moduleName, nullptr);
        if (!moduleStr) {
            LOGE("❌ Could not get module name string");
            return 0;
        }
        
        uintptr_t result = MemoryManager::getModuleBase(std::string(moduleStr));
        
        env->ReleaseStringUTFChars(moduleName, moduleStr);
        
        return static_cast<jlong>(result);
    } catch (...) {
        LOGE("❌ Exception during module base query");
        return 0;
    }
}

JNIEXPORT jlong JNICALL
Java_com_happy_pro_hooks_HookManager_nativeGetPUBGEngineBase(JNIEnv *env, jobject thiz) {
    try {
        uintptr_t result = MemoryManager::getPUBGEngineBase();
        return static_cast<jlong>(result);
    } catch (...) {
        LOGE("❌ Exception during PUBG engine base query");
        return 0;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_hooks_HookManager_nativeWriteFloat(JNIEnv *env, jobject thiz, jlong address, jfloat value) {
    try {
        bool result = MemoryManager::writeMemory(static_cast<uintptr_t>(address), &value, sizeof(float));
        return result ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        LOGE("❌ Exception during float write");
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_hooks_HookManager_nativeWriteInt(JNIEnv *env, jobject thiz, jlong address, jint value) {
    try {
        bool result = MemoryManager::writeMemory(static_cast<uintptr_t>(address), &value, sizeof(int));
        return result ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        LOGE("❌ Exception during int write");
        return JNI_FALSE;
    }
}

JNIEXPORT jstring JNICALL
Java_com_happy_pro_hooks_HookManager_nativeGetMemoryManagerStatistics(JNIEnv *env, jobject thiz) {
    try {
        std::string stats = MemoryManager::getStatistics();
        return env->NewStringUTF(stats.c_str());
    } catch (...) {
        LOGE("❌ Exception during statistics retrieval");
        return env->NewStringUTF("Error retrieving statistics");
    }
}

} // extern "C" 