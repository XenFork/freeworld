/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.util;

import freeworld.math.Vector4i;

/**
 * @author squid233
 * @since 0.1.0
 */
public record Color(Vector4i rgba) {
    public static final Color WHITE = new Color(255, 255, 255, 255);

    public Color(int red, int green, int blue, int alpha) {
        this(new Vector4i(red, green, blue, alpha));
    }

    public int red() {
        return rgba.x();
    }

    public int green() {
        return rgba.y();
    }

    public int blue() {
        return rgba.z();
    }

    public int alpha() {
        return rgba.w();
    }
}
