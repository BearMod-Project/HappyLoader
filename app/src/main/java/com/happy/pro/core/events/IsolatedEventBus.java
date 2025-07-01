package com.happy.pro.core.events;

import android.util.Log;
import com.happy.pro.core.container.BearModContainer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Map;

/**
 * 🔥 Isolated Event Bus
 * 
 * Container-specific event messaging system
 */
public class IsolatedEventBus {
    private static final String TAG = "IsolatedEventBus";
    private final BearModContainer container;
    private final Map<Class<?>, List<EventListener>> listeners;
    private boolean isActive;
    
    public interface EventListener {
        void onEvent(Object event);
    }
    
    public IsolatedEventBus(BearModContainer container) {
        this.container = container;
        this.listeners = new ConcurrentHashMap<>();
        this.isActive = false;
    }
    
    public boolean initialize() {
        isActive = true;
        Log.d(TAG, "Isolated event bus initialized for container: " + container.getId());
        return true;
    }
    
    public void subscribe(Class<?> eventType, EventListener listener) {
        if (!isActive || eventType == null || listener == null) {
            return;
        }
        
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        Log.d(TAG, "Subscribed to event type: " + eventType.getSimpleName());
    }
    
    public void unsubscribe(Class<?> eventType, EventListener listener) {
        if (!isActive || eventType == null || listener == null) {
            return;
        }
        
        List<EventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            if (eventListeners.isEmpty()) {
                listeners.remove(eventType);
            }
        }
    }
    
    public void post(Object event) {
        if (!isActive || event == null) {
            return;
        }
        
        Class<?> eventType = event.getClass();
        List<EventListener> eventListeners = listeners.get(eventType);
        
        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    Log.e(TAG, "Error delivering event to listener", e);
                }
            }
        }
    }
    
    public int getListenerCount(Class<?> eventType) {
        if (!isActive || eventType == null) {
            return 0;
        }
        
        List<EventListener> eventListeners = listeners.get(eventType);
        return eventListeners != null ? eventListeners.size() : 0;
    }
    
    public int getTotalListenerCount() {
        return isActive ? listeners.values().stream().mapToInt(List::size).sum() : 0;
    }
    
    /**
     * Cleanup the event bus
     */
    public void cleanup() {
        shutdown();
    }
    
    public void shutdown() {
        listeners.clear();
        isActive = false;
        Log.d(TAG, "Isolated event bus shutdown for container: " + container.getId());
    }
    
    public BearModContainer getContainer() {
        return container;
    }
    
    public boolean isActive() {
        return isActive;
    }
} 
