# 🐻 BEAR-LOADER v3.0.0

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![API](https://img.shields.io/badge/API-28%2B-brightgreen.svg)](https://android-arsenal.com/api?level=28)
[![Build](https://img.shields.io/badge/Build-Passing-success.svg)](https://github.com/BearMod-Project/HappyLoader)

> **Advanced Mobile Game Enhancement Framework with Professional KeyAuth Integration**

BEAR-LOADER is a sophisticated Android application framework designed for mobile game enhancement, featuring enterprise-grade architecture, secure KeyAuth authentication, and modular download management.

## 🚀 Features

### 🔐 **Security & Authentication**
- **KeyAuth Integration**: Secure user authentication and licensing
- **Anti-Detection System**: Advanced stealth mechanisms  
- **Signature Verification**: Multi-layer app integrity validation
- **Encrypted Communication**: Secure API endpoints with SSL/TLS

### 📦 **Download Management System**
- **File ID-Based Downloads**: KeyAuth dashboard file management
- **Concurrent Processing**: Multi-threaded download optimization
- **Progress Tracking**: Real-time download monitoring
- **Automatic Verification**: SHA-256 integrity checking
- **Modular Architecture**: Clean interface-based design

### 🎮 **Game Support**
- **PUBG Mobile Global** (`com.tencent.ig`)
- **PUBG Mobile Korea** (`com.pubg.krmobile`)
- **PUBG Mobile Taiwan** (`com.reko.pubgm`)
- **PUBG Mobile Vietnam** (`com.vng.pubgmobile`)
- **Battlegrounds Mobile India** (`com.battlegroundmobile.india`)

### 🏗️ **Architecture Highlights**
- **Clean Architecture**: Interface-based dependency injection
- **Thread Safety**: Concurrent download management
- **Memory Optimization**: Efficient resource handling
- **Error Recovery**: Robust error handling and retry logic
- **Configuration Management**: Centralized game-specific settings

## 📋 Requirements

- **Android API Level**: 28+ (Android 9.0)
- **Architecture**: ARM64-v8a (primary), ARMv7 (legacy support)
- **Storage**: 50MB+ free space
- **Network**: Internet connection required
- **KeyAuth Account**: Valid subscription required

## 🛠️ Installation

### Prerequisites
```bash
# Clone the repository
git clone https://github.com/BearMod-Project/HappyLoader.git
cd HappyLoader

# Ensure Android SDK and NDK are installed
# NDK Version: 26.1.10909125 (as specified in build.gradle)
```

### Build Configuration
```gradle
// Update KeyAuth credentials in app/build.gradle
buildConfigField "String", "KEYAUTH_APP_NAME", '"YOUR_APP_NAME"'
buildConfigField "String", "KEYAUTH_OWNER_ID", '"YOUR_OWNER_ID"'
buildConfigField "String", "KEYAUTH_APP_SECRET", '"YOUR_APP_SECRET"'
```

### Build & Install
```bash
# Build release APK
./gradlew assembleRelease

# Install to device
adb install app/build/outputs/apk/release/app-release.apk
```

## 📚 Usage

### Basic Implementation
```java
// Initialize download manager
IDownloadManager downloadManager = BearDownloadManager.getInstance(context);

// Download game resources
BearDownloadUtils.downloadGameResources(context, "com.tencent.ig");

// Check download status
String status = BearDownloadUtils.getDownloadStatus(context);
```

### Advanced Configuration
```java
// Custom download with progress tracking
IDownloadManager.DownloadRequest request = new IDownloadManager.DownloadRequest(
    "custom_mod",
    "Game Enhancement",
    "420371", // KeyAuth file ID
    "/data/data/com.tencent.ig/files/mod.so",
    IDownloadManager.DownloadType.LIBRARY
);

downloadManager.downloadWithUI(request, new IDownloadManager.DownloadCallback() {
    @Override
    public void onComplete(String downloadId, File file) {
        // Handle completion
        System.loadLibrary(file.getAbsolutePath());
    }
    
    @Override
    public void onError(String downloadId, String error) {
        // Handle error
        Log.e("Download", "Failed: " + error);
    }
});
```

## 🏗️ Architecture Overview

### Clean Architecture Layers
```
┌─────────────────────────────────────────┐
│                UI Layer                 │
├─────────────────────────────────────────┤
│             Use Cases                   │
├─────────────────────────────────────────┤
│          Domain Interfaces              │
├─────────────────────────────────────────┤
│        Implementation Layer             │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │   KeyAuth   │  │   Download      │   │
│  │     API     │  │   Manager       │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
```

### Key Components
- **`IDownloadManager`**: Core download interface
- **`BearDownloadManager`**: Thread-safe implementation
- **`FileMapping`**: Game-specific configuration
- **`KeyAuthAPIImpl`**: Secure authentication
- **`BearDownloadUtils`**: High-level utilities

## 🔧 Configuration

### File Mapping Setup
Update `FileMapping.java` with your KeyAuth file IDs:

```java
// Game configuration files
PATCH_CONFIG_IDS.put("com.tencent.ig", "YOUR_CONFIG_FILE_ID");

// Library files
STEALTH_LIB_IDS.put("com.tencent.ig", "YOUR_STEALTH_LIB_ID");
HOOK_LIB_IDS.put("com.tencent.ig", "YOUR_HOOK_LIB_ID");
```

### Build Variants
```gradle
buildTypes {
    release {
        buildConfigField "boolean", "ENABLE_DOWNLOAD_LOGGING", "false"
        buildConfigField "int", "MAX_CONCURRENT_DOWNLOADS", "3"
        buildConfigField "long", "DOWNLOAD_TIMEOUT_MS", "300000L"
    }
    
    debug {
        buildConfigField "boolean", "ENABLE_DOWNLOAD_LOGGING", "true"
        buildConfigField "int", "MAX_CONCURRENT_DOWNLOADS", "2"
        buildConfigField "long", "DOWNLOAD_TIMEOUT_MS", "180000L"
    }
}
```

## 🛡️ Security Features

### Anti-Detection Mechanisms
- **Process Hiding**: Advanced process concealment
- **Memory Protection**: Runtime memory encryption
- **Signature Spoofing**: Dynamic signature verification
- **Root Detection Bypass**: Multiple bypass strategies

### KeyAuth Integration
- **License Validation**: Real-time license checking
- **User Authentication**: Secure login system
- **File Distribution**: Secure file delivery system
- **Usage Analytics**: Download and usage tracking

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Development Setup
```bash
# Fork the repository
git clone https://github.com/YOUR_USERNAME/HappyLoader.git

# Create feature branch
git checkout -b feature/amazing-feature

# Make changes and commit
git commit -m "Add amazing feature"

# Push to branch
git push origin feature/amazing-feature

# Create Pull Request
```

### Code Standards
- **Java 17** compliance
- **Android Lint** clean builds
- **ProGuard** optimization compatible
- **Thread-safe** implementations
- **Comprehensive** error handling

### Testing
```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew connectedAndroidTest

# Generate test coverage report
./gradlew jacocoTestReport
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## ⚠️ Disclaimer

This software is provided for educational and research purposes only. Users are responsible for complying with all applicable laws and terms of service of target applications.

## 🙏 Acknowledgments

- **KeyAuth Team** - Authentication infrastructure
- **Android Open Source Project** - Platform foundation
- **OpenSSL** - Cryptographic libraries
- **cURL** - Network communication

## 📞 Support

- **Documentation**: [Wiki](https://github.com/BearMod-Project/HappyLoader/wiki)
- **Issues**: [GitHub Issues](https://github.com/BearMod-Project/HappyLoader/issues)
- **Security**: Report via private message

---

<div align="center">

**Made with ❤️ by the BEAR-LOADER Team**

[🌟 Star this repo](https://github.com/BearMod-Project/HappyLoader/stargazers) • [🐛 Report Bug](https://github.com/BearMod-Project/HappyLoader/issues) • [💡 Request Feature](https://github.com/BearMod-Project/HappyLoader/issues)

</div> 