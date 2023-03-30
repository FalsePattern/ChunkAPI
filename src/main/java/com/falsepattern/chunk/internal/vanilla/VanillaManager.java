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

package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.ChunkDataManager;

public abstract class VanillaManager implements ChunkDataManager {
    @Override
    public String domain() {
        return "minecraft";
    }
}
