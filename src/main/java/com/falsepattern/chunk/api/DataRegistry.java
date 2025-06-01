/*
 * ChunkAPI
 *
 * Copyright (C) 2023-2025 FalsePattern, The MEGA Team, LegacyModdingMC contributors
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
 * This is an API class covered by the additional permissions in the license.
 * <p>
 * This class is used to register ChunkDataManagers, as well as dispatch clone requests.
 *
 * @author FalsePattern
 * @version 0.5.0
 * @see DataManager
 * @since 0.5.0
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
    public static void registerDataManager(DataManager manager) throws IllegalStateException, IllegalArgumentException {
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
     *
     * @param from The chunk to read the data from
     * @param to   The chunk to write the data to
     */
    @Contract(mutates = "param2")
    @StableAPI.Expose(since = "0.5.0")
    public static void cloneChunk(Chunk from, Chunk to) {
        DataRegistryImpl.cloneChunk(from, to);
    }

    /**
     * Copies data from a source subChunk to a target subChunk.
     *
     * @param fromChunk The chunk that owns the *from* subChunk. Used by data managers for getting metadata about the world (skylight presence, etc.)
     * @param from      The subChunk to read the data from
     * @param to        The subChunk to write the data to
     */
    @Contract(mutates = "param3")
    @StableAPI.Expose(since = "0.5.0")
    public static void cloneSubChunk(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
        DataRegistryImpl.cloneSubChunk(fromChunk, from, to);
    }
}
