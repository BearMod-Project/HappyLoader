package com.happy.pro.Component;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.content.Intent;
import com.blankj.molihuan.utilcode.util.ToastUtils;
import com.happy.pro.R;
import com.happy.pro.utils.FLog;

import java.util.Objects;
import java.io.File;


public class MainService extends Service {
    private static MainService instance;
    public static boolean isRunning = false;
    private static String packageName;

    static {
        try {
            System.loadLibrary("happy");
        } catch(UnsatisfiedLinkError w) {
            FLog.error(w.getMessage());
        }
    }
    
    public static native String InitBase();
    public static native void closeSocket();
    
    public static MainService get() {
    	return instance;
    }
    
    public static void startService(Context context, String packageName) {
        MainService.packageName = packageName;
        if (instance == null) {

        }
    }
    
    public static void stopService() {
    	if (instance != null) {
            instance.onDestroy();
        }
    }
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    
    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        
        try {
            if (!isRunning) {
                RunServer();
                isRunning = true;
            }
        } catch(Exception err) {
        	FLog.error(err.getMessage());
        }
    }
    
    private static void RunServer() {
    	try {
    	    new Handler().postDelayed(() -> {
                String respon = InitBase();
                if (respon.equalsIgnoreCase("Server Accept")) {
                    toast(R.drawable.ic_check, "Server Connected");
                } else {
                    toast(R.drawable.ic_error, respon);//"Error Server No Connected, Please restart.");
                    stopService();
                }
            }, 10 * 1000);
    	} catch(Exception err) {
    		FLog.error(Objects.requireNonNull(err.getCause()).getMessage());
            stopService();
    	}
    }
    
    @Override
    public void onDestroy() {
        closeSocket();
        isRunning = false;
        stopSelf();
        instance = null;
        super.onDestroy();
    }
    
    @SuppressLint("ResourceAsColor")
    private static void toast(int id, CharSequence msg) {
        ToastUtils _toast = ToastUtils.make();
        _toast.setBgColor(android.R.color.white);
        _toast.setLeftIcon(id);
        _toast.setTextColor(android.R.color.black);
        _toast.setNotUseSystemToast();
        _toast.show(msg);
    }
    
    /**
     * Ensure loader directory exists and contains necessary files
     */
    public static boolean ensureLoaderDirectory(Context context) {
        try {
            File loaderDir = new File(context.getFilesDir(), "loader");
            if (!loaderDir.exists()) {
                if (!loaderDir.mkdirs()) {
                    FLog.error("❌ Failed to create loader directory");
                    return false;
                }
            }
            
            // List of required loader libraries
            String[] requiredLibs = {
                "libpubgm.so",
                "libSdk.so",
                "libbgmi.so",
                "libfarlight.so"
            };
            
            // Check if at least one library exists
            boolean hasLibrary = false;
            for (String lib : requiredLibs) {
                File libFile = new File(loaderDir, lib);
                if (libFile.exists()) {
                    hasLibrary = true;
                    FLog.info("✅ Found loader library: " + lib);
                }
            }
            
            if (!hasLibrary) {
                FLog.warn("⚠️ No loader libraries found in: " + loaderDir.getAbsolutePath());
                // Try to copy from native lib directory
                copyNativeLibraries(context, loaderDir);
            }
            
            return true;
        } catch (Exception e) {
            FLog.error("❌ Failed to ensure loader directory: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Copy native libraries to loader directory
     */
    private static void copyNativeLibraries(Context context, File loaderDir) {
        try {
            String nativeLibDir = context.getApplicationInfo().nativeLibraryDir;
            File nativeDir = new File(nativeLibDir);
            
            if (nativeDir.exists() && nativeDir.isDirectory()) {
                File[] libFiles = nativeDir.listFiles((dir, name) -> name.endsWith(".so"));
                if (libFiles != null) {
                    for (File libFile : libFiles) {
                        if (libFile.getName().contains("pubg") || 
                            libFile.getName().contains("bgmi") ||
                            libFile.getName().contains("Sdk")) {
                            
                            File destFile = new File(loaderDir, libFile.getName());
                            try {
                                com.blankj.molihuan.utilcode.util.FileUtils.copy(
                                    libFile.getAbsolutePath(), 
                                    destFile.getAbsolutePath()
                                );
                                FLog.info("✅ Copied library: " + libFile.getName());
                            } catch (Exception e) {
                                FLog.error("❌ Failed to copy " + libFile.getName() + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            FLog.error("❌ Failed to copy native libraries: " + e.getMessage());
        }
    }
}

