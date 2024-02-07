/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.world.block.property;

import io.github.xenfork.freeworld.util.Direction;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BlockStateProperties {
    public static final EnumProperty<Direction> DIRECTION = EnumProperty.of("direction", Direction.class);

    private BlockStateProperties() {
    }
}
