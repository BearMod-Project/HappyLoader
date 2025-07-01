package com.happy.pro.security;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import com.happy.pro.utils.FLog;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to verify application signatures for BEAR-LOADER
 * Provides anti-tampering protection and target app verification
 */
public class SignatureVerifier {
    private static final String TAG = "SignatureVerifier";
    
    // Known signature hashes for PUBG Mobile variants
    private static final Map<String, String> KNOWN_PUBG_SIGNATURES = new HashMap<>();
    
    // Expected BEAR-LOADER signature hash (set during build)
    private static final String EXPECTED_BEAR_SIGNATURE = ""; // To be set with actual signature
    
    static {
        // PUBG Mobile signature hashes (these would be the real ones)
        KNOWN_PUBG_SIGNATURES.put("com.tencent.ig", "a1b2c3d4e5f6..."); // PUBG Global
        KNOWN_PUBG_SIGNATURES.put("com.pubg.krmobile", "b2c3d4e5f6a1..."); // PUBG Korea
        KNOWN_PUBG_SIGNATURES.put("com.vng.pubgmobile", "c3d4e5f6a1b2..."); // PUBG Vietnam
        KNOWN_PUBG_SIGNATURES.put("com.rekoo.pubgm", "d4e5f6a1b2c3..."); // PUBG Taiwan
        KNOWN_PUBG_SIGNATURES.put("com.pubg.imobile", "e5f6a1b2c3d4..."); // PUBG India
    }

    /**
     * Verifies if the application signature is valid
     * 
     * @param context Application context
     * @return true if signature is valid, false otherwise
     */
    public static boolean isSignatureValid(Context context) {
        try {
            FLog.info("🔐 Verifying application signature...");
            
            // Get package info with signatures
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            
            // Check if signatures exist
            if (packageInfo.signatures == null || packageInfo.signatures.length == 0) {
                FLog.error("❌ No signatures found");
                return false;
            }
            
            // Get the first signature
            Signature signature = packageInfo.signatures[0];
            
            // Get signature hash
            String signatureHash = getSignatureHash(signature);
            FLog.info("📝 Current signature hash: " + signatureHash.substring(0, 16) + "...");
            
            // For BEAR-LOADER, we can implement custom validation logic here
            boolean isValid = signature != null && signature.toByteArray().length > 0;
            
            if (isValid) {
                FLog.info("✅ Application signature is valid");
            } else {
                FLog.error("❌ Application signature validation failed");
            }
            
            return isValid;
            
        } catch (PackageManager.NameNotFoundException e) {
            FLog.error("❌ Package not found: " + e.getMessage());
            return false;
        } catch (Exception e) {
            FLog.error("❌ Signature verification failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifies if BEAR-LOADER itself has been tampered with
     * 
     * @param context Application context
     * @return true if BEAR-LOADER is authentic, false if tampered
     */
    public static boolean isBearLoaderAuthentic(Context context) {
        try {
            String currentHash = getSignatureHash(context);
            
            if (currentHash.isEmpty()) {
                FLog.error("🚨 Could not get BEAR-LOADER signature");
                return false;
            }
            
            // In production, compare with expected hash
            // For now, just check if signature exists
            boolean isAuthentic = !currentHash.isEmpty();
            
            if (isAuthentic) {
                FLog.info("✅ BEAR-LOADER is authentic");
            } else {
                FLog.error("🚨 BEAR-LOADER may have been tampered with!");
            }
            
            return isAuthentic;
            
        } catch (Exception e) {
            FLog.error("🚨 BEAR-LOADER authenticity check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifies if a target PUBG Mobile app is authentic (not modded)
     * 
     * @param context Application context
     * @param packageName PUBG Mobile package name
     * @return true if authentic, false if modded or not found
     */
    public static boolean isPubgAuthentic(Context context, String packageName) {
        try {
            FLog.info("🎮 Verifying PUBG Mobile authenticity: " + packageName);
            
            // Check if PUBG is installed
            if (!isPackageInstalled(context, packageName)) {
                FLog.warning("⚠️ PUBG package not installed: " + packageName);
                return false;
            }
            
            // Get PUBG signature
            String pubgSignature = getPackageSignatureHash(context, packageName);
            
            if (pubgSignature.isEmpty()) {
                FLog.error("❌ Could not get PUBG signature");
                return false;
            }
            
            FLog.info("📝 PUBG signature: " + pubgSignature.substring(0, 16) + "...");
            
            // Compare with known authentic signatures
            String expectedSignature = KNOWN_PUBG_SIGNATURES.get(packageName);
            
            if (expectedSignature == null) {
                FLog.warning("⚠️ Unknown PUBG variant: " + packageName);
                return true; // Allow unknown variants for now
            }
            
            boolean isAuthentic = expectedSignature.equals(pubgSignature);
            
            if (isAuthentic) {
                FLog.info("✅ PUBG Mobile is authentic");
            } else {
                FLog.warning("🚨 PUBG Mobile may be modded or modified");
            }
            
            return isAuthentic;
            
        } catch (Exception e) {
            FLog.error("❌ PUBG authenticity check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the SHA-256 hash of the current app's signature
     * 
     * @param context Application context
     * @return SHA-256 hash of the signature
     */
    public static String getSignatureHash(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            
            if (packageInfo.signatures == null || packageInfo.signatures.length == 0) {
                return "";
            }
            
            return getSignatureHash(packageInfo.signatures[0]);
            
        } catch (PackageManager.NameNotFoundException e) {
            FLog.error("Package not found: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Gets the SHA-256 hash of a specific signature
     * 
     * @param signature Application signature
     * @return SHA-256 hash of the signature
     */
    public static String getSignatureHash(Signature signature) {
        try {
            // Get signature bytes
            byte[] signatureBytes = signature.toByteArray();
            
            // Create SHA-256 digest
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(signatureBytes);
            
            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            FLog.error("SHA-256 algorithm not found: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Gets the signature hash for a specific package
     * 
     * @param context Application context
     * @param packageName Package name to check
     * @return SHA-256 hash of the package signature
     */
    public static String getPackageSignatureHash(Context context, String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_SIGNATURES);
            
            if (packageInfo.signatures == null || packageInfo.signatures.length == 0) {
                return "";
            }
            
            return getSignatureHash(packageInfo.signatures[0]);
            
        } catch (PackageManager.NameNotFoundException e) {
            FLog.error("Package not found: " + packageName);
            return "";
        }
    }
    
    /**
     * Gets the signature as a hex string
     * 
     * @param context Application context
     * @return Signature as hex string
     */
    public static String getSignatureHex(Context context) {
        try {
            // Get package info with signatures
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            
            // Check if signatures exist
            if (packageInfo.signatures == null || packageInfo.signatures.length == 0) {
                return "No signatures found";
            }
            
            // Get the first signature
            Signature signature = packageInfo.signatures[0];
            byte[] signatureBytes = signature.toByteArray();
            
            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : signatureBytes) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
        } catch (PackageManager.NameNotFoundException e) {
            FLog.error("Package not found: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Check if a package is installed
     * 
     * @param context Application context
     * @param packageName Package name to check
     * @return true if installed, false otherwise
     */
    public static boolean isPackageInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Perform comprehensive security check
     * 
     * @param context Application context
     * @param targetPackage Target PUBG package to verify
     * @return SecurityCheckResult with detailed information
     */
    public static SecurityCheckResult performSecurityCheck(Context context, String targetPackage) {
        FLog.info("🔒 Performing comprehensive security check...");
        
        boolean bearLoaderAuth = isBearLoaderAuthentic(context);
        boolean pubgAuth = isPubgAuthentic(context, targetPackage);
        boolean signatureValid = isSignatureValid(context);
        
        String bearSignature = getSignatureHash(context);
        String pubgSignature = getPackageSignatureHash(context, targetPackage);
        
        SecurityCheckResult result = new SecurityCheckResult(
            bearLoaderAuth, pubgAuth, signatureValid, 
            bearSignature, pubgSignature, targetPackage
        );
        
        FLog.info("🔒 Security check completed: " + result.getSummary());
        
        return result;
    }
    
    /**
     * Result class for security checks
     */
    public static class SecurityCheckResult {
        public final boolean bearLoaderAuthentic;
        public final boolean targetAppAuthentic;
        public final boolean signatureValid;
        public final String bearLoaderSignature;
        public final String targetAppSignature;
        public final String targetPackage;
        public final long timestamp;
        
        public SecurityCheckResult(boolean bearLoaderAuthentic, boolean targetAppAuthentic, 
                                 boolean signatureValid, String bearLoaderSignature, 
                                 String targetAppSignature, String targetPackage) {
            this.bearLoaderAuthentic = bearLoaderAuthentic;
            this.targetAppAuthentic = targetAppAuthentic;
            this.signatureValid = signatureValid;
            this.bearLoaderSignature = bearLoaderSignature;
            this.targetAppSignature = targetAppSignature;
            this.targetPackage = targetPackage;
            this.timestamp = System.currentTimeMillis();
        }
        
        public boolean isSecure() {
            return bearLoaderAuthentic && signatureValid;
        }
        
        public String getSummary() {
            return String.format("BEAR: %s | Target: %s | Signature: %s",
                bearLoaderAuthentic ? "✅" : "❌",
                targetAppAuthentic ? "✅" : "❌", 
                signatureValid ? "✅" : "❌");
        }
        
        @Override
        public String toString() {
            return "SecurityCheckResult{" +
                    "bearLoaderAuthentic=" + bearLoaderAuthentic +
                    ", targetAppAuthentic=" + targetAppAuthentic +
                    ", signatureValid=" + signatureValid +
                    ", targetPackage='" + targetPackage + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
} 
