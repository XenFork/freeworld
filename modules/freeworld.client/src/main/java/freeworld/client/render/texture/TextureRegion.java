/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.texture;

/**
 * @author squid233
 * @since 0.1.0
 */
public record TextureRegion(TextureAtlas atlas, int x, int y, int width, int height) {
    public float u0() {
        return (float) x / atlas.width();
    }

    public float u1() {
        return (float) (x + width) / atlas.width();
    }

    public float v0() {
        return (float) y / atlas.height();
    }

    public float v1() {
        return (float) (y + height) / atlas.height();
    }
}
