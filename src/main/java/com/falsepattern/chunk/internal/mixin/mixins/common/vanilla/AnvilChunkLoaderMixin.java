/*
 * ChunkAPI
 *
 * Copyright (C) 2023-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
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

package com.falsepattern.chunk.internal.mixin.mixins.common.vanilla;

import com.falsepattern.chunk.internal.DataRegistryImpl;
import lombok.val;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import cpw.mods.fml.common.FMLLog;

import java.util.List;

@Mixin(AnvilChunkLoader.class)
public abstract class AnvilChunkLoaderMixin {
    /**
     * Writes the Chunk passed as an argument to the NBTTagCompound also passed, using the World argument to retrieve
     * the Chunk's last update time.
     *
     * @author FalsePattern
     * @reason Replace functionality
     * @implNote Inject-cancel instead of overwrite for compat with Metaworlds-Mixins
     */
    @Inject(method = "writeChunkToNBT",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void writeChunkToNBT(Chunk chunk, World world, NBTTagCompound nbt, CallbackInfo ci) {
        nbt.setByte("V", (byte) 1);
        nbt.setInteger("xPos", chunk.xPosition);
        nbt.setInteger("zPos", chunk.zPosition);
        nbt.setLong("LastUpdate", world.getTotalWorldTime());
        nbt.setBoolean("TerrainPopulated", chunk.isTerrainPopulated);
        nbt.setLong("InhabitedTime", chunk.inhabitedTime);
        writeSubChunks(chunk, nbt);
        writeCustomData(chunk, nbt);
        writeEntities(chunk, world, nbt);
        ci.cancel();
    }

    /**
     * Reads the data stored in the given NBTTagCompound and creates a Chunk with that data in the given World.
     * Returns the created Chunk.
     *
     * @author FalsePattern
     * @reason Replace functionality
     * @implNote Inject-cancel instead of overwrite for compat with Metaworlds-Mixins
     */
    @Inject(method = "readChunkFromNBT",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void readChunkFromNBT(World world, NBTTagCompound nbt, CallbackInfoReturnable<Chunk> cir) {
        int x = nbt.getInteger("xPos");
        int z = nbt.getInteger("zPos");
        Chunk chunk = new Chunk(world, x, z);
        chunk.isTerrainPopulated = nbt.getBoolean("TerrainPopulated");
        chunk.inhabitedTime = nbt.getLong("InhabitedTime");
        readSubChunks(chunk, nbt);
        readCustomData(chunk, nbt);

        // End this method here and split off entity loading to another method
        cir.setReturnValue(chunk);
    }

    private void readCustomData(Chunk chunk, NBTTagCompound nbt) {
        DataRegistryImpl.readChunkFromNBT(chunk, nbt);
    }

    private void readSubChunks(Chunk chunk, NBTTagCompound nbt) {
        NBTTagList subChunksNBT = nbt.getTagList("Sections", 10);
        byte segments = 16;
        ExtendedBlockStorage[] subChunkList = new ExtendedBlockStorage[segments];

        for (int k = 0; k < subChunksNBT.tagCount(); ++k) {
            NBTTagCompound subChunkNBT = subChunksNBT.getCompoundTagAt(k);
            byte yLevel = subChunkNBT.getByte("Y");
            ExtendedBlockStorage subChunk = new ExtendedBlockStorage(yLevel << 4, !chunk.worldObj.provider.hasNoSky);
            DataRegistryImpl.readSubChunkFromNBT(chunk, subChunk, subChunkNBT);

            subChunk.removeInvalidBlocks();
            subChunkList[yLevel] = subChunk;
        }

        chunk.setStorageArrays(subChunkList);
    }

    private void writeCustomData(Chunk chunk, NBTTagCompound nbt) {
        DataRegistryImpl.writeChunkToNBT(chunk, nbt);
    }

    private void writeSubChunks(Chunk chunk, NBTTagCompound nbt) {
        ExtendedBlockStorage[] subChunks = chunk.getBlockStorageArray();
        NBTTagList subChunksNBT = new NBTTagList();
        NBTTagCompound subChunkNBT;

        for (ExtendedBlockStorage subChunk : subChunks) {
            if (subChunk != null) {
                subChunkNBT = new NBTTagCompound();
                subChunkNBT.setByte("Y", (byte) (subChunk.getYLocation() >> 4 & 255));
                DataRegistryImpl.writeSubChunkToNBT(chunk, subChunk, subChunkNBT);
                subChunksNBT.appendTag(subChunkNBT);
            }
        }

        nbt.setTag("Sections", subChunksNBT);
    }

    private void writeEntities(Chunk chunk, World world, NBTTagCompound nbt) {
        chunk.hasEntities = false;
        NBTTagList entities = new NBTTagList();

        for (int i = 0; i < chunk.entityLists.length; ++i) {
            for (Object o : chunk.entityLists[i]) {
                Entity entity = (Entity) o;
                val entityNBT = new NBTTagCompound();

                try {
                    if (entity.writeToNBTOptional(entityNBT)) {
                        chunk.hasEntities = true;
                        entities.appendTag(entityNBT);
                    }
                } catch (Exception e) {
                    FMLLog.log(Level.ERROR, e,
                               "An Entity type %s has thrown an exception trying to write state. It will not persist. Report this to the mod author",
                               entity.getClass().getName());
                }
            }
        }

        nbt.setTag("Entities", entities);
        NBTTagList tileEntities = new NBTTagList();

        for (Object o : chunk.chunkTileEntityMap.values()) {
            TileEntity tileentity = (TileEntity) o;
            val tileEntityNBT = new NBTTagCompound();
            try {
                tileentity.writeToNBT(tileEntityNBT);
                tileEntities.appendTag(tileEntityNBT);
            } catch (Exception e) {
                FMLLog.log(Level.ERROR, e,
                           "A TileEntity type %s has throw an exception trying to write state. It will not persist. Report this to the mod author",
                           tileentity.getClass().getName());
            }
        }

        nbt.setTag("TileEntities", tileEntities);
        List<?> pendingUpdates = world.getPendingBlockUpdates(chunk, false);

        if (pendingUpdates != null) {
            long k = world.getTotalWorldTime();
            NBTTagList tileTicks = new NBTTagList();

            for (Object o : pendingUpdates) {
                NextTickListEntry update = (NextTickListEntry) o;
                NBTTagCompound updateNBT = new NBTTagCompound();
                updateNBT.setInteger("i", Block.getIdFromBlock(update.func_151351_a()));
                updateNBT.setInteger("x", update.xCoord);
                updateNBT.setInteger("y", update.yCoord);
                updateNBT.setInteger("z", update.zCoord);
                updateNBT.setInteger("t", (int) (update.scheduledTime - k));
                updateNBT.setInteger("p", update.priority);
                tileTicks.appendTag(updateNBT);
            }

            nbt.setTag("TileTicks", tileTicks);
        }
    }
}
