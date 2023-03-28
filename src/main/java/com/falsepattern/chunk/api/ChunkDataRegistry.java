package com.falsepattern.chunk.api;

import com.falsepattern.chunk.internal.ChunkDataRegistryImpl;
import com.falsepattern.chunk.api.exception.ManagerRegistrationException;

/**
 * This class is used to register ChunkDataManagers.
 * @see ChunkDataManager
 */
public class ChunkDataRegistry {
    /**
     * Registers a ChunkDataManager. Only do this during the preInit phase.
     * @param manager The manager to register.
     * @throws ManagerRegistrationException If the manager is already registered.
     */
    public static void registerDataManager(ChunkDataManager<?> manager) throws ManagerRegistrationException {
        ChunkDataRegistryImpl.registerDataManager(manager);
    }

    /**
     * Disables a ChunkDataManager. Only do this during the preInit phase.
     * @param id The id of the manager to disable.
     */
    public static void disableDataManager(String id) {
        ChunkDataRegistryImpl.disableDataManager(id);
    }
}
