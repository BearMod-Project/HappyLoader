#ifndef BEAR_SUPPORT_H
#define BEAR_SUPPORT_H

#include "BearInit.h"
#include "BearMundoSecurity.h"
#include <sys/uio.h>
#include <unistd.h>
#include <dirent.h>
#include <cmath>
#include <string>
#include <vector>
#include <memory>
#include <atomic>
#include <mutex>

// ========================================
// BEAR MEMORY MANAGEMENT SYSTEM
// ========================================

namespace BearSupport {

// ========================================
// CONSTANTS AND TYPES
// ========================================

constexpr float PI = 3.141592653589793238f;
constexpr int SIZE = sizeof(uintptr_t);

// Process management
extern std::atomic<pid_t> targetPid;
extern std::atomic<uintptr_t> baseAddress;
extern std::mutex memoryMutex;

// ========================================
// CORE STRUCTURES
// ========================================

struct Vec2 {
    float X, Y;
    Vec2() : X(0), Y(0) {}
    Vec2(float x, float y) : X(x), Y(y) {}
};

struct Vec3 {
    float X, Y, Z;
    Vec3() : X(0), Y(0), Z(0) {}
    Vec3(float x, float y, float z) : X(x), Y(y), Z(z) {}
    
    float Distance(const Vec3& other) const {
        float dx = X - other.X;
        float dy = Y - other.Y;
        float dz = Z - other.Z;
        return std::sqrt(dx*dx + dy*dy + dz*dz);
    }
};

struct Vec4 {
    float X, Y, Z, W;
    Vec4() : X(0), Y(0), Z(0), W(0) {}
    Vec4(float x, float y, float z, float w) : X(x), Y(y), Z(z), W(w) {}
};

struct D2DVector {
    float X, Y;
    D2DVector() : X(0), Y(0) {}
    D2DVector(float x, float y) : X(x), Y(y) {}
};

struct D3DMatrix {
    union {
        struct {
            float _11, _12, _13, _14;
            float _21, _22, _23, _24;
            float _31, _32, _33, _34;
            float _41, _42, _43, _44;
        };
        float m[4][4];
    };
    
    D3DMatrix() {
        memset(this, 0, sizeof(D3DMatrix));
        _11 = _22 = _33 = _44 = 1.0f;
    }
};

struct FRotator {
    float Pitch, Yaw, Roll;
    FRotator() : Pitch(0), Yaw(0), Roll(0) {}
    FRotator(float pitch, float yaw, float roll) : Pitch(pitch), Yaw(yaw), Roll(roll) {}
};

struct FPOV {
    Vec3 Location;
    FRotator Rotation;
    float FOV;
    FPOV() : FOV(90.0f) {}
};

struct FCameraCacheEntry {
    FPOV POV;
    float TimeStamp;
    FCameraCacheEntry() : TimeStamp(0) {}
};

// ========================================
// SECURE MEMORY OPERATIONS
// ========================================

/**
 * Secure process memory read with validation
 */
class SecureMemoryManager {
private:
    static std::mutex s_mutex;
    static std::atomic<bool> s_initialized;
    
public:
    static bool initialize();
    static void cleanup();
    
    // Core memory operations
    static bool readMemory(void* address, void* buffer, size_t size);
    static bool writeMemory(void* address, const void* buffer, size_t size);
    
    // Template read operations
    template<typename T>
    static T read(uintptr_t address) {
        T result{};
        if (BearMundo::isMemoryOperationSecure() && readMemory((void*)address, &result, sizeof(T))) {
            return result;
        }
        return result;
    }
    
    // Template write operations
    template<typename T>
    static bool write(uintptr_t address, const T& value) {
        return BearMundo::isMemoryOperationSecure() && 
               writeMemory((void*)address, &value, sizeof(T));
    }
    
    // Specialized operations
    static float readFloat(uintptr_t address);
    static int readInt(uintptr_t address);
    static uintptr_t readPointer(uintptr_t address);
    static Vec3 readVec3(uintptr_t address);
    static std::string readString(uintptr_t address, size_t maxLength = 256);
    
    static bool writeFloat(uintptr_t address, float value);
    static bool writeInt(uintptr_t address, int value);
    static bool writePointer(uintptr_t address, uintptr_t value);
};

// ========================================
// PROCESS MANAGEMENT
// ========================================

/**
 * Secure process management with anti-detection
 */
class ProcessManager {
public:
    static pid_t findProcess(const std::string& packageName);
    static bool attachToProcess(pid_t pid);
    static bool isProcessRunning(const std::string& packageName);
    static uintptr_t getModuleBase(const std::string& moduleName = "libUE4.so");
    static bool validateProcess(pid_t pid);
    static void detachFromProcess();
    
private:
    static bool isValidPid(pid_t pid);
};

// ========================================
// MATH UTILITIES
// ========================================

class MathUtils {
public:
    // Distance calculations
    static float distance3D(const Vec3& a, const Vec3& b);
    static float distance2D(const Vec2& a, const Vec2& b);
    
    // Matrix operations
    static D3DMatrix createTransformMatrix(const Vec3& translation, const Vec3& scale, const Vec4& rotation);
    static Vec3 transformPoint(const D3DMatrix& matrix1, const D3DMatrix& matrix2);
    
    // World to screen projection
    static Vec3 worldToScreen(const D3DMatrix& viewMatrix, const Vec3& worldPos, int screenWidth, int screenHeight);
    static Vec2 worldToScreen2D(const D3DMatrix& viewMatrix, const Vec3& worldPos, int screenWidth, int screenHeight);
    
    // Angle calculations
    static D2DVector calculateAimAngles(const Vec3& from, const Vec3& to, const Vec3& velocity = Vec3(), float bulletSpeed = 88000.0f);
    static Vec3 calculateMousePosition(const Vec3& targetPos, const Vec3& playerPos);
    
    // Radar/minimap utilities
    static Vec2 worldToRadar(const FCameraCacheEntry& camera, const Vec3& worldPos, const Vec2& radarCenter, float radarSize, bool circular = false);
    
    // Validation
    static bool isValid32BitAddress(uintptr_t addr);
    static bool isValid64BitAddress(uintptr_t addr);
    static bool isValidItemId(int id);
};

// ========================================
// SECURITY UTILITIES
// ========================================

class SecurityUtils {
public:
    // Memory protection
    static bool protectMemoryRegion(void* address, size_t size);
    static bool unprotectMemoryRegion(void* address, size_t size);
    
    // Anti-detection
    static void randomizeDelay();
    static void createDecoyOperations();
    static std::string obfuscateString(const std::string& input);
    
    // Validation
    static bool validateMemoryAccess(uintptr_t address, size_t size);
    static bool isSecureEnvironment();
};

// ========================================
// UTILITY FUNCTIONS
// ========================================

class Utils {
public:
    // String operations
    static std::string executeCommand(const std::string& command);
    static std::vector<std::string> split(const std::string& str, char delimiter);
    
    // File operations
    static bool dumpMemory(uintptr_t address, size_t size, const std::string& filename);
    static bool fileExists(const std::string& path);
    
    // Data conversion
    static std::string bytesToHex(const void* data, size_t size);
    static std::vector<uint8_t> hexToBytes(const std::string& hex);
};

// ========================================
// LEGACY COMPATIBILITY (DEPRECATED)
// ========================================

// Note: These functions are maintained for compatibility but deprecated
// Use SecureMemoryManager instead

[[deprecated("Use SecureMemoryManager::read<float> instead")]]
float getFloat(uintptr_t address);

[[deprecated("Use SecureMemoryManager::read<int> instead")]]
int getInt(uintptr_t address);

[[deprecated("Use SecureMemoryManager::read<uintptr_t> instead")]]
uintptr_t getAddress(uintptr_t address);

[[deprecated("Use ProcessManager::findProcess instead")]]
pid_t getPid(const char* name);

[[deprecated("Use ProcessManager::getModuleBase instead")]]
uintptr_t getBase();

} // namespace BearSupport

#endif // BEAR_SUPPORT_H 