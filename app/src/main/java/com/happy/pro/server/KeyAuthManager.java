package com.happy.pro.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.happy.pro.utils.FLog;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * KeyAuth API Manager for BEAR-LOADER
 * Handles authentication, library updates, and license management
 */
public class KeyAuthManager {
    private static final String TAG = "KeyAuthManager";
    private static KeyAuthManager instance;
    
    // KeyAuth Configuration
    private String appName;
    private String ownerID;
    private String appSecret;
    private String version;
    private String apiUrl = "https://keyauth.win/api/1.2/";
    
    // Session Management
    private String sessionID;
    private boolean initialized = false;
    private Context context;
    private SharedPreferences prefs;
    
    // Library Management
    private Map<String, LibraryInfo> availableLibraries = new HashMap<>();
    
    public static class LibraryInfo {
        public String name;
        public String version;
        public String hash;
        public String downloadUrl;
        public long size;
        public boolean isRequired;
        
        public LibraryInfo(String name, String version, String hash) {
            this.name = name;
            this.version = version;
            this.hash = hash;
        }
    }
    
    private KeyAuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences("keyauth_prefs", Context.MODE_PRIVATE);
    }
    
    public static synchronized KeyAuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new KeyAuthManager(context);
        }
        return instance;
    }
    
    /**
     * Initialize KeyAuth connection
     */
    public boolean initialize(String appName, String ownerID, String appSecret, String version) {
        try {
            this.appName = appName;
            this.ownerID = ownerID;
            this.appSecret = appSecret;
            this.version = version;
            
            FLog.info("🔐 Initializing KeyAuth for app: " + appName);
            
            // Create init request
            JSONObject initData = new JSONObject();
            initData.put("type", "init");
            initData.put("name", appName);
            initData.put("ownerid", ownerID);
            initData.put("secret", appSecret);
            initData.put("version", version);
            
            JSONObject response = makeRequest(initData);
            
            if (response != null && response.optBoolean("success", false)) {
                sessionID = response.optString("sessionid", "");
                initialized = true;
                
                FLog.info("✅ KeyAuth initialized successfully");
                FLog.info("📱 Session ID: " + sessionID.substring(0, 8) + "...");
                
                // Store session info
                prefs.edit()
                    .putString("session_id", sessionID)
                    .putLong("session_time", System.currentTimeMillis())
                    .apply();
                
                return true;
            }
            
            FLog.error("❌ KeyAuth init failed: " + response.optString("message", "Unknown error"));
            return false;
            
        } catch (Exception e) {
            FLog.error("❌ KeyAuth initialization error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Authenticate user with license key
     */
    public boolean login(String key) {
        if (!initialized) {
            FLog.error("❌ KeyAuth not initialized");
            return false;
        }
        
        try {
            FLog.info("🔑 Authenticating with license key...");
            
            JSONObject loginData = new JSONObject();
            loginData.put("type", "license");
            loginData.put("key", key);
            loginData.put("sessionid", sessionID);
            loginData.put("name", appName);
            loginData.put("ownerid", ownerID);
            
            JSONObject response = makeRequest(loginData);
            
            if (response != null && response.optBoolean("success", false)) {
                FLog.info("✅ Authentication successful");
                
                // Store user info
                JSONObject info = response.optJSONObject("info");
                if (info != null) {
                    prefs.edit()
                        .putString("username", info.optString("username", ""))
                        .putString("subscription", info.optString("subscriptions", ""))
                        .putString("expiry", info.optString("expiry", ""))
                        .putBoolean("authenticated", true)
                        .apply();
                }
                
                // Fetch available libraries
                fetchLibraryList();
                
                return true;
            }
            
            FLog.error("❌ Authentication failed: " + response.optString("message", "Invalid key"));
            return false;
            
        } catch (Exception e) {
            FLog.error("❌ Login error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Fetch list of available libraries from KeyAuth
     */
    private void fetchLibraryList() {
        try {
            FLog.info("📚 Fetching available libraries...");
            
            // In a real implementation, this would fetch from KeyAuth files
            // For now, we'll define the expected libraries
            availableLibraries.clear();
            
            // Core libraries
            availableLibraries.put("libclient.so", new LibraryInfo(
                "libclient.so", 
                "3.0.0", 
                calculateFileHash(new File(context.getFilesDir(), "libs/libclient.so"))
            ));
            
            availableLibraries.put("libmmkv.so", new LibraryInfo(
                "libmmkv.so", 
                "1.2.0", 
                calculateFileHash(new File(context.getFilesDir(), "libs/libmmkv.so"))
            ));
            
            // Game-specific libraries
            availableLibraries.put("libpubgm.so", new LibraryInfo(
                "libpubgm.so", 
                "2.9.0", 
                "PUBG_GLOBAL_LOADER"
            ));
            
            availableLibraries.put("libSdk.so", new LibraryInfo(
                "libSdk.so", 
                "2.9.0", 
                "PUBG_KOREA_LOADER"
            ));
            
            availableLibraries.put("libbgmi.so", new LibraryInfo(
                "libbgmi.so", 
                "2.9.0", 
                "PUBG_INDIA_LOADER"
            ));
            
            FLog.info("📚 Found " + availableLibraries.size() + " libraries");
            
        } catch (Exception e) {
            FLog.error("❌ Failed to fetch library list: " + e.getMessage());
        }
    }
    
    /**
     * Download and update a specific library
     */
    public boolean updateLibrary(String libraryName, UpdateCallback callback) {
        if (!initialized || !isAuthenticated()) {
            FLog.error("❌ Not authenticated");
            return false;
        }
        
        try {
            LibraryInfo libInfo = availableLibraries.get(libraryName);
            if (libInfo == null) {
                FLog.error("❌ Library not found: " + libraryName);
                return false;
            }
            
            FLog.info("📥 Downloading library: " + libraryName + " v" + libInfo.version);
            
            // Create library directory
            File libDir = new File(context.getFilesDir(), "libs");
            if (!libDir.exists()) {
                libDir.mkdirs();
            }
            
            File libFile = new File(libDir, libraryName);
            
            // Check if update needed
            if (libFile.exists()) {
                String currentHash = calculateFileHash(libFile);
                if (currentHash.equals(libInfo.hash)) {
                    FLog.info("✅ Library already up to date");
                    if (callback != null) callback.onComplete(true);
                    return true;
                }
            }
            
            // Download library (in real implementation, this would download from KeyAuth)
            // For now, we'll copy from assets or native lib directory
            boolean success = copyLibraryFromAssets(libraryName, libFile);
            
            if (success) {
                FLog.info("✅ Library updated successfully: " + libraryName);
                
                // Update library info
                libInfo.hash = calculateFileHash(libFile);
                
                // Make executable
                libFile.setExecutable(true);
                
                if (callback != null) callback.onComplete(true);
                return true;
            }
            
            FLog.error("❌ Failed to update library");
            if (callback != null) callback.onComplete(false);
            return false;
            
        } catch (Exception e) {
            FLog.error("❌ Library update error: " + e.getMessage());
            if (callback != null) callback.onError(e.getMessage());
            return false;
        }
    }
    
    /**
     * Update all required libraries
     */
    public void updateAllLibraries(UpdateCallback callback) {
        new Thread(() -> {
            try {
                int updated = 0;
                int failed = 0;
                
                for (Map.Entry<String, LibraryInfo> entry : availableLibraries.entrySet()) {
                    if (updateLibrary(entry.getKey(), null)) {
                        updated++;
                    } else {
                        failed++;
                    }
                    
                    // Progress callback
                    if (callback != null) {
                        int progress = (updated + failed) * 100 / availableLibraries.size();
                        callback.onProgress(progress);
                    }
                }
                
                FLog.info("📚 Library update complete - Updated: " + updated + ", Failed: " + failed);
                
                if (callback != null) {
                    callback.onComplete(failed == 0);
                }
                
            } catch (Exception e) {
                FLog.error("❌ Update all libraries error: " + e.getMessage());
                if (callback != null) callback.onError(e.getMessage());
            }
        }).start();
    }
    
    /**
     * Get path to a specific library
     */
    public File getLibraryPath(String libraryName) {
        File libDir = new File(context.getFilesDir(), "libs");
        return new File(libDir, libraryName);
    }
    
    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return prefs.getBoolean("authenticated", false);
    }
    
    /**
     * Get user subscription info
     */
    public String getSubscriptionInfo() {
        return prefs.getString("subscription", "");
    }
    
    /**
     * Get expiry date
     */
    public String getExpiryDate() {
        return prefs.getString("expiry", "");
    }
    
    /**
     * Get app name
     */
    public String getAppName() {
        return appName != null ? appName : "";
    }
    
    /**
     * Get owner ID
     */
    public String getOwnerID() {
        return ownerID != null ? ownerID : "";
    }
    
    /**
     * Make HTTP request to KeyAuth API
     */
    private JSONObject makeRequest(JSONObject data) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            
            // Convert JSON to form data
            StringBuilder formData = new StringBuilder();
            java.util.Iterator<String> keys = data.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (formData.length() > 0) formData.append("&");
                formData.append(key).append("=").append(data.getString(key));
            }
            
            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                os.write(formData.toString().getBytes(StandardCharsets.UTF_8));
            }
            
            // Read response
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            
            return new JSONObject(response.toString());
            
        } catch (Exception e) {
            FLog.error("❌ Request error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Calculate SHA-256 hash of a file
     */
    private String calculateFileHash(File file) {
        if (!file.exists()) return "";
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                while ((read = fis.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            FLog.error("❌ Hash calculation error: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Copy library from assets or native directory
     */
    private boolean copyLibraryFromAssets(String libraryName, File destFile) {
        try {
            // First try native lib directory
            File nativeLib = new File(context.getApplicationInfo().nativeLibraryDir, libraryName);
            if (nativeLib.exists()) {
                return com.blankj.molihuan.utilcode.util.FileUtils.copy(
                    nativeLib.getAbsolutePath(), 
                    destFile.getAbsolutePath()
                );
            }
            
            // Try assets
            try (java.io.InputStream is = context.getAssets().open("libs/" + libraryName);
                 FileOutputStream fos = new FileOutputStream(destFile)) {
                
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                
                return true;
                
            } catch (Exception e) {
                FLog.error("❌ Asset not found: " + libraryName);
            }
            
            return false;
            
        } catch (Exception e) {
            FLog.error("❌ Copy library error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Callback interface for library updates
     */
    public interface UpdateCallback {
        void onProgress(int percent);
        void onComplete(boolean success);
        void onError(String message);
    }
} 
