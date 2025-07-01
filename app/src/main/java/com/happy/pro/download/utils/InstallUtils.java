package com.happy.pro.download.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.happy.pro.utils.FLog;

import java.io.File;

/**
 * Installation utilities for BEAR-LOADER
 * 
 * @author BEAR-LOADER Team
 * @version 3.0.0
 */
public class InstallUtils {
    
    private static final String TAG = "InstallUtils";
    
    /**
     * Install APK file
     */
    public static void installApk(Context context, File apkFile) {
        try {
            FLog.info("📦 Installing APK: " + apkFile.getName());
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri apkUri;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Use FileProvider for Android 7.0+
                apkUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    apkFile
                );
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                // Direct file URI for older versions
                apkUri = Uri.fromFile(apkFile);
            }
            
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            context.startActivity(intent);
            FLog.info("✅ APK installation started");
            
        } catch (Exception e) {
            FLog.error("❌ Failed to install APK: " + e.getMessage());
            Toast.makeText(context, "Failed to install update: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Check if APK can be installed
     */
    public static boolean canInstallApk(Context context, File apkFile) {
        if (!apkFile.exists()) {
            FLog.error("❌ APK file does not exist: " + apkFile.getPath());
            return false;
        }
        
        if (!apkFile.canRead()) {
            FLog.error("❌ Cannot read APK file: " + apkFile.getPath());
            return false;
        }
        
        // Check if installation from unknown sources is allowed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.getPackageManager().canRequestPackageInstalls()) {
                FLog.warning("⚠️ Installation from unknown sources not allowed");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Extract ZIP file
     */
    public static void extractZip(File zipFile, File targetDir) {
        try {
            FLog.info("📦 Extracting: " + zipFile.getName());
            
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            
            // Use existing zip utility
            com.happy.pro.Component.Zip.unzip(zipFile, targetDir);
            
            // Set executable permissions for .so files
            setLibraryPermissions(targetDir);
            
            // Clean up zip file after extraction
            if (zipFile.exists()) {
                zipFile.delete();
            }
            
            FLog.info("✅ Extraction completed: " + zipFile.getName());
            
        } catch (Exception e) {
            FLog.error("❌ Extraction failed: " + e.getMessage());
        }
    }
    
    /**
     * Set executable permissions for library files
     */
    private static void setLibraryPermissions(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    setLibraryPermissions(file); // Recursive
                } else if (file.getName().endsWith(".so") || file.getName().endsWith(".elf")) {
                    file.setExecutable(true);
                    FLog.info("🔧 Set executable: " + file.getName());
                }
            }
        }
    }
} 