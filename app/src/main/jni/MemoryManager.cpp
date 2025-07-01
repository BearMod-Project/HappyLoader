#include "MemoryManager.h"
#include "BearMundoSecurity.h"
#include "MemoryProtection.h"
#include <android/log.h>
#include <sys/mman.h>
#include <unistd.h>
#include <fcntl.h>
#include <fstream>
#include <sstream>
#include <algorithm>
#include <regex>
#include <iomanip>

#define TAG "MemoryManager"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Static member definitions
bool MemoryManager::initialized = false;
bool MemoryManager::stealthMode = false;
std::mutex MemoryManager::operationMutex;
std::map<std::string, uintptr_t> MemoryManager::moduleCache;
uint64_t MemoryManager::readOperations = 0;
uint64_t MemoryManager::writeOperations = 0;
uint64_t MemoryManager::patternScans = 0;
uint64_t MemoryManager::moduleQueries = 0;

// ========================================
// CORE INITIALIZATION & SECURITY
// ========================================

bool MemoryManager::initialize() {
    std::lock_guard<std::mutex> lock(operationMutex);
    
    if (initialized) {
        LOGD("🔧 MemoryManager already initialized");
        return true;
    }
    
    LOGI("🚀 Initializing BEAR-LOADER Memory Manager...");
    
    // Security integration check
    if (!performSecurityCheck()) {
        LOGE("❌ Security check failed - initialization aborted");
        return false;
    }
    
    // Initialize module cache with common targets
    moduleCache.clear();
    
    // Cache common game modules
    getModuleBase("libUE4.so");      // Unreal Engine games
    getModuleBase("libil2cpp.so");   // Unity games
    getModuleBase("libmain.so");     // Common game main library
    getModuleBase("libunity.so");    // Unity engine
    
    // PUBG Mobile specific modules
    getModuleBase("libtgpa.so");     // PUBG anti-cheat
    getModuleBase("libanogs.so");    // PUBG anti-cheat
    getModuleBase("libtersafe.so");  // PUBG security
    
    initialized = true;
    LOGI("✅ BEAR-LOADER Memory Manager initialized successfully");
    return true;
}

bool MemoryManager::performSecurityCheck() {
    // Integrate with existing BearMundo security
    extern bool g_BearMundoActive;
    if (g_BearMundoActive) {
        BearMundo::DetectionThreat threat = BearMundo::validateSecurityEnvironment();
        if (threat >= BearMundo::DetectionThreat::HIGH) {
            LOGW("⚠️ High security threat detected - enabling stealth mode");
            stealthMode = true;
        }
    }
    
    // Verify memory protection is available
    if (bearmundo::security::BearMemoryManager::getInstance().initializeProtection()) {
        LOGD("🛡️ Memory protection integration successful");
        return true;
    }
    
    LOGW("⚠️ Memory protection not available - continuing with reduced security");
    return true; // Allow operation even without full protection
}

void MemoryManager::logMemoryOperation(const std::string& operation, uintptr_t address, size_t size) {
    if (!stealthMode) {
        LOGD("📝 %s: 0x%lx (size: %zu)", operation.c_str(), address, size);
    }
}

// ========================================
// MEMORY READ/WRITE OPERATIONS
// ========================================

bool MemoryManager::readMemory(uintptr_t address, void* buffer, size_t size) {
    if (!initialized) {
        LOGE("❌ MemoryManager not initialized");
        return false;
    }
    
    if (!buffer || size == 0) {
        LOGE("❌ Invalid read parameters");
        return false;
    }
    
    std::lock_guard<std::mutex> lock(operationMutex);
    
    // Security check
    if (!isAddressReadable(address, size)) {
        LOGE("❌ Address 0x%lx not readable", address);
        return false;
    }
    
    try {
        // Direct memory read
        memcpy(buffer, reinterpret_cast<void*>(address), size);
        
        // Update statistics
        readOperations++;
        updateStatistics("read");
        logMemoryOperation("READ", address, size);
        
        return true;
    } catch (...) {
        LOGE("❌ Memory read exception at 0x%lx", address);
        return false;
    }
}

bool MemoryManager::writeMemory(uintptr_t address, const void* buffer, size_t size) {
    if (!initialized) {
        LOGE("❌ MemoryManager not initialized");
        return false;
    }
    
    if (!buffer || size == 0) {
        LOGE("❌ Invalid write parameters");
        return false;
    }
    
    std::lock_guard<std::mutex> lock(operationMutex);
    
    // Security check
    if (!isAddressWritable(address, size)) {
        LOGE("❌ Address 0x%lx not writable", address);
        return false;
    }
    
    try {
        // Use memory protection guard for safe writing
        MemoryProtectionGuard guard(address, size, PROT_READ | PROT_WRITE);
        if (!guard.isValid()) {
            LOGE("❌ Could not change memory protection for write");
            return false;
        }
        
        // Direct memory write
        memcpy(reinterpret_cast<void*>(address), buffer, size);
        
        // Update statistics
        writeOperations++;
        updateStatistics("write");
        logMemoryOperation("WRITE", address, size);
        
        return true;
    } catch (...) {
        LOGE("❌ Memory write exception at 0x%lx", address);
        return false;
    }
}

// ========================================
// MODULE OPERATIONS
// ========================================

uintptr_t MemoryManager::getModuleBase(const std::string& moduleName) {
    std::lock_guard<std::mutex> lock(operationMutex);
    
    // Check cache first
    auto it = moduleCache.find(moduleName);
    if (it != moduleCache.end()) {
        return it->second;
    }
    
    // Read /proc/self/maps to find module
    std::ifstream maps("/proc/self/maps");
    if (!maps.is_open()) {
        LOGE("❌ Could not open /proc/self/maps");
        return 0;
    }
    
    std::string line;
    while (std::getline(maps, line)) {
        if (line.find(moduleName) != std::string::npos) {
            // Parse the line to get base address
            std::stringstream ss(line);
            std::string addrRange;
            ss >> addrRange;
            
            // Extract start address
            size_t dashPos = addrRange.find('-');
            if (dashPos != std::string::npos) {
                std::string startAddr = addrRange.substr(0, dashPos);
                uintptr_t baseAddr = std::stoull(startAddr, nullptr, 16);
                
                // Cache the result
                moduleCache[moduleName] = baseAddr;
                moduleQueries++;
                
                LOGD("📍 Module %s base: 0x%lx", moduleName.c_str(), baseAddr);
                return baseAddr;
            }
        }
    }
    
    LOGW("⚠️ Module %s not found", moduleName.c_str());
    return 0;
}

size_t MemoryManager::getModuleSize(const std::string& moduleName) {
    std::ifstream maps("/proc/self/maps");
    if (!maps.is_open()) {
        return 0;
    }
    
    uintptr_t startAddr = 0, endAddr = 0;
    std::string line;
    
    while (std::getline(maps, line)) {
        if (line.find(moduleName) != std::string::npos) {
            std::stringstream ss(line);
            std::string addrRange;
            ss >> addrRange;
            
            size_t dashPos = addrRange.find('-');
            if (dashPos != std::string::npos) {
                std::string start = addrRange.substr(0, dashPos);
                std::string end = addrRange.substr(dashPos + 1);
                
                uintptr_t curStart = std::stoull(start, nullptr, 16);
                uintptr_t curEnd = std::stoull(end, nullptr, 16);
                
                if (startAddr == 0) {
                    startAddr = curStart;
                }
                endAddr = curEnd;
            }
        }
    }
    
    if (startAddr && endAddr) {
        size_t size = endAddr - startAddr;
        LOGD("📏 Module %s size: %zu bytes", moduleName.c_str(), size);
        return size;
    }
    
    return 0;
}

// ========================================
// GAME-SPECIFIC MODULE FUNCTIONS
// ========================================

uintptr_t MemoryManager::getPUBGEngineBase() {
    // Try common PUBG Mobile libraries
    uintptr_t base = getModuleBase("libUE4.so");
    if (base) return base;
    
    base = getModuleBase("libmain.so");
    if (base) return base;
    
    base = getModuleBase("libpubgmain.so");
    if (base) return base;
    
    LOGW("⚠️ PUBG engine not found");
    return 0;
}

uintptr_t MemoryManager::getIL2CPPBase() {
    // Try Unity IL2CPP libraries
    uintptr_t base = getModuleBase("libil2cpp.so");
    if (base) return base;
    
    base = getModuleBase("libunity.so");
    if (base) return base;
    
    LOGW("⚠️ IL2CPP not found");
    return 0;
}

// ========================================
// PATTERN SCANNING
// ========================================

std::vector<uint8_t> MemoryManager::parsePattern(const std::string& pattern) {
    std::vector<uint8_t> bytes;
    std::stringstream ss(pattern);
    std::string byteStr;
    
    while (ss >> byteStr) {
        if (byteStr == "?") {
            bytes.push_back(0x00); // Placeholder for wildcard
        } else {
            bytes.push_back(static_cast<uint8_t>(std::stoul(byteStr, nullptr, 16)));
        }
    }
    
    return bytes;
}

bool MemoryManager::matchPattern(const uint8_t* data, const std::vector<uint8_t>& pattern, 
                                const std::vector<bool>& mask) {
    for (size_t i = 0; i < pattern.size(); ++i) {
        if (mask[i] && data[i] != pattern[i]) {
            return false;
        }
    }
    return true;
}

uintptr_t MemoryManager::findPattern(const std::string& pattern, uintptr_t start, uintptr_t end) {
    if (!initialized) {
        LOGE("❌ MemoryManager not initialized");
        return 0;
    }
    
    // Parse pattern and create mask
    std::vector<uint8_t> patternBytes = parsePattern(pattern);
    std::vector<bool> mask;
    
    std::stringstream ss(pattern);
    std::string byteStr;
    while (ss >> byteStr) {
        mask.push_back(byteStr != "?");
    }
    
    if (patternBytes.empty()) {
        LOGE("❌ Invalid pattern: %s", pattern.c_str());
        return 0;
    }
    
    // Scan memory
    for (uintptr_t addr = start; addr <= end - patternBytes.size(); ++addr) {
        if (!isAddressReadable(addr, patternBytes.size())) {
            continue;
        }
        
        const uint8_t* data = reinterpret_cast<const uint8_t*>(addr);
        if (matchPattern(data, patternBytes, mask)) {
            patternScans++;
            LOGD("🎯 Pattern found at: 0x%lx", addr);
            return addr;
        }
    }
    
    LOGW("⚠️ Pattern not found: %s", pattern.c_str());
    return 0;
}

uintptr_t MemoryManager::findPatternInModule(const std::string& pattern, const std::string& moduleName) {
    uintptr_t baseAddr = getModuleBase(moduleName);
    if (!baseAddr) {
        LOGE("❌ Module %s not found", moduleName.c_str());
        return 0;
    }
    
    size_t moduleSize = getModuleSize(moduleName);
    if (!moduleSize) {
        LOGE("❌ Could not get size for module %s", moduleName.c_str());
        return 0;
    }
    
    LOGD("🔍 Scanning pattern in %s (0x%lx - 0x%lx)", 
         moduleName.c_str(), baseAddr, baseAddr + moduleSize);
    
    return findPattern(pattern, baseAddr, baseAddr + moduleSize);
}

std::vector<uintptr_t> MemoryManager::findAllPatterns(const std::string& pattern, 
                                                     const std::string& moduleName, 
                                                     size_t maxResults) {
    std::vector<uintptr_t> results;
    
    uintptr_t baseAddr = getModuleBase(moduleName);
    if (!baseAddr) {
        return results;
    }
    
    size_t moduleSize = getModuleSize(moduleName);
    if (!moduleSize) {
        return results;
    }
    
    // Parse pattern
    std::vector<uint8_t> patternBytes = parsePattern(pattern);
    std::vector<bool> mask;
    
    std::stringstream ss(pattern);
    std::string byteStr;
    while (ss >> byteStr) {
        mask.push_back(byteStr != "?");
    }
    
    // Scan for all occurrences
    for (uintptr_t addr = baseAddr; addr <= baseAddr + moduleSize - patternBytes.size(); ++addr) {
        if (!isAddressReadable(addr, patternBytes.size())) {
            continue;
        }
        
        const uint8_t* data = reinterpret_cast<const uint8_t*>(addr);
        if (matchPattern(data, patternBytes, mask)) {
            results.push_back(addr);
            if (results.size() >= maxResults) {
                break;
            }
        }
    }
    
    LOGD("🎯 Found %zu pattern matches", results.size());
    return results;
}

// ========================================
// MEMORY SCANNING & ANALYSIS
// ========================================

std::vector<uintptr_t> MemoryManager::scanValueRange(uintptr_t startAddr, uintptr_t endAddr,
                                                     float minValue, float maxValue) {
    std::vector<uintptr_t> results;
    
    for (uintptr_t addr = startAddr; addr < endAddr; addr += sizeof(float)) {
        if (!isAddressReadable(addr, sizeof(float))) {
            continue;
        }
        
        float value;
        if (readMemory(addr, &value, sizeof(float))) {
            if (value >= minValue && value <= maxValue) {
                results.push_back(addr);
            }
        }
    }
    
    return results;
}

// ========================================
// MEMORY PROTECTION OPERATIONS
// ========================================

int MemoryManager::getMemoryProtection(uintptr_t address) {
    // Read /proc/self/maps to get protection flags
    std::ifstream maps("/proc/self/maps");
    if (!maps.is_open()) {
        return -1;
    }
    
    std::string line;
    while (std::getline(maps, line)) {
        std::stringstream ss(line);
        std::string addrRange, perms;
        ss >> addrRange >> perms;
        
        size_t dashPos = addrRange.find('-');
        if (dashPos != std::string::npos) {
            uintptr_t start = std::stoull(addrRange.substr(0, dashPos), nullptr, 16);
            uintptr_t end = std::stoull(addrRange.substr(dashPos + 1), nullptr, 16);
            
            if (address >= start && address < end) {
                int prot = 0;
                if (perms[0] == 'r') prot |= PROT_READ;
                if (perms[1] == 'w') prot |= PROT_WRITE;
                if (perms[2] == 'x') prot |= PROT_EXEC;
                return prot;
            }
        }
    }
    
    return -1;
}

int MemoryManager::changeProtection(uintptr_t address, size_t size, int newProtection) {
    int oldProt = getMemoryProtection(address);
    if (oldProt == -1) {
        return -1;
    }
    
    // Align to page boundary
    uintptr_t pageSize = getpagesize();
    uintptr_t alignedAddr = address & ~(pageSize - 1);
    size_t alignedSize = ((size + pageSize - 1) & ~(pageSize - 1));
    
    if (mprotect(reinterpret_cast<void*>(alignedAddr), alignedSize, newProtection) == 0) {
        return oldProt;
    }
    
    return -1;
}

bool MemoryManager::restoreProtection(uintptr_t address, size_t size, int originalProtection) {
    uintptr_t pageSize = getpagesize();
    uintptr_t alignedAddr = address & ~(pageSize - 1);
    size_t alignedSize = ((size + pageSize - 1) & ~(pageSize - 1));
    
    return mprotect(reinterpret_cast<void*>(alignedAddr), alignedSize, originalProtection) == 0;
}

// ========================================
// UTILITY FUNCTIONS
// ========================================

bool MemoryManager::isAddressReadable(uintptr_t address, size_t size) {
    // Try to read the first and last byte
    volatile char dummy;
    try {
        dummy = *reinterpret_cast<volatile char*>(address);
        dummy = *reinterpret_cast<volatile char*>(address + size - 1);
        return true;
    } catch (...) {
        return false;
    }
}

bool MemoryManager::isAddressWritable(uintptr_t address, size_t size) {
    int prot = getMemoryProtection(address);
    return (prot & PROT_WRITE) != 0;
}

void MemoryManager::updateStatistics(const std::string& operation) {
    // Statistics already updated in individual functions
    // This is for additional logging if needed
}

std::vector<std::string> MemoryManager::getMemoryMaps() {
    std::vector<std::string> maps;
    std::ifstream mapsFile("/proc/self/maps");
    
    if (!mapsFile.is_open()) {
        return maps;
    }
    
    std::string line;
    while (std::getline(mapsFile, line)) {
        maps.push_back(line);
    }
    
    return maps;
}

void MemoryManager::enableStealthMode(bool enabled) {
    std::lock_guard<std::mutex> lock(operationMutex);
    stealthMode = enabled;
    
    if (enabled) {
        LOGI("🥷 Stealth mode ENABLED");
        // Integrate with BearMundo stealth mode
        extern bool g_BearMundoActive;
        if (g_BearMundoActive) {
            BearMundo::enableStealthMode();
        }
    } else {
        LOGI("👁️ Stealth mode DISABLED");
    }
}

std::string MemoryManager::getStatistics() {
    std::stringstream ss;
    ss << "BEAR-LOADER Memory Manager Statistics:\n";
    ss << "  Initialized: " << (initialized ? "YES" : "NO") << "\n";
    ss << "  Stealth Mode: " << (stealthMode ? "ENABLED" : "DISABLED") << "\n";
    ss << "  Read Operations: " << readOperations << "\n";
    ss << "  Write Operations: " << writeOperations << "\n";
    ss << "  Pattern Scans: " << patternScans << "\n";
    ss << "  Module Queries: " << moduleQueries << "\n";
    ss << "  Cached Modules: " << moduleCache.size() << "\n";
    
    return ss.str();
}

void MemoryManager::shutdown() {
    std::lock_guard<std::mutex> lock(operationMutex);
    
    if (!initialized) {
        return;
    }
    
    LOGI("🔄 Shutting down BEAR-LOADER Memory Manager...");
    
    // Clear cache
    moduleCache.clear();
    
    // Reset statistics
    readOperations = 0;
    writeOperations = 0;
    patternScans = 0;
    moduleQueries = 0;
    
    initialized = false;
    stealthMode = false;
    
    LOGI("✅ Memory Manager shutdown complete");
}

// ========================================
// MEMORY PROTECTION GUARD IMPLEMENTATION
// ========================================

MemoryProtectionGuard::MemoryProtectionGuard(uintptr_t addr, size_t sz, int newProt)
    : address(addr), size(sz), originalProtection(-1), valid(false) {
    
    originalProtection = MemoryManager::changeProtection(address, size, newProt);
    valid = (originalProtection != -1);
}

MemoryProtectionGuard::~MemoryProtectionGuard() {
    if (valid && originalProtection != -1) {
        MemoryManager::restoreProtection(address, size, originalProtection);
    }
} 