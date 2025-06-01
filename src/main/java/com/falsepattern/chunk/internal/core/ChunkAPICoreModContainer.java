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

package com.falsepattern.chunk.internal.core;

import com.falsepattern.chunk.internal.DataRegistryImpl;
import com.falsepattern.chunk.internal.Tags;
import com.google.common.eventbus.EventBus;
import lombok.val;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.WorldAccessContainer;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;

import java.util.Map;

public class ChunkAPICoreModContainer extends DummyModContainer implements WorldAccessContainer {
    public ChunkAPICoreModContainer() {
        super(createMetadata());
    }

    private static ModMetadata createMetadata() {
        val meta = new ModMetadata();
        meta.modId = Tags.MOD_ID + "core";
        meta.name = Tags.MOD_NAME + " Core";
        meta.version = Tags.MOD_VERSION;
        meta.dependencies.add(new DefaultArtifactVersion(Tags.MOD_ID, Tags.MOD_VERSION));
        meta.parent = Tags.MOD_ID;
        return meta;
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

    @Override
    public NBTTagCompound getDataForWriting(SaveHandler handler, WorldInfo info) {
        val tag = new NBTTagCompound();
        DataRegistryImpl.writeLevelDat(tag);
        return tag;
    }

    @Override
    public void readData(SaveHandler handler, WorldInfo info, Map<String, NBTBase> propertyMap, NBTTagCompound tag) {
        DataRegistryImpl.readLevelDat(tag);
    }
}
