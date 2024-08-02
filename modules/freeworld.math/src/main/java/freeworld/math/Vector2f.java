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
public record Vector2f(float x, float y) {
    public static final Vector2f ZERO = new Vector2f(0.0f);
    public static final Vector2f ONE = new Vector2f(1.0f);

    public Vector2f(float d) {
        this(d, d);
    }

    public Vector2f add(float x, float y) {
        return new Vector2f(x() + x, y() + y);
    }

    public Vector2f add(Vector2f v) {
        return new Vector2f(x() + v.x, y() + v.y);
    }

    public Vector2f sub(float x, float y) {
        return new Vector2f(x() - x, y() - y);
    }

    public Vector2f sub(Vector2f v) {
        return new Vector2f(x() - v.x, y() - v.y);
    }

    public Vector2f mul(float x, float y) {
        return new Vector2f(x() * x, y() * y);
    }

    public Vector2f mul(Vector2f v) {
        return new Vector2f(x() * v.x, y() * v.y);
    }

    public Vector2f div(float x, float y) {
        return new Vector2f(x() / x, y() / y);
    }

    public Vector2f div(Vector2f v) {
        return new Vector2f(x() / v.x, y() / v.y);
    }
}
