package com.happy.pro.server;

import android.os.Handler;
import android.os.Looper;

import com.happy.pro.BoxApplication;
import com.happy.pro.utils.FLog;

/**
 * Pure KeyAuth Authentication Manager
 * No offline fallback - KeyAuth only authentication
 * 
 * @author BEAR-LOADER Team  
 * @version 3.0.0
 */
public class AuthenticationManager {

    private static volatile AuthenticationManager INSTANCE;
    private final KeyAuthClient keyAuthClient;
    private final Handler mainHandler;

    private AuthenticationManager() {
        keyAuthClient = KeyAuthClient.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static AuthenticationManager getInstance() {
        if (INSTANCE == null) {
            synchronized (AuthenticationManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AuthenticationManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Validate license key through KeyAuth only
     * No fallback - pure KeyAuth authentication
     */
    public void validateLicenseOnly(String licenseKey, AuthCallback callback) {
        FLog.info("🔐 Starting KeyAuth license validation...");
        
        // First initialize KeyAuth
        keyAuthClient.initialize(new KeyAuthClient.KeyAuthCallback() {
            @Override
            public void onSuccess(String message) {
                FLog.info("✅ KeyAuth initialized, validating license...");
                
                // Now validate the license
                keyAuthClient.validateLicense(licenseKey, new KeyAuthClient.KeyAuthCallback() {
                    @Override
                    public void onSuccess(String message) {
                        FLog.info("🎉 License validation successful!");
                        mainHandler.post(() -> callback.onSuccess("License validated successfully"));
                    }

                    @Override
                    public void onError(String error) {
                        FLog.error("❌ License validation failed: " + error);
                        mainHandler.post(() -> callback.onError(error));
                    }
                });
            }

            @Override
            public void onError(String error) {
                FLog.error("❌ KeyAuth initialization failed: " + error);
                mainHandler.post(() -> callback.onError("KeyAuth connection failed: " + error));
            }
        });
    }

    public boolean isAuthenticated() {
        return keyAuthClient.isAuthenticated();
    }

    public KeyAuthClient.UserInfo getUserInfo() {
        return keyAuthClient.getUserInfo();
    }

    public void logout() {
        keyAuthClient.logout();
        FLog.info("🚪 User logged out");
    }

    /**
     * Callback interface for authentication
     */
    public interface AuthCallback {
        void onSuccess(String message);
        void onError(String error);
    }
} 
