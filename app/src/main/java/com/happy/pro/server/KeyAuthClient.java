package com.happy.pro.server;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.happy.pro.utils.FLog;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * KeyAuth API 1.3 Client - Correct Form-Based Implementation
 * Based on working KeyAuth implementation pattern
 * 
 * @author BEAR-LOADER Team
 * @version 3.0.0
 */
public final class KeyAuthClient {

    // KeyAuth API 1.3 Configuration
    private static final String OWNER_ID = "yLoA9zcOEF";
    private static final String APP_NAME = "happyloader";
    private static final String APP_VERSION = "1.3";
    private static final String API_URL = "https://keyauth.win/api/1.3/";
    
    // API Endpoints
    private static final String INIT_ENDPOINT = "init";
    private static final String LICENSE_ENDPOINT = "license";
    private static final String CHECK_ENDPOINT = "check";
    
    // Instance Management
    private static volatile KeyAuthClient INSTANCE;
    private final OkHttpClient httpClient;
    private String sessionId = null;
    private UserInfo userInfo = null;
    private boolean isAuthenticated = false;
    private String lastMessage = "";
    private boolean lastSuccess = false;

    private KeyAuthClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    public static KeyAuthClient getInstance() {
        if (INSTANCE == null) {
            synchronized (KeyAuthClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new KeyAuthClient();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Initialize KeyAuth session using correct form-based API
     */
    public void initialize(@NonNull KeyAuthCallback callback) {
        FLog.info("🔐 Initializing KeyAuth API 1.3 with form-based requests");
        
        try {
            String hwid = generateHWID();
            String hash = createHash(INIT_ENDPOINT);
            
            // Use FormBody instead of JSON
            RequestBody formBody = new FormBody.Builder()
                    .add("type", INIT_ENDPOINT)
                    .add("name", APP_NAME)
                    .add("ownerid", OWNER_ID)
                    .add("version", APP_VERSION)
                    .add("hash", hash)
                    .add("hwid", hwid)
                    .build();
            
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(formBody)
                    .addHeader("User-Agent", "KeyAuth")
                    .build();
            
            FLog.info("📤 Sending init request with OwnerID: " + OWNER_ID);
            FLog.info("📤 HWID: " + hwid.substring(0, 8) + "...");
            FLog.info("📤 Hash: " + hash.substring(0, 8) + "...");
            
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    lastMessage = "Network error: " + e.getMessage();
                    lastSuccess = false;
                    FLog.error("❌ KeyAuth init network error: " + e.getMessage());
                    callback.onError(lastMessage);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    handleInitResponse(response, callback);
                }
            });
            
        } catch (Exception e) {
            lastMessage = "Init error: " + e.getMessage();
            lastSuccess = false;
            FLog.error("❌ KeyAuth init exception: " + e.getMessage());
            callback.onError(lastMessage);
        }
    }

    /**
     * Validate license key using correct form-based API
     */
    public void validateLicense(@NonNull String licenseKey, @NonNull KeyAuthCallback callback) {
        if (sessionId == null || sessionId.isEmpty()) {
            callback.onError("KeyAuth not initialized. Call initialize() first.");
            return;
        }

        FLog.info("🎫 Validating license with form-based API");
        FLog.info("🎫 License: " + licenseKey.substring(0, Math.min(8, licenseKey.length())) + "...");
        
        try {
            String hwid = generateHWID();
            String hash = createHash(LICENSE_ENDPOINT);
            
            // Use FormBody for license validation
            RequestBody formBody = new FormBody.Builder()
                    .add("type", LICENSE_ENDPOINT)
                    .add("name", APP_NAME)
                    .add("ownerid", OWNER_ID)
                    .add("version", APP_VERSION)
                    .add("hash", hash)
                    .add("hwid", hwid)
                    .add("key", licenseKey)
                    .add("sessionid", sessionId)
                    .build();
            
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(formBody)
                    .addHeader("User-Agent", "KeyAuth")
                    .build();
            
            FLog.info("📤 Sending license validation with SessionID: " + sessionId.substring(0, 8) + "...");
            
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    lastMessage = "Network error: " + e.getMessage();
                    lastSuccess = false;
                    FLog.error("❌ License validation network error: " + e.getMessage());
                    callback.onError(lastMessage);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    handleLicenseResponse(response, licenseKey, hwid, callback);
                }
            });
            
        } catch (Exception e) {
            lastMessage = "License validation error: " + e.getMessage();
            lastSuccess = false;
            FLog.error("❌ License validation exception: " + e.getMessage());
            callback.onError(lastMessage);
        }
    }

    private void handleInitResponse(Response response, KeyAuthCallback callback) throws IOException {
        if (!response.isSuccessful()) {
            lastMessage = "HTTP error: " + response.code();
            lastSuccess = false;
            callback.onError(lastMessage);
            return;
        }

        String responseBody = response.body().string();
        FLog.info("📥 Init response: " + responseBody);
        
        try {
            JSONObject json = new JSONObject(responseBody);
            lastSuccess = json.optBoolean("success", false);
            lastMessage = json.optString("message", "Unknown response");
            
            if (lastSuccess) {
                sessionId = json.optString("sessionid", "");
                if (!sessionId.isEmpty()) {
                    FLog.info("✅ KeyAuth initialized successfully");
                    FLog.info("🔑 Session ID: " + sessionId.substring(0, 8) + "...");
                    callback.onSuccess("KeyAuth initialized successfully");
                } else {
                    lastMessage = "No session ID received";
                    lastSuccess = false;
                    FLog.error("❌ " + lastMessage);
                    callback.onError(lastMessage);
                }
            } else {
                FLog.error("❌ Init failed: " + lastMessage);
                callback.onError("Initialization failed: " + lastMessage);
            }
        } catch (JSONException e) {
            lastMessage = "Failed to parse init response: " + e.getMessage();
            lastSuccess = false;
            FLog.error("❌ " + lastMessage);
            callback.onError(lastMessage);
        }
    }

    private void handleLicenseResponse(Response response, String licenseKey, String hwid, KeyAuthCallback callback) throws IOException {
        if (!response.isSuccessful()) {
            lastMessage = "HTTP error: " + response.code();
            lastSuccess = false;
            callback.onError(lastMessage);
            return;
        }

        String responseBody = response.body().string();
        FLog.info("📥 License response: " + responseBody);
        
        try {
            JSONObject json = new JSONObject(responseBody);
            lastSuccess = json.optBoolean("success", false);
            lastMessage = json.optString("message", "Unknown response");
            
            if (lastSuccess) {
                FLog.info("✅ License validated successfully!");
                
                // Extract user info if available
                JSONObject info = json.optJSONObject("info");
                if (info != null) {
                    userInfo = new UserInfo(
                        licenseKey,
                        info.optString("expires", ""),
                        info.optString("rank", "User"),
                        hwid
                    );
                    FLog.info("📋 License expires: " + userInfo.getExpires());
                } else {
                    userInfo = new UserInfo(licenseKey, "Unknown", "User", hwid);
                }
                
                isAuthenticated = true;
                callback.onSuccess("License validated successfully");
                
            } else {
                FLog.error("❌ License validation failed: " + lastMessage);
                String userMessage = getUserFriendlyError(lastMessage);
                callback.onError(userMessage);
            }
        } catch (JSONException e) {
            lastMessage = "Failed to parse license response: " + e.getMessage();
            lastSuccess = false;
            FLog.error("❌ " + lastMessage);
            callback.onError(lastMessage);
        }
    }

    private String getUserFriendlyError(String serverMessage) {
        String lower = serverMessage.toLowerCase();
        
        if (lower.contains("invalid") || lower.contains("not found")) {
            return "Invalid license key";
        } else if (lower.contains("expired")) {
            return "License has expired";
        } else if (lower.contains("used") || lower.contains("redeemed")) {
            return "License key already used";
        } else if (lower.contains("hwid") || lower.contains("hardware")) {
            return "Hardware ID mismatch";
        } else if (lower.contains("subscription")) {
            return "Subscription issue";
        } else {
            return serverMessage;
        }
    }

    /**
     * Create hash for API request authentication (like working example)
     */
    private String createHash(String endpoint) {
        try {
            // KeyAuth API v1.3 hash calculation
            String data = endpoint + APP_NAME + OWNER_ID + APP_VERSION;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            FLog.error("Error creating hash: " + e.getMessage());
            return "";
        }
    }

    /**
     * Generate hardware ID (like working example)
     */
    public static String generateHWID() {
        try {
            // Use device-specific information to create HWID
            String deviceInfo = android.os.Build.MANUFACTURER + 
                              android.os.Build.MODEL + 
                              android.os.Build.DEVICE + 
                              android.os.Build.BOARD +
                              android.os.Build.HARDWARE;
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(deviceInfo.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString().substring(0, 32).toUpperCase();
        } catch (Exception e) {
            FLog.error("Failed to generate HWID: " + e.getMessage());
            return "DEFAULT_HWID_" + System.currentTimeMillis();
        }
    }

    // Utility methods
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public boolean isInitialized() {
        return sessionId != null && !sessionId.isEmpty();
    }

    @Nullable
    public UserInfo getUserInfo() {
        return userInfo;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public boolean getLastSuccess() {
        return lastSuccess;
    }

    public void logout() {
        sessionId = null;
        userInfo = null;
        isAuthenticated = false;
        FLog.info("🚪 Logged out from KeyAuth");
    }

    /**
     * User information from KeyAuth
     */
    public static class UserInfo {
        private final String licenseKey;
        private final String expires;
        private final String rank;
        private final String hwid;

        public UserInfo(String licenseKey, String expires, String rank, String hwid) {
            this.licenseKey = licenseKey;
            this.expires = expires;
            this.rank = rank;
            this.hwid = hwid;
        }

        public String getLicenseKey() { return licenseKey; }
        public String getExpires() { return expires; }
        public String getRank() { return rank; }
        public String getHwid() { return hwid; }
    }

    /**
     * Callback interface for KeyAuth operations
     */
    public interface KeyAuthCallback {
        void onSuccess(String message);
        void onError(String error);
    }
} 
