Minecraft mod: ChunkAPI
- Author: FalsePattern 
- Type: library, coremod
- Status: in progress


Summary
-------

A mod for adding custom data to chunks.

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

Motivation
----------

Modifying chunk packet data involved incompatibly modifying network packets, the chunk class, and the anvil chunk loader,
which is a lot of work for a simple feature. This mod provides a way to add custom data to chunks without having
to modify any of the vanilla classes yourself.

Description
-----------

//TODO no solid plan yet, gotta think about it some more

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

Dependencies
------------

This mod directly depends on FalsePatternLib, and some sort of Mixin injector that provides SpongePowered Mixin >= 0.8.5.
The latter requirement may change in the future.