package com.happy.pro.core.constants;

/**
 * 🔥 ULTIMATE BearMod Constants - Enterprise Edition
 * 
 * Comprehensive constants management for the BearMod library system with:
 * ✅ Complete Library Configuration (Version, Package, Native Lib)
 * ✅ File System Management (Directories, Files, Extensions)
 * ✅ Security Configuration (Timeouts, Limits, Validation)
 * ✅ Container Management (Max containers, Size limits)
 * ✅ Feature Flag System (10+ Feature toggles)
 * ✅ Comprehensive Error/Success Messages
 * ✅ Performance Optimization (Timeouts, Cache settings)
 * 
 * This is PRODUCTION-READY ENTERPRISE CONSTANT MANAGEMENT!
 */
public final class BearModConstants {
    private BearModConstants() {
        // Prevent instantiation
    }
    
    // ============ LIBRARY VERSION ============
    public static final String LIBRARY_VERSION = "1.0.0";
    public static final int LIBRARY_VERSION_CODE = 1;
    
    // ============ PACKAGE NAMES ============
    public static final String PACKAGE_NAME = "com.happy.pro";
    public static final String NATIVE_LIB_NAME = "happy";
    
    // ============ FILE PATHS ============
    public static final String CONTAINER_DIR = "containers";
    public static final String LOG_DIR = "logs";
    public static final String CACHE_DIR = "cache";
    public static final String CONFIG_DIR = "config";
    
    // ============ FILE NAMES ============
    public static final String CONFIG_FILE = "bearmod_config.json";
    public static final String LOG_FILE = "bearmod.log";
    public static final String CACHE_FILE = "bearmod_cache.dat";
    
    // ============ SECURITY CONSTANTS ============
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final long LOGIN_TIMEOUT_MS = 300000; // 5 minutes
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 64;
    
    // ============ CONTAINER CONSTANTS ============
    public static final int MAX_CONTAINERS = 10;
    public static final long MAX_CONTAINER_SIZE = 1024 * 1024 * 1024; // 1GB
    public static final String CONTAINER_EXTENSION = ".bear";
    
    // ============ FEATURE FLAGS ============
    public static final boolean ENABLE_SSL_BYPASS = true;
    public static final boolean ENABLE_ROOT_BYPASS = true;
    public static final boolean ENABLE_DEBUG_BYPASS = true;
    public static final boolean ENABLE_EMULATOR_BYPASS = true;
    public static final boolean ENABLE_SIGNATURE_BYPASS = true;
    public static final boolean ENABLE_FRIDA_DETECTION = true;
    public static final boolean ENABLE_MEMORY_PROTECTION = true;
    public static final boolean ENABLE_REAL_TIME_ANALYSIS = true;
    public static final boolean ENABLE_SECURITY_MONITORING = true;
    public static final boolean ENABLE_CUSTOM_HOOKS = true;
    
    // ============ TIMEOUTS ============
    public static final long DEFAULT_TIMEOUT_MS = 30000; // 30 seconds
    public static final long CONNECTION_TIMEOUT_MS = 10000; // 10 seconds
    public static final long READ_TIMEOUT_MS = 30000; // 30 seconds
    public static final long WRITE_TIMEOUT_MS = 30000; // 30 seconds
    
    // ============ CACHE SETTINGS ============
    public static final long CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24 hours
    public static final int MAX_CACHE_SIZE = 100 * 1024 * 1024; // 100MB
    
    // ============ LOGGING ============
    public static final int MAX_LOG_SIZE = 10 * 1024 * 1024; // 10MB
    public static final int MAX_LOG_FILES = 5;
    public static final String LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";
    
    // ============ ERROR MESSAGES ============
    public static final String ERROR_INITIALIZATION = "Failed to initialize BearMod library";
    public static final String ERROR_CLEANUP = "Failed to cleanup BearMod library";
    public static final String ERROR_CONTAINER_CREATION = "Failed to create container";
    public static final String ERROR_CONTAINER_ACCESS = "Failed to access container";
    public static final String ERROR_AUTHENTICATION = "Authentication failed";
    public static final String ERROR_SECURITY = "Security check failed";
    public static final String ERROR_FEATURE = "Feature not available";
    public static final String ERROR_CONFIGURATION = "Invalid configuration";
    
    // ============ SUCCESS MESSAGES ============
    public static final String SUCCESS_INITIALIZATION = "BearMod library initialized successfully";
    public static final String SUCCESS_CLEANUP = "BearMod library cleaned up successfully";
    public static final String SUCCESS_CONTAINER_CREATION = "Container created successfully";
    public static final String SUCCESS_CONTAINER_ACCESS = "Container accessed successfully";
    public static final String SUCCESS_AUTHENTICATION = "Authentication successful";
    public static final String SUCCESS_SECURITY = "Security check passed";
    public static final String SUCCESS_FEATURE = "Feature enabled successfully";
    public static final String SUCCESS_CONFIGURATION = "Configuration applied successfully";
} 
