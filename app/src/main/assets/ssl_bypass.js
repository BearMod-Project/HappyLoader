/**
 * BEAR-LOADER SSL Pinning Bypass Module v3.0
 * Comprehensive Certificate Validation Bypass System
 *
 * FEATURES:
 * ✅ OkHttp3 Certificate Pinning Bypass
 * ✅ TrustManagerImpl Verification Bypass  
 * ✅ X509TrustManager Custom Trust Implementation
 * ✅ Appcelerator Titanium Pinning Bypass
 * ✅ TrustKit Hostname Verification Bypass
 * ✅ WebViewClient SSL Error Bypass
 * ✅ Cordova Certificate Verification Bypass
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

console.log("[*] BEAR-LOADER SSL Pinning Bypass Module v3.0 Loaded");

var BearSSLBypass = {
    isActive: false,
    bypassCount: 0,
    pinnedHosts: [],
    bypassedConnections: 0,
    
    init: function() {
        console.log("[*] Initializing BEAR SSL Bypass System...");
        this.isActive = true;
        this.setupSSLPinningBypass();
        console.log("[+] BEAR SSL Bypass System Active - " + this.bypassCount + " bypasses installed");
    }
};

Java.perform(function() {
    console.log("[*] BEAR SSL Bypass Java VM initialized");
    
    BearSSLBypass.setupSSLPinningBypass = function() {
        // Bypass OkHttp3 certificate pinning
        try {
            var CertificatePinner = Java.use("okhttp3.CertificatePinner");
            CertificatePinner.check.overload('java.lang.String', 'java.util.List').implementation = function(hostname, peerCertificates) {
                console.log("[BEAR] OkHttp3 CertificatePinner.check() bypassed for " + hostname);
                
                // Track pinned hosts
                if (BearSSLBypass.pinnedHosts.indexOf(hostname) === -1) {
                    BearSSLBypass.pinnedHosts.push(hostname);
                }
                BearSSLBypass.bypassedConnections++;
                
                return;
            };

            CertificatePinner.check.overload('java.lang.String', '[Ljava.security.cert.Certificate;').implementation = function(hostname, peerCertificates) {
                console.log("[BEAR] OkHttp3 CertificatePinner.check() bypassed for " + hostname);
                
                // Track pinned hosts
                if (BearSSLBypass.pinnedHosts.indexOf(hostname) === -1) {
                    BearSSLBypass.pinnedHosts.push(hostname);
                }
                BearSSLBypass.bypassedConnections++;
                
                return;
            };

            console.log("[+] OkHttp3 CertificatePinner hooks installed");
            BearSSLBypass.bypassCount++;
        } catch (e) {
            console.log("[-] OkHttp3 CertificatePinner not found: " + e);
        }

        // Bypass TrustManagerImpl certificate verification
        try {
            var TrustManagerImpl = Java.use("com.android.org.conscrypt.TrustManagerImpl");

            TrustManagerImpl.verifyChain.implementation = function(untrustedChain, trustAnchorChain, host, clientAuth, ocspData, tlsSctData) {
                console.log("[BEAR] TrustManagerImpl.verifyChain() bypassed for " + host);
                BearSSLBypass.bypassedConnections++;
                return untrustedChain;
            };

            console.log("[+] TrustManagerImpl hooks installed");
            BearSSLBypass.bypassCount++;
        } catch (e) {
            console.log("[-] TrustManagerImpl not found: " + e);
        }

        // Bypass X509TrustManager certificate verification
        try {
            var X509TrustManager = Java.use("javax.net.ssl.X509TrustManager");
            var SSLContext = Java.use("javax.net.ssl.SSLContext");

            // Create a custom TrustManager that trusts all certificates
            var TrustManager = Java.registerClass({
                name: "com.bearloader.TrustAllCertificates",
                implements: [X509TrustManager],
                methods: {
                    checkClientTrusted: function(chain, authType) {
                        console.log("[BEAR] checkClientTrusted bypassed");
                        BearSSLBypass.bypassedConnections++;
                    },
                    checkServerTrusted: function(chain, authType) {
                        console.log("[BEAR] checkServerTrusted bypassed");
                        BearSSLBypass.bypassedConnections++;
                    },
                    getAcceptedIssuers: function() {
                        return [];
                    }
                }
            });

            // Create a new SSLContext with our custom TrustManager
            var TrustManagers = [TrustManager.$new()];
            var SSLContext_init = SSLContext.init.overload(
                "[Ljavax.net.ssl.KeyManager;",
                "[Ljavax.net.ssl.TrustManager;",
                "java.security.SecureRandom"
            );

            SSLContext_init.implementation = function(keyManager, trustManager, secureRandom) {
                console.log("[BEAR] SSLContext.init() hooked - using BEAR trust manager");
                SSLContext_init.call(this, keyManager, TrustManagers, secureRandom);
            };

            console.log("[+] X509TrustManager hooks installed");
            BearSSLBypass.bypassCount++;
        } catch (e) {
            console.log("[-] X509TrustManager not found: " + e);
        }

        // Bypass Appcelerator Titanium certificate pinning
        try {
            var PinningTrustManager = Java.use("appcelerator.https.PinningTrustManager");
            PinningTrustManager.checkServerTrusted.implementation = function(chain, authType) {
                console.log("[BEAR] Appcelerator PinningTrustManager.checkServerTrusted() bypassed");
                BearSSLBypass.bypassedConnections++;
                return;
            };

            console.log("[+] Appcelerator Titanium hooks installed");
            BearSSLBypass.bypassCount++;
        } catch (e) {
            console.log("[-] Appcelerator Titanium not found: " + e);
        }

        // Bypass Trustkit certificate pinning
        try {
            var TrustKit = Java.use("com.datatheorem.android.trustkit.pinning.OkHostnameVerifier");
            TrustKit.verify.overload('java.lang.String', 'javax.net.ssl.SSLSession').implementation = function(hostname, session) {
                console.log("[BEAR] TrustKit.verify() bypassed for " + hostname);
                
                // Track TrustKit hosts
                if (BearSSLBypass.pinnedHosts.indexOf(hostname) === -1) {
                    BearSSLBypass.pinnedHosts.push(hostname);
                }
                BearSSLBypass.bypassedConnections++;
                
                return true;
            };

            console.log("[+] TrustKit hooks installed");
            BearSSLBypass.bypassCount++;
        } catch (e) {
            console.log("[-] TrustKit not found: " + e);
        }

        // Bypass TrustManagerImpl recursive check
        try {
            var ArrayList = Java.use("java.util.ArrayList");
            var TrustManagerImpl = Java.use("com.android.org.conscrypt.TrustManagerImpl");

            TrustManagerImpl.checkTrustedRecursive.implementation = function(certs, host, clientAuth, untrustedChain, trustedChain, used) {
                console.log("[BEAR] TrustManagerImpl.checkTrustedRecursive() bypassed for " + host);
                BearSSLBypass.bypassedConnections++;
                return ArrayList.$new();
            };

            console.log("[+] TrustManagerImpl.checkTrustedRecursive hooks installed");
            BearSSLBypass.bypassCount++;
        } catch (e) {
            console.log("[-] TrustManagerImpl.checkTrustedRecursive not found: " + e);
        }

        // Bypass Android WebViewClient certificate verification
        try {
            var WebViewClient = Java.use("android.webkit.WebViewClient");

            WebViewClient.onReceivedSslError.implementation = function(webView, sslErrorHandler, sslError) {
                console.log("[BEAR] WebViewClient.onReceivedSslError() bypassed");
                BearSSLBypass.bypassedConnections++;
                sslErrorHandler.proceed();
                return;
            };

            console.log("[+] WebViewClient hooks installed");
            BearSSLBypass.bypassCount++;
        } catch (e) {
            console.log("[-] WebViewClient not found: " + e);
        }

        // Bypass Cordova certificate verification
        try {
            var CordovaWebViewClient = Java.use("org.apache.cordova.CordovaWebViewClient");

            CordovaWebViewClient.onReceivedSslError.implementation = function(view, handler, error) {
                console.log("[BEAR] CordovaWebViewClient.onReceivedSslError() bypassed");
                BearSSLBypass.bypassedConnections++;
                handler.proceed();
                return;
            };

            console.log("[+] CordovaWebViewClient hooks installed");
            BearSSLBypass.bypassCount++;
        } catch (e) {
            console.log("[-] CordovaWebViewClient not found: " + e);
        }

        // Additional comprehensive bypasses
        try {
            // Hook HostnameVerifier
            var HttpsURLConnection = Java.use("javax.net.ssl.HttpsURLConnection");
            HttpsURLConnection.setDefaultHostnameVerifier.implementation = function(hostnameVerifier) {
                console.log("[BEAR] HttpsURLConnection.setDefaultHostnameVerifier() bypassed");
                
                // Create a hostname verifier that accepts all hosts
                var TrustAllHostnames = Java.registerClass({
                    name: "com.bearloader.TrustAllHostnameVerifier", 
                    implements: [Java.use("javax.net.ssl.HostnameVerifier")],
                    methods: {
                        verify: function(hostname, session) {
                            console.log("[BEAR] HostnameVerifier.verify() bypassed for " + hostname);
                            return true;
                        }
                    }
                });
                
                return this.setDefaultHostnameVerifier(TrustAllHostnames.$new());
            };

            console.log("[+] HostnameVerifier hooks installed");
            BearSSLBypass.bypassCount++;
        } catch (e) {
            console.log("[-] HostnameVerifier not found: " + e);
        }

        console.log("[*] BEAR SSL pinning bypass complete");
    };
    
    // Get bypass statistics
    BearSSLBypass.getStats = function() {
        return {
            isActive: this.isActive,
            bypassCount: this.bypassCount,
            pinnedHosts: this.pinnedHosts,
            bypassedConnections: this.bypassedConnections
        };
    };
    
    BearSSLBypass.init();
});

console.log("[BEAR] SSL Pinning Bypass Module v3.0 Initialized"); 