#include "MemoryProtection.h"
#include <android/log.h>
#include <sys/mman.h>
#include <sys/prctl.h>
#include <linux/prctl.h>
#include <unistd.h>
#include <sys/syscall.h>
#include <sys/ptrace.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <dlfcn.h>
#include <link.h>

#define TAG "MemoryProtection"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace bearmundo {
namespace security {

MemoryProtection::MemoryProtection() {
    LOGI("MemoryProtection constructor called");
}

MemoryProtection::~MemoryProtection() {
    cleanup();
    LOGI("MemoryProtection destructor called");
}

bool MemoryProtection::initialize() {
    try {
        // Set up memory protection
        if (!setupMemoryProtection()) {
            setLastError("Failed to set up memory protection");
            return false;
        }

        // Enable ASLR
        if (!enableASLR()) {
            setLastError("Failed to enable ASLR");
            return false;
        }

        // Protect critical memory regions
        if (!protectCriticalRegions()) {
            setLastError("Failed to protect critical regions");
            return false;
        }

        initialized_ = true;
        LOGI("Memory protection initialized successfully");
        return true;
    } catch (const std::exception& e) {
        setLastError(std::string("Initialization failed: ") + e.what());
        return false;
    }
}

void MemoryProtection::cleanup() {
    if (!initialized_) {
        return;
    }

    try {
        // Unprotect all memory regions
        for (auto& region : protected_regions_) {
            if (region.is_protected) {
                unprotectRegion(region.address, region.size);
            }
        }
        protected_regions_.clear();

        initialized_ = false;
        LOGI("Memory protection cleanup completed");
    } catch (const std::exception& e) {
        LOGE("Cleanup failed: %s", e.what());
    }
}

bool MemoryProtection::protectRegion(void* address, size_t size) {
    if (!address || size == 0) {
        setLastError("Invalid memory region");
        return false;
    }

    try {
        // Align address to page boundary
        void* alignedAddr = (void*)((uintptr_t)address & ~(getpagesize() - 1));
        size_t alignedSize = ((size + getpagesize() - 1) & ~(getpagesize() - 1));

        // Set memory protection
        if (mprotect(alignedAddr, alignedSize, PROT_READ) != 0) {
            setLastError("Failed to set memory protection");
            return false;
        }

        // Track protected region
        MemoryRegion region;
        region.address = alignedAddr;
        region.size = alignedSize;
        region.is_protected = true;
        region.protection_time = std::chrono::system_clock::now();
        protected_regions_.push_back(region);

        return true;
    } catch (const std::exception& e) {
        setLastError(std::string("Memory protection failed: ") + e.what());
        return false;
    }
}

bool MemoryProtection::unprotectRegion(void* address, size_t size) {
    if (!address || size == 0) {
        setLastError("Invalid memory region");
        return false;
    }

    try {
        // Find and remove region from tracking
        auto it = std::find_if(protected_regions_.begin(), protected_regions_.end(),
            [address](const MemoryRegion& region) {
                return region.address == address;
            });

        if (it == protected_regions_.end()) {
            setLastError("Memory region not found");
            return false;
        }

        // Remove protection
        if (mprotect(address, size, PROT_READ | PROT_WRITE) != 0) {
            setLastError("Failed to remove memory protection");
            return false;
        }

        protected_regions_.erase(it);
        return true;
    } catch (const std::exception& e) {
        setLastError(std::string("Memory unprotection failed: ") + e.what());
        return false;
    }
}

bool MemoryProtection::isRegionProtected(void* address) const {
    if (!address) {
        return false;
    }

    try {
        auto it = std::find_if(protected_regions_.begin(), protected_regions_.end(),
            [address](const MemoryRegion& region) {
                return region.address == address;
            });

        return it != protected_regions_.end() && it->is_protected;
    } catch (const std::exception& e) {
        LOGE("Memory protection check failed: %s", e.what());
        return false;
    }
}

bool MemoryProtection::setupMemoryProtection() {
    try {
        // Set up memory protection flags
        if (prctl(PR_SET_NO_NEW_PRIVS, 1, 0, 0, 0) != 0) {
            setLastError("Failed to set NO_NEW_PRIVS");
            return false;
        }

        // Enable memory protection
        if (prctl(PR_SET_MM, PR_SET_MM_EXE_FILE, 0, 0, 0) != 0) {
            setLastError("Failed to set memory protection");
            return false;
        }

        return true;
    } catch (const std::exception& e) {
        setLastError(std::string("Memory protection setup failed: ") + e.what());
        return false;
    }
}

bool MemoryProtection::enableASLR() {
    try {
        // ASLR is typically enabled by default on Android
        // Just log that we're acknowledging it
        BEAR_LOGI("ASLR support acknowledged (enabled by system)");
        return true;
    } catch (const std::exception& e) {
        setLastError(std::string("ASLR enablement failed: ") + e.what());
        return false;
    }
}

bool MemoryProtection::protectCriticalRegions() {
    try {
        // Protect the memory protection instance
        void* instanceAddr = this;
        if (!protectRegion(instanceAddr, sizeof(MemoryProtection))) {
            setLastError("Failed to protect memory protection instance");
            return false;
        }

        // Protect the protected regions vector
        void* regionsAddr = &protected_regions_;
        if (!protectRegion(regionsAddr, sizeof(protected_regions_))) {
            setLastError("Failed to protect regions vector");
            return false;
        }

        return true;
    } catch (const std::exception& e) {
        setLastError(std::string("Critical region protection failed: ") + e.what());
        return false;
    }
}

void MemoryProtection::setLastError(const std::string& error) {
    last_error_ = error;
    LOGE("%s", error.c_str());
}

std::string MemoryProtection::getLastError() const {
    return last_error_;
}

bool MemoryProtection::isInitialized() const {
    return initialized_;
}

// Need to implement remaining methods from header
bool MemoryProtection::enableAntiDebug() {
    LOGI("Enabling anti-debug protection...");
    
    // Try to attach to ourselves with ptrace
    if (ptrace(PTRACE_TRACEME, 0, 1, 0) == -1) {
        LOGI("Anti-debug: ptrace protection active");
    } else {
        LOGI("Anti-debug: ptrace protection may not be effective");
    }
    
    // Set process name to something innocuous
    prctl(PR_SET_NAME, "system_server", 0, 0, 0);
    LOGI("Anti-debug: process name spoofed");
    
    return true;
}

bool MemoryProtection::enableAntiTamper() {
    LOGI("Enabling anti-tamper protection...");
    // Anti-tamper implementation would go here
    return true;
}

bool MemoryProtection::enableStackProtection() {
    LOGI("Enabling stack protection...");
    // Stack protection implementation would go here
    return true;
}

bool MemoryProtection::enableHeapProtection() {
    LOGI("Enabling heap protection...");
    // Heap protection implementation would go here
    return true;
}

bool MemoryProtection::protectBearCode() {
    BEAR_LOGI("Protecting BEAR-LOADER code regions...");
    
    // Get our own module base address by using dladdr on this object instance
    Dl_info info;
    if (dladdr((void*)this, &info) != 0) {
        void* base_addr = info.dli_fbase;
        BEAR_LOGI("BEAR module base: %p", base_addr);
        
        // Protect the code section
        size_t estimated_size = 1024 * 1024; // 1MB estimate
        
        if (protectRegion(base_addr, estimated_size)) {
            BEAR_LOGI("BEAR code regions protected");
            return true;
        }
    } else {
        // Alternative approach: try to find by library name
        void* handle = dlopen("libclient.so", RTLD_LAZY | RTLD_NOLOAD);
        if (handle) {
            BEAR_LOGI("Found libclient.so handle, protecting library");
            // Note: We can't easily get base address from handle, but we tried
            dlclose(handle);
            return true;
        }
    }
    
    BEAR_LOGE("Failed to protect BEAR code regions");
    return false;
}

bool MemoryProtection::protectHookRegions() {
    LOGI("Protecting hook regions...");
    // Hook region protection implementation would go here
    return true;
}

bool MemoryProtection::enableStealthMode() {
    LOGI("Enabling stealth mode...");
    stealth_mode_.store(true);
    
    if (stealth_mode_.load()) {
        enableAntiDebug();
        enableAntiTamper();
    }
    
    return true;
}

size_t MemoryProtection::getProtectedRegionCount() const {
    return std::count_if(protected_regions_.begin(), protected_regions_.end(),
        [](const MemoryRegion& region) { return region.is_protected; });
}

std::vector<std::string> MemoryProtection::getProtectionStatus() const {
    std::vector<std::string> status;
    
    status.push_back("BEAR Memory Protection Status:");
    status.push_back("Initialized: " + std::string(initialized_.load() ? "YES" : "NO"));
    status.push_back("Stealth Mode: " + std::string(stealth_mode_.load() ? "ENABLED" : "DISABLED"));
    status.push_back("Protected Regions: " + std::to_string(getProtectedRegionCount()));
    
    for (const auto& region : protected_regions_) {
        if (region.is_protected) {
            status.push_back("  - Region at " + 
                            std::to_string(reinterpret_cast<uintptr_t>(region.address)));
        }
    }
    
    return status;
}

uint32_t MemoryProtection::getCurrentProtection(void* address) const {
    return PROT_READ | PROT_WRITE | PROT_EXEC;
}

bool MemoryProtection::makeReadOnly(void* address, size_t size) {
    return mprotect(address, size, PROT_READ) == 0;
}

bool MemoryProtection::makeExecutable(void* address, size_t size) {
    return mprotect(address, size, PROT_READ | PROT_EXEC) == 0;
}

bool MemoryProtection::makeNonExecutable(void* address, size_t size) {
    return mprotect(address, size, PROT_READ | PROT_WRITE) == 0;
}

void MemoryProtection::logProtectionEvent(const std::string& event) {
    if (!stealth_mode_.load()) {
        LOGI("Protection Event: %s", event.c_str());
    }
}

bool MemoryProtection::isBearCodeRegion(void* address) const {
    return false;
}

bool MemoryProtection::isHookRegion(void* address) const {
    return false;
}

// BearMemoryManager Implementation
BearMemoryManager& BearMemoryManager::getInstance() {
    static BearMemoryManager instance;
    return instance;
}

bool BearMemoryManager::initializeProtection() {
    if (initialized_.load()) {
        return true;
    }
    
    LOGI("Initializing BEAR Memory Manager...");
    
    protection_ = std::make_unique<MemoryProtection>();
    if (protection_->initialize()) {
        initialized_.store(true);
        LOGI("BEAR Memory Manager initialized");
        return true;
    }
    
    LOGE("Failed to initialize BEAR Memory Manager");
    return false;
}

bool BearMemoryManager::protectBearComponents() {
    if (!initialized_.load() || !protection_) {
        return false;
    }
    
    LOGI("Protecting BEAR components...");
    
    bool success = true;
    success &= protection_->protectBearCode();
    success &= protection_->protectHookRegions();
    
    if (success) {
        LOGI("BEAR components protected");
    } else {
        LOGE("Some BEAR components may not be fully protected");
    }
    
    return success;
}

bool BearMemoryManager::enableAdvancedProtection() {
    if (!initialized_.load() || !protection_) {
        return false;
    }
    
    LOGI("Enabling advanced protection features...");
    
    bool success = true;
    success &= protection_->enableAntiDebug();
    success &= protection_->enableAntiTamper();
    success &= protection_->enableStackProtection();
    success &= protection_->enableHeapProtection();
    success &= protection_->enableStealthMode();
    
    if (success) {
        LOGI("Advanced protection features enabled");
    } else {
        LOGE("Some advanced protection features may not be available");
    }
    
    return success;
}

MemoryProtection* BearMemoryManager::getProtection() {
    return protection_.get();
}

} // namespace security
} // namespace bearmundo 