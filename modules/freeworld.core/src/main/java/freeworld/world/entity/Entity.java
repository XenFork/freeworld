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

import freeworld.math.Vector3d;
import freeworld.world.World;
import freeworld.world.entity.component.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Entity {
    private final World world;
    private final UUID uuid;
    private final EntityType entityType;
    private final Map<EntityComponentKey<?>, Object> componentMap = new HashMap<>();

    public Entity(World world, UUID uuid, Vector3d position, EntityType entityType) {
        this.world = world;
        this.uuid = uuid;
        this.entityType = entityType;
        entityType.initializer().setup(world, this, position);
    }

    public <T> void addComponent(EntityComponentKey<T> key, T component) {
        Objects.requireNonNull(component);
        if (componentMap.containsKey(key)) {
            return;
        }
        componentMap.put(key, component);
    }

    public <T> void addComponent(EntityComponentKey<T> key) {
        addComponent(key, key.defaultValue().get());
    }

    public <T> void setComponent(EntityComponentKey<T> key, T component) {
        componentMap.put(key, component);
    }

    public void removeComponent(EntityComponentKey<?> id) {
        componentMap.remove(id);
    }

    @SuppressWarnings("unchecked")
    public <T> T getComponent(EntityComponentKey<T> id) {
        return (T) componentMap.get(id);
    }

    public boolean hasComponent(EntityComponentKey<?> id) {
        return componentMap.containsKey(id);
    }

    public World world() {
        return world;
    }

    public UUID uuid() {
        return uuid;
    }

    public EntityType entityType() {
        return entityType;
    }
}
