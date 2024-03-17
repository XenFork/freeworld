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

import io.github.xenfork.freeworld.world.entity.component.EntityComponent;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author squid233
 * @since 0.1.0
 */
@SuppressWarnings("ClassCanBeRecord")
public final class EntityType {
    private final List<Supplier<EntityComponent>> defaultComponents;

    public EntityType(List<Supplier<EntityComponent>> defaultComponents) {
        this.defaultComponents = defaultComponents;
    }

    public List<Supplier<EntityComponent>> defaultComponents() {
        return defaultComponents;
    }
}
