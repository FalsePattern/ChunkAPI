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

package com.falsepattern.chunk.internal.mixin.plugin;

import com.falsepattern.lib.mixin.IMixin;
import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.always;

@RequiredArgsConstructor
public enum Mixin implements IMixin {
    // @formatter:off
    AnvilChunkLoaderMixin(Side.COMMON, always(), "vanilla.AnvilChunkLoaderMixin"),
    PlayerInstanceMixin(Side.COMMON, always(), "vanilla.PlayerInstanceMixin"),
    S21PacketChunkDataMixin(Side.COMMON, always(), "vanilla.S21PacketChunkDataMixin"),
    S22PacketMultiBlockChangeMixin(Side.COMMON, always(), "vanilla.S22PacketMultiBlockChangeMixin"),
    S23PacketBlockChangeMixin(Side.COMMON, always(), "vanilla.S23PacketBlockChangeMixin"),
    S26PacketMapChunkBulkMixin(Side.COMMON, always(), "vanilla.S26PacketMapChunkBulkMixin"),

    ChunkMixin(Side.CLIENT, always(), "vanilla.ChunkMixin"),
    NetHandlerPlayClientMixin(Side.CLIENT, always(), "vanilla.NetHandlerPlayClientMixin"),
    ;
    // @formatter:on

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

