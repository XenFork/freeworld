/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.core.registry;

import io.github.xenfork.freeworld.core.Identifier;
import io.github.xenfork.freeworld.world.block.BlockType;
import io.github.xenfork.freeworld.world.block.BlockTypes;
import io.github.xenfork.freeworld.world.entity.EntityType;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BuiltinRegistries {
    public static final DefaultedRegistry<BlockType> BLOCK_TYPE = new DefaultedRegistry<>(Identifier.ofBuiltin("block_type"), () -> BlockTypes.AIR);
    public static final MappedRegistry<EntityType> ENTITY_TYPE = new MappedRegistry<>(Identifier.ofBuiltin("entity_type"));

    private BuiltinRegistries() {
    }
}
