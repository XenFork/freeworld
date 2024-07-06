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
public record Vector3f(float x, float y, float z) {
    public static final Vector3f ZERO = new Vector3f(0.0f);

    public Vector3f(float d) {
        this(d, d, d);
    }

    public Vector3f add(float x, float y, float z) {
        return new Vector3f(x() + x, y() + y, z() + z);
    }
}
