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
public record Vector4f(float x, float y, float z, float w) {
    public static final Vector4f ZERO = new Vector4f(0.0f);

    public Vector4f(float d) {
        this(d, d, d, d);
    }
}
