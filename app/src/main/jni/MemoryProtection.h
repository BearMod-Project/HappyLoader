#ifndef BEARMUNDO_MEMORY_PROTECTION_H
#define BEARMUNDO_MEMORY_PROTECTION_H

#include <string>
#include <vector>
#include <chrono>
#include <atomic>
#include <mutex>

#ifdef __ANDROID__
#include <android/log.h>
#define BEAR_LOG_TAG "BEAR-MemoryProtection"
#define BEAR_LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, BEAR_LOG_TAG, __VA_ARGS__)
#define BEAR_LOGI(...) __android_log_print(ANDROID_LOG_INFO, BEAR_LOG_TAG, __VA_ARGS__)
#define BEAR_LOGW(...) __android_log_print(ANDROID_LOG_WARN, BEAR_LOG_TAG, __VA_ARGS__)
#define BEAR_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, BEAR_LOG_TAG, __VA_ARGS__)
#else
#define BEAR_LOGD(...)
#define BEAR_LOGI(...)
#define BEAR_LOGW(...)
#define BEAR_LOGE(...)
#endif

namespace bearmundo {
namespace security {

class MemoryProtection {
public:
    MemoryProtection();
    ~MemoryProtection();

    // Core functionality
    bool initialize();
    void cleanup();
    
    // Memory protection operations
    bool protectRegion(void* address, size_t size);
    bool unprotectRegion(void* address, size_t size);
    bool isRegionProtected(void* address) const;
    
    // Advanced protection features
    bool enableAntiDebug();
    bool enableAntiTamper();
    bool enableStackProtection();
    bool enableHeapProtection();
    
    // BEAR-LOADER specific features
    bool protectBearCode();
    bool protectHookRegions();
    bool enableStealthMode();
    
    // Status and monitoring
    bool isInitialized() const;
    std::string getLastError() const;
    size_t getProtectedRegionCount() const;
    std::vector<std::string> getProtectionStatus() const;

private:
    // Prevent copying
    MemoryProtection(const MemoryProtection&) = delete;
    MemoryProtection& operator=(const MemoryProtection&) = delete;

    // Internal state
    std::atomic<bool> initialized_{false};
    std::atomic<bool> stealth_mode_{false};
    std::string last_error_;
    mutable std::mutex regions_mutex_;
    
    // Memory region tracking
    struct MemoryRegion {
        void* address;
        size_t size;
        bool is_protected;
        std::string region_name;
        std::chrono::system_clock::time_point protection_time;
        uint32_t original_protection;
    };
    std::vector<MemoryRegion> protected_regions_;
    
    // Internal utilities
    bool setupMemoryProtection();
    bool enableASLR();
    bool protectCriticalRegions();
    bool setupAntiDebugProtection();
    bool setupAntiTamperProtection();
    void setLastError(const std::string& error);
    
    // Platform-specific implementations
    bool makeReadOnly(void* address, size_t size);
    bool makeExecutable(void* address, size_t size);
    bool makeNonExecutable(void* address, size_t size);
    uint32_t getCurrentProtection(void* address) const;
    
    // BEAR-LOADER integration
    void logProtectionEvent(const std::string& event);
    bool isBearCodeRegion(void* address) const;
    bool isHookRegion(void* address) const;
};

// Singleton access for BEAR-LOADER integration
class BearMemoryManager {
public:
    static BearMemoryManager& getInstance();
    
    bool initializeProtection();
    bool protectBearComponents();
    bool enableAdvancedProtection();
    MemoryProtection* getProtection();

public:
    ~BearMemoryManager() = default;
    
private:
    BearMemoryManager() = default;
    
    std::unique_ptr<MemoryProtection> protection_;
    std::atomic<bool> initialized_{false};
};

} // namespace security
} // namespace bearmundo

#endif // BEARMUNDO_MEMORY_PROTECTION_H 