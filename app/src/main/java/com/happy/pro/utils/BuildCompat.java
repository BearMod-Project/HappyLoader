package com.happy.pro.utils;

import android.os.Build;


public class BuildCompat {
    
    public static boolean isA11below() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.R;
    }
    
    public static boolean isA12above() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }
    
    public static boolean atLeastTiramisu() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    public static boolean atLeastR() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }
}

