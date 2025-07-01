package com.happy.pro.download.impl;

import android.content.Context;
import android.util.Base64;

import com.happy.pro.BuildConfig;
import com.happy.pro.download.interfaces.IKeyAuthAPI;
import com.happy.pro.server.KeyAuthManager;
import com.happy.pro.utils.FLog;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of KeyAuth API operations
 * 
 * @author BEAR-LOADER Team
 * @version 3.0.0
 */
public class KeyAuthAPIImpl implements IKeyAuthAPI {
    
    private static final String TAG = "KeyAuthAPI";
    private static final String API_URL = "https://keyauth.win/api/1.2/";
    private static final long TIMEOUT = BuildConfig.DOWNLOAD_TIMEOUT_MS;
    
    private final KeyAuthManager keyAuthManager;
    
    public KeyAuthAPIImpl(Context context) {
        this.keyAuthManager = KeyAuthManager.getInstance(context);
    }
    
    @Override
    public byte[] downloadFile(String fileId) {
        try {
            FLog.info("🔐 Downloading file ID: " + fileId);
            
            JSONObject request = new JSONObject();
            request.put("type", "file");
            request.put("fileid", fileId);
            request.put("sessionid", getSessionId());
            request.put("name", getAppName());
            request.put("ownerid", getOwnerId());
            
            JSONObject response = makeRequest(request);
            
            if (response != null && response.optBoolean("success", false)) {
                String fileContent = response.optString("contents", "");
                
                if (fileContent.isEmpty()) {
                    FLog.error("❌ Empty file content received");
                    return null;
                }
                
                byte[] fileBytes = Base64.decode(fileContent, Base64.DEFAULT);
                FLog.info("✅ Downloaded " + fileBytes.length + " bytes");
                return fileBytes;
                
            } else {
                String error = response != null ? response.optString("message", "Unknown error") : "No response";
                FLog.error("❌ Download failed: " + error);
                return null;
            }
            
        } catch (Exception e) {
            FLog.error("❌ Download error: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean isAuthenticated() {
        return keyAuthManager.isAuthenticated();
    }
    
    @Override
    public String getSessionId() {
        // This should come from KeyAuthClient or stored session
        return ""; // Implement proper session management
    }
    
    @Override
    public String getAppName() {
        return keyAuthManager.getAppName();
    }
    
    @Override
    public String getOwnerId() {
        return keyAuthManager.getOwnerID();
    }
    
    private JSONObject makeRequest(JSONObject data) {
        HttpURLConnection connection = null;
        
        try {
            URL url = new URL(API_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setConnectTimeout((int) TIMEOUT);
            connection.setReadTimeout((int) TIMEOUT);
            connection.setDoOutput(true);
            
            // Convert JSON to form data
            String formData = jsonToFormData(data);
            
            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                os.write(formData.getBytes(StandardCharsets.UTF_8));
            }
            
            // Read response
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            
            return new JSONObject(response.toString());
            
        } catch (Exception e) {
            FLog.error("❌ Request failed: " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    private String jsonToFormData(JSONObject json) throws Exception {
        StringBuilder formData = new StringBuilder();
        java.util.Iterator<String> keys = json.keys();
        
        while (keys.hasNext()) {
            String key = keys.next();
            if (formData.length() > 0) {
                formData.append("&");
            }
            formData.append(key).append("=").append(json.getString(key));
        }
        
        return formData.toString();
    }
} 