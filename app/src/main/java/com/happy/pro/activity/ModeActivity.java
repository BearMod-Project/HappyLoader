package com.happy.pro.activity;

import static com.happy.pro.activity.SplashActivity.mahyong;
import com.happy.pro.config.AppConfigManager;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.happy.pro.Component.UpdateChecker;
import com.happy.pro.R;
import com.happy.pro.utils.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

/**************************
 * BUILD ON Android Studio
 * TELEGRAM : OxZeroo
 * *************************/


public class ModeActivity extends ActivityCompat {

    public static boolean Kooontoool = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);
        if (!mahyong){
            finish();
            finishActivity(1);
        }

        // Ensure native library is loaded before checking for updates
        try {
            // Force load the native library
            System.loadLibrary("happy");
            
            UpdateChecker updateChecker = new UpdateChecker(this);
            updateChecker.checkForUpdate();
        } catch (UnsatisfiedLinkError e) {
            // If native library fails to load, skip update check
            Toast.makeText(this, "Update check unavailable", Toast.LENGTH_SHORT).show();
        }
        LinearLayout silver = findViewById(R.id.btnfree);
        LinearLayout gold = findViewById(R.id.btnpremium);
        LinearLayout telegram = findViewById(R.id.telegramss);
        AnimationUtils.loadAnimation(this, R.anim.bounce);

        telegram.setOnClickListener(v -> {
            Animator scale = ObjectAnimator.ofPropertyValuesHolder(v,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 0.5f, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0.5f, 1));
            scale.setDuration(1000);
            scale.start();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // Use modern configuration manager instead of native API
            String telegramUrl = AppConfigManager.getInstance(this).getTelegramUrl();
            intent.setData(Uri.parse(telegramUrl));
            startActivity(intent);
        });

        silver.setOnClickListener(v -> {
            Animator scale = ObjectAnimator.ofPropertyValuesHolder(v,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 0.5f, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0.5f, 1));
            scale.setDuration(1000);
            scale.start();            LoginActivity.setModeSelect("FREE");
            startActivity(new Intent(ModeActivity.this, LoginActivity.class));
            Kooontoool = false;
        });

        gold.setOnClickListener(v -> {
            Animator scale = ObjectAnimator.ofPropertyValuesHolder(v,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 0.5f, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0.5f, 1));
            scale.setDuration(1000);
            scale.start();            LoginActivity.setModeSelect("VIP");
            startActivity(new Intent(ModeActivity.this, LoginActivity.class));
            Kooontoool = true;
        });
    }
}

