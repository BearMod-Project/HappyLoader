package com.happy.pro.libhelper;

import android.content.pm.ApplicationInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import com.happy.pro.BoxApplication;
import com.happy.pro.utils.FLog;

/**
 * Stub implementations for missing net_62v.external classes
 * These provide fallback functionality when the container library is not available
 */
public class MetaStubs {

    public static class MetaActivationManager {
        public static void activateSdk(String key) {
            FLog.info("MetaActivationManager stub: SDK activation requested");
            // Stub implementation - does nothing but logs
        }
    }

    public static class MetaPackageManager {
        public static ApplicationInfo getApplicationInfo(String packageName) {
            try {
                return BoxApplication.get().getPackageManager().getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                FLog.error("Package not found: " + packageName);
                return null;
            }
        }

        public static boolean isInnerAppInstalled(String packageName) {
            try {
                BoxApplication.get().getPackageManager().getApplicationInfo(packageName, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }

        public static void uninstallAppFully(String packageName) {
            FLog.info("MetaPackageManager stub: Uninstall requested for " + packageName);
            // Stub implementation - logs but doesn't actually uninstall
        }
    }

    public static class MetaActivityManager {
        public static boolean isAppRunning(String packageName, int userId) {
            FLog.info("MetaActivityManager stub: Checking if app is running: " + packageName);
            // Stub implementation - always returns false
            return false;
        }

        public static void killAppByPkg(String packageName) {
            FLog.info("MetaActivityManager stub: Kill app requested for " + packageName);
            // Stub implementation - does nothing but logs
        }

        public static void launchApp(String packageName) {
            try {
                Intent launchIntent = BoxApplication.get().getPackageManager().getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    BoxApplication.get().startActivity(launchIntent);
                } else {
                    FLog.error("Cannot launch app: " + packageName);
                }
            } catch (Exception e) {
                FLog.error("Error launching app " + packageName + ": " + e.getMessage());
            }
        }
    }

    public static class MetaApplicationInstaller {
        public static void installAppByPath(String path) {
            FLog.info("MetaApplicationInstaller stub: Install by path requested: " + path);
            // Stub implementation - logs but doesn't actually install
        }

        public static void cloneApp(String packageName) {
            FLog.info("MetaApplicationInstaller stub: Clone app requested for " + packageName);
            // Stub implementation - logs but doesn't actually clone
        }
    }

    public static class MetaStorageManager {
        public static String obtainAppExternalStorageDir(String packageName) {
            // Return a fallback path
            String fallbackPath = BoxApplication.get().getExternalFilesDir(null).getAbsolutePath();
            FLog.info("MetaStorageManager stub: External storage dir for " + packageName + " -> " + fallbackPath);
            return fallbackPath;
        }
    }
} 
