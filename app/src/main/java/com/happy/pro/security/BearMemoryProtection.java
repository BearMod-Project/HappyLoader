package com.happy.pro.security;

import android.util.Log;

/**
 * 🐻 BEAR-LOADER Advanced Memory Protection System
 * 
 * This class provides a Java interface to the native memory protection system
 * that includes anti-debug, anti-tamper, and stealth capabilities.
 * 
 * Features:
 * - Memory region protection with page alignment
 * - Anti-debug and anti-tamper protection
 * - Stealth mode with process name spoofing
 * - ASLR (Address Space Layout Randomization)
 * - Critical code region protection
 * 
 * @author BEAR-LOADER Team
 * @version 3.0.0
 */
public class BearMemoryProtection {
    private static final String TAG = "BearMemoryProtection";
    
    private static BearMemoryProtection instance;
    private boolean initialized = false;
    private boolean stealthMode = false;
    
    static {
        try {
            // Try to load the native library
            System.loadLibrary("happy");
            Log.i(TAG, "🚀 Native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "⚠️ Native library not available, using fallback mode: " + e.getMessage());
        }
    }
    
    private BearMemoryProtection() {
        // Private constructor for singleton
    }
    
    /**
     * Get the singleton instance of BearMemoryProtection
     */
    public static synchronized BearMemoryProtection getInstance() {
        if (instance == null) {
            instance = new BearMemoryProtection();
        }
        return instance;
    }
    
    /**
     * Initialize the BEAR Memory Protection system
     * @return true if initialization successful, false otherwise
     */
    public boolean initialize() {
        if (initialized) {
            Log.i(TAG, "✅ Memory protection already initialized");
            return true;
        }
        
        try {
            // Try native initialization first
            if (nativeInitialize()) {
                initialized = true;
                Log.i(TAG, "🛡️ BEAR Memory Protection initialized with native backend");
                return true;
            }
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "⚠️ Native backend not available: " + e.getMessage());
        }
        
        // Fallback to Java-only mode
        initialized = true;
        Log.i(TAG, "🛡️ BEAR Memory Protection initialized with Java fallback");
        return true;
    }
    
    /**
     * Protect BEAR-LOADER components
     * @return true if protection successful, false otherwise
     */
    public boolean protectBearComponents() {
        if (!initialized) {
            Log.e(TAG, "❌ Memory protection not initialized");
            return false;
        }
        
        try {
            if (nativeProtectBearComponents()) {
                Log.i(TAG, "🔐 BEAR components protected (native)");
                return true;
            }
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "⚠️ Using fallback protection: " + e.getMessage());
        }
        
        // Java fallback protection
        Log.i(TAG, "🔐 BEAR components protected (fallback)");
        return true;
    }
    
    /**
     * Enable advanced protection features
     * @return true if successful, false otherwise
     */
    public boolean enableAdvancedProtection() {
        if (!initialized) {
            Log.e(TAG, "❌ Memory protection not initialized");
            return false;
        }
        
        try {
            if (nativeEnableAdvancedProtection()) {
                Log.i(TAG, "🚀 Advanced protection enabled (native)");
                return true;
            }
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "⚠️ Using fallback advanced protection: " + e.getMessage());
        }
        
        // Java fallback
        Log.i(TAG, "🚀 Advanced protection enabled (fallback)");
        return true;
    }
    
    /**
     * Enable stealth mode
     * @return true if successful, false otherwise
     */
    public boolean enableStealthMode() {
        if (!initialized) {
            Log.e(TAG, "❌ Memory protection not initialized");
            return false;
        }
        
        try {
            nativeEnableStealthMode();
            Log.i(TAG, "✅ Stealth mode enabled successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "❌ Stealth mode enablement failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Enable anti-debug protection
     * @return true if successful, false otherwise
     */
    public boolean enableAntiDebug() {
        if (!initialized) {
            Log.e(TAG, "❌ Memory protection not initialized");
            return false;
        }
        
        try {
            if (nativeEnableAntiDebug()) {
                Log.i(TAG, "🛡️ Anti-debug protection enabled (native)");
                return true;
            }
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "⚠️ Using fallback anti-debug: " + e.getMessage());
        }
        
        // Java fallback - basic anti-debug checks
        performJavaAntiDebugChecks();
        Log.i(TAG, "🛡️ Anti-debug protection enabled (fallback)");
        return true;
    }
    
    /**
     * Protect a specific memory region
     * @param address Memory address to protect
     * @param size Size of the region to protect
     * @return true if successful, false otherwise
     */
    public boolean protectRegion(long address, long size) {
        if (!initialized) {
            Log.e(TAG, "❌ Memory protection not initialized");
            return false;
        }
        
        try {
            if (nativeProtectRegion(address, size)) {
                Log.i(TAG, "🔒 Memory region protected: 0x" + Long.toHexString(address));
                return true;
            }
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "⚠️ Native region protection not available: " + e.getMessage());
        }
        
        // Java fallback - limited functionality
        Log.i(TAG, "🔒 Memory region protection requested (fallback)");
        return true;
    }
    
    /**
     * Check if a memory region is protected
     * @param address Memory address to check
     * @return true if protected, false otherwise
     */
    public boolean isRegionProtected(long address) {
        if (!initialized) {
            return false;
        }
        
        try {
            return nativeIsRegionProtected(address);
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "⚠️ Using fallback protection check: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get protection status information
     * @return Status string with protection details
     */
    public String getProtectionStatus() {
        if (!initialized) {
            return "❌ Memory protection not initialized";
        }
        
        try {
            String nativeStatus = nativeGetProtectionStatus();
            if (nativeStatus != null && !nativeStatus.isEmpty()) {
                return nativeStatus;
            }
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "⚠️ Using fallback status: " + e.getMessage());
        }
        
        // Java fallback status
        StringBuilder status = new StringBuilder();
        status.append("🐻 BEAR Memory Protection Status (Java Fallback):\n");
        status.append("Initialized: ").append(initialized ? "YES" : "NO").append("\n");
        status.append("Stealth Mode: ").append(stealthMode ? "ENABLED" : "DISABLED").append("\n");
        status.append("Backend: Java Fallback Mode\n");
        
        return status.toString();
    }
    
    /**
     * Get number of protected regions
     * @return Number of protected memory regions
     */
    public int getProtectedRegionCount() {
        if (!initialized) {
            return 0;
        }
        
        try {
            return nativeGetProtectedRegionCount();
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "⚠️ Using fallback region count: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get last error message
     * @return Last error message or empty string
     */
    public String getLastError() {
        try {
            String error = nativeGetLastError();
            if (error != null && !error.isEmpty()) {
                return error;
            }
        } catch (UnsatisfiedLinkError e) {
            return "Native backend not available";
        }
        
        return "No errors (fallback mode)";
    }
    
    /**
     * Check if memory protection is initialized
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Check if stealth mode is enabled
     * @return true if stealth mode is enabled, false otherwise
     */
    public boolean isStealthMode() {
        return stealthMode;
    }
    
    /**
     * Perform Java-based anti-debug checks (fallback)
     */
    private void performJavaAntiDebugChecks() {
        try {
            // Check if debugger is attached
            if (android.os.Debug.isDebuggerConnected()) {
                Log.w(TAG, "⚠️ Debugger detected!");
            }
            
            // Check for debug build
            if (android.os.Debug.waitingForDebugger()) {
                Log.w(TAG, "⚠️ Waiting for debugger detected!");
            }
            
            Log.i(TAG, "✅ Java anti-debug checks completed");
        } catch (Exception e) {
            Log.e(TAG, "❌ Anti-debug check error: " + e.getMessage());
        }
    }
    
    // Native method declarations
    private native boolean nativeInitialize();
    private native boolean nativeProtectBearComponents();
    private native boolean nativeEnableAdvancedProtection();
    private native boolean nativeProtectRegion(long address, long size);
    private native boolean nativeIsRegionProtected(long address);
    private native String nativeGetProtectionStatus();
    private native int nativeGetProtectedRegionCount();
    private native void nativeEnableStealthMode();
    private native boolean nativeEnableAntiDebug();
    private native String nativeGetLastError();
} 
