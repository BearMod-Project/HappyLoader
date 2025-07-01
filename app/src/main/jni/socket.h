#ifndef BEAR_SOCKET_H
#define BEAR_SOCKET_H

#include "BearInit.h"
#include "BearMundoSecurity.h"
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <errno.h>
#include <netinet/in.h>
#include <atomic>
#include <mutex>
#include <vector>
#include <string>
#include <android/log.h>

// ========================================
// BEAR SECURE SOCKET SYSTEM
// ========================================

#define BEAR_LOG_TAG "BearSocket"
#define BEAR_LOGI(...) __android_log_print(ANDROID_LOG_INFO, BEAR_LOG_TAG, __VA_ARGS__)
#define BEAR_LOGW(...) __android_log_print(ANDROID_LOG_WARN, BEAR_LOG_TAG, __VA_ARGS__)
#define BEAR_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, BEAR_LOG_TAG, __VA_ARGS__)

namespace BearSocket {

// ========================================
// SECURE SOCKET CONFIGURATION
// ========================================

constexpr const char* SECURE_SOCKET_NAME = "\0bear_secure_comm";
constexpr int SOCKET_BACKLOG = 8;
constexpr size_t MAX_PACKET_SIZE = 8192;
constexpr size_t ENCRYPTION_KEY_SIZE = 32;

// ========================================
// SECURE SOCKET MANAGER CLASS
// ========================================

class SecureSocketManager {
private:
    std::atomic<int> serverSocket{-1};
    std::atomic<int> clientSocket{-1};
    std::atomic<bool> isConnected{false};
    std::atomic<bool> isSecurityEnabled{false};
    
    mutable std::mutex socketMutex;
    std::vector<uint8_t> encryptionKey;
    struct sockaddr_un serverAddr;
    char socketName[108];
    
    // Security validation
    bool validateSecurityContext() const;
    bool performSecurityHandshake();
    
    // Data encryption/decryption
    std::vector<uint8_t> encryptData(const void* data, size_t size);
    std::vector<uint8_t> decryptData(const void* data, size_t size);
    
    // Low-level socket operations
    ssize_t sendRawData(int socket, const void* data, size_t size);
    ssize_t recvRawData(int socket, void* data, size_t size);

public:
    SecureSocketManager();
    ~SecureSocketManager();
    
    // Server operations
    bool createServer();
    bool bindServer();
    bool listenForConnections();
    bool acceptConnection();
    
    // Client operations
    bool connectToServer();
    
    // Data transmission
    bool sendSecureData(const void* data, size_t size);
    size_t receiveSecureData(void* buffer, size_t maxSize);
    
    // Connection management
    void closeConnection();
    bool isConnectionActive() const;
    
    // Security operations
    bool enableSecurity();
    void disableSecurity();
    bool regenerateEncryptionKey();
    
    // Status and diagnostics
    std::string getConnectionStatus() const;
    size_t getBytesSent() const;
    size_t getBytesReceived() const;
};

// ========================================
// LEGACY COMPATIBILITY FUNCTIONS
// ========================================

// Global instance for legacy compatibility
extern SecureSocketManager g_socketManager;

// Legacy function wrappers (with security enhancements)
inline int Create() {
    if (!BearMundo::isMemoryOperationSecure()) {
        BEAR_LOGE("❌ Security check failed for socket creation");
        return 0;
    }
    return g_socketManager.createServer() ? 1 : 0;
}

inline void Close() {
    g_socketManager.closeConnection();
    BEAR_LOGI("🔌 Socket connection closed");
}

inline int Accept() {
    if (!BearInit::isSecurityEnabled.load()) {
        BEAR_LOGW("⚠️ Security not enabled for socket accept");
    }
    return g_socketManager.acceptConnection() ? 1 : 0;
}

inline int Bind() {
    return g_socketManager.bindServer() ? 1 : 0;
}

inline int Listen() {
    return g_socketManager.listenForConnections() ? 1 : 0;
}

inline int sendData(void* inData, size_t size) {
    if (!BearMundo::isMemoryOperationSecure()) {
        BEAR_LOGE("❌ Security check failed for data transmission");
        return -1;
    }
    return g_socketManager.sendSecureData(inData, size) ? static_cast<int>(size) : -1;
}

inline int send(void* inData, size_t size) {
    // Add random delay for anti-detection
    BearMundo::randomDelay();
    return sendData(inData, size);
}

inline int recvData(void* outData, size_t size) {
    if (!BearMundo::isMemoryOperationSecure()) {
        BEAR_LOGE("❌ Security check failed for data reception");
        return -1;
    }
    size_t received = g_socketManager.receiveSecureData(outData, size);
    return received > 0 ? static_cast<int>(received) : -1;
}

inline size_t receive(void* outData) {
    return g_socketManager.receiveSecureData(outData, MAX_PACKET_SIZE);
}

// ========================================
// ENHANCED SECURE FUNCTIONS
// ========================================

/**
 * Initialize secure socket system with BEAR security integration
 */
bool initializeSecureSocket();

/**
 * Create encrypted communication channel
 */
bool createSecureChannel();

/**
 * Send data with encryption and integrity checking
 */
bool sendEncryptedData(const void* data, size_t size);

/**
 * Receive and decrypt data with validation
 */
size_t receiveEncryptedData(void* buffer, size_t maxSize);

/**
 * Perform security handshake between client and server
 */
bool performSecureHandshake();

/**
 * Validate peer identity and security status
 */
bool validatePeerSecurity();

/**
 * Generate secure session key using BearMundo security
 */
std::vector<uint8_t> generateSecureSessionKey();

/**
 * Cleanup secure socket resources
 */
void cleanupSecureSocket();

// ========================================
// UTILITY FUNCTIONS
// ========================================

/**
 * Get socket connection statistics
 */
struct SocketStats {
    size_t bytesSent;
    size_t bytesReceived;
    size_t packetsDropped;
    bool securityEnabled;
    std::string lastError;
};

SocketStats getSocketStatistics();

/**
 * Test socket connectivity and security
 */
bool testSecureConnection();

/**
 * Enable debug logging for socket operations
 */
void enableSocketDebugLogging(bool enable);

} // namespace BearSocket

// ========================================
// GLOBAL WRAPPER FUNCTIONS FOR LEGACY COMPATIBILITY
// ========================================

// Global wrapper functions that call BearSocket namespace functions
inline int Create() {
    return BearSocket::Create();
}

inline void Close() {
    BearSocket::Close();
}

inline int Accept() {
    return BearSocket::Accept();
}

inline int Bind() {
    return BearSocket::Bind();
}

inline int Listen() {
    return BearSocket::Listen();
}

inline int send(void* inData, size_t size) {
    return BearSocket::send(inData, size);
}

inline size_t receive(void* outData) {
    return BearSocket::receive(outData);
}

// ========================================
// GLOBAL DECLARATIONS FOR LEGACY SUPPORT
// ========================================

// Legacy global variables (deprecated - use SecureSocketManager instead)
[[deprecated("Use BearSocket::SecureSocketManager instead")]]
extern int sock;

[[deprecated("Use BearSocket::SecureSocketManager instead")]]
extern int clientD;

[[deprecated("Use BearSocket::SecureSocketManager instead")]]
extern sockaddr_un addr_server;

[[deprecated("Use BearSocket::SecureSocketManager instead")]]
extern char socket_name[108];

#endif // BEAR_SOCKET_H
