package com.falsepattern.chunk.internal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Non-minecraft stuff to avoid accidental classloading in spaghetti code
 */
public class Common {
    public static final Logger LOG = LogManager.getLogger(Tags.MODID);
}
