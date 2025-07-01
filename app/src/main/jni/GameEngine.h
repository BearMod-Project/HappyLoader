#ifndef GAMEENGINE_H
#define GAMEENGINE_H

#include "Memory.h"
#include "UpLUK.h"

// ========================================
// GAME ENGINE STRUCTURES
// ========================================

struct ActorsEncryption {
    uint64_t Enc_1, Enc_2;
    uint64_t Enc_3, Enc_4;
};

struct Encryption_Chunk {
    uint32_t val_1, val_2, val_3, val_4;
    uint32_t val_5, val_6, val_7, val_8;
};

// ========================================
// FUNCTION DECLARATIONS
// ========================================

uint64_t DecryptActorsArray(uint64_t uLevel, int Actors_Offset, int EncryptedActors_Offset);
void p_write(uintptr_t address, void *buffer, int size);
Vec2 getPointingAngle(Vec3 camera, Vec3 xyz, float distance);
Vec3 Multiply_VectorFloat(const Vec3 &Vec, float Scale);
Vec3 Add_VectorVector(struct Vec3 Vect1, struct Vec3 Vect2);
uintptr_t getMatrix(uintptr_t base);
uintptr_t getEntityAddr(uintptr_t base);

// ========================================
// GLOBAL VARIABLES
// ========================================

extern uintptr_t GWorld;
extern uintptr_t GNames; 
extern uintptr_t ViewMatrix;

#endif // GAMEENGINE_H 