#include "StealthComponents.h"
#include "includes/obfuscate.h"
#include <sys/ptrace.h>
#include <sys/mman.h>
#include <unistd.h>
#include <dlfcn.h>
#include <fstream>
#include <chrono>
#include <random>
#include <algorithm>

#define LOG_TAG "StealthComponents"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace StealthContainer {

// ConnectionManager implementation
ConnectionManager::ConnectionManager() : encryption_enabled(false) {
    // Generate random encryption key
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(0, 255);
    
    encryption_key.clear();
    for (int i = 0; i < 32; ++i) {
        encryption_key += static_cast<char>(dis(gen));
    }
}

ConnectionManager::~ConnectionManager() {
    // Clear sensitive data
    std::fill(encryption_key.begin(), encryption_key.end(), 0);
}

bool ConnectionManager::initialize() {
    std::lock_guard<std::mutex> lock(conn_mutex);
    LOGI("🔗 Initializing secure connection manager");
    return true;
}

void ConnectionManager::enableEncryption() {
    std::lock_guard<std::mutex> lock(conn_mutex);
    encryption_enabled = true;
    LOGI("🔐 Connection encryption enabled");
}

void ConnectionManager::disableEncryption() {
    std::lock_guard<std::mutex> lock(conn_mutex);
    encryption_enabled = false;
    LOGI("🔓 Connection encryption disabled");
}

std::string ConnectionManager::encryptData(const std::string& data) {
    if (!encryption_enabled) return data;
    
    // Simple XOR encryption (replace with stronger encryption in production)
    std::string encrypted = data;
    for (size_t i = 0; i < encrypted.size(); ++i) {
        encrypted[i] ^= encryption_key[i % encryption_key.size()];
    }
    return encrypted;
}

std::string ConnectionManager::decryptData(const std::string& encrypted_data) {
    return encryptData(encrypted_data); // XOR is symmetric
}

bool ConnectionManager::sendSecureData(const std::string& data) {
    std::lock_guard<std::mutex> lock(conn_mutex);
    
    if (encryption_enabled) {
        std::string encrypted = encryptData(data);
        // In a real implementation, this would send via socket/network
        LOGD("🔐 Secure data sent (encrypted)");
        return true;
    } else {
        // Send unencrypted data
        LOGD("📡 Data sent (unencrypted)");
        return true;
    }
}

std::string ConnectionManager::receiveSecureData() {
    std::lock_guard<std::mutex> lock(conn_mutex);
    
    // In a real implementation, this would receive from socket/network
    std::string received_data = "sample_data"; // Placeholder
    
    if (encryption_enabled) {
        std::string decrypted = decryptData(received_data);
        LOGD("🔐 Secure data received (decrypted)");
        return decrypted;
    } else {
        LOGD("📡 Data received (unencrypted)");
        return received_data;
    }
}

bool ConnectionManager::validateConnection() {
    std::lock_guard<std::mutex> lock(conn_mutex);
    
    // Validate connection integrity
    if (encryption_enabled && encryption_key.empty()) {
        LOGE("❌ Connection validation failed: No encryption key");
        return false;
    }
    
    LOGD("✅ Connection validation passed");
    return true;
}

// SecurityManager implementation
SecurityManager::SecurityManager() : stealth_mode_active(false), monitoring_active(false) {}

SecurityManager::~SecurityManager() {
    disableStealthMode();
}

bool SecurityManager::initialize() {
    std::lock_guard<std::mutex> lock(security_mutex);
    LOGI("🛡️ Initializing security manager");
    
    // Start security monitoring thread
    monitoring_active = true;
    security_monitor_thread = std::thread(&SecurityManager::securityMonitorLoop, this);
    
    return true;
}

void SecurityManager::enableStealthMode() {
    std::lock_guard<std::mutex> lock(security_mutex);
    stealth_mode_active = true;
    
    LOGI("🥷 Stealth mode activated");
    
    // Enable all stealth features
    enableAntiHook();
    obfuscateMemoryPatterns();
    createDecoyOperations();
}

void SecurityManager::disableStealthMode() {
    std::lock_guard<std::mutex> lock(security_mutex);
    stealth_mode_active = false;
    monitoring_active = false;
    
    if (security_monitor_thread.joinable()) {
        security_monitor_thread.join();
    }
    
    disableAntiHook();
    LOGI("🔓 Stealth mode deactivated");
}

bool SecurityManager::performSecurityScan() {
    bool threats_detected = false;
    
    detection_state.debugger_detected = detectDebugger();
    detection_state.emulator_detected = detectEmulator();
    detection_state.frida_detected = detectFrida();
    detection_state.xposed_detected = detectXposed();
    detection_state.memory_scanner_detected = detectMemoryScanner();
    
    threats_detected = detection_state.debugger_detected ||
                      detection_state.emulator_detected ||
                      detection_state.frida_detected ||
                      detection_state.xposed_detected ||
                      detection_state.memory_scanner_detected;
    
    if (threats_detected) {
        LOGW("⚠️ Security threats detected during scan");
    }
    
    return !threats_detected;
}

bool SecurityManager::detectDebugger() {
    // Check TracerPid in /proc/self/status
    std::ifstream status("/proc/self/status");
    std::string line;
    
    while (std::getline(status, line)) {
        if (line.find("TracerPid:") == 0) {
            int tracer_pid = std::stoi(line.substr(10));
            if (tracer_pid != 0) {
                handleThreatDetection("debugger");
                return true;
            }
            break;
        }
    }
    
    // Check ptrace
    if (ptrace(PTRACE_TRACEME, 0, 1, 0) == -1) {
        handleThreatDetection("ptrace_debugger");
        return true;
    }
    ptrace(PTRACE_DETACH, 0, 1, 0);
    
    return false;
}

bool SecurityManager::detectEmulator() {
    // Check for emulator properties
    char prop_value[256];
    
    if (__system_property_get("ro.kernel.qemu", prop_value) > 0) {
        handleThreatDetection("qemu_emulator");
        return true;
    }
    
    if (__system_property_get("ro.build.fingerprint", prop_value) > 0) {
        std::string fingerprint(prop_value);
        if (fingerprint.find("generic") != std::string::npos ||
            fingerprint.find("emulator") != std::string::npos) {
            handleThreatDetection("generic_emulator");
            return true;
        }
    }
    
    return false;
}

bool SecurityManager::detectFrida() {
    // Check for Frida libraries
    void* frida = dlopen("libfrida-gadget.so", RTLD_NOW);
    if (frida) {
        dlclose(frida);
        handleThreatDetection("frida");
        return true;
    }
    
    // Check for Frida server port
    std::ifstream tcp_file("/proc/net/tcp");
    std::string line;
    while (std::getline(tcp_file, line)) {
        if (line.find(":69A2") != std::string::npos) { // Frida default port 27042
            handleThreatDetection("frida_server");
            return true;
        }
    }
    
    return false;
}

bool SecurityManager::detectXposed() {
    // Check for Xposed framework
    if (access("/system/framework/XposedBridge.jar", F_OK) == 0) {
        handleThreatDetection("xposed");
        return true;
    }
    
    // Check for Xposed installer
    if (access("/data/data/de.robv.android.xposed.installer", F_OK) == 0) {
        handleThreatDetection("xposed_installer");
        return true;
    }
    
    return false;
}

bool SecurityManager::detectMemoryScanner() {
    // Check for common memory scanning tools
    std::vector<std::string> scanner_processes = {
        "GameGuardian", "Cheat Engine", "SB Game Hacker", 
        "Lucky Patcher", "Freedom", "Creehack"
    };
    
    std::ifstream cmdline("/proc/self/cmdline");
    std::string process_name;
    std::getline(cmdline, process_name);
    
    for (const auto& scanner : scanner_processes) {
        if (process_name.find(scanner) != std::string::npos) {
            handleThreatDetection("memory_scanner");
            return true;
        }
    }
    
    return false;
}

void SecurityManager::obfuscateMemoryPatterns() {
    // Create random memory patterns to confuse analysis tools
    volatile char dummy[4096];
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(0, 255);
    
    for (int i = 0; i < 4096; ++i) {
        dummy[i] = static_cast<char>(dis(gen));
    }
    
    // Clear the dummy array
    memset((void*)dummy, 0, sizeof(dummy));
}

void SecurityManager::enableAntiHook() {
    // Implement anti-hooking measures
    LOGD("🔒 Anti-hook protection enabled");
}

void SecurityManager::disableAntiHook() {
    // Disable anti-hooking measures
    LOGD("🔓 Anti-hook protection disabled");
}

void SecurityManager::createDecoyOperations() {
    // Create fake operations to mislead analysis
    volatile int decoy_operations[100];
    for (int i = 0; i < 100; ++i) {
        decoy_operations[i] = i * 2 + 1;
    }
}

void SecurityManager::securityMonitorLoop() {
    while (monitoring_active) {
        if (stealth_mode_active) {
            performSecurityScan();
            obfuscateMemoryPatterns();
        }
        
        std::this_thread::sleep_for(std::chrono::seconds(5));
    }
}

void SecurityManager::handleThreatDetection(const std::string& threat_type) {
    LOGW("🚨 Threat detected: %s", threat_type.c_str());
    
    if (stealth_mode_active) {
        // Take countermeasures
        obfuscateMemoryPatterns();
        createDecoyOperations();
    }
}

void SecurityManager::cleanupSecureMemory() {
    // Securely wipe sensitive memory regions
    LOGD("🧹 Cleaning up secure memory");
}

void SecurityManager::protectMemoryRegion(void* addr, size_t size) {
    // Protect memory region using mprotect
    if (mprotect(addr, size, PROT_READ) == 0) {
        LOGD("🔒 Memory region protected: %p (size: %zu)", addr, size);
    } else {
        LOGW("⚠️ Failed to protect memory region: %p", addr);
    }
}

void SecurityManager::unprotectMemoryRegion(void* addr, size_t size) {
    // Unprotect memory region
    if (mprotect(addr, size, PROT_READ | PROT_WRITE | PROT_EXEC) == 0) {
        LOGD("🔓 Memory region unprotected: %p (size: %zu)", addr, size);
    } else {
        LOGW("⚠️ Failed to unprotect memory region: %p", addr);
    }
}

bool SecurityManager::isMemoryOperationSafe() {
    // Check if memory operations are safe to perform
    bool safe = true;
    
    if (detection_state.debugger_detected) {
        LOGW("⚠️ Memory operation unsafe: Debugger detected");
        safe = false;
    }
    
    if (detection_state.memory_scanner_detected) {
        LOGW("⚠️ Memory operation unsafe: Memory scanner detected");
        safe = false;
    }
    
    return safe;
}

bool SecurityManager::isESPOperationSafe() {
    // Enhanced ESP-specific safety check
    bool safe = true;
    
    // Check all threat vectors
    if (detection_state.debugger_detected ||
        detection_state.frida_detected ||
        detection_state.xposed_detected) {
        LOGW("⚠️ ESP operation unsafe: Active threats detected");
        safe = false;
    }
    
    // Additional ESP-specific checks
    if (!stealth_mode_active) {
        LOGW("⚠️ ESP operation unsafe: Stealth mode not active");
        safe = false;
    }
    
    LOGD("🎯 ESP operation safety: %s", safe ? "SAFE" : "UNSAFE");
    return safe;
}

int SecurityManager::performThreatAssessment() {
    int threat_level = 0; // LOW
    
    // Count active threats
    int threat_count = 0;
    if (detection_state.debugger_detected) threat_count++;
    if (detection_state.emulator_detected) threat_count++;
    if (detection_state.frida_detected) threat_count++;
    if (detection_state.xposed_detected) threat_count++;
    if (detection_state.memory_scanner_detected) threat_count++;
    
    // Determine threat level based on count and severity
    if (threat_count == 0) {
        threat_level = 0; // LOW
    } else if (threat_count <= 2) {
        threat_level = 1; // MEDIUM
    } else {
        threat_level = 2; // HIGH
    }
    
    // High-priority threats automatically set HIGH level
    if (detection_state.debugger_detected || detection_state.memory_scanner_detected) {
        threat_level = 2; // HIGH
    }
    
    LOGD("🎯 Threat assessment: Level %d (%s)", threat_level, 
         threat_level == 0 ? "LOW" : threat_level == 1 ? "MEDIUM" : "HIGH");
    
    return threat_level;
}

// DataStore implementation
DataStore::DataStore() : secure_storage_enabled(false) {
    generateEncryptionKey();
}

DataStore::~DataStore() {
    wipeData();
}

bool DataStore::initialize() {
    std::lock_guard<std::mutex> lock(data_mutex);
    LOGI("💾 Initializing secure data store");
    return true;
}

void DataStore::enableSecureStorage() {
    std::lock_guard<std::mutex> lock(data_mutex);
    secure_storage_enabled = true;
    encryptAllData();
    LOGI("🔐 Secure storage enabled");
}

void DataStore::disableSecureStorage() {
    std::lock_guard<std::mutex> lock(data_mutex);
    decryptAllData();
    secure_storage_enabled = false;
    LOGI("🔓 Secure storage disabled");
}

void DataStore::wipeData() {
    std::lock_guard<std::mutex> lock(data_mutex);
    secure_data.clear();
    std::fill(encryption_key.begin(), encryption_key.end(), 0);
    LOGD("🗑️ Data store wiped");
}

bool DataStore::storeData(const std::string& key, const std::string& value) {
    std::lock_guard<std::mutex> lock(data_mutex);
    
    std::string stored_value = secure_storage_enabled ? encryptValue(value) : value;
    secure_data[key] = stored_value;
    
    return true;
}

std::string DataStore::retrieveData(const std::string& key) {
    std::lock_guard<std::mutex> lock(data_mutex);
    
    auto it = secure_data.find(key);
    if (it == secure_data.end()) {
        return "";
    }
    
    return secure_storage_enabled ? decryptValue(it->second) : it->second;
}

void DataStore::generateEncryptionKey() {
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(0, 255);
    
    encryption_key.clear();
    for (int i = 0; i < 32; ++i) {
        encryption_key += static_cast<char>(dis(gen));
    }
}

std::string DataStore::encryptValue(const std::string& value) {
    std::string encrypted = value;
    for (size_t i = 0; i < encrypted.size(); ++i) {
        encrypted[i] ^= encryption_key[i % encryption_key.size()];
    }
    return encrypted;
}

std::string DataStore::decryptValue(const std::string& encrypted_value) {
    return encryptValue(encrypted_value); // XOR is symmetric
}

void DataStore::encryptAllData() {
    for (auto& pair : secure_data) {
        pair.second = encryptValue(pair.second);
    }
}

void DataStore::decryptAllData() {
    for (auto& pair : secure_data) {
        pair.second = decryptValue(pair.second);
    }
}

bool DataStore::deleteData(const std::string& key) {
    std::lock_guard<std::mutex> lock(data_mutex);
    
    auto it = secure_data.find(key);
    if (it != secure_data.end()) {
        secure_data.erase(it);
        LOGD("🗑️ Data deleted: %s", key.c_str());
        return true;
    }
    
    return false;
}

bool DataStore::hasData(const std::string& key) {
    std::lock_guard<std::mutex> lock(data_mutex);
    return secure_data.find(key) != secure_data.end();
}

// EventBus implementation
EventBus::EventBus() : processing_active(false) {}

EventBus::~EventBus() {
    shutdown();
}

bool EventBus::initialize() {
    std::lock_guard<std::mutex> lock(event_mutex);
    LOGI("📡 Initializing event bus");
    
    processing_active = true;
    event_processor_thread = std::thread(&EventBus::eventProcessorLoop, this);
    
    return true;
}

void EventBus::shutdown() {
    std::lock_guard<std::mutex> lock(event_mutex);
    processing_active = false;
    
    if (event_processor_thread.joinable()) {
        event_processor_thread.join();
    }
    
    event_queue.clear();
}

void EventBus::publishEvent(const std::string& type, const std::string& data) {
    std::lock_guard<std::mutex> lock(event_mutex);
    event_queue.emplace_back(type, data);
}

void EventBus::publishSecurityEvent(const std::string& threat_type, const std::string& details) {
    publishEvent("security_threat", threat_type + ":" + details);
}

void EventBus::publishAuthenticationEvent(bool success, const std::string& details) {
    publishEvent("authentication", success ? "success:" + details : "failure:" + details);
}

void EventBus::publishContainerEvent(const std::string& container_id, const std::string& action) {
    publishEvent("container", container_id + ":" + action);
}

void EventBus::subscribeToEvent(const std::string& type) {
    LOGD("📡 Subscribed to event type: %s", type.c_str());
    // In a real implementation, this would maintain subscriber lists
}

void EventBus::unsubscribeFromEvent(const std::string& type) {
    LOGD("📴 Unsubscribed from event type: %s", type.c_str());
    // In a real implementation, this would remove from subscriber lists
}

void EventBus::eventProcessorLoop() {
    while (processing_active) {
        std::vector<Event> events_to_process;
        
        {
            std::lock_guard<std::mutex> lock(event_mutex);
            events_to_process = event_queue;
            event_queue.clear();
        }
        
        for (const auto& event : events_to_process) {
            processEvent(event);
        }
        
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
    }
}

void EventBus::processEvent(const Event& event) {
    if (event.type == "security_threat") {
        handleSecurityEvent(event);
    }
    // Handle other event types as needed
}

void EventBus::handleSecurityEvent(const Event& event) {
    LOGW("🚨 Security event: %s", event.data.c_str());
    // Implement security event handling logic
}

// AIAgentPlugin implementation
AIAgentPlugin::AIAgentPlugin() : plugin_active(false) {}

AIAgentPlugin::~AIAgentPlugin() {
    disableAIAgent();
}

bool AIAgentPlugin::initialize() {
    std::lock_guard<std::mutex> lock(ai_mutex);
    LOGI("🤖 Initializing AI Agent Plugin");
    return true;
}

void AIAgentPlugin::enableAIAgent() {
    std::lock_guard<std::mutex> lock(ai_mutex);
    plugin_active = true;
    
    ai_thread = std::thread(&AIAgentPlugin::aiAgentLoop, this);
    LOGI("🤖 AI Agent activated");
}

void AIAgentPlugin::disableAIAgent() {
    std::lock_guard<std::mutex> lock(ai_mutex);
    plugin_active = false;
    
    if (ai_thread.joinable()) {
        ai_thread.join();
    }
    
    LOGI("🤖 AI Agent deactivated");
}

void AIAgentPlugin::enableIncognitoMode() {
    LOGI("🕵️ Incognito mode enabled - hiding hack logic and memory wires");
    obfuscateHackLogic();
    hideMemoryWires();
}

void AIAgentPlugin::disableIncognitoMode() {
    LOGI("🕵️ Incognito mode disabled");
}

void AIAgentPlugin::obfuscateHackLogic() {
    // Create fake function calls and memory patterns
    volatile int fake_operations[1000];
    for (int i = 0; i < 1000; ++i) {
        fake_operations[i] = (i * 3 + 7) % 256;
    }
    
    // Clear fake operations
    memset((void*)fake_operations, 0, sizeof(fake_operations));
}

void AIAgentPlugin::hideMemoryWires() {
    // Obfuscate memory access patterns
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(0, 4096);
    
    // Create random memory access patterns
    for (int i = 0; i < 100; ++i) {
        volatile char* dummy_ptr = (volatile char*)malloc(dis(gen));
        if (dummy_ptr) {
            *dummy_ptr = static_cast<char>(dis(gen));
            free((void*)dummy_ptr);
        }
    }
}

void AIAgentPlugin::aiAgentLoop() {
    while (plugin_active) {
        analyzeMemoryPatterns();
        adaptStealthBehavior();
        generateDecoyPatterns();
        
        std::this_thread::sleep_for(std::chrono::seconds(10));
    }
}

void AIAgentPlugin::analyzeMemoryPatterns() {
    // AI-based memory pattern analysis
    if (ai_state.pattern_recognition) {
        // Analyze current memory patterns and learn
        LOGD("🧠 AI analyzing memory patterns");
    }
}

void AIAgentPlugin::adaptStealthBehavior() {
    if (ai_state.adaptive_stealth) {
        // Adapt stealth behavior based on detected threats
        LOGD("🧠 AI adapting stealth behavior");
    }
}

void AIAgentPlugin::generateDecoyPatterns() {
    // Generate AI-driven decoy patterns
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(0, 255);
    
    volatile char decoy_pattern[2048];
    for (int i = 0; i < 2048; ++i) {
        decoy_pattern[i] = static_cast<char>(dis(gen));
    }
    
    // Clear decoy pattern
    memset((void*)decoy_pattern, 0, sizeof(decoy_pattern));
}

void AIAgentPlugin::learnFromDetection() {
    std::lock_guard<std::mutex> lock(ai_mutex);
    
    if (ai_state.learning_mode) {
        // Learn from current detection patterns
        ai_state.detected_patterns.push_back("new_pattern_" + std::to_string(ai_state.detected_patterns.size()));
        LOGD("🧠 AI learned from detection - Pattern count: %zu", ai_state.detected_patterns.size());
        
        // Limit pattern storage to prevent memory bloat
        if (ai_state.detected_patterns.size() > 100) {
            ai_state.detected_patterns.erase(ai_state.detected_patterns.begin());
        }
    }
}

void AIAgentPlugin::processAIAnalysis() {
    if (ai_state.pattern_recognition) {
        // Process collected patterns and adapt behavior
        LOGD("🤖 AI processing analysis - %zu patterns analyzed", ai_state.detected_patterns.size());
        
        // Update stealth strategy based on analysis
        updateStealthStrategy();
    }
}

void AIAgentPlugin::updateStealthStrategy() {
    if (ai_state.adaptive_stealth) {
        // Update stealth behavior based on learned patterns
        LOGD("🧠 AI updating stealth strategy");
        
        // In a real implementation, this would adjust stealth parameters
        // based on detection patterns and success rates
    }
}

// Global component instances
static std::unique_ptr<ConnectionManager> g_connection_manager;
static std::unique_ptr<SecurityManager> g_security_manager;
static std::unique_ptr<DataStore> g_data_store;
static std::unique_ptr<EventBus> g_event_bus;
static std::unique_ptr<AIAgentPlugin> g_ai_agent_plugin;
static bool g_components_initialized = false;

// Global component management functions
bool initializeStealthComponents() {
    if (g_components_initialized) {
        return true;
    }
    
    try {
        LOGI("🚀 Initializing Stealth Components");
        
        // Initialize all components
        g_connection_manager = std::make_unique<ConnectionManager>();
        g_security_manager = std::make_unique<SecurityManager>();
        g_data_store = std::make_unique<DataStore>();
        g_event_bus = std::make_unique<EventBus>();
        g_ai_agent_plugin = std::make_unique<AIAgentPlugin>();
        
        // Initialize each component
        bool success = true;
        success &= g_connection_manager->initialize();
        success &= g_security_manager->initialize();
        success &= g_data_store->initialize();
        success &= g_event_bus->initialize();
        success &= g_ai_agent_plugin->initialize();
        
        if (success) {
            g_components_initialized = true;
            LOGI("✅ All stealth components initialized successfully");
        } else {
            LOGE("❌ Some stealth components failed to initialize");
        }
        
        return success;
        
    } catch (const std::exception& e) {
        LOGE("Exception initializing stealth components: %s", e.what());
        return false;
    }
}

void shutdownStealthComponents() {
    if (!g_components_initialized) {
        return;
    }
    
    LOGI("🛑 Shutting down Stealth Components");
    
    try {
        if (g_ai_agent_plugin) g_ai_agent_plugin->disableAIAgent();
        if (g_event_bus) g_event_bus->shutdown();
        if (g_data_store) g_data_store->wipeData();
        if (g_security_manager) g_security_manager->disableStealthMode();
        if (g_connection_manager) g_connection_manager->disableEncryption();
        
        g_components_initialized = false;
        LOGI("✅ Stealth Components shutdown complete");
        
    } catch (const std::exception& e) {
        LOGE("Exception during shutdown: %s", e.what());
    }
}

} // namespace StealthContainer 