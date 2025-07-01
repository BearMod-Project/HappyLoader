package com.happy.pro.download.interfaces;

import java.io.File;
import java.util.Map;

/**
 * Interface for secure download management with KeyAuth integration
 * 
 * @author BEAR-LOADER Team
 * @version 3.0.0
 */
public interface IDownloadManager {
    
    /**
     * Download file with UI progress dialog
     */
    void downloadWithUI(DownloadRequest request, DownloadCallback callback);
    
    /**
     * Download file in background (no UI)
     */
    void downloadInBackground(DownloadRequest request, DownloadCallback callback);
    
    /**
     * Cancel active download
     */
    void cancelDownload(String downloadId);
    
    /**
     * Get active download count
     */
    int getActiveDownloadCount();
    
    /**
     * Check if download manager is ready
     */
    boolean isReady();
    
    /**
     * Clear download cache
     */
    void clearCache();
    
    /**
     * Download request configuration
     */
    class DownloadRequest {
        public final String id;
        public final String name;
        public final String keyAuthFileId;
        public final String targetPath;
        public final DownloadType type;
        public String expectedHash;
        public long expectedSize;
        
        public DownloadRequest(String id, String name, String keyAuthFileId, String targetPath, DownloadType type) {
            this.id = id;
            this.name = name;
            this.keyAuthFileId = keyAuthFileId;
            this.targetPath = targetPath;
            this.type = type;
        }
        
        public DownloadRequest withHash(String hash) {
            this.expectedHash = hash;
            return this;
        }
        
        public DownloadRequest withSize(long size) {
            this.expectedSize = size;
            return this;
        }
    }
    
    /**
     * Download types
     */
    enum DownloadType {
        LIBRARY, CONFIG, ASSETS, UPDATE, PATCH
    }
    
    /**
     * Download status
     */
    enum DownloadStatus {
        QUEUED, DOWNLOADING, VERIFYING, COMPLETED, FAILED, CANCELLED
    }
    
    /**
     * Download progress information
     */
    class DownloadProgress {
        public final String downloadId;
        public final String fileName;
        public final long totalBytes;
        public final long downloadedBytes;
        public final int progressPercent;
        public final DownloadStatus status;
        public final String statusMessage;
        public final long downloadSpeed;
        
        public DownloadProgress(String downloadId, String fileName, long totalBytes, 
                              long downloadedBytes, DownloadStatus status, String statusMessage) {
            this.downloadId = downloadId;
            this.fileName = fileName;
            this.totalBytes = totalBytes;
            this.downloadedBytes = downloadedBytes;
            this.progressPercent = totalBytes > 0 ? (int) ((downloadedBytes * 100) / totalBytes) : 0;
            this.status = status;
            this.statusMessage = statusMessage;
            this.downloadSpeed = 0; // Calculated by implementation
        }
    }
    
    /**
     * Download callback interface
     */
    interface DownloadCallback {
        void onStart(String downloadId);
        void onProgress(DownloadProgress progress);
        void onComplete(String downloadId, File file);
        void onError(String downloadId, String error);
    }
} 