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

/**
 * @author squid233
 * @since 0.1.0
 */
public final class EntityTypes {
    public static final EntityType<PlayerEntity> PLAYER = register("player", new EntityType<>(new EntityType.Settings().dimension(new Vector3d(0.6, 1.8, 0.6)).eyePosition(new Vector3d(0.0, 1.62, 0.0)), PlayerEntity::new));
    /**
     * A cube entity is for test.
     */
    public static final EntityType<CubeEntity> CUBE = register("cube", new EntityType<>(new EntityType.Settings().dimension(new Vector3d(1.0)), CubeEntity::new));

    private EntityTypes() {
    }

    private static <T extends Entity> EntityType<T> register(String name, EntityType<T> entityType) {
        return Registry.register(Registries.ENTITY_TYPE, Identifier.ofBuiltin(name), entityType);
    }

    public static void bootstrap() {
    }
}
