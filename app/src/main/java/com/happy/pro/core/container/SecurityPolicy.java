package com.happy.pro.core.container;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * 🛡️ Security Policy for Bear-Loader Container System
 * 
 * Defines security rules and restrictions for container operations
 */
public class SecurityPolicy {
    
    // Security levels
    public static final int SECURITY_LEVEL_NONE = 0;
    public static final int SECURITY_LEVEL_BASIC = 1;
    public static final int SECURITY_LEVEL_ENHANCED = 2;
    public static final int SECURITY_LEVEL_MILITARY = 3;
    
    // Permission types
    public static final String PERMISSION_MEMORY_READ = "memory.read";
    public static final String PERMISSION_MEMORY_WRITE = "memory.write";
    public static final String PERMISSION_HOOK_FUNCTIONS = "hook.functions";
    public static final String PERMISSION_NETWORK_ACCESS = "network.access";
    public static final String PERMISSION_FILE_SYSTEM = "filesystem.access";
    public static final String PERMISSION_ESP_OVERLAY = "esp.overlay";
    public static final String PERMISSION_AIMBOT = "aimbot.access";
    
    private final int securityLevel;
    private final Set<String> allowedPermissions;
    private final Set<String> deniedPermissions;
    private final Map<String, Object> policyData;
    private final boolean strictMode;
    
    private SecurityPolicy(Builder builder) {
        this.securityLevel = builder.securityLevel;
        this.allowedPermissions = new HashSet<>(builder.allowedPermissions);
        this.deniedPermissions = new HashSet<>(builder.deniedPermissions);
        this.policyData = new HashMap<>(builder.policyData);
        this.strictMode = builder.strictMode;
    }
    
    /**
     * Check if permission is allowed
     */
    public boolean isPermissionAllowed(String permission) {
        // Explicit deny takes precedence
        if (deniedPermissions.contains(permission)) {
            return false;
        }
        
        // Check explicit allow
        return allowedPermissions.contains(permission);
    }
    
    /**
     * Get security level
     */
    public int getSecurityLevel() {
        return securityLevel;
    }
    
    /**
     * Check if strict mode is enabled
     */
    public boolean isStrictMode() {
        return strictMode;
    }
    
    /**
     * Get policy data value
     */
    @SuppressWarnings("unchecked")
    public <T> T getPolicyData(String key, Class<T> type) {
        Object value = policyData.get(key);
        return type.isInstance(value) ? (T) value : null;
    }
    
    /**
     * Get all allowed permissions
     */
    public Set<String> getAllowedPermissions() {
        return new HashSet<>(allowedPermissions);
    }
    
    /**
     * Get all denied permissions
     */
    public Set<String> getDeniedPermissions() {
        return new HashSet<>(deniedPermissions);
    }
    
    /**
     * Builder for SecurityPolicy
     */
    public static class Builder {
        private int securityLevel = SECURITY_LEVEL_ENHANCED;
        private Set<String> allowedPermissions = new HashSet<>();
        private Set<String> deniedPermissions = new HashSet<>();
        private Map<String, Object> policyData = new HashMap<>();
        private boolean strictMode = true;
        
        public Builder setSecurityLevel(int level) {
            this.securityLevel = level;
            return this;
        }
        
        public Builder allowPermission(String permission) {
            this.allowedPermissions.add(permission);
            return this;
        }
        
        public Builder denyPermission(String permission) {
            this.deniedPermissions.add(permission);
            return this;
        }
        
        public Builder setPolicyData(String key, Object value) {
            this.policyData.put(key, value);
            return this;
        }
        
        public Builder setStrictMode(boolean strict) {
            this.strictMode = strict;
            return this;
        }
        
        public SecurityPolicy build() {
            return new SecurityPolicy(this);
        }
    }
    
    /**
     * Predefined security policies
     */
    public static class Presets {
        
        /**
         * Permissive policy for development
         */
        public static SecurityPolicy permissive() {
            return new Builder()
                .setSecurityLevel(SECURITY_LEVEL_BASIC)
                .allowPermission(PERMISSION_MEMORY_READ)
                .allowPermission(PERMISSION_MEMORY_WRITE)
                .allowPermission(PERMISSION_HOOK_FUNCTIONS)
                .allowPermission(PERMISSION_ESP_OVERLAY)
                .allowPermission(PERMISSION_AIMBOT)
                .setStrictMode(false)
                .build();
        }
        
        /**
         * Secure policy for production
         */
        public static SecurityPolicy secure() {
            return new Builder()
                .setSecurityLevel(SECURITY_LEVEL_ENHANCED)
                .allowPermission(PERMISSION_MEMORY_READ)
                .allowPermission(PERMISSION_ESP_OVERLAY)
                .denyPermission(PERMISSION_NETWORK_ACCESS)
                .setStrictMode(true)
                .build();
        }
        
        /**
         * Military-grade security policy
         */
        public static SecurityPolicy military() {
            return new Builder()
                .setSecurityLevel(SECURITY_LEVEL_MILITARY)
                .allowPermission(PERMISSION_MEMORY_READ)
                .denyPermission(PERMISSION_NETWORK_ACCESS)
                .denyPermission(PERMISSION_FILE_SYSTEM)
                .setStrictMode(true)
                .build();
        }
        
        /**
         * Game hacking optimized policy
         */
        public static SecurityPolicy gameHacking() {
            return new Builder()
                .setSecurityLevel(SECURITY_LEVEL_ENHANCED)
                .allowPermission(PERMISSION_MEMORY_READ)
                .allowPermission(PERMISSION_MEMORY_WRITE)
                .allowPermission(PERMISSION_HOOK_FUNCTIONS)
                .allowPermission(PERMISSION_ESP_OVERLAY)
                .allowPermission(PERMISSION_AIMBOT)
                .setStrictMode(false)
                .build();
        }
    }
    
    @Override
    public String toString() {
        return "SecurityPolicy{" +
                "securityLevel=" + securityLevel +
                ", allowedPermissions=" + allowedPermissions.size() +
                ", deniedPermissions=" + deniedPermissions.size() +
                ", strictMode=" + strictMode +
                '}';
    }
} 
