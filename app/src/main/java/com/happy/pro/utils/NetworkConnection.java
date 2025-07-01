package com.happy.pro.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

import com.blankj.molihuan.utilcode.util.ToastUtils;
import com.happy.pro.BoxApplication;


import com.happy.pro.R;


public class NetworkConnection {
    
    public static class CheckInternet {
        Context context;
        boolean isShow = false;
        
        public CheckInternet(Context ctx) {
            context = ctx;
        }
        
        
        public void registerNetworkCallback() {
            ToastUtils toast = ToastUtils.make();
            toast.setNotUseSystemToast();
            
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                new NetworkRequest.Builder();
                connectivityManager.registerDefaultNetworkCallback(
                        new ConnectivityManager.NetworkCallback() {
                            @Override
                            public void onAvailable(@NonNull Network network) {
                                isShow = false;
                                BoxApplication.get().setInternetAvailable(true);
                            }

                            @Override
                            public void onLost(@NonNull Network network) {
                                BoxApplication.get().setInternetAvailable(false);
                                if (!isShow) {
                                    toast.setLeftIcon(R.drawable.ic_error);
                                    toast.show("No Internet Connection");
                                    isShow = true;
                                }
                            }
                        });
                BoxApplication.get().setInternetAvailable(false);
            } catch (Exception e) {
                BoxApplication.get().setInternetAvailable(false);
            }
        }
    }
}

