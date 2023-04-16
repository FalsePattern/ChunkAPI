# ChunkAPI

## Author: FalsePattern
## License: CC-BY-NC-ND 4.0 <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc-nd/4.0/88x31.png" /></a>

Summary
-------

A mod for adding custom data to chunks without the hassle of writing custom packets, event handling, loading hooks, and more.

Goals
-----

It is a goal to provide a way to add custom data to chunks without having to modify any vanilla classes.
The API will also provide a simple way to implement the custom serialization/deserialization of the data,
both for the networking and the NBT format.
An optional goal is to provide a way to register DFUs for the custom data, but this is not a priority.

Non-Goals
---------

It is not a goal to provide any kind of callback/hook system for anything except saving/loading and netcode, any kind
of extra behavior should be implemented in the mod using ChunkAPI.

Storage is not provided by the API, the user is expected to implement their own storage system. A good example
is using a mixin to add a field to the chunk class, and using that field to store the data.

Motivation
----------

Modifying chunk packet data involved incompatibly modifying network packets, the chunk class, and the anvil chunk loader,
which is a lot of work for a simple feature. This mod provides a way to add custom data to chunks without having
to modify any of the vanilla classes yourself.

Description
-----------

The API exposes a way to add custom data to chunks, and a way to register custom serializers for the data.

### ChunkDataManager
The `ChunkDataManager` itself is the primary class used for managing registrations, but does not implement functionality
by itself. For that, you need to use the `PacketDataManager`, `ChunkNBTDataManager`, and `SectionNBTDataManager`
interfaces included inside the ChunkDataManager class.

### ChunkDataManager.PacketData
This interface is used for synchronizing data from the server to the client. If your data is only required on the server,
you can freely skip implementing this interface.

### ChunkDataManager.ChunkNBTData
This interface is used for saving/loading data from the chunk NBT. This is required if you want to keep persistent data
across world reloads. Note, if you store data *per block* in the chunk, you should use `SectionNBTData` instead, as it
is designed with the internal chunk format in mind.

### ChunkDataManager.SectionNBTData
This interface is mostly identical to `ChunkNBTData`, but is designed for storing data per block in the chunk.
Instead of being called once per chunk, it is called once per chunk section (16x16x16 blocks, `ExtendedBlockStage` class).

### ChunkDataRegistry
This is where you actually register your manager. You need to do all registrations inside the `init` phase.
You can also disable specific manager IDs by calling `disableDataManager`, but this is not recommended, and should
only be used if you know what you are doing. You need to do all the disabling inside the `postInit` phase.

Alternatives
------------

Mods can also implement their own chunk-independent data storage, or even use hidden entities to store data, but
this is not a good solution and prone to breaking.

Testing
-------

This mod will be tested by using it in my own mods, primarily EndlessIDs.

Risks and Assumptions
---------------------

This might slow down chunk loading/saving, and the networking, if the implementation turns out to be bad.
Ideally, mods should declare their custom data as new fields in the chunk class using mixins instead of the
ModdedChunk storage, but this is not a requirement.

Ideally, this project must not have any derivatives or forks, as that will counter the primary purpose of this project,
which is a single, unified codebase for chunk data exchange. For that goal, the project is explicitly licensed such as
no derivatives are permitted.

Dependencies
------------

This mod directly depends on FalsePatternLib, and some sort of Mixin injector that provides SpongePowered Mixin >= 0.8.5.