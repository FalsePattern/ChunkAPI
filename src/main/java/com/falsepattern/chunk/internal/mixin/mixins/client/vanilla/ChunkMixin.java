/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
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
