# KeyAuth Download Manager Usage Guide

## Overview

The KeyAuth Download Manager has been updated to work with KeyAuth's file system using file IDs instead of URLs. This provides better security and authentication integration.

## How It Works

1. **Upload files to KeyAuth Dashboard** - Upload your files (libraries, configs, assets) to your KeyAuth application dashboard
2. **Get File IDs** - Each uploaded file gets a unique file ID (like "420371")
3. **Use File IDs in Code** - Reference files by their KeyAuth file ID instead of URLs

## Your Current Files

Based on your KeyAuth dashboard, you have:
- `keyauth_config.json` - File ID: **420371**

## Basic Usage

### 1. Download Single File

```java
// Download the keyauth_config.json file
KeyAuthDownloadManager downloadManager = KeyAuthDownloadManager.getInstance(context);

KeyAuthDownloadManager.DownloadItem configItem = new KeyAuthDownloadManager.DownloadItem(
    "config_download",
    "KeyAuth Config", 
    "420371", // Your file ID from dashboard
    context.getFilesDir() + "/keyauth_config.json",
    KeyAuthDownloadManager.DownloadType.CONFIG
);

downloadManager.downloadWithUI(configItem, new KeyAuthDownloadManager.DownloadCallback() {
    @Override
    public void onStart(String downloadId) {
        FLog.info("Download started");
    }
    
    @Override
    public void onProgress(KeyAuthDownloadManager.DownloadProgress progress) {
        FLog.info("Progress: " + progress.progressPercent + "%");
    }
    
    @Override
    public void onComplete(String downloadId, File file) {
        FLog.info("Download completed: " + file.getName());
    }
    
    @Override
    public void onError(String downloadId, String error) {
        FLog.error("Download failed: " + error);
    }
});
```

### 2. Using Utility Methods

```java
// Download game libraries (uses predefined file IDs)
DownloadUtils.downloadGameLibraries(context, "com.tencent.ig");

// Download game loader
DownloadUtils.downloadGameLoader(context, "com.tencent.ig");

// Download configurations
DownloadUtils.downloadConfigurations(context);
```

## Setting Up File IDs

### 1. Update File ID Mappings

In `KeyAuthDownloadManager.java`, update the file ID mappings:

```java
private String getLibrariesFileId(String packageName) {
    Map<String, String> librariesFileIds = new HashMap<>();
    librariesFileIds.put("com.tencent.ig", "YOUR_PUBG_LIBS_FILE_ID");
    librariesFileIds.put("com.pubg.krmobile", "YOUR_KOREA_LIBS_FILE_ID");
    // Add more mappings...
    return librariesFileIds.get(packageName);
}

private String getLoaderFileId(String packageName) {
    Map<String, String> loaderFileIds = new HashMap<>();
    loaderFileIds.put("com.tencent.ig", "YOUR_PUBG_LOADER_FILE_ID");
    loaderFileIds.put("com.pubg.krmobile", "YOUR_KOREA_LOADER_FILE_ID");
    // Add more mappings...
    return loaderFileIds.get(packageName);
}
```

### 2. Upload Required Files

Upload these files to your KeyAuth dashboard:

1. **Game Libraries** (per game):
   - `pubgm-global-libs.zip` 
   - `pubgm-korea-libs.zip`
   - `pubgm-taiwan-libs.zip`
   - `bgmi-india-libs.zip`

2. **Game Loaders** (per game):
   - `pubgm-global-loader.zip`
   - `pubgm-korea-loader.zip` 
   - `pubgm-taiwan-loader.zip`
   - `bgmi-india-loader.zip`

3. **Configuration Files**:
   - `keyauth_config.json` (already uploaded - ID: 420371)
   - `app_config.json`
   - `game_signatures.db`

4. **App Updates**:
   - `bear-loader-v3.0.1.apk`
   - `bear-loader-v3.0.2.apk`

## Authentication

The KeyAuth Download Manager automatically handles authentication:

```java
// Authentication is handled automatically
// Files are only accessible to authenticated users
// No need for manual headers or tokens
```

## Error Handling

```java
@Override
public void onError(String downloadId, String error) {
    if (error.contains("KeyAuth authentication required")) {
        // User needs to login first
        // Redirect to login activity
    } else if (error.contains("file not found")) {
        // File ID doesn't exist in KeyAuth
        // Check your dashboard
    } else {
        // Other network or file errors
        FLog.error("Download error: " + error);
    }
}
```

## Migration from Old System

### Before (URL-based):
```java
new DownloadZip(context).execute("1", "https://example.com/file.zip");
```

### After (KeyAuth file ID-based):
```java
KeyAuthDownloadManager.DownloadItem item = new KeyAuthDownloadManager.DownloadItem(
    "download_id", "File Name", "420371", "/path/to/file", DownloadType.ASSETS
);
downloadManager.downloadWithUI(item, callback);
```

## Benefits

✅ **Secure** - Files only accessible to authenticated users  
✅ **Reliable** - No broken download links  
✅ **Integrated** - Works with existing KeyAuth authentication  
✅ **Trackable** - Download analytics in KeyAuth dashboard  
✅ **Manageable** - Update files without code changes  

## Next Steps

1. Upload your files to KeyAuth dashboard
2. Note down the file IDs  
3. Update the file ID mappings in the code
4. Test downloads with your file IDs
5. Replace old download methods with new KeyAuth Download Manager

## Example Implementation

Replace your existing download code in `MainActivity.java`:

```java
// OLD
File newFile = new File(getFilesDir().toString() + "/TW");
if (!newFile.exists()) {
    new DownloadZip(this).execute("1", downloadUrl);
}

// NEW  
DownloadUtils.checkAndDownloadMissingResources(this, "com.tencent.ig");
```

This provides better error handling, progress tracking, and authentication integration. 