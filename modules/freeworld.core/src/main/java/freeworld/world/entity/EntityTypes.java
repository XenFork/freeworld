/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.world.entity;

import freeworld.core.Identifier;
import freeworld.core.math.AABBox;
import freeworld.core.registry.BuiltinRegistries;
import freeworld.core.registry.Registry;
import freeworld.world.entity.component.*;

import java.util.List;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class EntityTypes {
    public static final EyeHeightComponent PLAYER_EYE_HEIGHT = new EyeHeightComponent(1.71);
    public static final EntityType PLAYER = register(1, "player",
        new EntityType(List.of(
            AccelerationComponent::new,
            () -> boundingBox(0.0, 0.0, 0.0, 0.6, 1.8, 0.6),
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

    public static BoundingBoxComponent boundingBox(
        double x,
        double y,
        double z,
        double width,
        double height,
        double depth
    ) {
        final double hw = width * 0.5;
        final double hd = depth * 0.5;
        return new BoundingBoxComponent(new AABBox(
            x - hw,
            y,
            z - hd,
            x + hw,
            y + height,
            z + hd
        ));
    }
}
