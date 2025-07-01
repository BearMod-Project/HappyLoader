package com.happy.pro.utils;

import android.content.Context;
import android.widget.Toast;

import com.happy.pro.server.KeyAuthDownloadManager;
import com.happy.pro.config.AppConfigManager;

import java.io.File;

/**
 * Download Utilities for BEAR-LOADER
 * 
 * Provides convenient wrapper methods for common download operations
 * using the KeyAuth Download Manager.
 * 
 * @author BEAR-LOADER Team
 * @version 3.0.0
 */
public class DownloadUtils {
    
    /**
     * Download game resources for a specific package
     */
    public static void downloadGameResources(Context context, String packageName) {
        KeyAuthDownloadManager downloadManager = KeyAuthDownloadManager.getInstance(context);
        
        // Create callback for handling download events
        KeyAuthDownloadManager.DownloadCallback callback = new KeyAuthDownloadManager.DownloadCallback() {
            @Override
            public void onStart(String downloadId) {
                FLog.info("🚀 Started downloading resources for: " + packageName);
                Toast.makeText(context, "Downloading game resources...", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onProgress(KeyAuthDownloadManager.DownloadProgress progress) {
                FLog.info("📊 Download progress: " + progress.progressPercent + "% - " + progress.statusMessage);
            }
            
            @Override
            public void onComplete(String downloadId, File file) {
                FLog.info("✅ Download completed: " + file.getName());
                Toast.makeText(context, "Download completed successfully!", Toast.LENGTH_SHORT).show();
                
                // Extract if it's a zip file
                if (file.getName().endsWith(".zip")) {
                    extractZipFile(file, file.getParentFile());
                }
            }
            
            @Override
            public void onError(String downloadId, String error) {
                FLog.error("❌ Download failed: " + error);
                Toast.makeText(context, "Download failed: " + error, Toast.LENGTH_LONG).show();
            }
        };
        
        // Download both libraries and loader
        downloadManager.downloadGameLibraries(packageName, callback);
        downloadManager.downloadGameLoader(packageName, callback);
    }
    
    /**
     * Download only game libraries
     */
    public static void downloadGameLibraries(Context context, String packageName) {
        KeyAuthDownloadManager downloadManager = KeyAuthDownloadManager.getInstance(context);
        
        KeyAuthDownloadManager.DownloadCallback callback = new KeyAuthDownloadManager.DownloadCallback() {
            @Override
            public void onStart(String downloadId) {
                FLog.info("📚 Downloading libraries for: " + packageName);
            }
            
            @Override
            public void onProgress(KeyAuthDownloadManager.DownloadProgress progress) {
                // Progress updates handled by UI
            }
            
            @Override
            public void onComplete(String downloadId, File file) {
                FLog.info("✅ Libraries downloaded: " + file.getName());
                Toast.makeText(context, "Libraries updated successfully!", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onError(String downloadId, String error) {
                FLog.error("❌ Library download failed: " + error);
                Toast.makeText(context, "Failed to download libraries: " + error, Toast.LENGTH_LONG).show();
            }
        };
        
        downloadManager.downloadGameLibraries(packageName, callback);
    }
    
    /**
     * Download only game loader
     */
    public static void downloadGameLoader(Context context, String packageName) {
        KeyAuthDownloadManager downloadManager = KeyAuthDownloadManager.getInstance(context);
        
        KeyAuthDownloadManager.DownloadCallback callback = new KeyAuthDownloadManager.DownloadCallback() {
            @Override
            public void onStart(String downloadId) {
                FLog.info("🎮 Downloading loader for: " + packageName);
            }
            
            @Override
            public void onProgress(KeyAuthDownloadManager.DownloadProgress progress) {
                // Progress updates handled by UI
            }
            
            @Override
            public void onComplete(String downloadId, File file) {
                FLog.info("✅ Loader downloaded: " + file.getName());
                Toast.makeText(context, "Loader downloaded successfully!", Toast.LENGTH_SHORT).show();
                
                // Extract loader zip
                if (file.getName().endsWith(".zip")) {
                    extractZipFile(file, new File(context.getFilesDir(), "loader"));
                }
            }
            
            @Override
            public void onError(String downloadId, String error) {
                FLog.error("❌ Loader download failed: " + error);
                Toast.makeText(context, "Failed to download loader: " + error, Toast.LENGTH_LONG).show();
            }
        };
        
        downloadManager.downloadGameLoader(packageName, callback);
    }
    
    /**
     * Download app update
     */
    public static void downloadAppUpdate(Context context) {
        KeyAuthDownloadManager downloadManager = KeyAuthDownloadManager.getInstance(context);
        AppConfigManager configManager = AppConfigManager.getInstance(context);
        
        // Get update info from config
        String latestVersion = configManager.getLatestVersion();
        String updateFileId = "420381"; // KeyAuth file ID for app update (replace with actual ID)
        
        KeyAuthDownloadManager.DownloadCallback callback = new KeyAuthDownloadManager.DownloadCallback() {
            @Override
            public void onStart(String downloadId) {
                FLog.info("⬆️ Downloading app update: " + latestVersion);
                Toast.makeText(context, "Downloading update...", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onProgress(KeyAuthDownloadManager.DownloadProgress progress) {
                FLog.info("📊 Update download: " + progress.progressPercent + "%");
            }
            
            @Override
            public void onComplete(String downloadId, File file) {
                FLog.info("✅ Update downloaded: " + file.getName());
                Toast.makeText(context, "Update downloaded! Tap to install.", Toast.LENGTH_LONG).show();
                
                // Trigger installation
                installApkUpdate(context, file);
            }
            
            @Override
            public void onError(String downloadId, String error) {
                FLog.error("❌ Update download failed: " + error);
                Toast.makeText(context, "Failed to download update: " + error, Toast.LENGTH_LONG).show();
            }
        };
        
        downloadManager.downloadAppUpdate(latestVersion, updateFileId, callback);
    }
    
    /**
     * Download custom file with KeyAuth authentication
     */
    public static void downloadCustomFile(Context context, String fileName, String keyAuthFileId, String targetPath, 
                                        KeyAuthDownloadManager.DownloadCallback callback) {
        KeyAuthDownloadManager downloadManager = KeyAuthDownloadManager.getInstance(context);
        
        KeyAuthDownloadManager.DownloadItem item = new KeyAuthDownloadManager.DownloadItem(
            "custom_" + fileName,
            fileName,
            keyAuthFileId,
            targetPath,
            KeyAuthDownloadManager.DownloadType.CUSTOM
        );
        
        downloadManager.downloadWithUI(item, callback);
    }
    
    /**
     * Download configuration files
     */
    public static void downloadConfigurations(Context context) {
        KeyAuthDownloadManager downloadManager = KeyAuthDownloadManager.getInstance(context);
        
        // Download main config using KeyAuth file ID
        String configFileId = "420371"; // Use the keyauth_config.json file ID from your dashboard
        String configPath = context.getFilesDir() + "/config/keyauth_config.json";
        
        KeyAuthDownloadManager.DownloadItem configItem = new KeyAuthDownloadManager.DownloadItem(
            "main_config",
            "KeyAuth Configuration",
            configFileId,
            configPath,
            KeyAuthDownloadManager.DownloadType.CONFIG
        );
        
        KeyAuthDownloadManager.DownloadCallback callback = new KeyAuthDownloadManager.DownloadCallback() {
            @Override
            public void onStart(String downloadId) {
                FLog.info("⚙️ Downloading configurations...");
            }
            
            @Override
            public void onProgress(KeyAuthDownloadManager.DownloadProgress progress) {
                // Progress updates
            }
            
            @Override
            public void onComplete(String downloadId, File file) {
                FLog.info("✅ Configuration downloaded: " + file.getName());
                Toast.makeText(context, "Configuration updated!", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onError(String downloadId, String error) {
                FLog.error("❌ Configuration download failed: " + error);
            }
        };
        
        downloadManager.downloadInBackground(configItem, callback);
    }
    
    /**
     * Check and download missing resources
     */
    public static void checkAndDownloadMissingResources(Context context, String packageName) {
        File libsDir = new File(context.getFilesDir(), "libs");
        File loaderDir = new File(context.getFilesDir(), "loader");
        
        boolean needsLibraries = !libsDir.exists() || libsDir.listFiles().length == 0;
        boolean needsLoader = !loaderDir.exists() || loaderDir.listFiles().length == 0;
        
        if (needsLibraries || needsLoader) {
            FLog.info("📋 Missing resources detected, downloading...");
            
            if (needsLibraries) {
                downloadGameLibraries(context, packageName);
            }
            
            if (needsLoader) {
                downloadGameLoader(context, packageName);
            }
        } else {
            FLog.info("✅ All resources are available");
            Toast.makeText(context, "All resources are up to date!", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Cancel all active downloads
     */
    public static void cancelAllDownloads(Context context) {
        KeyAuthDownloadManager downloadManager = KeyAuthDownloadManager.getInstance(context);
        
        for (String downloadId : downloadManager.getActiveDownloads().keySet()) {
            downloadManager.cancelDownload(downloadId);
        }
        
        FLog.info("🚫 All downloads cancelled");
        Toast.makeText(context, "Downloads cancelled", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Get download status information
     */
    public static String getDownloadStatus(Context context) {
        KeyAuthDownloadManager downloadManager = KeyAuthDownloadManager.getInstance(context);
        int activeDownloads = downloadManager.getActiveDownloads().size();
        
        if (activeDownloads == 0) {
            return "No active downloads";
        } else {
            return activeDownloads + " download(s) in progress";
        }
    }
    
    // Helper Methods
    
    private static void extractZipFile(File zipFile, File targetDir) {
        try {
            FLog.info("📦 Extracting: " + zipFile.getName());
            
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            
            // Use the existing Zip utility from the project
            com.happy.pro.Component.Zip.unzip(zipFile, targetDir);
            
            // Clean up zip file after extraction
            if (zipFile.exists()) {
                zipFile.delete();
            }
            
            FLog.info("✅ Extraction completed: " + zipFile.getName());
            
        } catch (Exception e) {
            FLog.error("❌ Extraction failed: " + e.getMessage());
        }
    }
    
    private static void installApkUpdate(Context context, File apkFile) {
        try {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            android.net.Uri apkUri;
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                apkUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    apkFile
                );
                intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                apkUri = android.net.Uri.fromFile(apkFile);
            }
            
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            
        } catch (Exception e) {
            FLog.error("❌ Failed to install update: " + e.getMessage());
            Toast.makeText(context, "Failed to install update", Toast.LENGTH_LONG).show();
        }
    }
} 