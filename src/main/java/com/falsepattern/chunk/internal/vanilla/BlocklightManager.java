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
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class BlocklightManager extends NibbleManager implements DataManager.SubChunkDataManager {
    @Override
    protected NibbleArray getNibbleArray(ExtendedBlockStorage subChunk) {
        return subChunk.getBlocklightArray();
    }

    @Override
    public String id() {
        return "blocklight";
    }

    @Override
    public boolean subChunkPrivilegedAccess() {
        return true;
    }

    @Override
    public void writeSubChunkToNBT(Chunk chunk, ExtendedBlockStorage subChunk, NBTTagCompound nbt) {
        nbt.setByteArray("BlockLight", subChunk.getBlocklightArray().data);
    }

    @Override
    public void readSubChunkFromNBT(Chunk chunk, ExtendedBlockStorage subChunk, NBTTagCompound nbt) {
        subChunk.setBlocklightArray(new NibbleArray(nbt.getByteArray("BlockLight"), 4));
    }

    @Override
    public void cloneSubChunk(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
        from.setBlocklightArray(ArrayUtil.copyArray(from.getBlocklightArray(), to.getBlocklightArray()));
    }
}
