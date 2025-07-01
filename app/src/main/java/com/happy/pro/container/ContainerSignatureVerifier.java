package com.happy.pro.container;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import com.happy.pro.security.SignKillDetector;
import com.happy.pro.security.StealthManager;
import com.happy.pro.utils.FLog;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 🔥 Enhanced Container Signature Verifier
 * 
 * Advanced signature verification system for Bear-Loader container apps
 * Integrates with our 8-Layer Security Architecture for ultimate protection
 * 
 * Features:
 * ✅ Multi-Signature Support
 * ✅ SignKill Attack Detection  
 * ✅ Stealth Security Integration
 * ✅ Trusted Signature Database
 * ✅ Real-Time Verification
 * ✅ Container Security Validation
 */
public class ContainerSignatureVerifier {
    private static final String TAG = "ContainerVerifier";
    
    // Security integration
    private static SignKillDetector signKillDetector;
    private static StealthManager stealthManager;
    
    // Trusted signatures database (can be extended)
    private static final List<String> TRUSTED_SIGNATURES = new ArrayList<>(Arrays.asList(
        // Add your trusted app signatures here
        "your_bear_loader_signature_here",
        "your_trusted_game_signature_here",
        "development_signature_for_testing"
    ));
    
    // Common debug signatures to detect (potential SignKill indicators)
    private static final List<String> DEBUG_SIGNATURES = new ArrayList<>(Arrays.asList(
        "308202A830820191A003020102020900B3998086D056CFFA30",  // Android debug signature
        "308201DD30820146020101300D06092A864886F70D010105",   // Common debug cert
        "3082018D308201F5A003020102020900936EACBE07F201DF"    // Another debug pattern
    ));
    
    /**
     * Initialize the signature verifier with security integration
     */
    public static void initialize(Context context) {
        try {
            signKillDetector = SignKillDetector.getInstance(context);
            stealthManager = StealthManager.getInstance();
            FLog.info("🔐 ContainerSignatureVerifier initialized with security integration");
        } catch (Exception e) {
            FLog.error("❌ Failed to initialize ContainerSignatureVerifier: " + e.getMessage());
        }
    }
    
    /**
     * Enhanced signature validation with container security
     * 
     * @param context Application context
     * @param packageName Package to verify (null for current app)
     * @return Enhanced verification result
     */
    public static VerificationResult verifySignatureEnhanced(Context context, String packageName) {
        FLog.info("🔍 Starting enhanced signature verification for " + 
                  (packageName != null ? packageName : "current app"));
        
        try {
            // Step 1: Pre-verification security check
            if (!performPreVerificationSecurityCheck()) {
                return new VerificationResult(false, VerificationStatus.SECURITY_THREAT, 
                                            "Security threat detected during verification");
            }
            
            // Step 2: Get signature information
            String signatureHex = getSignatureHex(context, packageName);
            if (signatureHex.isEmpty()) {
                return new VerificationResult(false, VerificationStatus.NO_SIGNATURE, 
                                            "Could not retrieve app signature");
            }
            
            // Step 3: Check for debug signatures (SignKill indicator)
            if (isDebugSignature(signatureHex)) {
                FLog.warn("⚠️ DEBUG SIGNATURE DETECTED - Potential SignKill attack");
                return new VerificationResult(false, VerificationStatus.DEBUG_SIGNATURE, 
                                            "Debug signature detected - potential bypass attempt");
            }
            
            // Step 4: Check against trusted signatures
            if (TRUSTED_SIGNATURES.contains(signatureHex)) {
                FLog.info("✅ Signature verified - App is trusted");
                return new VerificationResult(true, VerificationStatus.TRUSTED, 
                                            "App signature is in trusted database");
            }
            
            // Step 5: Perform real-time SignKill detection
            if (detectSignKillDuringVerification(context)) {
                FLog.error("🚨 SIGNKILL ATTACK DETECTED during verification");
                return new VerificationResult(false, VerificationStatus.SIGNKILL_DETECTED, 
                                            "SignKill signature bypass attack detected");
            }
            
            // Step 6: Default verification result
            FLog.warn("⚠️ Unknown signature - App not in trusted database");
            return new VerificationResult(false, VerificationStatus.UNKNOWN_SIGNATURE, 
                                        "App signature not recognized - Hash: " + signatureHex);
            
        } catch (Exception e) {
            FLog.error("💥 Exception during signature verification: " + e.getMessage());
            return new VerificationResult(false, VerificationStatus.VERIFICATION_ERROR, 
                                        "Verification process failed: " + e.getMessage());
        }
    }
    
    /**
     * Standard signature verification (for compatibility)
     */
    public static boolean isSignatureValid(Context context) {
        VerificationResult result = verifySignatureEnhanced(context, null);
        return result.isValid();
    }
    
    /**
     * Get signature hex with enhanced error handling
     */
    public static String getSignatureHex(Context context, String packageName) {
        try {
            String targetPackage = packageName != null ? packageName : context.getPackageName();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    targetPackage, PackageManager.GET_SIGNATURES);
            
            if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
                Signature signature = packageInfo.signatures[0];
                return getSHA256(signature.toByteArray());
            }
        } catch (PackageManager.NameNotFoundException e) {
            FLog.error("❌ Package not found: " + (packageName != null ? packageName : "current"));
        } catch (Exception e) {
            FLog.error("❌ Error getting signature: " + e.getMessage());
        }
        
        return "";
    }
    
    /**
     * Perform pre-verification security checks
     */
    private static boolean performPreVerificationSecurityCheck() {
        try {
            // Check for active SignKill attacks
            if (signKillDetector != null) {
                int signKillResult = signKillDetector.performSignKillDetection();
                if (signKillResult == SignKillDetector.DetectionResult.CRITICAL_BREACH) {
                    FLog.error("🚨 CRITICAL: Active SignKill attack detected");
                    return false;
                }
            }
            
            // Validate stealth environment
            if (stealthManager != null) {
                if (!stealthManager.validateEnvironment()) {
                    FLog.error("🔴 Environment validation failed");
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            FLog.error("💥 Pre-verification security check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if signature matches known debug patterns
     */
    private static boolean isDebugSignature(String signatureHex) {
        for (String debugSig : DEBUG_SIGNATURES) {
            if (signatureHex.toUpperCase().contains(debugSig)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Detect SignKill attacks during verification process
     */
    private static boolean detectSignKillDuringVerification(Context context) {
        try {
            // Test multiple signature calls to detect hooks
            long startTime = System.nanoTime();
            
            for (int i = 0; i < 3; i++) {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), PackageManager.GET_SIGNATURES);
                
                if (packageInfo.signatures == null || packageInfo.signatures.length == 0) {
                    FLog.warn("⚠️ Suspicious: No signatures returned on attempt " + (i + 1));
                    return true;
                }
            }
            
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000; // Convert to milliseconds
            
            // If verification takes too long, might indicate hooks
            if (duration > 500) { // 500ms threshold
                FLog.warn("⚠️ Suspicious: Signature verification took " + duration + "ms");
                return true;
            }
            
            return false;
        } catch (Exception e) {
            FLog.error("💥 SignKill detection error: " + e.getMessage());
            return true; // Assume threat if detection fails
        }
    }
    
    /**
     * Compute SHA-256 hash
     */
    private static String getSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            FLog.error("❌ SHA-256 algorithm not found");
            return "";
        }
    }
    
    /**
     * Convert bytes to hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
    
    /**
     * Add a trusted signature to the database
     */
    public static void addTrustedSignature(String signatureHex) {
        if (!TRUSTED_SIGNATURES.contains(signatureHex)) {
            TRUSTED_SIGNATURES.add(signatureHex);
            FLog.info("🔐 Added trusted signature: " + signatureHex);
        }
    }
    
    /**
     * Get all trusted signatures
     */
    public static List<String> getTrustedSignatures() {
        return new ArrayList<>(TRUSTED_SIGNATURES);
    }
    
    /**
     * Verification result class
     */
    public static class VerificationResult {
        private final boolean valid;
        private final VerificationStatus status;
        private final String message;
        
        public VerificationResult(boolean valid, VerificationStatus status, String message) {
            this.valid = valid;
            this.status = status;
            this.message = message;
        }
        
        public boolean isValid() { return valid; }
        public VerificationStatus getStatus() { return status; }
        public String getMessage() { return message; }
        
        @Override
        public String toString() {
            return "VerificationResult{" +
                    "valid=" + valid +
                    ", status=" + status +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
    
    /**
     * Verification status enumeration
     */
    public enum VerificationStatus {
        TRUSTED("✅ Trusted signature"),
        UNKNOWN_SIGNATURE("⚠️ Unknown signature"),
        DEBUG_SIGNATURE("🚨 Debug signature detected"),
        NO_SIGNATURE("❌ No signature found"),
        SIGNKILL_DETECTED("🚨 SignKill attack detected"),
        SECURITY_THREAT("🚨 Security threat detected"),
        VERIFICATION_ERROR("💥 Verification error");
        
        private final String description;
        
        VerificationStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
} 
