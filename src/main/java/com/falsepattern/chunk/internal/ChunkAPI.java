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

package com.falsepattern.chunk.internal;

import com.falsepattern.chunk.api.DataRegistry;
import com.falsepattern.chunk.internal.vanilla.BiomeManager;
import com.falsepattern.chunk.internal.vanilla.BlockIDManager;
import com.falsepattern.chunk.internal.vanilla.BlocklightManager;
import com.falsepattern.chunk.internal.vanilla.LightingManager;
import com.falsepattern.chunk.internal.vanilla.MetadataManager;
import com.falsepattern.chunk.internal.vanilla.SkylightManager;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = Tags.MOD_ID,
     version = Tags.MOD_VERSION,
     name = Tags.MOD_NAME,
     acceptedMinecraftVersions = "[1.7.10]")
public class ChunkAPI {

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        DataRegistry.registerDataManager(new BlockIDManager(), 0);
        DataRegistry.registerDataManager(new MetadataManager(), 0);
        DataRegistry.registerDataManager(new LightingManager(), 0);
        DataRegistry.registerDataManager(new BlocklightManager(), 0);
        DataRegistry.registerDataManager(new SkylightManager(), 0);
        DataRegistry.registerDataManager(new BiomeManager(), 0);
    }
}