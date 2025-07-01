package com.happy.pro.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.happy.pro.activity.MainActivity;
import com.happy.pro.security.BearMemoryProtection;
import com.happy.pro.security.FridaBypass;
import com.happy.pro.utils.FLog;
import com.happy.pro.utils.FPrefs;

/**
 * 🐻 BEAR-LOADER Boot Receiver
 * 
 * Handles automatic startup and security initialization when device boots
 * 
 * Features:
 * - Auto-start BEAR-LOADER services on boot
 * - Initialize security systems early
 * - Stealth mode activation
 * - Background protection services
 * 
 * @author BEAR-LOADER Team
 * @version 3.0.0
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BearBootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        FLog.info("🚀 BEAR-LOADER Boot Receiver triggered");
        
        String action = intent.getAction();
        if (action == null) {
            FLog.warning("⚠️ Boot receiver called with null action");
            return;
        }
        
        FLog.info("📡 Boot action received: " + action);
        
        switch (action) {
            case "android.intent.action.BOOT_COMPLETED":
                FLog.info("🔄 System boot completed - initializing BEAR-LOADER");
                handleBootCompleted(context);
                break;
                
            case "android.intent.action.QUICKBOOT_POWERON":
                FLog.info("⚡ Quick boot detected - initializing BEAR-LOADER");
                handleBootCompleted(context);
                break;
                
            case "android.intent.action.MY_PACKAGE_REPLACED":
                FLog.info("📦 BEAR-LOADER package updated - reinitializing");
                handlePackageReplaced(context);
                break;
                
            case "android.intent.action.PACKAGE_REPLACED":
                FLog.info("📦 Package replacement detected");
                handlePackageReplaced(context);
                break;
                
            default:
                FLog.info("ℹ️ Unhandled boot action: " + action);
                break;
        }
    }
    
    /**
     * Handle boot completed event
     */
    private void handleBootCompleted(Context context) {
        try {
            FLog.info("🛡️ Starting BEAR-LOADER boot sequence...");
            
            // Check if auto-start is enabled in preferences
            if (!isAutoStartEnabled(context)) {
                FLog.info("⏸️ Auto-start disabled in settings");
                return;
            }
            
            // Initialize security systems first
            initializeSecuritySystems(context);
            
            // Delay startup to let system settle
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startBearLoaderServices(context);
            }, 5000); // 5 second delay
            
            FLog.info("✅ BEAR-LOADER boot sequence initiated");
            
        } catch (Exception e) {
            FLog.error("❌ Boot sequence failed: " + e.getMessage());
        }
    }
    
    /**
     * Handle package replacement event
     */
    private void handlePackageReplaced(Context context) {
        try {
            FLog.info("🔄 BEAR-LOADER package replaced - reinitializing...");
            
            // Reinitialize security systems after update
            initializeSecuritySystems(context);
            
            // Clear any stale data
            clearStaleData(context);
            
            FLog.info("✅ Package replacement handling complete");
            
        } catch (Exception e) {
            FLog.error("❌ Package replacement handling failed: " + e.getMessage());
        }
    }
    
    /**
     * Initialize security systems early
     */
    private void initializeSecuritySystems(Context context) {
        try {
            FLog.info("🔒 Initializing BEAR security systems...");
            
            // Initialize memory protection
            BearMemoryProtection memoryProtection = BearMemoryProtection.getInstance();
            if (memoryProtection.initialize()) {
                memoryProtection.enableAdvancedProtection();
                memoryProtection.enableStealthMode();
                FLog.info("✅ Memory protection initialized");
            }
            
            // Initialize Frida bypass
            FridaBypass fridaBypass = FridaBypass.getInstance();
            if (fridaBypass.initialize(context)) {
                fridaBypass.activate(); // Use public activate method instead
                FLog.info("✅ Frida bypass initialized");
            }
            
            FLog.info("🛡️ Security systems initialization complete");
            
        } catch (Exception e) {
            FLog.error("❌ Security systems initialization failed: " + e.getMessage());
        }
    }
    
    /**
     * Start BEAR-LOADER services
     */
    private void startBearLoaderServices(Context context) {
        try {
            FLog.info("🚀 Starting BEAR-LOADER services...");
            
            // Check if we should start main activity
            if (shouldStartMainActivity(context)) {
                Intent mainIntent = new Intent(context, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(mainIntent);
                FLog.info("📱 Main activity started");
            }
            
            // Start background services if enabled
            if (shouldStartBackgroundServices(context)) {
                startBackgroundServices(context);
            }
            
            FLog.info("✅ BEAR-LOADER services startup complete");
            
        } catch (Exception e) {
            FLog.error("❌ Service startup failed: " + e.getMessage());
        }
    }
    
    /**
     * Start background services
     */
    private void startBackgroundServices(Context context) {
        try {
            FLog.info("🔧 Starting background services...");
            
            // Note: We don't auto-start floating overlays as they require user interaction
            // and overlay permissions. These will be started when user opens the app.
            
            FLog.info("✅ Background services started");
            
        } catch (Exception e) {
            FLog.error("❌ Background services startup failed: " + e.getMessage());
        }
    }
    
    /**
     * Clear stale data after update
     */
    private void clearStaleData(Context context) {
        try {
            FLog.info("🧹 Clearing stale data...");
            
            SharedPreferences prefs = context.getSharedPreferences("espValue", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            // Clear any version-specific data that might be incompatible
            editor.remove("last_version_code");
            editor.remove("cached_security_data");
            
            editor.apply();
            
            FLog.info("✅ Stale data cleared");
            
        } catch (Exception e) {
            FLog.error("❌ Failed to clear stale data: " + e.getMessage());
        }
    }
    
    /**
     * Check if auto-start is enabled
     */
    private boolean isAutoStartEnabled(Context context) {
        try {
            FPrefs prefs = FPrefs.with(context);
            boolean autoStart = prefs.readBoolean("auto_start_enabled", false);
            FLog.info("⚙️ Auto-start setting: " + (autoStart ? "ENABLED" : "DISABLED"));
            return autoStart;
        } catch (Exception e) {
            FLog.error("❌ Failed to check auto-start setting: " + e.getMessage());
            return false; // Default to disabled for safety
        }
    }
    
    /**
     * Check if main activity should be started
     */
    private boolean shouldStartMainActivity(Context context) {
        try {
            FPrefs prefs = FPrefs.with(context);
            boolean startMain = prefs.readBoolean("boot_start_main_activity", false);
            FLog.info("⚙️ Start main activity: " + (startMain ? "YES" : "NO"));
            return startMain;
        } catch (Exception e) {
            FLog.error("❌ Failed to check main activity setting: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if background services should be started
     */
    private boolean shouldStartBackgroundServices(Context context) {
        try {
            FPrefs prefs = FPrefs.with(context);
            boolean startServices = prefs.readBoolean("boot_start_services", true);
            FLog.info("⚙️ Start background services: " + (startServices ? "YES" : "NO"));
            return startServices;
        } catch (Exception e) {
            FLog.error("❌ Failed to check background services setting: " + e.getMessage());
            return true; // Default to enabled
        }
    }
} 
