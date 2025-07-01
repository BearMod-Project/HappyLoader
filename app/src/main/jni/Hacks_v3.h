#ifndef BEAR_LOADER_3_HACKS_H
#define BEAR_LOADER_3_HACKS_H

#include "socket.h"
#include "Color.h"
#include "items.h"
#include "rLogin/Login.h"
#include "Vector3.hpp"
#include "struct.h"

/**
 * BEAR-LOADER 3.0.0 Simplified Hacks System
 * 
 * This header provides streamlined ESP and memory hacking functionality
 * using the new simplified structure system.
 */

// ========================================
// GLOBAL VARIABLES
// ========================================
Color clrEnemy, clrEdge, clrBox, clrAlert, clr, clrTeam, clrDist, clrHealth, clrText, grenadeColor, clrtanda;
float h, w, x, y, z, magic_number, mx, my, top, bottom, textsize, mScale, skelSize;

int botCount, playerCount;
Response response;
Request request;
char extra[30];
char text[100];

// External references to global structures
extern Options options;
extern Memory memory;

// ========================================
// UTILITY FUNCTIONS
// ========================================

Color colorByDistance(int distance, int alpha) {
    Color clrDistance;
    if (distance < 600)
        clrDistance = Color::Yellow(255);
    if (distance < 300)
        clrDistance = Color::Orange(255);
    if (distance < 150)
        clrDistance = Color::Red(255);
    return clrDistance;
}

bool isOutsideSafeZone(Vec2 pos, Vec2 screen) {
    if (pos.y < 0) return true;
    if (pos.x > screen.x) return true;
    if (pos.y > screen.y) return true;
    return pos.x < 0;
}

Vec2 calculatePosition(const Vec2 &center, float radius, float angleDegrees) {
    float angleRadians = angleDegrees * (M_PI / 180.0f);
    float x = center.x + radius * cos(angleRadians);
    float y = center.y + radius * sin(angleRadians);
    return Vec2(x, y);
}

bool colorPosCenter(float sWidth, float smMx, float sHeight, float posT, float eWidth, float emMx,
                    float eHeight, float posB) {
    if (sWidth >= smMx && sHeight >= posT && eWidth <= emMx && eHeight <= posB) {
        return true;
    }
    return false;
}

Vec2 pushToScreenBorder(const Vec2 &location, const Vec2 &screen, float offset, float scale = 2.0f) {
    Vec2 center(screen.x / 2, screen.y / 2);
    float angle = atan2(location.y - center.y, location.x - center.x) * (180.0f / M_PI);
    return calculatePosition(center, offset * scale, angle);
}

// ========================================
// BEAR-LOADER 3.0.0 ESP SYSTEM
// ========================================

void DrawESP(ESP esp, int screenWidth, int screenHeight) {

    if (!xConnected && !xServerConnection)
        return;

    if (!g_Token.empty()) {
        if (!g_Auth.empty()) {
            if (g_Token == g_Auth) {

                // BEAR-LOADER 3.0.0 Status Display
                esp.DrawTextName(Color::White(255), "BEAR-LOADER 3.0.0 - FPS Check: ", 
                    Vec2(screenWidth / 12, screenHeight / 13.5), screenHeight / 40);
                esp.DrawTextMode(Color::White(255), "", 
                    Vec2(screenWidth / 5, screenHeight / 1.05), screenHeight / 45);

                // Simplified mode detection
                const char* aimText = "";
                if (options.openState == 0) {
                    aimText = "AIMBOT ACTIVE";
                } else if ((memory.LessRecoil || memory.SmallCrosshair || memory.WideView || memory.Aimbot)) {
                    aimText = "MEMORY HACK";
                } else {
                    aimText = "ESP ONLY";
                }

                esp.DrawTexture(Color::White(255), aimText, 
                    Vec2(screenWidth / 5, screenHeight / 1.09), screenHeight / 45);
                esp.DrawTextMode2(Color::White(255), "", 
                    Vec2(screenWidth / 5, screenHeight / 1.13), screenHeight / 45);

                // Setup request structure (simplified)
                request.ScreenHeight = screenHeight;
                request.ScreenWidth = screenWidth;
                request.options = options;
                request.memory = memory;
                request.Mode = InitMode;

                botCount = 0, playerCount = 0;
                BearSocket::sendData((void *) &request, sizeof(request));
                BearSocket::receive((void *) &response);
                
                float mScaleY = screenHeight / (float) 1080;
                mScale = screenHeight / (float) 1080;
                skelSize = (mScale * 1.5f);
                float BoxSize = (mScaleY * 2.0f);
                textsize = screenHeight / 50;
                Vec2 screen(screenWidth, screenHeight);
                
                if (!signValid) {
                    int *p = nullptr;
                    *p = 0;
                }

                if (response.Success) {

                    // ========================================
                    // PLAYER ESP RENDERING
                    // ========================================
                    for (int i = 0; i < response.PlayerCount; i++) {
                        PlayerData Player = response.Players[i];
                        x = Player.HeadLocation.x;
                        y = Player.HeadLocation.y;

                        sprintf(extra, "%0.0fM", Player.Distance);
                        float magic_number = (response.Players[i].Distance * response.fov);
                        float namewidht = (screenWidth / 6) / magic_number;
                        float pp2 = namewidht / 2;
                        float mx = (screenWidth / 4) / magic_number;
                        float my = (screenWidth / 1.38) / magic_number;
                        float top = y - my + (screenWidth / 1.7) / magic_number;
                        float bottom = response.Players[i].Bone.lAn.y + my - (screenWidth / 1.7) / magic_number;
                        
                        clrDist = colorByDistance((int) Player.Distance, 255);
                        clrAlert = _clrID((int) Player.TeamID, 80);
                        clrTeam = _clrID((int) Player.TeamID, 150);
                        clr = _clrID((int) Player.TeamID, 200);
                        Vec2 location(x, y);
                        float textsize = screenHeight / 50;
                        bool playerInCenter = colorPosCenter(screenWidth / 2, x - mx, screenHeight / 2, top,
                                                             screenWidth / 2, x + mx, screenHeight / 2, bottom);

                        if (Player.isBot) {
                            botCount++;
                            clrEnemy = Color::White(255);
                            clrEdge = Color::White(80);
                            clrBox = Color::White(255);
                            clrText = Color::Black(255);
                        } else {
                            playerCount++;
                            clrEnemy = clrDist;
                            clrEdge = clrAlert;
                            clrBox = Color::Red(255);
                            clrText = Color::White(255);
                        }

                        if (true) {
                            clrEnemy = playerInCenter ? Color::Green(255) : clrEnemy;
                            clrBox = playerInCenter ? Color::Green(255) : clrBox;
                            clrText = playerInCenter ? Color::Green(255) : clrText;
                            clrtanda = playerInCenter ? Color::White(255) : Color::Green(255);
                        }

                        if (response.Players[i].HeadLocation.z != 1) {
                            // On Screen
                            if (x > -50 && x < screenWidth + 50) {

                                // Player Skeleton
                                if (isSkeleton && Player.Bone.isBone) {
                                    float skelSize = (mScaleY * 2.0f);
                                    esp.DrawLine(clrBox, skelSize,
                                                 Vec2(response.Players[i].Bone.neck.x, response.Players[i].Bone.neck.y),
                                                 Vec2(response.Players[i].Bone.cheast.x, response.Players[i].Bone.cheast.y));
                                    esp.DrawLine(clrBox, skelSize,
                                                 Vec2(response.Players[i].Bone.cheast.x, response.Players[i].Bone.cheast.y),
                                                 Vec2(response.Players[i].Bone.pelvis.x, response.Players[i].Bone.pelvis.y));
                                    esp.DrawLine(clrBox, skelSize,
                                                 Vec2(response.Players[i].Bone.neck.x, response.Players[i].Bone.neck.y),
                                                 Vec2(response.Players[i].Bone.lSh.x, response.Players[i].Bone.lSh.y));
                                    esp.DrawLine(clrBox, skelSize,
                                                 Vec2(response.Players[i].Bone.neck.x, response.Players[i].Bone.neck.y),
                                                 Vec2(response.Players[i].Bone.rSh.x, response.Players[i].Bone.rSh.y));
                                    esp.DrawLine(clrBox, skelSize,
                                                 Vec2(response.Players[i].Bone.lSh.x, response.Players[i].Bone.lSh.y),
                                                 Vec2(response.Players[i].Bone.lElb.x, response.Players[i].Bone.lElb.y));
                                    esp.DrawLine(clrBox, skelSize,
                                                 Vec2(response.Players[i].Bone.rSh.x, response.Players[i].Bone.rSh.y),
                                                 Vec2(response.Players[i].Bone.rElb.x, response.Players[i].Bone.rElb.y));
                                    esp.DrawLine(clrBox, skelSize,
                                                 Vec2(response.Players[i].Bone.lElb.x, response.Players[i].Bone.lElb.y),
                                                 Vec2(response.Players[i].Bone.lWr.x, response.Players[i].Bone.lWr.y));
                                    esp.DrawLine(clrBox, skelSize,
                                                 Vec2(response.Players[i].Bone.rElb.x, response.Players[i].Bone.rElb.y),
                                                 Vec2(response.Players[i].Bone.rWr.x, response.Players[i].Bone.rWr.y));
                                    esp.DrawLine(clrBox, skelSize,
                                                 Vec2(response.Players[i].Bone.pelvis.x, response.Players[i].Bone.pelvis.y),
                                                 Vec2(response.Players[i].Bone.lTh.x, response.Players[i].Bone.lTh.y));
                                    esp.DrawLine(clrBox, skelSize,
                                                 Vec2(response.Players[i].Bone.pelvis.x, response.Players[i].Bone.pelvis.y),
                                                 Vec2(response.Players[i].Bone.rTh.x, response.Players[i].Bone.rTh.y));
                                    esp.DrawLine(clrBox, skelSize,
                                                 Vec2(response.Players[i].Bone.lTh.x, response.Players[i].Bone.lTh.y),
                                                 Vec2(response.Players[i].Bone.lKn.x, response.Players[i].Bone.lKn.y));
                                    esp.DrawLine(clrBox, skelSize,
                                                 Vec2(response.Players[i].Bone.rTh.x, response.Players[i].Bone.rTh.y),
                                                 Vec2(response.Players[i].Bone.rKn.x, response.Players[i].Bone.rKn.y));
                                    esp.DrawLine(clrBox, skelSize,
                                                 Vec2(response.Players[i].Bone.lKn.x, response.Players[i].Bone.lKn.y),
                                                 Vec2(response.Players[i].Bone.lAn.x, response.Players[i].Bone.lAn.y));
                                    esp.DrawLine(clrBox, skelSize,
                                                 Vec2(response.Players[i].Bone.rKn.x, response.Players[i].Bone.rKn.y),
                                                 Vec2(response.Players[i].Bone.rAn.x, response.Players[i].Bone.rAn.y));
                                }

                                // Player Box
                                if (isPlayerBox) {
                                    esp.DrawLine(clrBox, BoxSize, Vec2(x + pp2, top), Vec2(x + namewidht, top));
                                    esp.DrawLine(clrBox, BoxSize, Vec2(x - pp2, top), Vec2(x - namewidht, top));
                                    esp.DrawLine(clrBox, BoxSize, Vec2(x + namewidht, top), Vec2(x + namewidht, top + pp2));
                                    esp.DrawLine(clrBox, BoxSize, Vec2(x - namewidht, top), Vec2(x - namewidht, top + pp2));
                                    esp.DrawLine(clrBox, BoxSize, Vec2(x + pp2, bottom), Vec2(x + namewidht, bottom));
                                    esp.DrawLine(clrBox, BoxSize, Vec2(x - pp2, bottom), Vec2(x - namewidht, bottom));
                                    esp.DrawLine(clrBox, BoxSize, Vec2(x - namewidht, bottom), Vec2(x - namewidht, bottom - pp2));
                                    esp.DrawLine(clrBox, BoxSize, Vec2(x + namewidht, bottom), Vec2(x + namewidht, bottom - pp2));
                                }

                                // Player Line
                                if (isPlayerLine) {
                                    esp.DrawLine(clrBox, skelSize, Vec2(screenWidth / 2, screenHeight / 9), Vec2(x, top - screenHeight / 31));
                                }

                                // Player Health
                                if (isPlayerHealth) {
                                    float healthLength = screenWidth / 24;
                                    float healthHeight = mScale * (screenHeight / 10);
                                    float healthWidth = screenWidth / 200;
                                    float healthX = response.Players[i].Bone.cheast.x + mx - (screenHeight / 50) / magic_number;

                                    if (healthLength < mx) healthLength = mx;

                                    if (response.Players[i].Health < 25)
                                        clrHealth = Color(255, 0, 0, 185);
                                    else if (response.Players[i].Health < 50)
                                        clrHealth = Color(255, 204, 0, 185);
                                    else if (response.Players[i].Health < 75)
                                        clrHealth = Color(255, 255, 0, 185);
                                    else
                                        clrHealth = Color(34, 214, 97, 225);

                                    if (response.Players[i].Health == 0) {
                                        esp.DrawText(Color(255, 0, 0), "Knock", Vec2(healthX, response.Players[i].Bone.cheast.y), textsize);
                                    } else {
                                        if (Player.Distance <= 50) {
                                            float healthY = response.Players[i].Bone.pelvis.y - healthHeight / 2;
                                            float healthHeightFilled = (healthHeight * response.Players[i].Health) / 100;

                                            esp.DrawFilledRect(clrHealth,
                                                               Vec2(healthX, healthY + (healthHeight - healthHeightFilled)),
                                                               Vec2(healthX + healthWidth, healthY + healthHeight));
                                            esp.DrawRect(Color(0, 0, 0), screenHeight / 640,
                                                         Vec2(healthX, healthY),
                                                         Vec2(healthX + healthWidth, healthY + healthHeight));
                                        } else {
                                            esp.DrawFilledRect(clrHealth,
                                                               Vec2(x - healthLength, top - screenHeight / 30),
                                                               Vec2(x - healthLength + (2 * healthLength) * response.Players[i].Health / 100,
                                                                    top - screenHeight / 225));
                                            esp.DrawRect(Color(0, 0, 0), screenHeight / 640,
                                                         Vec2(x - healthLength, top - screenHeight / 30),
                                                         Vec2(x + healthLength, top - screenHeight / 255));
                                        }
                                    }
                                }

                                // Player Head
                                if (isPlayerHead) {
                                    esp.DrawFilledCircle(clrEdge, Vec2(response.Players[i].HeadLocation.x, response.Players[i].HeadLocation.y),
                                                         screenHeight / 12 / magic_number);
                                }

                                // Player Names
                                if (isPlayerName && response.Players[i].isBot) {
                                    sprintf(extra, "Bot");
                                    esp.DrawText(Color(255, 255, 255), extra, Vec2(x, top - 12), textsize);
                                } else if (isPlayerName) {
                                    esp.DrawName(Color().White(255), response.Players[i].PlayerNameByte,
                                                 response.Players[i].TeamID, Vec2(response.Players[i].HeadLocation.x, top - 12), textsize);
                                }

                                // Player Distance
                                if (isPlayerDistance) {
                                    sprintf(extra, "%0.0f M", response.Players[i].Distance);
                                    esp.DrawText(Color(247, 175, 63, 255), extra, Vec2(x, bottom + screenHeight / 45), textsize);
                                }

                                // Player Weapon
                                if (isPlayerWeapon && response.Players[i].Weapon.isWeapon) {
                                    if (Player.Distance <= 50) {
                                        esp.DrawWeapon(Color(247, 175, 63, 255), response.Players[i].Weapon.id,
                                                       response.Players[i].Weapon.ammo, response.Players[i].Weapon.ammo,
                                                       Vec2(x, top - 45), textsize);
                                    } else {
                                        esp.DrawWeapon(Color(247, 175, 63, 255), response.Players[i].Weapon.id,
                                                       response.Players[i].Weapon.ammo, response.Players[i].Weapon.ammo,
                                                       Vec2(x - 45, top - 45), textsize);
                                    }
                                }

                                // Player Weapon Icon
                                if (isPlayerWeaponIcon && response.Players[i].Weapon.isWeapon) {
                                    esp.DrawWeaponIcon(response.Players[i].Weapon.id,
                                                       Vec2(x - screenWidth / 45, top - screenHeight / 15));
                                }

                            } // OnScreen

                            // 360 Alert
                            if (is360Alert && isOutsideSafeZone(location, screen)) {
                                Vec2 hintDotRenderPos = pushToScreenBorder(location, screen, (mScaleY * 100) / 3, 5.0f);
                                esp.DrawFilledCircle(clrAlert, hintDotRenderPos, (mScaleY * 20));
                            }

                        } // Player.HeadLocation.z
                    } // response.PlayerCount

                    // ========================================
                    // GRENADE ESP RENDERING
                    // ========================================
                    for (int i = 0; i < response.GrenadeCount; i++) {
                        GrenadeData grenade = response.Grenade[i];
                        if (!isGrenadeWarning || grenade.Location.z == 1.0f) {
                            continue;
                        }
                        const char *grenadeTypeText;
                        switch (grenade.type) {
                            case 1:
                                grenadeColor = Color::Red(255);
                                grenadeTypeText = "Grenade";
                                break;
                            case 2:
                                grenadeColor = Color::Orange(255);
                                grenadeTypeText = "Molotov";
                                break;
                            case 3:
                                grenadeColor = Color::Yellow(255);
                                grenadeTypeText = "Stun";
                                break;
                            default:
                                grenadeColor = Color::White(255);
                                grenadeTypeText = "Smoke";
                        }

                        sprintf(extra, "%s (%0.0f m)", grenadeTypeText, grenade.Distance);
                        sprintf(text, "Throwable %s (%0.0f m)", grenadeTypeText, grenade.Distance);
                        esp.DrawText(grenadeColor, extra, Vec2(grenade.Location.x, grenade.Location.y + (screenHeight / 50)), textsize);
                        esp.DrawText(grenadeColor, "〇", Vec2(grenade.Location.x, grenade.Location.y), textsize);
                    } // response.GrenadeCount

                    // ========================================
                    // VEHICLE ESP RENDERING
                    // ========================================
                    for (int i = 0; i < response.VehicleCount; i++) {
                        VehicleData vehicle = response.Vehicles[i];
                        if (vehicle.Location.z != 1.0f) {
                            esp.DrawVehicles(vehicle.VehicleName, vehicle.Distance, vehicle.Health, vehicle.Fuel,
                                             Vec2(vehicle.Location.x, vehicle.Location.y), screenHeight / 47);
                        }
                    } // response.VehicleCount

                    // ========================================
                    // ITEM ESP RENDERING
                    // ========================================
                    for (int i = 0; i < response.ItemsCount; i++) {
                        ItemData currentItem = response.Items[i];
                        if (currentItem.Location.z != 1.0f) {
                            esp.DrawItems(currentItem.ItemName, currentItem.Distance,
                                          Vec2(currentItem.Location.x, currentItem.Location.y), screenHeight / 50);
                        }
                    } // response.ItemsCount

                } // response.Success
            } // g_Token == g_Auth
        } // !g_Auth.empty()
    } // !g_Token.empty()
}

#endif //BEAR_LOADER_3_HACKS_H 