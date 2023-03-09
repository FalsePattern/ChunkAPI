package com.falsepattern.chunk.internal;

import com.falsepattern.chunk.api.ChunkDataManager;
import com.falsepattern.chunk.api.exception.ManagerRegistrationException;
import lombok.val;
import lombok.var;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChunkDataRegistryImpl {
    private static final Map<String, ChunkDataManager> managers = new HashMap<>();
    private static final Set<String> disabledManagers = new HashSet<>();
    public static void registerDataManager(ChunkDataManager manager) throws ManagerRegistrationException {
        if (!ChunkAPI.isRegistrationStage()) {
            throw new ManagerRegistrationException("ChunkDataManager registration is not allowed at this time! " +
                                                   "Please register your ChunkDataManager in the init phase of your mod.");
        }
        var id = manager.id();
        if (id == null) {
            throw new ManagerRegistrationException("ChunkDataManager " + manager + " has a null id!");
        }
        id = id.intern();
        if (managers.containsKey(id)) {
            throw new ManagerRegistrationException("ChunkDataManager " + manager + " has a duplicate id!");
        }

        // If the manager was disabled, do not add it to the list of managers.
        if (disabledManagers.contains(id)) {
            return;
        }

        managers.put(id, manager);
    }

    public static void disableDataManager(String id) {
        if (!ChunkAPI.isRegistrationStage()) {
            throw new ManagerRegistrationException("ChunkDataManager disabling is not allowed at this time! " +
                                                   "Please disable any ChunkDataManagers in the init phase of your mod.");
        }
        if (id == null) {
            throw new ManagerRegistrationException("ChunkDataManager id cannot be null!");
        }
        id = id.intern();
        Common.LOG.warn("Disabling ChunkDataManager " + id + ". See the stacktrace for the source of this event.", new Throwable());
        //Remove the manager from the list of managers, if it exists
        managers.remove(id);

        //Add the manager to the list of disabled managers, in case it gets registered after this disable call.
        disabledManagers.add(id);
    }
}
