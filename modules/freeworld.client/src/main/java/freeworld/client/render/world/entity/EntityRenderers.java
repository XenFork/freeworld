/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.world.entity;

import freeworld.client.FreeworldClient;
import freeworld.world.entity.Entity;
import freeworld.world.entity.EntityType;
import freeworld.world.entity.EntityTypes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class EntityRenderers {
    private static final Map<EntityType<?>, EntityRenderer.Factory<?>> factoryMap = new HashMap<>();

    private EntityRenderers() {
    }

    public static void bootstrap() {
        register(EntityTypes.CUBE, CubeEntityRenderer::new);
    }

    public static <T extends Entity> void register(EntityType<T> entityType, EntityRenderer.Factory<T> renderer) {
        factoryMap.put(entityType, renderer);
    }

    public static Map<EntityType<?>, EntityRenderer<?>> loadRenderers(FreeworldClient context) {
        final Map<EntityType<?>, EntityRenderer<?>> renderers = HashMap.newHashMap(factoryMap.size());
        factoryMap.forEach((entityType, factory) -> renderers.put(entityType, factory.create(context)));
        return Collections.unmodifiableMap(renderers);
    }
}
