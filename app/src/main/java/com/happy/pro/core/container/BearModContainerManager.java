package com.happy.pro.core.container;

import android.util.Log;

import com.happy.pro.core.auth.HostContext;
import com.happy.pro.core.config.BearModConfiguration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

/**
 * Container manager for multi-tenant BearMod AAR support
 * Provides isolated execution environments for different host applications
 */
public class BearModContainerManager {
    
    private static final String TAG = "BearModContainerManager";
    
    private final Map<String, BearModContainer> containers;
    private final Map<String, String> hostToContainerMapping;
    private final ReentrantReadWriteLock containerLock;
    
    public BearModContainerManager() {
        this.containers = new ConcurrentHashMap<>();
        this.hostToContainerMapping = new ConcurrentHashMap<>();
        this.containerLock = new ReentrantReadWriteLock();
    }
    
    /**
     * Create a new isolated container for a host application
     */
    public BearModContainer createContainer(HostContext hostContext, ContainerConfig config) {
        containerLock.writeLock().lock();
        try {
            // Check if container already exists for this host
            String existingContainerId = hostToContainerMapping.get(hostContext.getHostId());
            if (existingContainerId != null) {
                BearModContainer existingContainer = containers.get(existingContainerId);
                if (existingContainer != null && !existingContainer.isDestroyed()) {
                    Log.i(TAG, "Returning existing container for host: " + hostContext.getHostId());
                    return existingContainer;
                }
            }
            
            // Generate unique container ID
            String containerId = generateContainerId(hostContext);
            
            // Create new container
            BearModContainer container = new BearModContainer(
                containerId,
                hostContext,
                config
            );
            
            // Initialize container
            InitializationResult initResult = container.initialize();
            if (!initResult.isSuccess()) {
                Log.e(TAG, "Failed to initialize container: " + initResult.getErrorMessage());
                throw new RuntimeException("Container initialization failed: " + initResult.getErrorMessage());
            }
            
            // Store container
            containers.put(containerId, container);
            hostToContainerMapping.put(hostContext.getHostId(), containerId);
            
            Log.i(TAG, "Created new container: " + containerId + " for host: " + hostContext.getHostId());
            return container;
            
        } finally {
            containerLock.writeLock().unlock();
        }
    }
    
    /**
     * Get container by ID
     */
    public BearModContainer getContainer(String containerId) {
        containerLock.readLock().lock();
        try {
            return containers.get(containerId);
        } finally {
            containerLock.readLock().unlock();
        }
    }
    
    /**
     * Get container for specific host
     */
    public BearModContainer getContainerForHost(String hostId) {
        containerLock.readLock().lock();
        try {
            String containerId = hostToContainerMapping.get(hostId);
            return containerId != null ? containers.get(containerId) : null;
        } finally {
            containerLock.readLock().unlock();
        }
    }
    
    /**
     * Destroy container and cleanup resources
     */
    public void destroyContainer(String containerId) {
        containerLock.writeLock().lock();
        try {
            BearModContainer container = containers.remove(containerId);
            if (container != null) {
                // Remove from host mapping
                hostToContainerMapping.entrySet().removeIf(entry -> 
                    entry.getValue().equals(containerId));
                
                // Cleanup container resources
                container.cleanup();
                
                Log.i(TAG, "Destroyed container: " + containerId);
            }
        } finally {
            containerLock.writeLock().unlock();
        }
    }
    
    /**
     * Destroy all containers for a specific host
     */
    public void destroyContainersForHost(String hostId) {
        containerLock.writeLock().lock();
        try {
            String containerId = hostToContainerMapping.remove(hostId);
            if (containerId != null) {
                BearModContainer container = containers.remove(containerId);
                if (container != null) {
                    container.cleanup();
                    Log.i(TAG, "Destroyed container for host: " + hostId);
                }
            }
        } finally {
            containerLock.writeLock().unlock();
        }
    }
    
    /**
     * Get all active containers
     */
    public Set<BearModContainer> getActiveContainers() {
        containerLock.readLock().lock();
        try {
            return new HashSet<>(containers.values());
        } finally {
            containerLock.readLock().unlock();
        }
    }
    
    /**
     * Get container statistics
     */
    public ContainerStatistics getStatistics() {
        containerLock.readLock().lock();
        try {
            int totalContainers = containers.size();
            int activeContainers = 0;
            int destroyedContainers = 0;
            
            for (BearModContainer container : containers.values()) {
                if (container.isDestroyed()) {
                    destroyedContainers++;
                } else {
                    activeContainers++;
                }
            }
            
            return new ContainerStatistics(totalContainers, activeContainers, destroyedContainers);
        } finally {
            containerLock.readLock().unlock();
        }
    }
    
    /**
     * Cleanup all containers (for shutdown)
     */
    public void cleanup() {
        containerLock.writeLock().lock();
        try {
            Log.i(TAG, "Cleaning up all containers");
            
            for (BearModContainer container : containers.values()) {
                try {
                    container.cleanup();
                } catch (Exception e) {
                    Log.e(TAG, "Error cleaning up container: " + container.getId(), e);
                }
            }
            
            containers.clear();
            hostToContainerMapping.clear();
            
            Log.i(TAG, "All containers cleaned up");
        } finally {
            containerLock.writeLock().unlock();
        }
    }
    
    /**
     * Generate unique container ID
     */
    private String generateContainerId(HostContext hostContext) {
        String prefix = "bearmod_container_";
        String hostPrefix = hostContext.getPackageName().replaceAll("[^a-zA-Z0-9]", "_");
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        
        return prefix + hostPrefix + "_" + uniqueId;
    }
}

/**
 * Container isolation levels
 */
enum IsolationLevel {
    BASIC,      // Basic process isolation
    MEDIUM,     // Process + data isolation
    FULL        // Complete isolation with separate security contexts
}

/**
 * Container statistics
 */
class ContainerStatistics {
    private final int totalContainers;
    private final int activeContainers;
    private final int destroyedContainers;
    
    public ContainerStatistics(int totalContainers, int activeContainers, int destroyedContainers) {
        this.totalContainers = totalContainers;
        this.activeContainers = activeContainers;
        this.destroyedContainers = destroyedContainers;
    }
    
    // Getters
    public int getTotalContainers() { return totalContainers; }
    public int getActiveContainers() { return activeContainers; }
    public int getDestroyedContainers() { return destroyedContainers; }
    
    @Override
    public String toString() {
        return String.format("ContainerStatistics{total=%d, active=%d, destroyed=%d}", 
                           totalContainers, activeContainers, destroyedContainers);
    }
}

/**
 * Container initialization result
 */
class InitializationResult {
    private final boolean success;
    private final String errorMessage;
    private final Exception exception;
    private final BearModContainer container;
    
    private InitializationResult(boolean success, String errorMessage, 
                               Exception exception, BearModContainer container) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.exception = exception;
        this.container = container;
    }
    
    public static InitializationResult success(BearModContainer container) {
        return new InitializationResult(true, null, null, container);
    }
    
    public static InitializationResult failure(String errorMessage) {
        return new InitializationResult(false, errorMessage, null, null);
    }
    
    public static InitializationResult failure(String errorMessage, Exception exception) {
        return new InitializationResult(false, errorMessage, exception, null);
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    public Exception getException() { return exception; }
    public BearModContainer getContainer() { return container; }
} 
