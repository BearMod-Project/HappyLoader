package com.happy.pro;

import static com.happy.pro.server.ApiServer.ApiKeyBox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.blankj.molihuan.utilcode.util.ToastUtils;
import com.happy.pro.activity.CrashHandler;
import com.happy.pro.utils.BuildCompat;
import com.happy.pro.utils.FLog;
import com.happy.pro.utils.FPrefs;
import com.happy.pro.utils.NetworkConnection;
import com.google.android.material.color.DynamicColors;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;

import com.happy.pro.libhelper.MetaStubs.MetaActivationManager;
import com.happy.pro.Component.MainService;

/**************************
 * BUILD ON Android Studio
 * TELEGRAM : OxZeroo
 * *************************/


public class BoxApplication extends MultiDexApplication {
    public static final String STATUS_BY = "online";
    public static BoxApplication gApp;
    private boolean isNetworkConnected = false;

    public static BoxApplication get() {
        return gApp;
    }

    public boolean isInternetAvailable() {
        return isNetworkConnected;
    }

    public void setInternetAvailable(boolean b) {
        isNetworkConnected = b;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
        FPrefs.with(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gApp = this;
        
        // Initialize loader directory early
        MainService.ensureLoaderDirectory(this);
        
        try {
            MetaActivationManager.activateSdk((new Object() {
                int t;

                @NonNull
                public String toString() {
                    byte[] buf = new byte[16];
                    t = -852817509;
                    buf[0] = (byte) (t >>> 3);
                    t = 346990136;
                    buf[1] = (byte) (t >>> 9);
                    t = -97806274;
                    buf[2] = (byte) (t >>> 15);
                    t = 575237568;
                    buf[3] = (byte) (t >>> 16);
                    t = 849788282;
                    buf[4] = (byte) (t >>> 15);
                    t = -263973642;
                    buf[5] = (byte) (t >>> 6);
                    t = -1569569754;
                    buf[6] = (byte) (t >>> 11);
                    t = -598298788;
                    buf[7] = (byte) (t >>> 9);
                    t = -988414233;
                    buf[8] = (byte) (t >>> 20);
                    t = 1201151192;
                    buf[9] = (byte) (t >>> 7);
                    t = 927789253;
                    buf[10] = (byte) (t >>> 16);
                    t = 71500930;
                    buf[11] = (byte) (t >>> 20);
                    t = -150651871;
                    buf[12] = (byte) (t >>> 10);
                    t = -1219959457;
                    buf[13] = (byte) (t >>> 4);
                    t = -987484954;
                    buf[14] = (byte) (t >>> 15);
                    t = -980547442;
                    buf[15] = (byte) (t >>> 20);
                    return new String(buf);
                }
            }.toString()));
        } catch (Exception e) {


            // Handle the exception, e.g., log an error message or take appropriate action
            e.printStackTrace(); // Print the stack trace for debugging
        }
        DynamicColors.applyToActivitiesIfAvailable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setCrashHandler();

        if (BuildCompat.isA11below()) {
            FLog.info("Android 11 below");
        } else {
            FLog.info("Android 12 above");
        }
        FLog.info("SDK INT: " + Build.VERSION.SDK_INT);
        FLog.info("SDK RELEASE: " + Build.VERSION.RELEASE);

        NetworkConnection.CheckInternet network = new NetworkConnection.CheckInternet(this);
        network.registerNetworkCallback();
    }


     /**********************************************************************************************
     * Setting the default uncaught exception handler that will handle all the uncaught exceptions.*
     ***********************************************************************************************/

    public void setCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
    }

    public boolean checkRootAccess() {
        if (Shell.rootAccess()) {
            FLog.info("Root granted");
            return true;
        } else {
            FLog.info("Root not granted");
            return false;
        }
    }

    public void doExe(String shell) {
        if (checkRootAccess()) {
            Shell.su(shell).exec();
        } else {
            try {
                Runtime.getRuntime().exec(shell);
                FLog.info("Shell: " + shell);
            } catch (IOException e) {
                FLog.error(e.getMessage());
            }
        }
    }

    public void doExecute(String shell) {
        doChmod(shell, 777);
        doExe(shell);
    }

    public void doChmod(String shell, int mask) {
        doExe("chmod " + mask + " " + shell);
    }

    public void toast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("ResourceAsColor")
    public void showToastWithImage(int id, CharSequence msg) {
        ToastUtils _toast = ToastUtils.make();
        _toast.setBgColor(android.R.color.white);
        _toast.setLeftIcon(id);
        _toast.setTextColor(android.R.color.black);
        _toast.setNotUseSystemToast();
        _toast.show(msg);
    }
}

