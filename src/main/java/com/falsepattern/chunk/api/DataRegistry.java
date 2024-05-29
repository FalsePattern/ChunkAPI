/*
 * ChunkAPI
 *
 * Copyright (C) 2023-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * -------------------------------------------------------------------------------
 * According to section 7 of the GNU AGPL, this software comes with the following additional permissions:
 *
 * You may link this mod against proprietary code developed by Mojang Studios (Minecraft and its dependencies), as well
 * as distribute binary builds of it as part of a "Minecraft modpack" without the AGPL restrictions spreading to any other
 * files in said modpack.
 *
 * You may also develop any mods that depend on classes the API packages (`com.falsepattern.chunk.api.*`) and distribute that
 * mod under terms of your own choice. Note that this permissions does not apply to anything outside the internal package,
 * as those are internal implementations of ChunkAPI and are not meant to be used in any external mod, thus the full force
 * of the AGPL applies in such cases.
 *
 * These additional permissions get removed if you modify, extend, rename, remove, or in any other way alter
 * any part of the public API code, save data binary formats, or the network protocol in such a way that makes
 * it not perfectly compatible with the official versions of ChunkAPI.
 *
 * If you wish to make modifications to the previously mentioned features, please create a pull request on the official
 * release of ChunkAPI with full reasoning and specifications behind the requested change, and once it's merged and
 * published in an official release, your modified version may once again inherit this additional permission.
 *
 * To avoid abuse caused by upstream API changes, these permissions are valid as long as your modified version of ChunkAPI
 * is perfectly compatible with any public release of the official ChunkAPI.
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
     * Copies data from a source subChunk to a target subChunk.
     * @param fromChunk The chunk that owns the *from* subChunk. Used by data managers for getting metadata about the world (skylight presence, etc.)
     * @param from The subChunk to read the data from
     * @param to The subChunk to write the data to
     */
    @Contract(mutates = "param3")
    @StableAPI.Expose(since = "0.5.0")
    public static void cloneSubChunk(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
        DataRegistryImpl.cloneSubChunk(fromChunk, from, to);
    }
}
