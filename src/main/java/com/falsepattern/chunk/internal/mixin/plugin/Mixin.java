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
    S21PacketChunkDataMixin(Side.COMMON, always(), "vanilla.S21PacketChunkDataMixin"),
    S26PacketMapChunkBulkMixin(Side.COMMON, always(), "vanilla.S26PacketMapChunkBulkMixin"),

    ChunkMixin(Side.CLIENT, always(), "vanilla.ChunkMixin"),
    ;
    // @formatter:on

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

