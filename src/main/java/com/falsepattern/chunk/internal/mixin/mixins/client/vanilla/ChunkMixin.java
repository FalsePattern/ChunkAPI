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

package com.falsepattern.chunk.internal.mixin.mixins.client.vanilla;

import com.falsepattern.chunk.internal.DataRegistryImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(Chunk.class)
public abstract class ChunkMixin {
    @Shadow
    public Map<?, ?> chunkTileEntityMap;
    @Shadow
    public boolean isLightPopulated;
    @Shadow
    public boolean isTerrainPopulated;
    @Shadow
    public World worldObj;
    @Shadow
    private ExtendedBlockStorage[] storageArrays;

    @Shadow
    public abstract void generateHeightMap();

    @Shadow
    public abstract Block getBlock(int p_150810_1_, int p_150810_2_, int p_150810_3_);

    @Shadow
    public abstract int getBlockMetadata(int p_76628_1_, int p_76628_2_, int p_76628_3_);

    /**
     * @author FalsePattern
     * @reason Replace functionality
     */
    @SideOnly(Side.CLIENT)
    @Overwrite
    public void fillChunk(byte[] data, int subChunkMask, int subChunkMSBMask, boolean forceUpdate) {
        for (Object o : chunkTileEntityMap.values()) {
            TileEntity tileEntity = (TileEntity) o;
            tileEntity.updateContainingBlockInfo();
            tileEntity.getBlockMetadata();
            tileEntity.getBlockType();
        }

        boolean hasSky = !this.worldObj.provider.hasNoSky;

        for (int i = 0; i < storageArrays.length; i++) {
            if ((subChunkMask & (1 << i)) != 0) {
                if (storageArrays[i] == null) {
                    storageArrays[i] = new ExtendedBlockStorage(i << 4, hasSky);
                }
            } else if (forceUpdate && storageArrays[i] != null) {
                storageArrays[i] = null;
            }
        }

        DataRegistryImpl.readFromBuffer((Chunk) (Object) this, subChunkMask, forceUpdate, data);

        for (int i = 0; i < storageArrays.length; ++i) {
            if ((storageArrays[i] != null) && ((subChunkMask & (1 << i)) != 0)) {
                storageArrays[i].removeInvalidBlocks();
            }
        }
        this.isLightPopulated = true;
        this.isTerrainPopulated = true;
        this.generateHeightMap();
        List<TileEntity> invalidList = new ArrayList<>();
        for (Object o : this.chunkTileEntityMap.values()) {
            TileEntity tileentity = (TileEntity) o;
            int x = tileentity.xCoord & 15;
            int y = tileentity.yCoord;
            int z = tileentity.zCoord & 15;
            Block block = tileentity.getBlockType();
            if ((block != getBlock(x, y, z) || tileentity.blockMetadata != this.getBlockMetadata(x, y, z)) &&
                tileentity.shouldRefresh(block, getBlock(x, y, z), tileentity.blockMetadata,
                                         this.getBlockMetadata(x, y, z), worldObj, x, y, z)) {
                invalidList.add(tileentity);
            }
            tileentity.updateContainingBlockInfo();
        }

        for (TileEntity te : invalidList) {
            te.invalidate();
        }
    }
}
