package com.happy.pro.floating;

import static com.happy.pro.activity.MainActivity.bitversi;
import static com.happy.pro.activity.MainActivity.game;
import static com.happy.pro.activity.MainActivity.gameint;
import static com.happy.pro.activity.MainActivity.kernel;
import static com.happy.pro.activity.ModeActivity.Kooontoool;
import static com.happy.pro.floating.FloatRei.toastImage;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.happy.pro.R;
import com.happy.pro.activity.MainActivity;
import com.happy.pro.utils.FLog;
import com.topjohnwu.superuser.Shell;

import java.util.Locale;



/**************************
 * BUILD ON Android Studio
 * TELEGRAM : OxZeroo
 * *************************/

public class FloatService extends Service {

    public static int islandint = 0;

    static {
        try {
            System.loadLibrary("happy");
        } catch (UnsatisfiedLinkError w) {
            FLog.error(w.getMessage());
        }
    }

    Context ctx;
    private View mainView;
    private PowerManager.WakeLock mWakeLock;
    private WindowManager windowManagerMainView;
    private WindowManager.LayoutParams paramsMainView;
    private LinearLayout layout_main_view;
    private RelativeLayout layout_icon_control_view;

    private static int getLayoutType() {
        int LAYOUT_FLAG;
        LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        return LAYOUT_FLAG;
    }

    private void setLokasi(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("bahasa", lang);
        editor.apply();

    }

    private void loadbahasa() {
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String bahasa = sharedPreferences.getString("bahasa", "");
        setLokasi(bahasa);
    }

    private void StartAimTouch() {
        startService(new Intent(getApplicationContext(), ToggleSimulation.class));
    }

    private void StopAimTouch() {
        stopService(new Intent(getApplicationContext(), ToggleSimulation.class));
    }

    private void StartAimFloat() {
        startService(new Intent(getApplicationContext(), ToggleAim.class));
    }

    private void StopAimFloat() {
        stopService(new Intent(getApplicationContext(), ToggleAim.class));
    }

    private void StartAimBulletFloat() {
        startService(new Intent(getApplicationContext(), ToggleBullet.class));
    }

    private void StopAimBulletFloat() {
        stopService(new Intent(getApplicationContext(), ToggleBullet.class));
    }

    public native void SettingValue(int setting_code, boolean value);

    public native void SettingMemory(int setting_code, boolean value);

    public native void SettingAim(int setting_code, boolean value);

    public native void RadarSize(int size);

    public native void Range(int range);

    public native void recoil(int recoil);

    public native void recoil2(int recoil);

    public native void recoil3(int recoil);

    public native void Target(int target);

    public native void AimBy(int aimby);

    public native void AimWhen(int aimwhen);

    public native void distances(int distances);

    public native void Bulletspeed(int bulletspeed);

    public native void WideView(int wideview);

    public native void AimingSpeed(int aimingspeed);

    public native void Smoothness(int smoothness);

    public native void TouchSize(int touchsize);

    public native void TouchPosX(int touchposx);

    public native void TouchPosY(int touchposy);


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = getApplicationContext();
        InitShowMainView();
        loadbahasa();
        FLog.info("🚀 FloatService created");
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            FLog.info("📱 Configuration change detected");
            
            // Handle screen orientation changes safely
            if (mainView != null && windowManagerMainView != null) {
                // Close overlay temporarily to prevent display issues
                closeOverlay();
                
                // Handle positioning for new screen dimensions after a delay
                mainView.postDelayed(() -> {
                    try {
                        handleScreenSurfaceAdjustment();
                        FLog.info("✅ Screen orientation change handled successfully");
                    } catch (Exception e) {
                        FLog.error("❌ Post-configuration adjustment failed: " + e.getMessage());
                    }
                }, 100); // Small delay to let the system settle
                
            } else {
                FLog.warning("⚠️ MainView or WindowManager not available during configuration change");
            }
        } catch (Exception e) {
            FLog.error("❌ Configuration change error: " + e.getMessage());
            // Don't let configuration changes crash the service
        }
    }


    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    private void InitShowMainView() {
        // Use the original working layout
        mainView = LayoutInflater.from(this).inflate(R.layout.float_service, null);
        paramsMainView = getparams();
        
        // Load saved position before adding to window manager
        loadFloatingPosition();
        
        windowManagerMainView = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManagerMainView.addView(mainView, paramsMainView);
        
        layout_icon_control_view = mainView.findViewById(R.id.layout_icon_control_view);
        layout_main_view = mainView.findViewById(R.id.layout_main_view);

        View layout_close_main_view = mainView.findViewById(R.id.layout_close_main_view);
        layout_close_main_view.setOnClickListener(p1 -> {
            closeOverlay();
        });

        LinearLayout layout_view = mainView.findViewById(R.id.layout_view);
        layout_view.setOnTouchListener(onTouchListener());
        
        // Add outside touch detection for auto-close
        mainView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                closeOverlay();
                return true;
            }
            return false;
        });
        
        // Add back button support when overlay is focused
        mainView.setFocusableInTouchMode(true);
        mainView.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                if (!isViewCollapsed()) {
                    closeOverlay();
                    return true;
                }
            }
            return false;
        });

            // Initialize original horizontal design
        initDesign();
        visual(mainView);
        aimbot(mainView);
        items(mainView);
        memory(mainView);
    }

    void animation(View v){
        // Ultra-fast, lightweight touch feedback animation with hardware acceleration
        v.animate()
            .scaleX(0.96f)
            .scaleY(0.96f)
            .setDuration(80)
            .withLayer() // Hardware acceleration
            .withEndAction(() -> {
                v.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(80)
                    .withLayer()
                    .start();
            })
            .start();
    }

    void effectnav(LinearLayout a, LinearLayout b, LinearLayout c, LinearLayout d){
        a.setBackgroundResource(R.drawable.buttonshape);
        b.setBackgroundResource(R.drawable.background_trans);
        c.setBackgroundResource(R.drawable.background_trans);
        d.setBackgroundResource(R.drawable.background_trans);
    }



    // Original horizontal tab design
    public void initDesign() {
        // Original horizontal tab navigation
        LinearLayout fvisual = mainView.findViewById(R.id.fvisual);
        LinearLayout fitems = mainView.findViewById(R.id.fitems);
        LinearLayout faimbot = mainView.findViewById(R.id.faimbot);
        LinearLayout fmemory = mainView.findViewById(R.id.fmemory);
        
        // Content areas
        LinearLayout menuf1 = mainView.findViewById(R.id.menuf1);
        LinearLayout menuf2 = mainView.findViewById(R.id.menuf2);
        LinearLayout menuf3 = mainView.findViewById(R.id.menuf3);
        LinearLayout menuf4 = mainView.findViewById(R.id.menuf4);

        // Tab click handlers with original design
        if (fvisual != null) {
            fvisual.setOnClickListener(v -> {
                switchOriginalTab(0, menuf1, menuf2, menuf3, menuf4);
                effectnav(fvisual, fitems, faimbot, fmemory);
                animation(v);
            });
        }

        if (fitems != null) {
            fitems.setOnClickListener(v -> {
                switchOriginalTab(1, menuf1, menuf2, menuf3, menuf4);
                effectnav(fitems, fvisual, faimbot, fmemory);
                animation(v);
            });
        }

        if (faimbot != null) {
            faimbot.setOnClickListener(v -> {
                switchOriginalTab(2, menuf1, menuf2, menuf3, menuf4);
                effectnav(faimbot, fvisual, fitems, fmemory);
                animation(v);
            });
        }

        if (fmemory != null) {
            fmemory.setOnClickListener(v -> {
                switchOriginalTab(3, menuf1, menuf2, menuf3, menuf4);
                effectnav(fmemory, fvisual, fitems, faimbot);
                animation(v);
            });
        }

        // Set initial state - Visual tab active
        if (fvisual != null && menuf1 != null) {
            switchOriginalTab(0, menuf1, menuf2, menuf3, menuf4);
            effectnav(fvisual, fitems, faimbot, fmemory);
        }
    }

    private void switchOriginalTab(int tabIndex, LinearLayout... contentAreas) {
        // Hide all content areas
        for (LinearLayout content : contentAreas) {
            if (content != null) {
                content.setVisibility(View.GONE);
            }
        }
        
        // Show selected content area with fade animation
        if (tabIndex < contentAreas.length && contentAreas[tabIndex] != null) {
            LinearLayout selectedContent = contentAreas[tabIndex];
            selectedContent.setAlpha(0.8f);
            selectedContent.setVisibility(View.VISIBLE);
            selectedContent.animate().alpha(1f).setDuration(200).start();
        }
    }


    private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {
            final View collapsedView = layout_icon_control_view;
            final View expandedView = layout_main_view;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = paramsMainView.x;
                        initialY = paramsMainView.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);
                        if (Math.abs(Xdiff) < 10 && Math.abs(Ydiff) < 10) {
                            if (isViewCollapsed()) {
                                // Animate opening
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setAlpha(0.7f);
                                expandedView.setScaleX(0.9f);
                                expandedView.setScaleY(0.9f);
                                expandedView.setVisibility(View.VISIBLE);
                                expandedView.animate()
                                    .alpha(1f)
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(200)
                                    .withLayer()
                                    .start();
                                FLog.info("📱 Floating overlay opened");
                            }
                        } else {
                            // Handle edge snapping and screen boundary detection
                            handleScreenSurfaceAdjustment();
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        // Calculate new position
                        int newX = initialX + (int) (event.getRawX() - initialTouchX);
                        int newY = initialY + (int) (event.getRawY() - initialTouchY);
                        
                        // Apply screen boundary constraints
                        android.graphics.Point screenSize = getScreenSize();
                        int viewWidth = mainView.getWidth();
                        int viewHeight = mainView.getHeight();
                        
                        // Constrain to screen boundaries
                        newX = Math.max(0, Math.min(newX, screenSize.x - viewWidth));
                        newY = Math.max(0, Math.min(newY, screenSize.y - viewHeight));
                        
                        paramsMainView.x = newX;
                        paramsMainView.y = newY;
                        windowManagerMainView.updateViewLayout(mainView, paramsMainView);
                        return true;

                }
                return false;
            }
        };
    }

    /**
     * Get screen size for boundary detection
     */
    private android.graphics.Point getScreenSize() {
        try {
            android.graphics.Point size = new android.graphics.Point();
            if (windowManagerMainView != null) {
                windowManagerMainView.getDefaultDisplay().getSize(size);
                
                // Validate the size
                if (size.x > 0 && size.y > 0) {
                    return size;
                }
            }
            
            // Fallback to default screen size if WindowManager fails
            FLog.warning("⚠️ Using fallback screen size");
            size.x = 720; // Reasonable default width
            size.y = 1280; // Reasonable default height
            return size;
            
        } catch (Exception e) {
            FLog.error("❌ Failed to get screen size: " + e.getMessage());
            // Return safe fallback
            android.graphics.Point fallback = new android.graphics.Point();
            fallback.x = 720;
            fallback.y = 1280;
            return fallback;
        }
    }

    /**
     * Handle automatic screen surface adjustments (edge snapping, positioning)
     */
    private void handleScreenSurfaceAdjustment() {
        try {
            android.graphics.Point screenSize = getScreenSize();
            int viewWidth = mainView.getWidth();
            int viewHeight = mainView.getHeight();
            
            // Get current position
            int currentX = paramsMainView.x;
            int currentY = paramsMainView.y;
            
            // Edge snapping threshold (within 50px of edge)
            int snapThreshold = 50;
            
            // Calculate distances to each edge
            int distanceToLeft = currentX;
            int distanceToRight = screenSize.x - (currentX + viewWidth);
            int distanceToTop = currentY;
            int distanceToBottom = screenSize.y - (currentY + viewHeight);
            
            // Determine if we should snap to an edge
            boolean snapToEdge = false;
            int targetX = currentX;
            int targetY = currentY;
            
            // Horizontal edge snapping
            if (distanceToLeft < snapThreshold && distanceToLeft < distanceToRight) {
                targetX = 0; // Snap to left edge
                snapToEdge = true;
                FLog.info("🧲 Snapping to left edge");
            } else if (distanceToRight < snapThreshold && distanceToRight < distanceToLeft) {
                targetX = screenSize.x - viewWidth; // Snap to right edge
                snapToEdge = true;
                FLog.info("🧲 Snapping to right edge");
            }
            
            // Vertical edge snapping (keep current Y position or adjust if needed)
            if (currentY < 0) {
                targetY = 0;
                snapToEdge = true;
            } else if (currentY + viewHeight > screenSize.y) {
                targetY = screenSize.y - viewHeight;
                snapToEdge = true;
            }
            
            // Apply edge snapping with smooth animation
            if (snapToEdge) {
                animateToPosition(targetX, targetY);
            }
            
            // Save position preference for next startup
            saveFloatingPosition(paramsMainView.x, paramsMainView.y);
            
        } catch (Exception e) {
            FLog.error("❌ Screen surface adjustment failed: " + e.getMessage());
        }
    }

    /**
     * Animate floating overlay to target position
     */
    private void animateToPosition(int targetX, int targetY) {
        try {
            // Create animator for smooth transition
            ObjectAnimator animatorX = ObjectAnimator.ofInt(paramsMainView, "x", paramsMainView.x, targetX);
            ObjectAnimator animatorY = ObjectAnimator.ofInt(paramsMainView, "y", paramsMainView.y, targetY);
            
            animatorX.setDuration(200);
            animatorY.setDuration(200);
            
            animatorX.addUpdateListener(animation -> {
                paramsMainView.x = (int) animation.getAnimatedValue();
                windowManagerMainView.updateViewLayout(mainView, paramsMainView);
            });
            
            animatorY.addUpdateListener(animation -> {
                paramsMainView.y = (int) animation.getAnimatedValue();
                windowManagerMainView.updateViewLayout(mainView, paramsMainView);
            });
            
            animatorX.start();
            animatorY.start();
            
        } catch (Exception e) {
            // Fallback to immediate positioning
            paramsMainView.x = targetX;
            paramsMainView.y = targetY;
            windowManagerMainView.updateViewLayout(mainView, paramsMainView);
        }
    }

    /**
     * Save floating overlay position for persistence
     */
    private void saveFloatingPosition(int x, int y) {
        SharedPreferences sp = getSharedPreferences("floatingPosition", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("floating_x", x);
        ed.putInt("floating_y", y);
        ed.apply();
    }

    /**
     * Load saved floating overlay position
     */
    private void loadFloatingPosition() {
        try {
            SharedPreferences sp = getSharedPreferences("floatingPosition", Context.MODE_PRIVATE);
            int savedX = sp.getInt("floating_x", 0);
            int savedY = sp.getInt("floating_y", 100);
            
            paramsMainView.x = savedX;
            paramsMainView.y = savedY;
            
            FLog.info("📍 Overlay positioned at: " + savedX + "," + savedY);
            
        } catch (Exception e) {
            FLog.error("❌ Failed to load floating position: " + e.getMessage());
            // Fallback to safe default position
            paramsMainView.x = 0;
            paramsMainView.y = 100;
        }
    }

    private boolean isViewCollapsed() {
        return mainView == null || layout_icon_control_view.getVisibility() == View.VISIBLE;
    }
    


    /**
     * Close the floating overlay menu with animation
     */
    private void closeOverlay() {
        try {
            if (layout_main_view != null && layout_icon_control_view != null && 
                layout_main_view.getVisibility() == View.VISIBLE) {
                
                // Animate out the main view
                layout_main_view.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        try {
                            if (layout_main_view != null && layout_icon_control_view != null) {
                                layout_main_view.setVisibility(View.GONE);
                                layout_icon_control_view.setVisibility(View.VISIBLE);
                                layout_main_view.setAlpha(1f);
                                layout_main_view.setScaleX(1f);
                                layout_main_view.setScaleY(1f);
                            }
                        } catch (Exception e) {
                            FLog.error("❌ Animation end action error: " + e.getMessage());
                        }
                    })
                    .start();
                    
                FLog.info("🔒 Floating overlay closed with animation");
            } else if (layout_main_view != null && layout_icon_control_view != null) {
                // Direct close without animation
                layout_main_view.setVisibility(View.GONE);
                layout_icon_control_view.setVisibility(View.VISIBLE);
                FLog.info("🔒 Floating overlay closed (direct)");
            } else {
                FLog.warning("⚠️ Cannot close overlay - views not available");
            }
        } catch (Exception e) {
            FLog.error("❌ Close overlay error: " + e.getMessage());
            // Fallback immediate close with null checks
            try {
                if (layout_main_view != null) {
                    layout_main_view.setVisibility(View.GONE);
                }
                if (layout_icon_control_view != null) {
                    layout_icon_control_view.setVisibility(View.VISIBLE);
                }
            } catch (Exception fallbackError) {
                FLog.error("❌ Fallback close also failed: " + fallbackError.getMessage());
            }
        }
    }

    @SuppressLint("RtlHardcoded")
    private WindowManager.LayoutParams getparams() {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getLayoutType(),
                getFlagsType(),
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;

        return params;
    }

    private int getFlagsType() {
        // Add flags for better touch handling and outside touch detection
        return WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
               WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
               WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            // Clean up resources
            if (mWakeLock != null) {
                mWakeLock.release();
                mWakeLock = null;
            }

            if (mainView != null && windowManagerMainView != null) {
                windowManagerMainView.removeView(mainView);
                mainView = null;
            }
            
            // Clear cached references
            layout_main_view = null;
            layout_icon_control_view = null;
            
            FLog.info("🛑 FloatService destroyed and cleaned up");
        } catch (Exception e) {
            FLog.error("❌ Error during FloatService destruction: " + e.getMessage());
        }
    }
    
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // Auto-close overlay when system is low on memory or app goes to background
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            closeOverlay();
            FLog.info("💾 Memory trim - overlay closed");
        }
    }

    boolean getConfig(String key) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getBoolean(key, false);
    }

    private int getFps() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("fps", 100);
    }

    private void setFps(int fps) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("fps", fps);
        ed.apply();
    }

    private void setValue(String key, boolean b) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean(key, b);
        ed.apply();

    }

    private void setradarSize(int radarSize) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("radarSize", radarSize);
        ed.apply();
    }

    private int getradarSize() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("radarSize", 0);
    }

    private int getrangeAim() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("getrangeAim", 0);
    }

    private void getrangeAim(int getrangeAim) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("getrangeAim", getrangeAim);
        ed.apply();
    }

    private int getDistances() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("Distances", 0);
    }

    private void setDistances(int Distances) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("Distances", Distances);
        ed.apply();
    }

    private int getrecoilAim() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("getrecoilAim", 0);
    }

    private void getrecoilAim(int getrecoilAim) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("getrecoilAim", getrecoilAim);
        ed.apply();
    }

    private int getrecoilAim2() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("getrecoilAim2", 0);
    }

    private void getrecoilAim2(int getrecoilAim) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("getrecoilAim2", getrecoilAim);
        ed.apply();
    }

    private int getrecoilAim3() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("getrecoilAim2", 0);
    }

    private void getrecoilAim3(int getrecoilAim) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("getrecoilAim2", getrecoilAim);
        ed.apply();
    }

    private int getbulletspeedAim() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("getbulletspeedAim", 0);
    }

    private void getbulletspeedAim(int getbulletspeedAim) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("getbulletspeedAim", getbulletspeedAim);
        ed.apply();
    }

    private int getwideview() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("getwideview", 0);
    }

    private void getwideview(int getwideview) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("getwideview", getwideview);
        ed.apply();
    }

    int getTouchSize() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("touchsize", 600);
    }

    void setTouchSize(int touchsize) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("touchsize", touchsize);
        ed.apply();
    }

    int getTouchPosX() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("posX", 650);
    }

    void setTouchPosX(int posX) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("posX", posX);
        ed.apply();
    }

    int getTouchPosY() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("posY", 1400);
    }

    void setTouchPosY(int posY) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("posY", posY);
        ed.apply();
    }

    private boolean getConfigitem(String key, boolean a) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getBoolean(key, a);
    }

    private void setConfigitem(String a, boolean b) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean(a, b);
        ed.apply();
    }

    private int getEspValue(String a, int b) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt(a, b);
    }

    private void setEspValue(String a, int b) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt(a, b);
        ed.apply();
    }

    private int getAimSpeed() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("AimingSpeed", 750);
    }

    private void setAimSpeed(int AimingSpeed) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("AimingSpeed", AimingSpeed);
        ed.apply();
    }

    private int getSmoothness() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("smoothness", 20);
    }

    private void setSmoothness(int smoothness) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("smoothness", smoothness);
        ed.apply();
    }

    public void toggleesp(final ToggleButton a, final int b) {
        a.setChecked(getConfig((String) a.getText()));
        SettingValue(b, getConfig((String) a.getText()));
        a.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            setValue(String.valueOf(a.getText()), a.isChecked());
            SettingValue(b, a.isChecked());
        });
    }

    public void espvisual(final CheckBox a, final int b) {
        a.setChecked(getConfig((String) a.getText()));
        SettingValue(b, getConfig((String) a.getText()));
        a.setOnCheckedChangeListener((p1, p2) -> {
            setValue(String.valueOf(a.getText()), a.isChecked());
            SettingValue(b, a.isChecked());
        });
    }

    public void setaim(final ToggleButton a, final int b) {
        a.setOnCheckedChangeListener((p1, isChecked) -> {
            setValue(String.valueOf(a.getText()), a.isChecked());
            SettingAim(b, a.isChecked());
        });
    }

    public void vehicless(final ToggleButton checkBox) {
        checkBox.setChecked(getConfig((String) checkBox.getText()));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> setValue(String.valueOf(checkBox.getText()), checkBox.isChecked()));
    }

    public void itemss(final ToggleButton checkBox) {
        checkBox.setChecked(getConfig((String) checkBox.getText()));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> setValue(String.valueOf(checkBox.getText()), checkBox.isChecked()));
    }

    public void memory(final ToggleButton a, final int b) {
        a.setOnCheckedChangeListener((p1, isChecked) -> {
            setValue(String.valueOf(a.getText()), a.isChecked());
            SettingMemory(b, a.isChecked());
        });
    }

    void setupSeekBar(final SeekBar seekBar, final TextView textView, final int initialValue, final Runnable onChangeFunction) {
        seekBar.setProgress(initialValue);
        textView.setText(String.valueOf(initialValue));
        onChangeFunction.run();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.valueOf(progress));
                onChangeFunction.run();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void DrawESP() {
        if (Shell.rootAccess()) {
            FLog.info("Root granted");
            MainActivity.socket = "su -c " + MainActivity.daemonPath;
            startService(new Intent(this, Overlay.class));
        } else {
            FLog.info("Root not granted");
            MainActivity.socket = MainActivity.daemonPath;
            startService(new Intent(MainActivity.get(), Overlay.class));
        }
    }

    public void Exec(String path, String toast) {
        try {
            ExecuteElf("su -c chmod 777 " + getFilesDir() + path);
            ExecuteElf("su -c " + getFilesDir() + path);
            ExecuteElf("chmod 777 " + getFilesDir() + path);
            ExecuteElf(getFilesDir() + path);
            toastImage(R.drawable.ic_check, toast);
        } catch (Exception ignored) {
        }
    }

    private void ExecuteElf(String shell) {
        try {
            Runtime.getRuntime().exec(shell, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void StopESP() {
        stopService(new Intent(this, Overlay.class));
    }

    @SuppressLint("SetTextI18n")
    private void visual(View visual) {
        final ToggleButton drawesp = visual.findViewById(R.id.isenableesp);
        final LinearLayout menuisland = visual.findViewById(R.id.menuisland);
        final LinearLayout menuloho = visual.findViewById(R.id.menuloho);
        final TextView textisland = visual.findViewById(R.id.textisland);
        final TextView textlogo = visual.findViewById(R.id.textlogo);
        final ImageView img1 = visual.findViewById(R.id.img1);
        final ImageView img2 = visual.findViewById(R.id.img2);

        if (Shell.rootAccess()) {
            menuloho.setVisibility(View.GONE);
        } else {
            menuloho.setVisibility(View.VISIBLE);
        }

        if (!Kooontoool) {
            menuloho.setAlpha(0.7f);
            menuisland.setAlpha(0.7f);
        }

        drawesp.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                DrawESP();
            } else {
                StopESP();
                StopAimFloat();
                StopAimBulletFloat();
                StopAimTouch();
            }
        });

        if (!Kooontoool) {
            img2.setBackgroundResource(R.drawable.baseline_lock_24);
            img1.setBackgroundResource(R.drawable.baseline_lock_24);
        }

        menuisland.setOnClickListener(v -> {
            if (Kooontoool) {
                if (islandint == 0) {
                    Animator scale = ObjectAnimator.ofPropertyValuesHolder(v,
                            PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 0.5f, 1),
                            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0.5f, 1));
                    scale.setDuration(1000);
                    scale.start();
                    if (bitversi == 64) {
                        if (gameint == 5) {
                            Exec("/TW " + game + " 003", "REPORT 64 BLOCK ENABLE");
                        } else {
                            Exec("/TW " + game + " 006", "REPORT 64 BLOCK ENABLE");
                        }
                    }
                    islandint = 1;
                    textisland.setText("Island Block Enable");
                } else if (islandint == 1) {
                    Animator scale = ObjectAnimator.ofPropertyValuesHolder(v,
                            PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 0.5f, 1),
                            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0.5f, 1));
                    scale.setDuration(1000);
                    scale.start();
                    if (bitversi == 64) {
                        if (gameint == 5) {
                            Exec("/TW " + game + " 004", "REPORT 64 BLOCK DISABLE");
                        } else {
                            Exec("/TW " + game + " 007", "REPORT 64 BLOCK DISABLE");
                        }
                    }
                    islandint = 0;
                    textisland.setText("Island Block Disable");
                }
            }
        });

        menuloho.setOnClickListener(v -> {
            if (Kooontoool) {
                Animator scale = ObjectAnimator.ofPropertyValuesHolder(v,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 0.5f, 1),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0.5f, 1));
                scale.setDuration(1000);
                scale.start();
                if (bitversi == 64) {
                    if (gameint == 5) {
                        Exec("/TW " + game + " 005", "BYPASS 64 ENABLE");
                    } else {
                        Exec("/TW " + game + " 002", "BYPASS 64 ENABLE");
                    }
                } else if (bitversi == 32) {
                    Exec("/TW " + game + " 33", "BYPASS 32 ENABLE");
                }
                textlogo.setText("Protection Cheat Enable");
            }
        });

        final SeekBar radarSizeSeekBar = visual.findViewById(R.id.strokeradar);
        final TextView radarSizeText = visual.findViewById(R.id.radartext);

        setupSeekBar(radarSizeSeekBar, radarSizeText, getradarSize(), () -> {
            int pos = radarSizeSeekBar.getProgress();
            setradarSize(pos);
            RadarSize(pos);
            String a = String.valueOf(pos);
            radarSizeText.setText(a);
        });


        final ToggleButton fps1 = mainView.findViewById(R.id.fps30);
        final ToggleButton fps2 = mainView.findViewById(R.id.fps60);
        final ToggleButton fps3 = mainView.findViewById(R.id.fps90);
        final ToggleButton fps4 = mainView.findViewById(R.id.fps120);
        int CheckFps = getFps();
        if (CheckFps == 30) {
            fps1.setChecked(true);
            ESPView.sleepTime = 1000 / 30;
        } else if (CheckFps == 60) {
            fps2.setChecked(true);
            ESPView.sleepTime = 1000 / 60;
        } else if (CheckFps == 90) {
            fps3.setChecked(true);
            ESPView.sleepTime = 1000 / 90;
        } else if (CheckFps == 120) {
            fps4.setChecked(true);
            ESPView.sleepTime = 1000 / 120;
        } else {
            fps1.setChecked(true);
            ESPView.sleepTime = 1000 / 30;
        }

        fps1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                fps2.setChecked(false);
                fps3.setChecked(false);
                fps4.setChecked(false);
                setFps(30);
                ESPView.ChangeFps(30);
            }
        });

        fps2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                fps1.setChecked(false);
                fps3.setChecked(false);
                fps4.setChecked(false);
                setFps(60);
                ESPView.ChangeFps(60);
            }
        });

        fps3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                fps2.setChecked(false);
                fps1.setChecked(false);
                fps4.setChecked(false);
                setFps(90);
                ESPView.ChangeFps(90);
            }
        });

        fps4.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                fps2.setChecked(false);
                fps3.setChecked(false);
                fps1.setChecked(false);
                setFps(120);
                ESPView.ChangeFps(120);
            }
        });


        final ToggleButton isLine = visual.findViewById(R.id.isline);
        toggleesp(isLine, 2);
        final ToggleButton isbox = visual.findViewById(R.id.isBox);
        toggleesp(isbox, 3);
        final ToggleButton isskeleton = visual.findViewById(R.id.isskeleton);
        toggleesp(isskeleton, 4);
        final ToggleButton isdistance = visual.findViewById(R.id.isdistance);
        toggleesp(isdistance, 5);
        final ToggleButton ishealth = visual.findViewById(R.id.ishealth);
        toggleesp(ishealth, 6);
        final ToggleButton isname = visual.findViewById(R.id.isName);
        toggleesp(isname, 7);
        final ToggleButton ishead = visual.findViewById(R.id.ishead);
        toggleesp(ishead, 8);
        final ToggleButton isalert = visual.findViewById(R.id.isalert);
        toggleesp(isalert, 9);
        final ToggleButton isweapon = visual.findViewById(R.id.isweapon);
        toggleesp(isweapon, 10);
        final ToggleButton isthrowables = visual.findViewById(R.id.isthrowables);
        toggleesp(isthrowables, 11);
        final ToggleButton isnobot = visual.findViewById(R.id.isnobot);
        toggleesp(isnobot, 15);
        final ToggleButton isweaponicon = visual.findViewById(R.id.isweaponicon);
        toggleesp(isweaponicon, 16);
        final ToggleButton isLootBox = visual.findViewById(R.id.isLootBox);
        toggleesp(isLootBox, 14);

    }

    private void items(View items) {
        LinearLayout menui1 = items.findViewById(R.id.items1);
        LinearLayout menui2 = items.findViewById(R.id.lyvehicle);
        View bottomi1 = items.findViewById(R.id.bottomi1);
        View bottomi2 = items.findViewById(R.id.bottomi2);
        LinearLayout navi1 = items.findViewById(R.id.navitems);
        LinearLayout navi2 = items.findViewById(R.id.navvehicle);

        navi1.setOnClickListener(v -> {
            menui1.setVisibility(View.VISIBLE);
            menui2.setVisibility(View.GONE);
            bottomi1.setVisibility(View.VISIBLE);
            bottomi2.setVisibility(View.GONE);
        });

        navi2.setOnClickListener(v -> {
            menui1.setVisibility(View.GONE);
            menui2.setVisibility(View.VISIBLE);
            bottomi1.setVisibility(View.GONE);
            bottomi2.setVisibility(View.VISIBLE);
        });

        /*final ToggleButton lootbox = items.findViewById(R.id.lootbox);
        espvisual(lootbox, 14);*/

        final ToggleButton Desert = items.findViewById(R.id.Desert);
        itemss(Desert);

        final ToggleButton M416 = items.findViewById(R.id.m416);
        itemss(M416);

        final ToggleButton QBZ = items.findViewById(R.id.QBZ);
        itemss(QBZ);

        final ToggleButton SCARL = items.findViewById(R.id.SCARL);
        itemss(SCARL);

        final ToggleButton AKM = items.findViewById(R.id.AKM);
        itemss(AKM);

        final ToggleButton M16A4 = items.findViewById(R.id.M16A4);
        itemss(M16A4);

        final ToggleButton AUG = items.findViewById(R.id.AUG);
        itemss(AUG);

        final ToggleButton M249 = items.findViewById(R.id.M249);
        itemss(M249);

        final ToggleButton Groza = items.findViewById(R.id.Groza);
        itemss(Groza);

        final ToggleButton MK47 = items.findViewById(R.id.MK47);
        itemss(MK47);

        final ToggleButton M762 = items.findViewById(R.id.M762);
        itemss(M762);

        final ToggleButton G36C = items.findViewById(R.id.G36C);
        itemss(G36C);

        final ToggleButton DP28 = items.findViewById(R.id.DP28);
        itemss(DP28);

        final ToggleButton MG3 = items.findViewById(R.id.MG3);
        itemss(MG3);

        final ToggleButton FAMAS = items.findViewById(R.id.FAMAS);
        itemss(FAMAS);


        final ToggleButton HoneyBadger = items.findViewById(R.id.HoneyBadger);
        itemss(HoneyBadger);


        final ToggleButton AC32 = items.findViewById(R.id.AC32);
        itemss(AC32);


        //SMG

        final ToggleButton UMP = items.findViewById(R.id.UMP);
        itemss(UMP);

        final ToggleButton bizon = items.findViewById(R.id.bizon);
        itemss(bizon);

        final ToggleButton MP5K = items.findViewById(R.id.MP5K);
        itemss(MP5K);

        final ToggleButton TommyGun = items.findViewById(R.id.TommyGun);
        itemss(TommyGun);

        final ToggleButton vector = items.findViewById(R.id.vector);
        itemss(vector);

        final ToggleButton P90 = items.findViewById(R.id.P90);
        itemss(P90);

        final ToggleButton UZI = items.findViewById(R.id.UZI);
        itemss(UZI);


        //Snipers

        final ToggleButton AWM = items.findViewById(R.id.AWM);
        itemss(AWM);

        final ToggleButton QBU = items.findViewById(R.id.QBU);
        itemss(QBU);

        final ToggleButton Kar98k = items.findViewById(R.id.Kar98k);
        itemss(Kar98k);

        final ToggleButton M24 = items.findViewById(R.id.M24);
        itemss(M24);

        final ToggleButton SLR = items.findViewById(R.id.SLR);
        itemss(SLR);

        final ToggleButton SKS = items.findViewById(R.id.SKS);
        itemss(SKS);

        final ToggleButton MK14 = items.findViewById(R.id.MK14);
        itemss(MK14);

        final ToggleButton Mini14 = items.findViewById(R.id.Mini14);
        itemss(Mini14);

        final ToggleButton Mosin = items.findViewById(R.id.Mosin);
        itemss(Mosin);

        final ToggleButton VSS = items.findViewById(R.id.VSS);
        itemss(VSS);

        final ToggleButton AMR = items.findViewById(R.id.AMR);
        itemss(AMR);

        final ToggleButton Win94 = items.findViewById(R.id.Win94);
        itemss(Win94);

        final ToggleButton MK12 = items.findViewById(R.id.MK12);
        itemss(MK12);

        //Scopes

        final ToggleButton x2 = items.findViewById(R.id.x2);
        itemss(x2);

        final ToggleButton x3 = items.findViewById(R.id.x3);
        itemss(x3);

        final ToggleButton x4 = items.findViewById(R.id.x4);
        itemss(x4);

        final ToggleButton x6 = items.findViewById(R.id.x6);
        itemss(x6);

        final ToggleButton x8 = items.findViewById(R.id.x8);
        itemss(x8);

        final ToggleButton canted = items.findViewById(R.id.canted);
        itemss(canted);

        final ToggleButton hollow = items.findViewById(R.id.hollow);
        itemss(hollow);

        final ToggleButton reddot = items.findViewById(R.id.reddot);
        itemss(reddot);

        //Armor

        final ToggleButton bag1 = items.findViewById(R.id.bag1);
        itemss(bag1);

        final ToggleButton bag2 = items.findViewById(R.id.bag2);
        itemss(bag2);

        final ToggleButton bag3 = items.findViewById(R.id.bag3);
        itemss(bag3);

        final ToggleButton helmet1 = items.findViewById(R.id.helmet1);
        itemss(helmet1);

        final ToggleButton helmet2 = items.findViewById(R.id.helmet2);
        itemss(helmet2);

        final ToggleButton helmet3 = items.findViewById(R.id.helmet3);
        itemss(helmet3);

        final ToggleButton vest1 = items.findViewById(R.id.vest1);
        itemss(vest1);

        final ToggleButton vest2 = items.findViewById(R.id.vest2);
        itemss(vest2);

        final ToggleButton vest3 = items.findViewById(R.id.vest3);
        itemss(vest3);

        //Ammo
        final ToggleButton a9 = items.findViewById(R.id.a9);
        itemss(a9);

        final ToggleButton a7 = items.findViewById(R.id.a7);
        itemss(a7);

        final ToggleButton a5 = items.findViewById(R.id.a5);
        itemss(a5);

        final ToggleButton a300 = items.findViewById(R.id.a300);
        itemss(a300);

        final ToggleButton a45 = items.findViewById(R.id.a45);
        itemss(a45);

        final ToggleButton Arrow = items.findViewById(R.id.arrow);
        itemss(Arrow);

        final ToggleButton BMG50 = items.findViewById(R.id.BMG50);
        itemss(BMG50);

        final ToggleButton a12 = items.findViewById(R.id.a12);
        itemss(a12);

        //Shotgun
        final ToggleButton DBS = items.findViewById(R.id.DBS);
        itemss(DBS);

        final ToggleButton NS2000 = items.findViewById(R.id.NS2000);
        itemss(NS2000);

        final ToggleButton S686 = items.findViewById(R.id.S686);
        itemss(S686);

        final ToggleButton sawed = items.findViewById(R.id.sawed);
        itemss(sawed);

        final ToggleButton M1014 = items.findViewById(R.id.M1014);
        itemss(M1014);

        final ToggleButton S1897 = items.findViewById(R.id.S1897);
        itemss(S1897);

        final ToggleButton S12K = items.findViewById(R.id.S12K);
        itemss(S12K);

        //Throwables
        final ToggleButton grenade = items.findViewById(R.id.grenade);
        itemss(grenade);

        final ToggleButton molotov = items.findViewById(R.id.molotov);
        itemss(molotov);

        final ToggleButton stun = items.findViewById(R.id.stun);
        itemss(stun);

        final ToggleButton smoke = items.findViewById(R.id.smoke);
        itemss(smoke);

        //Medics

        final ToggleButton painkiller = items.findViewById(R.id.painkiller);
        itemss(painkiller);

        final ToggleButton medkit = items.findViewById(R.id.medkit);
        itemss(medkit);

        final ToggleButton firstaid = items.findViewById(R.id.firstaid);
        itemss(firstaid);

        final ToggleButton bandage = items.findViewById(R.id.bandage);
        itemss(bandage);

        final ToggleButton injection = items.findViewById(R.id.injection);
        itemss(injection);

        final ToggleButton energydrink = items.findViewById(R.id.energydrink);
        itemss(energydrink);

        //Handy
        final ToggleButton Pan = items.findViewById(R.id.Pan);
        itemss(Pan);

        final ToggleButton Crowbar = items.findViewById(R.id.Crowbar);
        itemss(Crowbar);

        final ToggleButton Sickle = items.findViewById(R.id.Sickle);
        itemss(Sickle);

        final ToggleButton Machete = items.findViewById(R.id.Machete);
        itemss(Machete);

        final ToggleButton Crossbow = items.findViewById(R.id.Crossbow);
        itemss(Crossbow);

        final ToggleButton Explosive = items.findViewById(R.id.Explosive);
        itemss(Explosive);

        //Pistols
        final ToggleButton P92 = items.findViewById(R.id.P92);
        itemss(P92);

        final ToggleButton R45 = items.findViewById(R.id.R45);
        itemss(R45);

        final ToggleButton P18C = items.findViewById(R.id.P18C);
        itemss(P18C);

        final ToggleButton P1911 = items.findViewById(R.id.P1911);
        itemss(P1911);

        final ToggleButton R1895 = items.findViewById(R.id.R1895);
        itemss(R1895);

        final ToggleButton Scorpion = items.findViewById(R.id.Scorpion);
        itemss(Scorpion);

        //Other
        final ToggleButton CheekPad = items.findViewById(R.id.CheekPad);
        itemss(CheekPad);

        final ToggleButton Choke = items.findViewById(R.id.Choke);
        itemss(Choke);

        final ToggleButton CompensatorSMG = items.findViewById(R.id.CompensatorSMG);
        itemss(CompensatorSMG);


        final ToggleButton FlashHiderSMG = items.findViewById(R.id.FlashHiderSMG);
        itemss(FlashHiderSMG);


        final ToggleButton FlashHiderAr = items.findViewById(R.id.FlashHiderAr);
        itemss(FlashHiderAr);

        final ToggleButton ArCompensator = items.findViewById(R.id.ArCompensator);
        itemss(ArCompensator);

        final ToggleButton TacticalStock = items.findViewById(R.id.TacticalStock);
        itemss(TacticalStock);

        final ToggleButton Duckbill = items.findViewById(R.id.Duckbill);
        itemss(Duckbill);

        final ToggleButton FlashHiderSniper = items.findViewById(R.id.FlashHiderSniper);
        itemss(FlashHiderSniper);

        final ToggleButton SuppressorSMG = items.findViewById(R.id.SuppressorSMG);
        itemss(SuppressorSMG);

        final ToggleButton HalfGrip = items.findViewById(R.id.HalfGrip);
        itemss(HalfGrip);

        final ToggleButton StockMicroUZI = items.findViewById(R.id.StockMicroUZI);
        itemss(StockMicroUZI);

        final ToggleButton SuppressorSniper = items.findViewById(R.id.SuppressorSniper);
        itemss(SuppressorSniper);

        final ToggleButton SuppressorAr = items.findViewById(R.id.SuppressorAr);
        itemss(SuppressorAr);

        final ToggleButton SniperCompensator = items.findViewById(R.id.SniperCompensator);
        itemss(SniperCompensator);

        final ToggleButton ExQdSniper = items.findViewById(R.id.ExQdSniper);
        itemss(ExQdSniper);

        final ToggleButton QdSMG = items.findViewById(R.id.QdSMG);
        itemss(QdSMG);

        final ToggleButton ExSMG = items.findViewById(R.id.ExSMG);
        itemss(ExSMG);

        final ToggleButton QdSniper = items.findViewById(R.id.QdSniper);
        itemss(QdSniper);

        final ToggleButton ExSniper = items.findViewById(R.id.ExSniper);
        itemss(ExSniper);

        final ToggleButton ExAr = items.findViewById(R.id.ExAr);
        itemss(ExAr);

        final ToggleButton ExQdAr = items.findViewById(R.id.ExQdAr);
        itemss(ExQdAr);

        final ToggleButton QdAr = items.findViewById(R.id.QdAr);
        itemss(QdAr);

        final ToggleButton ExQdSMG = items.findViewById(R.id.ExQdSMG);
        itemss(ExQdSMG);

        final ToggleButton QuiverCrossBow = items.findViewById(R.id.QuiverCrossBow);
        itemss(QuiverCrossBow);

        final ToggleButton BulletLoop = items.findViewById(R.id.BulletLoop);
        itemss(BulletLoop);

        final ToggleButton ThumbGrip = items.findViewById(R.id.ThumbGrip);
        itemss(ThumbGrip);

        final ToggleButton LaserSight = items.findViewById(R.id.LaserSight);
        itemss(LaserSight);

        final ToggleButton AngledGrip = items.findViewById(R.id.AngledGrip);
        itemss(AngledGrip);

        final ToggleButton LightGrip = items.findViewById(R.id.LightGrip);
        itemss(LightGrip);

        final ToggleButton VerticalGrip = items.findViewById(R.id.VerticalGrip);
        itemss(VerticalGrip);

        final ToggleButton GasCan = items.findViewById(R.id.GasCan);
        itemss(GasCan);

        //Vehicle
        final ToggleButton UTV = items.findViewById(R.id.UTV);
        vehicless(UTV);

        final ToggleButton Buggy = items.findViewById(R.id.Buggy);
        vehicless(Buggy);

        final ToggleButton UAZ = items.findViewById(R.id.UAZ);
        vehicless(UAZ);

        final ToggleButton Trike = items.findViewById(R.id.Trike);
        vehicless(Trike);

        final ToggleButton Bike = items.findViewById(R.id.Bike);
        vehicless(Bike);

        final ToggleButton Dacia = items.findViewById(R.id.Dacia);
        vehicless(Dacia);

        final ToggleButton Jet = items.findViewById(R.id.Jet);
        vehicless(Jet);

        final ToggleButton Boat = items.findViewById(R.id.Boat);
        vehicless(Boat);

        final ToggleButton Scooter = items.findViewById(R.id.Scooter);
        vehicless(Scooter);

        final ToggleButton Bus = items.findViewById(R.id.Bus);
        vehicless(Bus);

        final ToggleButton Mirado = items.findViewById(R.id.Mirado);
        vehicless(Mirado);

        final ToggleButton Rony = items.findViewById(R.id.Rony);
        vehicless(Rony);

        final ToggleButton Snowbike = items.findViewById(R.id.Snowbike);
        vehicless(Snowbike);

        final ToggleButton Snowmobile = items.findViewById(R.id.Snowmobile);
        vehicless(Snowmobile);

        final ToggleButton Tempo = items.findViewById(R.id.Tempo);
        vehicless(Tempo);

        final ToggleButton Truck = items.findViewById(R.id.Truck);
        vehicless(Truck);

        final ToggleButton MonsterTruck = items.findViewById(R.id.MonsterTruck);
        vehicless(MonsterTruck);

        final ToggleButton BRDM = items.findViewById(R.id.BRDM);
        vehicless(BRDM);

        final ToggleButton ATV = items.findViewById(R.id.ATV);
        vehicless(ATV);

        final ToggleButton LadaNiva = items.findViewById(R.id.LadaNiva);
        vehicless(LadaNiva);

        final ToggleButton Motorglider = items.findViewById(R.id.Motorglider);
        vehicless(Motorglider);

        final ToggleButton CoupeRB = items.findViewById(R.id.CoupeRB);
        vehicless(CoupeRB);

        //Special
        final ToggleButton Crate = items.findViewById(R.id.Crate);
        itemss(Crate);

        final ToggleButton Airdrop = items.findViewById(R.id.Airdrop);
        itemss(Airdrop);

        final ToggleButton DropPlane = items.findViewById(R.id.DropPlane);
        itemss(DropPlane);

        final ToggleButton FlareGun = items.findViewById(R.id.FlareGun);
        itemss(FlareGun);

        final LinearLayout checkall = mainView.findViewById(R.id.itemscheckall);
        final LinearLayout noneall = mainView.findViewById(R.id.itemsblockall);
        final LinearLayout checkallv = mainView.findViewById(R.id.mobilscheckall);
        final LinearLayout noneallv = mainView.findViewById(R.id.mobilsblockall);

        checkallv.setOnClickListener(v -> {
            Buggy.setChecked(true);
            UAZ.setChecked(true);
            Trike.setChecked(true);
            Bike.setChecked(true);
            Dacia.setChecked(true);
            Jet.setChecked(true);
            Boat.setChecked(true);
            Scooter.setChecked(true);
            Bus.setChecked(true);
            Mirado.setChecked(true);
            Rony.setChecked(true);
            Snowbike.setChecked(true);
            Snowmobile.setChecked(true);
            Tempo.setChecked(true);
            Truck.setChecked(true);
            MonsterTruck.setChecked(true);
            BRDM.setChecked(true);
            LadaNiva.setChecked(true);
            ATV.setChecked(true);
            UTV.setChecked(true);
            CoupeRB.setChecked(true);
            Motorglider.setChecked(true);
        });

        noneallv.setOnClickListener(v -> {
            Buggy.setChecked(false);
            UAZ.setChecked(false);
            Trike.setChecked(false);
            Bike.setChecked(false);
            Dacia.setChecked(false);
            Jet.setChecked(false);
            Boat.setChecked(false);
            Scooter.setChecked(false);
            Bus.setChecked(false);
            Mirado.setChecked(false);
            Rony.setChecked(false);
            Snowbike.setChecked(false);
            Snowmobile.setChecked(false);
            Tempo.setChecked(false);
            Truck.setChecked(false);
            MonsterTruck.setChecked(false);
            BRDM.setChecked(false);
            LadaNiva.setChecked(false);
            ATV.setChecked(false);
            UTV.setChecked(false);
            CoupeRB.setChecked(false);
            Motorglider.setChecked(false);
        });

        checkall.setOnClickListener(v -> {

            /* Other */
            Crate.setChecked(true);
            Airdrop.setChecked(true);
            DropPlane.setChecked(true);
            CheekPad.setChecked(true);
            Choke.setChecked(true);


            /* Scope */
            canted.setChecked(true);
            reddot.setChecked(true);
            hollow.setChecked(true);
            x2.setChecked(true);
            x3.setChecked(true);
            x4.setChecked(true);
            x6.setChecked(true);
            x8.setChecked(true);

            /* Weapon */
            AWM.setChecked(true);
            QBU.setChecked(true);
            SLR.setChecked(true);
            SKS.setChecked(true);
            Mini14.setChecked(true);
            M24.setChecked(true);
            Kar98k.setChecked(true);
            VSS.setChecked(true);
            Win94.setChecked(true);
            AUG.setChecked(true);
            M762.setChecked(true);
            SCARL.setChecked(true);
            M416.setChecked(true);
            M16A4.setChecked(true);
            MK47.setChecked(true);
            G36C.setChecked(true);
            QBZ.setChecked(true);
            AKM.setChecked(true);
            Groza.setChecked(true);
            S12K.setChecked(true);
            DBS.setChecked(true);
            S686.setChecked(true);
            S1897.setChecked(true);
            sawed.setChecked(true);
            TommyGun.setChecked(true);
            MP5K.setChecked(true);
            vector.setChecked(true);
            UZI.setChecked(true);
            R1895.setChecked(true);
            Explosive.setChecked(true);
            P92.setChecked(true);
            P18C.setChecked(true);
            R45.setChecked(true);
            P1911.setChecked(true);
            Desert.setChecked(true);
            Sickle.setChecked(true);
            Machete.setChecked(true);
            Pan.setChecked(true);
            MK14.setChecked(true);
            Scorpion.setChecked(true);

            Mosin.setChecked(true);
            MK12.setChecked(true);
            AMR.setChecked(true);

            M1014.setChecked(true);
            NS2000.setChecked(true);
            P90.setChecked(true);
            MG3.setChecked(true);
            AC32.setChecked(true);
            HoneyBadger.setChecked(true);
            FAMAS.setChecked(true);

            /* Ammo */
            a45.setChecked(true);
            a9.setChecked(true);
            a7.setChecked(true);
            a300.setChecked(true);
            a5.setChecked(true);
            BMG50.setChecked(true);
            a12.setChecked(true);

            SniperCompensator.setChecked(true);
            DP28.setChecked(true);
            M249.setChecked(true);
            grenade.setChecked(true);
            smoke.setChecked(true);
            molotov.setChecked(true);
            painkiller.setChecked(true);
            injection.setChecked(true);
            energydrink.setChecked(true);
            firstaid.setChecked(true);
            bandage.setChecked(true);
            medkit.setChecked(true);
            FlareGun.setChecked(true);
            UMP.setChecked(true);
            bizon.setChecked(true);
            CompensatorSMG.setChecked(true);
            FlashHiderSMG.setChecked(true);
            FlashHiderAr.setChecked(true);
            ArCompensator.setChecked(true);
            TacticalStock.setChecked(true);
            Duckbill.setChecked(true);
            FlashHiderSniper.setChecked(true);
            SuppressorSMG.setChecked(true);
            HalfGrip.setChecked(true);
            StockMicroUZI.setChecked(true);
            SuppressorSniper.setChecked(true);
            SuppressorAr.setChecked(true);
            ExQdSniper.setChecked(true);
            QdSMG.setChecked(true);
            ExSMG.setChecked(true);
            QdSniper.setChecked(true);
            ExSniper.setChecked(true);
            ExAr.setChecked(true);
            ExQdAr.setChecked(true);
            QdAr.setChecked(true);
            ExQdSMG.setChecked(true);
            QuiverCrossBow.setChecked(true);
            BulletLoop.setChecked(true);
            ThumbGrip.setChecked(true);
            LaserSight.setChecked(true);
            AngledGrip.setChecked(true);
            LightGrip.setChecked(true);
            VerticalGrip.setChecked(true);
            GasCan.setChecked(true);
            Arrow.setChecked(true);
            Crossbow.setChecked(true);
            bag1.setChecked(true);
            bag2.setChecked(true);
            bag3.setChecked(true);
            helmet1.setChecked(true);
            helmet2.setChecked(true);
            helmet3.setChecked(true);
            vest1.setChecked(true);
            vest2.setChecked(true);
            vest3.setChecked(true);
            stun.setChecked(true);
            Crowbar.setChecked(true);
        });

        noneall.setOnClickListener(v -> {
            /* Other */
            Crate.setChecked(false);
            Airdrop.setChecked(false);
            DropPlane.setChecked(false);
            CheekPad.setChecked(false);
            Choke.setChecked(false);


            /* Scope */
            canted.setChecked(false);
            reddot.setChecked(false);
            hollow.setChecked(false);
            x2.setChecked(false);
            x3.setChecked(false);
            x4.setChecked(false);
            x6.setChecked(false);
            x8.setChecked(false);

            /* Weapon */
            AWM.setChecked(false);
            QBU.setChecked(false);
            SLR.setChecked(false);
            SKS.setChecked(false);
            Mini14.setChecked(false);
            M24.setChecked(false);
            Kar98k.setChecked(false);
            VSS.setChecked(false);
            Win94.setChecked(false);
            AUG.setChecked(false);
            M762.setChecked(false);
            SCARL.setChecked(false);
            M416.setChecked(false);
            M16A4.setChecked(false);
            MK47.setChecked(false);
            G36C.setChecked(false);
            QBZ.setChecked(false);
            AKM.setChecked(false);
            Groza.setChecked(false);
            S12K.setChecked(false);
            DBS.setChecked(false);
            S686.setChecked(false);
            S1897.setChecked(false);
            sawed.setChecked(false);
            TommyGun.setChecked(false);
            MP5K.setChecked(false);
            vector.setChecked(false);
            UZI.setChecked(false);
            R1895.setChecked(false);
            Explosive.setChecked(false);
            P92.setChecked(false);
            P18C.setChecked(false);
            R45.setChecked(false);
            P1911.setChecked(false);
            Desert.setChecked(false);
            Sickle.setChecked(false);
            Machete.setChecked(false);
            Pan.setChecked(false);
            MK14.setChecked(false);
            Scorpion.setChecked(false);

            Mosin.setChecked(false);
            MK12.setChecked(false);
            AMR.setChecked(false);

            M1014.setChecked(false);
            NS2000.setChecked(false);
            P90.setChecked(false);
            MG3.setChecked(false);
            AC32.setChecked(false);
            HoneyBadger.setChecked(false);
            FAMAS.setChecked(false);

            /* Ammo */
            a45.setChecked(false);
            a9.setChecked(false);
            a7.setChecked(false);
            a300.setChecked(false);
            a5.setChecked(false);
            BMG50.setChecked(false);
            a12.setChecked(false);

            SniperCompensator.setChecked(false);
            DP28.setChecked(false);
            M249.setChecked(false);
            grenade.setChecked(false);
            smoke.setChecked(false);
            molotov.setChecked(false);
            painkiller.setChecked(false);
            injection.setChecked(false);
            energydrink.setChecked(false);
            firstaid.setChecked(false);
            bandage.setChecked(false);
            medkit.setChecked(false);
            FlareGun.setChecked(false);
            UMP.setChecked(false);
            bizon.setChecked(false);
            CompensatorSMG.setChecked(false);
            FlashHiderSMG.setChecked(false);
            FlashHiderAr.setChecked(false);
            ArCompensator.setChecked(false);
            TacticalStock.setChecked(false);
            Duckbill.setChecked(false);
            FlashHiderSniper.setChecked(false);
            SuppressorSMG.setChecked(false);
            HalfGrip.setChecked(false);
            StockMicroUZI.setChecked(false);
            SuppressorSniper.setChecked(false);
            SuppressorAr.setChecked(false);
            ExQdSniper.setChecked(false);
            QdSMG.setChecked(false);
            ExSMG.setChecked(false);
            QdSniper.setChecked(false);
            ExSniper.setChecked(false);
            ExAr.setChecked(false);
            ExQdAr.setChecked(false);
            QdAr.setChecked(false);
            ExQdSMG.setChecked(false);
            QuiverCrossBow.setChecked(false);
            BulletLoop.setChecked(false);
            ThumbGrip.setChecked(false);
            LaserSight.setChecked(false);
            AngledGrip.setChecked(false);
            LightGrip.setChecked(false);
            VerticalGrip.setChecked(false);
            GasCan.setChecked(false);
            Arrow.setChecked(false);
            Crossbow.setChecked(false);
            bag1.setChecked(false);
            bag2.setChecked(false);
            bag3.setChecked(false);
            helmet1.setChecked(false);
            helmet2.setChecked(false);
            helmet3.setChecked(false);
            vest1.setChecked(false);
            vest2.setChecked(false);
            vest3.setChecked(false);
            stun.setChecked(false);
            Crowbar.setChecked(false);
        });
    }

    private void aimbot(View aimbot) {
        TextView menutextaimtouch = aimbot.findViewById(R.id.texttouch);
        LinearLayout aimspeedmenu = aimbot.findViewById(R.id.aimspeedmenu);
        LinearLayout recoilmenu = aimbot.findViewById(R.id.recoilmenu);
        LinearLayout smoothnessmenu = aimbot.findViewById(R.id.smoothnessmenu);
        final LinearLayout touchLocationmenu = aimbot.findViewById(R.id.touchlocationmenu);
        final LinearLayout touchsizemenu = aimbot.findViewById(R.id.touchsizemenu);
        final LinearLayout posXmenu = aimbot.findViewById(R.id.posXmenu);
        final LinearLayout posYmenu = aimbot.findViewById(R.id.posYmenu);


        ToggleButton aimbottoggle = aimbot.findViewById(R.id.aimbot);
        ToggleButton touchttoggle = aimbot.findViewById(R.id.touchsimulation);
        ToggleButton bttoggle = aimbot.findViewById(R.id.bullettrack);

        if (kernel) {
            bttoggle.setVisibility(View.GONE);
            aimbottoggle.setVisibility(View.GONE);
        } else {
            bttoggle.setVisibility(View.VISIBLE);
            aimbottoggle.setVisibility(View.VISIBLE);
        }

        if (!Shell.rootAccess()) {
            touchttoggle.setVisibility(View.GONE);
        } else {
            touchttoggle.setVisibility(View.VISIBLE);
            touchttoggle.setVisibility(View.VISIBLE);
        }

        aimbottoggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                touchttoggle.setChecked(false);
                bttoggle.setChecked(false);
                StartAimFloat();
                StopAimBulletFloat();
                StopAimTouch();
                menutextaimtouch.setVisibility(View.GONE);
                aimspeedmenu.setVisibility(View.GONE);
                smoothnessmenu.setVisibility(View.GONE);
                touchLocationmenu.setVisibility(View.GONE);
                touchsizemenu.setVisibility(View.GONE);
                recoilmenu.setVisibility(View.VISIBLE);
                posXmenu.setVisibility(View.GONE);
                posYmenu.setVisibility(View.GONE);
            } else {
                StopAimBulletFloat();
                StopAimFloat();
                StopAimTouch();
            }
        });

        touchttoggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                aimbottoggle.setChecked(false);
                bttoggle.setChecked(false);
                StartAimTouch();
                StopAimBulletFloat();
                StopAimFloat();
                menutextaimtouch.setVisibility(View.VISIBLE);
                aimspeedmenu.setVisibility(View.VISIBLE);
                smoothnessmenu.setVisibility(View.VISIBLE);
                touchLocationmenu.setVisibility(View.VISIBLE);
                touchsizemenu.setVisibility(View.VISIBLE);
                recoilmenu.setVisibility(View.VISIBLE);
                posXmenu.setVisibility(View.VISIBLE);
                posYmenu.setVisibility(View.VISIBLE);
            } else {
                menutextaimtouch.setVisibility(View.GONE);
                aimspeedmenu.setVisibility(View.GONE);
                smoothnessmenu.setVisibility(View.GONE);
                touchLocationmenu.setVisibility(View.GONE);
                touchsizemenu.setVisibility(View.GONE);
                recoilmenu.setVisibility(View.GONE);
                posXmenu.setVisibility(View.GONE);
                posYmenu.setVisibility(View.GONE);
                StopAimBulletFloat();
                StopAimFloat();
                StopAimTouch();
            }
        });

        bttoggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                touchttoggle.setChecked(false);
                aimbottoggle.setChecked(false);
                StartAimBulletFloat();
                StopAimFloat();
                StopAimTouch();
                menutextaimtouch.setVisibility(View.GONE);
                aimspeedmenu.setVisibility(View.GONE);
                smoothnessmenu.setVisibility(View.GONE);
                touchLocationmenu.setVisibility(View.GONE);
                touchsizemenu.setVisibility(View.GONE);
                recoilmenu.setVisibility(View.GONE);
                posXmenu.setVisibility(View.GONE);
                posYmenu.setVisibility(View.GONE);
            } else {
                StopAimBulletFloat();
                StopAimFloat();
                StopAimTouch();
            }
        });


        final ToggleButton aimKnocked = aimbot.findViewById(R.id.aimknocked);
        setaim(aimKnocked, 3);

        final ToggleButton aimignore = aimbot.findViewById(R.id.aimignorebot);
        setaim(aimignore, 4);

        final ToggleButton changerotation = aimbot.findViewById(R.id.rotationscren);
        setaim(changerotation, 5);

        final ToggleButton touchlocation = aimbot.findViewById(R.id.touchlocation);
        setaim(touchlocation, 6);

        final SeekBar rangeSeekBar = aimbot.findViewById(R.id.range);
        final TextView rangeText = aimbot.findViewById(R.id.rangetext);
        setupSeekBar(rangeSeekBar, rangeText, getrangeAim(), () -> {
            Range(rangeSeekBar.getProgress());
            getrangeAim(rangeSeekBar.getProgress());
        });

        final SeekBar distancesSeekBar = aimbot.findViewById(R.id.distances);
        final TextView distancesText = aimbot.findViewById(R.id.distancetext);
        setupSeekBar(distancesSeekBar, distancesText, getDistances(), () -> {
            distances(distancesSeekBar.getProgress());
            setDistances(distancesSeekBar.getProgress());
        });


        final SeekBar recoilSeekBar2 = aimbot.findViewById(R.id.Recoil2);
        final TextView recoilText2 = aimbot.findViewById(R.id.recoiltext2);
        setupSeekBar(recoilSeekBar2, recoilText2, getrecoilAim(), () -> {
            recoil(recoilSeekBar2.getProgress());
            getrecoilAim(recoilSeekBar2.getProgress());
        });

        final SeekBar recoilSeekBar = aimbot.findViewById(R.id.Recoil);
        final TextView recoilText = aimbot.findViewById(R.id.recoiltext);
        setupSeekBar(recoilSeekBar, recoilText, getrecoilAim(), () -> {
            recoil2(recoilSeekBar.getProgress());
            getrecoilAim2(recoilSeekBar.getProgress());
        });

        final SeekBar recoilSeekBars2 = aimbot.findViewById(R.id.Recoils2);
        final TextView recoilTexts2 = aimbot.findViewById(R.id.recoiltexts2);
        setupSeekBar(recoilSeekBars2, recoilTexts2, getrecoilAim(), () -> {
            recoil3(recoilSeekBars2.getProgress());
            getrecoilAim3(recoilSeekBars2.getProgress());
        });

        final SeekBar bulletSpeedSeekBar = aimbot.findViewById(R.id.bulletspeed);
        final TextView bulletSpeedText = aimbot.findViewById(R.id.bulletspeedtext);
        setupSeekBar(bulletSpeedSeekBar, bulletSpeedText, getbulletspeedAim(), () -> {
            Bulletspeed(bulletSpeedSeekBar.getProgress());
            getbulletspeedAim(bulletSpeedSeekBar.getProgress());
        });

        final SeekBar AimSpeedSize = aimbot.findViewById(R.id.aimingspeed);
        final TextView AimSpeedText = aimbot.findViewById(R.id.aimingspeedtext);
        setupSeekBar(AimSpeedSize, AimSpeedText, getAimSpeed(), () -> {
            AimingSpeed(AimSpeedSize.getProgress());
            setAimSpeed(AimSpeedSize.getProgress());
        });

        final SeekBar SmoothSize = aimbot.findViewById(R.id.Smoothness);
        final TextView SmoothText = aimbot.findViewById(R.id.smoothtext);
        setupSeekBar(SmoothSize, SmoothText, getSmoothness(), () -> {
            Smoothness(SmoothSize.getProgress());
            setSmoothness(SmoothSize.getProgress());
        });

        final SeekBar touchsize = mainView.findViewById(R.id.touchsize);
        final TextView touchsizetext = mainView.findViewById(R.id.touchsizetext);
        setupSeekBar(touchsize, touchsizetext, getTouchSize(), () -> {
            TouchSize(touchsize.getProgress());
            setTouchSize(touchsize.getProgress());
        });

        final SeekBar touchPosX = mainView.findViewById(R.id.touchPosX);
        final TextView touchPosXtext = mainView.findViewById(R.id.touchPosXtext);
        setupSeekBar(touchPosX, touchPosXtext, getTouchPosX(), () -> {
            TouchPosX(touchPosX.getProgress());
            setTouchPosX(touchPosX.getProgress());
        });

        final SeekBar touchPosY = mainView.findViewById(R.id.touchPosY);
        final TextView touchPosYtext = mainView.findViewById(R.id.touchPosYtext);
        setupSeekBar(touchPosY, touchPosYtext, getTouchPosY(), () -> {
            TouchPosY(touchPosY.getProgress());
            setTouchPosY(touchPosY.getProgress());
        });


        final RadioGroup aimby = aimbot.findViewById(R.id.aimby);
        aimby.setOnCheckedChangeListener((radioGroup, i) -> {
            int chkdId = aimby.getCheckedRadioButtonId();
            RadioButton btn = aimbot.findViewById(chkdId);
            AimBy(Integer.parseInt(btn.getTag().toString()));
        });

        final RadioGroup aimwhen = aimbot.findViewById(R.id.aimwhen);
        aimwhen.setOnCheckedChangeListener((radioGroup, i) -> {
            int chkdId = aimwhen.getCheckedRadioButtonId();
            RadioButton btn = aimbot.findViewById(chkdId);
            AimWhen(Integer.parseInt(btn.getTag().toString()));
        });

        final RadioGroup aimbotmode = aimbot.findViewById(R.id.aimbotmode);
        aimbotmode.setOnCheckedChangeListener((radioGroup, i) -> {
            int chkdId = aimbotmode.getCheckedRadioButtonId();
            RadioButton btn = aimbot.findViewById(chkdId);
            Target(Integer.parseInt(btn.getTag().toString()));
        });
    }

    private void memory(View memory) {
        final ToggleButton less = memory.findViewById(R.id.isreducerecoil);
        memory(less, 1);
        final ToggleButton Cross = memory.findViewById(R.id.issmallcross);
        memory(Cross, 2);
        final ToggleButton amms = memory.findViewById(R.id.isaimlock);
        memory(amms, 3);
        final ToggleButton mf = memory.findViewById(R.id.isMobilFast);
        memory(mf, 5);

      /*  final Switch MBH = memory.findViewById(R.id.ismagichead);
        memory(MBH, 6);
        final Switch MBB = memory.findViewById(R.id.ismagicbody);
        memory(MBB, 7);
        final Switch isipadview = memory.findViewById(R.id.isipadview);
        memory(isipadview, 5);*/

        final ToggleButton MBH = memory.findViewById(R.id.ismagichead);
        MBH.setOnClickListener(v -> Exec("/VNG 500", "Wait 20Sec, Magic Bullet Head Success"));

        final ToggleButton MBB = memory.findViewById(R.id.ismagicbody);
        MBB.setOnClickListener(v -> Exec("/VNG 600", "Wait 20Sec, Magic Bullet Body Success"));

        final SeekBar wideviewSeekBar = memory.findViewById(R.id.rangewide);
        final TextView wideviewText = memory.findViewById(R.id.rangetextwide);
        setupSeekBar(wideviewSeekBar, wideviewText, getwideview(), () -> {
            WideView(wideviewSeekBar.getProgress());
            getwideview(wideviewSeekBar.getProgress());
        });
    }

  /*  public class Dapter extends PagerAdapter {
        LayoutInflater inflater;
        Context context;

        public Dapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

       *//* @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = null;
            view = inflater.inflate(position == 0 ? R.layout.esp_visual : position == 1 ? R.layout.esp_inventory : position == 2 ? R.layout.esp_aimbot : R.layout.esp_memory, null);
            ViewPager viewPager = (ViewPager) container;
            viewPager.addView(view);
            if (position == 0) {
                visual(view);
            } else if (position == 1) {
                items(view);
            } else if (position == 2) {
                aimbot(view);
            } else if (position == 3) {
                memory(view);
            }
            return view;
        }*//*
    }*/
}

