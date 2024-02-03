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
import io.github.xenfork.freeworld.file.BuiltinFiles;
import io.github.xenfork.freeworld.util.Logging;
import org.slf4j.Logger;
import overrun.marshal.Unmarshal;
import overrungl.opengl.GL;
import overrungl.opengl.GL10C;
import overrungl.stb.STBImage;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Texture implements AutoCloseable {
    private static final Logger logger = Logging.caller();
    private final int id;

    private Texture(int id) {
        this.id = id;
    }

    public static Texture load(Identifier identifier) {
        try (Arena arena = Arena.ofConfined()) {
            final String path = identifier.toResourcePath(Identifier.ROOT_ASSETS, Identifier.RES_TEXTURE, null);
            final MemorySegment segment = BuiltinFiles.loadBinary(arena, BuiltinFiles.load(path), path);
            if (Unmarshal.isNullPointer(segment)) {
                return null;
            }
            final MemorySegment px = arena.allocate(ValueLayout.JAVA_INT);
            final MemorySegment py = arena.allocate(ValueLayout.JAVA_INT);
            final MemorySegment pc = arena.allocate(ValueLayout.JAVA_INT);
            final STBImage stbImage = STBImage.INSTANCE;
            final MemorySegment result = stbImage.loadFromMemory(segment, px, py, pc, STBImage.RGB_ALPHA);
            if (Unmarshal.isNullPointer(result)) {
                logger.error("Failed to load image {} from {}: {}", identifier, path, stbImage.failureReason());
                return null;
            }
            final MemorySegment data = result.reinterpret(arena, stbImage::free);
            final int width = px.get(ValueLayout.JAVA_INT, 0L);
            final int height = py.get(ValueLayout.JAVA_INT, 0L);
            final boolean hasMipmap = isPowerOfTwo(width) && isPowerOfTwo(height);

            final GL gl = GameRenderer.OpenGL.get();
            final int id = gl.genTextures();
            gl.bindTexture(GL10C.TEXTURE_2D, id);
            gl.texParameteri(GL10C.TEXTURE_2D, GL10C.TEXTURE_MIN_FILTER, GL10C.NEAREST_MIPMAP_NEAREST);
            gl.texParameteri(GL10C.TEXTURE_2D, GL10C.TEXTURE_MAG_FILTER, GL10C.NEAREST);
            gl.texParameteri(GL10C.TEXTURE_2D,
                GL.TEXTURE_MAX_LEVEL,
                hasMipmap ?
                    Math.min(Integer.numberOfTrailingZeros(width), Integer.numberOfTrailingZeros(height)) :
                    0);
            gl.texImage2D(GL10C.TEXTURE_2D,
                0,
                GL10C.RGBA,
                width,
                height,
                0,
                GL10C.RGBA,
                GL10C.UNSIGNED_BYTE,
                data);
            if (hasMipmap) {
                gl.generateMipmap(GL10C.TEXTURE_2D);
            }
            gl.bindTexture(GL10C.TEXTURE_2D, 0);
            return new Texture(id);
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

    @Override
    public void close() {
        final GL gl = GameRenderer.OpenGL.get();
        gl.deleteTextures(id());
    }
}
