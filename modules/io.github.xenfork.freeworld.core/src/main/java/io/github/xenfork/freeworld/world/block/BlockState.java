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

import io.github.xenfork.freeworld.world.block.property.BlockStateProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * @author baka4n
 * @author squid233
 * @since 0.1.0
 */
public final class BlockState {
    private final BlockType blockType;
    private final Map<BlockStateProperty<?>, Object> values;

    private BlockState(BlockType blockType, Map<BlockStateProperty<?>, Object> values) {
        this.blockType = blockType;
        this.values = values;
    }

    /**
     * {@return an empty block state of the given block type}
     *
     * @param blockType the block type
     */
    public static BlockState empty(BlockType blockType) {
        return new BlockState(blockType, Map.of());
    }

    public <T> BlockState with(BlockStateProperty<T> property, T value) {
        final var map = new HashMap<>(values);
        map.put(property, value);
        return new BlockState(blockType(), map);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(BlockStateProperty<T> property) {
        return (T) values.get(property);
    }

    public BlockType blockType() {
        return blockType;
    }
}
