package com.falsepattern.chunk.api.exception;

import com.falsepattern.chunk.api.ChunkDataManager;

/**
 * This exception is thrown when a {@link ChunkDataManager} could not be registered.
 * <p>
 * Possible reasons include:
 * <ul>
 *     <li>The manager is already registered</li>
 *     <li>The manager's name collides with a different manager</li>
 *     <li>The manager was registered too late (should be done in init)</li>
 * </ul>
 */
public class ManagerRegistrationException extends RuntimeException {
    public ManagerRegistrationException() {
        super();
    }

    public ManagerRegistrationException(String message) {
        super(message);
    }

    public ManagerRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManagerRegistrationException(Throwable cause) {
        super(cause);
    }

    protected ManagerRegistrationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
