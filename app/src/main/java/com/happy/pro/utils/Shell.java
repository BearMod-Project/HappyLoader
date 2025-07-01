package com.happy.pro.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 🐻 BEAR-LOADER Shell Utility
 * 
 * Provides safe and reliable shell access detection and command execution
 * with proper error handling and logging.
 * 
 * @author BEAR-LOADER Team
 * @version 3.0.0
 */
public class Shell {
    private static final String TAG = "BearShell";
    
    private static Boolean isRooted = null;
    
    /**
     * Check if the device has root access
     * @return true if root access is available, false otherwise
     */
    public static boolean rootAccess() {
        if (isRooted != null) {
            return isRooted;
        }
        
        FLog.info("🔍 Checking for root access...");
        
        // Method 1: Check for su binary
        if (checkSuBinary()) {
            isRooted = true;
            FLog.info("✅ Root access confirmed via su binary");
            return true;
        }
        
        // Method 2: Try to execute su command
        if (testSuCommand()) {
            isRooted = true;
            FLog.info("✅ Root access confirmed via su command");
            return true;
        }
        
        // Method 3: Check common root directories
        if (checkRootDirectories()) {
            isRooted = true;
            FLog.info("✅ Root access confirmed via directory check");
            return true;
        }
        
        isRooted = false;
        FLog.info("❌ No root access detected");
        return false;
    }
    
    /**
     * Check for su binary in common locations
     */
    private static boolean checkSuBinary() {
        String[] suPaths = {
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        };
        
        for (String path : suPaths) {
            try {
                java.io.File file = new java.io.File(path);
                if (file.exists() && file.canExecute()) {
                    FLog.info("📁 Found su binary at: " + path);
                    return true;
                }
            } catch (Exception e) {
                // Continue checking other paths
            }
        }
        
        return false;
    }
    
    /**
     * Test if su command can be executed
     */
    private static boolean testSuCommand() {
        Process process = null;
        DataOutputStream os = null;
        BufferedReader reader = null;
        
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            os.writeBytes("echo 'test'\n");
            os.writeBytes("exit\n");
            os.flush();
            
            String response = reader.readLine();
            int exitValue = process.waitFor();
            
            return exitValue == 0 && "test".equals(response);
            
        } catch (Exception e) {
            FLog.error("❌ Su command test failed: " + e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) os.close();
                if (reader != null) reader.close();
                if (process != null) process.destroy();
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }
    
    /**
     * Check for common root directories
     */
    private static boolean checkRootDirectories() {
        String[] rootDirs = {
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/which",
            "/data/local/xbin/su",
            "/data/local/bin/su"
        };
        
        for (String dir : rootDirs) {
            try {
                java.io.File file = new java.io.File(dir);
                if (file.exists()) {
                    FLog.info("📁 Found root indicator at: " + dir);
                    return true;
                }
            } catch (Exception e) {
                // Continue checking
            }
        }
        
        return false;
    }
    
    /**
     * Execute a shell command with root privileges
     * @param command The command to execute
     * @return true if command executed successfully, false otherwise
     */
    public static boolean executeRootCommand(String command) {
        if (!rootAccess()) {
            FLog.error("❌ Cannot execute root command: No root access");
            return false;
        }
        
        Process process = null;
        DataOutputStream os = null;
        
        try {
            FLog.info("🚀 Executing root command: " + command);
            
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            
            int exitValue = process.waitFor();
            
            if (exitValue == 0) {
                FLog.info("✅ Root command executed successfully");
                return true;
            } else {
                FLog.error("❌ Root command failed with exit code: " + exitValue);
                return false;
            }
            
        } catch (Exception e) {
            FLog.error("❌ Exception executing root command: " + e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) os.close();
                if (process != null) process.destroy();
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }
    
    /**
     * Execute a regular shell command (non-root)
     * @param command The command to execute
     * @return Output of the command or null if failed
     */
    public static String executeCommand(String command) {
        try {
            FLog.info("🔧 Executing command: " + command);
            
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitValue = process.waitFor();
            
            if (exitValue == 0) {
                FLog.info("✅ Command executed successfully");
                return output.toString().trim();
            } else {
                FLog.error("❌ Command failed with exit code: " + exitValue);
                return null;
            }
            
        } catch (Exception e) {
            FLog.error("❌ Exception executing command: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Clear the cached root status (useful for testing)
     */
    public static void clearRootCache() {
        isRooted = null;
        FLog.info("🔄 Root access cache cleared");
    }
    
    /**
     * Get detailed root status information
     * @return String with root status details
     */
    public static String getRootStatusInfo() {
        StringBuilder info = new StringBuilder();
        info.append("🐻 BEAR Shell Status:\n");
        info.append("Root Access: ").append(rootAccess() ? "✅ AVAILABLE" : "❌ NOT AVAILABLE").append("\n");
        info.append("Su Binary: ").append(checkSuBinary() ? "✅ FOUND" : "❌ NOT FOUND").append("\n");
        info.append("Su Command: ").append(testSuCommand() ? "✅ WORKING" : "❌ NOT WORKING").append("\n");
        info.append("Root Dirs: ").append(checkRootDirectories() ? "✅ FOUND" : "❌ NOT FOUND").append("\n");
        
        return info.toString();
    }
} 
