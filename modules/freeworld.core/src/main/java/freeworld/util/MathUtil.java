/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.util;

import freeworld.math.Vector3d;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class MathUtil {
    private MathUtil() {
    }

    public static Vector3d moveRelative(double x, double y, double z, double yawDegrees, double speed) {
        final double dst = x * x + z * z;
        double moveX = 0.0;
        double moveZ = 0.0;
        if (dst >= 0.01) {
            final double k = speed / Math.sqrt(dst);
            final double kx = k * x;
            final double kz = k * z;

            final double yaw = Math.toRadians(yawDegrees);
            final double sin = Math.sin(yaw);
            final double cos = Math.cos(yaw);

            moveX = kx * cos + kz * sin;
            moveZ = kz * cos - kx * sin;
        }
        return new Vector3d(moveX, y, moveZ);
    }
}
