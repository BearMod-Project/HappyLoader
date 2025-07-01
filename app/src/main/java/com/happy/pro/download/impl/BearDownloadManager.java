package com.happy.pro.download.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.happy.pro.BuildConfig;
import com.happy.pro.download.interfaces.IDownloadManager;
import com.happy.pro.download.interfaces.IKeyAuthAPI;
import com.happy.pro.download.tasks.DownloadTask;
import com.happy.pro.utils.FLog;
import com.techiness.progressdialoglibrary.ProgressDialog;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clean implementation of BEAR Download Manager
 * 
 * @author BEAR-LOADER Team
 * @version 3.0.0
 */
public class BearDownloadManager implements IDownloadManager {
    
    private static final String TAG = "BearDownloadManager";
    private static BearDownloadManager instance;
    
    private final Context context;
    private final IKeyAuthAPI keyAuthAPI;
    private final Handler mainHandler;
    private final ExecutorService downloadExecutor;
    private final Map<String, DownloadTask> activeDownloads;
    
    private BearDownloadManager(Context context) {
        this.context = context.getApplicationContext();
        this.keyAuthAPI = new KeyAuthAPIImpl(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.downloadExecutor = Executors.newFixedThreadPool(BuildConfig.MAX_CONCURRENT_DOWNLOADS);
        this.activeDownloads = new ConcurrentHashMap<>();
    }
    
    public static synchronized BearDownloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new BearDownloadManager(context);
        }
        return instance;
    }
    
    @Override
    public void downloadWithUI(DownloadRequest request, DownloadCallback callback) {
        if (!validateRequest(request, callback)) return;
        
        ProgressDialog progressDialog = createProgressDialog(request);
        progressDialog.show();
        
        executeDownload(request, wrapCallbackWithUI(callback, progressDialog));
    }
    
    @Override
    public void downloadInBackground(DownloadRequest request, DownloadCallback callback) {
        if (!validateRequest(request, callback)) return;
        
        executeDownload(request, callback);
    }
    
    @Override
    public void cancelDownload(String downloadId) {
        DownloadTask task = activeDownloads.get(downloadId);
        if (task != null) {
            task.cancel();
            activeDownloads.remove(downloadId);
            if (BuildConfig.ENABLE_DOWNLOAD_LOGGING) {
                FLog.info("🚫 Download cancelled: " + downloadId);
            }
        }
    }
    
    @Override
    public int getActiveDownloadCount() {
        return activeDownloads.size();
    }
    
    @Override
    public boolean isReady() {
        return keyAuthAPI.isAuthenticated();
    }
    
    @Override
    public void clearCache() {
        // Clear download cache
        File cacheDir = new File(context.getCacheDir(), "downloads");
        if (cacheDir.exists()) {
            deleteRecursive(cacheDir);
        }
        if (BuildConfig.ENABLE_DOWNLOAD_LOGGING) {
            FLog.info("🗑️ Download cache cleared");
        }
    }
    
    private boolean validateRequest(DownloadRequest request, DownloadCallback callback) {
        if (!keyAuthAPI.isAuthenticated()) {
            notifyError(callback, request.id, "Authentication required");
            return false;
        }
        
        if (activeDownloads.containsKey(request.id)) {
            if (BuildConfig.ENABLE_DOWNLOAD_LOGGING) {
                FLog.warning("⚠️ Download already in progress: " + request.id);
            }
            return false;
        }
        
        return true;
    }
    
    private void executeDownload(DownloadRequest request, DownloadCallback callback) {
        DownloadTask task = new DownloadTask(context, keyAuthAPI, request, callback);
        activeDownloads.put(request.id, task);
        
        downloadExecutor.execute(() -> {
            task.execute();
            activeDownloads.remove(request.id);
        });
    }
    
    private ProgressDialog createProgressDialog(DownloadRequest request) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTheme(ProgressDialog.THEME_DARK);
        dialog.setMode(ProgressDialog.MODE_DETERMINATE);
        dialog.setMaxValue(100);
        dialog.showProgressTextAsFraction(true);
        dialog.setCancelable(false);
        dialog.setTitle("Downloading " + request.type.name());
        dialog.setMessage(request.name);
        return dialog;
    }
    
    private DownloadCallback wrapCallbackWithUI(DownloadCallback callback, ProgressDialog dialog) {
        return new DownloadCallback() {
            @Override
            public void onStart(String downloadId) {
                mainHandler.post(() -> {
                    if (callback != null) callback.onStart(downloadId);
                });
            }
            
            @Override
            public void onProgress(DownloadProgress progress) {
                mainHandler.post(() -> {
                    dialog.setProgress(progress.progressPercent);
                    dialog.setMessage(progress.statusMessage);
                    if (callback != null) callback.onProgress(progress);
                });
            }
            
            @Override
            public void onComplete(String downloadId, File file) {
                mainHandler.post(() -> {
                    dialog.dismiss();
                    if (callback != null) callback.onComplete(downloadId, file);
                });
            }
            
            @Override
            public void onError(String downloadId, String error) {
                mainHandler.post(() -> {
                    dialog.dismiss();
                    if (callback != null) callback.onError(downloadId, error);
                });
            }
        };
    }
    
    private void notifyError(DownloadCallback callback, String downloadId, String error) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(downloadId, error));
        }
    }
    
    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }
    
    /**
     * Get context
     */
    public Context getContext() {
        return context;
    }
    
    /**
     * Shutdown the download manager
     */
    public void shutdown() {
        downloadExecutor.shutdown();
        activeDownloads.clear();
    }
} 