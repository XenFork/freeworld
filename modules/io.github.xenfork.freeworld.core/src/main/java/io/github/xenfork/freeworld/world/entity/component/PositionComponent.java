/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.world.entity.component;

import org.joml.Vector3d;

/**
 * @author squid233
 * @since 0.1.0
 */
public record PositionComponent(Vector3d position) implements EntityComponent {
    public static final String NAME = "position";

    public PositionComponent() {
        this(new Vector3d());
    }

    @Override
    public String componentName() {
        return NAME;
    }
}
