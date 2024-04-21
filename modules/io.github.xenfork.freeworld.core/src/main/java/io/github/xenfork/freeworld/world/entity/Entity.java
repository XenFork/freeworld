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
import io.github.xenfork.freeworld.world.World;
import io.github.xenfork.freeworld.world.entity.component.*;

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
    private final Map<Identifier, EntityComponent> componentMap = new HashMap<>();

    public Entity(World world, UUID uuid, EntityType entityType) {
        this.world = world;
        this.uuid = uuid;
        this.entityType = entityType;
        for (var supplier : entityType.defaultComponents()) {
            addComponent(supplier.get());
        }
    }

    public void addComponent(EntityComponent component) {
        Objects.requireNonNull(component);
        final Identifier id = component.componentId();
        if (componentMap.containsKey(id)) {
            return;
        }
        componentMap.put(id, component);
    }

    public void setComponent(EntityComponent component) {
        componentMap.put(component.componentId(), component);
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityComponent> T getComponent(Identifier id) {
        return (T) componentMap.get(id);
    }

    public boolean hasComponent(Identifier id) {
        return componentMap.containsKey(id);
    }

    public AccelerationComponent acceleration() {
        return getComponent(AccelerationComponent.ID);
    }

    public BoundingBoxComponent boundingBox() {
        return getComponent(BoundingBoxComponent.ID);
    }

    public EyeHeightComponent eyeHeight() {
        return getComponent(EyeHeightComponent.ID);
    }

    public PositionComponent position() {
        return getComponent(PositionComponent.ID);
    }

    public RotationXYComponent rotation() {
        return getComponent(RotationXYComponent.ID);
    }

    public VelocityComponent velocity() {
        return getComponent(VelocityComponent.ID);
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
