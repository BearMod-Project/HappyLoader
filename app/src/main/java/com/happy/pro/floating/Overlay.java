package com.happy.pro.floating;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.happy.pro.activity.MainActivity;
import com.happy.pro.utils.FLog;
import com.happy.pro.utils.FPrefs;

import java.io.IOException;

public class Overlay extends Service {

    static {
        try {
            System.loadLibrary("happy");
        } catch(UnsatisfiedLinkError w) {
            FLog.error(w.getMessage());
        }
    }
    
    public FPrefs getPref() {
        return FPrefs.with(this);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
	
	private native boolean getReady();
    private native void Close();
	public static native void DrawOn(ESPView espView, Canvas canvas);

    private WindowManager windowManager;
    private ESPView overlayView;
    private Overlay Instance;

    @SuppressLint("StaticFieldLeak")
    public static Context ctx;

    @SuppressLint("InflateParams")
    @Override
    public void onCreate() {
        super.onCreate();
        FLog.info("🚀 Overlay service onCreate() called");
        ctx = this;
        
        try {
            FLog.info("📡 Starting overlay initialization...");
            Start();
            FLog.info("✅ Start() completed successfully");
            
            windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            FLog.info("✅ WindowManager obtained");
            
            overlayView = new ESPView(ctx);
            FLog.info("✅ ESPView created");
            
            DrawCanvas();
            FLog.info("✅ DrawCanvas() completed - Overlay fully initialized");
        } catch (Exception e) {
            FLog.error("❌ Failed to initialize Overlay: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Close();
        if (overlayView != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }

    private void Start() {
        FLog.info("🔄 Start() method called - checking instance");
        if (Instance == null) {
            FLog.info("📡 Instance is null, starting initialization threads");
            
            new Thread(new Runnable() {
				@Override
				public void run() {
				    FLog.info("🚀 getReady() thread started");
					try {
					    boolean ready = getReady();
					    FLog.info("✅ getReady() returned: " + ready);
					} catch (Exception e) {
					    FLog.error("❌ Exception in getReady(): " + e.getMessage());
					    e.printStackTrace();
					}
				}
			}).start();
			
            new Thread(new Runnable() {
				@Override
				public void run() {
				    FLog.info("🚀 Shell command thread started");
					try {
						Thread.sleep(0);
						FLog.info("📞 Executing shell command: " + MainActivity.socket);
						Shell(MainActivity.socket);
						FLog.info("✅ Shell command executed");
					}
					catch (InterruptedException e) {
					    FLog.error("❌ Thread interrupted: " + e.getMessage());
						e.printStackTrace();
					} catch (Exception e) {
					    FLog.error("❌ Exception in shell execution: " + e.getMessage());
					    e.printStackTrace();
					}
				}
			}).start();
        } else {
            FLog.info("⚠️ Instance already exists, skipping initialization");
        }
    }
	
    private void DrawCanvas() {
        FLog.info("🎨 DrawCanvas() method called - setting up overlay view");
        
        try {
            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                FLog.info("📱 Using TYPE_APPLICATION_OVERLAY for API " + Build.VERSION.SDK_INT);
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
                FLog.info("📱 Using TYPE_SYSTEM_OVERLAY for API " + Build.VERSION.SDK_INT);
            }
    		
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
    			WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, 0, getNavigationBarHeight(), LAYOUT_FLAG,
    			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN, PixelFormat.RGBA_8888
    		);
    		FLog.info("✅ Layout params created");
    		
    		if (getPref().readBoolean("anti_recorder")) {
    			HideRecorder.setFakeRecorderWindowLayoutParams(params);
    			FLog.info("✅ Anti-recorder params applied");
            }
    		
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 0;
            params.y = 0;
    		//params.alpha = 0.8f;
    		FLog.info("✅ Position params set");
    		
    		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                FLog.info("✅ Display cutout mode set for API " + Build.VERSION.SDK_INT);
    		}
    		
            windowManager.addView(overlayView, params);
            FLog.info("✅ Overlay view successfully added to window manager");
            
        } catch (Exception e) {
            FLog.error("❌ Exception in DrawCanvas(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getNavigationBarHeight() {
        boolean hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey();
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0 && !hasMenuKey) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private void Shell(String str) {
        try {
            Runtime.getRuntime().exec(str);
        }
		catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public static boolean getConfig(String key) {
        SharedPreferences sp = ctx.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getBoolean(key, false);
    }
}

