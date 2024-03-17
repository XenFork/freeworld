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

import io.github.xenfork.freeworld.world.World;
import io.github.xenfork.freeworld.world.entity.component.EntityComponent;
import io.github.xenfork.freeworld.world.entity.component.PositionComponent;
import io.github.xenfork.freeworld.world.entity.component.VelocityComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Entity {
    private final World world;
    private final UUID uuid;
    private final EntityType entityType;
    private final Map<String, EntityComponent> componentMap = new HashMap<>();

    public Entity(World world, UUID uuid, EntityType entityType) {
        this.world = world;
        this.uuid = uuid;
        this.entityType = entityType;
        entityType.defaultComponents().forEach(this::addComponent);
    }

    public void addComponent(EntityComponent component) {
        final String name = component.componentName();
        if (componentMap.containsKey(name)) {
            return;
        }
        componentMap.put(name, component);
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityComponent> T getComponent(String name) {
        return (T) componentMap.get(name);
    }

    public PositionComponent position() {
        return getComponent(PositionComponent.NAME);
    }

    public VelocityComponent velocity() {
        return getComponent(VelocityComponent.NAME);
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
