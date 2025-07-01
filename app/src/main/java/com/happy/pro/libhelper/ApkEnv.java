package com.happy.pro.libhelper;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.blankj.molihuan.utilcode.util.FileUtils;
import com.happy.pro.libhelper.MetaStubs.MetaApplicationInstaller;
import com.happy.pro.libhelper.MetaStubs.MetaActivityManager;
import com.happy.pro.libhelper.MetaStubs.MetaPackageManager;
import com.happy.pro.libhelper.MetaStubs.MetaStorageManager;

import com.happy.pro.BoxApplication;
import com.happy.pro.R;
import com.happy.pro.utils.FLog;
import com.topjohnwu.superuser.Shell;

import java.io.File;

public class ApkEnv {
    File obbContaine;

    private static ApkEnv singleton;

    public static ApkEnv getInstance() {
        if (singleton == null) {
            singleton = new ApkEnv();
        }
        return singleton;
    }

    public ApplicationInfo getApplicationInfo(String packageName) {
        ApplicationInfo applicationInfo = null;
        try {
        	applicationInfo = BoxApplication.get().getPackageManager().getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException err) {
        	FLog.error(err.getMessage());
            BoxApplication.get().showToastWithImage(R.drawable.ic_error, err.getMessage());
            return null;
        }

        /*if (!AbiUtils.isSupport(new File(applicationInfo.sourceDir))) {
            BoxApplication.getInstance().showToastWithImage(R.drawable.ic_error, "Please Install Game " + (FCore.is64Bit() ? "64Bit" : "32Bit") + " version.");
            return null;
        }*/

        return applicationInfo;
    }

    public ApplicationInfo getApplicationInfoContainer(String packageName) {
    	if (!isInstalled(packageName)) {
            BoxApplication.get().showToastWithImage(R.drawable.ic_error, "App not install, install first");
            return null;
        }

        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = MetaPackageManager.getApplicationInfo(packageName);
        } catch (Exception e) {
            FLog.error("MetaPackageManager.getApplicationInfo failed: " + e.getMessage());
            // Fallback to regular PackageManager if Meta fails
            try {
                applicationInfo = getApplicationInfo(packageName);
            } catch (Exception fallbackException) {
                FLog.error("Fallback getApplicationInfo also failed: " + fallbackException.getMessage());
                return null;
            }
        }
        if (applicationInfo == null) {
            FLog.error("ApplicationInfo is null for package: " + packageName);
            return null;
        }
        return applicationInfo;
    }

    public boolean isInstalled(String packageName) {
        try {
            return MetaPackageManager.isInnerAppInstalled(packageName);
        } catch (Exception e) {
            FLog.error("MetaPackageManager.isInnerAppInstalled failed for " + packageName + ": " + e.getMessage());
            // Fallback to regular PackageManager check
            try {
                ApplicationInfo info = getApplicationInfo(packageName);
                return info != null;
            } catch (Exception fallbackException) {
                FLog.error("Fallback package check also failed: " + fallbackException.getMessage());
                return false;
            }
        }
    }

    public boolean isRunning(String packageName) {
    	try {
            return MetaActivityManager.isAppRunning(packageName, 0);
        } catch (Exception e) {
            FLog.error("Error checking if app is running: " + e.getMessage());
            return false;
        }

    }

    public boolean installByFile(String packageName) {
        ApplicationInfo applicationInfo = getApplicationInfo(packageName);
        if (applicationInfo == null) {
            return false;
        }
    	try {
            MetaApplicationInstaller.installAppByPath(applicationInfo.sourceDir);
        } catch (Exception e) {
            FLog.error("Error installing app by file: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean installByPackage(String packageName) {
    	try {
            MetaApplicationInstaller.cloneApp(packageName);
        } catch (Exception e) {
            FLog.error("Error installing app by package: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void unInstallApp(String packageName) {
    	try {
            MetaPackageManager.uninstallAppFully(packageName);
        } catch (Exception e) {
            FLog.error("Error uninstalling app: " + e.getMessage());
        }
    }

    public void stopRunningApp(String packageName) {
    	try {
            MetaActivityManager.killAppByPkg(packageName);
        } catch (Exception e) {
            FLog.error("Error stopping app: " + e.getMessage());
        }
    }

    public File getObbContainerPath(String packageName) {
    	try {
            return new File(MetaStorageManager.obtainAppExternalStorageDir(packageName) + "/Android/obb", packageName);
        } catch (Exception e) {
            FLog.error("Error getting OBB container path: " + e.getMessage());
            // Return a fallback path
            return new File(BoxApplication.get().getExternalFilesDir(null), "obb/" + packageName);
        }
    }

    public boolean tryAddLoader(String packageName) {
        try {
            FLog.info("🔧 tryAddLoader: Starting for package " + packageName);
            
            boolean is_online = BoxApplication.STATUS_BY.equals("online");
            
            // First check if running in container mode or root mode
            boolean isContainerMode = !Shell.rootAccess();
            FLog.info("📱 Mode: " + (isContainerMode ? "CONTAINER" : "ROOT"));
            
            ApplicationInfo applicationInfo = null;
            
            // Try to get application info based on mode
            if (isContainerMode) {
                // Container mode - use fallback implementation
                try {
                    applicationInfo = BoxApplication.get().getPackageManager().getApplicationInfo(packageName, 0);
                    FLog.info("📦 Container mode: Got app info for " + packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    FLog.error("❌ Container mode: Package not found: " + packageName);
                    BoxApplication.get().showToastWithImage(R.drawable.ic_error, "Game not installed: " + packageName);
                    return false;
                }
            } else {
                // Root mode - use traditional approach
                applicationInfo = getApplicationInfoContainer(packageName);
            }
            
            if (applicationInfo == null) {
                FLog.error("❌ Failed to get application info for package: " + packageName);
                BoxApplication.get().showToastWithImage(R.drawable.ic_error, "Failed to get app info for " + packageName);
                return false;
            }

            // Determine target library based on package
            String target = "libpubgm.so";
            if (packageName.equals("com.miraclegames.farlight84")) {
                target = "libfarlight.so";
            } else if (packageName.equals("com.pubg.krmobile")) {
                target = "libSdk.so";
            } else if (packageName.equals("com.pubg.imobile")) {
                target = "libbgmi.so";
            }
            
            FLog.info("🎯 Target library: " + target);

            // Determine source path
            File loader = null;
            if (is_online) {
                loader = new File(BoxApplication.get().getFilesDir(), "loader/" + target);
            } else {
                loader = new File(BoxApplication.get().getApplicationInfo().nativeLibraryDir, target);
            }
            
            if (!loader.exists()) {
                FLog.error("❌ Loader library not found: " + loader.getAbsolutePath());
                BoxApplication.get().showToastWithImage(R.drawable.ic_error, "BEAR-LOADER library missing");
                return false;
            }
            
            FLog.info("✅ Loader found at: " + loader.getAbsolutePath());

            // In container mode, we can't actually copy to target app directory
            // So we'll use a different approach
            if (isContainerMode) {
                FLog.info("📦 Container mode: Using alternative loader injection method");
                // Store the loader info for later use by container system
                BoxApplication.get().getSharedPreferences("bear_loader", Context.MODE_PRIVATE)
                    .edit()
                    .putString("loader_path_" + packageName, loader.getAbsolutePath())
                    .putString("target_lib_" + packageName, target)
                    .apply();
                FLog.info("✅ Container mode: Loader configuration saved");
                return true;
            }
            
            // Root mode - traditional file copy approach
            File loaderDest = new File(applicationInfo.nativeLibraryDir, 
                packageName.equals("com.miraclegames.farlight84") ? "libfarlight.so" : "libAkAudioVisiual.so");
            
            FLog.info("📂 Destination: " + loaderDest.getAbsolutePath());

            if (loaderDest.exists()) {
                FLog.info("🗑️ Removing existing loader");
                loaderDest.delete();
            }
            
            try {
                if (FileUtils.copy(loader.toString(), loaderDest.toString())) {
                    FLog.info("✅ BEAR-LOADER successfully added to game");
                    return true;
                } else {
                    FLog.error("❌ Failed to copy loader file");
                    return false;
                }
            } catch(Exception err) {
                FLog.error("❌ Copy error: " + err.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            FLog.error("💥 Critical error in tryAddLoader: " + e.getMessage());
            e.printStackTrace();
            BoxApplication.get().showToastWithImage(R.drawable.ic_error, "Failed to add BEAR-LOADER: " + e.getMessage());
            return false;
        }
    }

    public void launchApk(String packageName) {
        if (!isInstalled(packageName)) {
            BoxApplication.get().showToastWithImage(R.drawable.icon, "Client not installed");
            return;
        }
        try {
            MetaActivityManager.launchApp(packageName);
        } catch (Exception e) {
            FLog.error("Error launching app: " + e.getMessage());
        }
    }

}

