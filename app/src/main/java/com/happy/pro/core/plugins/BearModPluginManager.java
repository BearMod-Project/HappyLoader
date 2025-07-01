package com.happy.pro.core.plugins;

import android.util.Log;
import com.happy.pro.core.container.BearModContainer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 🔥 BearMod Plugin Manager
 * 
 * Container-specific plugin management system
 */
public class BearModPluginManager {
    private static final String TAG = "BearModPluginManager";
    private final BearModContainer container;
    private final Map<String, Plugin> plugins;
    private boolean isActive;
    
    public interface Plugin {
        String getName();
        String getVersion();
        boolean initialize();
        void shutdown();
        boolean isEnabled();
    }
    
    public BearModPluginManager(BearModContainer container) {
        this.container = container;
        this.plugins = new ConcurrentHashMap<>();
        this.isActive = false;
    }
    
    public boolean initialize() {
        isActive = true;
        Log.d(TAG, "Plugin manager initialized for container: " + container.getId());
        return true;
    }
    
    public boolean registerPlugin(String name, Plugin plugin) {
        if (!isActive || name == null || plugin == null) {
            return false;
        }
        
        if (plugins.containsKey(name)) {
            Log.w(TAG, "Plugin already registered: " + name);
            return false;
        }
        
        try {
            if (plugin.initialize()) {
                plugins.put(name, plugin);
                Log.d(TAG, "Plugin registered: " + name + " v" + plugin.getVersion());
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize plugin: " + name, e);
        }
        
        return false;
    }
    
    public boolean unregisterPlugin(String name) {
        if (!isActive || name == null) {
            return false;
        }
        
        Plugin plugin = plugins.remove(name);
        if (plugin != null) {
            try {
                plugin.shutdown();
                Log.d(TAG, "Plugin unregistered: " + name);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error shutting down plugin: " + name, e);
            }
        }
        
        return false;
    }
    
    public Plugin getPlugin(String name) {
        if (!isActive || name == null) {
            return null;
        }
        
        return plugins.get(name);
    }
    
    public boolean isPluginRegistered(String name) {
        return isActive && plugins.containsKey(name);
    }
    
    public Set<String> getRegisteredPluginNames() {
        return isActive ? plugins.keySet() : Set.of();
    }
    
    public int getPluginCount() {
        return isActive ? plugins.size() : 0;
    }
    
    /**
     * Cleanup the plugin manager
     */
    public void cleanup() {
        shutdown();
    }
    
    public void shutdown() {
        for (Map.Entry<String, Plugin> entry : plugins.entrySet()) {
            try {
                entry.getValue().shutdown();
                Log.d(TAG, "Plugin shutdown: " + entry.getKey());
            } catch (Exception e) {
                Log.e(TAG, "Error shutting down plugin: " + entry.getKey(), e);
            }
        }
        
        plugins.clear();
        isActive = false;
        Log.d(TAG, "Plugin manager shutdown for container: " + container.getId());
    }
    
    public BearModContainer getContainer() {
        return container;
    }
    
    public boolean isActive() {
        return isActive;
    }
} 
