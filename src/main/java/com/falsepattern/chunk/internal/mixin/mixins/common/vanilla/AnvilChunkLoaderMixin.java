/*
 * -------------------------------------------------------------------------------
 * @author FalsePattern
 *
 * Copyright 2023
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 * -------------------------------------------------------------------------------
 */

package com.falsepattern.chunk.internal.mixin.mixins.common.vanilla;

import com.falsepattern.chunk.internal.ChunkDataRegistryImpl;
import lombok.val;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import cpw.mods.fml.common.FMLLog;

import java.util.List;

@Mixin(AnvilChunkLoader.class)
public abstract class AnvilChunkLoaderMixin {
    /**
     * Writes the Chunk passed as an argument to the NBTTagCompound also passed, using the World argument to retrieve
     * the Chunk's last update time.
     * @author FalsePattern
     * @reason Replace functionality
     */
    @Overwrite
    private void writeChunkToNBT(Chunk chunk, World world, NBTTagCompound nbt) {
        nbt.setByte("V", (byte)1);
        nbt.setInteger("xPos", chunk.xPosition);
        nbt.setInteger("zPos", chunk.zPosition);
        System.out.println("Saving chunk " + chunk.xPosition + ", " + chunk.zPosition);
        nbt.setLong("LastUpdate", world.getTotalWorldTime());
        nbt.setIntArray("HeightMap", chunk.heightMap);
        nbt.setBoolean("TerrainPopulated", chunk.isTerrainPopulated);
        nbt.setBoolean("LightPopulated", chunk.isLightPopulated);
        nbt.setLong("InhabitedTime", chunk.inhabitedTime);
        writeSections(chunk, nbt);
        writeCustomData(chunk, nbt);
        writeEntities(chunk, world, nbt);
    }

    /**
     * Reads the data stored in the given NBTTagCompound and creates a Chunk with that data in the given World.
     * Returns the created Chunk.
     * @author FalsePattern
     * @reason Replace functionality
     */
    @Overwrite
    private Chunk readChunkFromNBT(World world, NBTTagCompound nbt) {
        int x = nbt.getInteger("xPos");
        int z = nbt.getInteger("zPos");
        System.out.println("Loading chunk " + x + ", " + z );
        Chunk chunk = new Chunk(world, x, z);
        chunk.heightMap = nbt.getIntArray("HeightMap");
        chunk.isTerrainPopulated = nbt.getBoolean("TerrainPopulated");
        chunk.isLightPopulated = nbt.getBoolean("LightPopulated");
        chunk.inhabitedTime = nbt.getLong("InhabitedTime");
        readSections(chunk, nbt);
        readCustomData(chunk, nbt);

        // End this method here and split off entity loading to another method
        return chunk;
    }

    private void readCustomData(Chunk chunk, NBTTagCompound nbt) {
        ChunkDataRegistryImpl.readChunkFromNBT(chunk, nbt);
    }

    private void readSections(Chunk chunk, NBTTagCompound nbt) {
        NBTTagList sections = nbt.getTagList("Sections", 10);
        byte segments = 16;
        ExtendedBlockStorage[] ebsList = new ExtendedBlockStorage[segments];

        for (int k = 0; k < sections.tagCount(); ++k) {
            NBTTagCompound section = sections.getCompoundTagAt(k);
            byte yLevel = section.getByte("Y");
            ExtendedBlockStorage ebs = new ExtendedBlockStorage(yLevel << 4, !chunk.worldObj.provider.hasNoSky);
            ChunkDataRegistryImpl.readSectionFromNBT(chunk, ebs, section);

            ebs.removeInvalidBlocks();
            ebsList[yLevel] = ebs;
        }

        chunk.setStorageArrays(ebsList);
    }

    private void writeCustomData(Chunk chunk, NBTTagCompound nbt) {
        ChunkDataRegistryImpl.writeChunkToNBT(chunk, nbt);
    }

    private void writeSections(Chunk chunk, NBTTagCompound nbt) {
        ExtendedBlockStorage[] ebsList = chunk.getBlockStorageArray();
        NBTTagList sections = new NBTTagList();
        NBTTagCompound section;

        for (ExtendedBlockStorage ebs: ebsList) {
            if (ebs != null) {
                section = new NBTTagCompound();
                section.setByte("Y", (byte) (ebs.getYLocation() >> 4 & 255));
                ChunkDataRegistryImpl.writeSectionToNBT(chunk, ebs, section);
                sections.appendTag(section);
            }
        }

        nbt.setTag("Sections", sections);
    }

    private void writeEntities(Chunk chunk, World world, NBTTagCompound nbt) {
        chunk.hasEntities = false;
        NBTTagList entities = new NBTTagList();

        for (int i = 0; i < chunk.entityLists.length; ++i) {
            for (Object o: chunk.entityLists[i]) {
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

        for (Object o: chunk.chunkTileEntityMap.values()) {
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