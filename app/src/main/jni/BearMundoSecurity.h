#ifndef BEAR_MUNDO_SECURITY_H
#define BEAR_MUNDO_SECURITY_H

#include <jni.h>
#include <string>
#include <vector>
#include <mutex>
#include <random>
#include <cstdint>
#include <atomic>
#include <memory>
#include <unistd.h>
#include <sys/mman.h>

namespace BearMundo {

// ========================================
// SECURITY ENUMERATIONS
// ========================================

enum class SecurityLevel {
    BASIC = 0,
    ENHANCED = 1,
    MAXIMUM = 2
};

enum class OperationMode {
    NORMAL = 0,
    STEALTH = 1,
    DECOY = 2
};

enum class DetectionThreat {
    NONE = 0,
    LOW = 1,
    MEDIUM = 2,
    HIGH = 3,
    CRITICAL = 4
};

// ========================================
// SECURITY SYSTEM
// ========================================

extern std::atomic<bool> g_securityEnabled;
extern std::atomic<bool> g_antiDebugActive;
extern std::atomic<bool> g_memoryProtectionActive;

// ========================================
// SECURITY CONTEXT
// ========================================

struct RandomizedSecurityContext {
    SecurityLevel level;
    OperationMode mode;
    uint64_t checksum;
    uint32_t token;
    uint64_t lastValidation;
    
    RandomizedSecurityContext() 
        : level(SecurityLevel::BASIC)
        , mode(OperationMode::NORMAL)
        , checksum(0)
        , token(0)
        , lastValidation(0) {
        generateToken();
    }
    
    void generateToken() {
        static std::random_device rd;
        static std::mt19937 gen(rd());
        token = gen();
        checksum = static_cast<uint64_t>(level) ^ static_cast<uint64_t>(mode) ^ token;
        lastValidation = getCurrentTime();
    }
    
    bool validateIntegrity() const {
        uint64_t expectedChecksum = static_cast<uint64_t>(level) ^ static_cast<uint64_t>(mode) ^ token;
        return checksum == expectedChecksum;
    }
    
    bool isExpired(uint64_t maxAge = 300000) const { // 5 minutes default
        uint64_t now = getCurrentTime();
        return (now - lastValidation) > maxAge;
    }
    
private:
    static uint64_t getCurrentTime() {
        return std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::system_clock::now().time_since_epoch()).count();
    }
};

// ========================================
// BEAR MUNDO SECURITY FUNCTIONS
// ========================================

/**
 * Initialize BEAR Mundo Security System
 */
bool initializeBearMundoSecurity(JNIEnv* env);

/**
 * Check if BEAR Mundo Security is active
 */
bool isBearMundoSecurityActive();

/**
 * Get current security level
 */
SecurityLevel getCurrentSecurityLevel();

/**
 * Enable stealth mode
 */
bool enableStealthMode();

/**
 * Disable stealth mode
 */
bool disableStealthMode();

/**
 * Validate security environment
 */
DetectionThreat validateSecurityEnvironment();

/**
 * Validate KeyAuth with security checks
 */
bool validateKeyAuthWithSecurity();

/**
 * Check if memory operations are secure
 */
bool isMemoryOperationSecure();

/**
 * Check if ESP operations are secure
 */
bool isESPOperationSecure();

// ========================================
// DETECTION FUNCTIONS
// ========================================

/**
 * Detect Frida framework
 */
bool detectFridaFramework();

/**
 * Detect advanced debugging
 */
bool detectAdvancedDebugging();

/**
 * Detect root with evasion
 */
bool detectRootWithEvasion();

/**
 * Detect emulator environment
 */
bool detectEmulatorEnvironment();

// ========================================
// UTILITY FUNCTIONS
// ========================================

/**
 * Generate random stack name
 */
std::string generateRandomStackName();

/**
 * Generate obfuscated function name
 */
std::string generateObfuscatedFunctionName();

/**
 * Create random delay
 */
void randomDelay();

/**
 * Create decoy operations
 */
void createDecoyOperations();

// ========================================
// ENCRYPTION UTILITIES
// ========================================

/**
 * Generate secure random key
 */
std::string generateSecureKey(size_t length = 32);

/**
 * XOR encryption/decryption
 */
std::vector<uint8_t> xorCrypt(const std::vector<uint8_t>& data, const std::string& key);

/**
 * Simple AES-like encryption (for demo purposes)
 */
std::vector<uint8_t> simpleEncrypt(const std::vector<uint8_t>& data, const std::string& key);

/**
 * Simple AES-like decryption (for demo purposes)
 */
std::vector<uint8_t> simpleDecrypt(const std::vector<uint8_t>& encryptedData, const std::string& key);

namespace security {

// ========================================
// BEAR MEMORY MANAGER
// ========================================

class BearMemoryProtection {
private:
    bool initialized = false;
    std::vector<std::pair<void*, size_t>> protectedRegions;
    mutable std::mutex protectionMutex;

public:
    bool initialize();
    bool protectRegion(void* address, size_t size);
    bool isRegionProtected(void* address);
    std::vector<std::string> getProtectionStatus();
    void cleanup();
    
    static BearMemoryProtection& getInstance() {
        static BearMemoryProtection instance;
        return instance;
    }
};

class BearMemoryManager {
private:
    std::unique_ptr<BearMemoryProtection> protection;
    bool initialized = false;

public:
    static BearMemoryManager& getInstance() {
        static BearMemoryManager instance;
        return instance;
    }
    
    bool initializeProtection();
    bool protectBearComponents();
    bool enableAdvancedProtection();
    BearMemoryProtection* getProtection();
};

} // namespace security

/**
 * Enable all security features
 */
bool enableSecurity();

/**
 * Disable security (for debugging)
 */
void disableSecurity();

/**
 * Check for debugging tools
 */
bool isBeingDebugged();

/**
 * Protect memory region
 */
bool protectMemoryRegion(void* addr, size_t size);

/**
 * Get process ID by name
 */
int getProcessId(const char* processName);

/**
 * Read memory from target process
 */
bool readMemory(int pid, uintptr_t address, void* buffer, size_t size);

/**
 * Write memory to target process
 */
bool writeMemory(int pid, uintptr_t address, const void* data, size_t size);

/**
 * Get module base address
 */
uintptr_t getModuleBase(int pid, const char* moduleName);

} // namespace BearMundo

#endif // BEAR_MUNDO_SECURITY_H 