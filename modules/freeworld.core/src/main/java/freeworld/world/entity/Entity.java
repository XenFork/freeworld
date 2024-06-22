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

import freeworld.math.Vector3d;
import freeworld.world.World;
import freeworld.world.component.ComponentKey;

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
    private final Map<ComponentKey<?>, Object> componentMap = new HashMap<>();

    public Entity(World world, UUID uuid, Vector3d position, EntityType entityType) {
        this.world = world;
        this.uuid = uuid;
        this.entityType = entityType;
        entityType.initializer().setup(world, this, position);
    }

    public <T> void addComponent(ComponentKey<T> key, T component) {
        Objects.requireNonNull(component);
        if (componentMap.containsKey(key)) {
            return;
        }
        componentMap.put(key, component);
    }

    public <T> void addComponent(ComponentKey<T> key) {
        addComponent(key, key.defaultValue().get());
    }

    public <T> void setComponent(ComponentKey<T> key, T component) {
        componentMap.put(key, component);
    }

    public void removeComponent(ComponentKey<?> id) {
        componentMap.remove(id);
    }

    @SuppressWarnings("unchecked")
    public <T> T getComponent(ComponentKey<T> id) {
        return (T) componentMap.get(id);
    }

    public boolean hasComponent(ComponentKey<?> id) {
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
