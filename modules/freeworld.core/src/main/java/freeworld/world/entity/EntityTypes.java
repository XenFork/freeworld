/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.world.entity;

import freeworld.util.Identifier;
import freeworld.registry.Registries;
import freeworld.registry.Registry;
import freeworld.math.Vector3d;
import freeworld.world.entity.player.PlayerEntity;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class EntityTypes {
    public static final EntityType<PlayerEntity> PLAYER = register("player", EntityType.builder(PlayerEntity::new).dimension(new Vector3d(0.6, 1.8, 0.6)));
    /**
     * A cube entity is for test.
     */
    public static final EntityType<CubeEntity> CUBE = register("cube", EntityType.builder(CubeEntity::new).dimension(new Vector3d(1.0)));

    private EntityTypes() {
    }

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> entityType) {
        return Registry.register(Registries.ENTITY_TYPE, Identifier.ofBuiltin(name), entityType.build());
    }

    public static void bootstrap() {
    }
}
