/**
 * BEAR-LOADER Advanced App Analyzer v3.0
 * Real-Time Intelligence Gathering & Security Analysis System
 *
 * CAPABILITIES:
 * ✅ Security Mechanism Detection (Root, SSL Pinning, Anti-Cheat)
 * ✅ Network Traffic Analysis (HTTP, OkHttp3, Socket monitoring)
 * ✅ Native Library Tracking (dlopen, System.loadLibrary)
 * ✅ File Access Monitoring (Read/Write operations)
 * ✅ Cryptographic Operations (Cipher, MessageDigest, KeyGenerator)
 * ✅ Dynamic Class Analysis (Method hooking, Reflection tracking)
 * ✅ Real-Time Reporting (Periodic intelligence reports)
 *
 * DISCLAIMER:
 * BEAR-LOADER is designed for security researchers, app developers, and educational purposes only.
 * Users must:
 * 1. Only analyze applications they own or have explicit permission to test
 * 2. Respect intellectual property rights and terms of service
 * 3. Use findings responsibly through proper disclosure channels
 * 4. Not use this tool to access unauthorized content or services
 *
 * Misuse of this tool may violate laws including but not limited to the Computer Fraud and Abuse Act,
 * Digital Millennium Copyright Act, and equivalent legislation in other jurisdictions.
 */

console.log("[*] BEAR-LOADER Advanced App Analyzer v3.0 Loaded");

// Enhanced Configuration
const BearConfig = {
    logLevel: "info",  // debug, info, warn, error
    traceNativeCalls: true,
    monitorNetwork: true,
    monitorFileAccess: true,
    monitorCrypto: true,
    generateReports: true,
    reportInterval: 15000, // 15 seconds
    monitorClasses: [
        "security", "crypto", "network", "http", "socket", 
        "ssl", "tls", "cert", "anticheat", "protection",
        "pubg", "tencent", "unity", "il2cpp", "native"
    ]
};

// Enhanced Logging System
const BearLog = {
    d: function(message) {
        if (BearConfig.logLevel === "debug") {
            console.log(`[BEAR-DEBUG] ${message}`);
        }
    },
    i: function(message) {
        if (BearConfig.logLevel === "debug" || BearConfig.logLevel === "info") {
            console.log(`[BEAR-INFO] ${message}`);
        }
    },
    w: function(message) {
        if (BearConfig.logLevel === "debug" || BearConfig.logLevel === "info" || BearConfig.logLevel === "warn") {
            console.log(`[BEAR-WARN] ${message}`);
        }
    },
    e: function(message) {
        console.log(`[BEAR-ERROR] ${message}`);
    },
    highlight: function(message) {
        console.log(`\n[BEAR] ======== ${message} ========\n`);
    },
    security: function(message) {
        console.log(`[BEAR-SECURITY] 🛡️ ${message}`);
    },
    network: function(message) {
        console.log(`[BEAR-NETWORK] 🌐 ${message}`);
    },
    crypto: function(message) {
        console.log(`[BEAR-CRYPTO] 🔐 ${message}`);
    }
};

// Enhanced Analysis State
const BearState = {
    sessionId: Math.random().toString(36).substr(2, 9),
    startTime: new Date().getTime(),
    securityMechanisms: [],
    nativeLibraries: [],
    networkCalls: [],
    fileAccess: [],
    cryptoOperations: [],
    interestingClasses: [],
    hookCount: 0,
    analysisEvents: 0,
    
    // Statistics
    stats: {
        rootDetectionAttempts: 0,
        sslPinningAttempts: 0,
        networkRequests: 0,
        fileOperations: 0,
        cryptoOperations: 0,
        nativeLibrariesLoaded: 0
    }
};

// Main initialization
function bearInitialize() {
    BearLog.highlight("BEAR App Analyzer Starting");
    BearLog.i(`Session ID: ${BearState.sessionId}`);
    BearLog.i(`Target Application Analysis Beginning...`);

    // Get app information first
    if (Java.available) {
        Java.perform(function() {
            try {
                const context = Java.use('android.app.ActivityThread').currentApplication().getApplicationContext();
                const packageName = context.getPackageName();
                const packageManager = context.getPackageManager();
                const packageInfo = packageManager.getPackageInfo(packageName, 0);
                const appVersion = packageInfo.versionName.value;

                BearLog.highlight(`Analyzing Target: ${packageName} v${appVersion}`);
                BearState.targetPackage = packageName;
                BearState.targetVersion = appVersion;
                
                // Start comprehensive analysis
                analyzeJavaEnvironment();
                
            } catch (e) {
                BearLog.e("Failed to get app information: " + e);
            }
        });
    } else {
        BearLog.e("Java VM is not available - limited analysis mode");
    }

    // Hook native functions
    if (BearConfig.traceNativeCalls) {
        analyzeNativeEnvironment();
    }

    // Setup periodic reporting
    if (BearConfig.generateReports) {
        setTimeout(generateIntelligenceReport, BearConfig.reportInterval);
        setInterval(generateIntelligenceReport, BearConfig.reportInterval);
    }

    BearLog.highlight("BEAR App Analyzer Initialized Successfully");
}

// Comprehensive Java Environment Analysis
function analyzeJavaEnvironment() {
    BearLog.i("🔍 Starting comprehensive Java environment analysis...");

    // Enhanced class monitoring
    Java.enumerateLoadedClasses({
        onMatch: function(className) {
            let isInteresting = false;
            let matchedKeyword = "";

            // Check against monitored keywords
            for (const keyword of BearConfig.monitorClasses) {
                if (className.toLowerCase().includes(keyword)) {
                    isInteresting = true;
                    matchedKeyword = keyword;
                    break;
                }
            }

            // Special interest in PUBG/game-specific classes
            if (className.includes("pubg") || 
                className.includes("tencent") || 
                className.includes("unity") ||
                className.includes("il2cpp") ||
                className.includes("anticheat")) {
                isInteresting = true;
                matchedKeyword = "game-specific";
            }

            if (isInteresting) {
                BearLog.i(`🎯 Interesting class found: ${className} (${matchedKeyword})`);
                
                try {
                    analyzeClassDeep(className, matchedKeyword);
                    BearState.analysisEvents++;
                } catch (e) {
                    BearLog.d(`Could not analyze class ${className}: ${e}`);
                }
            }
        },
        onComplete: function() {
            BearLog.i(`✅ Class enumeration completed - ${BearState.interestingClasses.length} classes analyzed`);
        }
    });

    // Enhanced System.loadLibrary monitoring
    const System = Java.use("java.lang.System");
    System.loadLibrary.implementation = function(libraryName) {
        BearLog.highlight(`📚 Native Library Loading: ${libraryName}`);

        // Enhanced library analysis
        const libraryInfo = {
            name: libraryName,
            time: new Date().getTime(),
            sessionTime: new Date().getTime() - BearState.startTime,
            suspicious: false
        };

        // Check for suspicious libraries
        const suspiciousLibs = ["anticheat", "protection", "security", "pubg", "tencent"];
        for (const suspicious of suspiciousLibs) {
            if (libraryName.toLowerCase().includes(suspicious)) {
                libraryInfo.suspicious = true;
                BearLog.security(`Suspicious library detected: ${libraryName}`);
                break;
            }
        }

        BearState.nativeLibraries.push(libraryInfo);
        BearState.stats.nativeLibrariesLoaded++;

        // Call original implementation
        this.loadLibrary(libraryName);

        BearLog.i(`✅ Library ${libraryName} loaded successfully`);
    };

    // Setup specialized monitoring
    if (BearConfig.monitorNetwork) { setupNetworkMonitoring(); }
    if (BearConfig.monitorFileAccess) { setupFileAccessMonitoring(); }
    if (BearConfig.monitorCrypto) { setupCryptoMonitoring(); }
    
    // Security mechanism detection
    detectSecurityMechanisms();
}

// Deep Class Analysis
function analyzeClassDeep(className, category) {
    try {
        const javaClass = Java.use(className);
        const methods = javaClass.class.getDeclaredMethods();

        BearLog.d(`🔬 Deep analysis: ${className} (${methods.length} methods)`);

        // Enhanced class information
        const classInfo = {
            name: className,
            category: category,
            methods: [],
            hookedMethods: [],
            time: new Date().getTime(),
            suspicious: false
        };

        // Analyze each method
        for (let i = 0; i < methods.length; i++) {
            const method = methods[i];
            const methodName = method.getName();

            // Skip common methods
            if (methodName === "$init" || methodName === "toString" || 
                methodName === "hashCode" || methodName === "equals") {
                continue;
            }

            classInfo.methods.push(methodName);

            // Enhanced method analysis
            const isSecurityMethod = methodName.includes("check") ||
                methodName.includes("verify") || methodName.includes("validate") ||
                methodName.includes("encrypt") || methodName.includes("decrypt") ||
                methodName.includes("sign") || methodName.includes("authenticate") ||
                methodName.includes("root") || methodName.includes("detect");

            if (isSecurityMethod) {
                BearLog.security(`Security method detected: ${className}.${methodName}`);
                classInfo.suspicious = true;

                // Hook security-critical methods
                try {
                    if (javaClass[methodName] && javaClass[methodName].overloads.length > 0) {
                        javaClass[methodName].overloads[0].implementation = function() {
                            BearLog.security(`🔍 Security method called: ${className}.${methodName}`);
                            BearState.analysisEvents++;

                            // Log arguments if possible
                            try {
                                BearLog.d(`Arguments: ${Array.prototype.slice.call(arguments)}`);
                            } catch (e) {
                                BearLog.d("Arguments not readable");
                            }

                            // Call original implementation
                            const result = this[methodName].apply(this, arguments);
                            
                            BearLog.security(`🔍 ${className}.${methodName} returned: ${result}`);
                            return result;
                        };

                        classInfo.hookedMethods.push(methodName);
                        BearState.hookCount++;
                        BearLog.d(`✅ Hooked: ${className}.${methodName}`);
                    }
                } catch (e) {
                    BearLog.d(`Failed to hook ${className}.${methodName}: ${e}`);
                }
            }
        }

        BearState.interestingClasses.push(classInfo);
        
    } catch (e) {
        BearLog.e(`Failed to analyze class ${className}: ${e}`);
    }
}

// Enhanced Network Monitoring
function setupNetworkMonitoring() {
    BearLog.i("🌐 Setting up comprehensive network monitoring...");

    try {
        // Enhanced URL monitoring
        const URL = Java.use("java.net.URL");
        URL.openConnection.overload().implementation = function() {
            const url = this.toString();
            BearLog.network(`🌐 URL Connection: ${url}`);

            const networkCall = {
                url: url,
                type: "URL.openConnection",
                time: new Date().getTime(),
                sessionTime: new Date().getTime() - BearState.startTime
            };

            BearState.networkCalls.push(networkCall);
            BearState.stats.networkRequests++;

            return this.openConnection();
        };

        // Enhanced HttpURLConnection monitoring
        const HttpURLConnection = Java.use("java.net.HttpURLConnection");
        HttpURLConnection.connect.implementation = function() {
            const url = this.getURL().toString();
            const method = this.getRequestMethod();
            
            BearLog.network(`🌐 HTTP ${method}: ${url}`);

            const networkCall = {
                url: url,
                method: method,
                type: "HttpURLConnection",
                time: new Date().getTime(),
                sessionTime: new Date().getTime() - BearState.startTime
            };

            BearState.networkCalls.push(networkCall);
            BearState.stats.networkRequests++;

            this.connect();
        };

        // Enhanced OkHttp monitoring
        try {
            const OkHttpClient = Java.use("okhttp3.OkHttpClient");
            OkHttpClient.newCall.overload("okhttp3.Request").implementation = function(request) {
                const url = request.url().toString();
                const method = request.method();
                
                BearLog.network(`🌐 OkHttp ${method}: ${url}`);

                const networkCall = {
                    url: url,
                    method: method,
                    type: "OkHttp",
                    time: new Date().getTime(),
                    sessionTime: new Date().getTime() - BearState.startTime,
                    headers: {}
                };

                BearState.networkCalls.push(networkCall);
                BearState.stats.networkRequests++;

                return this.newCall(request);
            };
        } catch (e) {
            BearLog.d("OkHttp not found or failed to hook");
        }

        BearLog.i("✅ Network monitoring setup complete");
    } catch (e) {
        BearLog.e(`Network monitoring setup failed: ${e}`);
    }
}

// Enhanced File Access Monitoring
function setupFileAccessMonitoring() {
    BearLog.i("📁 Setting up file access monitoring...");

    try {
        const FileInputStream = Java.use("java.io.FileInputStream");
        FileInputStream.$init.overload("java.io.File").implementation = function(file) {
            const path = file.getAbsolutePath();
            BearLog.i(`📖 File read: ${path}`);

            const fileAccess = {
                path: path,
                operation: "read",
                time: new Date().getTime(),
                sessionTime: new Date().getTime() - BearState.startTime,
                suspicious: false
            };

            if (path.includes("/data/data/") || path.includes("/system/") || 
                path.includes("/proc/") || path.includes("su") || path.includes("root")) {
                fileAccess.suspicious = true;
                BearLog.security(`Suspicious file read: ${path}`);
            }

            BearState.fileAccess.push(fileAccess);
            BearState.stats.fileOperations++;

            return this.$init(file);
        };

        BearLog.i("✅ File access monitoring setup complete");
    } catch (e) {
        BearLog.e(`File access monitoring setup failed: ${e}`);
    }
}

// Enhanced Crypto Monitoring
function setupCryptoMonitoring() {
    BearLog.i("🔐 Setting up cryptographic operations monitoring...");

    try {
        const Cipher = Java.use("javax.crypto.Cipher");
        Cipher.getInstance.overload("java.lang.String").implementation = function(transformation) {
            BearLog.crypto(`🔐 Cipher: ${transformation}`);

            const cryptoOp = {
                type: "Cipher",
                algorithm: transformation,
                time: new Date().getTime(),
                sessionTime: new Date().getTime() - BearState.startTime
            };

            BearState.cryptoOperations.push(cryptoOp);
            BearState.stats.cryptoOperations++;

            return this.getInstance(transformation);
        };

        BearLog.i("✅ Crypto monitoring setup complete");
    } catch (e) {
        BearLog.e(`Crypto monitoring setup failed: ${e}`);
    }
}

// Enhanced Security Mechanism Detection
function detectSecurityMechanisms() {
    BearLog.i("🛡️ Starting security mechanism detection...");

    // Enhanced root detection monitoring
    detectRootDetectionMechanisms();
    
    // Enhanced SSL pinning detection
    detectSSLPinningMechanisms();
    
    // Anti-cheat detection
    detectAntiCheatMechanisms();

    BearLog.i("✅ Security mechanism detection setup complete");
}

// Enhanced Root Detection
function detectRootDetectionMechanisms() {
    BearLog.security("🔍 Setting up root detection monitoring...");

    try {
        const File = Java.use("java.io.File");
        const originalExists = File.exists;

        File.exists.implementation = function() {
            const fileName = this.getAbsolutePath();

            const rootFiles = [
                "/system/app/Superuser.apk", "/system/xbin/su", "/system/bin/su",
                "/sbin/su", "/system/su", "/system/bin/.ext/.su", "/system/xbin/daemonsu"
            ];

            for (const rootFile of rootFiles) {
                if (fileName === rootFile) {
                    BearLog.security(`🚨 Root detection attempt via file: ${fileName}`);

                    BearState.securityMechanisms.push({
                        type: "root_detection",
                        method: "file_check",
                        file: fileName,
                        time: new Date().getTime()
                    });

                    BearState.stats.rootDetectionAttempts++;
                    break;
                }
            }

            return originalExists.call(this);
        };

        BearLog.d("✅ File-based root detection monitoring active");
    } catch (e) {
        BearLog.e("Failed to setup file-based root detection: " + e);
    }
}

// Enhanced SSL Pinning Detection  
function detectSSLPinningMechanisms() {
    BearLog.security("🔍 Setting up SSL pinning detection...");

    try {
        const CertificatePinner = Java.use("okhttp3.CertificatePinner");
        const originalCheck = CertificatePinner.check.overload('java.lang.String', 'java.util.List');

        CertificatePinner.check.overload('java.lang.String', 'java.util.List').implementation = function(hostname, peerCertificates) {
            BearLog.security(`🚨 SSL pinning attempt (OkHttp3): ${hostname}`);

            BearState.securityMechanisms.push({
                type: "ssl_pinning",
                method: "OkHttp3.CertificatePinner",
                hostname: hostname,
                time: new Date().getTime()
            });

            BearState.stats.sslPinningAttempts++;

            return originalCheck.call(this, hostname, peerCertificates);
        };

        BearLog.d("✅ OkHttp3 SSL pinning detection active");
    } catch (e) {
        BearLog.d("OkHttp3 not found: " + e);
    }
}

// Anti-Cheat Detection
function detectAntiCheatMechanisms() {
    BearLog.security("🔍 Setting up anti-cheat detection...");

    const antiCheatKeywords = ["anticheat", "protection", "security", "integrity", "validation"];
    
    Java.enumerateLoadedClasses({
        onMatch: function(className) {
            for (const keyword of antiCheatKeywords) {
                if (className.toLowerCase().includes(keyword)) {
                    BearLog.security(`🚨 Anti-cheat mechanism detected: ${className}`);

                    BearState.securityMechanisms.push({
                        type: "anti_cheat",
                        class: className,
                        time: new Date().getTime()
                    });
                    break;
                }
            }
        },
        onComplete: function() {
            BearLog.d("✅ Anti-cheat detection scan complete");
        }
    });
}

// Enhanced Intelligence Report Generation
function generateIntelligenceReport() {
    const currentTime = new Date().getTime();
    const sessionDuration = (currentTime - BearState.startTime) / 1000;

    BearLog.highlight("BEAR Intelligence Report");
    BearLog.i(`📊 Session: ${BearState.sessionId} | Duration: ${sessionDuration.toFixed(1)}s`);
    BearLog.i(`🎯 Target: ${BearState.targetPackage || 'Unknown'} v${BearState.targetVersion || 'Unknown'}`);
    BearLog.i(`📈 Analysis Events: ${BearState.analysisEvents} | Hooks Installed: ${BearState.hookCount}`);

    // Security Mechanisms Summary
    BearLog.i(`🛡️ Security Mechanisms: ${BearState.securityMechanisms.length}`);
    BearLog.i(`   Root Detection: ${BearState.stats.rootDetectionAttempts}`);
    BearLog.i(`   SSL Pinning: ${BearState.stats.sslPinningAttempts}`);

    // Network Analysis Summary
    BearLog.i(`🌐 Network Activity: ${BearState.stats.networkRequests} requests`);
    const uniqueHosts = [...new Set(BearState.networkCalls.map(call => {
        try { return new URL(call.url).hostname; } catch { return call.url; }
    }))];
    BearLog.i(`   Unique Hosts: ${uniqueHosts.length}`);

    if (uniqueHosts.length > 0) {
        BearLog.i(`   Top Destinations:`);
        uniqueHosts.slice(0, 5).forEach(host => {
            BearLog.i(`     - ${host}`);
        });
    }

    // File Operations Summary
    BearLog.i(`📁 File Operations: ${BearState.stats.fileOperations}`);
    
    // Crypto Operations Summary
    BearLog.i(`🔐 Crypto Operations: ${BearState.stats.cryptoOperations}`);
    
    // Native Libraries Summary
    BearLog.i(`📚 Native Libraries: ${BearState.stats.nativeLibrariesLoaded}`);
    
    // Interesting Classes Summary
    BearLog.i(`🔬 Analyzed Classes: ${BearState.interestingClasses.length}`);
    const hookedClasses = BearState.interestingClasses.filter(cls => cls.hookedMethods.length > 0);
    BearLog.i(`   With Hooks: ${hookedClasses.length}`);

    BearLog.highlight("End Intelligence Report");
}

// Start the analyzer
bearInitialize();

console.log("[BEAR] Advanced App Analyzer v3.0 Initialized Successfully");