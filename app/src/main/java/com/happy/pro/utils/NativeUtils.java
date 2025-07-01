package com.happy.pro.utils;

import android.util.Log;

/**
 * Native utility class to handle native library loading
 * Provides fallback behavior when native libraries are not available
 */
public class NativeUtils {
    private static final String TAG = "NativeUtils";
    private static boolean nativeLoaded = false;
    
    static {
        try {
            System.loadLibrary("happy");
            nativeLoaded = true;
            FLog.info("✅ Native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            nativeLoaded = false;
            FLog.warning("⚠️ Native library not available - using fallback mode");
            FLog.info("💡 This is normal for development/testing");
        }
    }
    
    /**
     * Check if native library is loaded
     */
    public static boolean isNativeLoaded() {
        return nativeLoaded;
    }
    
    /**
     * Get native library version
     */
    public static String getNativeVersion() {
        if (nativeLoaded) {
            try {
                return nativeGetVersion();
            } catch (UnsatisfiedLinkError e) {
                // Fallback
            }
        }
        return "BEAR-LOADER 3.0.0 (Fallback)";
    }
    
    /**
     * Initialize BEAR for a specific package
     */
    public static boolean initializeBear(String packageName) {
        FLog.info("🎮 Initializing BEAR for: " + packageName);
        if (nativeLoaded) {
            try {
                return nativeInitializeBear(packageName);
            } catch (UnsatisfiedLinkError e) {
                FLog.error("Native init failed: " + e.getMessage());
            }
        }
        // Fallback - always return true in non-native mode
        FLog.info("📦 Using fallback initialization");
        return true;
    }
    
    // Native method declarations (will be implemented in C++)
    private static native String nativeGetVersion();
    private static native boolean nativeInitializeBear(String packageName);
} 
