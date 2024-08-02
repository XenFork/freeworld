/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
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

    public Vector3d add(double x, double y, double z) {
        return new Vector3d(x() + x, y() + y, z() + z);
    }

    public Vector3d add(Vector3d v) {
        return new Vector3d(x() + v.x(), y() + v.y(), z() + v.z());
    }

    public Vector3d sub(double x, double y, double z) {
        return new Vector3d(x() - x, y() - y, z() - z);
    }

    public Vector3d sub(Vector3d v) {
        return new Vector3d(x() - v.x(), y() - v.y(), z() - v.z());
    }

    public Vector3d mul(double x, double y, double z) {
        return new Vector3d(x() * x, y() * y, z() * z);
    }

    public Vector3d mul(Vector3d v) {
        return new Vector3d(x() * v.x(), y() * v.y(), z() * v.z());
    }

    public Vector3d div(double x, double y, double z) {
        return new Vector3d(x() / x, y() / y, z() / z);
    }

    public Vector3d div(Vector3d v) {
        return new Vector3d(x() / v.x(), y() / v.y(), z() / v.z());
    }

    public Vector3d linearInterpolate(Vector3d v, double t) {
        return new Vector3d(
            Maths.linearInterpolate(x, v.x(), t),
            Maths.linearInterpolate(y, v.y(), t),
            Maths.linearInterpolate(z, v.z(), t)
        );
    }

    public Vector3f toVector3f() {
        return new Vector3f((float) x, (float) y, (float) z);
    }
}
