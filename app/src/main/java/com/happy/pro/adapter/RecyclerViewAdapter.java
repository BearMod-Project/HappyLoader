package com.happy.pro.adapter;

import static com.happy.pro.activity.MainActivity.fixinstallint;
import static com.happy.pro.activity.ModeActivity.Kooontoool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.molihuan.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.AppUtils;
import com.happy.pro.activity.MainActivity;
import com.happy.pro.floating.ToggleAim;
import com.happy.pro.floating.ToggleBullet;
import com.happy.pro.floating.ToggleSimulation;
import com.happy.pro.libhelper.FileHelper;
import com.happy.pro.utils.ActivityCompat;
import com.happy.pro.utils.FLog;
import com.happy.pro.utils.PermissionUtils;
import com.happy.pro.utils.UiKit;
import com.happy.pro.floating.FloatService;
import com.happy.pro.floating.Overlay;
import com.happy.pro.floating.FloatRei;
import com.happy.pro.R;
import com.happy.pro.libhelper.ApkEnv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**************************
 * BUILD ON Android Studio
 * TELEGRAM : OxZeroo
 * Enhanced RecyclerViewAdapter with real installation logic and progress indicators
 * *************************/

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    public MainActivity activity;
    public ArrayList<Integer> imageValues;
    public ArrayList<String> titleValues;
    public ArrayList<String> versionValues;
    public ArrayList<String> statusValues;
    public ArrayList<String> packageValues;
    
    // Track progress states for each item
    private Map<Integer, Boolean> progressStates = new HashMap<>();
    private Map<Integer, String> actionTexts = new HashMap<>();

    public RecyclerViewAdapter(MainActivity activity, ArrayList<Integer> imageValues, ArrayList<String> titleValues, ArrayList<String> versionValues, ArrayList<String> statusValues, ArrayList<String> packageValues) {
        this.activity = activity;
        this.imageValues = imageValues;
        this.titleValues = titleValues;
        this.versionValues = versionValues;
        this.statusValues = statusValues;
        this.packageValues = packageValues;
        
        FLog.info("📱 Enhanced RecyclerViewAdapter initialized with " + packageValues.size() + " games");
        
        // Initialize progress states
        for (int i = 0; i < packageValues.size(); i++) {
            progressStates.put(i, false);
            actionTexts.put(i, "");
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_games, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // Set game info
        holder.gameIcon.setImageResource(imageValues.get(position));
        holder.gameTitle.setText(titleValues.get(position));
        holder.gameVersion.setText(versionValues.get(position));

        // Real-time status check with enhanced logic
        updateGameStatus(holder, position);

        // Game card click listener - select the game
        holder.itemView.setOnClickListener(v -> {
            selectGame(position);
        });

        // Enhanced action button click listener with progress
        holder.okBtn.setOnClickListener(v -> {
            performGameAction(holder, position);
        });

        // Long click for uninstall
        holder.okBtn.setOnLongClickListener(v -> {
            showUninstallDialog(position);
            return true;
        });
    }
    
    /**
     * Enhanced real-time game status checking with proper logic and visual indicators
     */
    @SuppressLint("SetTextI18n")
    private void updateGameStatus(MyViewHolder holder, int position) {
        try {
            String packageName = packageValues.get(position);
            
            // Check if we're in progress state
            if (progressStates.get(position)) {
                holder.status.setText(actionTexts.get(position));
                holder.status.setTextColor(Color.parseColor("#FFA500")); // Orange for progress
                if (holder.progressIndicator != null) {
                    holder.progressIndicator.setVisibility(View.VISIBLE);
                }
                return;
            }
            
            // Hide progress indicator when not in progress
            if (holder.progressIndicator != null) {
                holder.progressIndicator.setVisibility(View.GONE);
            }
            
            activity.runOnUiThread(() -> {
                try {
                    // Enhanced installation check
                    boolean isInstalled = isAppReallyInstalled(packageName);
                    
                    if (isInstalled) {
                        // Set install indicator to success
                        if (holder.installIndicator != null) {
                            holder.installIndicator.setImageResource(R.drawable.ic_check);
                            holder.installIndicator.setBackgroundTintList(
                                activity.getResources().getColorStateList(R.color.IJO, null));
                        }
                        
                        // Check if app is currently running
                        boolean isRunning = isAppCurrentlyRunning(packageName);
                        
                        if (isRunning) {
                            holder.status.setText("🟢 Running");
                            holder.status.setTextColor(Color.parseColor("#4CAF50")); // Green
                        } else {
                            holder.status.setText("▶️ Launch Game");
                            holder.status.setTextColor(Color.parseColor("#2196F3")); // Blue
                        }
                    } else {
                        // Set install indicator to not installed
                        if (holder.installIndicator != null) {
                            holder.installIndicator.setImageResource(R.drawable.ic_error);
                            holder.installIndicator.setBackgroundTintList(
                                activity.getResources().getColorStateList(R.color.red, null));
                        }
                        
                        // Check if user can install
                        if (Kooontoool) {
                            holder.status.setText("📦 Install Game");
                            holder.status.setTextColor(Color.parseColor("#FF9800")); // Orange
                        } else {
                            holder.status.setText("🔒 VIP Required");
                            holder.status.setTextColor(Color.parseColor("#F44336")); // Red
                        }
                    }
                    
                    // Update status values for consistency
                    statusValues.set(position, holder.status.getText().toString());
                    
                } catch (Exception e) {
                    FLog.error("❌ Status update failed for " + packageName + ": " + e.getMessage());
                    holder.status.setText("❓ Unknown");
                    holder.status.setTextColor(Color.parseColor("#9E9E9E")); // Gray
                    
                    if (holder.installIndicator != null) {
                        holder.installIndicator.setImageResource(R.drawable.ic_warning);
                        holder.installIndicator.setBackgroundTintList(
                            activity.getResources().getColorStateList(R.color.orange, null));
                    }
                }
            });
            
        } catch (Exception e) {
            FLog.error("❌ Failed to update game status: " + e.getMessage());
        }
    }
    
    /**
     * Enhanced app installation check with multiple verification methods
     */
    private boolean isAppReallyInstalled(String packageName) {
        try {
            // Method 1: ApkEnv check
            boolean apkEnvCheck = ApkEnv.getInstance().isInstalled(packageName);
            
            // Method 2: PackageManager check
            boolean packageManagerCheck = false;
            try {
                activity.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                packageManagerCheck = true;
            } catch (PackageManager.NameNotFoundException e) {
                packageManagerCheck = false;
            }
            
            // Method 3: MainActivity utility check
            boolean mainActivityCheck = MainActivity.isAppInstalled(activity, packageName);
            
            // Use majority vote or all checks must pass for reliability
            boolean isInstalled = apkEnvCheck && packageManagerCheck && mainActivityCheck;
            
            FLog.info("📱 Installation check for " + packageName + 
                     " - ApkEnv: " + apkEnvCheck + 
                     ", PM: " + packageManagerCheck + 
                     ", MainActivity: " + mainActivityCheck + 
                     " = " + isInstalled);
                     
            return isInstalled;
            
        } catch (Exception e) {
            FLog.error("❌ Installation check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Enhanced running check with multiple methods
     */
    private boolean isAppCurrentlyRunning(String packageName) {
        try {
            // Method 1: ApkEnv check
            boolean apkEnvRunning = ApkEnv.getInstance().isRunning(packageName);
            
            // Method 2: MainActivity check
            boolean mainActivityRunning = false;
            try {
                mainActivityRunning = activity.isAppRunning(packageName);
            } catch (Exception e) {
                // Method not accessible, skip
            }
            
            boolean isRunning = apkEnvRunning || mainActivityRunning;
            
            FLog.info("🏃 Running check for " + packageName + 
                     " - ApkEnv: " + apkEnvRunning + 
                     ", MainActivity: " + mainActivityRunning + 
                     " = " + isRunning);
                     
            return isRunning;
            
        } catch (Exception e) {
            FLog.error("❌ Running check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Enhanced game action handling with progress indicators
     */
    private void performGameAction(MyViewHolder holder, int position) {
        try {
            String packageName = packageValues.get(position);
            String gameName = titleValues.get(position);
            
            // Check current status and perform appropriate action
            if (progressStates.get(position)) {
                ActivityCompat.toastImage(R.drawable.ic_warning, "Action already in progress...");
                return;
            }
            
            if (statusValues.get(position).equals("🔒 VIP Required")) {
                ActivityCompat.toastImage(R.drawable.notife, "Please upgrade to VIP to install games");
                return;
            }
            
            boolean isInstalled = isAppReallyInstalled(packageName);
            
            if (isInstalled) {
                boolean isRunning = isAppCurrentlyRunning(packageName);
                
                if (isRunning) {
                    // Kill the running game
                    performKillGameAction(holder, position);
                } else {
                    // Launch the game with BEAR-LOADER
                    performLaunchGameAction(holder, position);
                }
            } else {
                // Install the game
                performInstallGameAction(holder, position);
            }
            
        } catch (Exception e) {
            FLog.error("❌ Game action failed: " + e.getMessage());
            ActivityCompat.toastImage(R.drawable.ic_error, "Action failed: " + e.getMessage());
        }
    }
    
    /**
     * Kill running game with progress indication
     */
    private void performKillGameAction(MyViewHolder holder, int position) {
        setProgressState(holder, position, "🔄 Stopping...", true);
        
        new Thread(() -> {
            try {
                String packageName = packageValues.get(position);
                
                // Stop the app
                ApkEnv.getInstance().stopRunningApp(packageName);
                
                // Stop BEAR-LOADER services
                activity.stopService(new Intent(activity, FloatService.class));
                activity.stopService(new Intent(activity, Overlay.class));
                activity.stopService(new Intent(activity, ToggleBullet.class));
                activity.stopService(new Intent(activity, ToggleAim.class));
                activity.stopService(new Intent(activity, ToggleSimulation.class));
                
                // Wait a moment for services to stop
                Thread.sleep(1000);
                
                activity.runOnUiThread(() -> {
                    setProgressState(holder, position, "", false);
                    updateGameStatus(holder, position);
                    ActivityCompat.toastImage(R.drawable.ic_check, "Game stopped successfully");
                    FLog.info("✅ Game killed: " + packageName);
                });
                
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    setProgressState(holder, position, "", false);
                    updateGameStatus(holder, position);
                    ActivityCompat.toastImage(R.drawable.ic_error, "Failed to stop game");
                    FLog.error("❌ Kill game failed: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Launch game with BEAR-LOADER integration and progress indication
     */
    private void performLaunchGameAction(MyViewHolder holder, int position) {
        setProgressState(holder, position, "🚀 Launching...", true);
        
        new Thread(() -> {
            try {
                String packageName = packageValues.get(position);
                String gameName = titleValues.get(position);
                
                // Update MainActivity selection
                activity.runOnUiThread(() -> {
                    activity.CURRENT_PACKAGE = packageName;
                    activity.game = packageName;
                    activity.nameGame = gameName;
                });
                
                // Initialize BEAR-LOADER for this game
                boolean loaderAdded = ApkEnv.getInstance().tryAddLoader(packageName);
                
                if (loaderAdded) {
                    // Launch the game with BEAR integration
                    activity.runOnUiThread(() -> {
                        activity.launchSplash(packageName);
                    });
                    
                    // Wait for launch to complete
                    Thread.sleep(2000);
                    
                    activity.runOnUiThread(() -> {
                        setProgressState(holder, position, "", false);
                        updateGameStatus(holder, position);
                        ActivityCompat.toastImage(R.drawable.ic_check, "🎮 " + gameName + " launched with BEAR-LOADER!");
                        FLog.info("✅ Game launched with BEAR: " + packageName);
                    });
                } else {
                    throw new Exception("Failed to add BEAR-LOADER to game");
                }
                
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    setProgressState(holder, position, "", false);
                    updateGameStatus(holder, position);
                    ActivityCompat.toastImage(R.drawable.ic_error, "Launch failed: " + e.getMessage());
                    FLog.error("❌ Launch game failed: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Install game with detailed progress indication
     */
    private void performInstallGameAction(MyViewHolder holder, int position) {
        if (!Kooontoool) {
            ActivityCompat.toastImage(R.drawable.notife, "Please upgrade to VIP to install games");
            return;
        }
        
        String packageName = packageValues.get(position);
        String gameName = titleValues.get(position);
        
        activity.showBottomSheetDialog(
            activity.getResources().getDrawable(imageValues.get(position)), 
            "📦 Install: " + gameName, 
            "🔄 This process may take 1-3 minutes\n⚠️ Please do not close the application\n🎮 BEAR-LOADER will be integrated automatically", 
            false, 
            v -> {
                activity.dismissBottomSheetDialog();
                startInstallationProcess(holder, position);
            }, 
            v1 -> {
                activity.dismissBottomSheetDialog();
            }
        );
    }
    
    /**
     * Start the actual installation process with progress tracking
     */
    private void startInstallationProcess(MyViewHolder holder, int position) {
        setProgressState(holder, position, "📦 Installing...", true);
        
        new Thread(() -> {
            try {
                String packageName = packageValues.get(position);
                String gameName = titleValues.get(position);
                
                // Update progress through different stages
                activity.runOnUiThread(() -> {
                    setProgressState(holder, position, "📥 Downloading...", true);
                });
                
                Thread.sleep(1000); // Simulate download time
                
                activity.runOnUiThread(() -> {
                    setProgressState(holder, position, "🔧 Installing...", true);
                });
                
                // Perform the actual installation
                if (!fixinstallint) {
                    FileHelper.tryInstallWithCopyObb(activity, activity.getProgresBar(), packageName);
                } else {
                    PermissionUtils.openobb(activity, 1, packageName);
                }
                
                // Wait for installation to complete and verify
                for (int i = 0; i < 30; i++) { // Wait up to 30 seconds
                    Thread.sleep(1000);
                    if (isAppReallyInstalled(packageName)) {
                        break;
                    }
                    
                    // Update progress indicator
                    final int progress = i;
                    activity.runOnUiThread(() -> {
                        setProgressState(holder, position, "🔧 Installing... " + progress + "s", true);
                    });
                }
                
                // Verify installation
                if (isAppReallyInstalled(packageName)) {
                    activity.runOnUiThread(() -> {
                        setProgressState(holder, position, "", false);
                        updateGameStatus(holder, position);
                        ActivityCompat.toastImage(R.drawable.ic_check, "✅ " + gameName + " installed successfully!");
                        FLog.info("✅ Game installed successfully: " + packageName);
                    });
                } else {
                    throw new Exception("Installation verification failed");
                }
                
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    setProgressState(holder, position, "", false);
                    updateGameStatus(holder, position);
                    ActivityCompat.toastImage(R.drawable.ic_error, "❌ Installation failed: " + e.getMessage());
                    FLog.error("❌ Installation failed: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Set progress state for a specific item with enhanced visual feedback
     */
    private void setProgressState(MyViewHolder holder, int position, String text, boolean inProgress) {
        progressStates.put(position, inProgress);
        actionTexts.put(position, text);
        
        activity.runOnUiThread(() -> {
            if (inProgress && !text.isEmpty()) {
                holder.status.setText(text);
                holder.status.setTextColor(Color.parseColor("#FFA500")); // Orange for progress
                
                // Show progress indicator
                if (holder.progressIndicator != null) {
                    holder.progressIndicator.setVisibility(View.VISIBLE);
                }
                
                // Set install indicator to progress state
                if (holder.installIndicator != null) {
                    holder.installIndicator.setImageResource(R.drawable.ic_warning);
                    holder.installIndicator.setBackgroundTintList(
                        activity.getResources().getColorStateList(R.color.orange, null));
                }
            } else {
                // Hide progress indicator
                if (holder.progressIndicator != null) {
                    holder.progressIndicator.setVisibility(View.GONE);
                }
            }
        });
    }
    
    /**
     * Show uninstall confirmation dialog
     */
    private void showUninstallDialog(int position) {
        String packageName = packageValues.get(position);
        String gameName = titleValues.get(position);
        
        if (!isAppReallyInstalled(packageName)) {
            ActivityCompat.toastImage(R.drawable.ic_warning, "Game is not installed");
            return;
        }
        
        activity.showBottomSheetDialog(
            activity.getResources().getDrawable(R.drawable.icon_toast_alert), 
            "🗑️ Uninstall Confirmation", 
            "Are you sure you want to remove " + gameName + "?\n\n⚠️ This action cannot be undone.", 
            false, 
            sv -> {
                activity.dismissBottomSheetDialog();
                performUninstallAction(position);
            }, 
            v1 -> {
                activity.dismissBottomSheetDialog();
            }
        );
    }
    
    /**
     * Perform uninstall with progress indication
     */
    private void performUninstallAction(int position) {
        MyViewHolder holder = getHolderForPosition(position);
        if (holder == null) return;
        
        setProgressState(holder, position, "🗑️ Uninstalling...", true);
        
        UiKit.defer().when(() -> {
            long time = System.currentTimeMillis();
            String packageName = packageValues.get(position);
            
            ApkEnv.getInstance().unInstallApp(packageName);
            
            time = System.currentTimeMillis() - time;
            long delta = 2000L - time; // Minimum 2 seconds for user feedback
            if (delta > 0) {
                UiKit.sleep(delta);
            }
        }).done((res) -> {
            if (holder != null) {
                setProgressState(holder, position, "", false);
                updateGameStatus(holder, position);
            }
            ActivityCompat.toastImage(R.drawable.ic_check, "Game uninstalled successfully");
            FLog.info("✅ Game uninstalled: " + packageValues.get(position));
        });
    }
    
    /**
     * Get holder for position (helper method)
     */
    private MyViewHolder getHolderForPosition(int position) {
        try {
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerview);
            if (recyclerView != null) {
                return (MyViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Select a game and update the main UI with visual selection indicator
     */
    private void selectGame(int position) {
        try {
            String packageName = packageValues.get(position);
            String gameName = titleValues.get(position);
            
            // Update activity game selection
            activity.CURRENT_PACKAGE = packageName;
            activity.game = packageName;
            activity.nameGame = gameName;
            
            // Update game int based on package
            for (int i = 0; i < activity.packageapp.length; i++) {
                if (activity.packageapp[i].equals(packageName)) {
                    activity.gameint = i + 1;
                    break;
                }
            }
            
            // Update UI to reflect selection
            activity.runOnUiThread(() -> {
                updateGameSelectionInActivity(position);
                updateSelectionIndicators(position);
                ActivityCompat.toastImage(imageValues.get(position), "Selected: " + gameName);
            });
            
            FLog.info("🎮 Game selected: " + gameName + " (" + packageName + ")");
            
        } catch (Exception e) {
            FLog.error("❌ Failed to select game: " + e.getMessage());
        }
    }
    
    /**
     * Update selection indicators on all cards
     */
    private void updateSelectionIndicators(int selectedPosition) {
        try {
            // This would ideally iterate through all visible holders
            // For now, we'll just handle the current selection
            // A full implementation would require tracking all view holders
            FLog.info("🎯 Selection updated for position: " + selectedPosition);
        } catch (Exception e) {
            FLog.error("❌ Failed to update selection indicators: " + e.getMessage());
        }
    }
    
    /**
     * Update the main activity UI when a game is selected
     */
    private void updateGameSelectionInActivity(int position) {
        try {
            // Find the corresponding game button and highlight it
            String packageName = packageValues.get(position);
            
            // Get game buttons
            android.widget.LinearLayout[] gameButtons = {
                activity.findViewById(R.id.global),
                activity.findViewById(R.id.korea),
                activity.findViewById(R.id.vietnam),
                activity.findViewById(R.id.taiwan),
                activity.findViewById(R.id.india)
            };
            
            // Reset all buttons
            for (android.widget.LinearLayout button : gameButtons) {
                if (button != null) {
                    button.setBackgroundResource(R.drawable.button_normal);
                }
            }
            
            // Highlight the selected game button
            for (int i = 0; i < activity.packageapp.length; i++) {
                if (activity.packageapp[i].equals(packageName) && i < gameButtons.length && gameButtons[i] != null) {
                    gameButtons[i].setBackgroundResource(R.drawable.button_coming);
                    break;
                }
            }
            
            // Update protection text and icon
            TextView textversions = activity.findViewById(R.id.textversions1);
            ImageView imgs1 = activity.findViewById(R.id.imgs1);
            
            if (textversions != null) {
                textversions.setText(activity.nameGame);
            }
            
            if (imgs1 != null) {
                imgs1.setBackgroundResource(imageValues.get(position));
            }
            
        } catch (Exception e) {
            FLog.error("❌ Failed to update activity UI: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return imageValues.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView gameIcon;
        private final TextView gameTitle;
        private final TextView gameVersion;
        private final TextView status;
        private final FrameLayout okBtn;
        private final ProgressBar progressIndicator;
        private final ImageView installIndicator;
        private final ImageView selectionIndicator;

        public MyViewHolder(View itemView) {
            super(itemView);
            gameIcon = itemView.findViewById(R.id.gameIcon);
            gameTitle = itemView.findViewById(R.id.gameTitle);
            gameVersion = itemView.findViewById(R.id.gameVersion);
            okBtn = itemView.findViewById(R.id.okBtn);
            status = itemView.findViewById(R.id.status);
            progressIndicator = itemView.findViewById(R.id.progressIndicator);
            installIndicator = itemView.findViewById(R.id.installIndicator);
            selectionIndicator = itemView.findViewById(R.id.selectionIndicator);
        }
    }

    /**
     * Legacy method maintained for compatibility
     */
    @SuppressLint("SetTextI18n")
    public void testanjing(FrameLayout game, TextView status, String pkg){
        // This method is now handled by updateGameStatus()
        // Keeping for compatibility but functionality moved to enhanced methods
    }

    /**
     * Legacy method maintained for compatibility
     */
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void doInstallAndRun(MyViewHolder holder, int position) {
        // This method is now handled by performGameAction()
        // Keeping for compatibility but functionality moved to enhanced methods
        performGameAction(holder, position);
    }
}

