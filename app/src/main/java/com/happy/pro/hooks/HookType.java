package com.happy.pro.hooks;

/**
 * Enumeration of different hook types supported by BEAR-LOADER
 */
public enum HookType {
    /**
     * ESP (Extra Sensory Perception) hooks for player visualization
     */
    ESP("ESP", "Extra Sensory Perception hooks"),
    
    /**
     * Aimbot hooks for automatic targeting
     */
    AIMBOT("Aimbot", "Automatic targeting system"),
    
    /**
     * Memory manipulation hooks
     */
    MEMORY("Memory", "Memory manipulation and patching"),
    
    /**
     * Security bypass hooks (Frida, Xposed, etc.)
     */
    SECURITY_BYPASS("Security", "Anti-detection and bypass systems"),
    
    /**
     * Function hooking for API interception
     */
    FUNCTION_HOOK("Function", "Direct function interception"),
    
    /**
     * System call hooks
     */
    SYSCALL("SysCall", "System call interception"),
    
    /**
     * Library loading hooks
     */
    LIBRARY_HOOK("Library", "Dynamic library loading hooks"),
    
    /**
     * Anti-cheat bypass hooks
     */
    ANTICHEAT_BYPASS("AntiCheat", "Anti-cheat system bypass"),
    
    /**
     * Network packet hooks
     */
    NETWORK("Network", "Network packet interception"),
    
    /**
     * Game engine hooks (Unity, Unreal, etc.)
     */
    GAME_ENGINE("Engine", "Game engine specific hooks"),
    
    /**
     * Unknown or custom hook type
     */
    UNKNOWN("Unknown", "Unknown or custom hook type");
    
    private final String displayName;
    private final String description;
    
    /**
     * Constructor for HookType enum
     * 
     * @param displayName Human-readable name
     * @param description Description of the hook type
     */
    HookType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Get the display name of the hook type
     * 
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the description of the hook type
     * 
     * @return Description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get hook type from string name
     * 
     * @param name Name to match
     * @return Matching HookType or UNKNOWN if not found
     */
    public static HookType fromString(String name) {
        if (name == null || name.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        for (HookType type : values()) {
            if (type.name().equalsIgnoreCase(name.trim()) || 
                type.displayName.equalsIgnoreCase(name.trim())) {
                return type;
            }
        }
        
        return UNKNOWN;
    }
    
    /**
     * Check if this hook type is security-related
     * 
     * @return True if security-related
     */
    public boolean isSecurityRelated() {
        return this == SECURITY_BYPASS || this == ANTICHEAT_BYPASS || this == SYSCALL;
    }
    
    /**
     * Check if this hook type is game-related
     * 
     * @return True if game-related
     */
    public boolean isGameRelated() {
        return this == ESP || this == AIMBOT || this == MEMORY || 
               this == GAME_ENGINE || this == NETWORK;
    }
    
    @Override
    public String toString() {
        return displayName + " (" + description + ")";
    }
} 
