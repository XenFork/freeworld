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
public record TextureRegion(int x, int y, int width, int height) {
    public float u0(int atlasWidth) {
        return (float) x / atlasWidth;
    }

    public float u1(int atlasWidth) {
        return (float) (x + width) / atlasWidth;
    }

    public float v0(int atlasHeight) {
        return (float) y / atlasHeight;
    }

    public float v1(int atlasHeight) {
        return (float) (y + height) / atlasHeight;
    }

    public float u(int atlasWidth, float offset) {
        return (x + offset) / atlasWidth;
    }

    public float v(int atlasHeight, float offset) {
        return (y + offset) / atlasHeight;
    }
}
