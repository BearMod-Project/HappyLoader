package com.happy.pro.core.container;

import java.util.HashMap;
import java.util.Map;

/**
 * 🔥 Container Configuration for Bear-Loader Advanced Container System
 * 
 * Comprehensive configuration system for isolated containers with:
 * ✅ Security Policy Configuration
 * ✅ Component Settings
 * ✅ Resource Limits
 * ✅ Isolation Levels
 * ✅ Hook Permissions
 */
public class ContainerConfig {
    
    // Configuration keys
    public static final String SECURITY_LEVEL = "security_level";
    public static final String HOOK_PERMISSIONS = "hook_permissions";
    public static final String DATA_ENCRYPTION = "data_encryption";
    public static final String EVENT_BUS_SIZE = "event_bus_size";
    public static final String PLUGIN_SUPPORT = "plugin_support";
    public static final String MEMORY_LIMIT = "memory_limit";
    public static final String STEALTH_MODE = "stealth_mode";
    public static final String ANTI_DETECTION = "anti_detection";
    
    // Security levels
    public static final int SECURITY_LEVEL_BASIC = 1;
    public static final int SECURITY_LEVEL_ENHANCED = 2;
    public static final int SECURITY_LEVEL_MILITARY = 3;
    
    private final Map<String, Object> configMap;
    private SecurityPolicy securityPolicy;
    private String containerId;
    private boolean sealed;
    
    public ContainerConfig() {
        this.configMap = new HashMap<>();
        this.sealed = false;
        
        // Set default values
        setDefaults();
    }
    
    public ContainerConfig(String containerId) {
        this();
        this.containerId = containerId;
    }
    
    /**
     * Set default configuration values
     */
    private void setDefaults() {
        configMap.put(SECURITY_LEVEL, SECURITY_LEVEL_ENHANCED);
        configMap.put(HOOK_PERMISSIONS, HookPermissions.SAFE_HOOKS_ONLY);
        configMap.put(DATA_ENCRYPTION, true);
        configMap.put(EVENT_BUS_SIZE, 1000);
        configMap.put(PLUGIN_SUPPORT, true);
        configMap.put(MEMORY_LIMIT, 64 * 1024 * 1024); // 64MB
        configMap.put(STEALTH_MODE, true);
        configMap.put(ANTI_DETECTION, true);
    }
    
    /**
     * Set configuration value
     */
    public ContainerConfig set(String key, Object value) {
        if (sealed) {
            throw new IllegalStateException("Configuration is sealed and cannot be modified");
        }
        configMap.put(key, value);
        return this;
    }
    
    /**
     * Get configuration value
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = configMap.get(key);
        return type.isInstance(value) ? (T) value : null;
    }
    
    /**
     * Get configuration value with default
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type, T defaultValue) {
        Object value = configMap.get(key);
        return type.isInstance(value) ? (T) value : defaultValue;
    }
    
    /**
     * Set security policy
     */
    public ContainerConfig setSecurityPolicy(SecurityPolicy policy) {
        if (sealed) {
            throw new IllegalStateException("Configuration is sealed and cannot be modified");
        }
        this.securityPolicy = policy;
        return this;
    }
    
    /**
     * Get security policy
     */
    public SecurityPolicy getSecurityPolicy() {
        return securityPolicy;
    }
    
    /**
     * Seal configuration to prevent further modifications
     */
    public ContainerConfig seal() {
        this.sealed = true;
        return this;
    }
    
    /**
     * Check if configuration is sealed
     */
    public boolean isSealed() {
        return sealed;
    }
    
    /**
     * Get container ID
     */
    public String getContainerId() {
        return containerId;
    }
    
    /**
     * Set container ID
     */
    public ContainerConfig setContainerId(String containerId) {
        if (sealed) {
            throw new IllegalStateException("Configuration is sealed and cannot be modified");
        }
        this.containerId = containerId;
        return this;
    }
    
    /**
     * Create a copy of this configuration
     */
    public ContainerConfig copy() {
        ContainerConfig copy = new ContainerConfig(containerId);
        copy.configMap.putAll(this.configMap);
        copy.securityPolicy = this.securityPolicy;
        return copy;
    }
    
    /**
     * Builder pattern for easy configuration
     */
    public static class Builder {
        private final ContainerConfig config;
        
        public Builder() {
            config = new ContainerConfig();
        }
        
        public Builder(String containerId) {
            config = new ContainerConfig(containerId);
        }
        
        public Builder securityLevel(int level) {
            config.set(SECURITY_LEVEL, level);
            return this;
        }
        
        public Builder hookPermissions(int permissions) {
            config.set(HOOK_PERMISSIONS, permissions);
            return this;
        }
        
        public Builder dataEncryption(boolean enabled) {
            config.set(DATA_ENCRYPTION, enabled);
            return this;
        }
        
        public Builder eventBusSize(int size) {
            config.set(EVENT_BUS_SIZE, size);
            return this;
        }
        
        public Builder pluginSupport(boolean enabled) {
            config.set(PLUGIN_SUPPORT, enabled);
            return this;
        }
        
        public Builder memoryLimit(long limit) {
            config.set(MEMORY_LIMIT, limit);
            return this;
        }
        
        public Builder stealthMode(boolean enabled) {
            config.set(STEALTH_MODE, enabled);
            return this;
        }
        
        public Builder antiDetection(boolean enabled) {
            config.set(ANTI_DETECTION, enabled);
            return this;
        }
        
        public Builder securityPolicy(SecurityPolicy policy) {
            config.setSecurityPolicy(policy);
            return this;
        }
        
        public ContainerConfig build() {
            return config.seal();
        }
    }
    
    /**
     * Hook permissions enumeration
     */
    public static class HookPermissions {
        public static final int NO_HOOKS = 0;
        public static final int SAFE_HOOKS_ONLY = 1;
        public static final int EXTENDED_HOOKS = 2;
        public static final int ALL_HOOKS = 3;
        public static final int DANGEROUS_HOOKS = 4;
    }
    
    /**
     * Predefined configurations for common use cases
     */
    public static class Presets {
        
        /**
         * Basic security configuration for development
         */
        public static ContainerConfig development() {
            return new Builder()
                .securityLevel(SECURITY_LEVEL_BASIC)
                .hookPermissions(HookPermissions.SAFE_HOOKS_ONLY)
                .dataEncryption(false)
                .stealthMode(false)
                .antiDetection(false)
                .build();
        }
        
        /**
         * Enhanced security configuration for production
         */
        public static ContainerConfig production() {
            return new Builder()
                .securityLevel(SECURITY_LEVEL_ENHANCED)
                .hookPermissions(HookPermissions.EXTENDED_HOOKS)
                .dataEncryption(true)
                .stealthMode(true)
                .antiDetection(true)
                .build();
        }
        
        /**
         * Military-grade security for high-risk environments
         */
        public static ContainerConfig military() {
            return new Builder()
                .securityLevel(SECURITY_LEVEL_MILITARY)
                .hookPermissions(HookPermissions.ALL_HOOKS)
                .dataEncryption(true)
                .stealthMode(true)
                .antiDetection(true)
                .memoryLimit(128 * 1024 * 1024) // 128MB
                .eventBusSize(2000)
                .build();
        }
        
        /**
         * Game hacking optimized configuration
         */
        public static ContainerConfig gameHacking() {
            return new Builder()
                .securityLevel(SECURITY_LEVEL_ENHANCED)
                .hookPermissions(HookPermissions.DANGEROUS_HOOKS)
                .dataEncryption(true)
                .stealthMode(true)
                .antiDetection(true)
                .memoryLimit(256 * 1024 * 1024) // 256MB for game assets
                .eventBusSize(5000) // High event throughput for games
                .build();
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ContainerConfig{");
        sb.append("containerId='").append(containerId).append('\'');
        sb.append(", sealed=").append(sealed);
        sb.append(", securityLevel=").append(get(SECURITY_LEVEL, Integer.class));
        sb.append(", hookPermissions=").append(get(HOOK_PERMISSIONS, Integer.class));
        sb.append(", stealthMode=").append(get(STEALTH_MODE, Boolean.class));
        sb.append(", antiDetection=").append(get(ANTI_DETECTION, Boolean.class));
        sb.append('}');
        return sb.toString();
    }
} 
