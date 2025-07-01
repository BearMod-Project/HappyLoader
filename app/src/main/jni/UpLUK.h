#ifndef UpLUK_h
#define UpLUK_h

#include <cstdint>

namespace Offsets64 {
    uintptr_t GNames = 0xb849220;
    uintptr_t GWorld = 0xb8a2a70;
    uintptr_t ViewMatrix = 0xbca91a0;

    uintptr_t RelativeLocation = 0x17c;
    uintptr_t World = 0x78; //Class: GameViewportClient.ScriptViewportClient.Object to World* World;//[Offset:
    uintptr_t ViewMatrixLevel = 0xc0;//Manual Pointer
    uintptr_t RootComponent = 0x1b0; // SceneComponent* RootComponent
    uintptr_t SkeletalMeshComponent = 0x458; // SkeletalMeshComponent* Mesh
    uintptr_t CharacterMovementComponent = 0x460;
    uintptr_t FixAttachInfoList = 0x1a0; // FixAttachInfoList
    uintptr_t MinLOD = 0x7a8; // StaticMesh* StaticMesh;//[Offset:
    uintptr_t Health = 0xd18; // float Health
    uintptr_t NearDeathBreath = 0x1878; // float NearDeathBreath
    uintptr_t NearDeathComponent = 0x1858; // STCharacterNearDeathComp* NearDeatchComponent
    uintptr_t BreathMax = 0x164; // float BreathMax
    uintptr_t bDead = 0xd34; // bool bDead
    //Class: UAECharacter.Character.Pawn.Actor.Object
    uintptr_t PlayerName = 0x8b0; // FString PlayerName
    uintptr_t TeamID = 0x8f8; // int TeamID
    uintptr_t Role = 0x150;//byte Role
    uintptr_t bIsAI = 0x990; // bool bIsAI
    // Class: STExtraWeapon.Actor.Object
    uintptr_t ActorChildren = 0x1a0; // Actor*[] Children
    uintptr_t WeaponEntityComp = 0x7b8; //WeaponEntity* WeaponEntityComp
    ///Class: WeaponEntity.WeaponLogicBaseComponent.ActorComponent.Object
    uintptr_t DrawShootLineTime = 0x13C; // DrawShootLineTime
    uintptr_t UploadInterval = 0x170; // UploadInterval
    uintptr_t CurBulletNumInClip = 0xe88; // int CurBulletNumInClip
    uintptr_t CurMaxBulletNumInOneClip = 0xea8;// int CurMaxBulletNumInOneClip
    uintptr_t bIsGunADS = 0xfa9;// bool bIsGunADS
    uintptr_t bIsWeaponFiring = 0x1560; //bool bIsWeaponFiring
    uintptr_t STPlayerController = 0x3b08; //STExtraPlayerController* STPlayerController
    uintptr_t PlayerCameraManager = 0x490;//PlayerCameraManager* PlayerCameraManager
    uintptr_t CameraCacheEntry = 0x470;//CameraCacheEntry CameraCache;//[Offset:
    uintptr_t ViewPitchMin = 0x1c5c; //float ViewPitchMin;//[Offset:
    uintptr_t ViewPitchMax = 0x1c60; //float ViewPitchMax;//[Offset:
    uintptr_t ViewYawMin = 0x1c64;//float ViewYawMin;//[Offset:
    uintptr_t ViewYawMax = 0x1c68;//float ViewYawMax;//[Offset:

    //Vehicles HP
    uintptr_t VehicleCommon = 0x9b8; //VehicleCommonComponent* VehicleCommon;//[Offset:
    uintptr_t VHealth = 0x2a4; //float HP;//[Offset:
    uintptr_t VHealthMax = 0x2a0;//float HPMax;//[Offset:
    uintptr_t VFuel = 0x2c8; //float Fuel;//[Offset:
    uintptr_t VFuelMax = 0x2c4; //float FuelMax;//[Offset:
    uintptr_t CurrentVehicle = 0xd60;//STExtraVehicleBase* CurrentVehicle;//[Offset:
    uintptr_t ReplicatedMovement = 0xb0;
    uintptr_t Velocity = 0x124;//Vector Velocity;//[Offset:
    uintptr_t cLoc = 0x750; //Pointer Manual
    uintptr_t fovPntr = 0x660; //Pointer Manual
    uintptr_t vMatrix = 0x590; //Pointer Manual
    uintptr_t CameraComponent = 0x19c0; //CameraComponent* ThirdPersonCameraComponent
    uintptr_t FieldOfView = 0x2cc; //float FieldOfView
    uintptr_t WeaponManagerComponent = 0x2158; //CharacterWeaponManagerComponent* WeaponManagerComponent
    uintptr_t CurrentWeaponReplicated = 0x4e8; //STExtraWeapon* CurrentWeaponReplicated
    uintptr_t ShootWeaponEntityComp = 0xfe8;  //ShootWeaponEntity* ShootWeaponEntityComp

    //Class: ShootWeaponEntity.WeaponEntity.WeaponLogicBaseComponent.ActorComponent.Object
    uintptr_t BulletFireSpeed = 0x4e0;
    uintptr_t AccessoriesVRecoilFactor = 0xab8;
    uintptr_t AccessoriesHRecoilFactor = 0xabc;
    uintptr_t AccessoriesRecoveryFactor = 0xac0;
    uintptr_t RecoilKickADS = 0xbe8;
    uintptr_t ExtraHitPerformScale = 0xbec;
    uintptr_t ShootInterval = 0x518;
    uintptr_t GameDeviationFactor = 0xb30;
    uintptr_t AnimationKick = 0xc04;
    uintptr_t AutoAimingConfig = 0x920;
    uintptr_t SwitchWeaponSpeedScale = 0x2628; //float SwitchWeaponSpeedScale

    // Visiblechek -> PrimitiveComponent.SceneComponent.ActorComponent.Object
    uintptr_t LastRenderTime = 0x38c;
    uintptr_t LastRenderTimeOnScreen = 0x390;
    uintptr_t AcknowledgedPawn = 0x470; //Pawn* AcknowledgedPawn;//[Offs
}

#endif //UpLUK 