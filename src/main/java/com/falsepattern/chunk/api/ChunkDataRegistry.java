package com.falsepattern.chunk.api;

import com.falsepattern.chunk.internal.ChunkDataRegistryImpl;
import com.falsepattern.chunk.api.exception.ManagerRegistrationException;

/**
 * This class is used to register ChunkDataManagers.
 */
public class ChunkDataRegistry {
    public static void registerDataManager(ChunkDataManager manager) throws ManagerRegistrationException {
        ChunkDataRegistryImpl.registerDataManager(manager);
    }

    public static void disableDataManager(String id) {
        ChunkDataRegistryImpl.disableDataManager(id);
    }
}
