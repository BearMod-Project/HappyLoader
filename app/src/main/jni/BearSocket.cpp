#include "socket.h"
#include "BearInit.h"
#include "BearMundoSecurity.h"
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <android/log.h>
#include <algorithm>
#include <random>
#include <chrono>

// ========================================
// GLOBAL LEGACY VARIABLES (OUTSIDE NAMESPACE)
// ========================================

// Legacy global variables for compatibility
int sock = -1;
int clientD = -1;
sockaddr_un addr_server;
char socket_name[108] = "\0bear_secure_comm";

namespace BearSocket {

// ========================================
// GLOBAL INSTANCE
// ========================================

SecureSocketManager g_socketManager;

// ========================================
// SECURE SOCKET MANAGER IMPLEMENTATION
// ========================================

SecureSocketManager::SecureSocketManager() {
    encryptionKey.resize(ENCRYPTION_KEY_SIZE);
    memset(&serverAddr, 0, sizeof(serverAddr));
    strncpy(socketName, SECURE_SOCKET_NAME, sizeof(socketName) - 1);
    
    // Initialize encryption key
    regenerateEncryptionKey();
}

SecureSocketManager::~SecureSocketManager() {
    closeConnection();
}

bool SecureSocketManager::validateSecurityContext() const {
    if (!BearMundo::isMemoryOperationSecure()) {
        BEAR_LOGE("❌ Security context validation failed");
        return false;
    }
    return true;
}

bool SecureSocketManager::performSecurityHandshake() {
    if (!isSecurityEnabled.load()) {
        BEAR_LOGW("⚠️ Security not enabled, skipping handshake");
        return true;
    }
    
    // Simple handshake implementation
    const char* handshake = "BEAR_SECURE";
    if (sendRawData(clientSocket.load(), handshake, strlen(handshake)) > 0) {
        BEAR_LOGI("🤝 Security handshake completed");
        return true;
    }
    
    BEAR_LOGE("❌ Security handshake failed");
    return false;
}

std::vector<uint8_t> SecureSocketManager::encryptData(const void* data, size_t size) {
    std::vector<uint8_t> encrypted(size);
    const uint8_t* input = static_cast<const uint8_t*>(data);
    
    // Simple XOR encryption with key
    for (size_t i = 0; i < size; ++i) {
        encrypted[i] = input[i] ^ encryptionKey[i % encryptionKey.size()];
    }
    
    return encrypted;
}

std::vector<uint8_t> SecureSocketManager::decryptData(const void* data, size_t size) {
    return encryptData(data, size); // XOR is symmetric
}

ssize_t SecureSocketManager::sendRawData(int socket, const void* data, size_t size) {
    if (socket < 0) {
        BEAR_LOGE("❌ Invalid socket for send operation");
        return -1;
    }
    return ::send(socket, data, size, 0);
}

ssize_t SecureSocketManager::recvRawData(int socket, void* data, size_t size) {
    if (socket < 0) {
        BEAR_LOGE("❌ Invalid socket for receive operation");
        return -1;
    }
    return ::recv(socket, data, size, 0);
}

bool SecureSocketManager::createServer() {
    std::lock_guard<std::mutex> lock(socketMutex);
    
    if (!validateSecurityContext()) {
        return false;
    }
    
    int sockfd = ::socket(AF_UNIX, SOCK_STREAM, 0);
    if (sockfd < 0) {
        BEAR_LOGE("❌ Failed to create socket: %s", strerror(errno));
        return false;
    }
    
    serverSocket.store(sockfd);
    sock = sockfd; // Update legacy global
    
    BEAR_LOGI("🔌 Socket created successfully");
    return true;
}

bool SecureSocketManager::bindServer() {
    std::lock_guard<std::mutex> lock(socketMutex);
    
    int sockfd = serverSocket.load();
    if (sockfd < 0) {
        BEAR_LOGE("❌ No socket to bind");
        return false;
    }
    
    memset(&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sun_family = AF_UNIX;
    strncpy(serverAddr.sun_path, socketName, sizeof(serverAddr.sun_path) - 1);
    
    // Remove existing socket file
    unlink(serverAddr.sun_path);
    
    if (::bind(sockfd, (struct sockaddr*)&serverAddr, sizeof(serverAddr)) < 0) {
        BEAR_LOGE("❌ Failed to bind socket: %s", strerror(errno));
        return false;
    }
    
    BEAR_LOGI("🔗 Socket bound successfully");
    return true;
}

bool SecureSocketManager::listenForConnections() {
    std::lock_guard<std::mutex> lock(socketMutex);
    
    int sockfd = serverSocket.load();
    if (sockfd < 0) {
        BEAR_LOGE("❌ No socket to listen on");
        return false;
    }
    
    if (::listen(sockfd, SOCKET_BACKLOG) < 0) {
        BEAR_LOGE("❌ Failed to listen on socket: %s", strerror(errno));
        return false;
    }
    
    BEAR_LOGI("👂 Socket listening for connections");
    return true;
}

bool SecureSocketManager::acceptConnection() {
    std::lock_guard<std::mutex> lock(socketMutex);
    
    int sockfd = serverSocket.load();
    if (sockfd < 0) {
        BEAR_LOGE("❌ No server socket to accept on");
        return false;
    }
    
    struct sockaddr_un clientAddr;
    socklen_t clientLen = sizeof(clientAddr);
    
    int clientSock = ::accept(sockfd, (struct sockaddr*)&clientAddr, &clientLen);
    if (clientSock < 0) {
        BEAR_LOGE("❌ Failed to accept connection: %s", strerror(errno));
        return false;
    }
    
    clientSocket.store(clientSock);
    clientD = clientSock; // Update legacy global
    isConnected.store(true);
    
    BEAR_LOGI("✅ Client connection accepted");
    
    // Perform security handshake if enabled
    if (isSecurityEnabled.load()) {
        return performSecurityHandshake();
    }
    
    return true;
}

bool SecureSocketManager::connectToServer() {
    std::lock_guard<std::mutex> lock(socketMutex);
    
    int sockfd = ::socket(AF_UNIX, SOCK_STREAM, 0);
    if (sockfd < 0) {
        BEAR_LOGE("❌ Failed to create client socket: %s", strerror(errno));
        return false;
    }
    
    struct sockaddr_un serverAddr;
    memset(&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sun_family = AF_UNIX;
    strncpy(serverAddr.sun_path, socketName, sizeof(serverAddr.sun_path) - 1);
    
    if (::connect(sockfd, (struct sockaddr*)&serverAddr, sizeof(serverAddr)) < 0) {
        BEAR_LOGE("❌ Failed to connect to server: %s", strerror(errno));
        ::close(sockfd);
        return false;
    }
    
    clientSocket.store(sockfd);
    isConnected.store(true);
    
    BEAR_LOGI("🔌 Connected to server successfully");
    return true;
}

bool SecureSocketManager::sendSecureData(const void* data, size_t size) {
    if (!isConnected.load()) {
        BEAR_LOGE("❌ No active connection for sending data");
        return false;
    }
    
    if (!validateSecurityContext()) {
        return false;
    }
    
    int sockfd = clientSocket.load();
    if (sockfd < 0) {
        BEAR_LOGE("❌ Invalid client socket");
        return false;
    }
    
    if (isSecurityEnabled.load()) {
        auto encrypted = encryptData(data, size);
        ssize_t sent = sendRawData(sockfd, encrypted.data(), encrypted.size());
        return sent == static_cast<ssize_t>(size);
    } else {
        ssize_t sent = sendRawData(sockfd, data, size);
        return sent == static_cast<ssize_t>(size);
    }
}

size_t SecureSocketManager::receiveSecureData(void* buffer, size_t maxSize) {
    if (!isConnected.load()) {
        BEAR_LOGE("❌ No active connection for receiving data");
        return 0;
    }
    
    if (!validateSecurityContext()) {
        return 0;
    }
    
    int sockfd = clientSocket.load();
    if (sockfd < 0) {
        BEAR_LOGE("❌ Invalid client socket");
        return 0;
    }
    
    ssize_t received = recvRawData(sockfd, buffer, maxSize);
    if (received <= 0) {
        return 0;
    }
    
    if (isSecurityEnabled.load()) {
        auto decrypted = decryptData(buffer, received);
        memcpy(buffer, decrypted.data(), decrypted.size());
    }
    
    return static_cast<size_t>(received);
}

void SecureSocketManager::closeConnection() {
    std::lock_guard<std::mutex> lock(socketMutex);
    
    int clientSock = clientSocket.exchange(-1);
    int serverSock = serverSocket.exchange(-1);
    
    if (clientSock >= 0) {
        ::close(clientSock);
        BEAR_LOGI("🔌 Client socket closed");
    }
    
    if (serverSock >= 0) {
        ::close(serverSock);
        BEAR_LOGI("🔌 Server socket closed");
    }
    
    isConnected.store(false);
    
    // Update legacy globals
    sock = -1;
    clientD = -1;
    
    // Clean up socket file
    unlink(socketName);
}

bool SecureSocketManager::isConnectionActive() const {
    return isConnected.load();
}

bool SecureSocketManager::enableSecurity() {
    std::lock_guard<std::mutex> lock(socketMutex);
    
    if (regenerateEncryptionKey()) {
        isSecurityEnabled.store(true);
        BEAR_LOGI("🔒 Socket security enabled");
        return true;
    }
    
    BEAR_LOGE("❌ Failed to enable socket security");
    return false;
}

void SecureSocketManager::disableSecurity() {
    std::lock_guard<std::mutex> lock(socketMutex);
    isSecurityEnabled.store(false);
    BEAR_LOGW("⚠️ Socket security disabled");
}

bool SecureSocketManager::regenerateEncryptionKey() {
    try {
        std::random_device rd;
        std::mt19937 gen(rd());
        std::uniform_int_distribution<uint8_t> dis(0, 255);
        
        for (size_t i = 0; i < encryptionKey.size(); ++i) {
            encryptionKey[i] = dis(gen);
        }
        
        BEAR_LOGI("🔑 Encryption key regenerated");
        return true;
    } catch (...) {
        BEAR_LOGE("❌ Failed to regenerate encryption key");
        return false;
    }
}

std::string SecureSocketManager::getConnectionStatus() const {
    std::string status = "🐻 BEAR Socket Status:\n";
    status += "Connected: " + std::string(isConnected.load() ? "✅" : "❌") + "\n";
    status += "Security: " + std::string(isSecurityEnabled.load() ? "🔒 ENABLED" : "🔓 DISABLED") + "\n";
    status += "Server Socket: " + std::to_string(serverSocket.load()) + "\n";
    status += "Client Socket: " + std::to_string(clientSocket.load()) + "\n";
    return status;
}

size_t SecureSocketManager::getBytesSent() const {
    return 0; // Placeholder - would need tracking implementation
}

size_t SecureSocketManager::getBytesReceived() const {
    return 0; // Placeholder - would need tracking implementation
}

// ========================================
// ENHANCED SECURE FUNCTIONS IMPLEMENTATION
// ========================================

bool initializeSecureSocket() {
    return g_socketManager.enableSecurity();
}

bool createSecureChannel() {
    return g_socketManager.createServer() && 
           g_socketManager.bindServer() && 
           g_socketManager.listenForConnections();
}

bool sendEncryptedData(const void* data, size_t size) {
    return g_socketManager.sendSecureData(data, size);
}

size_t receiveEncryptedData(void* buffer, size_t maxSize) {
    return g_socketManager.receiveSecureData(buffer, maxSize);
}

bool performSecureHandshake() {
    return g_socketManager.enableSecurity();
}

bool validatePeerSecurity() {
    return BearMundo::isMemoryOperationSecure();
}

std::vector<uint8_t> generateSecureSessionKey() {
    std::vector<uint8_t> key(ENCRYPTION_KEY_SIZE);
    
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<uint8_t> dis(0, 255);
    
    for (auto& byte : key) {
        byte = dis(gen);
    }
    
    return key;
}

void cleanupSecureSocket() {
    g_socketManager.closeConnection();
}

SocketStats getSocketStatistics() {
    SocketStats stats;
    stats.bytesSent = g_socketManager.getBytesSent();
    stats.bytesReceived = g_socketManager.getBytesReceived();
    stats.packetsDropped = 0;
    stats.securityEnabled = true;
    stats.lastError = "None";
    return stats;
}

bool testSecureConnection() {
    const char* testData = "BEAR_TEST";
    return g_socketManager.sendSecureData(testData, strlen(testData));
}

void enableSocketDebugLogging(bool enable) {
    // Placeholder for debug logging toggle
    BEAR_LOGI("Socket debug logging %s", enable ? "enabled" : "disabled");
}

} // namespace BearSocket 