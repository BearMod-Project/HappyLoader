package com.happy.pro.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.airbnb.lottie.LottieAnimationView;
import com.blankj.molihuan.utilcode.util.ToastUtils;
import com.happy.pro.BuildConfig;
import com.happy.pro.activity.CrashHandler;
import com.happy.pro.activity.MainActivity;
//import com.craiyon.loader.overlay.MenuFloatingView;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.happy.pro.R;

import org.jdeferred.android.AndroidDeferredManager;

import com.happy.pro.libhelper.ApkEnv;

public class ActivityCompat extends AppCompatActivity {
    private static ActivityCompat activityCompat;
    public static int REQUEST_OVERLAY_PERMISSION = 5469;
    public static int PERMISSION_REQUEST_STORAGE = 100;
    public static int REQUEST_MANAGE_UNKNOWN_APP_SOURCES = 200;
    public boolean isLogin = false;
    public FPrefs prefs;
    private BottomSheetDialog bottomSheetDialog;
    public static String gamename;
    public static String name;
    public static int version;
    public static String url;
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
   
    public static ActivityCompat getActivityCompat() {
        return activityCompat;
    }

    protected static void requestPermissions(MainActivity mainActivity, String[] strings, int requestPermissions) {
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static void setExecutorService(ExecutorService executorService) {
        ActivityCompat.executorService = executorService;
    }

    public FPrefs getPref() {
        return FPrefs.with(this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityCompat = this;
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        setNavBar(R.color.background);

        prefs = getPref();

        ManageFiles();
    }
    
    public void setNavBar(int color){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(this,color));
    }
    
    @SuppressLint("WrongConstant")
    public void restartApp(String clazz) {
        Intent lauchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        assert lauchIntent != null;
        lauchIntent.addFlags(335577088);
        lauchIntent.putExtra("restartApp", clazz);
        startActivity(lauchIntent);
        Runtime.getRuntime().exit(0);
    }
    
    @SuppressLint("ResourceAsColor")
    public void toast(CharSequence msg) {
        ToastUtils _toast = ToastUtils.make();
        _toast.setBgColor(android.R.color.white);
        _toast.setLeftIcon(R.drawable.icon);
        //_toast.setGravity(Gravity.BOTTOM, Gravity.CENTER, Gravity.CENTER);
        _toast.setTextColor(android.R.color.black);
        _toast.setNotUseSystemToast();
        //_toast.setBgResource(R.drawable.button_coming);
        _toast.show(msg);
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    
    @SuppressLint("ResourceAsColor")
    public static void toastImage(int id, CharSequence msg) {
        ToastUtils _toast = ToastUtils.make();
        _toast.setBgColor(android.R.color.white);
        _toast.setLeftIcon(id);
        _toast.setTextColor(android.R.color.black);
        _toast.setNotUseSystemToast();
        _toast.show(msg);
    }
    
    public void RestartAppp() {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void ShowRestartApp() {
        showBottomSheetDialog(
                getResources().getDrawable(R.drawable.ic_check),
                "Download Success: Restart Loader",
                "The loader has been downloaded successfully. Please restart the loader now.",
                false,
                v -> {
                    MainActivity.get().doShowProgress(true);
                    RestartAppp();
                    dismissBottomSheetDialog();
                },
                null);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void takeFilePermissions() {
        new MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setTitle(R.string.file_access_title)
            .setMessage(R.string.file_access_message)
            .setPositiveButton(
                R.string.grant_permission,
                (d, w) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
            .setNegativeButton(
                R.string.exit,
                (d, w) -> {
                  finish();
                  System.exit(0);
                })
            .show();
    }
    
    public boolean isPermissionGaranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          return !Environment.isExternalStorageManager();
        } else {
          return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        }
    }
    
    public void InstllUnknownApp() {
        if (!getPackageManager().canRequestPackageInstalls()) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setMessage("Please allow Install Unknown App Source");
            builder.setPositiveButton("Yes", (p1, p2) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_MANAGE_UNKNOWN_APP_SOURCES);
            });
            builder.setCancelable(false);
            builder.show();
        } else {
            if (isPermissionGaranted()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    takeFilePermissions();
                }
            }
        }
    }
    
    public void OverlayPermision() {
        if (!Settings.canDrawOverlays(this)) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setMessage("Please allow permision floating");
            builder.setPositiveButton("Yes", (p1, p2) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            });
            builder.setCancelable(false);
            builder.show();
        } else {
            InstllUnknownApp();
        }
    }
    
    public void ManageFiles() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_STORAGE);
        } else {
            OverlayPermision();
        }
    }
    
    protected AndroidDeferredManager defer() {
        return UiKit.defer();
    }
    
    private long backPressedTime = 0; 
    
    @Override
    public void onBackPressed() {
        if (isLogin) {
            long t = System.currentTimeMillis();
            if (t - backPressedTime > 2000) {    // 2 secs
                backPressedTime = t;
                toast("Press back again to exit");
            } else {
                super.onBackPressed();
            }
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }
    
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    
    @SuppressLint("SetTextI18n")
    public static void doActionAnimation(LottieAnimationView lottie, TextView txt, String pkg) {
        txt.setText("Starting Client " + pkg + " ...");
        lottie.setAnimation(R.raw.anim_robot);
        lottie.animate().setStartDelay(5000);
        lottie.playAnimation();
    }
    
    public void launch(AlertDialog dialog, String pkg) {
        UiKit.defer().when(() -> {
            long startTime = System.currentTimeMillis();
            dialog.dismiss();
            long elapsedTime = System.currentTimeMillis() - startTime;
            long delta = 500L - elapsedTime;
            if (delta > 0) {
                UiKit.sleep(delta);
            }
        }).done((ree) -> {
            ApkEnv.getInstance().launchApk(pkg);
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void launchSplash(String pkg) {
        try {
            // First check if the package is installed
            if (!com.happy.pro.libhelper.ApkEnv.getInstance().isInstalled(pkg)) {
                FLog.error("❌ Game not installed: " + pkg);
                toastImage(R.drawable.ic_error, "Game not installed: " + pkg);
                return;
            }
            
            View view = getLayoutInflater().inflate(R.layout.launcher, null);
            CardView cv = view.findViewById(R.id.cv_lauch);
            TextView txt = view.findViewById(R.id.start_client);
            LottieAnimationView lottie = view.findViewById(R.id.animationRobot);

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setCancelable(false)
                   .setView(view)
                   .setBackground(this.getResources().getDrawable(R.drawable.background_trans));

            AlertDialog dialog = builder.create();
            dialog.show();

            // Set initial text
            txt.setText("Preparing BEAR-LOADER...");
            
            defer().when(() -> {
                long startTime = System.currentTimeMillis();
                
                // Update animation and text
                doActionAnimation(lottie, txt, pkg);
                
                // Wait for animation
                long elapsedTime = System.currentTimeMillis() - startTime;
                long delta = 3000L - elapsedTime; // Reduced from 5000L for better UX
                if (delta > 0) {
                    UiKit.sleep(delta);
                }
            }).done((ree) -> {
                // Update text before launching
                txt.setText("Launching " + pkg + "...");
                
                // Launch the app
                launch(dialog, pkg);
            }).fail(fa -> {
                FLog.error("❌ Launch animation failed: " + fa.getMessage());
                dialog.dismiss();
                toastImage(R.drawable.ic_error, "Launch failed");
            });

        } catch(Exception err) {
            FLog.error("❌ launchSplash error: " + err.getMessage());
            err.printStackTrace();
            toastImage(R.drawable.ic_error, "Failed to launch game");
        }
    }



    public void showBottomSheetDialog(Drawable icon, String title, String msg, boolean cancelable, View.OnClickListener listener, View.OnClickListener listenerCancle) {
        if (BuildConfig.VERSION_CODE == 200) {
            return;
        }
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setCancelable(cancelable);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout);
        
        ImageView img = bottomSheetDialog.findViewById(R.id.icon);
        if (icon != null) {
            assert img != null;
            img.setImageDrawable(icon);
        }
        TextView title_tv = bottomSheetDialog.findViewById(R.id.title);
        assert title_tv != null;
        title_tv.setText(title);
        TextView msg_tv = bottomSheetDialog.findViewById(R.id.msg);
        assert msg_tv != null;
        msg_tv.setText(msg);
        
        MaterialButton download = bottomSheetDialog.findViewById(R.id.btn);
        if (listener != null) {
            assert download != null;
            download.setOnClickListener(listener);
        }
        
        MaterialButton cancle = bottomSheetDialog.findViewById(R.id.btn_cancle);
        if (listenerCancle != null) {
            assert cancle != null;
            cancle.setOnClickListener(listenerCancle);
        } else {
            assert cancle != null;
            cancle.setVisibility(View.GONE);
        }
        
        bottomSheetDialog.show();
    }

    public void showBottomSheetDialog2(Drawable icon, String title, String msg, boolean cancelable, View.OnClickListener listener,View.OnClickListener updates, View.OnClickListener listenerCancle) {
        if (BuildConfig.VERSION_CODE == 200) {
            return;
        }
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setCancelable(cancelable);
        bottomSheetDialog.setContentView(R.layout.bottom_update);

        ImageView img = bottomSheetDialog.findViewById(R.id.icon);
        if (icon != null) {
            assert img != null;
            img.setImageDrawable(icon);
        }
        TextView title_tv = bottomSheetDialog.findViewById(R.id.title);
        assert title_tv != null;
        title_tv.setText(title);
        TextView msg_tv = bottomSheetDialog.findViewById(R.id.msg);
        assert msg_tv != null;
        msg_tv.setText(msg);

        MaterialButton download = bottomSheetDialog.findViewById(R.id.btn);
        if (listener != null) {
            assert download != null;
            download.setOnClickListener(listener);
        }

        MaterialButton updatess = bottomSheetDialog.findViewById(R.id.updateee);
        if (updates != null) {
            assert updatess != null;
            updatess.setOnClickListener(updates);
        }


        MaterialButton cancle = bottomSheetDialog.findViewById(R.id.btn_cancle);
        if (listenerCancle != null) {
            assert cancle != null;
            cancle.setOnClickListener(listenerCancle);
        } else {
            assert cancle != null;
            cancle.setVisibility(View.GONE);
        }

        bottomSheetDialog.show();
    }

    public void dismissBottomSheetDialog() {
        try {
        	if (bottomSheetDialog.isShowing()) {
                bottomSheetDialog.dismiss();
                bottomSheetDialog = null;
            }
        } catch(Exception err) {
        	FLog.error(err.getMessage());
        }
    }
    
}

