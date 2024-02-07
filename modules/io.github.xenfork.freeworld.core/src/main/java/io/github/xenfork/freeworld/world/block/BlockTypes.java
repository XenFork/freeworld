/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.world.block;

import io.github.xenfork.freeworld.core.Identifier;
import io.github.xenfork.freeworld.core.registry.BuiltinRegistries;
import io.github.xenfork.freeworld.core.registry.Registry;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BlockTypes {
    public static final BlockType.Builder BUILDER_EMPTY = BlockType.builder();
    public static final BlockType.Builder BUILDER_AIR = BlockType.builder().air();
    public static final BlockType AIR = BUILDER_AIR.build();
    public static final BlockType GRASS_BLOCK = BUILDER_EMPTY.build();
    public static final BlockType DIRT = BUILDER_EMPTY.build();
    public static final BlockType STONE = BUILDER_EMPTY.build();

    private BlockTypes() {
    }

    private static void register(String name, int rawId, BlockType blockType) {
        Registry.register(BuiltinRegistries.BLOCK_TYPE, Identifier.ofBuiltin(name), rawId, blockType);
    }

    public static void bootstrap() {
        register("air", 0, AIR);
        register("grass_block", 1, GRASS_BLOCK);
        register("dirt", 2, DIRT);
        register("stone", 3, STONE);
    }
}
