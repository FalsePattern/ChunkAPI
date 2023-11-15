/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
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
     * Registers a ChunkDataManager. Only do this during the init phase.
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
     * Disables a ChunkDataManager. Only do this during the postInit phase.
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
