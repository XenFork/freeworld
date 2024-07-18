/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.world.block;

import freeworld.core.Identifier;
import freeworld.core.registry.Registries;
import freeworld.core.registry.Registry;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BlockTypes {
    public static final BlockType AIR = register("air", new AirBlockType(new BlockType.Settings().air().nonOpaque()));
    public static final BlockType GRASS_BLOCK = register("grass_block", new BlockType(new BlockType.Settings()));
    public static final BlockType DIRT = register("dirt", new BlockType(new BlockType.Settings()));
    public static final BlockType STONE = register("stone", new BlockType(new BlockType.Settings()));

    private BlockTypes() {
    }

    private static BlockType register(String name, BlockType blockType) {
        return Registry.register(Registries.BLOCK_TYPE, Identifier.ofBuiltin(name), blockType);
    }

    public static void bootstrap() {
    }
}
