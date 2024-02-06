/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.texture;

import io.github.xenfork.freeworld.client.render.GameRenderer;
import io.github.xenfork.freeworld.core.Identifier;
import io.github.xenfork.freeworld.util.Logging;
import org.slf4j.Logger;
import overrungl.opengl.GL;
import overrungl.opengl.GL10C;

import java.lang.foreign.Arena;

/**
 * @author squid233
 * @since 0.1.0
 */
public sealed class Texture implements AutoCloseable permits TextureAtlas {
    private static final Logger logger = Logging.caller();
    private final int id;
    private final int width;
    private final int height;
    private final int mipmapLevel;

    protected Texture(int id, int width, int height, int mipmapLevel) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.mipmapLevel = mipmapLevel;
    }

    public static Texture load(Identifier identifier) {
        try (Arena arena = Arena.ofConfined()) {
            final String path = identifier.toResourcePath(Identifier.ROOT_ASSETS, null, null);
            final NativeImage image = NativeImage.load(arena, path);
            if (image.failed()) {
                logger.error("Failed to load texture {}", identifier);
            }
            final int width = image.width();
            final int height = image.height();
            final boolean hasMipmap = isPowerOfTwo(width) && isPowerOfTwo(height);
            final int mipmapLevel = hasMipmap ? Math.min(Integer.numberOfTrailingZeros(width), Integer.numberOfTrailingZeros(height)) : 0;

            final GL gl = GameRenderer.OpenGL.get();
            final int id = gl.genTextures();
            gl.bindTexture(GL10C.TEXTURE_2D, id);
            gl.texParameteri(GL10C.TEXTURE_2D, GL10C.TEXTURE_MIN_FILTER, hasMipmap ? GL10C.NEAREST_MIPMAP_NEAREST : GL10C.NEAREST);
            gl.texParameteri(GL10C.TEXTURE_2D, GL10C.TEXTURE_MAG_FILTER, GL10C.NEAREST);
            gl.texParameteri(GL10C.TEXTURE_2D, GL.TEXTURE_MAX_LEVEL, mipmapLevel);
            gl.texImage2D(GL10C.TEXTURE_2D,
                0,
                GL10C.RGBA,
                width,
                height,
                0,
                GL10C.RGBA,
                GL10C.UNSIGNED_BYTE,
                image.segment());
            if (hasMipmap) {
                gl.generateMipmap(GL10C.TEXTURE_2D);
            }
            gl.bindTexture(GL10C.TEXTURE_2D, 0);
            return new Texture(id, width, height, mipmapLevel);
        }
    }

    public static boolean isPowerOfTwo(int value) {
        return value != 0 && value == Integer.highestOneBit(value);
    }

    public void bind() {
        final GL gl = GameRenderer.OpenGL.get();
        gl.bindTexture(GL10C.TEXTURE_2D, id());
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
    public void close() {
        final GL gl = GameRenderer.OpenGL.get();
        gl.deleteTextures(id());
    }
}
