#ifndef BEAR_INIT_H
#define BEAR_INIT_H

#include "BearMundoSecurity.h"
#include <string>
#include <atomic>

// ========================================
// BEAR-LOADER CORE CONFIGURATION
// ========================================

namespace BearInit {

// Security flags
extern std::atomic<bool> isPremium;
extern std::atomic<bool> isSecurityEnabled;
extern std::atomic<bool> isStealthActive;

// Target configuration  
extern std::string targetPackage;
extern std::string targetLibrary;
extern std::string bearVersion;

// Game state
extern float healthBuffer[2];
extern std::atomic<float> currentHealth;
extern std::atomic<bool> gameActive;

// ========================================
// GLOBAL INITIALIZATION FLAGS
// ========================================

extern std::atomic<bool> isMemoryInitialized;
extern std::atomic<bool> isSocketInitialized;

// ========================================
// INITIALIZATION FUNCTIONS
// ========================================

/**
 * Initialize BEAR system
 */
bool initializeBearSystem();

/**
 * Initialize security components
 */
bool initializeSecurity();

/**
 * Initialize memory protection
 */
bool initializeMemoryProtection();

/**
 * Initialize socket communication
 */
bool initializeSocketSystem();

/**
 * Cleanup all BEAR components
 */
void cleanupBearSystem();

/**
 * Get initialization status
 */
std::string getInitializationStatus();

/**
 * Initialize BEAR-LOADER with security checks
 */
bool initializeBearLoader();

/**
 * Validate target application
 */
bool validateTarget(const std::string& packageName);

/**
 * Enable premium features (requires KeyAuth validation)
 */
bool enablePremiumFeatures();

/**
 * Setup security measures
 */
bool setupSecurityMeasures();

/**
 * Cleanup and shutdown
 */
void cleanup();

// ========================================
// SECURITY CONSTANTS
// ========================================

constexpr const char* DEFAULT_TARGET = "com.tencent.ig";
constexpr const char* BEAR_VERSION = "3.0.0-ENHANCED";
constexpr const char* SECURITY_TAG = "BEAR-SECURITY";

// Security levels
enum class BearSecurityLevel {
    BASIC = 0,
    ENHANCED = 1,
    PREMIUM = 2,
    STEALTH = 3
};

// Current security configuration
extern BearSecurityLevel currentSecurityLevel;

} // namespace BearInit

#endif // BEAR_INIT_H 