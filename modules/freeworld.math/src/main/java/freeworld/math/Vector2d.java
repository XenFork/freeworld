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
public record Vector2d(double x, double y) {
    public static final Vector2d ZERO = new Vector2d(0.0);

    public Vector2d(double d) {
        this(d, d);
    }

    public static double distanceSquared(double x1, double y1, double x2, double y2) {
        final double dx = x1 - x2;
        final double dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    public Vector2d add(double x, double y) {
        return new Vector2d(x() + x, y() + y);
    }

    public Vector2d add(Vector2d v) {
        return new Vector2d(x() + v.x(), y() + v.y());
    }

    public Vector2d sub(double x, double y) {
        return new Vector2d(x() - x, y() - y);
    }

    public Vector2d sub(Vector2d v) {
        return new Vector2d(x() - v.x(), y() - v.y());
    }

    public Vector2d mul(double x, double y) {
        return new Vector2d(x() * x, y() * y);
    }

    public Vector2d mul(Vector2d v) {
        return new Vector2d(x() * v.x(), y() * v.y());
    }

    public Vector2d div(double x, double y) {
        return new Vector2d(x() / x, y() / y);
    }

    public Vector2d div(Vector2d v) {
        return new Vector2d(x() / v.x(), y() / v.y());
    }
}
