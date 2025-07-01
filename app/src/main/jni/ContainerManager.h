#ifndef BEAR_MUNDO_CONTAINER_MANAGER_H
#define BEAR_MUNDO_CONTAINER_MANAGER_H

#include <string>
#include <memory>
#include <unordered_map>
#include <mutex>
#include <random>

namespace BearMundo {
namespace Container {

// ========================================
// ENUMERATIONS
// ========================================

enum class ContainerType {
    STANDARD = 0,
    PRIVILEGED = 1,
    STEALTH = 2,
    DECOY = 3
};

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

enum class EnvironmentType {
    UNKNOWN = 0,
    NON_ROOT = 1,
    ROOT = 2,
    EMULATOR = 3
};

// ========================================
// SECURITY CONTEXT
// ========================================

struct RandomizedSecurityContext {
    SecurityLevel level;
    OperationMode mode;
    uint64_t checksum;
    uint32_t token;
    
    RandomizedSecurityContext() 
        : level(SecurityLevel::BASIC)
        , mode(OperationMode::NORMAL)
        , checksum(0)
        , token(0) {
        generateToken();
    }
    
    void generateToken() {
        static std::random_device rd;
        static std::mt19937 gen(rd());
        token = gen();
        checksum = static_cast<uint64_t>(level) ^ static_cast<uint64_t>(mode) ^ token;
    }
    
    bool validateIntegrity() const {
        uint64_t expectedChecksum = static_cast<uint64_t>(level) ^ static_cast<uint64_t>(mode) ^ token;
        return checksum == expectedChecksum;
    }
};

// ========================================
// CONTAINER CONFIGURATION
// ========================================

struct ContainerConfiguration {
    ContainerType type;
    SecurityLevel securityLevel;
    OperationMode operationMode;
    
    bool enableStealth;
    bool enableAntiDetection;
    bool enableMemoryProtection;
    
    ContainerConfiguration();
    bool validate() const;
};

// ========================================
// CONTAINER INSTANCE
// ========================================

class ContainerInstance {
public:
    std::string containerId;
    ContainerConfiguration config;
    EnvironmentType detectedEnvironment;
    bool isActive;
    
    uint64_t creationTime;
    uint64_t lastActivity;
    
    RandomizedSecurityContext* securityContext;
    
public:
    ContainerInstance(const std::string& id, const ContainerConfiguration& cfg);
    ~ContainerInstance();
    
    bool activate();
    bool deactivate();
    bool updateActivity();
    bool validateSecurity();
};

// ========================================
// CONTAINER MANAGER
// ========================================

class BearMundoContainerManager {
private:
    static BearMundoContainerManager* s_instance;
    static std::mutex s_instanceMutex;
    
    bool m_initialized;
    EnvironmentType m_detectedEnvironment;
    
    std::unordered_map<std::string, std::unique_ptr<ContainerInstance>> m_containers;
    ContainerInstance* m_activeContainer;
    
    mutable std::mutex m_containerMutex;
    
private:
    BearMundoContainerManager();
    
public:
    ~BearMundoContainerManager();
    
    // Singleton access
    static BearMundoContainerManager* getInstance();
    
    // Initialization
    bool initialize();
    bool isManagerInitialized() const;
    
    // Environment detection
    EnvironmentType detectEnvironment();
    bool isRootEnvironment() const;
    bool isEmulatorEnvironment() const;
    EnvironmentType getDetectedEnvironment() const;
    
    // Container management
    std::string generateContainerId();
    std::string createContainer(const ContainerConfiguration& config);
    bool activateContainer(const std::string& containerId);
    bool deactivateContainer(const std::string& containerId);
    
    // Container access
    ContainerInstance* getActiveContainer();
    ContainerInstance* getContainer(const std::string& containerId);
    size_t getContainerCount() const;
};

// ========================================
// FACTORY FUNCTIONS
// ========================================

ContainerConfiguration createStandardConfiguration();
ContainerConfiguration createRootConfiguration();
ContainerConfiguration createNonRootConfiguration();
ContainerConfiguration createStealthConfiguration();
ContainerConfiguration createDecoyConfiguration();

bool validateContainerConfig(const ContainerConfiguration& config);
ContainerConfiguration getRecommendedConfiguration(EnvironmentType env);
bool isConfigurationCompatible(const ContainerConfiguration& config, EnvironmentType env);
std::string generateSecureContainerId();

} // namespace Container
} // namespace BearMundo

#endif // BEAR_MUNDO_CONTAINER_MANAGER_H 