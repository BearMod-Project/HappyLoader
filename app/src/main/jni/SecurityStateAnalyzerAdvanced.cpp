#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <map>
#include <memory>
#include <mutex>
#include <thread>
#include <chrono>
#include <sys/ptrace.h>
#include <sys/syscall.h>
#include <sys/mman.h>
#include <sys/prctl.h>
#include <linux/prctl.h>
#include <linux/seccomp.h>
#include <linux/filter.h>
#include <linux/audit.h>
#include <linux/signal.h>
#include <linux/elf.h>
#include <linux/version.h>
#include <unistd.h>
#include <fcntl.h>
#include <dlfcn.h>
#include <link.h>
#include <sys/system_properties.h>
#include <sys/mount.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <dirent.h>
#include <fstream>
#include <sstream>
#include <regex>
#include <json/json.h>

// Integration with existing BEAR-LOADER security
#include "BearMundoSecurity.h"
#include "MemoryManager.h"

#define LOG_TAG "SecurityStateAnalyzerAdvanced"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

using namespace std;

class SecurityStateAnalyzerAdvanced {
private:
    static SecurityStateAnalyzerAdvanced* instance;
    static mutex instanceMutex;
    bool isAnalyzing;
    thread analyzerThread;
    mutex stateMutex;
    map<string, bool> securityState;
    vector<string> suspiciousPatterns;
    vector<string> protectedRegions;
    int seccompFilter;
    
    // Enhanced statistics
    uint64_t analysisCount;
    uint64_t threatsDetected;
    uint64_t protectedRegionsCount;

    SecurityStateAnalyzerAdvanced() : isAnalyzing(false), seccompFilter(-1), 
                                     analysisCount(0), threatsDetected(0), protectedRegionsCount(0) {
        LOGI("🔍 Advanced SecurityStateAnalyzer initializing...");
        initializePatterns();
        setupSeccompFilter();
        integrateBearSecurity();
        LOGI("✅ Advanced SecurityStateAnalyzer ready");
    }

    void initializePatterns() {
        LOGI("🔧 Initializing advanced security patterns...");
        
        // Memory access patterns
        suspiciousPatterns.push_back("ptrace");
        suspiciousPatterns.push_back("inject");
        suspiciousPatterns.push_back("hook");
        suspiciousPatterns.push_back("debug");
        suspiciousPatterns.push_back("trace");
        suspiciousPatterns.push_back("frida");
        suspiciousPatterns.push_back("xposed");
        suspiciousPatterns.push_back("substrate");
        suspiciousPatterns.push_back("magisk");
        suspiciousPatterns.push_back("supersu");
        suspiciousPatterns.push_back("riru");
        suspiciousPatterns.push_back("lsposed");
        suspiciousPatterns.push_back("edxposed");
        suspiciousPatterns.push_back("gameguardian");
        suspiciousPatterns.push_back("cheat_engine");
        suspiciousPatterns.push_back("artmoney");
        suspiciousPatterns.push_back("speedhack");
        suspiciousPatterns.push_back("memoryanalyzer");
        suspiciousPatterns.push_back("objectionalysis");
        
        // Initialize security state
        securityState["system_secure"] = true;
        securityState["memory_protected"] = false;
        securityState["bear_integration_active"] = false;
        
        LOGI("✅ Advanced patterns initialized (%zu patterns)", suspiciousPatterns.size());
    }

    void setupSeccompFilter() {
        LOGI("🛡️ Setting up advanced seccomp filter...");
        
        try {
            // Enhanced seccomp filter with more comprehensive syscall monitoring
            struct sock_filter filter[] = {
                // Load syscall number
                BPF_STMT(BPF_LD | BPF_W | BPF_ABS, offsetof(struct seccomp_data, nr)),
                
                // Check for ptrace - critical for debugging detection
                BPF_JUMP(BPF_JMP | BPF_JEQ | BPF_K, SYS_ptrace, 0, 1),
                BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_TRACE),
                
                // Check for process_vm_readv/writev - memory manipulation
                BPF_JUMP(BPF_JMP | BPF_JEQ | BPF_K, SYS_process_vm_readv, 0, 1),
                BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_TRACE),
                BPF_JUMP(BPF_JMP | BPF_JEQ | BPF_K, SYS_process_vm_writev, 0, 1),
                BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_TRACE),
                
                // Check for mprotect - memory protection changes
                BPF_JUMP(BPF_JMP | BPF_JEQ | BPF_K, SYS_mprotect, 0, 1),
                BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_TRACE),
                
                // Check for mmap/munmap - memory mapping
                BPF_JUMP(BPF_JMP | BPF_JEQ | BPF_K, SYS_mmap, 0, 1),
                BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_TRACE),
                
                // Check for dlopen/dlsym - dynamic library loading
                BPF_JUMP(BPF_JMP | BPF_JEQ | BPF_K, SYS_openat, 0, 1),
                BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_TRACE),
                
                // Default allow
                BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_ALLOW)
            };

            struct sock_fprog prog = {
                .len = (unsigned short)(sizeof(filter) / sizeof(filter[0])),
                .filter = filter
            };

            // Set NO_NEW_PRIVS first
            if (prctl(PR_SET_NO_NEW_PRIVS, 1, 0, 0, 0) == 0) {
                seccompFilter = prctl(PR_SET_SECCOMP, SECCOMP_MODE_FILTER, &prog);
                if (seccompFilter >= 0) {
                    LOGI("✅ Advanced seccomp filter applied successfully");
                } else {
                    LOGW("⚠️ Failed to set up advanced seccomp filter: %d", seccompFilter);
                }
            } else {
                LOGE("❌ Failed to set NO_NEW_PRIVS for seccomp");
            }
        } catch (...) {
            LOGE("❌ Exception during advanced seccomp filter setup");
        }
    }

    void integrateBearSecurity() {
        LOGI("🔗 Integrating with BEAR-LOADER security systems...");
        
        try {
            // Check if BEAR security is active
            if (BearMundo::g_BearMundoActive) {
                securityState["bear_integration_active"] = true;
                LOGI("🛡️ BEAR security integration active");
                
                // Enable stealth mode if threats detected
                BearMundo::DetectionThreat threat = BearMundo::validateSecurityEnvironment();
                if (threat >= BearMundo::DetectionThreat::HIGH) {
                    BearMundo::enableStealthMode();
                    LOGW("⚠️ High threat - enabling stealth mode");
                }
            }
            
            // Initialize Memory Manager integration
            if (MemoryManager::initialize()) {
                securityState["memory_manager_active"] = true;
                MemoryManager::enableStealthMode(true);
                LOGI("🧠 Memory Manager integration active");
            }
            
        } catch (...) {
            LOGE("❌ BEAR security integration failed");
        }
    }

    void analyzeMemoryRegions() {
        try {
            ifstream maps("/proc/self/maps");
            string line;
            regex protectedRegex("(frida|xposed|substrate|magisk|supersu|riru|lsposed|gameguardian)");
            
            int regionsProtected = 0;
            
            while (getline(maps, line)) {
                if (regex_search(line, protectedRegex)) {
                    stringstream ss(line);
                    string addressRange, perms;
                    ss >> addressRange >> perms;
                    
                    // Parse address range
                    size_t dashPos = addressRange.find('-');
                    if (dashPos != string::npos) {
                        string startHex = addressRange.substr(0, dashPos);
                        string endHex = addressRange.substr(dashPos + 1);
                        
                        try {
                            // Convert hex addresses to long
                            uintptr_t startAddr = stoull(startHex, nullptr, 16);
                            uintptr_t endAddr = stoull(endHex, nullptr, 16);
                            size_t regionSize = endAddr - startAddr;
                            
                            // Mark region as protected
                            protectedRegions.push_back(addressRange);
                            
                            // Apply memory protection using BEAR Memory Manager
                            if (MemoryManager::isInitialized()) {
                                int origProt = MemoryManager::getMemoryProtection(startAddr);
                                if (origProt != -1) {
                                    // Make region non-executable if it contains suspicious content
                                    MemoryManager::changeProtection(startAddr, regionSize, PROT_READ | PROT_WRITE);
                                    regionsProtected++;
                                    
                                    LOGD("🔒 Protected suspicious region: %s", addressRange.c_str());
                                }
                            }
                            
                            securityState["memory_tampered"] = true;
                            threatsDetected++;
                            
                        } catch (const exception& e) {
                            LOGW("⚠️ Failed to parse address range: %s", e.what());
                        }
                    }
                }
            }
            
            protectedRegionsCount = regionsProtected;
            securityState["memory_protected"] = (regionsProtected > 0);
            
            if (regionsProtected > 0) {
                LOGI("🛡️ Protected %d suspicious memory regions", regionsProtected);
            }
            
        } catch (const exception& e) {
            LOGE("❌ Memory region analysis failed: %s", e.what());
        }
    }

    void checkForHooks() {
        try {
            bool hooksDetected = false;
            
            // Check critical system functions for hooks
            vector<string> criticalFunctions = {
                "ptrace", "dlopen", "dlsym", "mmap", "mprotect",
                "open", "read", "write", "ioctl"
            };
            
            for (const string& funcName : criticalFunctions) {
                Dl_info info;
                void* handle = dlopen("libc.so", RTLD_NOW);
                if (handle) {
                    void* symbol = dlsym(handle, funcName.c_str());
                    if (symbol && dladdr(symbol, &info)) {
                        // Check if symbol is in expected location
                        if (info.dli_fbase != handle) {
                            hooksDetected = true;
                            LOGD("🎣 Hook detected in function: %s", funcName.c_str());
                            break;
                        }
                    }
                    dlclose(handle);
                }
            }
            
            securityState["hook_detected"] = hooksDetected;
            if (hooksDetected) {
                threatsDetected++;
                LOGW("⚠️ Function hooks detected!");
            }
            
        } catch (...) {
            LOGE("❌ Hook detection failed");
        }
    }

    void checkForDebuggers() {
        try {
            bool debuggerDetected = false;
            
            // Method 1: TracerPid check
            ifstream status("/proc/self/status");
            string line;
            while (getline(status, line)) {
                if (line.find("TracerPid:") == 0) {
                    string pidStr = line.substr(11);
                    int tracerPid = stoi(pidStr);
                    if (tracerPid != 0) {
                        debuggerDetected = true;
                        LOGD("🔍 Debugger TracerPid: %d", tracerPid);
                        break;
                    }
                }
            }
            
            // Method 2: ptrace self-attach test
            if (!debuggerDetected) {
                if (ptrace(PTRACE_TRACEME, 0, 0, 0) == -1) {
                    debuggerDetected = true;
                    LOGD("🔍 ptrace self-attach failed - debugger present");
                } else {
                    ptrace(PTRACE_DETACH, 0, 0, 0);
                }
            }
            
            securityState["debugger_detected"] = debuggerDetected;
            if (debuggerDetected) {
                threatsDetected++;
                LOGW("⚠️ Debugger detected!");
                
                // Activate BEAR emergency protocol
                if (BearMundo::g_BearMundoActive) {
                    BearMundo::enableStealthMode();
                }
            }
            
        } catch (...) {
            LOGE("❌ Debugger detection failed");
        }
    }

    void checkForEmulators() {
        try {
            bool emulatorDetected = false;
            
            // Check system properties
            vector<pair<string, string>> emulatorProps = {
                {"ro.kernel.qemu", "1"},
                {"ro.hardware", "goldfish"},
                {"ro.hardware", "ranchu"},
                {"ro.product.device", "generic"},
                {"ro.build.product", "sdk"},
                {"ro.build.fingerprint", "generic"}
            };
            
            char prop[PROP_VALUE_MAX];
            for (const auto& propPair : emulatorProps) {
                if (__system_property_get(propPair.first.c_str(), prop) > 0) {
                    string propValue(prop);
                    if (propValue.find(propPair.second) != string::npos) {
                        emulatorDetected = true;
                        LOGD("🔍 Emulator property: %s=%s", propPair.first.c_str(), prop);
                        break;
                    }
                }
            }
            
            // Check for emulator files
            if (!emulatorDetected) {
                vector<string> emulatorFiles = {
                    "/system/lib/libc_malloc_debug_qemu.so",
                    "/sys/qemu_trace",
                    "/dev/socket/qemud",
                    "/dev/qemu_pipe"
                };
                
                for (const string& file : emulatorFiles) {
                    if (access(file.c_str(), F_OK) == 0) {
                        emulatorDetected = true;
                        LOGD("🔍 Emulator file found: %s", file.c_str());
                        break;
                    }
                }
            }
            
            securityState["emulator_detected"] = emulatorDetected;
            if (emulatorDetected) {
                threatsDetected++;
                LOGW("⚠️ Emulator environment detected!");
            }
            
        } catch (...) {
            LOGE("❌ Emulator detection failed");
        }
    }

    void checkForRoot() {
        try {
            bool rootDetected = false;
            
            const vector<string> rootPaths = {
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su",
                "/system/app/SuperSU.apk",
                "/system/app/Magisk.apk",
                "/sbin/.magisk",
                "/cache/.disable_magisk"
            };

            for (const string& path : rootPaths) {
                if (access(path.c_str(), F_OK) == 0) {
                    rootDetected = true;
                    LOGD("🔍 Root binary found: %s", path.c_str());
                    break;
                }
            }
            
            // Check for root properties
            if (!rootDetected) {
                char prop[PROP_VALUE_MAX];
                if (__system_property_get("ro.debuggable", prop) > 0 && string(prop) == "1") {
                    rootDetected = true;
                    LOGD("🔍 Root property: ro.debuggable=1");
                }
            }
            
            securityState["root_detected"] = rootDetected;
            if (rootDetected) {
                threatsDetected++;
                LOGW("⚠️ Root access detected!");
            }
            
        } catch (...) {
            LOGE("❌ Root detection failed");
        }
    }

    void analyzeSecurityState() {
        LOGI("🔍 Security analysis thread started");
        
        while (isAnalyzing) {
            try {
                lock_guard<mutex> lock(stateMutex);
                
                // Reset threat counter for this analysis
                threatsDetected = 0;
                
                // Run comprehensive security checks
                checkForHooks();
                checkForDebuggers();
                checkForEmulators();
                checkForRoot();
                analyzeMemoryRegions();
                
                // Update analysis statistics
                analysisCount++;
                
                // Calculate overall security level
                securityState["system_secure"] = (threatsDetected == 0);
                securityState["threat_level"] = (threatsDetected >= 3) ? "CRITICAL" : 
                                               (threatsDetected >= 2) ? "HIGH" :
                                               (threatsDetected >= 1) ? "MEDIUM" : "LOW";
                
                // Log security summary
                if (analysisCount % 50 == 0) { // Every 5 seconds
                    LOGI("📊 Analysis #%llu: %llu threats, %llu protected regions", 
                         analysisCount, threatsDetected, protectedRegionsCount);
                }
                
            } catch (const exception& e) {
                LOGE("❌ Security analysis iteration failed: %s", e.what());
            }
            
            // Sleep to prevent high CPU usage (optimized timing)
            this_thread::sleep_for(chrono::milliseconds(100));
        }
        
        LOGI("🔍 Security analysis thread stopped");
    }

public:
    static SecurityStateAnalyzerAdvanced* getInstance() {
        lock_guard<mutex> lock(instanceMutex);
        if (!instance) {
            instance = new SecurityStateAnalyzerAdvanced();
        }
        return instance;
    }

    void startAnalysis() {
        if (isAnalyzing) {
            LOGW("⚠️ Advanced security analysis already running");
            return;
        }
        
        LOGI("🚀 Starting advanced security state analysis...");
        isAnalyzing = true;
        analyzerThread = thread(&SecurityStateAnalyzerAdvanced::analyzeSecurityState, this);
        LOGI("✅ Advanced security analysis started");
    }

    void stopAnalysis() {
        if (!isAnalyzing) {
            return;
        }
        
        LOGI("🛑 Stopping advanced security state analysis...");
        isAnalyzing = false;
        if (analyzerThread.joinable()) {
            analyzerThread.join();
        }
        LOGI("✅ Advanced security analysis stopped");
    }

    Json::Value getSecurityState() {
        lock_guard<mutex> lock(stateMutex);
        Json::Value state;
        
        // Security state
        for (const auto& pair : securityState) {
            state["security"][pair.first] = pair.second;
        }
        
        // Statistics
        state["statistics"]["analysis_count"] = static_cast<Json::UInt64>(analysisCount);
        state["statistics"]["threats_detected"] = static_cast<Json::UInt64>(threatsDetected);
        state["statistics"]["protected_regions"] = static_cast<Json::UInt64>(protectedRegionsCount);
        state["statistics"]["suspicious_patterns"] = static_cast<Json::UInt>(suspiciousPatterns.size());
        
        // System info
        state["system"]["analyzer_active"] = isAnalyzing;
        state["system"]["seccomp_filter"] = (seccompFilter >= 0);
        state["system"]["bear_integration"] = securityState["bear_integration_active"];
        
        return state;
    }
    
    // Get simplified status for quick checks
    bool isSystemSecure() {
        lock_guard<mutex> lock(stateMutex);
        return securityState["system_secure"];
    }
    
    uint64_t getThreatCount() {
        lock_guard<mutex> lock(stateMutex);
        return threatsDetected;
    }

    ~SecurityStateAnalyzerAdvanced() {
        stopAnalysis();
        if (seccompFilter >= 0) {
            // Note: Cannot disable seccomp once enabled for security reasons
            LOGI("🔒 Seccomp filter remains active for security");
        }
        LOGI("🔍 Advanced SecurityStateAnalyzer destroyed");
    }
};

// Static member definitions
SecurityStateAnalyzerAdvanced* SecurityStateAnalyzerAdvanced::instance = nullptr;
mutex SecurityStateAnalyzerAdvanced::instanceMutex;

// ========================================
// JNI BINDINGS FOR JAVA INTEGRATION
// ========================================

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_bearmod_security_ai_SecurityAnalyzer_nativeGetSecurityState(JNIEnv* env, jclass clazz) {
    try {
        auto analyzer = SecurityStateAnalyzerAdvanced::getInstance();
        Json::Value state = analyzer->getSecurityState();
        
        Json::FastWriter writer;
        string jsonStr = writer.write(state);
        
        return env->NewStringUTF(jsonStr.c_str());
    } catch (const exception& e) {
        LOGE("❌ Failed to get security state: %s", e.what());
        return env->NewStringUTF("{\"error\":\"Failed to get security state\"}");
    }
}

JNIEXPORT void JNICALL
Java_com_bearmod_security_ai_SecurityAnalyzer_nativeStartAnalysis(JNIEnv* env, jclass clazz) {
    try {
        SecurityStateAnalyzerAdvanced::getInstance()->startAnalysis();
    } catch (const exception& e) {
        LOGE("❌ Failed to start analysis: %s", e.what());
    }
}

JNIEXPORT void JNICALL
Java_com_bearmod_security_ai_SecurityAnalyzer_nativeStopAnalysis(JNIEnv* env, jclass clazz) {
    try {
        SecurityStateAnalyzerAdvanced::getInstance()->stopAnalysis();
    } catch (const exception& e) {
        LOGE("❌ Failed to stop analysis: %s", e.what());
    }
}

JNIEXPORT jboolean JNICALL
Java_com_bearmod_security_ai_SecurityAnalyzer_nativeIsSystemSecure(JNIEnv* env, jclass clazz) {
    try {
        bool secure = SecurityStateAnalyzerAdvanced::getInstance()->isSystemSecure();
        return secure ? JNI_TRUE : JNI_FALSE;
    } catch (const exception& e) {
        LOGE("❌ Failed to check system security: %s", e.what());
        return JNI_FALSE;
    }
}

JNIEXPORT jlong JNICALL
Java_com_bearmod_security_ai_SecurityAnalyzer_nativeGetThreatCount(JNIEnv* env, jclass clazz) {
    try {
        return static_cast<jlong>(SecurityStateAnalyzerAdvanced::getInstance()->getThreatCount());
    } catch (const exception& e) {
        LOGE("❌ Failed to get threat count: %s", e.what());
        return -1;
    }
}

} // extern "C" 