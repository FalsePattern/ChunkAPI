/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.ChunkDataManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class VanillaManager implements ChunkDataManager {
    @Override
    public String domain() {
        return "minecraft";
    }

    public @NotNull String version() {
        return "";
    }

    public @Nullable String newInstallDescription() {
        return null;
    }

    public @NotNull String uninstallMessage() {
        return "";
    }

    public @Nullable String versionChangeMessage(String priorVersion) {
        return null;
    }
}
