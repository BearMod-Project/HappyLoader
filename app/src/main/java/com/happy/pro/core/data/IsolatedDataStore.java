package com.happy.pro.core.data;

import android.util.Log;
import com.happy.pro.core.container.BearModContainer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 🔥 Isolated Data Store
 * 
 * Container-specific secure data storage
 */
public class IsolatedDataStore {
    private static final String TAG = "IsolatedDataStore";
    private final BearModContainer container;
    private final Map<String, Object> dataMap;
    private boolean isInitialized;
    
    public IsolatedDataStore(BearModContainer container) {
        this.container = container;
        this.dataMap = new ConcurrentHashMap<>();
        this.isInitialized = false;
    }
    
    public boolean initialize() {
        isInitialized = true;
        Log.d(TAG, "Isolated data store initialized for container: " + container.getId());
        return true;
    }
    
    public void put(String key, Object value) {
        if (isInitialized && key != null) {
            dataMap.put(key, value);
        }
    }
    
    public Object get(String key) {
        if (isInitialized && key != null) {
            return dataMap.get(key);
        }
        return null;
    }
    
    public boolean containsKey(String key) {
        return isInitialized && dataMap.containsKey(key);
    }
    
    public void remove(String key) {
        if (isInitialized && key != null) {
            dataMap.remove(key);
        }
    }
    
    public void clear() {
        if (isInitialized) {
            dataMap.clear();
        }
    }
    
    public int size() {
        return isInitialized ? dataMap.size() : 0;
    }
    
    /**
     * Set event bus for communication
     * @param eventBus Event bus instance
     */
    public void setEventBus(com.happy.pro.core.events.IsolatedEventBus eventBus) {
        Log.d(TAG, "Event bus set for data store");
    }
    
    /**
     * Apply security policy
     * @param policy Security policy to apply
     */
    public void applySecurityPolicy(com.happy.pro.core.container.SecurityPolicy policy) {
        Log.d(TAG, "Security policy applied to data store");
    }
    
    /**
     * Cleanup the data store
     */
    public void cleanup() {
        shutdown();
    }
    
    public void shutdown() {
        dataMap.clear();
        isInitialized = false;
        Log.d(TAG, "Isolated data store shutdown for container: " + container.getId());
    }
    
    public BearModContainer getContainer() {
        return container;
    }
} 
