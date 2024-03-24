/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.core.math;

/**
 * @author squid233
 * @since 0.1.0
 */
public record AABBox(
    double minX,
    double minY,
    double minZ,
    double maxX,
    double maxY,
    double maxZ
) {
    public static final AABBox EMPTY = new AABBox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    public static final AABBox FULL_CUBE = new AABBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

    public AABBox {
        if (minX > maxX) {
            double _minX = minX;
            minX = maxX;
            maxX = _minX;
        }
        if (minY > maxY) {
            double _minY = minY;
            minY = maxY;
            maxY = _minY;
        }
        if (minZ > maxZ) {
            double _minZ = minZ;
            minZ = maxZ;
            maxZ = _minZ;
        }
    }

    public AABBox move(double x, double y, double z) {
        return new AABBox(
            minX + x,
            minY + y,
            minZ + z,
            maxX + x,
            maxY + y,
            maxZ + z
        );
    }
}
