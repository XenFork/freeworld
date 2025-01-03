/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2025  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.texture;

import freeworld.client.render.RenderSystem;
import freeworld.client.render.gl.GLStateMgr;

import static overrungl.opengl.GL10.*;
import static overrungl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class ImagedTexture extends Texture2D {
    private ImagedTexture(int id, int width, int height, int mipmapLevel) {
        super(id, width, height, mipmapLevel);
    }

    public static ImagedTexture of(GLStateMgr gl, NativeImage image) {
        int width = image.width();
        int height = image.height();
        ImageFormats formats = image.formats();
        int id = gl.GenTextures();
        ImagedTexture texture = new ImagedTexture(id, width, height, 0);
        RenderSystem.bindTexture2D(texture);
        gl.TexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl.TexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl.TexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
        gl.TexImage2D(GL_TEXTURE_2D,
            0,
            formats.internalFormat().glEnum(),
            width,
            height,
            0,
            formats.format().glEnum(),
            GL_UNSIGNED_BYTE,
            image.segment());
        return texture;
    }
}
