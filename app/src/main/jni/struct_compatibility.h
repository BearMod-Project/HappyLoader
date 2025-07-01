#ifndef BEAR_LOADER_COMPATIBILITY_H
#define BEAR_LOADER_COMPATIBILITY_H

#include "struct.h"

/**
 * BEAR-LOADER 3.0.0 Compatibility Layer
 * 
 * This header provides compatibility between the old complex structure system
 * and the new simplified structure system for seamless transition.
 */

// ========================================
// GLOBAL INSTANCES
// ========================================

extern Options options;
extern Memory memory;  // Replaces otherFeature

// ========================================
// COMPATIBILITY MACROS FOR OPTIONS
// ========================================

// Map old complex Options fields to new simplified ones
#define aimT openState           // Use openState for aim toggle
#define aimBullet openState      // Use openState for bullet state  
#define aimingState openState    // Use openState for aiming state
#define aimingDist aimingRange   // Use aimingRange for distance
#define aimingSpeed aimingRange  // Use aimingRange for speed
#define touchSpeed aimingRange   // Use aimingRange for touch speed
#define recCompe priority        // Use priority for recoil compensation
#define recCompe1 priority       // Use priority for recoil comp 1
#define recCompe2 priority       // Use priority for recoil comp 2
#define touchSize aimingRange    // Use aimingRange for touch size
#define touchX aimingRange       // Use aimingRange for touch X
#define touchY aimingRange       // Use aimingRange for touch Y
#define Smoothing aimingRange    // Use aimingRange for smoothing

// Boolean options mapped to pour field
#define ignoreBot pour         // Use pour for ignore bot
#define ignoreAi pour          // Use pour for ignore AI
#define tracingStatus pour     // Use pour for tracing
#define InputInversion pour    // Use pour for input inversion

// Scope and custom settings (simplified)
#define customScope false                // Disabled in 3.0.0
static float recScope[9] = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};

// ========================================
// COMPATIBILITY MACROS FOR MEMORY (was OtherFeature)
// ========================================

// Direct mappings - these align perfectly with new Memory struct
#define otherFeature memory              // Direct replacement

// ========================================
// LEGACY FIELD EMULATION FUNCTIONS
// ========================================

/**
 * Emulate complex option setting behavior with simplified options
 */
static inline void setComplexOption(int code, int value) {
    switch (code) {
        case 1: // aimT
        case 2: // aimBullet  
        case 3: // aimingState
            options.openState = value;
            break;
        case 4: // aimingDist
        case 5: // aimingSpeed
        case 6: // touchSpeed
        case 7: // touchSize
        case 8: // touchX
        case 9: // touchY
            options.aimingRange = value;
            break;
        case 10: // recCompe
        case 11: // recCompe1
        case 12: // recCompe2
            options.priority = value;
            break;
        case 13: // Smoothing (float converted to int)
            options.aimingRange = (int)value;
            break;
        default:
            // Unknown option - use default mapping
            options.openState = value;
            break;
    }
}

/**
 * Emulate complex boolean option setting
 */
static inline void setComplexBoolOption(int code, bool value) {
    switch (code) {
        case 1: // ignoreBot
        case 2: // ignoreAi
        case 3: // tracingStatus
        case 4: // InputInversion
            options.pour = value;
            break;
        default:
            options.pour = value;
            break;
    }
}

/**
 * Emulate complex memory feature setting
 */
static inline void setMemoryFeature(int code, bool value) {
    switch (code) {
        case 1:
            memory.LessRecoil = value;
            break;
        case 2:
            memory.ZeroRecoil = value;
            break;
        case 3:
            memory.InstantHit = value;
            break;
        case 4:
            memory.FastShootInterval = value;
            break;
        case 5:
            memory.HitX = value;
            break;
        case 6:
            memory.SmallCrosshair = value;
            break;
        case 7:
            memory.FastSwitchWeapon = value;
            break;
        case 8:
            memory.NoShake = value;
            break;
        case 9:
            memory.WideView = value;
            break;
        case 10:
            memory.SpeedKnock = value;
            break;
        case 11:
            memory.Aimbot = value;
            break;
        case 12:
            memory.HeadShot = value;
            break;
        default:
            // Default to aimbot for unknown codes
            memory.Aimbot = value;
            break;
    }
}

// ========================================
// INITIALIZATION FUNCTIONS
// ========================================

/**
 * Initialize BEAR-LOADER 3.0.0 structures with safe defaults
 */
static inline void initializeBearStructures() {
    // Initialize Options with safe defaults
    options.aimbotmode = 0;
    options.openState = -1;     // Disabled by default
    options.priority = 0;
    options.pour = false;
    options.aimingRange = 100;  // Default range
    
    // Initialize Memory with all features disabled
    memory.LessRecoil = false;
    memory.ZeroRecoil = false;
    memory.InstantHit = false;
    memory.FastShootInterval = false;
    memory.HitX = false;
    memory.SmallCrosshair = false;
    memory.FastSwitchWeapon = false;
    memory.NoShake = false;
    memory.WideView = false;
    memory.SpeedKnock = false;
    memory.Aimbot = false;
    memory.HeadShot = false;
}

/**
 * Get current system status as string
 */
static inline const char* getBearSystemStatus() {
    static char status[256];
    snprintf(status, sizeof(status), 
        "BEAR-LOADER 3.0.0 | Options: [Mode:%d, State:%d, Range:%d] | Memory: [Aimbot:%s, Recoil:%s, ESP:%s]",
        options.aimbotmode, 
        options.openState, 
        options.aimingRange,
        memory.Aimbot ? "ON" : "OFF",
        memory.LessRecoil ? "ON" : "OFF", 
        memory.WideView ? "ON" : "OFF"
    );
    return status;
}

// ========================================
// COMPATIBILITY VALIDATION
// ========================================

/**
 * Validate structure integrity
 */
static inline bool validateBearStructures() {
    // Validate Options
    if (options.aimingRange < 0 || options.aimingRange > 1000) {
        return false;
    }
    
    if (options.aimbotmode < 0 || options.aimbotmode > 10) {
        return false;
    }
    
    if (options.priority < 0 || options.priority > 100) {
        return false;
    }
    
    // All validations passed
    return true;
}

#endif //BEAR_LOADER_COMPATIBILITY_H 