# ChunkAPI
A multipurpose chunk hooking library that makes adding arbitrary extra data to chunks much easier.

## How
Read the code.
TL;DR: Replacing the vanilla chunk netcode and save logic with a modular system, and reimplementing the stock vanilla
logic on top of this. You can then add your own stuff.

## Compatibility
This will definitely break things, but the mods that were developed on top of this would've broken stuff anyways, and
having the most breaking changes centralized in a single hook mod makes writing compat much simpler.

## API
You can use the api of this mod to add your own chunk data. Minimum requirements you need:
1. A unique ID string for your data (collision avoidance)
2. An object that can be stored in a Map<String, Object> inside each chunk (note: the vanilla replacements bypass this!)
3. Packet data size/serialization/deserialization (for client/server communication)
4. NBT serialization/serialization (for savegames)

