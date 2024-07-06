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

import freeworld.core.Identifier;
import freeworld.core.registry.BuiltinRegistries;
import freeworld.core.registry.Registry;
import freeworld.math.Vector3d;
import freeworld.world.World;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class EntityTypes {
    public static final Vector3d PLAYER_EYE_POSITION = new Vector3d(0.0, 1.62, 0.0);
    public static final EntityType PLAYER = register(1, "player", EntityTypes::setupComponentPlayer);

    private EntityTypes() {
    }

    private static void setupComponentPlayer(World world, Entity entity, Vector3d position) {
        entity.addComponent(EntityComponents.ACCELERATION);
        entity.addComponent(EntityComponents.BOUNDING_BOX, EntityType.boundingBox(position.x(), position.y(), position.z(), 0.6, 1.8, 0.6));
        entity.addComponent(EntityComponents.EYE_POSITION, PLAYER_EYE_POSITION);
        entity.addComponent(EntityComponents.POSITION, position);
        entity.addComponent(EntityComponents.ROTATION);
        entity.addComponent(EntityComponents.VELOCITY);
    }

    private static EntityType register(int rawId, String name, EntityType.Initializer initializer) {
        return Registry.register(BuiltinRegistries.ENTITY_TYPE, Identifier.ofBuiltin(name), rawId, new EntityType(initializer));
    }

    public static void bootstrap() {
    }
}
