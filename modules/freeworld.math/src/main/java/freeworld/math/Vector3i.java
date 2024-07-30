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
public record Vector3i(int x, int y, int z) {
    public static final Vector3i ZERO = new Vector3i(0);

    public Vector3i(int d) {
        this(d, d, d);
    }

    public Vector3i add(Vector3i v) {
        return new Vector3i(x + v.x, y + v.y, z + v.z);
    }

    public Vector3i mul(int i) {
        return new Vector3i(x * i, y * i, z * i);
    }

    public Vector3f toVector3f() {
        return new Vector3f(x, y, z);
    }
}
