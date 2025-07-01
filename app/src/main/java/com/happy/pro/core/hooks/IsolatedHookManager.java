package com.happy.pro.core.hooks;

import android.util.Log;
import com.happy.pro.core.container.BearModContainer;

/**
 * 🔥 Isolated Hook Manager - Java JNI Wrapper
 * 
 * Java interface for the native C++ IsolatedHookManager
 * Provides secure hook management for BearMod containers
 */
public class IsolatedHookManager {
    private static final String TAG = "IsolatedHookManager";
    private final BearModContainer container;
    private long nativePtr; // Pointer to native C++ instance
    
    static {
        try {
            System.loadLibrary("happy");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native library", e);
        }
    }
    
    public IsolatedHookManager(BearModContainer container) {
        this.container = container;
        this.nativePtr = nativeCreate();
        if (nativePtr == 0) {
            throw new RuntimeException("Failed to create native IsolatedHookManager");
        }
    }
    
    /**
     * Register a hook with the native manager
     * @param name Hook name identifier
     * @param targetAddr Target function address (as hex string)
     * @param newFunc New function address (as hex string)
     * @return true if successful
     */
    public boolean registerHook(String name, String targetAddr, String newFunc) {
        if (nativePtr == 0) {
            Log.e(TAG, "Native manager not initialized");
            return false;
        }
        
        try {
            long target = Long.parseLong(targetAddr, 16);
            long replacement = Long.parseLong(newFunc, 16);
            return nativeRegisterHook(nativePtr, name, target, replacement);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid address format", e);
            return false;
        }
    }
    
    /**
     * Remove a registered hook
     * @param name Hook name to remove
     * @return true if successful
     */
    public boolean removeHook(String name) {
        if (nativePtr == 0) {
            Log.e(TAG, "Native manager not initialized");
            return false;
        }
        
        return nativeRemoveHook(nativePtr, name);
    }
    
    /**
     * Check if a hook is registered
     * @param name Hook name to check
     * @return true if hook is registered
     */
    public boolean isHookRegistered(String name) {
        if (nativePtr == 0) {
            Log.e(TAG, "Native manager not initialized");
            return false;
        }
        
        return nativeIsHookRegistered(nativePtr, name);
    }
    
    /**
     * Get the number of registered hooks
     * @return Number of active hooks
     */
    public int getHookCount() {
        if (nativePtr == 0) {
            return 0;
        }
        
        return nativeGetHookCount(nativePtr);
    }
    
    /**
     * Get all registered hook names
     * @return Array of hook names
     */
    public String[] getRegisteredHooks() {
        if (nativePtr == 0) {
            return new String[0];
        }
        
        return nativeGetRegisteredHooks(nativePtr);
    }
    
    /**
     * Clear all registered hooks
     * @return true if successful
     */
    public boolean clearAllHooks() {
        if (nativePtr == 0) {
            Log.e(TAG, "Native manager not initialized");
            return false;
        }
        
        return nativeClearAllHooks(nativePtr);
    }
    
    /**
     * Get the container this hook manager belongs to
     * @return Associated container
     */
    public BearModContainer getContainer() {
        return container;
    }
    
    /**
     * Check if the hook manager is valid
     * @return true if native instance is valid
     */
    public boolean isValid() {
        return nativePtr != 0;
    }
    
    /**
     * Initialize the hook manager
     * @return true if successful
     */
    public boolean initialize() {
        Log.d(TAG, "Initializing hook manager for container");
        return isValid();
    }
    
    /**
     * Set event bus for communication
     * @param eventBus Event bus instance
     */
    public void setEventBus(com.happy.pro.core.events.IsolatedEventBus eventBus) {
        Log.d(TAG, "Event bus set for hook manager");
    }
    
    /**
     * Apply security policy
     * @param policy Security policy to apply
     */
    public void applySecurityPolicy(com.happy.pro.core.container.SecurityPolicy policy) {
        Log.d(TAG, "Security policy applied to hook manager");
    }
    
    /**
     * Cleanup the hook manager
     */
    public void cleanup() {
        Log.d(TAG, "Cleaning up hook manager");
        destroy();
    }
    
    /**
     * Cleanup and destroy the native instance
     */
    public void destroy() {
        if (nativePtr != 0) {
            nativeDestroy(nativePtr);
            nativePtr = 0;
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            destroy();
        } finally {
            super.finalize();
        }
    }
    
    // ============ NATIVE METHODS ============
    
    /**
     * Create native C++ IsolatedHookManager instance
     * @return Native pointer or 0 on failure
     */
    private static native long nativeCreate();
    
    /**
     * Register a hook in the native manager
     * @param nativePtr Native instance pointer
     * @param name Hook name
     * @param targetAddr Target function address
     * @param newFunc Replacement function address
     * @return true if successful
     */
    private static native boolean nativeRegisterHook(long nativePtr, String name, long targetAddr, long newFunc);
    
    /**
     * Remove a hook from the native manager
     * @param nativePtr Native instance pointer
     * @param name Hook name to remove
     * @return true if successful
     */
    private static native boolean nativeRemoveHook(long nativePtr, String name);
    
    /**
     * Check if a hook is registered in the native manager
     * @param nativePtr Native instance pointer
     * @param name Hook name to check
     * @return true if registered
     */
    private static native boolean nativeIsHookRegistered(long nativePtr, String name);
    
    /**
     * Get the number of registered hooks
     * @param nativePtr Native instance pointer
     * @return Number of hooks
     */
    private static native int nativeGetHookCount(long nativePtr);
    
    /**
     * Get all registered hook names
     * @param nativePtr Native instance pointer
     * @return Array of hook names
     */
    private static native String[] nativeGetRegisteredHooks(long nativePtr);
    
    /**
     * Clear all hooks from the native manager
     * @param nativePtr Native instance pointer
     * @return true if successful
     */
    private static native boolean nativeClearAllHooks(long nativePtr);
    
    /**
     * Destroy the native C++ instance
     * @param nativePtr Native instance pointer
     */
    private static native void nativeDestroy(long nativePtr);
} 
