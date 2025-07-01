package com.happy.pro.download.config;

import java.util.HashMap;
import java.util.Map;

/**
 * KeyAuth File ID Mappings Configuration
 * 
 * @author BEAR-LOADER Team
 * @version 3.0.0
 */
public class FileMapping {
    
    // Patch configuration file IDs
    private static final Map<String, String> PATCH_CONFIG_IDS = new HashMap<>();
    
    // Library file IDs
    private static final Map<String, String> STEALTH_LIB_IDS = new HashMap<>();
    private static final Map<String, String> HOOK_LIB_IDS = new HashMap<>();
    private static final Map<String, String> CRYPTO_LIB_IDS = new HashMap<>();
    
    // Asset file IDs
    private static final Map<String, String> ASSET_IDS = new HashMap<>();
    
    static {
        // Initialize patch configuration mappings
        PATCH_CONFIG_IDS.put("com.tencent.ig", "420390");          // PUBG Global
        PATCH_CONFIG_IDS.put("com.pubg.krmobile", "420391");       // PUBG Korea
        PATCH_CONFIG_IDS.put("com.reko.pubgm", "420392");          // PUBG Taiwan
        PATCH_CONFIG_IDS.put("com.vng.pubgmobile", "420393");      // PUBG Vietnam
        PATCH_CONFIG_IDS.put("com.battlegroundmobile.india", "420394"); // BGMI India
        
        // Initialize stealth library mappings
        STEALTH_LIB_IDS.put("com.tencent.ig", "420395");
        STEALTH_LIB_IDS.put("com.pubg.krmobile", "420396");
        STEALTH_LIB_IDS.put("com.reko.pubgm", "420397");
        STEALTH_LIB_IDS.put("com.vng.pubgmobile", "420398");
        STEALTH_LIB_IDS.put("com.battlegroundmobile.india", "420399");
        
        // Initialize hook engine mappings
        HOOK_LIB_IDS.put("com.tencent.ig", "420400");
        HOOK_LIB_IDS.put("com.pubg.krmobile", "420401");
        HOOK_LIB_IDS.put("com.reko.pubgm", "420402");
        HOOK_LIB_IDS.put("com.vng.pubgmobile", "420403");
        HOOK_LIB_IDS.put("com.battlegroundmobile.india", "420404");
        
        // Initialize crypto engine mappings
        CRYPTO_LIB_IDS.put("com.tencent.ig", "420405");
        CRYPTO_LIB_IDS.put("com.pubg.krmobile", "420406");
        CRYPTO_LIB_IDS.put("com.reko.pubgm", "420407");
        CRYPTO_LIB_IDS.put("com.vng.pubgmobile", "420408");
        CRYPTO_LIB_IDS.put("com.battlegroundmobile.india", "420409");
        
        // Initialize asset mappings
        ASSET_IDS.put("main_config", "420371");        // Main configuration
        ASSET_IDS.put("app_update", "420410");         // App update APK
        ASSET_IDS.put("game_signatures", "420411");    // Game signature database
    }
    
    /**
     * Get patch configuration file ID for game package
     */
    public static String getPatchConfigId(String packageName) {
        return PATCH_CONFIG_IDS.get(packageName);
    }
    
    /**
     * Get stealth library file ID for game package
     */
    public static String getStealthLibraryId(String packageName) {
        return STEALTH_LIB_IDS.get(packageName);
    }
    
    /**
     * Get hook engine file ID for game package
     */
    public static String getHookEngineId(String packageName) {
        return HOOK_LIB_IDS.get(packageName);
    }
    
    /**
     * Get crypto engine file ID for game package
     */
    public static String getCryptoEngineId(String packageName) {
        return CRYPTO_LIB_IDS.get(packageName);
    }
    
    /**
     * Get asset file ID by name
     */
    public static String getAssetId(String assetName) {
        return ASSET_IDS.get(assetName);
    }
    
    /**
     * Check if package is supported
     */
    public static boolean isPackageSupported(String packageName) {
        return PATCH_CONFIG_IDS.containsKey(packageName);
    }
    
    /**
     * Get all supported packages
     */
    public static String[] getSupportedPackages() {
        return PATCH_CONFIG_IDS.keySet().toArray(new String[0]);
    }
    
    /**
     * Game information
     */
    public static class GameInfo {
        public final String packageName;
        public final String displayName;
        public final String region;
        
        public GameInfo(String packageName, String displayName, String region) {
            this.packageName = packageName;
            this.displayName = displayName;
            this.region = region;
        }
    }
    
    /**
     * Get game information
     */
    public static GameInfo getGameInfo(String packageName) {
        switch (packageName) {
            case "com.tencent.ig":
                return new GameInfo(packageName, "PUBG Mobile Global", "Global");
            case "com.pubg.krmobile":
                return new GameInfo(packageName, "PUBG Mobile Korea", "Korea");
            case "com.reko.pubgm":
                return new GameInfo(packageName, "PUBG Mobile Taiwan", "Taiwan");
            case "com.vng.pubgmobile":
                return new GameInfo(packageName, "PUBG Mobile Vietnam", "Vietnam");
            case "com.battlegroundmobile.india":
                return new GameInfo(packageName, "Battlegrounds Mobile India", "India");
            default:
                return null;
        }
    }
} 