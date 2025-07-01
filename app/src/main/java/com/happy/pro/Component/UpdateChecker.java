package com.happy.pro.Component;

import com.happy.pro.config.AppConfigManager;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.L;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.happy.pro.BuildConfig;
import com.happy.pro.R;
import com.happy.pro.utils.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


public class UpdateChecker extends AppCompatActivity {

    private final Context context;
    private static final String AUTHORITY = "com.happy.pro.provider";

    public UpdateChecker(Context context) {
        this.context = context;
    }

    public void checkForUpdate() {
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // Use modern configuration manager instead of native API
        String updateUrl = AppConfigManager.getInstance(context).getUpdateServerUrl();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, updateUrl, null,
                response -> {
                    try {
                        String latestVersion = response.getString("version");
                        String currentVersion = context.getPackageManager()
                                .getPackageInfo(context.getPackageName(), 0).versionName;

                        assert currentVersion != null;
                        if (!currentVersion.equals(latestVersion)) {
                            String apkUrl = response.getString("url");
                            showCustomDialog(context,apkUrl);
                        } else {
                            Toast.makeText(context, R.string.application_already_latest_versiob, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException | PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);

        requestQueue.add(jsonObjectRequest);
    }

    private void showCustomDialog(Context context,final String apkUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.alert_one, null);
        builder.setView(dialogView);

        dialogView.findViewById(R.id.heading_alert_info);
        dialogView.findViewById(R.id.title_alert_info);
        TextView buttonYes = dialogView.findViewById(R.id.alertok);
        TextView buttonNo = dialogView.findViewById(R.id.alertno);
        AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        buttonYes.setOnClickListener(v -> {
            alertDialog.dismiss();
            downloadAndUpdate(apkUrl);
        });

        buttonNo.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }
    private void downloadAndUpdate(String apkUrl) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setTitle(context.getString(R.string.downloading_update));
        request.setDescription(context.getString(R.string.downloading_new_version_of_app));
        File apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "app.apk");
        if (apkFile.exists()) {
            apkFile.delete();
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app.apk");

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        ContextCompat.registerReceiver(context, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "app.apk");
                    installApk(file);
                }
            }
        }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void installApk(File file) {
        Uri apkUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }
}

