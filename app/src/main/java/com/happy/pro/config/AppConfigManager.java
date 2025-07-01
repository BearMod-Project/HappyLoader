package com.happy.pro.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.happy.pro.utils.FLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modern Configuration Manager for BEAR-LOADER 3.0.0
 * Replaces the old native API calls with JSON-based configuration
 */
public class AppConfigManager {
    private static final String TAG = "AppConfigManager";
    private static AppConfigManager instance;
    private JSONObject config;
    private Context context;
    private SharedPreferences prefs;
    
    // Configuration keys
    private static final String PREF_CONFIG_CACHE = "config_cache";
    private static final String PREF_CONFIG_TIMESTAMP = "config_timestamp";
    private static final String PREF_CONFIG_VERSION = "config_version";
    
    private AppConfigManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences("bear_loader_config", Context.MODE_PRIVATE);
        loadConfiguration();
    }
    
    public static synchronized AppConfigManager getInstance(Context context) {
        if (instance == null) {
            instance = new AppConfigManager(context);
        }
        return instance;
    }
    
    /**
     * Load configuration from assets or cache
     */
    private void loadConfiguration() {
        try {
            // Try to load from cache first
            String cachedConfig = prefs.getString(PREF_CONFIG_CACHE, null);
            long cacheTimestamp = prefs.getLong(PREF_CONFIG_TIMESTAMP, 0);
            
            // Check if cache is still valid (24 hours)
            boolean cacheValid = (System.currentTimeMillis() - cacheTimestamp) < (24 * 60 * 60 * 1000);
            
            if (cachedConfig != null && cacheValid) {
                config = new JSONObject(cachedConfig);
                FLog.info("📋 Loaded configuration from cache");
            } else {
                // Load from assets
                config = loadFromAssets();
                // Cache the configuration
                cacheConfiguration();
                FLog.info("📋 Loaded configuration from assets");
            }
            
        } catch (Exception e) {
            FLog.error("❌ Failed to load configuration: " + e.getMessage());
            // Load fallback configuration
            loadFallbackConfiguration();
        }
    }
    
    /**
     * Load configuration from assets
     */
    private JSONObject loadFromAssets() throws IOException, JSONException {
        InputStream is = context.getAssets().open("config.json");
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, StandardCharsets.UTF_8);
        return new JSONObject(json);
    }
    
    /**
     * Cache the current configuration
     */
    private void cacheConfiguration() {
        try {
            prefs.edit()
                .putString(PREF_CONFIG_CACHE, config.toString())
                .putLong(PREF_CONFIG_TIMESTAMP, System.currentTimeMillis())
                .putString(PREF_CONFIG_VERSION, getAppVersion())
                .apply();
        } catch (Exception e) {
            FLog.error("❌ Failed to cache configuration: " + e.getMessage());
        }
    }
    
    /**
     * Load fallback configuration if main config fails
     */
    private void loadFallbackConfiguration() {
        try {
            config = new JSONObject();
            
            // Basic fallback configuration
            JSONObject appConfig = new JSONObject();
            appConfig.put("version", "3.0.0");
            appConfig.put("version_code", 100);
            config.put("app_config", appConfig);
            
            JSONObject apiEndpoints = new JSONObject();
            apiEndpoints.put("main_server", "https://api.bear-loader.com");
            apiEndpoints.put("backup_server", "https://backup.bear-loader.com");
            config.put("api_endpoints", apiEndpoints);
            
            FLog.info("📋 Loaded fallback configuration");
            
        } catch (JSONException e) {
            FLog.error("❌ Failed to create fallback configuration: " + e.getMessage());
        }
    }
    
    // ============ API ENDPOINTS ============
    
    /**
     * Get main server URL (replaces ApiServer.mainURL())
     */
    public String getMainServerUrl() {
        try {
            return config.getJSONObject("api_endpoints").getString("main_server");
        } catch (JSONException e) {
            FLog.error("❌ Failed to get main server URL: " + e.getMessage());
            return "https://api.bear-loader.com";
        }
    }
    
    /**
     * Get backup server URL
     */
    public String getBackupServerUrl() {
        try {
            return config.getJSONObject("api_endpoints").getString("backup_server");
        } catch (JSONException e) {
            return "https://backup.bear-loader.com";
        }
    }
    
    /**
     * Get update server URL (replaces URLJSON())
     */
    public String getUpdateServerUrl() {
        try {
            return config.getJSONObject("api_endpoints").getString("update_server");
        } catch (JSONException e) {
            return "https://updates.bear-loader.com";
        }
    }
    
    /**
     * Get configuration server URL
     */
    public String getConfigServerUrl() {
        try {
            return config.getJSONObject("api_endpoints").getString("config_server");
        } catch (JSONException e) {
            return "https://config.bear-loader.com";
        }
    }
    
    // ============ SOCIAL LINKS ============
    
    /**
     * Get Telegram URL (replaces ApiServer.getTelegram())
     */
    public String getTelegramUrl() {
        try {
            return config.getJSONObject("social_links").getString("telegram");
        } catch (JSONException e) {
            return "https://t.me/bear_loader_support";
        }
    }
    
    /**
     * Get Discord URL
     */
    public String getDiscordUrl() {
        try {
            return config.getJSONObject("social_links").getString("discord");
        } catch (JSONException e) {
            return "https://discord.gg/bearloader";
        }
    }
    
    /**
     * Get website URL
     */
    public String getWebsiteUrl() {
        try {
            return config.getJSONObject("social_links").getString("website");
        } catch (JSONException e) {
            return "https://bear-loader.com";
        }
    }
    
    /**
     * Get support URL
     */
    public String getSupportUrl() {
        try {
            return config.getJSONObject("social_links").getString("support");
        } catch (JSONException e) {
            return "https://support.bear-loader.com";
        }
    }
    
    // ============ KEYAUTH CONFIGURATION ============
    
    /**
     * Get KeyAuth app name
     */
    public String getKeyAuthAppName() {
        try {
            return config.getJSONObject("keyauth_config").getString("app_name");
        } catch (JSONException e) {
            return "happyloader";
        }
    }
    
    /**
     * Get KeyAuth owner ID
     */
    public String getKeyAuthOwnerId() {
        try {
            return config.getJSONObject("keyauth_config").getString("owner_id");
        } catch (JSONException e) {
            return "yLoA9zcOEF";
        }
    }
    
    /**
     * Get KeyAuth app secret
     */
    public String getKeyAuthAppSecret() {
        try {
            return config.getJSONObject("keyauth_config").getString("app_secret");
        } catch (JSONException e) {
            return "37c1ed3b6ee34a5bdd9b6fbdf30d502d5e11ba43ddce22b346ae05e9d18b936c";
        }
    }
    
    /**
     * Get KeyAuth version
     */
    public String getKeyAuthVersion() {
        try {
            return config.getJSONObject("keyauth_config").getString("version");
        } catch (JSONException e) {
            return "1.3";
        }
    }
    
    // ============ UPDATE CONFIGURATION ============
    
    /**
     * Get update check interval in hours
     */
    public int getUpdateCheckInterval() {
        try {
            return config.getJSONObject("update_config").getInt("check_interval_hours");
        } catch (JSONException e) {
            return 24;
        }
    }
    
    /**
     * Check if force update is enabled
     */
    public boolean isForceUpdateEnabled() {
        try {
            return config.getJSONObject("update_config").getBoolean("force_update");
        } catch (JSONException e) {
            return false;
        }
    }
    
    /**
     * Get download URL for updates
     */
    public String getUpdateDownloadUrl() {
        try {
            return config.getJSONObject("update_config").getString("download_url");
        } catch (JSONException e) {
            return "https://downloads.bear-loader.com/latest.apk";
        }
    }
    
    // ============ SECURITY CONFIGURATION ============
    
    /**
     * Check if stealth mode is enabled
     */
    public boolean isStealthModeEnabled() {
        try {
            return config.getJSONObject("security_config").getBoolean("enable_stealth_mode");
        } catch (JSONException e) {
            return true;
        }
    }
    
    /**
     * Check if anti-debug is enabled
     */
    public boolean isAntiDebugEnabled() {
        try {
            return config.getJSONObject("security_config").getBoolean("enable_anti_debug");
        } catch (JSONException e) {
            return true;
        }
    }
    
    /**
     * Get maximum login attempts
     */
    public int getMaxLoginAttempts() {
        try {
            return config.getJSONObject("security_config").getInt("max_login_attempts");
        } catch (JSONException e) {
            return 3;
        }
    }
    
    // ============ FEATURES ============
    
    /**
     * Check if ESP is enabled
     */
    public boolean isEspEnabled() {
        try {
            return config.getJSONObject("features").getBoolean("esp_enabled");
        } catch (JSONException e) {
            return true;
        }
    }
    
    /**
     * Check if aimbot is enabled
     */
    public boolean isAimbotEnabled() {
        try {
            return config.getJSONObject("features").getBoolean("aimbot_enabled");
        } catch (JSONException e) {
            return true;
        }
    }
    
    /**
     * Check if container mode is enabled
     */
    public boolean isContainerModeEnabled() {
        try {
            return config.getJSONObject("features").getBoolean("container_mode_enabled");
        } catch (JSONException e) {
            return true;
        }
    }
    
    // ============ SUPPORTED GAMES ============
    
    /**
     * Get list of supported games
     */
    public List<GameConfig> getSupportedGames() {
        List<GameConfig> games = new ArrayList<>();
        try {
            JSONObject gamesConfig = config.getJSONObject("supported_games");
            JSONArray gameNames = gamesConfig.names();
            
            for (int i = 0; i < gameNames.length(); i++) {
                String gameName = gameNames.getString(i);
                JSONObject gameConfig = gamesConfig.getJSONObject(gameName);
                
                GameConfig game = new GameConfig();
                game.setName(gameName);
                game.setPackageName(gameConfig.getString("package_name"));
                game.setVersion(gameConfig.getString("version"));
                game.setSupported(gameConfig.getBoolean("supported"));
                
                // Parse ESP config
                JSONObject espConfig = gameConfig.getJSONObject("esp_config");
                game.setPlayerEsp(espConfig.getBoolean("player_esp"));
                game.setItemEsp(espConfig.getBoolean("item_esp"));
                game.setVehicleEsp(espConfig.getBoolean("vehicle_esp"));
                game.setDistanceEsp(espConfig.getBoolean("distance_esp"));
                
                games.add(game);
            }
        } catch (JSONException e) {
            FLog.error("❌ Failed to parse supported games: " + e.getMessage());
        }
        
        return games;
    }
    
    /**
     * Check if a specific game is supported
     */
    public boolean isGameSupported(String packageName) {
        List<GameConfig> games = getSupportedGames();
        for (GameConfig game : games) {
            if (game.getPackageName().equals(packageName)) {
                return game.isSupported();
            }
        }
        return false;
    }
    
    /**
     * Get game configuration for a specific package
     */
    public GameConfig getGameConfig(String packageName) {
        List<GameConfig> games = getSupportedGames();
        for (GameConfig game : games) {
            if (game.getPackageName().equals(packageName)) {
                return game;
            }
        }
        return null;
    }
    
    // ============ UTILITY METHODS ============
    
    /**
     * Get app version
     */
    public String getAppVersion() {
        try {
            return config.getJSONObject("app_config").getString("version");
        } catch (JSONException e) {
            return "3.0.0";
        }
    }
    
    /**
     * Get app version code
     */
    public int getAppVersionCode() {
        try {
            return config.getJSONObject("app_config").getInt("version_code");
        } catch (JSONException e) {
            return 100;
        }
    }
    
    /**
     * Refresh configuration from server (future implementation)
     */
    public void refreshConfiguration() {
        // TODO: Implement server-based configuration refresh
        FLog.info("🔄 Configuration refresh requested (not implemented yet)");
    }
    
    /**
     * Get full configuration as JSON string
     */
    public String getConfigurationJson() {
        return config.toString();
    }
    
    /**
     * Game configuration class
     */
    public static class GameConfig {
        private String name;
        private String packageName;
        private String version;
        private boolean supported;
        private boolean playerEsp;
        private boolean itemEsp;
        private boolean vehicleEsp;
        private boolean distanceEsp;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public boolean isSupported() { return supported; }
        public void setSupported(boolean supported) { this.supported = supported; }
        
        public boolean isPlayerEsp() { return playerEsp; }
        public void setPlayerEsp(boolean playerEsp) { this.playerEsp = playerEsp; }
        
        public boolean isItemEsp() { return itemEsp; }
        public void setItemEsp(boolean itemEsp) { this.itemEsp = itemEsp; }
        
        public boolean isVehicleEsp() { return vehicleEsp; }
        public void setVehicleEsp(boolean vehicleEsp) { this.vehicleEsp = vehicleEsp; }
        
        public boolean isDistanceEsp() { return distanceEsp; }
        public void setDistanceEsp(boolean distanceEsp) { this.distanceEsp = distanceEsp; }
    }
} 
