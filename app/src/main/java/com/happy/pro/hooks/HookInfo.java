package com.happy.pro.hooks;

/**
 * Class for storing information about a hook in BEAR-LOADER
 * Provides comprehensive tracking and management of active hooks
 */
public class HookInfo {
    private final String libraryName;
    private final String functionName;
    private final HookType hookType;
    private final long timestamp;
    private final String targetAddress;
    private final String replacementAddress;
    private final boolean isActive;
    private final String description;
    
    /**
     * Constructor for basic hook information
     * 
     * @param libraryName Name of the library containing the function
     * @param functionName Name of the function
     * @param hookType Type of hook
     */
    public HookInfo(String libraryName, String functionName, HookType hookType) {
        this(libraryName, functionName, hookType, null, null, true, null);
    }
    
    /**
     * Constructor for detailed hook information
     * 
     * @param libraryName Name of the library containing the function
     * @param functionName Name of the function
     * @param hookType Type of hook
     * @param targetAddress Memory address of target function
     * @param replacementAddress Memory address of replacement function
     * @param isActive Whether the hook is currently active
     * @param description Optional description of the hook
     */
    public HookInfo(String libraryName, String functionName, HookType hookType,
                   String targetAddress, String replacementAddress, boolean isActive, String description) {
        this.libraryName = libraryName != null ? libraryName : "Unknown";
        this.functionName = functionName != null ? functionName : "Unknown";
        this.hookType = hookType != null ? hookType : HookType.UNKNOWN;
        this.targetAddress = targetAddress;
        this.replacementAddress = replacementAddress;
        this.isActive = isActive;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get the library name
     * 
     * @return Library name
     */
    public String getLibraryName() {
        return libraryName;
    }
    
    /**
     * Get the function name
     * 
     * @return Function name
     */
    public String getFunctionName() {
        return functionName;
    }
    
    /**
     * Get the hook type
     * 
     * @return Hook type
     */
    public HookType getHookType() {
        return hookType;
    }
    
    /**
     * Get the timestamp when the hook was created
     * 
     * @return Timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get the target function address
     * 
     * @return Target address as string, null if not available
     */
    public String getTargetAddress() {
        return targetAddress;
    }
    
    /**
     * Get the replacement function address
     * 
     * @return Replacement address as string, null if not available
     */
    public String getReplacementAddress() {
        return replacementAddress;
    }
    
    /**
     * Check if the hook is currently active
     * 
     * @return True if active, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Get the hook description
     * 
     * @return Description, null if not provided
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get a formatted timestamp string
     * 
     * @return Formatted timestamp
     */
    public String getFormattedTimestamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date(timestamp));
    }
    
    /**
     * Get the full qualified name (library + function)
     * 
     * @return Full qualified name
     */
    public String getFullQualifiedName() {
        return libraryName + "::" + functionName;
    }
    
    /**
     * Check if this hook is security-related
     * 
     * @return True if security-related
     */
    public boolean isSecurityHook() {
        return hookType.isSecurityRelated();
    }
    
    /**
     * Check if this hook is game-related
     * 
     * @return True if game-related
     */
    public boolean isGameHook() {
        return hookType.isGameRelated();
    }
    
    /**
     * Create a copy of this HookInfo with updated active status
     * 
     * @param newActiveStatus New active status
     * @return New HookInfo instance
     */
    public HookInfo withActiveStatus(boolean newActiveStatus) {
        return new HookInfo(libraryName, functionName, hookType, 
                           targetAddress, replacementAddress, newActiveStatus, description);
    }
    
    /**
     * Get a summary string for logging
     * 
     * @return Summary string
     */
    public String getSummary() {
        return String.format("[%s] %s::%s (%s) - %s", 
                           hookType.getDisplayName(),
                           libraryName, 
                           functionName,
                           isActive ? "ACTIVE" : "INACTIVE",
                           getFormattedTimestamp());
    }
    
    @Override
    public String toString() {
        return "HookInfo{" +
                "libraryName='" + libraryName + '\'' +
                ", functionName='" + functionName + '\'' +
                ", hookType=" + hookType +
                ", timestamp=" + timestamp +
                ", targetAddress='" + targetAddress + '\'' +
                ", replacementAddress='" + replacementAddress + '\'' +
                ", isActive=" + isActive +
                ", description='" + description + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        HookInfo hookInfo = (HookInfo) o;
        
        if (!libraryName.equals(hookInfo.libraryName)) return false;
        if (!functionName.equals(hookInfo.functionName)) return false;
        return hookType == hookInfo.hookType;
    }
    
    @Override
    public int hashCode() {
        int result = libraryName.hashCode();
        result = 31 * result + functionName.hashCode();
        result = 31 * result + hookType.hashCode();
        return result;
    }
} 
