package com.happy.pro.libhelper;

import android.content.Context;
import android.os.Build;

import com.happy.pro.server.KeyAuthManager;
import com.happy.pro.utils.FLog;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Reusable Library Manager for BEAR-LOADER
 * Replaces the old Mundo kernel API approach with local, updateable libraries
 */
public class LibraryManager {
    private static final String TAG = "LibraryManager";
    private static LibraryManager instance;
    
    private Context context;
    private KeyAuthManager keyAuth;
    private Map<String, LibraryModule> loadedModules = new HashMap<>();
    
    public static class LibraryModule {
        public String name;
        public String path;
        public long handle; // For native library handle
        public boolean isLoaded;
        public Map<String, String> exports; // Exported functions
        
        public LibraryModule(String name, String path) {
            this.name = name;
            this.path = path;
            this.exports = new HashMap<>();
        }
    }
    
    private LibraryManager(Context context) {
        this.context = context.getApplicationContext();
        this.keyAuth = KeyAuthManager.getInstance(context);
    }
    
    public static synchronized LibraryManager getInstance(Context context) {
        if (instance == null) {
            instance = new LibraryManager(context);
        }
        return instance;
    }
    
    /**
     * Load a library module with automatic updates
     */
    public boolean loadLibrary(String libraryName, boolean checkUpdates) {
        try {
            FLog.info("📚 Loading library: " + libraryName);
            
            // Check if already loaded
            if (loadedModules.containsKey(libraryName)) {
                FLog.info("✅ Library already loaded: " + libraryName);
                return true;
            }
            
            // Check for updates if requested
            if (checkUpdates && keyAuth.isAuthenticated()) {
                FLog.info("🔄 Checking for updates...");
                keyAuth.updateLibrary(libraryName, null);
            }
            
            // Get library path
            File libFile = getLibraryFile(libraryName);
            if (!libFile.exists()) {
                FLog.error("❌ Library file not found: " + libraryName);
                return false;
            }
            
            // Load the library
            try {
                System.load(libFile.getAbsolutePath());
                
                LibraryModule module = new LibraryModule(libraryName, libFile.getAbsolutePath());
                module.isLoaded = true;
                
                // Initialize exports
                initializeExports(module);
                
                loadedModules.put(libraryName, module);
                FLog.info("✅ Library loaded successfully: " + libraryName);
                
                return true;
                
            } catch (UnsatisfiedLinkError e) {
                FLog.error("❌ Failed to load native library: " + e.getMessage());
                
                // Try fallback loading
                return loadLibraryFallback(libraryName);
            }
            
        } catch (Exception e) {
            FLog.error("❌ Library loading error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get library file path
     */
    private File getLibraryFile(String libraryName) {
        // First check KeyAuth managed libraries
        File keyAuthLib = keyAuth.getLibraryPath(libraryName);
        if (keyAuthLib.exists()) {
            return keyAuthLib;
        }
        
        // Check app native lib directory
        File nativeLib = new File(context.getApplicationInfo().nativeLibraryDir, libraryName);
        if (nativeLib.exists()) {
            return nativeLib;
        }
        
        // Check custom libs directory
        File customLib = new File(context.getFilesDir(), "libs/" + libraryName);
        return customLib;
    }
    
    /**
     * Fallback loading mechanism
     */
    private boolean loadLibraryFallback(String libraryName) {
        try {
            FLog.info("🔄 Trying fallback loading for: " + libraryName);
            
            // Remove .so extension if present
            String libName = libraryName.replace("lib", "").replace(".so", "");
            
            // Try standard system load
            System.loadLibrary(libName);
            
            LibraryModule module = new LibraryModule(libraryName, "system");
            module.isLoaded = true;
            loadedModules.put(libraryName, module);
            
            FLog.info("✅ Fallback loading successful");
            return true;
            
        } catch (UnsatisfiedLinkError e) {
            FLog.error("❌ Fallback loading failed: " + e.getMessage());
            
            // Create stub module
            LibraryModule stubModule = new LibraryModule(libraryName, "stub");
            stubModule.isLoaded = false;
            loadedModules.put(libraryName, stubModule);
            
            return false;
        }
    }
    
    /**
     * Initialize exported functions from library
     */
    private void initializeExports(LibraryModule module) {
        // Define known exports for each library
        switch (module.name) {
            case "libclient.so":
                module.exports.put("connect", "bear_connect");
                module.exports.put("authenticate", "bear_auth");
                module.exports.put("initialize", "bear_init");
                break;
                
            case "libmmkv.so":
                module.exports.put("initialize", "mmkv_init");
                module.exports.put("setString", "mmkv_set_string");
                module.exports.put("getString", "mmkv_get_string");
                break;
                
            case "libpubgm.so":
                module.exports.put("hook_main", "pubg_hook_main");
                module.exports.put("esp_init", "pubg_esp_init");
                module.exports.put("aimbot_init", "pubg_aimbot_init");
                break;
        }
    }
    
    /**
     * Call a function from loaded library
     */
    public native Object callLibraryFunction(String libraryName, String functionName, Object... args);
    
    /**
     * Get library info
     */
    public LibraryModule getLibraryInfo(String libraryName) {
        return loadedModules.get(libraryName);
    }
    
    /**
     * Check if library is loaded
     */
    public boolean isLibraryLoaded(String libraryName) {
        LibraryModule module = loadedModules.get(libraryName);
        return module != null && module.isLoaded;
    }
    
    /**
     * Unload a library
     */
    public void unloadLibrary(String libraryName) {
        LibraryModule module = loadedModules.remove(libraryName);
        if (module != null) {
            FLog.info("🗑️ Unloaded library: " + libraryName);
            // Native unloading would happen here if supported
        }
    }
    
    /**
     * Update all libraries
     */
    public void updateAllLibraries(KeyAuthManager.UpdateCallback callback) {
        if (!keyAuth.isAuthenticated()) {
            FLog.error("❌ Not authenticated for library updates");
            if (callback != null) callback.onError("Not authenticated");
            return;
        }
        
        keyAuth.updateAllLibraries(callback);
    }
    
    /**
     * Get recommended libraries for a game
     */
    public String[] getRecommendedLibraries(String packageName) {
        switch (packageName) {
            case "com.tencent.ig":
                return new String[]{"libclient.so", "libmmkv.so", "libpubgm.so"};
                
            case "com.pubg.krmobile":
                return new String[]{"libclient.so", "libmmkv.so", "libSdk.so"};
                
            case "com.pubg.imobile":
                return new String[]{"libclient.so", "libmmkv.so", "libbgmi.so"};
                
            default:
                return new String[]{"libclient.so", "libmmkv.so"};
        }
    }
    
    /**
     * Initialize required libraries for a game
     */
    public boolean initializeForGame(String packageName) {
        try {
            FLog.info("🎮 Initializing libraries for: " + packageName);
            
            String[] requiredLibs = getRecommendedLibraries(packageName);
            boolean allLoaded = true;
            
            for (String lib : requiredLibs) {
                if (!loadLibrary(lib, true)) {
                    FLog.error("❌ Failed to load required library: " + lib);
                    allLoaded = false;
                }
            }
            
            if (allLoaded) {
                FLog.info("✅ All libraries initialized for " + packageName);
            } else {
                FLog.warning("⚠️ Some libraries failed to load, features may be limited");
            }
            
            return allLoaded;
            
        } catch (Exception e) {
            FLog.error("❌ Game initialization error: " + e.getMessage());
            return false;
        }
    }
} 
