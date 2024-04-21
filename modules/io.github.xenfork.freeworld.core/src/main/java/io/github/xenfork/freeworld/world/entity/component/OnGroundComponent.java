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

import io.github.xenfork.freeworld.core.Identifier;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class OnGroundComponent implements EntityComponent {
    public static final Identifier ID = Identifier.ofBuiltin("on_ground");
    public static final OnGroundComponent INSTANCE = new OnGroundComponent();

    @Override
    public Identifier componentId() {
        return ID;
    }
}
