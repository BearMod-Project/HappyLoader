package com.happy.pro.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.happy.pro.R;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.List;

/**
 * BEAR-LOADER Comprehensive Permission Manager
 * Handles all permissions automatically and smoothly
 */
public class PermissionManager {
    private static final String TAG = "PermissionManager";
    
    // Permission request codes
    public static final int REQUEST_PERMISSIONS = 1000;
    public static final int REQUEST_OVERLAY_PERMISSION = 1001;
    public static final int REQUEST_MANAGE_STORAGE = 1002;
    public static final int REQUEST_INSTALL_PACKAGES = 1003;
    
    // Essential permissions
    private static final String[] ESSENTIAL_PERMISSIONS = {
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.GET_TASKS,
        Manifest.permission.KILL_BACKGROUND_PROCESSES
    };
    
    // Dangerous permissions that need special handling
    private static final String[] SPECIAL_PERMISSIONS = {
        Manifest.permission.SYSTEM_ALERT_WINDOW,
        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
        Manifest.permission.REQUEST_INSTALL_PACKAGES
    };
    
    private Activity activity;
    private PermissionCallback callback;
    private boolean rootCheckPerformed = false;
    private boolean hasRootAccess = false;
    
    // SharedPreferences keys for dialog states
    private static final String PREFS_NAME = "bear_permission_dialogs";
    private static final String KEY_STORAGE_DIALOG_SHOWN = "storage_dialog_shown";
    private static final String KEY_ROOT_DIALOG_SHOWN = "root_dialog_shown";
    private static final String KEY_OVERLAY_DIALOG_SHOWN = "overlay_dialog_shown";
    private static final String KEY_INSTALL_DIALOG_SHOWN = "install_dialog_shown";
    
    public interface PermissionCallback {
        void onPermissionsGranted(boolean hasRoot);
        void onPermissionsDenied(String[] deniedPermissions);
        void onRootCheckCompleted(boolean hasRoot);
    }
    
    public PermissionManager(Activity activity) {
        this.activity = activity;
    }
    
    /**
     * Check and request all permissions automatically
     */
    public void checkAndRequestAllPermissions(PermissionCallback callback) {
        this.callback = callback;
        
        FLog.info("🔐 Starting comprehensive permission check...");
        
        // Step 1: Check root access first
        checkRootAccess();
        
        // Step 2: Check basic permissions
        List<String> deniedPermissions = new ArrayList<>();
        
        for (String permission : ESSENTIAL_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }
        
        if (!deniedPermissions.isEmpty()) {
            FLog.info("📋 Requesting " + deniedPermissions.size() + " basic permissions...");
            requestBasicPermissions(deniedPermissions.toArray(new String[0]));
        } else {
            // Basic permissions granted, check special permissions
            checkSpecialPermissions();
        }
    }
    
    /**
     * Check root access automatically
     */
    private void checkRootAccess() {
        if (rootCheckPerformed) {
            return;
        }
        
        FLog.info("🔐 Checking root access...");
        
        try {
            // Use background thread for root check to avoid blocking UI
            new Thread(() -> {
                try {
                    hasRootAccess = Shell.rootAccess();
                    rootCheckPerformed = true;
                    
                    activity.runOnUiThread(() -> {
                        if (hasRootAccess) {
                            FLog.info("✅ Root access granted!");
                            showRootSuccessDialog();
                        } else {
                            FLog.warning("⚠️ Root access not available");
                            showRootNotFoundDialog();
                        }
                        
                        if (callback != null) {
                            callback.onRootCheckCompleted(hasRootAccess);
                        }
                    });
                    
                } catch (Exception e) {
                    FLog.error("❌ Root check failed: " + e.getMessage());
                    activity.runOnUiThread(() -> {
                        hasRootAccess = false;
                        rootCheckPerformed = true;
                        showRootErrorDialog();
                        
                        if (callback != null) {
                            callback.onRootCheckCompleted(false);
                        }
                    });
                }
            }).start();
            
        } catch (Exception e) {
            FLog.error("❌ Failed to start root check: " + e.getMessage());
            hasRootAccess = false;
            rootCheckPerformed = true;
        }
    }
    
    /**
     * Request basic permissions
     */
    private void requestBasicPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_PERMISSIONS);
    }
    
    /**
     * Check and request special permissions
     */
    private void checkSpecialPermissions() {
        FLog.info("🔧 Checking special permissions...");
        
        // Check overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
            !Settings.canDrawOverlays(activity)) {
            requestOverlayPermission();
            return;
        }
        
        // Check manage external storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && 
            !Environment.isExternalStorageManager()) {
            requestManageStoragePermission();
            return;
        }
        
        // Check install packages permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && 
            !activity.getPackageManager().canRequestPackageInstalls()) {
            requestInstallPackagesPermission();
            return;
        }
        
        // All permissions granted
        onAllPermissionsGranted();
    }
    
    /**
     * Request overlay permission
     */
    private void requestOverlayPermission() {
        FLog.info("📱 Requesting overlay permission...");
        
        // Check if dialog has been shown before
        if (hasDialogBeenShown(KEY_OVERLAY_DIALOG_SHOWN)) {
            FLog.info("ℹ️ Overlay dialog already shown, skipping to next check");
            checkSpecialPermissions();
            return;
        }
        
        new AlertDialog.Builder(activity)
            .setTitle("🎯 Overlay Permission Required")
            .setMessage("BEAR-LOADER needs overlay permission to display ESP and menu overlays during gameplay.\n\n" +
                       "This is essential for the floating menu functionality.")
            .setCancelable(false)
            .setPositiveButton("Grant Permission", (dialog, which) -> {
                markDialogAsShown(KEY_OVERLAY_DIALOG_SHOWN);
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            })
            .setNegativeButton("Skip", (dialog, which) -> {
                markDialogAsShown(KEY_OVERLAY_DIALOG_SHOWN);
                FLog.warning("⚠️ Overlay permission skipped");
                checkSpecialPermissions();
            })
            .show();
    }
    
    /**
     * Request manage external storage permission
     */
    private void requestManageStoragePermission() {
        FLog.info("📁 Requesting manage storage permission...");
        
        // Check if dialog has been shown before
        if (hasDialogBeenShown(KEY_STORAGE_DIALOG_SHOWN)) {
            FLog.info("ℹ️ Storage dialog already shown, skipping to next check");
            checkSpecialPermissions();
            return;
        }
        
        new AlertDialog.Builder(activity)
            .setTitle("💾 Storage Access Required")
            .setMessage("BEAR-LOADER needs full storage access to:\n" +
                       "• Install game modifications\n" +
                       "• Cache game assets\n" +
                       "• Store configuration files\n\n" +
                       "This permission is required for proper functionality.")
            .setCancelable(false)
            .setPositiveButton("Grant Permission", (dialog, which) -> {
                markDialogAsShown(KEY_STORAGE_DIALOG_SHOWN);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivityForResult(intent, REQUEST_MANAGE_STORAGE);
                }
            })
            .setNegativeButton("Skip", (dialog, which) -> {
                markDialogAsShown(KEY_STORAGE_DIALOG_SHOWN);
                FLog.warning("⚠️ Storage permission skipped");
                checkSpecialPermissions();
            })
            .show();
    }
    
    /**
     * Request install packages permission
     */
    private void requestInstallPackagesPermission() {
        FLog.info("📦 Requesting install packages permission...");
        
        // Check if dialog has been shown before
        if (hasDialogBeenShown(KEY_INSTALL_DIALOG_SHOWN)) {
            FLog.info("ℹ️ Install packages dialog already shown, skipping to next check");
            checkSpecialPermissions();
            return;
        }
        
        new AlertDialog.Builder(activity)
            .setTitle("📦 Install Permission Required")
            .setMessage("BEAR-LOADER needs permission to install packages for:\n" +
                       "• Game modification installation\n" +
                       "• Plugin management\n" +
                       "• Update functionality\n\n" +
                       "This is required for mod installation features.")
            .setCancelable(false)
            .setPositiveButton("Grant Permission", (dialog, which) -> {
                markDialogAsShown(KEY_INSTALL_DIALOG_SHOWN);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivityForResult(intent, REQUEST_INSTALL_PACKAGES);
                }
            })
            .setNegativeButton("Skip", (dialog, which) -> {
                markDialogAsShown(KEY_INSTALL_DIALOG_SHOWN);
                FLog.warning("⚠️ Install packages permission skipped");
                checkSpecialPermissions();
            })
            .show();
    }
    
    /**
     * Handle permission results
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            List<String> deniedPermissions = new ArrayList<>();
            
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }
            
            if (deniedPermissions.isEmpty()) {
                FLog.info("✅ Basic permissions granted");
                checkSpecialPermissions();
            } else {
                FLog.error("❌ Some permissions denied: " + deniedPermissions);
                showPermissionDeniedDialog(deniedPermissions.toArray(new String[0]));
            }
        }
    }
    
    /**
     * Handle activity results for special permissions
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_OVERLAY_PERMISSION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
                    Settings.canDrawOverlays(activity)) {
                    FLog.info("✅ Overlay permission granted");
                } else {
                    FLog.warning("⚠️ Overlay permission not granted");
                }
                checkSpecialPermissions();
                break;
                
            case REQUEST_MANAGE_STORAGE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && 
                    Environment.isExternalStorageManager()) {
                    FLog.info("✅ Manage storage permission granted");
                } else {
                    FLog.warning("⚠️ Manage storage permission not granted");
                }
                checkSpecialPermissions();
                break;
                
            case REQUEST_INSTALL_PACKAGES:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && 
                    activity.getPackageManager().canRequestPackageInstalls()) {
                    FLog.info("✅ Install packages permission granted");
                } else {
                    FLog.warning("⚠️ Install packages permission not granted");
                }
                checkSpecialPermissions();
                break;
        }
    }
    
    /**
     * All permissions granted callback
     */
    private void onAllPermissionsGranted() {
        FLog.info("🎉 All permissions granted successfully!");
        
        if (callback != null) {
            callback.onPermissionsGranted(hasRootAccess);
        }
        
        showPermissionSuccessDialog();
    }
    
    /**
     * Show success dialogs
     */
    private void showRootSuccessDialog() {
        new AlertDialog.Builder(activity)
            .setTitle("🔐 Root Access Granted")
            .setMessage("✅ BEAR-LOADER has detected root access!\n\n" +
                       "🚀 Advanced features available:\n" +
                       "• Enhanced ESP capabilities\n" +
                       "• Advanced memory modifications\n" +
                       "• Stealth bypass protection\n" +
                       "• Premium root-only features")
            .setPositiveButton("Continue", null)
            .show();
    }
    
    private void showRootNotFoundDialog() {
        // Check if dialog has been shown before
        if (hasDialogBeenShown(KEY_ROOT_DIALOG_SHOWN)) {
            FLog.info("ℹ️ Root dialog already shown, continuing silently");
            return;
        }
        
        new AlertDialog.Builder(activity)
            .setTitle("⚠️ Root Access Not Available")
            .setMessage("BEAR-LOADER is running in non-root mode.\n\n" +
                       "🔧 Available features:\n" +
                       "• Basic ESP functionality\n" +
                       "• Container-based modifications\n" +
                       "• Standard game enhancements\n\n" +
                       "💡 For advanced features, consider rooting your device.")
            .setPositiveButton("Continue", (dialog, which) -> {
                markDialogAsShown(KEY_ROOT_DIALOG_SHOWN);
            })
            .show();
    }
    
    private void showRootErrorDialog() {
        new AlertDialog.Builder(activity)
            .setTitle("❌ Root Check Error")
            .setMessage("Unable to determine root status.\n\n" +
                       "BEAR-LOADER will continue in safe mode.\n" +
                       "You can manually enable root features in settings if available.")
            .setPositiveButton("Continue", null)
            .show();
    }
    
    private void showPermissionSuccessDialog() {
        new AlertDialog.Builder(activity)
            .setTitle("🎉 Setup Complete")
            .setMessage("✅ All permissions configured successfully!\n\n" +
                       "BEAR-LOADER is ready to use with " + 
                       (hasRootAccess ? "advanced root features" : "standard features") + ".")
            .setPositiveButton("Start Using", null)
            .show();
    }
    
    private void showPermissionDeniedDialog(String[] deniedPermissions) {
        StringBuilder message = new StringBuilder("Some permissions were denied:\n\n");
        for (String permission : deniedPermissions) {
            message.append("• ").append(getPermissionName(permission)).append("\n");
        }
        message.append("\nBEAR-LOADER may not function properly without these permissions.");
        
        new AlertDialog.Builder(activity)
            .setTitle("⚠️ Permissions Required")
            .setMessage(message.toString())
            .setPositiveButton("Retry", (dialog, which) -> {
                requestBasicPermissions(deniedPermissions);
            })
            .setNegativeButton("Continue Anyway", (dialog, which) -> {
                if (callback != null) {
                    callback.onPermissionsDenied(deniedPermissions);
                }
            })
            .show();
    }
    
    /**
     * Get user-friendly permission names
     */
    private String getPermissionName(String permission) {
        switch (permission) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Storage Write Access";
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return "Storage Read Access";
            case Manifest.permission.INTERNET:
                return "Internet Access";
            case Manifest.permission.WAKE_LOCK:
                return "Wake Lock";
            case Manifest.permission.FOREGROUND_SERVICE:
                return "Foreground Service";
            case Manifest.permission.GET_TASKS:
                return "Task Management";
            case Manifest.permission.KILL_BACKGROUND_PROCESSES:
                return "Process Management";
            default:
                return permission.replace("android.permission.", "");
        }
    }
    
    /**
     * Check if all permissions are granted
     */
    public boolean areAllPermissionsGranted() {
        // Check basic permissions
        for (String permission : ESSENTIAL_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        
        // Check special permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
            !Settings.canDrawOverlays(activity)) {
            return false;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && 
            !Environment.isExternalStorageManager()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get current permission status
     */
    public PermissionStatus getPermissionStatus() {
        return new PermissionStatus(
            areAllPermissionsGranted(),
            rootCheckPerformed,
            hasRootAccess
        );
    }
    
    /**
     * Helper methods for dialog state management
     */
    private boolean hasDialogBeenShown(String key) {
        return activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(key, false);
    }
    
    private void markDialogAsShown(String key) {
        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(key, true)
                .apply();
        FLog.info("✅ Dialog marked as shown: " + key);
    }
    
    private void resetDialogState(String key) {
        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(key)
                .apply();
        FLog.info("🔄 Dialog state reset: " + key);
    }
    
    /**
     * Reset all dialog states (for testing or settings reset)
     */
    public void resetAllDialogStates() {
        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        FLog.info("🔄 All dialog states reset");
    }
    
    /**
     * Permission status class
     */
    public static class PermissionStatus {
        public final boolean allPermissionsGranted;
        public final boolean rootCheckCompleted;
        public final boolean hasRootAccess;
        
        public PermissionStatus(boolean allPermissions, boolean rootCompleted, boolean hasRoot) {
            this.allPermissionsGranted = allPermissions;
            this.rootCheckCompleted = rootCompleted;
            this.hasRootAccess = hasRoot;
        }
        
        @Override
        public String toString() {
            return String.format("PermissionStatus{permissions=%s, rootCheck=%s, root=%s}",
                allPermissionsGranted, rootCheckCompleted, hasRootAccess);
        }
    }
    
    public boolean hasRootAccess() {
        return hasRootAccess;
    }
    
    public boolean isRootCheckCompleted() {
        return rootCheckPerformed;
    }
} 
