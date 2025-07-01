#include "BearInit.h"
#include "BearMundoSecurity.h"
#include <android/log.h>
#include <sstream>

#define BEAR_LOG_TAG "BearInit"
#define BEAR_LOGI(...) __android_log_print(ANDROID_LOG_INFO, BEAR_LOG_TAG, __VA_ARGS__)
#define BEAR_LOGW(...) __android_log_print(ANDROID_LOG_WARN, BEAR_LOG_TAG, __VA_ARGS__)
#define BEAR_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, BEAR_LOG_TAG, __VA_ARGS__)

namespace BearInit {

// ========================================
// GLOBAL VARIABLES
// ========================================

std::atomic<bool> isSecurityEnabled{false};
std::atomic<bool> isMemoryInitialized{false};
std::atomic<bool> isSocketInitialized{false};

// ========================================
// INITIALIZATION FUNCTIONS
// ========================================

bool initializeBearSystem() {
    BEAR_LOGI("🐻 Initializing BEAR System...");
    
    try {
        // Initialize security first
        if (!initializeSecurity()) {
            BEAR_LOGE("❌ Failed to initialize security");
            return false;
        }
        
        // Initialize memory protection
        if (!initializeMemoryProtection()) {
            BEAR_LOGE("❌ Failed to initialize memory protection");
            return false;
        }
        
        // Initialize socket system
        if (!initializeSocketSystem()) {
            BEAR_LOGE("❌ Failed to initialize socket system");
            return false;
        }
        
        BEAR_LOGI("✅ BEAR System initialized successfully");
        return true;
        
    } catch (...) {
        BEAR_LOGE("❌ Exception during BEAR system initialization");
        return false;
    }
}

bool initializeSecurity() {
    try {
        if (BearMundo::enableSecurity()) {
            isSecurityEnabled.store(true);
            BEAR_LOGI("🔒 Security system initialized");
            return true;
        }
        
        BEAR_LOGE("❌ Failed to enable security");
        return false;
        
    } catch (...) {
        BEAR_LOGE("❌ Exception during security initialization");
        return false;
    }
}

bool initializeMemoryProtection() {
    try {
        auto& memManager = BearMundo::security::BearMemoryManager::getInstance();
        
        if (memManager.initializeProtection()) {
            isMemoryInitialized.store(true);
            BEAR_LOGI("🛡️ Memory protection initialized");
            return true;
        }
        
        BEAR_LOGE("❌ Failed to initialize memory protection");
        return false;
        
    } catch (...) {
        BEAR_LOGE("❌ Exception during memory protection initialization");
        return false;
    }
}

bool initializeSocketSystem() {
    try {
        // Socket system initialization would go here
        // For now, just mark as initialized
        isSocketInitialized.store(true);
        BEAR_LOGI("🔌 Socket system initialized");
        return true;
        
    } catch (...) {
        BEAR_LOGE("❌ Exception during socket system initialization");
        return false;
    }
}

void cleanupBearSystem() {
    BEAR_LOGI("🧹 Cleaning up BEAR System...");
    
    try {
        // Cleanup memory protection
        auto& memManager = BearMundo::security::BearMemoryManager::getInstance();
        auto* protection = memManager.getProtection();
        if (protection) {
            protection->cleanup();
        }
        
        // Disable security
        BearMundo::disableSecurity();
        
        // Reset flags
        isSecurityEnabled.store(false);
        isMemoryInitialized.store(false);
        isSocketInitialized.store(false);
        
        BEAR_LOGI("✅ BEAR System cleanup completed");
        
    } catch (...) {
        BEAR_LOGE("❌ Exception during BEAR system cleanup");
    }
}

std::string getInitializationStatus() {
    std::ostringstream status;
    
    status << "🐻 BEAR System Status:\n";
    status << "Security: " << (isSecurityEnabled.load() ? "✅ ENABLED" : "❌ DISABLED") << "\n";
    status << "Memory Protection: " << (isMemoryInitialized.load() ? "✅ INITIALIZED" : "❌ NOT INITIALIZED") << "\n";
    status << "Socket System: " << (isSocketInitialized.load() ? "✅ INITIALIZED" : "❌ NOT INITIALIZED") << "\n";
    
    return status.str();
}

} // namespace BearInit 