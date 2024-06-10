/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.math;

/**
 * @author squid233
 * @since 0.1.0
 */
public record Vector3d(double x, double y, double z) {
    public static final Vector3d ZERO = new Vector3d(0.0);

    public Vector3d(double d) {
        this(d, d, d);
    }

    public Vector3d add(Vector3d v) {
        return add(v.x(), v.y(), v.z());
    }

    public Vector3d add(double x, double y, double z) {
        return new Vector3d(this.x + x, this.y + y, this.z + z);
    }

    public Vector3d mul(double x, double y, double z) {
        return new Vector3d(this.x * x, this.y * y, this.z * z);
    }

    public Vector3d lerp(Vector3d v, double t) {
        return new Vector3d(
            MathUtil.fma(v.x() - x, t, x),
            MathUtil.fma(v.y() - y, t, y),
            MathUtil.fma(v.z() - z, t, z)
        );
    }
}
