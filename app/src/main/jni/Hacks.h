#ifndef HACKS_H
#define HACKS_H
#include "socket.h"
#include "StrEnc.h" 
#include <jni.h>
#include <string>
#include <array>
#include <cmath>
#include "struct.h"

#ifndef OBFUSCATE
#define OBFUSCATE(str) str
#endif

// ========================================
// BEAR-LOADER 3.0.0 GLOBAL VARIABLES
// ========================================
Request request;
Response response;
float x,y;
char extra[30];
int botCount,playerCount;
int stylehealth,boxstyle,countStyle,WarningRange;
Color clr, clrFilled, clrHead, clrHealth, clrID, clrWarning;

// BEAR-LOADER 3.0.0 Global References
extern Memory memory;  // Defined in Main.cpp
extern Options options; // Defined in Main.cpp

// ========================================
// UTILITY FUNCTIONS
// ========================================
float CalculateDistance(float x1, float y1, float x2, float y2) {
    float dx = x1 - x2;
    float dy = y1 - y2;
    return sqrt(dx * dx + dy * dy);
}

bool IsPointInsideCircle(float x, float y, float centerX, float centerY, float radius) {
    return CalculateDistance(x, y, centerX, centerY) <= radius;
}

bool IsInCenter(Rect box, Vec2 Crosshair) {
    return box.x < Crosshair.x && box.y < Crosshair.y && box.x + box.width > Crosshair.x && box.y + box.height  > Crosshair.y;
}

Vec2 calculatePosition(const Vec2 &center, float radius, float angleDegrees) {
    float angleRadians = angleDegrees * (M_PI / 180.0f);
    float x = center.x + radius * cos(angleRadians);
    float y = center.y + radius * sin(angleRadians);
    return Vec2(x, y);
}

Vec2 pushToScreenBorder(const Vec2 &location, const Vec2 &screen, float offset, float scale = 2.0f) {
    Vec2 center(screen.x / 2, screen.y / 2);
    float angle = atan2(location.y - center.y, location.x - center.x) * (180.0f / M_PI);
    return calculatePosition(center, offset * scale, angle);
}

bool isOutsideSafeZone(Vec2 pos, Vec2 screen) {
    if (pos.y < 0) return true;
    if (pos.x > screen.x) return true;
    if (pos.y > screen.y) return true;
    return pos.x < 0;
}

// ========================================
// BEAR-LOADER 3.0.0 ESP SYSTEM
// ========================================
void DrawESP(ESP esp, int screenWidth, int screenHeight) {

    // BEAR-LOADER 3.0.0 Header
    esp.DrawText(Color(255,0,0,255),OBFUSCATE("BEAR-LOADER 3.0.0"),
                         Vec2(screenWidth / 2 + screenHeight / 40 + 50, screenHeight / 10.8 - 5),
                         screenHeight / 35);

    botCount=0;playerCount=0;
    request.ScreenHeight=screenHeight;
    request.ScreenWidth=screenWidth;
    request.Mode=InitMode;
    request.options=options;
    request.memory=memory;
    send((void*)&request,sizeof(request));
    receive((void*)&response);
    float textsize=screenHeight/50;
    
    if(response.Success) {
        float textsize=screenHeight/50;
        
        // ========================================
        // PLAYER ESP LOOP
        // ========================================
        for (int i = 0; i < response.PlayerCount; i++) {
            x = response.Players[i].HeadLocation.x;
            y = response.Players[i].HeadLocation.y;
            float magic_number = (response.Players[i].Distance * response.fov);
            float mScaleY = screenHeight / (float) 1080;
            float mx = (screenWidth / 4 ) / magic_number;
            float my = (screenWidth / 1.38) / magic_number;
            float pp2 = mx / 2;
            float top = y - my + (screenWidth / 1.7) / magic_number;
            float bottom = response.Players[i].Bone.root.y + my - (screenWidth / 1.7) / magic_number;
            
            Vec2 screen(screenWidth, screenHeight);
            Vec2 location(x,y);
            float P_Height = bottom - top;
            float P_Width = P_Height * 0.65f;
            Vec2 vBox(x - (P_Width / 2), y);
            Rect BoxRect(vBox.x, vBox.y, P_Width, P_Height);
            
            // Color assignment based on player type
            if (response.Players[i].isBot) {
                botCount++;
                clr = Color::White(255);
                clrFilled = Color::White(100);
                clrHead = Color::White(255);
                if (response.Players[i].isKnocked) {
                    clrWarning = Color(0, 0, 0, 130);
                } else {
                    clrWarning = Color(0, 255, 255, 255);
                }
            } else {
                playerCount++;
                if (response.Players[i].Distance < 125) {
                    clr = Color::Red(255);
                    clrFilled = Color::Red(100);
                    clrHead = Color::Red(255);
                } else if (response.Players[i].Distance < 175) {
                    clr = Color::Orange(255);
                    clrFilled = Color::Orange(100);
                    clrHead = Color::Orange(255);
                } else {
                    clr = Color::Yellow(255);
                    clrFilled = Color::Yellow(100);
                    clrHead = Color::Yellow(255);
                }
                if (response.Players[i].isKnocked) {
                    clrWarning = Color(0, 0, 0, 130);
                } else {
                    clrWarning = Color::Red(255);
                }
            }
            
            // Center target highlighting
            if (IsInCenter(BoxRect, Vec2(screenWidth / 2, screenHeight / 2))) {
                clr = Color::Green(255);
                clrFilled = Color::Green(100);
                clrHead = Color::Green(255);
            }
            
            // Team ID Color Assignment (Simplified for BEAR 3.0.0)
            int teamColorIndex = response.Players[i].TeamID % 50;
            switch (teamColorIndex % 10) {
                case 0: clrID = Color(220, 20, 60, 160); break;
                case 1: clrID = Color(178, 34, 34, 160); break;
                case 2: clrID = Color(255, 20, 147, 160); break;
                case 3: clrID = Color(255, 69, 0, 160); break;
                case 4: clrID = Color(255, 215, 0, 160); break;
                case 5: clrID = Color(138, 43, 226, 160); break;
                case 6: clrID = Color(0, 128, 0, 160); break;
                case 7: clrID = Color(32, 178, 170, 160); break;
                case 8: clrID = Color(70, 130, 180, 160); break;
                case 9: clrID = Color(184, 134, 11, 160); break;
                default: clrID = Color(255, 255, 255, 160); break;
            }

            if (response.Players[i].HeadLocation.z >= 0) {
                if (x > -50 && x < screenWidth + 50) { // onScreen
                    PlayerData player = response.Players[i];
                    
                    // Skeleton Drawing (Fixed typo: isSkelton -> isSkeleton)
                    if (isSkeleton && player.Bone.isBone) {
                        esp.DrawLine(clr, 2.5,
                                    Vec2(response.Players[i].Bone.neck.x, response.Players[i].Bone.neck.y),
                                    Vec2(response.Players[i].Bone.cheast.x, response.Players[i].Bone.cheast.y));
                        esp.DrawLine(clr, 2.5,
                                    Vec2(response.Players[i].Bone.cheast.x, response.Players[i].Bone.cheast.y),
                                    Vec2(response.Players[i].Bone.pelvis.x, response.Players[i].Bone.pelvis.y));
                        esp.DrawLine(clr, 2.5,
                                    Vec2(response.Players[i].Bone.neck.x, response.Players[i].Bone.neck.y),
                                    Vec2(response.Players[i].Bone.lSh.x, response.Players[i].Bone.lSh.y));
                        esp.DrawLine(clr, 2.5,
                                    Vec2(response.Players[i].Bone.neck.x, response.Players[i].Bone.neck.y),
                                    Vec2(response.Players[i].Bone.rSh.x, response.Players[i].Bone.rSh.y));
                        esp.DrawLine(clr, 2.5,
                                    Vec2(response.Players[i].Bone.lSh.x, response.Players[i].Bone.lSh.y),
                                    Vec2(response.Players[i].Bone.lElb.x, response.Players[i].Bone.lElb.y));
                        esp.DrawLine(clr, 2.5,
                                    Vec2(response.Players[i].Bone.rSh.x, response.Players[i].Bone.rSh.y),
                                    Vec2(response.Players[i].Bone.rElb.x, response.Players[i].Bone.rElb.y));
                        esp.DrawLine(clr, 2.5,
                                    Vec2(response.Players[i].Bone.lElb.x, response.Players[i].Bone.lElb.y),
                                    Vec2(response.Players[i].Bone.lWr.x, response.Players[i].Bone.lWr.y));
                        esp.DrawLine(clr, 2.5,
                                    Vec2(response.Players[i].Bone.rElb.x, response.Players[i].Bone.rElb.y),
                                    Vec2(response.Players[i].Bone.rWr.x, response.Players[i].Bone.rWr.y));
                        esp.DrawLine(clr, 2.5,
                                    Vec2(response.Players[i].Bone.pelvis.x, response.Players[i].Bone.pelvis.y),
                                    Vec2(response.Players[i].Bone.lTh.x, response.Players[i].Bone.lTh.y));
                        esp.DrawLine(clr, 2.5,
                                    Vec2(response.Players[i].Bone.pelvis.x, response.Players[i].Bone.pelvis.y),
                                    Vec2(response.Players[i].Bone.rTh.x, response.Players[i].Bone.rTh.y));
                        esp.DrawLine(clr, 2.5,
                                    Vec2(response.Players[i].Bone.lTh.x, response.Players[i].Bone.lTh.y),
                                    Vec2(response.Players[i].Bone.lKn.x, response.Players[i].Bone.lKn.y));
                        esp.DrawLine(clr, 2.5,
                                    Vec2(response.Players[i].Bone.rTh.x, response.Players[i].Bone.rTh.y),
                                    Vec2(response.Players[i].Bone.rKn.x, response.Players[i].Bone.rKn.y));
                        esp.DrawLine(clr, 2.5,
                                    Vec2(response.Players[i].Bone.lKn.x, response.Players[i].Bone.lKn.y),
                                    Vec2(response.Players[i].Bone.lAn.x, response.Players[i].Bone.lAn.y));
                        esp.DrawLine(clr, 2.5,
                                    Vec2(response.Players[i].Bone.rKn.x, response.Players[i].Bone.rKn.y),
                                    Vec2(response.Players[i].Bone.rAn.x, response.Players[i].Bone.rAn.y));
                    }

                    // Head Drawing (Changed DrawHead to DrawFilledCircle)
                    if (isPlayerHead) {
                        esp.DrawFilledCircle(clr, Vec2(response.Players[i].HeadLocation.x, response.Players[i].HeadLocation.y),
                                           screenHeight / 12 / magic_number);
                    }

                    // Box Drawing
                    if (isPlayerBox) {
                        esp.DrawLine(clr, 2.5, Vec2(x + pp2, top), Vec2(x + mx, top));
                        esp.DrawLine(clr, 2.5, Vec2(x - pp2, top), Vec2(x - mx, top));
                        esp.DrawLine(clr, 2.5, Vec2(x + mx, top), Vec2(x + mx, top + pp2));
                        esp.DrawLine(clr, 2.5, Vec2(x - mx, top), Vec2(x - mx, top + pp2));
                        esp.DrawLine(clr, 2.5, Vec2(x + pp2, bottom), Vec2(x + mx, bottom));
                        esp.DrawLine(clr, 2.5, Vec2(x - pp2, bottom), Vec2(x - mx, bottom));
                        esp.DrawLine(clr, 2.5, Vec2(x - mx, bottom), Vec2(x - mx, bottom - pp2));
                        esp.DrawLine(clr, 2.5, Vec2(x + mx, bottom), Vec2(x + mx, bottom - pp2));
                    }
                    
                    // Line to Player
                    if (isPlayerLine) {
                        esp.DrawLine(clr, 2.5, Vec2(screenWidth / 2, screenHeight / 10.5),
                                    Vec2(response.Players[i].HeadLocation.x, top));
                    }

                    // Health Bar
                    if (isPlayerHealth) {
                        int Ihealth = 13;
                        float healthLength = screenWidth / Ihealth;
                        if (healthLength < mx) healthLength = mx;
                        
                        if (response.Players[i].Health < 25) {
                            clrHealth = player.isKnocked ? Color(0, 0, 0, 120) : Color(255, 0, 0, 150);
                        } else if (response.Players[i].Health < 50) {
                            clrHealth = player.isKnocked ? Color(0, 0, 0, 120) : Color(255, 165, 0, 150);
                        } else if (response.Players[i].Health < 75) {
                            clrHealth = player.isKnocked ? Color(0, 0, 0, 120) : Color(255, 255, 0, 150);
                        } else {
                            clrHealth = player.isKnocked ? Color(0, 0, 0, 120) : Color(0, 120, 0, 150);
                        }
                        
                        if (player.isKnocked) {
                            esp.DrawText(Color(255, 0, 0), OBFUSCATE("Knocked"),
                                        Vec2(x, top - screenHeight / 15.9), textsize);
                        }
                        
                        esp.DrawFilledRect(clrHealth,
                                          Vec2(x - healthLength, top - screenHeight / 30),
                                          Vec2(x - healthLength + (2 * healthLength) * response.Players[i].Health / 100,
                                               top - screenHeight / 110));
                        esp.DrawRect(Color(20,20,20), 1,
                                    Vec2(x - healthLength, top - screenHeight / 30),
                                    Vec2(x + healthLength, top - screenHeight / 110));
                    }
                    
                    // Player Name (Fixed Color().White() -> Color::White(255))
                    if (isPlayerName && response.Players[i].isBot) {
                        esp.DrawText(Color::White(255), OBFUSCATE(" BOT "),
                                    Vec2(response.Players[i].HeadLocation.x, top - screenHeight / 27), screenHeight / 60);
                    } else if (isPlayerName) {
                        esp.DrawName(Color::White(255), response.Players[i].PlayerNameByte,
                                    response.Players[i].TeamID,
                                    Vec2(x, top - screenHeight / 65), screenHeight / 60);
                    }

                    // Distance (Fixed isPlayerDist -> isPlayerDistance)
                    if (isPlayerDistance) {
                        sprintf(extra, "%0.0f m", response.Players[i].Distance);
                        esp.DrawText(Color(255,180,0), extra,
                                    Vec2(x, bottom + screenHeight / 60), screenHeight / 55);
                    }

                    // Weapon (Fixed DrawWeapon parameters and added isPlayerWeapon)
                    if (isPlayerWeapon && !player.isKnocked) {
                        esp.DrawWeapon(Color(247, 244, 200), response.Players[i].Weapon.id,
                                      response.Players[i].Weapon.ammo, response.Players[i].Weapon.ammo,
                                      Vec2(x, bottom + screenHeight / 27), screenHeight / 50);
                    }
                }

                // 360 Alert (Fixed isr360Alert -> is360Alert)
                if (is360Alert && isOutsideSafeZone(location, screen)) {
                    Vec2 hintDotRenderPos = pushToScreenBorder(location, screen, (mScaleY * 100) / 3, 5.0f);
                    esp.DrawFilledCircle(clrWarning, hintDotRenderPos, (mScaleY * 20));
                }
            }
        }

        // ========================================
        // GRENADE ESP
        // ========================================
        for (int i = 0; i < response.GrenadeCount; i++) {
            if (!isGrenadeWarning) continue;
            if (response.Grenade[i].Location.z >= 1.0f) {
                if (response.Grenade[i].type == 1) {
                    sprintf(extra, "Grenade (%0.0f m)", response.Grenade[i].Distance);
                    esp.DrawText(Color(255, 0, 0, 255), extra,
                                Vec2(response.Grenade[i].Location.x, response.Grenade[i].Location.y + 20), textsize);
                } else if (response.Grenade[i].type == 2) {
                    sprintf(extra, "Molotov (%0.0f m)", response.Grenade[i].Distance);
                    esp.DrawText(Color(255, 169, 0, 255), extra,
                                Vec2(response.Grenade[i].Location.x, response.Grenade[i].Location.y + 20), textsize);
                } else if (response.Grenade[i].type == 3) {
                    sprintf(extra, "Stun (%0.0f m)", response.Grenade[i].Distance);
                    esp.DrawText(Color(255, 255, 0, 255), extra,
                                Vec2(response.Grenade[i].Location.x, response.Grenade[i].Location.y + 20), textsize);
                } else if (response.Grenade[i].type == 4) {
                    sprintf(extra, "Smoke (%0.0f m)", response.Grenade[i].Distance);
                    esp.DrawText(Color(0, 255, 0, 255), extra,
                                Vec2(response.Grenade[i].Location.x, response.Grenade[i].Location.y + 20), textsize);
                }
            }

            // Fixed DrawOTH to only use position parameter
            esp.DrawOTH(Vec2(screenWidth / 2 - 160, 120));
            esp.DrawText(Color(255, 255, 255), OBFUSCATE("Warning Grenade"),
                        Vec2(screenWidth / 2 + 20, 145), 21);
            
            if (response.Grenade[i].Location.z >= 1.0f) {
                Color grenadeColor;
                if (response.Grenade[i].type == 1) grenadeColor = Color(255, 0, 0, 255);
                else if (response.Grenade[i].type == 2) grenadeColor = Color(255, 169, 0, 255);
                else if (response.Grenade[i].type == 3) grenadeColor = Color(255, 255, 0, 255);
                else grenadeColor = Color(0, 255, 0, 255);
                
                esp.DrawText(grenadeColor, "〇", Vec2(response.Grenade[i].Location.x, response.Grenade[i].Location.y), textsize);
            }
        }

        // ========================================
        // VEHICLE ESP
        // ========================================
        for(int i = 0; i < response.VehicleCount; i++){
            if(response.Vehicles[i].Location.z >= 1.0f) {
                esp.DrawVehicles(response.Vehicles[i].VehicleName, response.Vehicles[i].Distance,
                               response.Vehicles[i].Health, response.Vehicles[i].Fuel,
                               Vec2(response.Vehicles[i].Location.x, response.Vehicles[i].Location.y), screenHeight / 50);
            }
        }

        // ========================================
        // ITEM ESP
        // ========================================
        for (int i = 0; i < response.ItemsCount; i++) {
            if(response.Items[i].Location.z >= 1.0f) {
                esp.DrawItems(response.Items[i].ItemName,response.Items[i].Distance,
                             Vec2(response.Items[i].Location.x,response.Items[i].Location.y),textsize);
            }
        }
    }
    
    // Aimbot Circle
    if (options.openState == 0) {
        esp.DrawCircle(Color(255,0,0), Vec2(screenWidth / 2, screenHeight / 2), options.aimingRange, 1.5);
    }
    
    // ========================================
    // PLAYER COUNT DISPLAY (Fixed DrawOTH calls)
    // ========================================
    if (botCount + playerCount > 0) {
        char cn[10]; sprintf(cn,"%d",playerCount);
        char bt[10]; sprintf(bt,"%d",botCount);

        esp.DrawOTH(Vec2(screenWidth/2 - 80, 60));
        esp.DrawOTH(Vec2(screenWidth/2, 60));
        esp.DrawText(Color(255,255,255,255), cn, Vec2(screenWidth/2 - 20, 87), 23);
        esp.DrawText(Color(255,255,255,255), bt, Vec2(screenWidth/2 + 50, 87), 23);
    } else {
        esp.DrawOTH(Vec2(screenWidth/2 - 80, 60));
        esp.DrawOTH(Vec2(screenWidth/2, 60));
        esp.DrawText(Color(255,255,255,255), "0", Vec2(screenWidth/2 - 20, 87), 23);
        esp.DrawText(Color(255,255,255,255), "0", Vec2(screenWidth/2 + 50, 87), 23);
    }
}

#endif //HACKS_H 