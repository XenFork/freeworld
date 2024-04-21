/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.world.entity;

import io.github.xenfork.freeworld.core.Identifier;
import io.github.xenfork.freeworld.core.math.AABBox;
import io.github.xenfork.freeworld.core.registry.BuiltinRegistries;
import io.github.xenfork.freeworld.core.registry.Registry;
import io.github.xenfork.freeworld.world.entity.component.*;

import java.util.List;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class EntityTypes {
    public static final BoundingBoxComponent PLAYER_BOUNDING_BOX = new BoundingBoxComponent(new AABBox(0.0, 0.0, 0.0, 0.6, 1.8, 0.6));
    public static final EyeHeightComponent PLAYER_EYE_HEIGHT = new EyeHeightComponent(1.71);
    public static final EntityType PLAYER = register(1, "player",
        new EntityType(List.of(
            AccelerationComponent::new,
            () -> PLAYER_BOUNDING_BOX,
            () -> PLAYER_EYE_HEIGHT,
            PositionComponent::new,
            RotationXYComponent::new,
            VelocityComponent::new
        )));

    private EntityTypes() {
    }

    private static EntityType register(int rawId, String name, EntityType entityType) {
        return Registry.register(BuiltinRegistries.ENTITY_TYPE, Identifier.ofBuiltin(name), rawId, entityType);
    }

    public static void bootstrap() {
    }
}
