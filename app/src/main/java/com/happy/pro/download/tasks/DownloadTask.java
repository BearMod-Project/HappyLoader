package com.happy.pro.download.tasks;

import android.content.Context;

import com.happy.pro.download.interfaces.IDownloadManager;
import com.happy.pro.download.interfaces.IKeyAuthAPI;
import com.happy.pro.utils.FLog;

import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;

/**
 * Download task executor
 * 
 * @author BEAR-LOADER Team  
 * @version 3.0.0
 */
public class DownloadTask {
    
    private static final String TAG = "DownloadTask";
    private static final int BUFFER_SIZE = 8192;
    
    private final Context context;
    private final IKeyAuthAPI keyAuthAPI;
    private final IDownloadManager.DownloadRequest request;
    private final IDownloadManager.DownloadCallback callback;
    
    private volatile boolean cancelled = false;
    
    public DownloadTask(Context context, IKeyAuthAPI keyAuthAPI, 
                       IDownloadManager.DownloadRequest request,
                       IDownloadManager.DownloadCallback callback) {
        this.context = context;
        this.keyAuthAPI = keyAuthAPI;
        this.request = request;
        this.callback = callback;
    }
    
    public void execute() {
        try {
            FLog.info("📥 Starting download: " + request.name);
            
            if (callback != null) {
                callback.onStart(request.id);
            }
            
            // Download file from KeyAuth
            byte[] fileData = keyAuthAPI.downloadFile(request.keyAuthFileId);
            
            if (fileData == null || fileData.length == 0) {
                throw new Exception("Failed to download file or file is empty");
            }
            
            if (cancelled) return;
            
            // Verify size if expected
            if (request.expectedSize > 0 && fileData.length != request.expectedSize) {
                FLog.warning("⚠️ Size mismatch: expected " + request.expectedSize + ", got " + fileData.length);
            }
            
            // Write file with progress updates
            File outputFile = writeFileWithProgress(fileData);
            
            if (cancelled) {
                outputFile.delete();
                return;
            }
            
            // Verify hash if expected
            if (request.expectedHash != null && !request.expectedHash.isEmpty()) {
                updateProgress(IDownloadManager.DownloadStatus.VERIFYING, "Verifying file integrity...", 95);
                
                String actualHash = calculateHash(outputFile);
                if (!actualHash.equals(request.expectedHash)) {
                    outputFile.delete();
                    throw new Exception("File integrity check failed");
                }
            }
            
            // Set permissions for executables
            if (request.type == IDownloadManager.DownloadType.LIBRARY) {
                outputFile.setExecutable(true);
            }
            
            FLog.info("✅ Download completed: " + request.name);
            
            if (callback != null) {
                callback.onComplete(request.id, outputFile);
            }
            
        } catch (Exception e) {
            FLog.error("❌ Download failed: " + e.getMessage());
            
            if (callback != null) {
                callback.onError(request.id, e.getMessage());
            }
        }
    }
    
    public void cancel() {
        cancelled = true;
        FLog.info("🚫 Cancelling download: " + request.id);
    }
    
    private File writeFileWithProgress(byte[] data) throws Exception {
        File outputFile = new File(request.targetPath);
        
        // Create parent directories
        File parentDir = outputFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            int totalSize = data.length;
            int written = 0;
            int chunkSize = Math.min(BUFFER_SIZE, totalSize);
            
            while (written < totalSize && !cancelled) {
                int remaining = totalSize - written;
                int writeSize = Math.min(chunkSize, remaining);
                
                fos.write(data, written, writeSize);
                written += writeSize;
                
                int progress = (written * 100) / totalSize;
                updateProgress(IDownloadManager.DownloadStatus.DOWNLOADING, 
                             "Writing file... " + formatBytes(written) + "/" + formatBytes(totalSize),
                             progress);
                
                // Small delay for UI updates on fast writes
                if (totalSize > 1024 * 1024) { // Only for files > 1MB
                    Thread.sleep(10);
                }
            }
        }
        
        return outputFile;
    }
    
    private void updateProgress(IDownloadManager.DownloadStatus status, String message, int percent) {
        if (callback != null) {
            IDownloadManager.DownloadProgress progress = new IDownloadManager.DownloadProgress(
                request.id,
                request.name,
                request.expectedSize > 0 ? request.expectedSize : 0,
                (request.expectedSize * percent) / 100,
                status,
                message
            );
            
            callback.onProgress(progress);
        }
    }
    
    private String calculateHash(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }
        
        byte[] hash = md.digest();
        StringBuilder hexString = new StringBuilder();
        
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        
        return hexString.toString();
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
} 