#include "INCLUDE/struct.h"
#include "Offsets.h"
#include "UpLUK.h"
#include "Memory.h"

struct ActorsEncryption {
    uint64_t Enc_1, Enc_2;
    uint64_t Enc_3, Enc_4;
};

struct Encryption_Chunk {
    uint32_t val_1, val_2, val_3, val_4;
    uint32_t val_5, val_6, val_7, val_8;
};

uint64_t DecryptActorsArray(uint64_t uLevel, int Actors_Offset, int EncryptedActors_Offset) {
    if (uLevel < 0x10000000)
        return 0;

    if (ReadMemory<uint64_t>(uLevel + Actors_Offset) > 0)
        return uLevel + Actors_Offset;

    if (ReadMemory<uint64_t>(uLevel + EncryptedActors_Offset) > 0)
        return uLevel + EncryptedActors_Offset;

    auto Encryption = ReadMemory<ActorsEncryption>(uLevel + EncryptedActors_Offset + 0x10);

    if (Encryption.Enc_1 > 0) {
        auto Enc = ReadMemory<Encryption_Chunk>(Encryption.Enc_1 + 0x80);
        return (((ReadMemory<uint8_t>(Encryption.Enc_1 + Enc.val_1)
                  | (ReadMemory<uint8_t>(Encryption.Enc_1 + Enc.val_2) << 8))
                 | (ReadMemory<uint8_t>(Encryption.Enc_1 + Enc.val_3) << 0x10)) & 0xFFFFFF
                | ((uint64_t) ReadMemory<uint8_t>(Encryption.Enc_1 + Enc.val_4) << 0x18)
                | ((uint64_t) ReadMemory<uint8_t>(Encryption.Enc_1 + Enc.val_5) << 0x20)) &
               0xFFFF00FFFFFFFFFF
               | ((uint64_t) ReadMemory<uint8_t>(Encryption.Enc_1 + Enc.val_6) << 0x28)
               | ((uint64_t) ReadMemory<uint8_t>(Encryption.Enc_1 + Enc.val_7) << 0x30)
               | ((uint64_t) ReadMemory<uint8_t>(Encryption.Enc_1 + Enc.val_8) << 0x38);
    } else if (Encryption.Enc_2 > 0) {
        auto Encrypted_Actors = ReadMemory<uint64_t>(Encryption.Enc_2);
        if (Encrypted_Actors > 0) {
            return (uint16_t) (Encrypted_Actors - 0x400) & 0xFF00
                   | (uint8_t) (Encrypted_Actors - 0x04)
                   | (Encrypted_Actors + 0xFC0000) & 0xFF0000
                   | (Encrypted_Actors - 0x4000000) & 0xFF000000
                   | (Encrypted_Actors + 0xFC00000000) & 0xFF00000000
                   | (Encrypted_Actors + 0xFC0000000000) & 0xFF0000000000
                   | (Encrypted_Actors + 0xFC000000000000) & 0xFF000000000000
                   | (Encrypted_Actors - 0x400000000000000) & 0xFF00000000000000;
        }
    } else if (Encryption.Enc_3 > 0) {
        auto Encrypted_Actors = ReadMemory<uint64_t>(Encryption.Enc_3);
        if (Encrypted_Actors > 0)
            return (Encrypted_Actors >> 0x38) | (Encrypted_Actors << (64 - 0x38));
    } else if (Encryption.Enc_4 > 0) {
        auto Encrypted_Actors = ReadMemory<uint64_t>(Encryption.Enc_4);
        if (Encrypted_Actors > 0)
            return Encrypted_Actors ^ 0xCDCD00;
    }
    return 0;
}

void p_write(uintptr_t address, void *buffer, int size) {
    struct iovec local[1];
    struct iovec remote[1];
    local[0].iov_base = (void *) buffer;
    local[0].iov_len = size;
    remote[0].iov_base = (void *) address;
    remote[0].iov_len = size;
    process_vm_writev(pid, local, 1, remote, 1, 0);
}

Vec2 getPointingAngle(Vec3 camera, Vec3 xyz, float distance)
{
    Vec2 PointingAngle;
    float ytime = distance / 88000;
    xyz.Z = xyz.Z + 360 * ytime * ytime;
    float zbcx = xyz.X - camera.X;
    float zbcy = xyz.Y - camera.Y;
    float zbcz = xyz.Z - camera.Z;
    PointingAngle.Y = atan2(zbcy, zbcx) * 180 / PI;
    PointingAngle.X = atan2(zbcz, sqrt(zbcx * zbcx + zbcy * zbcy)) * 180 / PI;
    return PointingAngle;
}

Vec3 Multiply_VectorFloat(const Vec3 &Vec, float Scale) {
    Vec3 multiply = {Vec.X * Scale, Vec.Y * Scale, Vec.Z * Scale};
    return multiply;
}

Vec3 Add_VectorVector(struct Vec3 Vect1, struct Vec3 Vect2) {
    Vec3 add;
    add.X = Vect1.X + Vect2.X;
    add.Y = Vect1.Y + Vect2.Y;
    add.Z = Vect1.Z + Vect2.Z;
    return add;
}

uintptr_t GWorld = 0, GNames = 0, ViewMatrix = 0;

uintptr_t getMatrix(uintptr_t base) {
    return getA(getA(base + 0xbca91a0) + 0xc0);
}

uintptr_t getEntityAddr(uintptr_t base) {
    uintptr_t level = getA(getA(getA(getA(base + 0xbca91a0) + 0x58) + 0x78) + 0x30);
    return DecryptActorsArray(level, 0xA0, 0x448);
}

PlayerBone getPlayerBone(uintptr_t pBase, struct D3DMatrix viewMatrix) {
    PlayerBone b;
    b.isBone = true;
    struct D3DMatrix oMatrix;
    uintptr_t boneAddr = getA(pBase + Offsets64::SkeletalMeshComponent);
    struct D3DMatrix baseMatrix = getOMatrix(boneAddr + Offsets64::FixAttachInfoList);
    boneAddr = getA(boneAddr + Offsets64::MinLOD) + 0x30;
    // neck 0
    oMatrix = getOMatrix(boneAddr + 4 * 48);
    b.neck = World2ScreenMain(viewMatrix, mat2Cord(oMatrix, baseMatrix));
    // cheast 1
    oMatrix = getOMatrix(boneAddr + 4 * 48);
    b.cheast = World2ScreenMain(viewMatrix, mat2Cord(oMatrix, baseMatrix));
    // pelvis 2
    oMatrix = getOMatrix(boneAddr + 1 * 48);
    b.pelvis = World2ScreenMain(viewMatrix, mat2Cord(oMatrix, baseMatrix));
    // lSh 3
    oMatrix = getOMatrix(boneAddr + 11 * 48);
    b.lSh = World2ScreenMain(viewMatrix, mat2Cord(oMatrix, baseMatrix));
    // rSh 4
    oMatrix = getOMatrix(boneAddr + 32 * 48);
    b.rSh = World2ScreenMain(viewMatrix, mat2Cord(oMatrix, baseMatrix));
    // lElb 5
    oMatrix = getOMatrix(boneAddr + 12 * 48);
    b.lElb = World2ScreenMain(viewMatrix, mat2Cord(oMatrix, baseMatrix));
    // rElb 6
    oMatrix = getOMatrix(boneAddr + 33 * 48);
    b.rElb = World2ScreenMain(viewMatrix, mat2Cord(oMatrix, baseMatrix));
    // lWr 7
    oMatrix = getOMatrix(boneAddr + 63 * 48);
    b.lWr = World2ScreenMain(viewMatrix, mat2Cord(oMatrix, baseMatrix));
    // rWr 8
    oMatrix = getOMatrix(boneAddr + 62 * 48);
    b.rWr = World2ScreenMain(viewMatrix, mat2Cord(oMatrix, baseMatrix));
    // lTh 9
    oMatrix = getOMatrix(boneAddr + 52 * 48);
    b.lTh = World2ScreenMain(viewMatrix, mat2Cord(oMatrix, baseMatrix));
    // rTh 10
    oMatrix = getOMatrix(boneAddr + 56 * 48);
    b.rTh = World2ScreenMain(viewMatrix, mat2Cord(oMatrix, baseMatrix));
    // lKn 11
    oMatrix = getOMatrix(boneAddr + 53 * 48);
    b.lKn = World2ScreenMain(viewMatrix, mat2Cord(oMatrix, baseMatrix));
    // rKn 12
    oMatrix = getOMatrix(boneAddr + 57 * 48);
    b.rKn = World2ScreenMain(viewMatrix, mat2Cord(oMatrix, baseMatrix));
    // lAn 13 
    oMatrix = getOMatrix(boneAddr + 54 * 48);
    b.lAn = World2ScreenMain(viewMatrix, mat2Cord(oMatrix, baseMatrix));
    // rAn 14
    oMatrix = getOMatrix(boneAddr + 58 * 48);
    b.rAn = World2ScreenMain(viewMatrix, mat2Cord(oMatrix, baseMatrix));

    return b;
}

PlayerWeapon getPlayerWeapon(uintptr_t base) {
    PlayerWeapon p;
    uintptr_t addr[3];
    pvm(getA(base + Offsets64::ActorChildren), addr, sizeof(addr));

    if (isValid64(addr[0]) && getI(addr[0] + Offsets64::DrawShootLineTime) == 2) {
        p.isWeapon = true;
        p.id = getI(getA(addr[0] + Offsets64::WeaponEntityComp) + Offsets64::UploadInterval);
        p.ammo = getI(addr[0] + Offsets64::CurBulletNumInClip);
        p.ammo2 = getI(addr[0] + Offsets64::CurBulletNumInClip);
    } else if (isValid64(addr[1]) && getI(addr[1] + Offsets64::DrawShootLineTime) == 2) {
        p.isWeapon = true;
        p.id = getI(getA(addr[1] + Offsets64::WeaponEntityComp) + Offsets64::UploadInterval);
        p.ammo = getI(addr[1] + Offsets64::CurBulletNumInClip);
        p.ammo2 = getI(addr[1] + Offsets64::CurBulletNumInClip);
    } else if (isValid64(addr[2]) && getI(addr[2] + Offsets64::DrawShootLineTime) == 2) {
        p.isWeapon = true;
        p.id = getI(getA(addr[2] + Offsets64::WeaponEntityComp) + Offsets64::UploadInterval);
        p.ammo = getI(addr[2] + Offsets64::CurBulletNumInClip);
        p.ammo2 = getI(addr[2] + Offsets64::CurBulletNumInClip);
    }
    return p;
}

struct ShootWeaponBase {
    uintptr_t FromBase;
    uintptr_t Base;
    uintptr_t ShootWeaponEntity;
    int bIsWeaponFiring;

    ShootWeaponBase(uintptr_t pBase) {
        FromBase = getA(pBase + Offsets64::WeaponManagerComponent);
        Base = getA(FromBase + Offsets64::CurrentWeaponReplicated);
        ShootWeaponEntity = getA(Base + Offsets64::ShootWeaponEntityComp);
        bIsWeaponFiring = getI(pBase + Offsets64::bIsWeaponFiring);
    }

    void setInstantHit() {
        Write(ShootWeaponEntity + Offsets64::BulletFireSpeed, "900000", TYPE_FLOAT);
    }

    void setLessRecoil() {
        Write(ShootWeaponEntity + Offsets64::AccessoriesVRecoilFactor, "0", TYPE_FLOAT);
        Write(ShootWeaponEntity + Offsets64::AccessoriesHRecoilFactor, "0", TYPE_FLOAT);
        Write(ShootWeaponEntity + Offsets64::AccessoriesRecoveryFactor, "0", TYPE_FLOAT);
    }

    void setZeroRecoil() {
        Write(ShootWeaponEntity + Offsets64::RecoilKickADS, "0", TYPE_FLOAT);
    }

    void setFastShootInterval() {
        Write(ShootWeaponEntity + Offsets64::ShootInterval, "0.048000", TYPE_FLOAT);
    }

    void setSmallCrosshair() {
        Write(ShootWeaponEntity + Offsets64::GameDeviationFactor, "0", TYPE_FLOAT);
    }

    void setHitX() {
        Write(ShootWeaponEntity + Offsets64::ExtraHitPerformScale, "50", TYPE_FLOAT);
    }

    void setNoShake() {
        Write(ShootWeaponEntity + Offsets64::AnimationKick, "0", TYPE_FLOAT);
    }

    void setFastSwitchWeapon() {
        Write(ShootWeaponEntity + Offsets64::SwitchWeaponSpeedScale, "100", TYPE_FLOAT);
    }

    void setAimlock() {
        if (getI(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x0) != 0) {
            Write2<float>(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x0, 4.0);
            if (getI(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x8) != 0) {
                Write2<float>(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x8, 3);
                if (getI(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0xC) != 0) {
                    Write2<float>(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0xC, 3);
                    if (getI(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x10) != 0) {
                        Write2<float>(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x10,
                                      3);
                        if (getI(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x14) != 0) {
                            Write2<float>(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x14,
                                          3);
                            if (getI(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x38) != 0) {
                                Write2<float>(
                                        ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x38,
                                        205);
                                if (getI(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x3C) !=
                                    0) {
                                    Write2<float>(
                                            ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x3C,
                                            1);
                                    if (getI(ShootWeaponEntity + Offsets64::AutoAimingConfig +
                                             0x40) != 0) {
                                        Write2<float>(
                                                ShootWeaponEntity + Offsets64::AutoAimingConfig +
                                                0x40, 120);
                                        if (getI(ShootWeaponEntity + Offsets64::AutoAimingConfig +
                                                 0x44) != 0) {
                                            Write2<float>(ShootWeaponEntity +
                                                          Offsets64::AutoAimingConfig + 0x44, 8200);
                                            if (getI(ShootWeaponEntity +
                                                     Offsets64::AutoAimingConfig + 0x48) != 0) {
                                                Write2<float>(ShootWeaponEntity +
                                                              Offsets64::AutoAimingConfig + 0x48,
                                                              1);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    void setAimbot() {
        if (getI(ShootWeaponEntity + Offsets64::AutoAimingConfig) != 0) {
            if (getF(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x0) != 8.0)
                Write(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x0, "1090519040",
                      TYPE_DWORD);

            if (getF(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x4c) != 0) {
                if (getF(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x4c) != 8.0)
                    Write(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x4c, "1090519040",
                          TYPE_DWORD);

                if (getF(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x4) != 0) {
                    if (getF(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x4) != 4.0)
                        Write(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x4, "1082130432",
                              TYPE_DWORD);

                    if (getF(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x50) != 0) {
                        if (getF(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x50) != 4.0)
                            Write(ShootWeaponEntity + Offsets64::AutoAimingConfig + 0x50,
                                  "1082130432", TYPE_DWORD);

                    }
                }
            }
        }
    }

    bool isFiring() {
        return (bIsWeaponFiring != 0);
    }

    bool isValid() {
        return (Base != 0);
    }
};

struct SceneComponent {
    uintptr_t CameraComponent;

    SceneComponent(uintptr_t pBase) {
        CameraComponent = getA(pBase + Offsets64::CameraComponent);
    }

    void setWideView() {
        Write(CameraComponent + Offsets64::FieldOfView, "110", TYPE_FLOAT);
    }

    bool isValid() {
        return (CameraComponent != 0);
    }
};

int gameEngineMain() {
    SetValue sv{};
    if (!isBeta) {
        if (!Create()) {
            perror("Creation Failed");
            return 0;
        }
        if (!Connect()) {
            perror("Connection Failed");
            return 0;
        }
        int no;

        receive((void *) &sv);
        no = sv.mode;

        if (isapkrunning("com.pubg.imobile") == 1) {
            GNames = Offsets64::GNames;
            GWorld = Offsets64::GWorld;
            ViewMatrix = Offsets64::ViewMatrix;
            strcpy(version, "com.pubg.imobile");
        } else if (isapkrunning("com.pubg.imobile") == 1) {
            GNames = Offsets64::GNames;
            GWorld = Offsets64::GWorld;
            ViewMatrix = Offsets64::ViewMatrix;
            strcpy(version, "com.pubg.imobile");
        } else if (isapkrunning("com.pubg.imobile") == 1) {
            GNames = Offsets64::GNames;
            GWorld = Offsets64::GWorld;
            ViewMatrix = Offsets64::ViewMatrix;
            strcpy(version, "com.pubg.imobile");
        } else if (isapkrunning("com.pubg.imobile") == 1) {
            GNames = Offsets64::GNames;
            GWorld = Offsets64::GWorld;
            ViewMatrix = Offsets64::ViewMatrix;
            strcpy(version, "com.pubg.imobile");
        }
    }
    
    pid = getPid(version);
    if (pid < 10) {
        printf("\nGame is not running");
        Close();
        return 0;
    }
    if (isBeta == 1)
        printf("\n Game Pid: %d", pid);
    isPremium = true;
    uintptr_t base = getBase();
    if (isBeta == 1)
        printf("\n Base: %lX\n", base);
    uintptr_t vMatrix = getMatrix(base);
    if (!vMatrix) {
        if (isBeta == 1)
            puts("\nMatrix Not Found");
        return 0;
    }
    if (isBeta == 1)
        printf("\nvMatrix: %lX", vMatrix);

    uintptr_t enAddrPntr;
    float xy0, xy1;
    struct Vec3 xyz;
    struct Vec3 screen;
    struct Vec3 exyz;
    int isBack = 0, type = 69;
    int changed = 1;
    int myteamID = 101;
    uintptr_t cLoc = vMatrix + Offsets64::cLoc;
    uintptr_t fovPntr = vMatrix + Offsets64::fovPntr;
    vMatrix = vMatrix + Offsets64::vMatrix;
    char loaded[0x4000], loadedpn[20];
    char name[100];
    uintptr_t gname_buff[30];
    uintptr_t gname = getA(getA(base + 0xb849220) + 0x110);
    pvm(gname, &gname_buff, sizeof(gname_buff));
    int isVisible = 0;
    char cont[0x500];
    char boneData[1024];
    struct D3DMatrix vMat;
    char weaponData[100];
    bool firing = false, ads = false; //adsfiring = false;
    bool AimTriger;
    float aimRadius = 200;
    float recoil;
    float aimDist = 200;
    float bSpeed = 600;
    struct Vec3 pointingAngle;
    uintptr_t cameraManager;
    struct Vec3 cam;
    struct Vec3 targetLoc;
    uintptr_t yawPitch = 0;
    uintptr_t localPlayer = 0;
    uint closestTargetUID;
    uint lastTargetUID;
    uintptr_t localPKey = 0;

    int aimFor = 1;
    bool aimKnoced = false;
    bool ignoreBot = false;
    int aimBy = 1;
    int aimWhen = 3;
    bool aimbot = false;

    struct Vec2 RadarPosition = {width / 2, height / 2};
    float RadarSize = 125;
    struct FCameraCacheEntry CameraCache;
    Request request{};
    Response response{};

    bool isLessRecoil = false;
    bool isZeroRecoil = false;
    bool isInstantHit = false;
    bool isFastShootInterval = false;
    bool isSmallCrosshair = false;
    bool isHitX = false;
    bool isNoShake = false;
    bool isWideView = false;
    bool isAimbot = false;
    bool isAimlock = false;
    bool isFastSwitchWeapon = false;

    while (isBeta || (receive((void *) &request) > 0)) {

        if (!isBeta) {
            height = request.ScreenHeight;
            width = request.ScreenWidth;
        }
        if (request.Mode == InitMode) {
            aimRadius = (float) request.options.aimingRange;
            recoil = (float) request.options.Recoil;
            aimDist = (float) request.options.aimingDistance;
            bSpeed = (float) request.options.bulletSpeed;
            aimFor = request.options.aimbotmode;
            aimbot = request.options.openState == 0;
            aimWhen = request.options.aimingState;
            aimBy = request.options.priority;
            aimKnoced = request.options.pour;
            ignoreBot = request.options.iBot;
            isLessRecoil = request.memory.LessRecoil;
            isZeroRecoil = request.memory.ZeroRecoil;
            isInstantHit = request.memory.InstantHit;
            isFastShootInterval = request.memory.FastShootInterval;
            isHitX = request.memory.HitX;
            isNoShake = request.memory.NoShake;
            isSmallCrosshair = request.memory.SmallCrosshair;
            isWideView = request.memory.WideView;
            isAimbot = request.memory.Aimbot;
            isAimlock = request.memory.Aimlock;
            isFastSwitchWeapon = request.memory.FastSwitchWeapon;
            RadarPosition = request.radarPos;
            RadarSize = request.radarSize;
        }
        
        response.Success = false;
        response.PlayerCount = 0;
        response.VehicleCount = 0;
        response.ItemsCount = 0;
        response.GrenadeCount = 0;
        response.ZoneCount = 0;
        response.InLobby = false;

        pvm(cLoc, &xyz, sizeof(xyz));

        if ((xyz.Z == 88.441124f || xyz.X == 0 || xyz.Z == 5278.43f || xyz.Z == 88.440918f) &&
            !isBeta) {
            changed = 1;
            send((void *) &response, sizeof(response));
            continue;
        }
        pvm(fovPntr, &response.fov, 4);
        pvm(vMatrix, &vMat, sizeof(vMat));
        if (isBeta)
            printf("\nvMatChk: %0.1f | FOV: %0.2f | XYZ: %f %f %f", vMat._43, response.fov, xyz.X,
                   xyz.Y, xyz.Z);
        if (changed == 1) {
            enAddrPntr = getEntityAddr(base);
            changed = 0;
        }
        Ulevel ulevel;
        pvm(enAddrPntr, &ulevel, sizeof(ulevel));
        if (ulevel.size < 1 || ulevel.size > 0x1000 || !isValid64(ulevel.addr)) {
            if (isBeta)
                puts("\nWrong Entity Address");
            changed = 1;
            if (!isBeta) {
                send((void *) &response, sizeof(response));
                continue;
            }
        }

        if (isBeta)
            printf("\nEntity Address: %lX | Size: %d", enAddrPntr, ulevel.size);
        memset(loaded, 0, 1000);
        float nearest = -1.0f;
        
        // Main entity processing loop would continue here...
        // [Rest of implementation truncated for brevity - this is a very large function]
        
        if (response.PlayerCount + response.ItemsCount + response.VehicleCount +
            response.GrenadeCount + response.ZoneCount > 0)
            response.Success = true;
        send((void *) &response, sizeof(response));
    }

    if (isBeta)
        puts("\n\nScript Completed ");
    
    return 0;
} 