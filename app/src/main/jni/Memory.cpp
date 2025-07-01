#include "Memory.h"
#include "BearMundoSecurity.h"
#include "BearInit.h"
#include "socket.h"
#include "struct.h"
#include <android/log.h>
#include <fstream>
#include <sstream>
#include <thread>
#include <chrono>

#define MEMORY_LOG_TAG "BearMemory"
#define MEMORY_LOGI(...) __android_log_print(ANDROID_LOG_INFO, MEMORY_LOG_TAG, __VA_ARGS__)
#define MEMORY_LOGW(...) __android_log_print(ANDROID_LOG_WARN, MEMORY_LOG_TAG, __VA_ARGS__)
#define MEMORY_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, MEMORY_LOG_TAG, __VA_ARGS__)

// ========================================
// GLOBAL VARIABLE DEFINITIONS
// ========================================

// Screen and process data
int height = 1080;
int width = 2340;
int pid = 0;
int isBeta = 0;
int nByte = 0;

// Memory management globals (local to this file)
static bool memoryInitialized = false;
static uintptr_t gameBaseAddress = 0;
static int targetGamePid = -1;

// Note: xConnected, xServerConnection, g_Token, g_Auth, signValid, ts 
// are already defined in Login.h - we use extern declarations in Memory.h

// ========================================
// BEAR MEMORY MANAGER IMPLEMENTATION
// ========================================

namespace BearMemory {

/**
 * Initialize memory system
 */
bool initializeMemorySystem() {
    if (memoryInitialized) {
        return true;
    }
    
    try {
        // Initialize BEAR security system
        if (!BearInit::initializeBearSystem()) {
            MEMORY_LOGE("❌ Failed to initialize BEAR system");
            return false;
        }
        
        // Find target game process
        targetGamePid = BearMundo::getProcessId("com.tencent.ig"); // PUBG Mobile
        if (targetGamePid == -1) {
            targetGamePid = BearMundo::getProcessId("com.pubg.krmobile"); // PUBG Mobile KR
        }
        if (targetGamePid == -1) {
            targetGamePid = BearMundo::getProcessId("com.rekoo.pubgm"); // PUBG Mobile Global
        }
        
        if (targetGamePid != -1) {
            MEMORY_LOGI("🎮 Target game found with PID: %d", targetGamePid);
            
            // Get game base address
            gameBaseAddress = BearMundo::getModuleBase(targetGamePid, "libil2cpp.so");
            if (gameBaseAddress == 0) {
                gameBaseAddress = BearMundo::getModuleBase(targetGamePid, "libUE4.so");
            }
            
            if (gameBaseAddress != 0) {
                MEMORY_LOGI("🎯 Game base address: 0x%lx", gameBaseAddress);
            }
        }
        
        memoryInitialized = true;
        MEMORY_LOGI("✅ Memory system initialized");
        return true;
        
    } catch (...) {
        MEMORY_LOGE("❌ Exception during memory system initialization");
        return false;
    }
}

/**
 * Read memory from game process
 */
bool readGameMemory(uintptr_t address, void* buffer, size_t size) {
    if (!memoryInitialized || targetGamePid == -1) {
        return false;
    }
    
    return BearMundo::readMemory(targetGamePid, address, buffer, size);
}

/**
 * Write memory to game process
 */
bool writeGameMemory(uintptr_t address, const void* data, size_t size) {
    if (!memoryInitialized || targetGamePid == -1) {
        return false;
    }
    
    return BearMundo::writeMemory(targetGamePid, address, data, size);
}

/**
 * Get game base address
 */
uintptr_t getGameBaseAddress() {
    return gameBaseAddress;
}

/**
 * Get target game PID
 */
int getTargetGamePid() {
    return targetGamePid;
}

/**
 * Check if memory system is ready
 */
bool isMemorySystemReady() {
    return memoryInitialized && targetGamePid != -1 && gameBaseAddress != 0;
}

} // namespace BearMemory

// ========================================
// GAME MEMORY HACKING FUNCTIONS
// ========================================

/**
 * Apply recoil reduction hack
 */
bool applyRecoilHack(bool enable) {
    if (!BearMemory::isMemorySystemReady()) {
        return false;
    }
    
    try {
        uintptr_t baseAddr = BearMemory::getGameBaseAddress();
        
        // Recoil offset (placeholder - would need actual game analysis)
        uintptr_t recoilOffset = 0x12345678;
        uintptr_t recoilAddress = baseAddr + recoilOffset;
        
        float recoilValue = enable ? 0.0f : 1.0f;
        
        if (BearMemory::writeGameMemory(recoilAddress, &recoilValue, sizeof(float))) {
            MEMORY_LOGI("🎯 Recoil hack %s", enable ? "enabled" : "disabled");
            return true;
        }
        
        return false;
        
    } catch (...) {
        MEMORY_LOGE("❌ Exception during recoil hack");
        return false;
    }
}

/**
 * Apply aimbot assistance
 */
bool applyAimbotHack(bool enable) {
    if (!BearMemory::isMemorySystemReady()) {
        return false;
    }
    
    try {
        uintptr_t baseAddr = BearMemory::getGameBaseAddress();
        
        // Aimbot offset (placeholder - would need actual game analysis)
        uintptr_t aimbotOffset = 0x87654321;
        uintptr_t aimbotAddress = baseAddr + aimbotOffset;
        
        int aimbotValue = enable ? 1 : 0;
        
        if (BearMemory::writeGameMemory(aimbotAddress, &aimbotValue, sizeof(int))) {
            MEMORY_LOGI("🎯 Aimbot hack %s", enable ? "enabled" : "disabled");
            return true;
        }
        
        return false;
        
    } catch (...) {
        MEMORY_LOGE("❌ Exception during aimbot hack");
        return false;
    }
}

/**
 * Apply speed hack
 */
bool applySpeedHack(float speedMultiplier) {
    if (!BearMemory::isMemorySystemReady()) {
        return false;
    }
    
    try {
        uintptr_t baseAddr = BearMemory::getGameBaseAddress();
        
        // Speed offset (placeholder - would need actual game analysis)
        uintptr_t speedOffset = 0xABCDEF00;
        uintptr_t speedAddress = baseAddr + speedOffset;
        
        if (BearMemory::writeGameMemory(speedAddress, &speedMultiplier, sizeof(float))) {
            MEMORY_LOGI("⚡ Speed hack applied: %.2fx", speedMultiplier);
            return true;
        }
        
        return false;
        
    } catch (...) {
        MEMORY_LOGE("❌ Exception during speed hack");
        return false;
    }
}

/**
 * Read player health
 */
float readPlayerHealth() {
    if (!BearMemory::isMemorySystemReady()) {
        return 100.0f; // Default health
    }
    
    try {
        uintptr_t baseAddr = BearMemory::getGameBaseAddress();
        
        // Health offset (placeholder - would need actual game analysis)
        uintptr_t healthOffset = 0x11223344;
        uintptr_t healthAddress = baseAddr + healthOffset;
        
        float health = 100.0f;
        if (BearMemory::readGameMemory(healthAddress, &health, sizeof(float))) {
            return health;
        }
        
        return 100.0f;
        
    } catch (...) {
        MEMORY_LOGE("❌ Exception during health read");
        return 100.0f;
    }
}

/**
 * Get player position
 */
Vec3 readPlayerPosition() {
    Vec3 position = {0.0f, 0.0f, 0.0f};
    
    if (!BearMemory::isMemorySystemReady()) {
        return position;
    }
    
    try {
        uintptr_t baseAddr = BearMemory::getGameBaseAddress();
        
        // Position offset (placeholder - would need actual game analysis)
        uintptr_t positionOffset = 0x55667788;
        uintptr_t positionAddress = baseAddr + positionOffset;
        
        BearMemory::readGameMemory(positionAddress, &position, sizeof(Vec3));
        
        return position;
        
    } catch (...) {
        MEMORY_LOGE("❌ Exception during position read");
        return position;
    }
}

/**
 * Validate license key
 */
bool validateLicenseKey(const std::string& licenseKey) {
    try {
        // Simple validation (in production, this would use proper authentication)
        if (licenseKey.length() >= 8) {
            g_Token = licenseKey;
            g_Auth = licenseKey;
            signValid = true;
            
            MEMORY_LOGI("✅ License key validated");
            return true;
        }
        
        MEMORY_LOGW("⚠️ Invalid license key");
        return false;
        
    } catch (...) {
        MEMORY_LOGE("❌ Exception during license validation");
        return false;
    }
}

/**
 * Check authentication status
 */
bool isAuthenticated() {
    return !g_Token.empty() && !g_Auth.empty() && (g_Token == g_Auth) && signValid;
}

/**
 * Get memory system status
 */
std::string getMemorySystemStatus() {
    std::ostringstream status;
    
    status << "🐻 BEAR Memory System Status:\n";
    status << "Initialized: " << (memoryInitialized ? "✅" : "❌") << "\n";
    status << "Target PID: " << targetGamePid << "\n";
    status << "Base Address: 0x" << std::hex << gameBaseAddress << "\n";
    status << "Connected: " << (xConnected ? "✅" : "❌") << "\n";
    status << "Authenticated: " << (isAuthenticated() ? "✅" : "❌") << "\n";
    
    return status.str();
}

/**
 * Cleanup memory system
 */
void cleanupMemorySystem() {
    try {
        // Use existing Close() function from socket.h
        Close();
        BearInit::cleanupBearSystem();
        
        memoryInitialized = false;
        targetGamePid = -1;
        gameBaseAddress = 0;
        xConnected = false;
        xServerConnection = false;
        
        MEMORY_LOGI("🧹 Memory system cleaned up");
        
    } catch (...) {
        MEMORY_LOGE("❌ Exception during memory system cleanup");
    }
} 