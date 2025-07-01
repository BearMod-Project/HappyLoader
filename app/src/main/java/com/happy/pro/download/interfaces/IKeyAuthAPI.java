package com.happy.pro.download.interfaces;

/**
 * Interface for KeyAuth API operations
 * 
 * @author BEAR-LOADER Team
 * @version 3.0.0
 */
public interface IKeyAuthAPI {
    
    /**
     * Download file from KeyAuth using file ID
     * @param fileId KeyAuth file ID
     * @return File bytes or null if failed
     */
    byte[] downloadFile(String fileId);
    
    /**
     * Check if user is authenticated
     */
    boolean isAuthenticated();
    
    /**
     * Get current session ID
     */
    String getSessionId();
    
    /**
     * Get app name
     */
    String getAppName();
    
    /**
     * Get owner ID  
     */
    String getOwnerId();
    
    /**
     * KeyAuth download result
     */
    class DownloadResult {
        public final boolean success;
        public final byte[] data;
        public final String error;
        
        public DownloadResult(boolean success, byte[] data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }
        
        public static DownloadResult success(byte[] data) {
            return new DownloadResult(true, data, null);
        }
        
        public static DownloadResult error(String error) {
            return new DownloadResult(false, null, error);
        }
    }
} 