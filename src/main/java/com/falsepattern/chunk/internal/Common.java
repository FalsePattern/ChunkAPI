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

package com.falsepattern.chunk.internal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Non-minecraft stuff to avoid accidental classloading in spaghetti code
 */
public class Common {
    public static final Logger LOG = LogManager.getLogger(Tags.MODID);

    public static final int EBS_PER_CHUNK = 16;
    public static final int BLOCKS_PER_EBS = 16 * 16 * 16;
}
