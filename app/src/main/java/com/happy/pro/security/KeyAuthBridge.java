package com.happy.pro.security;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import com.happy.pro.automation.BearAutomationManager;
import com.happy.pro.container.StealthManager;

/**
 * KeyAuthBridge - Secure bridge for KeyAuth authentication
 * Handles authentication workflow with enhanced security
 */
public class KeyAuthBridge {
    private static final String TAG = "KeyAuthBridge";
    private static KeyAuthBridge instance;
    private boolean authenticated = false;
    private String sessionToken = null;
    
    static {
        try {
            System.loadLibrary("happy");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native library", e);
        }
    }
    
    private KeyAuthBridge() {}
    
    public static synchronized KeyAuthBridge getInstance() {
        if (instance == null) {
            instance = new KeyAuthBridge();
        }
        return instance;
    }
    
    /**
     * Initialize KeyAuth system
     * @return true if initialization successful
     */
    public boolean initialize() {
        try {
            boolean result = nativeInit();
            if (result) {
                Log.i(TAG, "KeyAuth initialized successfully");
            } else {
                Log.e(TAG, "KeyAuth initialization failed");
            }
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Exception during KeyAuth initialization", e);
            return false;
        }
    }
    
    /**
     * Authenticate using license key
     * @param licenseKey License key for authentication
     * @return true if authentication successful
     */
    public boolean authenticateWithLicense(String licenseKey) {
        if (licenseKey == null || licenseKey.trim().isEmpty()) {
            Log.e(TAG, "Invalid license key provided");
            return false;
        }
        
        try {
            boolean result = nativeLicense(licenseKey.trim());
            authenticated = result;
            
            if (result) {
                Log.i(TAG, "License authentication successful");
                // Store session securely in native code
                sessionToken = getSessionFingerprint();
            } else {
                Log.e(TAG, "License authentication failed");
                sessionToken = null;
            }
            
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Exception during license authentication", e);
            authenticated = false;
            sessionToken = null;
            return false;
        }
    }
    
    /**
     * Authenticate using username and password
     * @param username User's username
     * @param password User's password
     * @return true if authentication successful
     */
    public boolean authenticateWithCredentials(String username, String password) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            Log.e(TAG, "Invalid credentials provided");
            return false;
        }
        
        try {
            boolean result = nativeLogin(username.trim(), password);
            authenticated = result;
            
            if (result) {
                Log.i(TAG, "Credential authentication successful");
                // Store session securely in native code
                sessionToken = getSessionFingerprint();
            } else {
                Log.e(TAG, "Credential authentication failed");
                sessionToken = null;
            }
            
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Exception during credential authentication", e);
            authenticated = false;
            sessionToken = null;
            return false;
        }
    }
    
    /**
     * Get session fingerprint (not the actual token)
     * @return Session fingerprint for validation
     */
    public String getSessionFingerprint() {
        if (!authenticated) {
            return null;
        }
        
        try {
            String response = nativeGetResponse();
            // Return only a hash/fingerprint, never the actual token
            return response != null ? String.valueOf(response.hashCode()) : null;
        } catch (Exception e) {
            Log.e(TAG, "Exception getting session fingerprint", e);
            return null;
        }
    }
    
    /**
     * Check if user is currently authenticated
     * @return true if authenticated and session is valid
     */
    public boolean isAuthenticated() {
        return authenticated && sessionToken != null;
    }
    
    /**
     * Terminate current session and cleanup
     */
    public void terminateSession() {
        try {
            nativeTerminate();
            authenticated = false;
            sessionToken = null;
            Log.i(TAG, "Session terminated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Exception during session termination", e);
        }
    }
    
    /**
     * Perform complete authentication workflow
     * @param context Application context
     * @param licenseKey License key for authentication
     * @return true if complete workflow successful
     */
    public boolean performSecureAuthentication(Context context, String licenseKey) {
        // Step 1: Initialize security managers
        BearAutomationManager bearAutomation = BearAutomationManager.getInstance(context);
        StealthManager stealthManager = StealthManager.getInstance();
        AntiDetectionManager antiDetection = AntiDetectionManager.getInstance();
        
        // Step 2: Initialize all security components
        if (!antiDetection.initialize()) {
            Log.e(TAG, "Anti-detection initialization failed");
            return false;
        }
        
        if (!antiDetection.isEnvironmentSafe()) {
            Log.e(TAG, "Environment is not safe for authentication");
            return false;
        }
        
        // Step 3: Initialize KeyAuth
        if (!initialize()) {
            Log.e(TAG, "KeyAuth initialization failed");
            return false;
        }
        
        // Step 4: Authenticate with license
        if (!authenticateWithLicense(licenseKey)) {
            Log.e(TAG, "License authentication failed");
            return false;
        }
        
        // Step 5: Initialize container for current brand
        String deviceId = getDeviceId(context);
        String brandName = "DefaultBrand"; // This should be determined dynamically
        
        if (!stealthManager.initializeContainer(brandName)) {
            Log.e(TAG, "Container initialization failed");
            terminateSession();
            return false;
        }
        
        if (!stealthManager.authenticateContainer(brandName, licenseKey)) {
            Log.e(TAG, "Container authentication failed");
            terminateSession();
            return false;
        }
        
        // Step 6: Initialize automation system
        if (!bearAutomation.initialize(null)) {
            Log.w(TAG, "Bear automation initialization failed");
        }
        
        // Step 7: Enable protection systems
        stealthManager.enableStealthMode(brandName);
        
        if (!antiDetection.enableProtection()) {
            Log.w(TAG, "Anti-detection protection activation failed");
        }
        
        Log.i(TAG, "Secure authentication workflow completed successfully");
        return true;
    }
    
    private String getDeviceId(Context context) {
        try {
            return Settings.Secure.getString(context.getContentResolver(), 
                                           Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get device ID", e);
            return "unknown_device";
        }
    }
    
    // Native method declarations
    private native boolean nativeInit();
    private native boolean nativeLicense(String key);
    private native boolean nativeLogin(String username, String password);
    private native void nativeTerminate();
    private native String nativeGetResponse();
} 
