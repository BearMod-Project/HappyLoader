#ifndef LOGIN_H
#define LOGIN_H

#include "StrEnc.h"
#include "Includes.h"
#include "json.hpp"
#include "Log.h"
#include <jni.h>
#include <string>
#include <cstring>
#include "obfuscate.h"
#include <jni.h>
#include <string>
#include <android/log.h>
#include <openssl/evp.h>
#include <openssl/pem.h>
#include <openssl/rsa.h>
#include <openssl/err.h>
#include <openssl/md5.h>
#include <openssl/sha.h>
#include <sys/stat.h>
#include <fcntl.h>

using json = nlohmann::ordered_json;
bool xConnected = false, xServerConnection = false, memek = false;
std::string g_Auth, g_Token,ts;
//std::string g_Auth, g_Token,EXP;
bool bValid = false, xEnv = false;
bool check;
//int modekey = 1;



const char *GetAndroidID(JNIEnv *env, jobject context) {
    jclass contextClass = env->FindClass("android/content/Context");
    jmethodID getContentResolverMethod = env->GetMethodID(contextClass,"getContentResolver","()Landroid/content/ContentResolver;");
    jclass settingSecureClass = env->FindClass("android/provider/Settings$Secure");
    jmethodID getStringMethod = env->GetStaticMethodID(settingSecureClass,"getString", "(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;");

    auto obj = env->CallObjectMethod(context, getContentResolverMethod);
    auto str = (jstring) env->CallStaticObjectMethod(settingSecureClass, getStringMethod, obj,env->NewStringUTF("android_id"));
    return env->GetStringUTFChars(str, nullptr);
}

const char *GetDeviceModel(JNIEnv *env) {
    jclass buildClass = env->FindClass("android/os/Build");
    jfieldID modelId = env->GetStaticFieldID(buildClass, "MODEL","Ljava/lang/String;");

    auto str = (jstring) env->GetStaticObjectField(buildClass, modelId);
    return env->GetStringUTFChars(str, nullptr);
}

const char *GetDeviceBrand(JNIEnv *env) {
    jclass buildClass = env->FindClass("android/os/Build");
    jfieldID modelId = env->GetStaticFieldID(buildClass, "BRAND","Ljava/lang/String;");

    auto str = (jstring) env->GetStaticObjectField(buildClass, modelId);
    return env->GetStringUTFChars(str, nullptr);
}

const char *GetPackageName(JNIEnv *env, jobject context) {
    jclass contextClass = env->FindClass("android/content/Context");
    jmethodID getPackageNameId = env->GetMethodID(contextClass, "getPackageName","()Ljava/lang/String;");

    auto str = (jstring) env->CallObjectMethod(context, getPackageNameId);
    return env->GetStringUTFChars(str, nullptr);
}

const char *GetDeviceUniqueIdentifier(JNIEnv *env, const char *uuid) {
    jclass uuidClass = env->FindClass("java/util/UUID");

    auto len = strlen(uuid);

    jbyteArray myJByteArray = env->NewByteArray(len);
    env->SetByteArrayRegion(myJByteArray, 0, len, (jbyte *) uuid);

    jmethodID nameUUIDFromBytesMethod = env->GetStaticMethodID(uuidClass,"nameUUIDFromBytes","([B)Ljava/util/UUID;");
    jmethodID toStringMethod = env->GetMethodID(uuidClass, "toString","()Ljava/lang/String;");

    auto obj = env->CallStaticObjectMethod(uuidClass, nameUUIDFromBytesMethod, myJByteArray);
    auto str = (jstring) env->CallObjectMethod(obj, toStringMethod);
    return env->GetStringUTFChars(str, nullptr);
}

std::string CalcMD5(std::string s) {
    std::string result;

    unsigned char hash[MD5_DIGEST_LENGTH];
    char tmp[4];

    MD5_CTX md5;
    MD5_Init(&md5);
    MD5_Update(&md5, s.c_str(), s.length());
    MD5_Final(hash, &md5);
    for (unsigned char i : hash) {
        sprintf(tmp, "%02x", i);
        result += tmp;
    }
    return result;
}

std::string CalcSHA256(const std::string& s) {
    std::string result;

    unsigned char hash[SHA256_DIGEST_LENGTH];
    char tmp[4];

    SHA256_CTX sha256;
    SHA256_Init(&sha256);
    SHA256_Update(&sha256, s.c_str(), s.length());
    SHA256_Final(hash, &sha256);
    for (unsigned char i : hash) {
        sprintf(tmp, "%02x", i);
        result += tmp;
    }
    return result;

}
extern "C" JNIEXPORT jstring JNICALL native_Check(JNIEnv *env, jclass clazz, jobject mContext, jstring mUserKey, jstring mModeSelect) {
  const char* user_key = env->GetStringUTFChars(mUserKey, nullptr);
  const char* mode_select = env->GetStringUTFChars(mModeSelect, nullptr);
  std::string hwid = user_key;
  hwid += GetAndroidID(env, mContext);
  hwid += GetDeviceModel(env);
  hwid += GetDeviceBrand(env);
  std::string UUID = GetDeviceUniqueIdentifier(env, hwid.c_str());
  std::string errMsg;

  // Generate simple local auth token (legacy cURL removed)
  std::string authData = "AUTH-";
  authData += user_key;
  authData += "-";
  authData += UUID;

  std::string outputAuth = CalcMD5(authData);
  
  // For now, perform simple validation
  // You can implement your own validation logic here
  if (strlen(user_key) > 0) {
      g_Token = outputAuth;
      g_Auth = outputAuth;
      xConnected = true;
      xServerConnection = true;
      memek = true;
      xEnv = true;
      bValid = true;
  } else {
      errMsg = "Invalid user key";
      bValid = false;
  }
    
  env->ReleaseStringUTFChars(mUserKey, user_key);
  env->ReleaseStringUTFChars(mModeSelect, mode_select);

  return bValid ? env->NewStringUTF("OK") : env->NewStringUTF(errMsg.c_str());
}


bool signValid = false;
extern "C" JNIEXPORT void JNICALL
Java_com_happy_pro_Component_Utils_sign(JNIEnv *env, jclass, jstring signatureHash) {
    const char *hashStr = env->GetStringUTFChars(signatureHash, nullptr);
    if (strcmp(hashStr, "MUE6RTA6MjY6OEU6QjA6OUY6QjM6RUM6Nzc6NDM6NjE6Q0Y6RUQ6RjM6Qjk6REE6QTE6RDM6NTI6Nzc6NDk6QTI6MTc6RUM6QkY6RkU6RUU6ODI6RDU6REM6RjU6ODI=") == 0) {
        signValid = true;
    } else {
        int *p = nullptr;
        *p = 0;
    }
    env->ReleaseStringUTFChars(signatureHash, hashStr);
}



/*
bool SecherREI(const std::string& folderPath) {
    struct stat buffer{};
    return (stat(folderPath.c_str(), &buffer) == 0);
}

void Detected_REIHttpCanary() {
    std::string folderPath = OBFUSCATE("/storage/emulated/0/Android/data/com.guoshi.httpcanary");
    if (SecherREI(folderPath)) {
        REI_HttpsCanay_Closed();
    }
}
void Detected_REIHttpCanary1() {
    std::string folderPath = OBFUSCATE("/storage/emulated/0/Android/data/com.guoshi.httpcanary.premium");
    if (SecherREI(folderPath)) {
        REI_HttpsCanay_Closed();
    }
}
void Detected_REIHttpCanary2() {
    std::string folderPath = OBFUSCATE("/storage/emulated/0/Android/data/com.sniffer");
    if (SecherREI(folderPath)) {
        REI_HttpsCanay_Closed();
    }
}

void Detected_REIHttpCanary3() {
    std::string folderPath = OBFUSCATE("/storage/emulated/0/Android/data/com.httpcanary.pro");
    if (SecherREI(folderPath)) {
        REI_HttpsCanay_Closed();
    } else {

    }
}
*/

#endif


