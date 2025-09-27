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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.Set;
import java.util.SortedSet;

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
@ApiStatus.NonExtendable
public class DataRegistry {
    /**
     * Registers a ChunkDataManager. Only do this during the init phase.
     *
     * @param manager The manager to register.
     * @param ordering The natural ordering index for the data manager when iterating the list of managers.
     *                 <p>
     *                 ChunkAPI, Lumi, RPLE, and EndlessIDs all use 0. Negative numbers are sorted earlier. Positive numbers are sorted later.
     *                 <p>
     *                 Use 0 unless you specifically need to do something later.
     *                 <p>
     *                 As a convention, do this in increments of 1000 so that other people can order "between" your manager and the base managers.
     *
     *
     * @throws IllegalStateException    If the registration stage is over.
     * @throws IllegalArgumentException If the manager has a duplicate id.
     */
    public static void registerDataManager(DataManager manager, int ordering) throws IllegalStateException, IllegalArgumentException {
        DataRegistryImpl.registerDataManager(manager, ordering);
    }

    /**
     * Registers a ChunkDataManager. Only do this during the init phase.
     * Has an implicit ordering index of 0.
     *
     * @deprecated Use {@link #registerDataManager(DataManager, int)} with an explicit ordering.
     *
     * @param manager The manager to register.
     *
     * @throws IllegalStateException    If the registration stage is over.
     * @throws IllegalArgumentException If the manager has a duplicate id.
     */
    @Deprecated
    public static void registerDataManager(DataManager manager) throws IllegalStateException, IllegalArgumentException {
        DataRegistryImpl.registerDataManager(manager, 0);
    }

    /**
     * Disables a ChunkDataManager. Only do this during the postInit phase.
     *
     * @param id The id of the manager to disable.
     *
     * @throws IllegalStateException If the disable stage is over.
     */
    public static void disableDataManager(String domain, String id) throws IllegalStateException {
        DataRegistryImpl.disableDataManager(domain, id);
    }

    /**
     * Returns an unmodifiable set of all registered ChunkDataManagers.
     * The id of a manager is its domain and id separated by a colon. (domain:id)
     */
    @Contract(pure = true)
    @Deprecated
    public static @Unmodifiable Set<String> getRegisteredManagers() {
        return DataRegistryImpl.getRegisteredManagers();
    }

    /**
     * Returns an unmodifiable set of all registered ChunkDataManagers.
     * The id of a manager is its domain and id separated by a colon. (domain:id)
     * <p>
     * This function also includes the ordering index of each manager.
     */
    @Contract(pure = true)
    public static @Unmodifiable SortedSet<OrderedManager> getRegisteredManagersOrdered() {
        return DataRegistryImpl.getRegisteredManagersOrdered();
    }

    /**
     * Copies chunk-level data from a source chunk to a target chunk.
     * DOES NOT copy data contained inside its ExtendedBlockStorage instances!!
     *
     * @param from The chunk to read the data from
     * @param to   The chunk to write the data to
     */
    @Contract(mutates = "param2")
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
    public static void cloneSubChunk(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
        DataRegistryImpl.cloneSubChunk(fromChunk, from, to);
    }
}
