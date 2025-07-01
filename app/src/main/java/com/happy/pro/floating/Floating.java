package com.happy.pro.floating;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.happy.pro.R;
import com.happy.pro.utils.FLog;
import com.happy.pro.utils.FPrefs;

import java.util.Locale;

/**
 * 🐻 BEAR-LOADER iOS-Style Floating Overlay Service
 * 
 * Modern iOS-inspired floating overlay with:
 * - Vertical sidebar navigation (left side)
 * - Dynamic content area (right side)
 * - Material Design 3 animations
 * - Smooth transitions and ripple effects
 * - Dark mode and RTL support
 * - ViewPropertyAnimator for performance
 * 
 * @author BEAR-LOADER Team
 * @version 3.0.0
 */
public class Floating extends Service {
    
    private static final String TAG = "BearFloating";
    
    // UI Components
    private View mainView;
    private PowerManager.WakeLock mWakeLock;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    
    // Main containers
    private LinearLayout layoutIconControlView;
    private LinearLayout layoutMainView;
    private CardView sidebarCard;
    private CardView contentCard;
    
    // Sidebar tabs
    private LinearLayout sidebarTabVisual;
    private LinearLayout sidebarTabItems;
    private LinearLayout sidebarTabAimbot;
    private LinearLayout sidebarTabMemory;
    
    // Content areas (ScrollViews in new layout)
    private ScrollView contentVisual;
    private ScrollView contentItems;
    private ScrollView contentAimbot;
    private ScrollView contentMemory;
    
    // Content header
    private TextView contentTitle;
    private ImageView contentIcon;
    
    // Current selected tab
    private int currentSelectedTab = 0; // 0=Visual, 1=Items, 2=Aimbot, 3=Memory
    
    // Animation durations
    private static final int ANIMATION_DURATION_SHORT = 200;
    private static final int ANIMATION_DURATION_MEDIUM = 300;
    private static final int ANIMATION_DURATION_LONG = 400;
    
    // Tab data
    private final String[] tabTitles = {"ESP Visual", "Items & Loot", "Aimbot", "Memory Hacks"};
    private final int[] tabIcons = {R.drawable.visual, R.drawable.loot, R.drawable.ic_aim, R.drawable.baseline_warning_24};
    
    static {
        try {
            System.loadLibrary("happy");
            FLog.info("🚀 Floating overlay native library loaded");
        } catch (UnsatisfiedLinkError e) {
            FLog.error("❌ Failed to load floating overlay native library: " + e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FLog.info("🎨 Creating iOS-style floating overlay...");
        
        try {
            initializeFloatingOverlay();
            loadLanguageSettings();
            FLog.info("✅ iOS-style floating overlay created successfully");
        } catch (Exception e) {
            FLog.error("❌ Failed to create floating overlay: " + e.getMessage());
        }
    }

    /**
     * Initialize the floating overlay UI
     */
    @SuppressLint("InflateParams")
    private void initializeFloatingOverlay() {
        // Inflate the iOS-style layout
        mainView = LayoutInflater.from(this).inflate(R.layout.premium_floating_menu_split, null);
        
        // Setup window parameters
        layoutParams = createWindowLayoutParams();
        
        // Load saved position before adding to window manager
        loadFloatingPosition();
        
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // Add view to window manager
        windowManager.addView(mainView, layoutParams);
        
        // Initialize UI components
        initializeUIComponents();
        
        // Setup interactions
        setupTouchHandling();
        setupSidebarNavigation();
        
        // Set initial state
        setInitialState();
        
        FLog.info("🎯 Floating overlay UI initialized");
    }

    /**
     * Initialize all UI components
     */
    private void initializeUIComponents() {
        // Note: New layout doesn't use separate collapse/expand views - create stubs for compatibility
        layoutIconControlView = new LinearLayout(this);
        layoutMainView = new LinearLayout(this);
        layoutIconControlView.setVisibility(View.GONE); // Always hidden in new layout
        
        // Note: New layout doesn't use separate cards - integrated design
        sidebarCard = null;
        contentCard = null;
        
        // Sidebar tabs
        sidebarTabVisual = mainView.findViewById(R.id.sidebar_tab_visual);
        sidebarTabItems = mainView.findViewById(R.id.sidebar_tab_items);
        sidebarTabAimbot = mainView.findViewById(R.id.sidebar_tab_aimbot);
        sidebarTabMemory = mainView.findViewById(R.id.sidebar_tab_memory);
        
        // Content areas (these are ScrollViews in new layout)
        contentVisual = mainView.findViewById(R.id.content_visual);
        contentItems = mainView.findViewById(R.id.content_items);  
        contentAimbot = mainView.findViewById(R.id.content_aimbot);
        contentMemory = mainView.findViewById(R.id.content_memory);
        
        // Content header
        contentTitle = mainView.findViewById(R.id.content_title);
        contentIcon = mainView.findViewById(R.id.content_icon);
        
        // Close button (updated ID for new layout)
        View closeButton = mainView.findViewById(R.id.close_menu);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> closeFloatingMenu());
        }
        
        FLog.info("📱 UI components initialized");
    }

    /**
     * Setup touch handling for drag and tap
     */
    private void setupTouchHandling() {
        // Apply touch listener to main view for new layout
        mainView.setOnTouchListener(new FloatingTouchListener());
    }

    /**
     * Setup sidebar navigation with smooth animations
     */
    private void setupSidebarNavigation() {
        // Visual tab
        sidebarTabVisual.setOnClickListener(v -> switchToTab(0, v));
        
        // Items tab
        sidebarTabItems.setOnClickListener(v -> switchToTab(1, v));
        
        // Aimbot tab  
        sidebarTabAimbot.setOnClickListener(v -> switchToTab(2, v));
        
        // Memory tab
        sidebarTabMemory.setOnClickListener(v -> switchToTab(3, v));
        
        FLog.info("🎛️ Sidebar navigation setup complete");
    }

    /**
     * Switch to a specific tab with smooth animations
     */
    private void switchToTab(int tabIndex, View clickedView) {
        if (currentSelectedTab == tabIndex) {
            return; // Already selected
        }
        
        FLog.info("🔄 Switching to tab: " + tabIndex + " (" + tabTitles[tabIndex] + ")");
        
        // Animate tab selection
        animateTabSelection(clickedView);
        
        // Update content area
        updateContentArea(tabIndex);
        
        // Update current selection
        currentSelectedTab = tabIndex;
        
        // Update sidebar visual states
        updateSidebarStates();
    }

    /**
     * Animate tab selection with ripple and scale effects
     */
    private void animateTabSelection(View view) {
        // Scale animation
        ViewPropertyAnimator animator = view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(ANIMATION_DURATION_SHORT / 2)
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(ANIMATION_DURATION_SHORT / 2)
                    .start();
            });
        
        animator.start();
    }

    /**
     * Update content area with smooth transition
     */
    private void updateContentArea(int tabIndex) {
        // Hide all content areas
        hideAllContentAreas();
        
        // Update header
        contentTitle.setText(tabTitles[tabIndex]);
        contentIcon.setImageResource(tabIcons[tabIndex]);
        
        // Show selected content area with animation
        ScrollView targetContent = getContentAreaByIndex(tabIndex);
        if (targetContent != null) {
            showContentAreaWithAnimation(targetContent);
        }
        
        FLog.info("📄 Content area updated to: " + tabTitles[tabIndex]);
    }

    /**
     * Hide all content areas
     */
    private void hideAllContentAreas() {
        contentVisual.setVisibility(View.GONE);
        contentItems.setVisibility(View.GONE);
        contentAimbot.setVisibility(View.GONE);
        contentMemory.setVisibility(View.GONE);
    }

    /**
     * Get content area by tab index
     */
    private ScrollView getContentAreaByIndex(int index) {
        switch (index) {
            case 0: return contentVisual;
            case 1: return contentItems;
            case 2: return contentAimbot;
            case 3: return contentMemory;
            default: return contentVisual;
        }
    }

    /**
     * Show content area with fade-in animation
     */
    private void showContentAreaWithAnimation(ScrollView content) {
        content.setAlpha(0f);
        content.setVisibility(View.VISIBLE);
        content.animate()
            .alpha(1f)
            .setDuration(ANIMATION_DURATION_MEDIUM)
            .start();
    }

    /**
     * Update sidebar visual states
     */
    private void updateSidebarStates() {
        // Reset all tabs
        resetTabState(sidebarTabVisual);
        resetTabState(sidebarTabItems);
        resetTabState(sidebarTabAimbot);
        resetTabState(sidebarTabMemory);
        
        // Set selected tab
        LinearLayout selectedTab = getSidebarTabByIndex(currentSelectedTab);
        if (selectedTab != null) {
            selectedTab.setSelected(true);
        }
    }

    /**
     * Reset tab visual state
     */
    private void resetTabState(LinearLayout tab) {
        tab.setSelected(false);
    }

    /**
     * Get sidebar tab by index
     */
    private LinearLayout getSidebarTabByIndex(int index) {
        switch (index) {
            case 0: return sidebarTabVisual;
            case 1: return sidebarTabItems;
            case 2: return sidebarTabAimbot;
            case 3: return sidebarTabMemory;
            default: return sidebarTabVisual;
        }
    }

    /**
     * Set initial state for the floating overlay
     */
    private void setInitialState() {
        // Start with Visual tab selected
        switchToTab(0, sidebarTabVisual);
        
        // New layout is always visible - no collapse/expand
        FLog.info("🎯 Initial state set - Visual tab active");
    }

    /**
     * Close floating menu with animation
     */
    private void closeFloatingMenu() {
        // For new layout, hide the entire overlay
        animateViewOut(mainView, () -> {
            mainView.setVisibility(View.GONE);
        });
        
        FLog.info("📱 Floating menu closed");
    }

    /**
     * Animate view out with scale and alpha
     */
    private void animateViewOut(View view, Runnable onComplete) {
        view.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .alpha(0f)
            .setDuration(ANIMATION_DURATION_MEDIUM)
            .withEndAction(() -> {
                view.setScaleX(1f);
                view.setScaleY(1f);
                view.setAlpha(1f);
                if (onComplete != null) {
                    onComplete.run();
                }
            })
            .start();
    }

    /**
     * Animate view in with scale and alpha
     */
    private void animateViewIn(View view, Runnable onComplete) {
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(ANIMATION_DURATION_MEDIUM)
            .withEndAction(onComplete)
            .start();
    }

    /**
     * Create window layout parameters
     */
    @SuppressLint("RtlHardcoded")
    private WindowManager.LayoutParams createWindowLayoutParams() {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getLayoutType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
                
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        return params;
    }

    /**
     * Get appropriate layout type for different Android versions
     */
    private static int getLayoutType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            return WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }
    }

    /**
     * Load language settings
     */
    private void loadLanguageSettings() {
        try {
            FPrefs prefs = FPrefs.with(this);
            String language = prefs.read("language", "en");
            setLocale(language);
            FLog.info("🌐 Language settings loaded: " + language);
        } catch (Exception e) {
            FLog.error("❌ Failed to load language settings: " + e.getMessage());
        }
    }

    /**
     * Set locale for internationalization
     */
    private void setLocale(String lang) {
        try {
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, 
                getBaseContext().getResources().getDisplayMetrics());
                
            // Save language preference
            SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("language", lang);
            editor.apply();
            
        } catch (Exception e) {
            FLog.error("❌ Failed to set locale: " + e.getMessage());
        }
    }

    /**
     * Check if view is collapsed
     */
    private boolean isViewCollapsed() {
        // New layout is always expanded - never collapsed
        return false;  
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FLog.info("🔄 Destroying floating overlay service...");
        
        try {
            if (mWakeLock != null && mWakeLock.isHeld()) {
                mWakeLock.release();
                mWakeLock = null;
            }

            if (mainView != null && windowManager != null) {
                windowManager.removeView(mainView);
                mainView = null;
            }
            
            FLog.info("✅ Floating overlay service destroyed");
        } catch (Exception e) {
            FLog.error("❌ Error destroying floating overlay: " + e.getMessage());
        }
    }

    /**
     * Get screen size for boundary detection
     */
    private android.graphics.Point getScreenSize() {
        android.graphics.Point size = new android.graphics.Point();
        windowManager.getDefaultDisplay().getSize(size);
        return size;
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
            int currentX = layoutParams.x;
            int currentY = layoutParams.y;
            
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
            saveFloatingPosition(layoutParams.x, layoutParams.y);
            
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
            ObjectAnimator animatorX = ObjectAnimator.ofInt(layoutParams, "x", layoutParams.x, targetX);
            ObjectAnimator animatorY = ObjectAnimator.ofInt(layoutParams, "y", layoutParams.y, targetY);
            
            animatorX.setDuration(200);
            animatorY.setDuration(200);
            
            animatorX.addUpdateListener(animation -> {
                layoutParams.x = (int) animation.getAnimatedValue();
                windowManager.updateViewLayout(mainView, layoutParams);
            });
            
            animatorY.addUpdateListener(animation -> {
                layoutParams.y = (int) animation.getAnimatedValue();
                windowManager.updateViewLayout(mainView, layoutParams);
            });
            
            animatorX.start();
            animatorY.start();
            
        } catch (Exception e) {
            // Fallback to immediate positioning
            layoutParams.x = targetX;
            layoutParams.y = targetY;
            windowManager.updateViewLayout(mainView, layoutParams);
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
        SharedPreferences sp = getSharedPreferences("floatingPosition", Context.MODE_PRIVATE);
        int savedX = sp.getInt("floating_x", 0);
        int savedY = sp.getInt("floating_y", 100);
        
        layoutParams.x = savedX;
        layoutParams.y = savedY;
    }

    /**
     * Touch listener for floating overlay drag and tap handling
     */
    private class FloatingTouchListener implements View.OnTouchListener {
        private int initialX;
        private int initialY;
        private float initialTouchX;
        private float initialTouchY;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = layoutParams.x;
                    initialY = layoutParams.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    return true;

                case MotionEvent.ACTION_UP:
                    int xDiff = (int) (event.getRawX() - initialTouchX);
                    int yDiff = (int) (event.getRawY() - initialTouchY);
                    
                    // If minimal movement, treat as tap
                    if (Math.abs(xDiff) < 10 && Math.abs(yDiff) < 10) {
                        // For new layout, always show the overlay
                        mainView.setVisibility(View.VISIBLE);
                        animateViewIn(mainView, null);
                        FLog.info("📱 Floating menu opened");
                        
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
                    
                    layoutParams.x = newX;
                    layoutParams.y = newY;
                    windowManager.updateViewLayout(mainView, layoutParams);
                    return true;
            }
            return false;
        }
    }
} 
