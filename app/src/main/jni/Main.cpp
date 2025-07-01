#include <jni.h>
#include <string>
#include <sys/ptrace.h>
#include <unistd.h>
#include <sys/wait.h>
#include <dlfcn.h>
#include <dirent.h>
#include <fstream>
#include <sys/system_properties.h>
#include <sys/prctl.h>
#include <sys/stat.h>
#include <mutex>
#include "import.h"
#include "rLogin/Login.h"
#include "ESP.h"
#include "Hacks.h"
#include "HookManager.h"
#include "MemoryProtection.h"
#include "struct_compatibility.h"

// ========================================
// BEAR-LOADER 3.0.0 GLOBAL INSTANCES  
// ========================================
Options options;
Memory memory;

// Constants for Android system properties
#ifndef PROP_VALUE_MAX
#define PROP_VALUE_MAX 92
#endif

// Forward declarations for HttpCanary detection functions
void Detected_REIHttpCanary();
void Detected_REIHttpCanary1();
void Detected_REIHttpCanary2();
void Detected_REIHttpCanary3();

ESP espOverlay;
int type = 1, utype = 2;

// ============ Advanced Security & Anti-Detection ============
namespace BearSecurity {
    bool antiDebugActive = false;
    bool stealthModeEnabled = false;
    bool memoryProtectionEnabled = false;
    
    // Anti-Frida Detection
    bool detectFrida() {
        // Check for Frida server process
        DIR* dir = opendir("/proc");
        if (dir == nullptr) return false;
        
        struct dirent* entry;
        while ((entry = readdir(dir)) != nullptr) {
            if (strstr(entry->d_name, "frida") != nullptr) {
                closedir(dir);
                return true; // Frida detected
            }
        }
        closedir(dir);
        
        // Check for Frida libraries
        void* handle = dlopen("libfrida-gum.so", RTLD_NOW);
        if (handle != nullptr) {
            dlclose(handle);
            return true; // Frida library detected
        }
        
        return false;
    }
    
    // Anti-Xposed Detection
    bool detectXposed() {
        // Check for Xposed framework
        std::ifstream maps("/proc/self/maps");
        std::string line;
        while (std::getline(maps, line)) {
            if (line.find("XposedBridge") != std::string::npos ||
                line.find("lsposed") != std::string::npos ||
                line.find("edxposed") != std::string::npos) {
                return true;
            }
        }
        
        // Check for Xposed property
        char prop[PROP_VALUE_MAX];
        if (__system_property_get("ro.xposed.framework", prop) > 0) {
            return true;
        }
        
        return false;
    }
    
    // Anti-Debug Protection
    void enableAntiDebug() {
        if (antiDebugActive) return;
        
        // Ptrace protection
        if (ptrace(PTRACE_TRACEME, 0, 1, 0) == -1) {
            // Already being traced - terminate
            exit(1);
        }
        
        // Fork bomb for debugging protection
        pid_t child = fork();
        if (child == 0) {
            // Child process - monitor parent
            if (ptrace(PTRACE_ATTACH, getppid(), 0, 0) == -1) {
                // Parent is being debugged - terminate
                kill(getppid(), SIGKILL);
            }
            exit(0);
        } else if (child > 0) {
            // Parent process - wait for child
            waitpid(child, nullptr, 0);
        }
        
        antiDebugActive = true;
    }
    
    // Memory Protection
    void enableMemoryProtection() {
        if (memoryProtectionEnabled) return;
        
        // Protect critical memory regions
        mprotect(&espOverlay, sizeof(espOverlay), PROT_READ | PROT_EXEC);
        mprotect(&options, sizeof(options), PROT_READ | PROT_WRITE);
        
        memoryProtectionEnabled = true;
    }
    
    // Stealth Mode
    void enableStealthMode() {
        if (stealthModeEnabled) return;
        
        // Hide process name
        prctl(PR_SET_NAME, "system_server", 0, 0, 0);
        
        // Enable all security measures
        enableAntiDebug();
        enableMemoryProtection();
        
        stealthModeEnabled = true;
    }
    
    // Runtime Security Check
    bool performSecurityCheck() {
        // Check for threats
        if (detectFrida()) {
            return false; // Frida detected
        }
        
        if (detectXposed()) {
            return false; // Xposed detected
        }
        
        // Validate ESP overlay integrity
        if (!espOverlay.isValid()) {
            return false; // ESP overlay corrupted
        }
        
        return true; // All checks passed
    }
}

// ============ BEAR Memory Protection Integration ============
namespace BearMemoryIntegration {
    static std::mutex g_protectionMutex;
    static bool g_memoryProtectionInitialized = false;
    static std::unique_ptr<bearmundo::security::BearMemoryManager> g_memoryManager;
    
    /**
     * Initialize BEAR Memory Protection System
     */
    bool initializeMemoryProtection() {
        std::lock_guard<std::mutex> lock(g_protectionMutex);
        
        if (g_memoryProtectionInitialized) {
            return true;
        }
        
        try {
            // Get the memory manager instance
            auto& manager = bearmundo::security::BearMemoryManager::getInstance();
            
            // Initialize protection
            if (manager.initializeProtection()) {
                g_memoryProtectionInitialized = true;
                return true;
            }
            
            return false;
        } catch (const std::exception& e) {
            return false;
        }
    }
    
    /**
     * Protect BEAR components
     */
    bool protectBearComponents() {
        std::lock_guard<std::mutex> lock(g_protectionMutex);
        
        if (!g_memoryProtectionInitialized) {
            return false;
        }
        
        try {
            auto& manager = bearmundo::security::BearMemoryManager::getInstance();
            return manager.protectBearComponents();
        } catch (const std::exception& e) {
            return false;
        }
    }
    
    /**
     * Enable advanced protection
     */
    bool enableAdvancedProtection() {
        std::lock_guard<std::mutex> lock(g_protectionMutex);
        
        if (!g_memoryProtectionInitialized) {
            return false;
        }
        
        try {
            auto& manager = bearmundo::security::BearMemoryManager::getInstance();
            return manager.enableAdvancedProtection();
        } catch (const std::exception& e) {
            return false;
        }
    }
    
    /**
     * Get memory protection status
     */
    std::string getMemoryProtectionStatus() {
        std::lock_guard<std::mutex> lock(g_protectionMutex);
        
        if (!g_memoryProtectionInitialized) {
            return "Memory protection not initialized";
        }
        
        try {
            auto& manager = bearmundo::security::BearMemoryManager::getInstance();
            auto* protection = manager.getProtection();
            if (protection) {
                auto statusList = protection->getProtectionStatus();
                std::string result;
                for (const auto& line : statusList) {
                    result += line + "\n";
                }
                return result;
            }
            return "Protection status unavailable";
        } catch (const std::exception& e) {
            return "Error retrieving status";
        }
    }
}

/* ================ SERVER FUNCTION =========================*/

extern "C"
JNIEXPORT jstring JNICALL
Java_com_happy_pro_material_LoginActivity_GetKey(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(OBFUSCATE("https://t.me/kothar1992"));
}

/* ================ ENHANCED ESP FUNCTION =========================*/

extern "C" JNIEXPORT void JNICALL
Java_com_happy_pro_floating_Overlay_DrawOn(JNIEnv *env, jclass , jobject espView, jobject canvas) {
    // Log that the native method was called
    __android_log_print(ANDROID_LOG_INFO, "BEAR", "🎯 DrawOn native method called");
    
    // ============ Advanced Security Checks ============
    
    // 1. Runtime Security Validation
    if (!BearSecurity::performSecurityCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, "BEAR", "❌ Security check failed");
        // Security threat detected - disable ESP
        options.openState = -1;
        options.aimBullet = -1;
        options.aimT = -1;
        return;
    }
    __android_log_print(ANDROID_LOG_INFO, "BEAR", "✅ Security check passed");
    
    // 2. Anti-Tampering Check
    static int callCount = 0;
    callCount++;
    if (callCount % 100 == 0) { // Check every 100 calls
        BearSecurity::enableMemoryProtection();
    }
    
    // 3. ESP Overlay Initialization with Security
    try {
        espOverlay = ESP(env, espView, canvas);
        
        if (!espOverlay.isValid()) {
            // ESP overlay validation failed
            return;
        }
        
        // 4. Token Validation (Enhanced)
        if (g_Token.empty() || g_Auth.empty()) {
            // No valid authentication
            return;
        }
        
        if (g_Token != g_Auth) {
            // Authentication mismatch
            return;
        }
        
        // 5. Advanced Signature Validation
        if (!signValid) {
            // Signature validation failed - crash protection
            int *p = nullptr;
            *p = 0;
            return;
        }
        
        // ============ Enhanced ESP Rendering ============
        
        // 6. Stealth Rendering Mode
        if (BearSecurity::stealthModeEnabled) {
            // Reduce rendering frequency to avoid detection
            static int stealthCounter = 0;
            stealthCounter++;
            if (stealthCounter % 3 != 0) {
                return; // Skip 2/3 of frames in stealth mode
            }
        }
        
        // 7. Memory-Safe ESP Drawing
        const int screenWidth = espOverlay.getWidth();
        const int screenHeight = espOverlay.getHeight();
        
        if (screenWidth <= 0 || screenHeight <= 0) {
            return; // Invalid screen dimensions
        }
        
        // 8. Protected ESP Execution
        DrawESP(espOverlay, screenWidth, screenHeight);
        
        // 9. Post-Render Security Check
        if (callCount % 50 == 0) { // Periodic security validation
            if (!BearSecurity::performSecurityCheck()) {
                // Threat detected during rendering
                options.openState = -1;
                options.aimBullet = -1;
                options.aimT = -1;
            }
        }
        
    } catch (const std::exception& e) {
        // Exception handling - secure cleanup
        options.openState = -1;
        options.aimBullet = -1;
        options.aimT = -1;
    } catch (...) {
        // Unknown exception - emergency shutdown
        options.openState = -1;
        options.aimBullet = -1;
        options.aimT = -1;
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_happy_pro_server_ApiServer_EXP(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(ts.c_str());
}


int Register1(JNIEnv *env) {
    JNINativeMethod methods[] = {
            {"native_Check", "(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", (void *) native_Check}
    };
    jclass clazz = env->FindClass("com/happy/pro/activity/LoginActivity");
    if (!clazz)
        return -1;

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) != 0)
        return -1;

    return 0;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    
    // ============ Enhanced Security Initialization ============
    
    // 1. Register native methods
    if (Register1(env) != 0)
        return -1;
    
    // 2. Enable Advanced Anti-Detection (Re-enabled and Enhanced)
    try {
        // Enable Frida detection
        if (BearSecurity::detectFrida()) {
            // Frida detected - terminate immediately
            exit(1);
        }
        
        // Enable Xposed detection  
        if (BearSecurity::detectXposed()) {
            // Xposed detected - terminate immediately
            exit(1);
        }
        
        // Enable anti-debug protection
        BearSecurity::enableAntiDebug();
        
        // Enable memory protection
        BearSecurity::enableMemoryProtection();
        
        // Initialize stealth mode
        BearSecurity::enableStealthMode();
        
        // Enhanced HttpCanary Detection (Re-enabled)
        Detected_REIHttpCanary();
        Detected_REIHttpCanary1();
        Detected_REIHttpCanary2();
        Detected_REIHttpCanary3();
        
    } catch (...) {
        // Security initialization failed - terminate
        exit(1);
    }
    
    // 3. Runtime Environment Validation
    static bool securityValidated = false;
    if (!securityValidated) {
        // Perform comprehensive security check
        if (!BearSecurity::performSecurityCheck()) {
            // Security validation failed
            exit(1);
        }
        securityValidated = true;
    }
    
    return JNI_VERSION_1_6;
}

// ============ HttpCanary Detection Functions ============

void Detected_REIHttpCanary() {
    // Check for HttpCanary main package
    std::string folderPath = "/storage/emulated/0/Android/data/com.guoshi.httpcanary";
    struct stat buffer{};
    if (stat(folderPath.c_str(), &buffer) == 0) {
        // HttpCanary detected - terminate
        exit(1);
    }
}

void Detected_REIHttpCanary1() {
    // Check for HttpCanary premium package
    std::string folderPath = "/storage/emulated/0/Android/data/com.guoshi.httpcanary.premium";
    struct stat buffer{};
    if (stat(folderPath.c_str(), &buffer) == 0) {
        // HttpCanary Premium detected - terminate
        exit(1);
    }
}

void Detected_REIHttpCanary2() {
    // Check for Sniffer package
    std::string folderPath = "/storage/emulated/0/Android/data/com.sniffer";
    struct stat buffer{};
    if (stat(folderPath.c_str(), &buffer) == 0) {
        // Sniffer detected - terminate
        exit(1);
    }
}

void Detected_REIHttpCanary3() {
    // Check for HttpCanary Pro package
    std::string folderPath = "/storage/emulated/0/Android/data/com.httpcanary.pro";
    struct stat buffer{};
    if (stat(folderPath.c_str(), &buffer) == 0) {
        // HttpCanary Pro detected - terminate
        exit(1);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_happy_pro_floating_Overlay_Close(JNIEnv *, jobject) {
    Close();
    options.openState = -1;
    options.aimBullet = -1;
    options.aimT = -1;
}


extern "C" JNIEXPORT jboolean JNICALL
Java_com_happy_pro_floating_Overlay_getReady(JNIEnv *, jobject thiz) {
    __android_log_print(ANDROID_LOG_INFO, "BEAR", "🚀 getReady native method called");
    
    // BEAR-LOADER 3.0.0: Bypass socket dependency for standalone operation
    // Return success immediately to avoid socket connection issues
    __android_log_print(ANDROID_LOG_INFO, "BEAR", "🔄 Using standalone mode - bypassing socket initialization");
    __android_log_print(ANDROID_LOG_INFO, "BEAR", "✅ getReady returning true (standalone mode)");
    return true;
    
    // Legacy socket code (disabled for standalone operation)
    /*
    int sockCheck = 1;

    if (!Create()) {
        __android_log_print(ANDROID_LOG_ERROR, "BEAR", "❌ Socket creation failed");
        perror("Creation failed");
        return false;
    }
    __android_log_print(ANDROID_LOG_INFO, "BEAR", "✅ Socket created successfully");
    setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &sockCheck, sizeof(int));
    if (!Bind()) {
        perror("Bind failed");
        return false;
    }

    if (!Listen()) {
        perror("Listen failed");
        return false;
    }
    if (Accept()) {
        return true;
    }
    */
}


extern "C"
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_ToggleAim_ToggleAim(JNIEnv *, jobject thiz, jboolean value) {
    if (value)
        options.openState = 0;
    else
        options.openState = -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_ToggleBullet_ToggleBullet(JNIEnv *, jobject thiz, jboolean value) {
    if (value)
        options.aimBullet = 0;
    else
        options.aimBullet = -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_ToggleSimulation_ToggleSimulation(JNIEnv *, jobject thiz, jboolean value) {
    if (value)
        options.aimT = 0;
    else
        options.aimT = -1;
}

extern "C" JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_SettingValue(JNIEnv *, jobject, jint code, jboolean jboolean1) {

    switch ((int) code) {
        case 2:
            isPlayerLine = jboolean1;
            break;
        case 3:
            isPlayerBox = jboolean1;
            break;
        case 4:
            isSkeleton = jboolean1;
            break;
        case 5:
            isPlayerDistance = jboolean1;
            break;
        case 6:
            isPlayerHealth = jboolean1;
            break;
        case 7:
            isPlayerName = jboolean1;
            break;
        case 8:
            isPlayerHead = jboolean1;
            break;
        case 9:
            is360Alert = jboolean1;
            break;
        case 10:
            isPlayerWeapon = jboolean1;
            break;
        case 11:
            isGrenadeWarning = jboolean1;
            break;
        case 12:
            isVehicles = jboolean1;
            break;
        case 13:
            isItems = jboolean1;
            break;
        case 14:
            isLootBox = jboolean1;
            break;
        case 15:
            options.ignoreAi = jboolean1;
            break;
        case 16:
            isPlayerWeaponIcon = jboolean1;
            break;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_SettingAim(JNIEnv *env, jobject thiz, jint setting_code, jboolean value) {
    switch ((int) setting_code) {
        case 1:
            options.openState = -1;
            break;
        case 2:
            options.aimBullet = -1;
            break;
        case 3:
            options.pour = value;
            break;
        case 4:
            options.ignoreBot = value;
            break;
        case 5:
            options.InputInversion = value;
            break;
        case 6:
            options.tracingStatus = value;
            break;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_SettingMemory(JNIEnv *env, jobject thiz, jint setting_code, jboolean value) {
    switch ((int) setting_code) {
        case 1:
            otherFeature.LessRecoil = value;
            break;
        case 2:
            otherFeature.SmallCrosshair = value;
            break;
        case 3:
            otherFeature.Aimbot = value;
            break;
        case 4:
            otherFeature.WideView = value;
            break;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_Range(JNIEnv *, jobject, jint range) {
    options.aimingRange = 1 + range;
}

extern "C" JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_distances(JNIEnv *, jobject, jint distances) {
    options.aimingDist = distances;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_recoil(JNIEnv *env, jobject thiz, jint recoil) {
    options.recCompe = recoil;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_recoil2(JNIEnv *env, jobject thiz, jint recoil) {
    options.recCompe1 = recoil;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_recoil3(JNIEnv *env, jobject thiz, jint recoil) {
    options.recCompe2 = recoil;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_Bulletspeed(JNIEnv *env, jobject thiz, jint bulletspeed) {
    options.aimingSpeed = bulletspeed;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_AimingSpeed(JNIEnv *env, jobject thiz, jint aimingspeed) {
    options.touchSpeed = aimingspeed;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_Smoothness(JNIEnv *env, jobject thiz, jint smoothness) {
    options.Smoothing = smoothness;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_TouchSize(JNIEnv *env, jobject thiz, jint touchsize) {
    options.touchSize = touchsize;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_TouchPosX(JNIEnv *env, jobject thiz, jint touchposx) {
    options.touchX = touchposx;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_TouchPosY(JNIEnv *env, jobject thiz, jint touchposy) {
    options.touchY = touchposy;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_WideView(JNIEnv *env, jobject thiz, jint wideview) {
    otherFeature.WideView = wideview;
}

extern "C" JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_Target(JNIEnv *, jobject, jint target) {
    options.aimbotmode = target;
}
extern "C" JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_AimWhen(JNIEnv *, jobject, jint state) {
    options.aimingState = state;
}
extern "C" JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_AimBy(JNIEnv *, jobject, jint aimby) {
    options.priority = aimby;
}
extern "C" JNIEXPORT void JNICALL
Java_com_happy_pro_floating_FloatService_RadarSize(JNIEnv *, jobject, jint size) {
    // BEAR-LOADER 3.0.0: Radar size is now handled locally in Hacks.h
    // No longer part of the Request structure - radar is calculated locally
}

// ====================== Main Activity ====================== //

extern "C" JNIEXPORT jstring JNICALL
Java_com_happy_pro_material_MainActivity_Telegram(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(OBFUSCATE("https://t.me/kothar1992"));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_happy_pro_server_ApiServer_getOwner(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(OBFUSCATE("https://t.me/bear_mod"));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_happy_pro_server_ApiServer_getTelegram(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(OBFUSCATE("https://t.me/bearfeedbackbot"));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_happy_pro_server_ApiServer_getGrup(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(OBFUSCATE("https://t.me/kothar1992"));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_happy_pro_server_ApiServer_FixCrash(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(
            OBFUSCATE("https://mod-key.click/"));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_happy_pro_server_ApiServer_activity(JNIEnv *env, jclass clazz) {
    if (!memek){
        return env->NewStringUTF(
                OBFUSCATE("com.happy.pro.activity.LoginActivity"));
    }else{
        return env->NewStringUTF(
                OBFUSCATE("com.happy.pro.activity.MainActivity"));
    }
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_happy_pro_server_ApiServer_mainURL(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(
            OBFUSCATE("https://api.mod-key.click"));

}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_happy_pro_Component_DownloadZip_pw(JNIEnv *env,
                                            [[maybe_unused]] jobject thiz) {
    return env->NewStringUTF(
            OBFUSCATE(" "));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_happy_pro_server_ApiServer_URLJSON(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(
            OBFUSCATE("https://config.mod-key.click/games.json"));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_happy_pro_server_ApiServer_ApiKeyBox(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(
            OBFUSCATE(" "));
}

extern "C" [[maybe_unused]] JNIEXPORT jstring JNICALL
Java_com_happy_pro_Component_MainService_InitBase(JNIEnv* env, jclass clazz) {
    // Return empty string instead of causing undefined behavior
    return env->NewStringUTF("");
}

//extern "C" [[maybe_unused]] JNIEXPORT void JNICALL
//Java_com_happy_pro_Component_MainService_closeSocket(JNIEnv* env, jclass clazz) {
    // Safely close socket connections
   //Close();
//}

#if 0 // Disabled duplicate JNI functions (implemented in BearMundoJNI.cpp)
// ============ JNI Methods for HookManager Integration ============

extern "C" JNIEXPORT jboolean JNICALL
Java_com_happy_pro_hooks_HookManager_nativeInitializeMemoryProtection(JNIEnv *env, jobject thiz) {
    try {
        // Enhanced security validation (from BearMod pattern)
        if (!BearSecurity::performSecurityCheck()) {
            return JNI_FALSE;
        }
        
        bool result = BearMemoryIntegration::initializeMemoryProtection();
        return result ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_happy_pro_hooks_HookManager_nativeProtectESPRegions(JNIEnv *env, jobject thiz) {
    try {
        // Security check before protecting ESP regions
        if (!BearSecurity::performSecurityCheck()) {
            return JNI_FALSE;
        }
        
        // Protect ESP overlay memory
        if (BearMemoryIntegration::g_memoryProtectionInitialized) {
            auto& manager = bearmundo::security::BearMemoryManager::getInstance();
            auto* protection = manager.getProtection();
            if (protection) {
                // Protect ESP overlay structure
                void* espAddr = &espOverlay;
                size_t espSize = sizeof(espOverlay);
                return protection->protectRegion(espAddr, espSize) ? JNI_TRUE : JNI_FALSE;
            }
        }
        
        return JNI_FALSE;
    } catch (...) {
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_happy_pro_hooks_HookManager_nativeProtectAimbotRegions(JNIEnv *env, jobject thiz) {
    try {
        // Security check before protecting Aimbot regions
        if (!BearSecurity::performSecurityCheck()) {
            return JNI_FALSE;
        }
        
        // Protect Aimbot configuration memory
        if (BearMemoryIntegration::g_memoryProtectionInitialized) {
            auto& manager = bearmundo::security::BearMemoryManager::getInstance();
            auto* protection = manager.getProtection();
            if (protection) {
                // Protect options structure (contains aimbot settings)
                void* optionsAddr = &options;
                size_t optionsSize = sizeof(options);
                return protection->protectRegion(optionsAddr, optionsSize) ? JNI_TRUE : JNI_FALSE;
            }
        }
        
        return JNI_FALSE;
    } catch (...) {
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_happy_pro_hooks_HookManager_nativeProtectMemoryRegions(JNIEnv *env, jobject thiz) {
    try {
        // Security check before protecting Memory regions
        if (!BearSecurity::performSecurityCheck()) {
            return JNI_FALSE;
        }
        
        // Protect critical memory hack regions
        if (BearMemoryIntegration::g_memoryProtectionInitialized) {
            return BearMemoryIntegration::protectBearComponents() ? JNI_TRUE : JNI_FALSE;
        }
        
        return JNI_FALSE;
    } catch (...) {
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_happy_pro_hooks_HookManager_nativeEnableStealthMode(JNIEnv *env, jobject thiz) {
    try {
        // Enable comprehensive stealth mode
        BearSecurity::enableStealthMode();
        
        // Enable memory protection stealth mode
        if (BearMemoryIntegration::g_memoryProtectionInitialized) {
            auto& manager = bearmundo::security::BearMemoryManager::getInstance();
            auto* protection = manager.getProtection();
            if (protection) {
                return protection->enableStealthMode() ? JNI_TRUE : JNI_FALSE;
            }
        }
        
        return JNI_TRUE;
    } catch (...) {
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_happy_pro_hooks_HookManager_nativeGetMemoryProtectionStatus(JNIEnv *env, jobject thiz) {
    try {
        std::string status = "🐻 BEAR Memory Protection Status:\n";
        status += "Security Framework: " + std::string(BearSecurity::stealthModeEnabled ? "STEALTH" : "NORMAL") + "\n";
        status += "Anti-Debug: " + std::string(BearSecurity::antiDebugActive ? "ACTIVE" : "INACTIVE") + "\n";
        status += "Memory Protection: " + std::string(BearSecurity::memoryProtectionEnabled ? "ENABLED" : "DISABLED") + "\n";
        
        // Add detailed memory protection status
        std::string memoryStatus = BearMemoryIntegration::getMemoryProtectionStatus();
        status += memoryStatus;
        
        return env->NewStringUTF(status.c_str());
    } catch (...) {
        return env->NewStringUTF("Error retrieving memory protection status");
    }
}

// ============ Additional Security Integration Methods ============

extern "C" JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeInitialize(JNIEnv *env, jobject thiz) {
    try {
        // Use the integrated memory protection system
        return BearMemoryIntegration::initializeMemoryProtection() ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeProtectBearComponents(JNIEnv *env, jobject thiz) {
    try {
        return BearMemoryIntegration::protectBearComponents() ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeEnableAdvancedProtection(JNIEnv *env, jobject thiz) {
    try {
        return BearMemoryIntegration::enableAdvancedProtection() ? JNI_TRUE : JNI_FALSE;
    } catch (...) {
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeProtectRegion(JNIEnv *env, jobject thiz, jlong address, jlong size) {
    try {
        if (BearMemoryIntegration::g_memoryProtectionInitialized) {
            auto& manager = bearmundo::security::BearMemoryManager::getInstance();
            auto* protection = manager.getProtection();
            if (protection) {
                void* addr = reinterpret_cast<void*>(address);
                size_t regionSize = static_cast<size_t>(size);
                return protection->protectRegion(addr, regionSize) ? JNI_TRUE : JNI_FALSE;
            }
        }
        return JNI_FALSE;
    } catch (...) {
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeIsRegionProtected(JNIEnv *env, jobject thiz, jlong address) {
    try {
        if (BearMemoryIntegration::g_memoryProtectionInitialized) {
            auto& manager = bearmundo::security::BearMemoryManager::getInstance();
            auto* protection = manager.getProtection();
            if (protection) {
                void* addr = reinterpret_cast<void*>(address);
                return protection->isRegionProtected(addr) ? JNI_TRUE : JNI_FALSE;
            }
        }
        return JNI_FALSE;
    } catch (...) {
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeGetProtectionStatus(JNIEnv *env, jobject thiz) {
    try {
        std::string status = BearMemoryIntegration::getMemoryProtectionStatus();
        return env->NewStringUTF(status.c_str());
    } catch (...) {
        return env->NewStringUTF("Error retrieving protection status");
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeGetProtectedRegionCount(JNIEnv *env, jobject thiz) {
    try {
        if (BearMemoryIntegration::g_memoryProtectionInitialized) {
            auto& manager = bearmundo::security::BearMemoryManager::getInstance();
            auto* protection = manager.getProtection();
            if (protection) {
                return static_cast<jint>(protection->getProtectedRegionCount());
            }
        }
        return 0;
    } catch (...) {
        return 0;
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeEnableStealthMode(JNIEnv *env, jobject thiz) {
    try {
        return Java_com_happy_pro_hooks_HookManager_nativeEnableStealthMode(env, thiz);
    } catch (...) {
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeEnableAntiDebug(JNIEnv *env, jobject thiz) {
    try {
        BearSecurity::enableAntiDebug();
        return JNI_TRUE;
    } catch (...) {
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_happy_pro_security_BearMemoryProtection_nativeGetLastError(JNIEnv *env, jobject thiz) {
    try {
        if (BearMemoryIntegration::g_memoryProtectionInitialized) {
            auto& manager = bearmundo::security::BearMemoryManager::getInstance();
            auto* protection = manager.getProtection();
            if (protection) {
                std::string error = protection->getLastError();
                return env->NewStringUTF(error.c_str());
            }
        }
        return env->NewStringUTF("Memory protection not initialized");
    } catch (...) {
        return env->NewStringUTF("Error retrieving last error");
    }
}
#endif // duplicate JNI functions disabled

// ============ STEALTH MANAGER JNI INTEGRATION ============
// NOTE: Basic JNI methods removed - now using advanced StealthOperations.cpp implementations
// The advanced implementations provide:
// - Military-grade threat assessment with scoring system
// - Comprehensive environment validation (emulator, debugger, root, anti-cheat detection)
// - Advanced memory protection with region-based security
// - Real-time threat monitoring and adaptive security measures
// - Emergency shutdown with memory cleanup
// - Multi-layered operation safety validation