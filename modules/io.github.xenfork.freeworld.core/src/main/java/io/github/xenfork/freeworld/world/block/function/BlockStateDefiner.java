/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.world.block.function;

import io.github.xenfork.freeworld.world.block.BlockState;

/**
 * @author squid233
 * @since 0.1.0
 */
@FunctionalInterface
public interface BlockStateDefiner {
    BlockState apply(BlockState blockState);

    static BlockStateDefiner identity() {
        return blockState -> blockState;
    }
}
