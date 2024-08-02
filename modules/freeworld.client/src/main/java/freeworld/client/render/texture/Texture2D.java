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

import freeworld.client.render.RenderSystem;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.gl.GLResource;
import freeworld.util.Identifier;
import freeworld.util.Logging;
import org.slf4j.Logger;
import overrungl.opengl.GL;
import overrungl.opengl.GL10C;

import java.lang.foreign.Arena;

/**
 * @author squid233
 * @since 0.1.0
 */
public sealed class Texture2D implements GLResource permits ImagedTexture, TextureAtlas {
    private static final Logger logger = Logging.caller();
    public static final Identifier MISSING = Identifier.ofBuiltin("builtin/missing");
    private final int id;
    private final int width;
    private final int height;
    private final int mipmapLevel;

    protected Texture2D(int id, int width, int height, int mipmapLevel) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.mipmapLevel = mipmapLevel;
    }

    public static Texture2D load(GLStateMgr gl, Identifier identifier) {
        try (Arena arena = Arena.ofConfined()) {
            final String path = "assets/" + identifier.withPath(s -> "texture/" + s + ".png").toResourcePath();
            boolean missing = MISSING.equals(identifier);
            final NativeImage image = missing ? NativeImage.fail() : NativeImage.load(arena, path, ImageFormats.RGBA);
            if (image.failed() && !missing) {
                logger.error("Failed to load texture {}", identifier);
            }
            final int width = image.width();
            final int height = image.height();
            final boolean hasMipmap = isPowerOfTwo(width) && isPowerOfTwo(height);
            final int mipmapLevel = hasMipmap ? Math.min(Integer.numberOfTrailingZeros(width), Integer.numberOfTrailingZeros(height)) : 0;
            ImageFormats formats = image.formats();

            final int id = gl.genTextures();
            final Texture2D texture = new Texture2D(id, width, height, mipmapLevel);
            RenderSystem.bindTexture2D(texture);
            gl.texParameteri(GL10C.TEXTURE_2D, GL10C.TEXTURE_MIN_FILTER, hasMipmap ? GL10C.NEAREST_MIPMAP_NEAREST : GL10C.NEAREST);
            gl.texParameteri(GL10C.TEXTURE_2D, GL10C.TEXTURE_MAG_FILTER, GL10C.NEAREST);
            gl.texParameteri(GL10C.TEXTURE_2D, GL.TEXTURE_MAX_LEVEL, mipmapLevel);
            gl.texImage2D(GL10C.TEXTURE_2D,
                0,
                formats.internalFormat().glEnum(),
                width,
                height,
                0,
                formats.format().glEnum(),
                GL10C.UNSIGNED_BYTE,
                image.segment());
            if (hasMipmap) {
                gl.generateMipmap(GL10C.TEXTURE_2D);
            }
            return texture;
        }
    }

    public static boolean isPowerOfTwo(int value) {
        return value != 0 && value == Integer.highestOneBit(value);
    }

    public void bind(GLStateMgr gl) {
        gl.setTextureBinding2D(id());
    }

    public int id() {
        return id;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int mipmapLevel() {
        return mipmapLevel;
    }

    @Override
    public void close(GLStateMgr gl) {
        gl.deleteTextures(id());
    }
}
