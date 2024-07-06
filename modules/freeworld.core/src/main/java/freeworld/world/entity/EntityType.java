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

import freeworld.core.math.AABBox;
import freeworld.math.Vector3d;
import freeworld.world.World;

/**
 * @author squid233
 * @since 0.1.0
 */
public record EntityType(Initializer initializer) {
    public interface Initializer {
        void setup(World world, Entity entity, Vector3d position);
    }

    public static AABBox boundingBox(
        double x,
        double y,
        double z,
        double width,
        double height,
        double depth
    ) {
        final double hw = width * 0.5;
        final double hd = depth * 0.5;
        return new AABBox(
            x - hw,
            y,
            z - hd,
            x + hw,
            y + height,
            z + hd
        );
    }
}
