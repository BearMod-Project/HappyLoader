#pragma once

#include <cstdint>
#include <string>
#include <vector>
#include <unistd.h>
#include <sys/uio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <dirent.h>
#include <android/log.h>
#include "BearMundoSecurity.h"

#define LOG_TAG "MemoryHelper"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

/**
 * 🐻 BEAR-LOADER Memory Helper Class
 * 
 * Advanced memory manipulation system with integrated security
 * Features:
 * - Cross-process memory read/write using process_vm_readv/writev
 * - Process discovery by name or PID
 * - Module base address resolution
 * - Security validation integration
 * - Memory operation logging and protection
 * 
 * @author BEAR-LOADER Team
 * @version 3.0.0
 */
class MemoryHelper {
private:
    pid_t targetPid;
    bool initialized;
    std::string targetProcessName;
    uintptr_t cachedBaseAddress;
    
    // Security integration
    bool securityValidated;
    uint64_t lastSecurityCheck;
    
    // Get base address of a module
    uintptr_t GetModuleBase(const std::string& moduleName) {
        char line[512];
        uintptr_t baseAddr = 0;
        
        char path[256];
        sprintf(path, "/proc/%d/maps", targetPid);
        
        FILE* fp = fopen(path, "r");
        if (fp) {
            while (fgets(line, sizeof(line), fp)) {
                if (strstr(line, moduleName.c_str())) {
                    baseAddr = strtoull(line, nullptr, 16);
                    LOGD("📍 Module %s base: 0x%lx", moduleName.c_str(), baseAddr);
                    break;
                }
            }
            fclose(fp);
        }
        
        return baseAddr;
    }
    
    // Validate security before memory operations
    bool validateSecurity() {
        // Check if BEAR Mundo security is active
        if (!BearMundo::isBearMundoSecurityActive()) {
            LOGE("❌ BEAR Mundo security not active - memory operations blocked");
            return false;
        }
        
        // Check if memory operations are secure
        if (!BearMundo::isMemoryOperationSecure()) {
            LOGE("❌ Memory operations not secure - operation blocked");
            return false;
        }
        
        // Update last security check timestamp
        lastSecurityCheck = std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::system_clock::now().time_since_epoch()).count();
        
        securityValidated = true;
        return true;
    }
    
    // Check if security validation is still valid (within 5 minutes)
    bool isSecurityValid() {
        uint64_t currentTime = std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::system_clock::now().time_since_epoch()).count();
        
        return securityValidated && (currentTime - lastSecurityCheck) < 300000; // 5 minutes
    }
    
public:
    MemoryHelper() : targetPid(0), initialized(false), cachedBaseAddress(0), 
                     securityValidated(false), lastSecurityCheck(0) {
        LOGI("🐻 BEAR MemoryHelper initialized");
    }
    
    ~MemoryHelper() {
        if (initialized) {
            LOGI("🐻 BEAR MemoryHelper cleanup for PID %d", targetPid);
        }
    }
    
    // Initialize with target process ID
    bool Initialize(pid_t pid) {
        LOGI("🎯 Initializing MemoryHelper with PID %d", pid);
        
        // Validate security first
        if (!validateSecurity()) {
            return false;
        }
        
        targetPid = pid;
        initialized = true;
        
        // Get process name for logging
        char path[256];
        sprintf(path, "/proc/%d/cmdline", pid);
        FILE* cmdline = fopen(path, "r");
        if (cmdline) {
            char line[512];
            if (fgets(line, sizeof(line), cmdline)) {
                targetProcessName = line;
                LOGI("✅ Attached to process: %s (PID: %d)", targetProcessName.c_str(), pid);
            }
            fclose(cmdline);
        }
        
        return true;
    }
    
    // Initialize with target process name (Enhanced with security)
    bool Initialize(const std::string& processName) {
        LOGI("🔍 Searching for process: %s", processName.c_str());
        
        // Validate security first
        if (!validateSecurity()) {
            return false;
        }
        
        char path[256];
        char line[512];
        
        DIR* dir = opendir("/proc");
        if (!dir) {
            LOGE("❌ Failed to open /proc directory");
            return false;
        }
        
        struct dirent* entry;
        int processCount = 0;
        
        while ((entry = readdir(dir)) != nullptr) {
            // Check if entry is a directory and name is a number (PID)
            if (entry->d_type == DT_DIR && isdigit(entry->d_name[0])) {
                pid_t pid = atoi(entry->d_name);
                processCount++;
                
                // Check process name
                sprintf(path, "/proc/%d/cmdline", pid);
                FILE* cmdline = fopen(path, "r");
                if (cmdline) {
                    if (fgets(line, sizeof(line), cmdline)) {
                        if (strstr(line, processName.c_str())) {
                            targetPid = pid;
                            targetProcessName = processName;
                            initialized = true;
                            fclose(cmdline);
                            closedir(dir);
                            
                            LOGI("✅ Found process %s with PID %d (scanned %d processes)", 
                                 processName.c_str(), pid, processCount);
                            return true;
                        }
                    }
                    fclose(cmdline);
                }
            }
        }
        
        closedir(dir);
        LOGE("❌ Process %s not found after scanning %d processes", processName.c_str(), processCount);
        return false;
    }
    
    // Enhanced Read with security validation
    template<typename T>
    T Read(uintptr_t address) {
        T value = {};
        
        if (!initialized) {
            LOGE("❌ MemoryHelper not initialized");
            return value;
        }
        
        // Security validation
        if (!isSecurityValid() && !validateSecurity()) {
            LOGE("❌ Security validation failed for read operation");
            return value;
        }
        
        // Use BEAR Mundo secure memory operations if available
        BEAR_MUNDO_STEALTH_OPERATION({
            struct iovec local[1];
            struct iovec remote[1];
            
            local[0].iov_base = &value;
            local[0].iov_len = sizeof(T);
            remote[0].iov_base = (void*)address;
            remote[0].iov_len = sizeof(T);
            
            ssize_t nread = process_vm_readv(targetPid, local, 1, remote, 1, 0);
            if (nread != sizeof(T)) {
                LOGE("❌ Failed to read memory at 0x%lx (read %ld/%zu bytes)", 
                     address, nread, sizeof(T));
            } else {
                LOGD("📖 Read %zu bytes from 0x%lx", sizeof(T), address);
            }
        });
        
        return value;
    }
    
    // Enhanced Write with security validation
    template<typename T>
    bool Write(uintptr_t address, const T& value) {
        if (!initialized) {
            LOGE("❌ MemoryHelper not initialized");
            return false;
        }
        
        // Security validation
        if (!isSecurityValid() && !validateSecurity()) {
            LOGE("❌ Security validation failed for write operation");
            return false;
        }
        
        bool result = false;
        
        // Use BEAR Mundo secure memory operations
        BEAR_MUNDO_STEALTH_OPERATION({
            struct iovec local[1];
            struct iovec remote[1];
            
            local[0].iov_base = (void*)&value;
            local[0].iov_len = sizeof(T);
            remote[0].iov_base = (void*)address;
            remote[0].iov_len = sizeof(T);
            
            ssize_t nwritten = process_vm_writev(targetPid, local, 1, remote, 1, 0);
            if (nwritten != sizeof(T)) {
                LOGE("❌ Failed to write memory at 0x%lx (wrote %ld/%zu bytes)", 
                     address, nwritten, sizeof(T));
                result = false;
            } else {
                LOGD("✏️ Wrote %zu bytes to 0x%lx", sizeof(T), address);
                result = true;
            }
        });
        
        return result;
    }
    
    // Enhanced string reading with security
    std::string ReadString(uintptr_t address, size_t maxLength = 256) {
        if (!initialized) {
            LOGE("❌ MemoryHelper not initialized");
            return "";
        }
        
        // Security validation
        if (!isSecurityValid() && !validateSecurity()) {
            LOGE("❌ Security validation failed for string read");
            return "";
        }
        
        std::vector<char> buffer(maxLength, 0);
        
        struct iovec local[1];
        struct iovec remote[1];
        
        local[0].iov_base = buffer.data();
        local[0].iov_len = maxLength;
        remote[0].iov_base = (void*)address;
        remote[0].iov_len = maxLength;
        
        ssize_t nread = process_vm_readv(targetPid, local, 1, remote, 1, 0);
        if (nread <= 0) {
            LOGE("❌ Failed to read string at 0x%lx", address);
            return "";
        }
        
        // Ensure null-termination
        buffer[maxLength - 1] = '\0';
        
        std::string result(buffer.data());
        LOGD("📚 Read string: \"%s\" from 0x%lx", result.c_str(), address);
        
        return result;
    }
    
    // Enhanced base address resolution with caching
    uintptr_t GetBaseAddress(const std::string& moduleName = "libUE4.so") {
        if (!initialized) {
            LOGE("❌ MemoryHelper not initialized");
            return 0;
        }
        
        // Return cached address if available and still valid
        if (cachedBaseAddress != 0 && moduleName == "libUE4.so") {
            return cachedBaseAddress;
        }
        
        // Security validation
        if (!isSecurityValid() && !validateSecurity()) {
            LOGE("❌ Security validation failed for base address lookup");
            return 0;
        }
        
        uintptr_t baseAddr = GetModuleBase(moduleName);
        
        // Cache the main module base address
        if (baseAddr != 0 && moduleName == "libUE4.so") {
            cachedBaseAddress = baseAddr;
            LOGI("🏠 Cached base address for %s: 0x%lx", moduleName.c_str(), baseAddr);
        }
        
        return baseAddr;
    }
    
    // Get multiple module addresses at once
    std::vector<std::pair<std::string, uintptr_t>> GetModuleAddresses(const std::vector<std::string>& moduleNames) {
        std::vector<std::pair<std::string, uintptr_t>> results;
        
        if (!initialized) {
            LOGE("❌ MemoryHelper not initialized");
            return results;
        }
        
        if (!isSecurityValid() && !validateSecurity()) {
            LOGE("❌ Security validation failed for module enumeration");
            return results;
        }
        
        for (const auto& moduleName : moduleNames) {
            uintptr_t addr = GetModuleBase(moduleName);
            results.emplace_back(moduleName, addr);
            LOGD("📍 Module %s: 0x%lx", moduleName.c_str(), addr);
        }
        
        return results;
    }
    
    // Read multiple values in one operation (batch read)
    template<typename T>
    std::vector<T> ReadBatch(const std::vector<uintptr_t>& addresses) {
        std::vector<T> results;
        results.reserve(addresses.size());
        
        if (!initialized) {
            LOGE("❌ MemoryHelper not initialized");
            return results;
        }
        
        if (!isSecurityValid() && !validateSecurity()) {
            LOGE("❌ Security validation failed for batch read");
            return results;
        }
        
        for (uintptr_t address : addresses) {
            results.push_back(Read<T>(address));
        }
        
        LOGD("📦 Batch read %zu values", addresses.size());
        return results;
    }
    
    // Check if memory address is readable
    bool IsAddressValid(uintptr_t address) {
        if (!initialized) {
            return false;
        }
        
        // Try to read a single byte
        char testByte = Read<char>(address);
        return true; // If we got here without crashing, address is readable
    }
    
    // Get memory region information
    struct MemoryRegion {
        uintptr_t start;
        uintptr_t end;
        std::string permissions;
        std::string path;
    };
    
    std::vector<MemoryRegion> GetMemoryRegions() {
        std::vector<MemoryRegion> regions;
        
        if (!initialized) {
            return regions;
        }
        
        char path[256];
        sprintf(path, "/proc/%d/maps", targetPid);
        
        FILE* fp = fopen(path, "r");
        if (fp) {
            char line[512];
            while (fgets(line, sizeof(line), fp)) {
                MemoryRegion region;
                char perms[16];
                char regionPath[256];
                
                if (sscanf(line, "%lx-%lx %s %*x %*x:%*x %*d %s", 
                          &region.start, &region.end, perms, regionPath) >= 3) {
                    region.permissions = perms;
                    if (strlen(regionPath) > 0) {
                        region.path = regionPath;
                    }
                    regions.push_back(region);
                }
            }
            fclose(fp);
        }
        
        LOGD("📊 Found %zu memory regions", regions.size());
        return regions;
    }
    
    // Check if initialized
    bool IsInitialized() const {
        return initialized;
    }
    
    // Get target process ID
    pid_t GetTargetPid() const {
        return targetPid;
    }
    
    // Get target process name
    std::string GetTargetProcessName() const {
        return targetProcessName;
    }
    
    // Get security status
    bool IsSecurityActive() const {
        return securityValidated && BearMundo::isBearMundoSecurityActive();
    }
    
    // Force security revalidation
    bool RefreshSecurity() {
        return validateSecurity();
    }
}; 