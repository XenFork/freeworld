/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.texture;

import overrungl.opengl.GL;
import overrungl.stb.STBImage;

/**
 * @author squid233
 * @since 0.1.0
 */
public enum ImageFormat {
    RED(STBImage.GREY, GL.RED),
    RGBA(STBImage.RGB_ALPHA, GL.RGBA);

    private final int stbEnum;
    private final int glEnum;

    ImageFormat(int stbEnum, int glEnum) {
        this.stbEnum = stbEnum;
        this.glEnum = glEnum;
    }

    public int stbEnum() {
        return stbEnum;
    }

    public int glEnum() {
        return glEnum;
    }
}
