package com.happy.pro.core.auth;

import android.content.Context;

/**
 * 🔥 Host Context for Bear-Loader Container System
 * 
 * Represents the authentication and identification context 
 * for a host application using the container system
 */
public class HostContext {
    
    private final String hostId;
    private final String packageName;
    private final String applicationName;
    private final Context androidContext;
    private final long createdAt;
    private final String signature;
    
    public HostContext(String hostId, String packageName, String applicationName, 
                      Context androidContext, String signature) {
        this.hostId = hostId;
        this.packageName = packageName;
        this.applicationName = applicationName;
        this.androidContext = androidContext;
        this.signature = signature;
        this.createdAt = System.currentTimeMillis();
    }
    
    public String getHostId() { return hostId; }
    public String getPackageName() { return packageName; }
    public String getApplicationName() { return applicationName; }
    public Context getAndroidContext() { return androidContext; }
    public String getSignature() { return signature; }
    public long getCreatedAt() { return createdAt; }
    
    @Override
    public String toString() {
        return "HostContext{" +
                "hostId='" + hostId + '\'' +
                ", packageName='" + packageName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                '}';
    }
} 
