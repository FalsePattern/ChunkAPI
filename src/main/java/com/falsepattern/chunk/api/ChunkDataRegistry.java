/*
 * -------------------------------------------------------------------------------
 * @author FalsePattern
 *
 * Copyright 2023
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 * -------------------------------------------------------------------------------
 */

package com.falsepattern.chunk.api;

import com.falsepattern.chunk.internal.ChunkDataRegistryImpl;
import com.falsepattern.lib.StableAPI;

import java.util.Set;

/**
 * This class is used to register ChunkDataManagers.
 *
 * @since 0.1.0
 * @see ChunkDataManager
 * @author FalsePattern
 * @version 0.1.0
 */
@StableAPI(since = "0.1.0")
public class ChunkDataRegistry {
    /**
     * Registers a ChunkDataManager. Only do this during the preInit phase.
     *
     * @param manager The manager to register.
     *
     * @throws IllegalStateException    If the registration stage is over.
     * @throws IllegalArgumentException If the manager has a duplicate id.
     */
    @StableAPI.Expose
    public static void registerDataManager(ChunkDataManager manager)
            throws IllegalStateException, IllegalArgumentException {
        ChunkDataRegistryImpl.registerDataManager(manager);
    }

    /**
     * Disables a ChunkDataManager. Only do this during the preInit phase.
     *
     * @param id The id of the manager to disable.
     *
     * @throws IllegalStateException If the disable stage is over.
     */
    @StableAPI.Expose
    public static void disableDataManager(String domain, String id) throws IllegalStateException {
        ChunkDataRegistryImpl.disableDataManager(domain, id);
    }

    /**
     * Returns an unmodifiable set of all registered ChunkDataManagers.
     * The id of a manager is its domain and id separated by a colon. (domain:id)
     */
    @StableAPI.Expose
    public static Set<String> getRegisteredManagers() {
        return ChunkDataRegistryImpl.getRegisteredManagers();
    }
}
