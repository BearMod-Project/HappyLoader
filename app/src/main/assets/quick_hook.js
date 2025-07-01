/**
 * BEAR-LOADER Quick Hook v3.0
 * Rapid Testing & Debugging Script for HappyLoader
 * 
 * This script provides essential hooks for quick testing and debugging
 * of BEAR-LOADER ESP functionality and socket connections.
 * 
 * TARGETS:
 * ✅ com.happy.pro package structure
 * ✅ MainActivity, LoginActivity hooks
 * ✅ libclient.so native library monitoring
 * ✅ ESP overlay service tracking
 * ✅ Socket connection debugging
 * ✅ Floating service monitoring
 * 
 * @author BEAR-LOADER Team
 * @version 3.0.0
 */

console.log("[*] 🐻 BEAR-LOADER Quick Hook v3.0 Loaded");

var BearQuickHook = {
    sessionId: Math.random().toString(36).substr(2, 9),
    startTime: new Date().getTime(),
    nativeLibraryLoaded: false,
    espServiceStarted: false,
    socketConnected: false,
    hookCount: 0,
    
    log: function(level, message) {
        var timestamp = new Date().getTime() - this.startTime;
        console.log(`[BEAR-${level}] [+${timestamp}ms] ${message}`);
    },
    
    info: function(message) { this.log("INFO", message); },
    warn: function(message) { this.log("WARN", message); },
    error: function(message) { this.log("ERROR", message); },
    success: function(message) { this.log("SUCCESS", "✅ " + message); },
    
    init: function() {
        this.info(`🚀 BEAR Quick Hook Session: ${this.sessionId}`);
        this.info("🎯 Target: BEAR-LOADER HappyLoader");
        this.setupJavaHooks();
    }
};

// Hook Java components
Java.perform(function() {
    BearQuickHook.info("☕ Java.perform() called - setting up hooks");
    
    BearQuickHook.setupJavaHooks = function() {
        // Hook MainActivity
        try {
            var MainActivity = Java.use("com.happy.pro.activity.MainActivity");
            
            MainActivity.onCreate.implementation = function(savedInstanceState) {
                BearQuickHook.success("MainActivity.onCreate() called");
                
                // Call original implementation
                this.onCreate(savedInstanceState);
                
                BearQuickHook.info("MainActivity.onCreate() completed successfully");
            };
            
            BearQuickHook.hookCount++;
            BearQuickHook.success("MainActivity.onCreate() hooked");
        } catch (e) {
            BearQuickHook.warn("MainActivity not found or failed to hook: " + e);
        }
        
        // Hook LoginActivity
        try {
            var LoginActivity = Java.use("com.happy.pro.activity.LoginActivity");
            
            LoginActivity.onCreate.implementation = function(savedInstanceState) {
                BearQuickHook.success("LoginActivity.onCreate() called");
                
                // Call original implementation
                this.onCreate(savedInstanceState);
                
                BearQuickHook.info("LoginActivity.onCreate() completed successfully");
            };
            
            BearQuickHook.hookCount++;
            BearQuickHook.success("LoginActivity.onCreate() hooked");
        } catch (e) {
            BearQuickHook.warn("LoginActivity not found or failed to hook: " + e);
        }
        
        // Hook ESP Overlay Services
        BearQuickHook.hookESPServices();
        
        // Hook Floating Services
        BearQuickHook.hookFloatingServices();
        
        // Hook Native Library Loading
        BearQuickHook.hookNativeLibrary();
        
        // Hook Socket Operations
        BearQuickHook.hookSocketOperations();
        
        BearQuickHook.success(`Quick Hook setup complete - ${BearQuickHook.hookCount} hooks installed`);
    };
    
    // Hook ESP-related services
    BearQuickHook.hookESPServices = function() {
        // Hook Overlay service
        try {
            var Overlay = Java.use("com.happy.pro.floating.Overlay");
            
            Overlay.onCreate.implementation = function() {
                BearQuickHook.success("🎯 ESP Overlay.onCreate() called");
                BearQuickHook.espServiceStarted = true;
                
                // Call original implementation
                this.onCreate();
                
                BearQuickHook.info("ESP Overlay service started successfully");
            };
            
            Overlay.onDestroy.implementation = function() {
                BearQuickHook.warn("🛑 ESP Overlay.onDestroy() called");
                BearQuickHook.espServiceStarted = false;
                
                // Call original implementation
                this.onDestroy();
                
                BearQuickHook.info("ESP Overlay service destroyed");
            };
            
            BearQuickHook.hookCount += 2;
            BearQuickHook.success("ESP Overlay service hooked");
        } catch (e) {
            BearQuickHook.warn("ESP Overlay service not found: " + e);
        }
        
        // Hook Standalone ESP Overlay
        try {
            var StandaloneESP = Java.use("com.happy.pro.floating.StandaloneESPOverlay");
            
            StandaloneESP.onCreate.implementation = function() {
                BearQuickHook.success("🎯 StandaloneESPOverlay.onCreate() called");
                
                // Call original implementation
                this.onCreate();
                
                BearQuickHook.info("Standalone ESP Overlay started successfully");
            };
            
            BearQuickHook.hookCount++;
            BearQuickHook.success("Standalone ESP Overlay hooked");
        } catch (e) {
            BearQuickHook.warn("Standalone ESP Overlay not found: " + e);
        }
        
        // Hook ESPView
        try {
            var ESPView = Java.use("com.happy.pro.floating.ESPView");
            
            // Hook constructor
            ESPView.$init.overload("android.content.Context").implementation = function(context) {
                BearQuickHook.success("🎨 ESPView constructor called");
                
                // Call original implementation
                this.$init(context);
                
                BearQuickHook.info("ESPView initialized successfully");
            };
            
            BearQuickHook.hookCount++;
            BearQuickHook.success("ESPView constructor hooked");
        } catch (e) {
            BearQuickHook.warn("ESPView not found: " + e);
        }
    };
    
    // Hook floating services
    BearQuickHook.hookFloatingServices = function() {
        // Hook FloatRei (main floating menu)
        try {
            var FloatRei = Java.use("com.happy.pro.floating.FloatRei");
            
            FloatRei.onCreate.implementation = function() {
                BearQuickHook.success("🎈 FloatRei.onCreate() called");
                
                // Call original implementation
                this.onCreate();
                
                BearQuickHook.info("FloatRei floating menu started");
            };
            
            BearQuickHook.hookCount++;
            BearQuickHook.success("FloatRei floating menu hooked");
        } catch (e) {
            BearQuickHook.warn("FloatRei not found: " + e);
        }
        
        // Hook FloatService
        try {
            var FloatService = Java.use("com.happy.pro.floating.FloatService");
            
            FloatService.onCreate.implementation = function() {
                BearQuickHook.success("🎈 FloatService.onCreate() called");
                
                // Call original implementation
                this.onCreate();
                
                BearQuickHook.info("FloatService started successfully");
            };
            
            BearQuickHook.hookCount++;
            BearQuickHook.success("FloatService hooked");
        } catch (e) {
            BearQuickHook.warn("FloatService not found: " + e);
        }
    };
    
    // Hook native library loading
    BearQuickHook.hookNativeLibrary = function() {
        var System = Java.use("java.lang.System");
        System.loadLibrary.implementation = function(libraryName) {
            BearQuickHook.info(`📚 Loading native library: ${libraryName}`);
            
            // Track specific libraries
            if (libraryName === "client") {
                BearQuickHook.success("🎯 TARGET LIBRARY: libclient.so loading...");
                BearQuickHook.nativeLibraryLoaded = true;
                
                // Call original implementation
                this.loadLibrary(libraryName);
                
                BearQuickHook.success("libclient.so loaded successfully");
                
                // Hook native functions after library loads
                setTimeout(function() {
                    BearQuickHook.hookNativeFunctions();
                }, 1000);
                
                return;
            }
            
            // Call original implementation for other libraries
            this.loadLibrary(libraryName);
            
            BearQuickHook.info(`Library ${libraryName} loaded successfully`);
        };
        
        BearQuickHook.hookCount++;
        BearQuickHook.success("System.loadLibrary() hooked");
    };
    
    // Hook socket-related operations
    BearQuickHook.hookSocketOperations = function() {
        // Hook MainService (socket server)
        try {
            var MainService = Java.use("com.happy.pro.Component.MainService");
            
            MainService.onCreate.implementation = function() {
                BearQuickHook.success("🔌 MainService.onCreate() called");
                
                // Call original implementation
                this.onCreate();
                
                BearQuickHook.info("MainService socket server started");
            };
            
            BearQuickHook.hookCount++;
            BearQuickHook.success("MainService socket operations hooked");
        } catch (e) {
            BearQuickHook.warn("MainService not found: " + e);
        }
        
        // Hook BearMemoryProtection
        try {
            var BearMemoryProtection = Java.use("com.happy.pro.security.BearMemoryProtection");
            
            // Hook getInstance
            BearMemoryProtection.getInstance.implementation = function() {
                BearQuickHook.success("🛡️ BearMemoryProtection.getInstance() called");
                
                var instance = this.getInstance();
                BearQuickHook.info("BearMemoryProtection instance obtained");
                return instance;
            };
            
            BearQuickHook.hookCount++;
            BearQuickHook.success("BearMemoryProtection hooked");
        } catch (e) {
            BearQuickHook.warn("BearMemoryProtection not found: " + e);
        }
    };
    
    BearQuickHook.init();
});

// Hook native functions after libclient.so loads
BearQuickHook.hookNativeFunctions = function() {
    BearQuickHook.info("🔍 Searching for native functions in libclient.so");
    
    // Find the libclient.so module
    var clientModule = Process.findModuleByName("libclient.so");
    if (!clientModule) {
        BearQuickHook.error("❌ libclient.so module not found!");
        return;
    }
    
    BearQuickHook.success(`Found libclient.so at base address: ${clientModule.base}`);
    
    // Enumerate exports
    var exports = clientModule.enumerateExports();
    BearQuickHook.info(`Found ${exports.length} exported functions`);
    
    var hookedFunctions = 0;
    
    // Hook important JNI functions
    exports.forEach(function(exp) {
        if (exp.type === 'function') {
            // Hook ESP-related functions
            if (exp.name.includes("DrawOn") || 
                exp.name.includes("getReady") ||
                exp.name.includes("ESP") ||
                exp.name.includes("Overlay")) {
                
                BearQuickHook.success(`🎯 Hooking ESP function: ${exp.name}`);
                
                Interceptor.attach(exp.address, {
                    onEnter: function(args) {
                        BearQuickHook.info(`📞 Called ${exp.name}`);
                        
                        if (exp.name.includes("getReady")) {
                            BearQuickHook.info("🔌 Socket getReady() called - checking connection");
                        }
                        
                        if (exp.name.includes("DrawOn")) {
                            BearQuickHook.info("🎨 ESP DrawOn() called - rendering frame");
                        }
                    },
                    onLeave: function(retval) {
                        if (exp.name.includes("getReady")) {
                            var result = retval.toInt32();
                            if (result === 1) {
                                BearQuickHook.success("✅ getReady() returned TRUE - socket ready");
                                BearQuickHook.socketConnected = true;
                            } else {
                                BearQuickHook.error("❌ getReady() returned FALSE - socket failed");
                                BearQuickHook.socketConnected = false;
                            }
                        }
                        
                        BearQuickHook.info(`📞 ${exp.name} returned: ${retval}`);
                    }
                });
                
                hookedFunctions++;
            }
            
            // Hook socket-related functions
            else if (exp.name.includes("Create") ||
                     exp.name.includes("Bind") ||
                     exp.name.includes("Listen") ||
                     exp.name.includes("Accept") ||
                     exp.name.includes("Socket")) {
                
                BearQuickHook.success(`🔌 Hooking socket function: ${exp.name}`);
                
                Interceptor.attach(exp.address, {
                    onEnter: function(args) {
                        BearQuickHook.info(`🔌 Socket operation: ${exp.name}`);
                    },
                    onLeave: function(retval) {
                        var result = retval.toInt32();
                        if (result === 1 || result === 0) {
                            BearQuickHook.info(`🔌 ${exp.name} result: ${result === 1 ? "SUCCESS" : "FAILED"}`);
                        }
                    }
                });
                
                hookedFunctions++;
            }
            
            // Hook BEAR security functions
            else if (exp.name.includes("Bear") ||
                     exp.name.includes("Security") ||
                     exp.name.includes("Memory") ||
                     exp.name.includes("Protection")) {
                
                BearQuickHook.success(`🛡️ Hooking security function: ${exp.name}`);
                
                Interceptor.attach(exp.address, {
                    onEnter: function(args) {
                        BearQuickHook.info(`🛡️ Security function called: ${exp.name}`);
                    },
                    onLeave: function(retval) {
                        BearQuickHook.info(`🛡️ ${exp.name} completed`);
                    }
                });
                
                hookedFunctions++;
            }
        }
    });
    
    BearQuickHook.success(`Native function hooking complete - ${hookedFunctions} functions hooked`);
    
    // Set up periodic status reporting
    setInterval(function() {
        BearQuickHook.printStatus();
    }, 10000); // Every 10 seconds
};

// Print current status
BearQuickHook.printStatus = function() {
    var uptime = new Date().getTime() - this.startTime;
    
    console.log("\n🐻 ===== BEAR-LOADER STATUS REPORT =====");
    console.log(`📊 Session: ${this.sessionId} | Uptime: ${Math.floor(uptime/1000)}s`);
    console.log(`🎯 Hooks Installed: ${this.hookCount}`);
    console.log(`📚 Native Library: ${this.nativeLibraryLoaded ? "✅ LOADED" : "❌ NOT LOADED"}`);
    console.log(`🎨 ESP Service: ${this.espServiceStarted ? "✅ ACTIVE" : "❌ INACTIVE"}`);
    console.log(`🔌 Socket Connection: ${this.socketConnected ? "✅ CONNECTED" : "❌ DISCONNECTED"}`);
    console.log("🐻 =====================================\n");
};

console.log("[*] 🐻 BEAR-LOADER Quick Hook v3.0 Initialized");
console.log("[*] 🎯 Ready for rapid testing and debugging");
console.log("[*] 📊 Use BearQuickHook.printStatus() for current status");