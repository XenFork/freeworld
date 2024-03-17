/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.util;

import org.joml.Vector3d;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class MathUtil {
    private MathUtil() {
    }

    public static Vector3d moveRelative(double x, double y, double z, double yawDegrees, Vector3d dest) {
        final double dst = x * x + z * z;
        double moveX = 0.0;
        double moveY = 0.0;
        double moveZ = 0.0;
        if (dst > 0.001) {
            final double sqrt = Math.sqrt(dst);
            final double invSqrt = 1.0 / sqrt;
            final double yaw = Math.toRadians(yawDegrees);

            final double normalX = x * invSqrt;
            final double normalZ = z * invSqrt;
            final double sin = Math.sin(yaw);
            final double cos = Math.cos(yaw);
            moveX += normalX * cos + normalZ * sin;
            moveZ += normalZ * cos - normalX * sin;
        }
        moveY += y;
        return dest.set(moveX, moveY, moveZ);
    }
}
