package com.happy.pro.loader;

import android.content.Context;

import com.happy.pro.security.EnhancedSignatureVerifier;
import com.happy.pro.utils.FLog;
import com.topjohnwu.superuser.Shell;

/**
 * Adaptive Mod Loader for BEAR-LOADER
 * 
 * This loader adapts to different app signatures:
 * 1. Original apps - Full injection capabilities
 * 2. Signature-killed apps - Virtual injection
 * 3. Self-signed apps - Standard methods
 * 
 * KEY POINT: We support ALL types, not restrict them!
 */
public class AdaptiveModLoader {
    private static final String TAG = "AdaptiveModLoader";
    private Context context;
    
    public AdaptiveModLoader(Context context) {
        this.context = context;
    }
    
    /**
     * Load mod adaptively based on app signature
     */
    public boolean loadMod(String packageName) {
        FLog.info("🎯 Adaptive mod loading for: " + packageName);
        
        // Verify app signature
        EnhancedSignatureVerifier.VerificationResult verification = 
            EnhancedSignatureVerifier.verifyApp(context, packageName);
        
        // Log recommendation
        String recommendation = EnhancedSignatureVerifier.getInjectionRecommendation(verification);
        FLog.info("💡 " + recommendation);
        
        // Adapt loading strategy based on signature
        switch (verification.type) {
            case ORIGINAL:
                return loadForOriginalApp(packageName, verification);
                
            case MODIFIED:
                return loadForModifiedApp(packageName, verification);
                
            case SELF_SIGNED:
                return loadForSelfSignedApp(packageName, verification);
                
            case UNKNOWN:
            default:
                return loadForUnknownApp(packageName, verification);
        }
    }
    
    /**
     * Load mod for original signed app
     */
    private boolean loadForOriginalApp(String packageName, 
                                      EnhancedSignatureVerifier.VerificationResult verification) {
        FLog.info("✅ Loading mod for ORIGINAL app: " + packageName);
        
        if (Shell.rootAccess()) {
            FLog.info("🔐 Using root injection for maximum features");
            // Direct library replacement
            return ModLoaderSystem.getInstance(context).applyMod(packageName, null);
        } else {
            FLog.info("📦 Using container mode for non-root device");
            // Virtual container approach
            return loadVirtualContainer(packageName);
        }
    }
    
    /**
     * Load mod for signature-killed and re-signed app
     */
    private boolean loadForModifiedApp(String packageName, 
                                      EnhancedSignatureVerifier.VerificationResult verification) {
        FLog.info("🔧 Loading mod for MODIFIED app: " + packageName);
        FLog.info("💀 App has killed signature - using bypass methods");
        
        // Modified apps work perfectly with BEAR-LOADER!
        // We use virtual injection to avoid conflicts
        
        if (verification.hasKilledSignature) {
            FLog.info("🎯 Detected signature killer artifacts");
            
            // Store signature killer info for reference
            context.getSharedPreferences("bear_loader", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("has_killed_signature_" + packageName, true)
                .putString("signing_key_" + packageName, verification.signingKey)
                .apply();
        }
        
        // Use virtual container - works great with modified apps
        return loadVirtualContainer(packageName);
    }
    
    /**
     * Load mod for self-signed app (like with BearOwner+ key)
     */
    private boolean loadForSelfSignedApp(String packageName, 
                                        EnhancedSignatureVerifier.VerificationResult verification) {
        FLog.info("🔑 Loading mod for SELF-SIGNED app: " + packageName);
        FLog.info("✍️ Signed with: " + verification.signingKey);
        
        // Self-signed apps are fully supported
        // Use standard injection methods
        
        if (Shell.rootAccess()) {
            return ModLoaderSystem.getInstance(context).applyMod(packageName, null);
        } else {
            return loadVirtualContainer(packageName);
        }
    }
    
    /**
     * Load mod for unknown signature app
     */
    private boolean loadForUnknownApp(String packageName, 
                                     EnhancedSignatureVerifier.VerificationResult verification) {
        FLog.info("❓ Loading mod for UNKNOWN signature app: " + packageName);
        
        // Even unknown signatures work with overlay mode
        FLog.info("📱 Using safe overlay mode");
        
        return loadOverlayMode(packageName);
    }
    
    /**
     * Load virtual container (works with all signature types)
     */
    private boolean loadVirtualContainer(String packageName) {
        try {
            FLog.info("📦 Loading virtual container for: " + packageName);
            
            // This works with:
            // - Original apps
            // - Modified apps
            // - Self-signed apps
            // - Any signature type!
            
            // Initialize container
            com.happy.pro.container.BearContainerManager container = 
                com.happy.pro.container.BearContainerManager.getInstance(context);
            
            // Virtual injection
            boolean success = container.injectApp("virtual://" + packageName, packageName);
            
            if (success) {
                FLog.info("✅ Virtual container loaded successfully");
                
                // Start overlay service
                android.content.Intent serviceIntent = new android.content.Intent(
                    context, com.happy.pro.floating.FloatService.class);
                serviceIntent.putExtra("package", packageName);
                serviceIntent.putExtra("mode", "container");
                context.startService(serviceIntent);
            }
            
            return success;
            
        } catch (Exception e) {
            FLog.error("❌ Virtual container error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load overlay mode (safest, works with any app)
     */
    private boolean loadOverlayMode(String packageName) {
        try {
            FLog.info("📱 Loading overlay mode for: " + packageName);
            
            // Start overlay service
            android.content.Intent serviceIntent = new android.content.Intent(
                context, com.happy.pro.floating.FloatService.class);
            serviceIntent.putExtra("package", packageName);
            serviceIntent.putExtra("mode", "overlay");
            context.startService(serviceIntent);
            
            FLog.info("✅ Overlay mode activated");
            return true;
            
        } catch (Exception e) {
            FLog.error("❌ Overlay mode error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get feature availability based on app signature
     */
    public static FeatureAvailability getFeatureAvailability(
        EnhancedSignatureVerifier.VerificationResult verification) {
        
        FeatureAvailability features = new FeatureAvailability();
        
        switch (verification.type) {
            case ORIGINAL:
                // Original apps get all features
                features.esp = true;
                features.aimbot = true;
                features.memoryHacks = true;
                features.skinChanger = true;
                features.bypass = true;
                break;
                
            case MODIFIED:
                // Modified apps get most features
                features.esp = true;
                features.aimbot = true;
                features.memoryHacks = true;
                features.skinChanger = false; // May conflict with mods
                features.bypass = true;
                break;
                
            case SELF_SIGNED:
                // Self-signed apps get standard features
                features.esp = true;
                features.aimbot = true;
                features.memoryHacks = false; // Limited
                features.skinChanger = false;
                features.bypass = true;
                break;
                
            case UNKNOWN:
            default:
                // Unknown apps get basic features
                features.esp = true;
                features.aimbot = false;
                features.memoryHacks = false;
                features.skinChanger = false;
                features.bypass = false;
                break;
        }
        
        return features;
    }
    
    public static class FeatureAvailability {
        public boolean esp;
        public boolean aimbot;
        public boolean memoryHacks;
        public boolean skinChanger;
        public boolean bypass;
    }
} 
