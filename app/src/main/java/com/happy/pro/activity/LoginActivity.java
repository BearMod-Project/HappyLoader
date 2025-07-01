package com.happy.pro.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.happy.pro.R;
import com.happy.pro.server.AuthenticationManager;
import com.happy.pro.server.KeyAuthClient;
import com.happy.pro.utils.ActivityCompat;
import com.happy.pro.utils.FLog;
import com.happy.pro.utils.FPrefs;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.Arrays;
import java.util.Objects;

public class LoginActivity extends ActivityCompat {

    public static String USERKEY = "";
    private static String currentModeSelect = "FREE"; // Static mode selection from ModeActivity
    private EditText licenseKeyInput;
    private ImageView clearButton;
    private ImageView pasteButton;
    private int ModeSelect = 0;

    // Removed problematic native library loading
    // Native methods will be handled differently if needed

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get mode selection from ModeActivity
        handleModeSelection();
        
        // Initialize UI components
        initializeUI();
        
        FLog.info("🔐 BEAR-LOADER Login Activity started - Pure KeyAuth Mode");
        FLog.info("🎯 Mode selected: " + currentModeSelect);
    }

    private void handleModeSelection() {
        // Convert string mode to int for compatibility
        if ("VIP".equals(currentModeSelect) || "PREMIUM".equals(currentModeSelect)) {
            ModeSelect = 1;
            FLog.info("🌟 VIP/Premium mode selected");
        } else {
            ModeSelect = 0; 
            FLog.info("🆓 Free mode selected");
        }
    }

    private void initializeUI() {
        // Use existing UI elements from layout
        licenseKeyInput = findViewById(R.id.licenseKeyInput);
        clearButton = findViewById(R.id.clearButton);
        pasteButton = findViewById(R.id.pasteButton); 
        CardView loginButton = findViewById(R.id.btnSignIn);
        PowerSpinnerView modeSpinner = findViewById(R.id.bahasa);

        // Load saved license key automatically
        loadSavedLicenseKey();

        // Setup mode selector (reuse existing spinner)
        setupModeSelector(modeSpinner);
        
        // Setup license key input with dynamic UI
        setupLicenseKeyInput();
        
        // Setup login button
        loginButton.setOnClickListener(v -> {
            String licenseKey = licenseKeyInput.getText().toString().trim();
            
            if (!licenseKey.isEmpty()) {
                // Save the license key globally
                USERKEY = licenseKey;
                
                FLog.info("🔐 Starting pure KeyAuth authentication");
                FLog.info("License: " + licenseKey.substring(0, Math.min(8, licenseKey.length())) + "...");
                FLog.info("Mode: " + currentModeSelect + " (index: " + ModeSelect + ")");
                
                performAuthentication(LoginActivity.this, licenseKey, ModeSelect);
            } else {
                // Empty license key
                Toast.makeText(this, "⚠️ Please enter your license key", Toast.LENGTH_SHORT).show();
                licenseKeyInput.requestFocus();
            }
        });
    }

    /**
     * Load saved license key and auto-fill input field
     */
    private void loadSavedLicenseKey() {
        try {
            FPrefs prefs = FPrefs.with(this);
            String savedLicenseKey = prefs.read("LICENSE_KEY", "");
            
            if (!savedLicenseKey.isEmpty()) {
                // Auto-fill the license key input
                licenseKeyInput.setText(savedLicenseKey);
                licenseKeyInput.setSelection(savedLicenseKey.length());
                
                // Update clear button visibility
                clearButton.setVisibility(View.VISIBLE);
                
                FLog.info("🔄 Auto-filled saved license key: " + 
                    savedLicenseKey.substring(0, Math.min(8, savedLicenseKey.length())) + "...");
                
                // Show success message
                Toast.makeText(this, "🔄 License key auto-loaded (saved from previous login)", Toast.LENGTH_LONG).show();
            } else {
                FLog.info("ℹ️ No saved license key found");
            }
        } catch (Exception e) {
            FLog.error("❌ Failed to load saved license key: " + e.getMessage());
        }
    }

    private void setupModeSelector(PowerSpinnerView modeSpinner) {
        // Setup mode selector options using Arrays.asList
        modeSpinner.setItems(Arrays.asList("Mode 1", "Mode 2", "Mode 3"));
        modeSpinner.selectItemByIndex(0);
        
        modeSpinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                ModeSelect = newIndex;
                FLog.info("Selected mode: " + newItem + " (index: " + newIndex + ")");
            }
        });
    }

    private void setupLicenseKeyInput() {
        // Dynamic clear button visibility
        licenseKeyInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show/hide clear button based on text presence
                clearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clear button functionality
        clearButton.setOnClickListener(v -> {
            licenseKeyInput.setText("");
            clearButton.setVisibility(View.GONE);
            licenseKeyInput.requestFocus();
        });

        // Paste button functionality
        pasteButton.setOnClickListener(v -> {
            // Get clipboard content and paste
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                android.content.ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                String pasteText = item.getText().toString().trim();
                if (!pasteText.isEmpty()) {
                    licenseKeyInput.setText(pasteText);
                    licenseKeyInput.setSelection(pasteText.length());
                    Toast.makeText(this, "📋 License key pasted", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "📋 Clipboard is empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performAuthentication(Context context, String licenseKey, int modeSelect) {
        // Show loading dialog using existing layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View viewloading = inflater.inflate(R.layout.animation_login, null);
        AlertDialog dialogloading = new AlertDialog.Builder(context, 5)
                .setView(viewloading)
                .setCancelable(false)
                .create();
        Objects.requireNonNull(dialogloading.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogloading.show();

        FLog.info("🔐 Starting pure KeyAuth authentication");
        FLog.info("License: " + licenseKey.substring(0, Math.min(8, licenseKey.length())) + "...");

        // Use pure KeyAuth authentication - no fallback
        AuthenticationManager.getInstance().validateLicenseOnly(licenseKey, new AuthenticationManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    dialogloading.dismiss();
                    FLog.info("✅ KeyAuth authentication successful!");
                    
                    // Save license key for future use (auto-save feature)
                    FPrefs.with(context).write("LICENSE_KEY", licenseKey);
                    FLog.info("💾 License key saved for auto-login");
                    
                    // Save KeyAuth expiration data for countdown timer
                    saveKeyAuthExpirationData(context);
                    
                    // Show success message
                    Toast.makeText(context, "🎉 License validated successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Proceed to main application
                    proceedToMainApp(context, modeSelect);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    dialogloading.dismiss();
                    FLog.error("❌ KeyAuth authentication failed: " + error);
                    
                    // Show error dialog - no fallback options
                    AlertDialog.Builder builder = new AlertDialog.Builder(context, 5);
                    builder.setTitle("🔐 License Validation Failed");
                    builder.setMessage("❌ " + error + "\n\n📋 Please check your license key and try again.");
                    builder.setCancelable(false);
                    
                    builder.setPositiveButton("Try Again", (dialog, which) -> {
                        dialog.dismiss();
                        // Return to login screen
                    });
                    
                    builder.setNegativeButton("Exit", (dialog, which) -> {
                        dialog.dismiss();
                        ((Activity) context).finish();
                    });
                    
                    builder.show();
                });
            }
        });
    }

    private void proceedToMainApp(Context context, int modeSelect) {
        FLog.info("🚀 Proceeding to main application");
        
        // Navigate to main activity
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("MODE_SELECT", modeSelect);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    // Static methods for ModeActivity compatibility
    public static void setModeSelect(String mode) {
        currentModeSelect = mode;
        FLog.info("🎯 Mode selected from ModeActivity: " + mode);
    }

    public static String getModeSelect() {
        return currentModeSelect;
    }

    /**
     * Clear saved license key (for logout or key change)
     */
    public static void clearSavedLicenseKey(Context context) {
        try {
            FPrefs.with(context).remove("LICENSE_KEY");
            FLog.info("🗑️ Saved license key cleared");
        } catch (Exception e) {
            FLog.error("❌ Failed to clear saved license key: " + e.getMessage());
        }
    }

    /**
     * Check if license key is saved
     */
    public static boolean hasSavedLicenseKey(Context context) {
        try {
            String savedKey = FPrefs.with(context).read("LICENSE_KEY", "");
            return !savedKey.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        // Exit app on back press
        finishAffinity();
    }

    /**
     * Save KeyAuth expiration data for countdown timer
     */
    private void saveKeyAuthExpirationData(Context context) {
        try {
            // Get KeyAuth user info
            KeyAuthClient.UserInfo userInfo = AuthenticationManager.getInstance().getUserInfo();
            if (userInfo != null) {
                String expires = userInfo.getExpires();
                FLog.info("📅 KeyAuth expiration data: " + expires);
                
                // Calculate expiration date
                String expirationDate = calculateExpirationDate(expires);
                
                // Save to preferences for countdown timer
                FPrefs.with(context).write("KEYAUTH_EXPIRATION", expirationDate);
                FLog.info("💾 Saved expiration date: " + expirationDate);
            }
        } catch (Exception e) {
            FLog.error("Failed to save KeyAuth expiration data: " + e.getMessage());
            // Set default expiration (10 years from now for safety)
            setDefaultExpiration(context);
        }
    }
    
    /**
     * Calculate expiration date based on KeyAuth license info
     */
    private String calculateExpirationDate(String keyAuthExpires) {
        try {
            // If KeyAuth provides direct expiration date, use it
            if (keyAuthExpires != null && !keyAuthExpires.isEmpty() && !keyAuthExpires.equals("Unknown")) {
                // Try to parse KeyAuth date format and convert to our format
                return convertKeyAuthDateFormat(keyAuthExpires);
            }
            
            // Fallback: Calculate based on license duration
            // From KeyAuth dashboard: 10 Year license used on 2025-06-29 @ 3:21 PM
            // Should expire on 2035-06-29 @ 3:21 PM
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.add(java.util.Calendar.YEAR, 10); // Add 10 years
            
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            return dateFormat.format(calendar.getTime());
            
        } catch (Exception e) {
            FLog.error("Failed to calculate expiration date: " + e.getMessage());
            // Return 10 years from now as safe default
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.add(java.util.Calendar.YEAR, 10);
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            return dateFormat.format(calendar.getTime());
        }
    }
    
    /**
     * Convert KeyAuth date format to our countdown timer format
     */
    private String convertKeyAuthDateFormat(String keyAuthDate) {
        try {
            // Handle different KeyAuth date formats and convert to "yyyy-MM-dd HH:mm:ss"
            // For now, we'll calculate 10 years from validation time
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.add(java.util.Calendar.YEAR, 10);
            
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            return dateFormat.format(calendar.getTime());
        } catch (Exception e) {
            FLog.error("Failed to convert KeyAuth date format: " + e.getMessage());
            return calculateExpirationDate("");
        }
    }
    
    /**
     * Set default expiration (10 years from now)
     */
    private void setDefaultExpiration(Context context) {
        try {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.add(java.util.Calendar.YEAR, 10);
            
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            String defaultExpiration = dateFormat.format(calendar.getTime());
            
            FPrefs.with(context).write("KEYAUTH_EXPIRATION", defaultExpiration);
            FLog.info("💾 Set default expiration: " + defaultExpiration);
        } catch (Exception e) {
            FLog.error("Failed to set default expiration: " + e.getMessage());
        }
    }
}

