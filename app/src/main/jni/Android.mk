LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libssl
LOCAL_SRC_FILES := rLogin/curl/openssl-android-$(TARGET_ARCH_ABI)/lib/libssl.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libcrypto
LOCAL_SRC_FILES := rLogin/curl/openssl-android-$(TARGET_ARCH_ABI)/lib/libcrypto.a
include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE := happy

LOCAL_SRC_FILES := \
    BearStub.cpp \
    BearSocket.cpp \
    Memory.cpp \
    AntiDetection.cpp

LOCAL_C_INCLUDES := \
    $(LOCAL_PATH)/includes \
    $(LOCAL_PATH)/includes/curl/curl-android-$(TARGET_ARCH_ABI)/include \
    $(LOCAL_PATH)/includes/curl/openssl-android-$(TARGET_ARCH_ABI)/include

LOCAL_CFLAGS := -Wno-error=format-security -fvisibility=hidden -ffunction-sections -fdata-sections -w
LOCAL_CFLAGS += -fno-rtti -fno-exceptions -fpermissive
LOCAL_CPPFLAGS := -std=c++14 -frtti -fexceptions -w
LOCAL_CPPFLAGS += -Wno-error=c++11-narrowing -fms-extensions -fno-rtti -fno-exceptions -fpermissive
LOCAL_LDFLAGS += -Wl,--gc-sections,--strip-all, -llog
LOCAL_ARM_MODE := arm

LOCAL_LDFLAGS += -Wl,--gc-sections
LOCAL_LDFLAGS += -L$(SYSROOT)/usr/lib -lz -llog

LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_CPP_FEATURES                      := exceptions
LOCAL_LDLIBS                            := -llog -landroid -lz
LOCAL_STATIC_LIBRARIES                  := libssl libcrypto

include $(BUILD_SHARED_LIBRARY)

# Include prebuilt libraries if they exist
ifneq ($(wildcard $(LOCAL_PATH)/includes/curl/curl-android-$(TARGET_ARCH_ABI)/lib/libcurl.a),)
    include $(CLEAR_VARS)
    LOCAL_MODULE := curl_static
    LOCAL_SRC_FILES := includes/curl/curl-android-$(TARGET_ARCH_ABI)/lib/libcurl.a
    include $(PREBUILT_STATIC_LIBRARY)
    
    include $(CLEAR_VARS)
    LOCAL_MODULE := ssl_static
    LOCAL_SRC_FILES := includes/curl/openssl-android-$(TARGET_ARCH_ABI)/lib/libssl.a
    include $(PREBUILT_STATIC_LIBRARY)
    
    include $(CLEAR_VARS)
    LOCAL_MODULE := crypto_static
    LOCAL_SRC_FILES := includes/curl/openssl-android-$(TARGET_ARCH_ABI)/lib/libcrypto.a
    include $(PREBUILT_STATIC_LIBRARY)
endif