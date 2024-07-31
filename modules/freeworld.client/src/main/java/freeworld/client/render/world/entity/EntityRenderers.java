/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.world.entity;

import freeworld.registry.MappedRegistry;
import freeworld.registry.Registries;
import freeworld.registry.Registry;
import freeworld.util.Identifier;
import freeworld.world.entity.Entity;
import freeworld.world.entity.EntityType;
import freeworld.world.entity.EntityTypes;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class EntityRenderers {
    private static final MappedRegistry<EntityRenderer.Factory<Entity>> REGISTRY = new MappedRegistry<>(Identifier.ofBuiltin("entity_renderer"));

    private EntityRenderers() {
    }

    public static void bootstrap() {
        register(EntityTypes.CUBE, CubeEntityRenderer::new);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> void register(EntityType<T> entityType, EntityRenderer.Factory<T> renderer) {
        Registry.register(REGISTRY, Registries.ENTITY_TYPE.getId(entityType), (EntityRenderer.Factory<Entity>) renderer);
    }

    public static Registry<EntityRenderer.Factory<Entity>> registry() {
        return REGISTRY;
    }
}
