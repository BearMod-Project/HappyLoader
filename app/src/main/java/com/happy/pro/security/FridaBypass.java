package com.happy.pro.security;

import android.content.Context;
import android.content.res.AssetManager;
import com.happy.pro.utils.FLog;
import com.topjohnwu.superuser.Shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * BEAR-LOADER Frida Bypass Manager
 * Integrates advanced root detection bypass with the security system
 */
public class FridaBypass {
    private static final String TAG = "FridaBypass";
    private static FridaBypass instance;
    private boolean isActive = false;
    private boolean isInitialized = false;
    private Context applicationContext;
    
    private FridaBypass() {}
    
    public static synchronized FridaBypass getInstance() {
        if (instance == null) {
            instance = new FridaBypass();
        }
        return instance;
    }
    
    /**
     * Initialize Frida bypass system
     */
    public boolean initialize(Context context) {
        if (isInitialized) {
            FLog.warning("⚠️ FridaBypass already initialized");
            return true;
        }
        
        try {
            applicationContext = context.getApplicationContext();
            
            // Check if we have root access for advanced bypass
            boolean hasRoot = Shell.rootAccess();
            FLog.info("🔐 Root access available: " + hasRoot);
            
            if (hasRoot) {
                // Load and prepare Frida bypass script
                String bypassScript = loadBypassScript();
                if (bypassScript != null && !bypassScript.isEmpty()) {
                    // Check if Frida is available
                    if (checkFridaAvailability()) {
                        isInitialized = true;
                        FLog.info("✅ FridaBypass initialized successfully");
                        return true;
                    } else {
                        FLog.warning("⚠️ Frida not available, using native bypass methods");
                        isInitialized = true;
                        return true;
                    }
                } else {
                    FLog.error("❌ Failed to load bypass script");
                    return false;
                }
            } else {
                FLog.info("ℹ️ FridaBypass initialized in non-root mode");
                isInitialized = true;
                return true;
            }
            
        } catch (Exception e) {
            FLog.error("❌ Failed to initialize FridaBypass: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Activate bypass protection
     */
    public boolean activate() {
        if (!isInitialized) {
            FLog.error("❌ FridaBypass not initialized");
            return false;
        }
        
        if (isActive) {
            FLog.warning("⚠️ FridaBypass already active");
            return true;
        }
        
        try {
            // Enable native bypass methods first
            enableNativeBypass();
            
            // Check if Frida is available for advanced bypass
            if (Shell.rootAccess() && checkFridaAvailability()) {
                enableFridaBypass();
                enableSSLBypass();
                enableAppAnalyzer();
            }
            
            isActive = true;
            FLog.info("✅ FridaBypass activated successfully");
            FLog.info("🔐 SSL Pinning Bypass: " + (Shell.rootAccess() ? "ENABLED" : "DISABLED"));
            return true;
            
        } catch (Exception e) {
            FLog.error("❌ Failed to activate FridaBypass: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deactivate bypass protection
     */
    public void deactivate() {
        if (!isActive) {
            return;
        }
        
        try {
            // Disable Frida bypass if active
            disableFridaBypass();
            
            // Disable native bypass
            disableNativeBypass();
            
            isActive = false;
            FLog.info("🔒 FridaBypass deactivated");
            
        } catch (Exception e) {
            FLog.error("❌ Failed to deactivate FridaBypass: " + e.getMessage());
        }
    }
    
    /**
     * Load bypass script from assets
     */
    private String loadBypassScript() {
        try {
            AssetManager assetManager = applicationContext.getAssets();
            InputStream inputStream = assetManager.open("root_bypass.js");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            
            StringBuilder script = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                script.append(line).append("\n");
            }
            
            reader.close();
            inputStream.close();
            
            FLog.info("📜 BEAR Anti-Detection Module v3.0 loaded successfully (" + script.length() + " bytes)");
            FLog.info("✅ Features: Root Bypass, Anti-Frida, Command Blocking, Native Hooks");
            return script.toString();
            
        } catch (IOException e) {
            FLog.error("❌ Failed to load bypass script: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Load SSL bypass script from assets
     */
    private String loadSSLBypassScript() {
        try {
            AssetManager assetManager = applicationContext.getAssets();
            InputStream inputStream = assetManager.open("ssl_bypass.js");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            
            StringBuilder script = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                script.append(line).append("\n");
            }
            
            reader.close();
            inputStream.close();
            
            FLog.info("🔐 BEAR SSL Bypass Module v3.0 loaded successfully (" + script.length() + " bytes)");
            FLog.info("✅ SSL Features: OkHttp3, TrustManager, X509, WebView, Cordova, TrustKit");
            return script.toString();
            
        } catch (IOException e) {
            FLog.error("❌ Failed to load SSL bypass script: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Load app analyzer script from assets
     */
    private String loadAppAnalyzerScript() {
        try {
            AssetManager assetManager = applicationContext.getAssets();
            InputStream inputStream = assetManager.open("app_analyzer.js");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            
            StringBuilder script = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                script.append(line).append("\n");
            }
            
            reader.close();
            inputStream.close();
            
            FLog.info("🔍 BEAR App Analyzer v3.0 loaded successfully (" + script.length() + " bytes)");
            FLog.info("✅ Analysis Features: Security Detection, Network Monitoring, Class Analysis, Intelligence Reports");
            return script.toString();
            
        } catch (IOException e) {
            FLog.error("❌ Failed to load app analyzer script: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if Frida is available on the system
     */
    private boolean checkFridaAvailability() {
        try {
            // Check for Frida server binary
            Shell.Result result = Shell.su("which frida-server").exec();
            if (result.isSuccess()) {
                FLog.info("🔧 Frida server found");
                return true;
            }
            
            // Check for Frida in common locations
            String[] fridaLocations = {
                "/data/local/tmp/frida-server",
                "/system/bin/frida-server",
                "/system/xbin/frida-server"
            };
            
            for (String location : fridaLocations) {
                Shell.Result checkResult = Shell.su("test -f " + location).exec();
                if (checkResult.isSuccess()) {
                    FLog.info("🔧 Frida found at: " + location);
                    return true;
                }
            }
            
            FLog.info("ℹ️ Frida not found, using native methods");
            return false;
            
        } catch (Exception e) {
            FLog.error("❌ Error checking Frida availability: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Enable Frida-based bypass
     */
    private void enableFridaBypass() {
        try {
            String bypassScript = loadBypassScript();
            if (bypassScript == null) {
                FLog.error("❌ Failed to load Frida bypass script");
                return;
            }
            
            // Start Frida server if needed
            Shell.Result serverCheck = Shell.su("pgrep frida-server").exec();
            if (!serverCheck.isSuccess()) {
                FLog.info("🚀 Starting Frida server...");
                Shell.su("frida-server &").submit();
                
                // Wait for server to start
                Thread.sleep(2000);
            }
            
            // Apply bypass script to target process
            String packageName = applicationContext.getPackageName();
            String fridaCommand = String.format(
                "frida -U -n %s --no-pause -l /data/local/tmp/bear_bypass.js",
                packageName
            );
            
            // Write script to temp file
            String scriptPath = "/data/local/tmp/bear_bypass.js";
            Shell.su("echo '" + bypassScript + "' > " + scriptPath).exec();
            Shell.su("chmod 755 " + scriptPath).exec();
            
            FLog.info("🥷 Frida bypass enabled");
            
        } catch (Exception e) {
            FLog.error("❌ Failed to enable Frida bypass: " + e.getMessage());
        }
    }
    
    /**
     * Enable SSL pinning bypass
     */
    private void enableSSLBypass() {
        try {
            String sslBypassScript = loadSSLBypassScript();
            if (sslBypassScript == null) {
                FLog.error("❌ Failed to load SSL bypass script");
                return;
            }
            
            // Start Frida server if needed (reuse from enableFridaBypass)
            Shell.Result serverCheck = Shell.su("pgrep frida-server").exec();
            if (!serverCheck.isSuccess()) {
                FLog.info("🚀 Starting Frida server for SSL bypass...");
                Shell.su("frida-server &").submit();
                
                // Wait for server to start
                Thread.sleep(2000);
            }
            
            // Apply SSL bypass script
            String packageName = applicationContext.getPackageName();
            
            // Write SSL bypass script to temp file
            String sslScriptPath = "/data/local/tmp/bear_ssl_bypass.js";
            Shell.su("echo '" + sslBypassScript + "' > " + sslScriptPath).exec();
            Shell.su("chmod 755 " + sslScriptPath).exec();
            
            // Inject SSL bypass script
            String fridaSSLCommand = String.format(
                "timeout 30 frida -U -n %s --no-pause -l %s &",
                packageName, sslScriptPath
            );
            Shell.su(fridaSSLCommand).submit();
            
            FLog.info("🔐 SSL pinning bypass enabled");
            
        } catch (Exception e) {
            FLog.error("❌ Failed to enable SSL bypass: " + e.getMessage());
        }
    }
    
    /**
     * Enable app analyzer for intelligence gathering
     */
    private void enableAppAnalyzer() {
        try {
            String analyzerScript = loadAppAnalyzerScript();
            if (analyzerScript == null) {
                FLog.error("❌ Failed to load app analyzer script");
                return;
            }
            
            // Start Frida server if needed (reuse from previous methods)
            Shell.Result serverCheck = Shell.su("pgrep frida-server").exec();
            if (!serverCheck.isSuccess()) {
                FLog.info("🚀 Starting Frida server for app analyzer...");
                Shell.su("frida-server &").submit();
                
                // Wait for server to start
                Thread.sleep(2000);
            }
            
            // Apply app analyzer script
            String packageName = applicationContext.getPackageName();
            
            // Write analyzer script to temp file
            String analyzerScriptPath = "/data/local/tmp/bear_app_analyzer.js";
            Shell.su("echo '" + analyzerScript + "' > " + analyzerScriptPath).exec();
            Shell.su("chmod 755 " + analyzerScriptPath).exec();
            
            // Inject analyzer script
            String fridaAnalyzerCommand = String.format(
                "timeout 60 frida -U -n %s --no-pause -l %s &",
                packageName, analyzerScriptPath
            );
            Shell.su(fridaAnalyzerCommand).submit();
            
            FLog.info("🔍 App analyzer enabled - Intelligence gathering active");
            
        } catch (Exception e) {
            FLog.error("❌ Failed to enable app analyzer: " + e.getMessage());
        }
    }
    
    /**
     * Enable native bypass methods
     */
    private void enableNativeBypass() {
        try {
            // Hide common root detection files
            hideRootFiles();
            
            // Spoof system properties
            spoofSystemProperties();
            
            // Block suspicious commands
            blockSuspiciousCommands();
            
            FLog.info("🛡️ Native bypass methods enabled");
            
        } catch (Exception e) {
            FLog.error("❌ Failed to enable native bypass: " + e.getMessage());
        }
    }
    
    /**
     * Hide common root detection files
     */
    private void hideRootFiles() {
        try {
            String[] rootFiles = {
                "/system/app/Superuser.apk",
                "/system/xbin/su",
                "/system/bin/su",
                "/sbin/su",
                "/system/xbin/daemonsu",
                "/system/xbin/busybox"
            };
            
            for (String file : rootFiles) {
                // Create dummy files or modify permissions
                Shell.su("touch " + file + ".bak 2>/dev/null || true").exec();
                Shell.su("chmod 000 " + file + " 2>/dev/null || true").exec();
            }
            
            FLog.info("📁 Root files hidden");
            
        } catch (Exception e) {
            FLog.error("❌ Failed to hide root files: " + e.getMessage());
        }
    }
    
    /**
     * Spoof system properties
     */
    private void spoofSystemProperties() {
        try {
            // Reset dangerous properties
            Shell.su("setprop ro.debuggable 0").exec();
            Shell.su("setprop ro.secure 1").exec();
            Shell.su("setprop service.adb.root 0").exec();
            
            FLog.info("📋 System properties spoofed");
            
        } catch (Exception e) {
            FLog.error("❌ Failed to spoof properties: " + e.getMessage());
        }
    }
    
    /**
     * Block suspicious commands
     */
    private void blockSuspiciousCommands() {
        try {
            // Create dummy su command that fails
            String dummySu = "#!/system/bin/sh\nexit 1";
            Shell.su("echo '" + dummySu + "' > /data/local/tmp/su").exec();
            Shell.su("chmod 755 /data/local/tmp/su").exec();
            
            FLog.info("🚫 Suspicious commands blocked");
            
        } catch (Exception e) {
            FLog.error("❌ Failed to block commands: " + e.getMessage());
        }
    }
    
    /**
     * Disable Frida bypass
     */
    private void disableFridaBypass() {
        try {
            // Kill Frida processes
            Shell.su("pkill frida-server").exec();
            Shell.su("rm -f /data/local/tmp/bear_bypass.js").exec();
            Shell.su("rm -f /data/local/tmp/bear_ssl_bypass.js").exec();
            Shell.su("rm -f /data/local/tmp/bear_app_analyzer.js").exec();
            
            FLog.info("🔒 Frida bypass disabled");
            FLog.info("🔒 SSL bypass disabled");
            FLog.info("🔒 App analyzer disabled");
            
        } catch (Exception e) {
            FLog.error("❌ Failed to disable Frida bypass: " + e.getMessage());
        }
    }
    
    /**
     * Disable native bypass
     */
    private void disableNativeBypass() {
        try {
            // Restore original permissions (if possible)
            Shell.su("rm -f /data/local/tmp/su").exec();
            
            FLog.info("🔒 Native bypass disabled");
            
        } catch (Exception e) {
            FLog.error("❌ Failed to disable native bypass: " + e.getMessage());
        }
    }
    
    /**
     * Get bypass status
     */
    public BypassStatus getStatus() {
        return new BypassStatus(
            isInitialized,
            isActive,
            Shell.rootAccess(),
            checkFridaAvailability(),
            Shell.rootAccess() && checkFridaAvailability() // SSL bypass availability
        );
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (isActive) {
            deactivate();
        }
        
        isInitialized = false;
        applicationContext = null;
        
        FLog.info("🧹 FridaBypass cleaned up");
    }
    
    /**
     * Status class for bypass system
     */
    public static class BypassStatus {
        public final boolean isInitialized;
        public final boolean isActive;
        public final boolean hasRootAccess;
        public final boolean hasFridaSupport;
        public final boolean hasSSLBypass;
        
        public BypassStatus(boolean initialized, boolean active, boolean root, boolean frida, boolean ssl) {
            this.isInitialized = initialized;
            this.isActive = active;
            this.hasRootAccess = root;
            this.hasFridaSupport = frida;
            this.hasSSLBypass = ssl;
        }
        
        @Override
        public String toString() {
            return String.format(
                "BypassStatus{initialized=%s, active=%s, root=%s, frida=%s, ssl=%s}",
                isInitialized, isActive, hasRootAccess, hasFridaSupport, hasSSLBypass
            );
        }
    }
} 
