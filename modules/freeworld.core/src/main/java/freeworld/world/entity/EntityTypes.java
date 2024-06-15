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
import freeworld.core.registry.BuiltinRegistries;
import freeworld.core.registry.Registry;
import freeworld.math.Vector3d;
import freeworld.world.World;
import freeworld.world.entity.component.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class EntityTypes {
    public static final double PLAYER_EYE_HEIGHT = 1.71;
    public static final EntityType PLAYER = register(1, "player", EntityTypes::setupComponentPlayer);

    private EntityTypes() {
    }

    private static void setupComponentPlayer(World world, Entity entity, Vector3d position) {
        entity.addComponent(EntityComponentKeys.ACCELERATION);
        entity.addComponent(EntityComponentKeys.BOUNDING_BOX, EntityType.boundingBox(position.x(), position.y(), position.z(), 0.6, 1.8, 0.6));
        entity.addComponent(EntityComponentKeys.EYE_HEIGHT, PLAYER_EYE_HEIGHT);
        entity.addComponent(EntityComponentKeys.POSITION, position);
        entity.addComponent(EntityComponentKeys.ROTATION);
        entity.addComponent(EntityComponentKeys.VELOCITY);
    }

    private static EntityType register(int rawId, String name, EntityType.Initializer initializer) {
        return Registry.register(BuiltinRegistries.ENTITY_TYPE, Identifier.ofBuiltin(name), rawId, new EntityType(initializer));
    }

    public static void bootstrap() {
    }
}
