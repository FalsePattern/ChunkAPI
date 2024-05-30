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

@Mod(modid = Tags.MODID,
     version = Tags.VERSION,
     name = Tags.MODNAME,
     acceptedMinecraftVersions = "[1.7.10]",
     dependencies = "required-after:falsepatternlib@[1.0.0,);")
public class ChunkAPI {

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        DataRegistry.registerDataManager(new BlockIDManager());
        DataRegistry.registerDataManager(new MetadataManager());
        DataRegistry.registerDataManager(new LightingManager());
        DataRegistry.registerDataManager(new BlocklightManager());
        DataRegistry.registerDataManager(new SkylightManager());
        DataRegistry.registerDataManager(new BiomeManager());
    }
}