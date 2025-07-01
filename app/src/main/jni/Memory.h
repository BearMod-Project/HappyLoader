#ifndef BEAR_MEMORY_H
#define BEAR_MEMORY_H

#include "struct.h"
#include <string>

// ========================================
// GLOBAL VARIABLES
// ========================================

extern int height;
extern int width;
extern int pid;
extern int isBeta;
extern int nByte;

// Connection status
extern bool xConnected;
extern bool xServerConnection;
extern std::string g_Token;
extern std::string g_Auth;
extern bool signValid;
extern std::string ts;

// ========================================
// BEAR MEMORY MANAGER
// ========================================

namespace BearMemory {

/**
 * Initialize memory system
 */
bool initializeMemorySystem();

/**
 * Read memory from game process
 */
bool readGameMemory(uintptr_t address, void* buffer, size_t size);

/**
 * Write memory to game process
 */
bool writeGameMemory(uintptr_t address, const void* data, size_t size);

/**
 * Get game base address
 */
uintptr_t getGameBaseAddress();

/**
 * Get target game PID
 */
int getTargetGamePid();

/**
 * Check if memory system is ready
 */
bool isMemorySystemReady();

} // namespace BearMemory

// ========================================
// GAME MEMORY HACKING FUNCTIONS
// ========================================

/**
 * Apply recoil reduction hack
 */
bool applyRecoilHack(bool enable);

/**
 * Apply aimbot assistance
 */
bool applyAimbotHack(bool enable);

/**
 * Apply speed hack
 */
bool applySpeedHack(float speedMultiplier);

/**
 * Read player health
 */
float readPlayerHealth();

/**
 * Get player position
 */
Vec3 readPlayerPosition();

// ========================================
// AUTHENTICATION FUNCTIONS
// ========================================

/**
 * Validate license key
 */
bool validateLicenseKey(const std::string& licenseKey);

/**
 * Check authentication status
 */
bool isAuthenticated();

// ========================================
// UTILITY FUNCTIONS
// ========================================

/**
 * Get memory system status
 */
std::string getMemorySystemStatus();

/**
 * Cleanup memory system
 */
void cleanupMemorySystem();

#endif // BEAR_MEMORY_H 