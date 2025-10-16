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

package com.falsepattern.chunk.internal.mixin.plugin;

import com.falsepattern.chunk.internal.Tags;
import com.falsepattern.chunk.internal.mixin.plugin.fplib.MixinHelper;
import com.falsepattern.chunk.internal.mixin.plugin.fplib.SidedMixins;
import com.falsepattern.chunk.internal.mixin.plugin.fplib.TaggedMod;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.Language;

import java.util.function.BooleanSupplier;

import static com.falsepattern.chunk.internal.mixin.plugin.TargetMod.LookingGlass;
import static com.falsepattern.chunk.internal.mixin.plugin.TargetMod.Spool;
import static com.falsepattern.chunk.internal.mixin.plugin.fplib.MixinHelper.avoid;
import static com.falsepattern.chunk.internal.mixin.plugin.fplib.MixinHelper.builder;
import static com.falsepattern.chunk.internal.mixin.plugin.fplib.MixinHelper.mods;
import static com.falsepattern.chunk.internal.mixin.plugin.fplib.MixinHelper.require;

@RequiredArgsConstructor
public enum Mixin implements IMixins {
    // @formatter:off
    Core(Phase.EARLY,
         common("vanilla.PlayerInstanceMixin",
                "vanilla.S21PacketChunkDataMixin",
                "vanilla.S22PacketMultiBlockChangeMixin",
                "vanilla.S23PacketBlockChangeMixin",
                "vanilla.S26PacketMapChunkBulkMixin"),
         client("vanilla.NetHandlerPlayClientMixin")),

    //from: https://github.com/BallOfEnergy1/ChunkAPI/commit/4f5c0e60e04b6892d206f5f5d93cb20ba6b45608
    Core_NoSpool(Phase.EARLY,
                 avoid(Spool),
                 common("vanilla.AnvilChunkLoaderMixin"),
                 client("vanilla.ChunkMixin")),

    Compat_LookingGlass(Phase.LATE,
                        require(LookingGlass),
                        common("lookingglass.PacketChunkInfoMixin")),
    // @formatter:on

    //region boilerplate
    ;
    @Getter
    private final MixinBuilder builder;

    Mixin(Phase phase, SidedMixins... mixins) {
        this(builder(mixins).setPhase(phase));
    }

    Mixin(Phase phase, BooleanSupplier cond, SidedMixins... mixins) {
        this(builder(cond, mixins).setPhase(phase));
    }

    Mixin(Phase phase, TaggedMod mod, SidedMixins... mixins) {
        this(builder(mod, mixins).setPhase(phase));
    }

    Mixin(Phase phase, TaggedMod[] mods, SidedMixins... mixins) {
        this(builder(mods, mixins).setPhase(phase));
    }

    Mixin(Phase phase, BooleanSupplier cond, TaggedMod mod, SidedMixins... mixins) {
        this(builder(cond, mod, mixins).setPhase(phase));
    }

    Mixin(Phase phase, BooleanSupplier cond, TaggedMod[] mods, SidedMixins... mixins) {
        this(builder(cond, mods, mixins).setPhase(phase));
    }

    private static SidedMixins common(@Language(value = "JAVA",
                                                prefix = "import " + Tags.ROOT_PKG + ".internal.mixin.mixins.common.",
                                                suffix = ";") String... mixins) {
        return MixinHelper.common(mixins);
    }

    private static SidedMixins client(@Language(value = "JAVA",
                                                prefix = "import " + Tags.ROOT_PKG + ".internal.mixin.mixins.client.",
                                                suffix = ";") String... mixins) {
        return MixinHelper.client(mixins);
    }

    private static SidedMixins server(@Language(value = "JAVA",
                                                prefix = "import " + Tags.ROOT_PKG + ".mixin.mixins.server.",
                                                suffix = ";") String... mixins) {
        return MixinHelper.server(mixins);
    }
    //endregion
}
