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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This program comes with additional permissions according to Section 7 of the
 * GNU Affero General Public License. See the full LICENSE file for details.
 */

package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.ArrayUtil;
import com.falsepattern.chunk.api.DataManager;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

public class LightingManager extends VanillaManager implements DataManager.ChunkDataManager {
    @Override
    public String id() {
        return "lighting";
    }

    @Override
    public void writeChunkToNBT(Chunk chunk, NBTTagCompound nbt) {
        nbt.setIntArray("HeightMap", chunk.heightMap);
        nbt.setBoolean("LightPopulated", chunk.isLightPopulated);
    }

    @Override
    public void readChunkFromNBT(Chunk chunk, NBTTagCompound nbt) {
        chunk.heightMap = nbt.getIntArray("HeightMap");
        chunk.isLightPopulated = nbt.getBoolean("LightPopulated");
    }

    @Override
    public boolean chunkPrivilegedAccess() {
        return true;
    }

    @Override
    public void cloneChunk(Chunk from, Chunk to) {
        to.heightMap = ArrayUtil.copyArray(from.heightMap, to.heightMap);
        to.isLightPopulated = from.isLightPopulated;
    }
}
