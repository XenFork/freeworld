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
import overrungl.opengl.GL;
import overrungl.opengl.GL10C;
import overrungl.stb.STBRPContext;
import overrungl.stb.STBRPNode;
import overrungl.stb.STBRPRect;
import overrungl.stb.STBRectPack;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class TextureAtlas extends Texture {
    private final Map<Identifier, TextureRegion> regionMap;

    private TextureAtlas(int id, int width, int height, int mipmapLevel, Map<Identifier, TextureRegion> regionMap) {
        super(id, width, height, mipmapLevel);
        this.regionMap = regionMap;
    }

    public static TextureAtlas load(List<Identifier> identifierList) {
        final int numIds = identifierList.size();
        final STBRectPack stbrp = STBRectPack.INSTANCE;
        try (Arena arena = Arena.ofConfined()) {
            final Map<Identifier, NativeImage> imageMap = HashMap.newHashMap(numIds);
            identifierList.forEach(identifier -> imageMap.put(identifier, NativeImage.load(arena, identifier.toResourcePath(Identifier.ROOT_ASSETS, null, null))));

            final STBRPContext context = new STBRPContext(arena);
            final STBRPNode nodes = new STBRPNode(arena, numIds);
            final STBRPRect rects = new STBRPRect(arena, numIds);
            int mipmapLevel = 4;
            for (int i = 0; i < numIds; i++) {
                final NativeImage image = imageMap.get(identifierList.get(i));
                final int width = image.width();
                final int height = image.height();
                if (!isPowerOfTwo(width) || !isPowerOfTwo(height)) {
                    mipmapLevel = 0;
                } else if (mipmapLevel > 0) {
                    mipmapLevel = Math.min(Integer.numberOfTrailingZeros(width), Integer.numberOfTrailingZeros(height));
                }
                rects.id.set(i, i);
                rects.w.set(i, width);
                rects.h.set(i, height);
            }

            int packerSize = 256;
            do {
                stbrp.initTarget(context, packerSize, packerSize, nodes, numIds);
                stbrp.setupHeuristic(context, STBRectPack.HEURISTIC_Skyline_BF_sortHeight);
                packerSize *= 2;
            } while (stbrp.packRects(context, rects, numIds) == 0);
            packerSize /= 2;

            final Map<Identifier, TextureRegion> regionMap = HashMap.newHashMap(numIds);
            final GL gl = GameRenderer.OpenGL.get();
            final int id = gl.genTextures();
            gl.bindTexture(GL10C.TEXTURE_2D, id);
            gl.texParameteri(GL10C.TEXTURE_2D, GL10C.TEXTURE_MIN_FILTER, mipmapLevel > 0 ? GL10C.NEAREST_MIPMAP_NEAREST : GL10C.NEAREST);
            gl.texParameteri(GL10C.TEXTURE_2D, GL10C.TEXTURE_MAG_FILTER, GL10C.NEAREST);
            gl.texParameteri(GL10C.TEXTURE_2D, GL.TEXTURE_MAX_LEVEL, mipmapLevel);
            gl.texImage2D(GL10C.TEXTURE_2D,
                0,
                GL10C.RGBA,
                packerSize,
                packerSize,
                0,
                GL10C.RGBA,
                GL10C.UNSIGNED_BYTE,
                MemorySegment.NULL);
            for (int i = 0; i < numIds; i++) {
                if (rects.wasPacked.get(i) != 0) {
                    final Identifier identifier = identifierList.get(rects.id.get(i));
                    final int xo = rects.x.get(i);
                    final int yo = rects.y.get(i);
                    final int width = rects.w.get(i);
                    final int height = rects.h.get(i);
                    regionMap.put(identifier, new TextureRegion(xo, yo, width, height));
                    gl.texSubImage2D(GL10C.TEXTURE_2D,
                        0,
                        xo,
                        yo,
                        width,
                        height,
                        GL10C.RGBA,
                        GL10C.UNSIGNED_BYTE,
                        imageMap.get(identifier).segment());
                }
            }
            if (mipmapLevel > 0) {
                gl.generateMipmap(GL10C.TEXTURE_2D);
            }
            gl.bindTexture(GL10C.TEXTURE_2D, 0);
            return new TextureAtlas(id, packerSize, packerSize, mipmapLevel, regionMap);
        }
    }

    public TextureRegion getRegion(Identifier identifier) {
        return regionMap.get(identifier);
    }
}
