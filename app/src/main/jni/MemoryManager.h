#pragma once

#include <cstdint>
#include <vector>
#include <string>
#include <memory>
#include <mutex>
#include <map>

/**
 * @brief BEAR-LOADER Memory Manager - High-level memory operations API
 * 
 * This class provides a unified interface for memory operations
 * such as reading, writing, and pattern scanning for game hacking.
 * Integrates with existing BearMundo security and protection systems.
 */
class MemoryManager {
public:
    /**
     * @brief Initialize the memory manager with BEAR security integration
     * 
     * @return true if initialization was successful
     * @return false if initialization failed
     */
    static bool initialize();

    /**
     * @brief Read memory from a specific address with security checks
     * 
     * @param address Address to read from  
     * @param buffer Buffer to store the read data
     * @param size Size of the data to read
     * @return true if reading was successful
     * @return false if reading failed
     */
    static bool readMemory(uintptr_t address, void* buffer, size_t size);

    /**
     * @brief Write memory to a specific address with protection bypass
     * 
     * @param address Address to write to
     * @param buffer Buffer containing the data to write
     * @param size Size of the data to write
     * @return true if writing was successful
     * @return false if writing failed
     */
    static bool writeMemory(uintptr_t address, const void* buffer, size_t size);

    /**
     * @brief Read primitive types with automatic endianness handling
     */
    template<typename T>
    static bool read(uintptr_t address, T& value) {
        return readMemory(address, &value, sizeof(T));
    }

    /**
     * @brief Write primitive types with automatic endianness handling
     */
    template<typename T>
    static bool write(uintptr_t address, const T& value) {
        return writeMemory(address, &value, sizeof(T));
    }

    /**
     * @brief Find a pattern in memory (supports wildcards with ?)
     * 
     * @param pattern Pattern to search for (e.g., "48 8B 05 ? ? ? ? 48 8B 08")
     * @param start Start address for the search
     * @param end End address for the search
     * @return Address where the pattern was found, or 0 if not found
     */
    static uintptr_t findPattern(const std::string& pattern, uintptr_t start, uintptr_t end);

    /**
     * @brief Find a pattern in a specific module (PUBG, UE4, etc.)
     * 
     * @param pattern Pattern to search for
     * @param moduleName Name of the module to search in
     * @return Address where the pattern was found, or 0 if not found
     */
    static uintptr_t findPatternInModule(const std::string& pattern, const std::string& moduleName);

    /**
     * @brief Get the base address of a module (game engine, libraries, etc.)
     * 
     * @param moduleName Name of the module (e.g., "libUE4.so", "libil2cpp.so")
     * @return Base address of the module, or 0 if not found
     */
    static uintptr_t getModuleBase(const std::string& moduleName);

    /**
     * @brief Get the size of a module
     * 
     * @param moduleName Name of the module
     * @return Size of the module, or 0 if not found
     */
    static size_t getModuleSize(const std::string& moduleName);

    /**
     * @brief BEAR-LOADER specific: Get PUBG Mobile engine base
     * 
     * @return Base address of PUBG Mobile engine, or 0 if not found
     */
    static uintptr_t getPUBGEngineBase();

    /**
     * @brief BEAR-LOADER specific: Get Unity/IL2CPP base for Unity games
     * 
     * @return Base address of IL2CPP, or 0 if not found
     */
    static uintptr_t getIL2CPPBase();

    /**
     * @brief Advanced pattern scanning with multiple results
     * 
     * @param pattern Pattern to search for
     * @param moduleName Module to search in
     * @param maxResults Maximum number of results to return
     * @return Vector of addresses where pattern was found
     */
    static std::vector<uintptr_t> findAllPatterns(const std::string& pattern, 
                                                  const std::string& moduleName, 
                                                  size_t maxResults = 10);

    /**
     * @brief Memory scanning for value ranges (useful for ESP/Aimbot)
     * 
     * @param startAddr Start address for scanning
     * @param endAddr End address for scanning  
     * @param minValue Minimum value to search for
     * @param maxValue Maximum value to search for
     * @return Vector of addresses containing values in range
     */
    static std::vector<uintptr_t> scanValueRange(uintptr_t startAddr, uintptr_t endAddr,
                                                 float minValue, float maxValue);

    /**
     * @brief Get memory protection status of an address
     * 
     * @param address Address to check
     * @return Protection flags (PROT_READ, PROT_WRITE, PROT_EXEC)
     */
    static int getMemoryProtection(uintptr_t address);

    /**
     * @brief Temporarily change memory protection for writing
     * 
     * @param address Address to modify protection
     * @param size Size of region
     * @param newProtection New protection flags
     * @return Original protection flags, or -1 on failure
     */
    static int changeProtection(uintptr_t address, size_t size, int newProtection);

    /**
     * @brief Restore original memory protection
     * 
     * @param address Address to restore
     * @param size Size of region
     * @param originalProtection Original protection flags
     * @return true if successful
     */
    static bool restoreProtection(uintptr_t address, size_t size, int originalProtection);

    /**
     * @brief Get current process memory maps (for debugging)
     * 
     * @return Vector of memory map entries
     */
    static std::vector<std::string> getMemoryMaps();

    /**
     * @brief BEAR-LOADER integration: Enable stealth memory operations
     * 
     * @param enabled Whether to enable stealth mode
     */
    static void enableStealthMode(bool enabled);

    /**
     * @brief Get statistics about memory operations
     * 
     * @return String containing operation statistics
     */
    static std::string getStatistics();

    /**
     * @brief Clean up and shutdown memory manager
     */
    static void shutdown();

private:
    // Prevent instantiation
    MemoryManager() = delete;
    ~MemoryManager() = delete;

    // Internal state
    static bool initialized;
    static bool stealthMode;
    static std::mutex operationMutex;
    static std::map<std::string, uintptr_t> moduleCache;
    
    // Statistics tracking
    static uint64_t readOperations;
    static uint64_t writeOperations;  
    static uint64_t patternScans;
    static uint64_t moduleQueries;

    // Internal utility functions
    static std::vector<uint8_t> parsePattern(const std::string& pattern);
    static bool matchPattern(const uint8_t* data, const std::vector<uint8_t>& pattern, 
                           const std::vector<bool>& mask);
    static bool isAddressReadable(uintptr_t address, size_t size);
    static bool isAddressWritable(uintptr_t address, size_t size);
    static void updateStatistics(const std::string& operation);
    
    // BEAR-LOADER integration
    static bool performSecurityCheck();
    static void logMemoryOperation(const std::string& operation, uintptr_t address, size_t size);
};

/**
 * @brief RAII helper for temporary memory protection changes
 */
class MemoryProtectionGuard {
public:
    MemoryProtectionGuard(uintptr_t addr, size_t sz, int newProt);
    ~MemoryProtectionGuard();
    
    bool isValid() const { return valid; }

private:
    uintptr_t address;
    size_t size;
    int originalProtection;
    bool valid;
}; 