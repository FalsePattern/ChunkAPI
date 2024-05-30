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
        meta.modId = Tags.MODID + "core";
        meta.name = Tags.MODNAME + " Core";
        meta.version = Tags.VERSION;
        meta.dependencies.add(new DefaultArtifactVersion(Tags.MODID, Tags.VERSION));
        meta.parent = Tags.MODID;
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
