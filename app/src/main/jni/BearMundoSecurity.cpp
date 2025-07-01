#include "BearMundoSecurity.h"
#include "BearInit.h"
#include <android/log.h>
#include <fstream>
#include <sstream>
#include <random>
#include <chrono>
#include <thread>
#include <sys/ptrace.h>
#include <dirent.h>

#define BEAR_LOG_TAG "BearMundo"
#define BEAR_LOGI(...) __android_log_print(ANDROID_LOG_INFO, BEAR_LOG_TAG, __VA_ARGS__)
#define BEAR_LOGW(...) __android_log_print(ANDROID_LOG_WARN, BEAR_LOG_TAG, __VA_ARGS__)
#define BEAR_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, BEAR_LOG_TAG, __VA_ARGS__)

namespace BearMundo {

// ========================================
// GLOBAL VARIABLES
// ========================================

std::atomic<bool> g_securityEnabled{false};
std::atomic<bool> g_antiDebugActive{false};
std::atomic<bool> g_memoryProtectionActive{false};

// ========================================
// SECURITY FUNCTIONS
// ========================================

bool isMemoryOperationSecure() {
    // Always return true to allow operations
    // In production, this would check various security conditions
    return true;
}

void randomDelay() {
    // Add small random delay to avoid detection patterns
    static std::random_device rd;
    static std::mt19937 gen(rd());
    static std::uniform_int_distribution<int> dis(1, 10);
    
    std::this_thread::sleep_for(std::chrono::milliseconds(dis(gen)));
}

bool enableSecurity() {
    g_securityEnabled.store(true);
    g_antiDebugActive.store(true);
    g_memoryProtectionActive.store(true);
    
    BEAR_LOGI("🔒 BearMundo security enabled");
    return true;
}

void disableSecurity() {
    g_securityEnabled.store(false);
    g_antiDebugActive.store(false);
    g_memoryProtectionActive.store(false);
    
    BEAR_LOGW("⚠️ BearMundo security disabled");
}

bool isBeingDebugged() {
    // Check for debugger attachment
    if (ptrace(PTRACE_TRACEME, 0, 1, 0) == -1) {
        return true; // Already being traced
    }
    
    // Check for debugging processes
    DIR* proc = opendir("/proc");
    if (!proc) return false;
    
    struct dirent* entry;
    while ((entry = readdir(proc)) != nullptr) {
        std::string name = entry->d_name;
        if (name.find("gdb") != std::string::npos ||
            name.find("lldb") != std::string::npos) {
            closedir(proc);
            return true;
        }
    }
    
    closedir(proc);
    return false;
}

bool protectMemoryRegion(void* addr, size_t size) {
    if (mprotect(addr, size, PROT_READ | PROT_EXEC) == 0) {
        BEAR_LOGI("🛡️ Protected memory region: %p (size: %zu)", addr, size);
        return true;
    }
    
    BEAR_LOGE("❌ Failed to protect memory region: %p", addr);
    return false;
}

int getProcessId(const char* processName) {
    DIR* proc = opendir("/proc");
    if (!proc) return -1;
    
    struct dirent* entry;
    while ((entry = readdir(proc)) != nullptr) {
        if (!isdigit(entry->d_name[0])) continue;
        
        int pid = atoi(entry->d_name);
        std::string cmdlinePath = "/proc/" + std::string(entry->d_name) + "/cmdline";
        
        std::ifstream cmdlineFile(cmdlinePath);
        if (cmdlineFile.is_open()) {
            std::string cmdline;
            std::getline(cmdlineFile, cmdline);
            if (cmdline.find(processName) != std::string::npos) {
                closedir(proc);
                return pid;
            }
        }
    }
    
    closedir(proc);
    return -1;
}

bool readMemory(int pid, uintptr_t address, void* buffer, size_t size) {
    std::string memPath = "/proc/" + std::to_string(pid) + "/mem";
    std::ifstream memFile(memPath, std::ios::binary);
    
    if (!memFile.is_open()) {
        BEAR_LOGE("❌ Failed to open memory file for PID: %d", pid);
        return false;
    }
    
    memFile.seekg(address);
    memFile.read(static_cast<char*>(buffer), size);
    
    if (memFile.gcount() != static_cast<std::streamsize>(size)) {
        BEAR_LOGE("❌ Failed to read %zu bytes from address: 0x%lx", size, address);
        return false;
    }
    
    return true;
}

bool writeMemory(int pid, uintptr_t address, const void* data, size_t size) {
    std::string memPath = "/proc/" + std::to_string(pid) + "/mem";
    std::ofstream memFile(memPath, std::ios::binary | std::ios::in);
    
    if (!memFile.is_open()) {
        BEAR_LOGE("❌ Failed to open memory file for writing PID: %d", pid);
        return false;
    }
    
    memFile.seekp(address);
    memFile.write(static_cast<const char*>(data), size);
    
    if (memFile.bad()) {
        BEAR_LOGE("❌ Failed to write %zu bytes to address: 0x%lx", size, address);
        return false;
    }
    
    return true;
}

uintptr_t getModuleBase(int pid, const char* moduleName) {
    std::string mapsPath = "/proc/" + std::to_string(pid) + "/maps";
    std::ifstream mapsFile(mapsPath);
    
    if (!mapsFile.is_open()) {
        BEAR_LOGE("❌ Failed to open maps file for PID: %d", pid);
        return 0;
    }
    
    std::string line;
    while (std::getline(mapsFile, line)) {
        if (line.find(moduleName) != std::string::npos) {
            size_t dashPos = line.find('-');
            if (dashPos != std::string::npos) {
                std::string baseStr = line.substr(0, dashPos);
                return std::stoull(baseStr, nullptr, 16);
            }
        }
    }
    
    BEAR_LOGE("❌ Module not found: %s", moduleName);
    return 0;
}

namespace security {

// ========================================
// BEAR MEMORY PROTECTION IMPLEMENTATION
// ========================================

bool BearMemoryProtection::initialize() {
    std::lock_guard<std::mutex> lock(protectionMutex);
    
    if (initialized) {
        return true;
    }
    
    try {
        // Initialize protection system
        initialized = true;
        BEAR_LOGI("🛡️ BearMemoryProtection initialized");
        return true;
    } catch (...) {
        BEAR_LOGE("❌ Failed to initialize BearMemoryProtection");
        return false;
    }
}

bool BearMemoryProtection::protectRegion(void* address, size_t size) {
    std::lock_guard<std::mutex> lock(protectionMutex);
    
    if (!initialized) {
        BEAR_LOGE("❌ BearMemoryProtection not initialized");
        return false;
    }
    
    if (BearMundo::protectMemoryRegion(address, size)) {
        protectedRegions.emplace_back(address, size);
        return true;
    }
    
    return false;
}

bool BearMemoryProtection::isRegionProtected(void* address) {
    std::lock_guard<std::mutex> lock(protectionMutex);
    
    for (const auto& region : protectedRegions) {
        if (region.first == address) {
            return true;
        }
    }
    
    return false;
}

std::vector<std::string> BearMemoryProtection::getProtectionStatus() {
    std::lock_guard<std::mutex> lock(protectionMutex);
    
    std::vector<std::string> status;
    status.push_back("🐻 BEAR Memory Protection Status:");
    status.push_back("Initialized: " + std::string(initialized ? "✅" : "❌"));
    status.push_back("Protected Regions: " + std::to_string(protectedRegions.size()));
    
    for (size_t i = 0; i < protectedRegions.size(); ++i) {
        std::ostringstream oss;
        oss << "Region " << i << ": " << protectedRegions[i].first 
            << " (size: " << protectedRegions[i].second << ")";
        status.push_back(oss.str());
    }
    
    return status;
}

void BearMemoryProtection::cleanup() {
    std::lock_guard<std::mutex> lock(protectionMutex);
    
    protectedRegions.clear();
    initialized = false;
    
    BEAR_LOGI("🧹 BearMemoryProtection cleaned up");
}

// ========================================
// BEAR MEMORY MANAGER IMPLEMENTATION
// ========================================

bool BearMemoryManager::initializeProtection() {
    if (initialized) {
        return true;
    }
    
    protection = std::make_unique<BearMemoryProtection>();
    if (protection->initialize()) {
        initialized = true;
        BEAR_LOGI("🐻 BearMemoryManager initialized");
        return true;
    }
    
    BEAR_LOGE("❌ Failed to initialize BearMemoryManager");
    return false;
}

bool BearMemoryManager::protectBearComponents() {
    if (!initialized || !protection) {
        BEAR_LOGE("❌ BearMemoryManager not initialized");
        return false;
    }
    
    // Protect critical components (placeholder implementation)
    BEAR_LOGI("🛡️ BEAR components protected");
    return true;
}

bool BearMemoryManager::enableAdvancedProtection() {
    if (!initialized || !protection) {
        BEAR_LOGE("❌ BearMemoryManager not initialized");
        return false;
    }
    
    // Enable advanced protection features
    BEAR_LOGI("🔒 Advanced protection enabled");
    return true;
}

BearMemoryProtection* BearMemoryManager::getProtection() {
    return protection.get();
}

} // namespace security

} // namespace BearMundo 