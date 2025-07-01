package com.happy.pro.security;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import com.happy.pro.utils.FLog;

import java.io.File;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Signature Verifier for BEAR-LOADER
 * 
 * This verifier works with:
 * 1. Original signed apps (Tencent, PUBG Corp signatures)
 * 2. Modified apps (signature-killed and re-signed)
 * 3. Self-signed apps (custom keys like BearOwner+)
 * 
 * The verification is used for DETECTION, not RESTRICTION
 */
public class EnhancedSignatureVerifier {
    private static final String TAG = "EnhancedSigVerifier";
    
    // Known signatures (for reference, not enforcement)
    private static final Map<String, SignatureInfo> KNOWN_SIGNATURES = new HashMap<>();
    
    static {
        // PUBG Mobile Global 3.8 (from your screenshot)
        KNOWN_SIGNATURES.put("com.tencent.ig", new SignatureInfo(
            "c222bef60564a3fc5bd208a4", // MD5
            "2c680ac4d5bc392ddc29e7e7", // SHA1
            "eacca5e4335e6a7b69b837ce" // SHA256
        ));
        
        // Add more known signatures as needed
    }
    
    public static class SignatureInfo {
        public final String md5;
        public final String sha1;
        public final String sha256;
        public boolean isOriginal;
        public boolean isModified;
        public boolean isSelfSigned;
        
        public SignatureInfo(String md5, String sha1, String sha256) {
            this.md5 = md5;
            this.sha1 = sha1;
            this.sha256 = sha256;
            this.isOriginal = true;
        }
    }
    
    public static class VerificationResult {
        public String packageName;
        public SignatureInfo signatureInfo;
        public SignatureType type;
        public String signingKey; // e.g., "BearOwner+", "Original", etc.
        public boolean hasKilledSignature;
        public String recommendedApproach;
        
        public enum SignatureType {
            ORIGINAL,      // Original app signature
            MODIFIED,      // Signature killed and re-signed
            SELF_SIGNED,   // Custom signature (like BearOwner+)
            UNKNOWN        // Unknown signature
        }
    }
    
    /**
     * Perform comprehensive signature verification
     */
    public static VerificationResult verifyApp(Context context, String packageName) {
        VerificationResult result = new VerificationResult();
        result.packageName = packageName;
        
        try {
            // Get app info
            PackageInfo packageInfo = context.getPackageManager()
                .getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            
            if (packageInfo.signatures == null || packageInfo.signatures.length == 0) {
                FLog.error("❌ No signatures found for " + packageName);
                result.type = VerificationResult.SignatureType.UNKNOWN;
                return result;
            }
            
            // Calculate signature hashes
            Signature signature = packageInfo.signatures[0];
            result.signatureInfo = calculateSignatureHashes(signature);
            
            // Check if it's a known original signature
            SignatureInfo knownSig = KNOWN_SIGNATURES.get(packageName);
            if (knownSig != null && matchesSignature(result.signatureInfo, knownSig)) {
                result.type = VerificationResult.SignatureType.ORIGINAL;
                result.signingKey = "Original";
                result.hasKilledSignature = false;
                result.recommendedApproach = "Direct injection (Root) or Overlay (Non-root)";
                FLog.info("✅ Original signature detected for " + packageName);
            } else {
                // Check for signature killing indicators
                result.hasKilledSignature = checkForSignatureKilling(context, packageName);
                
                if (result.hasKilledSignature) {
                    result.type = VerificationResult.SignatureType.MODIFIED;
                    result.signingKey = detectSigningKey(context, packageName);
                    result.recommendedApproach = "Virtual injection with signature bypass";
                    FLog.info("🔧 Modified signature detected (killed & re-signed) for " + packageName);
                } else if (isSelfSigned(result.signatureInfo)) {
                    result.type = VerificationResult.SignatureType.SELF_SIGNED;
                    result.signingKey = "Self-signed";
                    result.recommendedApproach = "Standard injection methods";
                    FLog.info("🔑 Self-signed app detected for " + packageName);
                } else {
                    result.type = VerificationResult.SignatureType.UNKNOWN;
                    result.signingKey = "Unknown";
                    result.recommendedApproach = "Overlay mode only";
                    FLog.info("❓ Unknown signature for " + packageName);
                }
            }
            
            // Log detailed info
            logVerificationResult(result);
            
        } catch (Exception e) {
            FLog.error("❌ Signature verification error: " + e.getMessage());
            result.type = VerificationResult.SignatureType.UNKNOWN;
        }
        
        return result;
    }
    
    /**
     * Calculate signature hashes using the same approach as FileHashCalculator
     */
    private static SignatureInfo calculateSignatureHashes(Signature signature) {
        try {
            byte[] signatureBytes = signature.toByteArray();
            
            // Calculate MD5
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(signatureBytes);
            String md5Hash = bytesToHexString(md5.digest());
            
            // Calculate SHA1
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.update(signatureBytes);
            String sha1Hash = bytesToHexString(sha1.digest());
            
            // Calculate SHA256
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(signatureBytes);
            String sha256Hash = bytesToHexString(sha256.digest());
            
            SignatureInfo info = new SignatureInfo(md5Hash, sha1Hash, sha256Hash);
            info.isOriginal = false; // Will be set later based on comparison
            
            return info;
            
        } catch (Exception e) {
            FLog.error("❌ Hash calculation error: " + e.getMessage());
            return new SignatureInfo("", "", "");
        }
    }
    
    /**
     * Check for signature killing indicators
     */
    private static boolean checkForSignatureKilling(Context context, String packageName) {
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                .getApplicationInfo(packageName, 0);
            
            // Check for common signature killer artifacts
            File appDir = new File(appInfo.dataDir);
            
            // Check for signature killer files
            String[] killerIndicators = {
                "SignatureKiller268",
                "bin.mt.signature.KillerApplication",
                "origin268.apk",
                "files/origin268.apk"
            };
            
            for (String indicator : killerIndicators) {
                File indicatorFile = new File(appDir, indicator);
                if (indicatorFile.exists()) {
                    FLog.info("🔍 Found signature killer indicator: " + indicator);
                    return true;
                }
            }
            
            // Check for signature killer in shared prefs or other locations
            File sharedPrefs = new File(appDir, "shared_prefs/signature_killer.xml");
            if (sharedPrefs.exists()) {
                return true;
            }
            
        } catch (Exception e) {
            FLog.error("❌ Error checking for signature killing: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Detect the signing key used (e.g., BearOwner+)
     */
    private static String detectSigningKey(Context context, String packageName) {
        // This would require parsing the certificate info
        // For now, return a generic identifier
        return "Custom Key";
    }
    
    /**
     * Check if signature appears to be self-signed
     */
    private static boolean isSelfSigned(SignatureInfo info) {
        // Self-signed certificates often have certain patterns
        // This is a simplified check
        return info.sha256.startsWith("30") || info.sha256.contains("debug");
    }
    
    /**
     * Compare two signatures
     */
    private static boolean matchesSignature(SignatureInfo sig1, SignatureInfo sig2) {
        return sig1.md5.equalsIgnoreCase(sig2.md5) &&
               sig1.sha1.equalsIgnoreCase(sig2.sha1) &&
               sig1.sha256.equalsIgnoreCase(sig2.sha256);
    }
    
    /**
     * Convert bytes to hex string (matching FileHashCalculator format)
     */
    private static String bytesToHexString(byte[] bytes) {
        if (bytes == null) return "";
        
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b)); // lowercase for consistency
        }
        return sb.toString();
    }
    
    /**
     * Log verification result details
     */
    private static void logVerificationResult(VerificationResult result) {
        FLog.info("📋 === Signature Verification Result ===");
        FLog.info("📦 Package: " + result.packageName);
        FLog.info("🔑 Type: " + result.type);
        FLog.info("✍️ Signing Key: " + result.signingKey);
        FLog.info("💀 Killed Signature: " + result.hasKilledSignature);
        FLog.info("💡 Recommended: " + result.recommendedApproach);
        
        if (result.signatureInfo != null) {
            FLog.info("🔍 MD5: " + result.signatureInfo.md5.substring(0, 16) + "...");
            FLog.info("🔍 SHA1: " + result.signatureInfo.sha1.substring(0, 16) + "...");
            FLog.info("🔍 SHA256: " + result.signatureInfo.sha256.substring(0, 16) + "...");
        }
        
        FLog.info("=====================================");
    }
    
    /**
     * Get injection recommendation based on signature
     */
    public static String getInjectionRecommendation(VerificationResult result) {
        switch (result.type) {
            case ORIGINAL:
                return "✅ Original app - All injection methods available";
                
            case MODIFIED:
                return "🔧 Modified app - Use virtual injection or overlay";
                
            case SELF_SIGNED:
                return "🔑 Self-signed - Standard injection with caution";
                
            case UNKNOWN:
            default:
                return "❓ Unknown signature - Overlay mode recommended";
        }
    }
} 
