package com.happy.pro.hooks;

/**
 * Class for storing statistical information about hooks in BEAR-LOADER
 */
public class HookStatistics {
    private final int activeHookCount;
    private final int totalHookCount;
    private final int securityHookCount;
    private final int gameHookCount;
    private final long timestamp;
    
    /**
     * Constructor for HookStatistics
     * 
     * @param activeHookCount Number of currently active hooks
     * @param totalHookCount Total number of hooks (including history)
     * @param securityHookCount Number of security-related hooks
     * @param gameHookCount Number of game-related hooks
     */
    public HookStatistics(int activeHookCount, int totalHookCount, 
                         int securityHookCount, int gameHookCount) {
        this.activeHookCount = activeHookCount;
        this.totalHookCount = totalHookCount;
        this.securityHookCount = securityHookCount;
        this.gameHookCount = gameHookCount;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get the number of currently active hooks
     * 
     * @return Active hook count
     */
    public int getActiveHookCount() {
        return activeHookCount;
    }
    
    /**
     * Get the total number of hooks (including history)
     * 
     * @return Total hook count
     */
    public int getTotalHookCount() {
        return totalHookCount;
    }
    
    /**
     * Get the number of security-related hooks
     * 
     * @return Security hook count
     */
    public int getSecurityHookCount() {
        return securityHookCount;
    }
    
    /**
     * Get the number of game-related hooks
     * 
     * @return Game hook count
     */
    public int getGameHookCount() {
        return gameHookCount;
    }
    
    /**
     * Get the timestamp when these statistics were generated
     * 
     * @return Timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get the number of inactive hooks
     * 
     * @return Inactive hook count
     */
    public int getInactiveHookCount() {
        return totalHookCount - activeHookCount;
    }
    
    /**
     * Get the percentage of active hooks
     * 
     * @return Percentage as double (0.0 to 100.0)
     */
    public double getActiveHookPercentage() {
        if (totalHookCount == 0) return 0.0;
        return (double) activeHookCount / totalHookCount * 100.0;
    }
    
    /**
     * Get a formatted summary of the statistics
     * 
     * @return Formatted summary string
     */
    public String getSummary() {
        return String.format("Hooks: %d active, %d total | Security: %d | Game: %d | Active: %.1f%%",
                           activeHookCount, totalHookCount, securityHookCount, gameHookCount, 
                           getActiveHookPercentage());
    }
    
    /**
     * Check if any hooks are active
     * 
     * @return True if at least one hook is active
     */
    public boolean hasActiveHooks() {
        return activeHookCount > 0;
    }
    
    /**
     * Check if security hooks are active
     * 
     * @return True if security hooks are active
     */
    public boolean hasSecurityHooks() {
        return securityHookCount > 0;
    }
    
    /**
     * Check if game hooks are active
     * 
     * @return True if game hooks are active
     */
    public boolean hasGameHooks() {
        return gameHookCount > 0;
    }
    
    @Override
    public String toString() {
        return "HookStatistics{" +
                "activeHookCount=" + activeHookCount +
                ", totalHookCount=" + totalHookCount +
                ", securityHookCount=" + securityHookCount +
                ", gameHookCount=" + gameHookCount +
                ", timestamp=" + timestamp +
                '}';
    }
} 
