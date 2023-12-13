/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.chunk.api;

import com.falsepattern.chunk.internal.DataRegistryImpl;
import com.falsepattern.lib.StableAPI;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.Set;

/**
 * This class is used to register ChunkDataManagers, as well as dispatch clone requests.
 *
 * @since 0.5.0
 * @see DataManager
 * @author FalsePattern
 * @version 0.5.0
 */
@StableAPI(since = "0.5.0")
public class DataRegistry {
    /**
     * Registers a ChunkDataManager. Only do this during the init phase.
     *
     * @param manager The manager to register.
     *
     * @throws IllegalStateException    If the registration stage is over.
     * @throws IllegalArgumentException If the manager has a duplicate id.
     */
    @StableAPI.Expose
    public static void registerDataManager(DataManager manager)
            throws IllegalStateException, IllegalArgumentException {
        DataRegistryImpl.registerDataManager(manager);
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
        DataRegistryImpl.disableDataManager(domain, id);
    }

    /**
     * Returns an unmodifiable set of all registered ChunkDataManagers.
     * The id of a manager is its domain and id separated by a colon. (domain:id)
     */
    @Contract(pure = true)
    @StableAPI.Expose
    public static @Unmodifiable Set<String> getRegisteredManagers() {
        return DataRegistryImpl.getRegisteredManagers();
    }

    /**
     * Copies chunk-level data from a source chunk to a target chunk.
     * DOES NOT copy data contained inside its ExtendedBlockStorage instances!!
     * @param from The chunk to read the data from
     * @param to The chunk to write the data to
     */
    @Contract(mutates = "param2")
    @StableAPI.Expose(since = "0.5.0")
    public static void cloneChunk(Chunk from, Chunk to) {
        DataRegistryImpl.cloneChunk(from, to);
    }

    /**
     * Copies data from a source subchunk to a target subchunk.
     * @param fromChunk The chunk that owns the *from* subchunk. Used by data managers for getting metadata about the world (skylight presence, etc.)
     * @param from The subchunk to read the data from
     * @param to The subchunk to write the data to
     */
    @Contract(mutates = "param3")
    @StableAPI.Expose(since = "0.5.0")
    public static void cloneSubchunk(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
        DataRegistryImpl.cloneSubchunk(fromChunk, from, to);
    }
}
