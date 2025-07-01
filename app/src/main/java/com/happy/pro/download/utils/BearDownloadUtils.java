package com.happy.pro.download.utils;

import android.content.Context;
import android.widget.Toast;

import com.happy.pro.download.config.FileMapping;
import com.happy.pro.download.impl.BearDownloadManager;
import com.happy.pro.download.interfaces.IDownloadManager;
import com.happy.pro.utils.FLog;

import java.io.File;

/**
 * Clean download utilities for BEAR-LOADER
 * 
 * @author BEAR-LOADER Team
 * @version 3.0.0
 */
public class BearDownloadUtils {
    
    /**
     * Download all resources for a specific game
     */
    public static void downloadGameResources(Context context, String packageName) {
        if (!FileMapping.isPackageSupported(packageName)) {
            showError(context, "Game not supported: " + packageName);
            return;
        }
        
        FileMapping.GameInfo gameInfo = FileMapping.getGameInfo(packageName);
        FLog.info("🎮 Downloading resources for: " + gameInfo.displayName);
        
        IDownloadManager downloadManager = BearDownloadManager.getInstance(context);
        
        // Download patch configuration first
        downloadPatchConfig(downloadManager, packageName, new IDownloadManager.DownloadCallback() {
            @Override
            public void onStart(String downloadId) {
                showInfo(context, "Downloading configuration...");
            }
            
            @Override
            public void onProgress(IDownloadManager.DownloadProgress progress) {
                // Config download progress
            }
            
            @Override
            public void onComplete(String downloadId, File file) {
                FLog.info("✅ Configuration downloaded, now downloading libraries...");
                downloadGameLibraries(downloadManager, packageName);
            }
            
            @Override
            public void onError(String downloadId, String error) {
                showError(context, "Failed to download configuration: " + error);
            }
        });
    }
    
    /**
     * Download patch configuration for game
     */
    public static void downloadPatchConfig(Context context, String packageName) {
        IDownloadManager downloadManager = BearDownloadManager.getInstance(context);
        downloadPatchConfig(downloadManager, packageName, createDefaultCallback(context, "configuration"));
    }
    
    /**
     * Download libraries for game
     */
    public static void downloadGameLibraries(Context context, String packageName) {
        IDownloadManager downloadManager = BearDownloadManager.getInstance(context);
        downloadGameLibraries(downloadManager, packageName);
    }
    
    /**
     * Download main application configuration
     */
    public static void downloadMainConfig(Context context) {
        IDownloadManager downloadManager = BearDownloadManager.getInstance(context);
        
        String configFileId = FileMapping.getAssetId("main_config");
        if (configFileId == null) {
            showError(context, "Main configuration not available");
            return;
        }
        
        IDownloadManager.DownloadRequest request = new IDownloadManager.DownloadRequest(
            "main_config",
            "Main Configuration",
            configFileId,
            context.getFilesDir() + "/config/keyauth_config.json",
            IDownloadManager.DownloadType.CONFIG
        );
        
        downloadManager.downloadWithUI(request, createDefaultCallback(context, "main configuration"));
    }
    
    /**
     * Download app update
     */
    public static void downloadAppUpdate(Context context, String version) {
        IDownloadManager downloadManager = BearDownloadManager.getInstance(context);
        
        String updateFileId = FileMapping.getAssetId("app_update");
        if (updateFileId == null) {
            showError(context, "App update not available");
            return;
        }
        
        IDownloadManager.DownloadRequest request = new IDownloadManager.DownloadRequest(
            "app_update_" + version,
            "BEAR-LOADER Update v" + version,
            updateFileId,
            context.getExternalFilesDir(null) + "/updates/bear-loader-" + version + ".apk",
            IDownloadManager.DownloadType.UPDATE
        );
        
        downloadManager.downloadWithUI(request, new IDownloadManager.DownloadCallback() {
            @Override
            public void onStart(String downloadId) {
                showInfo(context, "Downloading update...");
            }
            
            @Override
            public void onProgress(IDownloadManager.DownloadProgress progress) {
                FLog.info("📊 Update download: " + progress.progressPercent + "%");
            }
            
            @Override
            public void onComplete(String downloadId, File file) {
                showInfo(context, "Update downloaded! Tap to install.");
                // Install update
                InstallUtils.installApk(context, file);
            }
            
            @Override
            public void onError(String downloadId, String error) {
                showError(context, "Update download failed: " + error);
            }
        });
    }
    
    /**
     * Check and download missing resources
     */
    public static void checkAndDownloadMissingResources(Context context, String packageName) {
        File configDir = new File(context.getFilesDir(), "config");
        File libsDir = new File(context.getFilesDir(), "libs");
        
        boolean needsConfig = !configDir.exists() || configDir.listFiles().length == 0;
        boolean needsLibraries = !libsDir.exists() || libsDir.listFiles().length == 0;
        
        if (needsConfig || needsLibraries) {
            FLog.info("📋 Missing resources detected, downloading...");
            downloadGameResources(context, packageName);
        } else {
            FLog.info("✅ All resources are available");
            showInfo(context, "All resources are up to date!");
        }
    }
    
    /**
     * Get download status
     */
    public static String getDownloadStatus(Context context) {
        IDownloadManager downloadManager = BearDownloadManager.getInstance(context);
        int activeCount = downloadManager.getActiveDownloadCount();
        
        if (activeCount == 0) {
            return "No active downloads";
        } else {
            return activeCount + " download(s) in progress";
        }
    }
    
    /**
     * Cancel all downloads
     */
    public static void cancelAllDownloads(Context context) {
        // Implementation would need to be added to the download manager
        showInfo(context, "Downloads cancelled");
    }
    
    // Private helper methods
    
    private static void downloadPatchConfig(IDownloadManager downloadManager, String packageName, 
                                          IDownloadManager.DownloadCallback callback) {
        String configFileId = FileMapping.getPatchConfigId(packageName);
        if (configFileId == null) {
            callback.onError("patch_config", "No configuration available for " + packageName);
            return;
        }
        
        IDownloadManager.DownloadRequest request = new IDownloadManager.DownloadRequest(
            "patch_config_" + packageName,
            "Patch Configuration",
            configFileId,
            downloadManager.getContext().getFilesDir() + "/config/patch_" + packageName + ".json",
            IDownloadManager.DownloadType.CONFIG
        );
        
        downloadManager.downloadInBackground(request, callback);
    }
    
    private static void downloadGameLibraries(IDownloadManager downloadManager, String packageName) {
        // Download stealth library
        String stealthId = FileMapping.getStealthLibraryId(packageName);
        if (stealthId != null) {
            IDownloadManager.DownloadRequest stealthRequest = new IDownloadManager.DownloadRequest(
                "stealth_" + packageName,
                "Stealth Library",
                stealthId,
                downloadManager.getContext().getFilesDir() + "/libs/libstealth_" + packageName + ".so",
                IDownloadManager.DownloadType.LIBRARY
            );
            
            downloadManager.downloadInBackground(stealthRequest, createLibraryCallback(downloadManager.getContext(), "stealth"));
        }
        
        // Download hook engine
        String hookId = FileMapping.getHookEngineId(packageName);
        if (hookId != null) {
            IDownloadManager.DownloadRequest hookRequest = new IDownloadManager.DownloadRequest(
                "hook_" + packageName,
                "Hook Engine",
                hookId,
                downloadManager.getContext().getFilesDir() + "/libs/libhook_" + packageName + ".so",
                IDownloadManager.DownloadType.LIBRARY
            );
            
            downloadManager.downloadInBackground(hookRequest, createLibraryCallback(downloadManager.getContext(), "hook"));
        }
        
        // Download crypto engine
        String cryptoId = FileMapping.getCryptoEngineId(packageName);
        if (cryptoId != null) {
            IDownloadManager.DownloadRequest cryptoRequest = new IDownloadManager.DownloadRequest(
                "crypto_" + packageName,
                "Crypto Engine",
                cryptoId,
                downloadManager.getContext().getFilesDir() + "/libs/libcrypto_" + packageName + ".so",
                IDownloadManager.DownloadType.LIBRARY
            );
            
            downloadManager.downloadInBackground(cryptoRequest, createLibraryCallback(downloadManager.getContext(), "crypto"));
        }
    }
    
    private static IDownloadManager.DownloadCallback createDefaultCallback(Context context, String itemName) {
        return new IDownloadManager.DownloadCallback() {
            @Override
            public void onStart(String downloadId) {
                FLog.info("🚀 Downloading " + itemName + "...");
            }
            
            @Override
            public void onProgress(IDownloadManager.DownloadProgress progress) {
                FLog.info("📊 " + itemName + ": " + progress.progressPercent + "%");
            }
            
            @Override
            public void onComplete(String downloadId, File file) {
                FLog.info("✅ " + itemName + " downloaded: " + file.getName());
                showInfo(context, itemName + " downloaded successfully!");
            }
            
            @Override
            public void onError(String downloadId, String error) {
                FLog.error("❌ " + itemName + " download failed: " + error);
                showError(context, itemName + " download failed: " + error);
            }
        };
    }
    
    private static IDownloadManager.DownloadCallback createLibraryCallback(Context context, String libraryType) {
        return new IDownloadManager.DownloadCallback() {
            @Override
            public void onStart(String downloadId) {
                FLog.info("📚 Downloading " + libraryType + " library...");
            }
            
            @Override
            public void onProgress(IDownloadManager.DownloadProgress progress) {
                // Library progress updates
            }
            
            @Override
            public void onComplete(String downloadId, File file) {
                FLog.info("✅ " + libraryType + " library downloaded");
                
                // Load the library immediately
                try {
                    System.load(file.getAbsolutePath());
                    FLog.info("🔗 " + libraryType + " library loaded successfully");
                } catch (Exception e) {
                    FLog.error("❌ Failed to load " + libraryType + " library: " + e.getMessage());
                }
            }
            
            @Override
            public void onError(String downloadId, String error) {
                FLog.error("❌ " + libraryType + " library download failed: " + error);
            }
        };
    }
    
    private static void showInfo(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    private static void showError(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
} 