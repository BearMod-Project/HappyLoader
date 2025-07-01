#ifndef STEALTH_CONTAINER_MANAGER_H
#define STEALTH_CONTAINER_MANAGER_H

#include <string>
#include <memory>
#include <map>
#include <vector>
#include <jni.h>
#include <android/log.h>

namespace StealthContainer {

// Forward declarations
class ConnectionManager;
class SecurityManager;
class DataStore;
class EventBus;

/**
 * KeyAuth Configuration Structure
 */
namespace KeyAuth {
    struct Config {
        std::string name;
        std::string ownerid;
        std::string version;
        std::string api_endpoint;
        std::string signature;
        
        Config(const std::string& app_name,
               const std::string& owner_id, 
               const std::string& app_version,
               const std::string& endpoint,
               const std::string& sig)
            : name(app_name), ownerid(owner_id), version(app_version),
              api_endpoint(endpoint), signature(sig) {}
    };
    
    class API {
    public:
        API(const Config& config) : config_(config) {}
        bool init() { return true; } // Placeholder implementation
        bool license(const std::string& license_key) { return true; } // Placeholder
        
    private:
        Config config_;
    };
}

/**
 * Brand Configuration Structure
 */
struct BrandConfig {
    std::string brand_name;
    std::string package_name;
    std::string keyauth_name;
    std::string keyauth_ownerid;
    std::string signature_hash;
    std::string api_endpoint;
    
    BrandConfig() = default;
    
    BrandConfig(const std::string& brand,
                const std::string& package,
                const std::string& ka_name,
                const std::string& ka_ownerid,
                const std::string& sig_hash,
                const std::string& endpoint = "https://keyauth.win/api/1.2/")
        : brand_name(brand), package_name(package), keyauth_name(ka_name),
          keyauth_ownerid(ka_ownerid), signature_hash(sig_hash), api_endpoint(endpoint) {}
};

/**
 * BrandContainer - Individual container for each brand/app
 */
class BrandContainer {
public:
    BrandContainer(const BrandConfig& brand_config);
    ~BrandContainer();
    
    // Lifecycle
    bool initialize();
    
    // Authentication
    bool validateSignature();
    bool authenticateWithKeyAuth(const std::string& license);
    
    // Security controls
    void enableStealthMode();
    void disableStealthMode();
    
    // Getters
    const BrandConfig& getConfig() const { return config; }
    bool isInitialized() const { return is_initialized; }
    
    // Component access
    ConnectionManager* getConnectionManager() const { return conn_manager.get(); }
    SecurityManager* getSecurityManager() const { return security.get(); }
    DataStore* getDataStore() const { return data_store.get(); }
    EventBus* getEventBus() const { return event_bus.get(); }
    
private:
    BrandConfig config;
    bool is_initialized;
    
    // Components
    std::unique_ptr<ConnectionManager> conn_manager;
    std::unique_ptr<SecurityManager> security;
    std::unique_ptr<DataStore> data_store;
    std::unique_ptr<EventBus> event_bus;
};

/**
 * ContainerManager - Singleton manager for all brand containers
 */
class ContainerManager {
public:
    static ContainerManager* getInstance();
    
    // Container management
    bool createContainer(const std::string& brand_name);
    BrandContainer* getContainer(const std::string& brand_name);
    void removeContainer(const std::string& brand_name);
    
    // Authentication
    bool authenticateContainer(const std::string& brand_name, const std::string& license);
    bool validateAllContainers();
    
    // Security operations
    void enableStealthMode(const std::string& brand_name);
    void disableStealthMode(const std::string& brand_name);
    
    // Status
    size_t getContainerCount() const { return containers.size(); }
    std::vector<std::string> getBrandNames() const;
    
    // Lifecycle
    void shutdown();
    
private:
    ContainerManager();
    static ContainerManager* instance;
    
    // Container storage
    std::map<std::string, std::unique_ptr<BrandContainer>> containers;
    
    // Initialization
    void initializeBrandConfigs();
};

} // namespace StealthContainer

// JNI Bridge Functions
extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_container_StealthManager_nativeInitializeContainer(JNIEnv* env,
                                                                      jclass clazz,
                                                                      jstring brand_name);

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_container_StealthManager_nativeAuthenticateContainer(JNIEnv* env,
                                                                        jclass clazz,
                                                                        jstring brand_name,
                                                                        jstring license);

JNIEXPORT void JNICALL
Java_com_happy_pro_container_StealthManager_nativeEnableStealthMode(JNIEnv* env,
                                                                   jclass clazz,
                                                                   jstring brand_name);

JNIEXPORT void JNICALL
Java_com_happy_pro_container_StealthManager_nativeDisableStealthMode(JNIEnv* env,
                                                                    jclass clazz,
                                                                    jstring brand_name);

JNIEXPORT jboolean JNICALL
Java_com_happy_pro_container_StealthManager_nativeValidateAllContainers(JNIEnv* env,
                                                                       jclass clazz);

JNIEXPORT jint JNICALL
Java_com_happy_pro_container_StealthManager_nativeGetContainerCount(JNIEnv* env,
                                                                   jclass clazz);

JNIEXPORT void JNICALL
Java_com_happy_pro_container_StealthManager_nativeShutdown(JNIEnv* env,
                                                         jclass clazz);

} // extern "C"

#endif // STEALTH_CONTAINER_MANAGER_H 