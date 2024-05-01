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

import freeworld.client.render.gl.GLStateMgr;
import freeworld.core.Identifier;
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

    public static TextureAtlas load(GLStateMgr gl, List<Identifier> identifierList) {
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
                STBRPRect.id.set(rects, i, i);
                STBRPRect.w.set(rects, i, width);
                STBRPRect.h.set(rects, i, height);
            }

            int packerSize = 256;
            do {
                stbrp.initTarget(context, packerSize, packerSize, nodes, numIds);
                stbrp.setupHeuristic(context, STBRectPack.HEURISTIC_Skyline_BF_sortHeight);
                packerSize *= 2;
            } while (stbrp.packRects(context, rects, numIds) == 0);
            packerSize /= 2;

            final Map<Identifier, TextureRegion> regionMap = HashMap.newHashMap(numIds);
            final int id = gl.genTextures();
            gl.setTextureBinding2D(id);
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
                if (STBRPRect.wasPacked.get(rects, i) != 0) {
                    final Identifier identifier = identifierList.get(STBRPRect.id.get(rects, i));
                    final int xo = STBRPRect.x.get(rects, i);
                    final int yo = STBRPRect.y.get(rects, i);
                    final int width = STBRPRect.w.get(rects, i);
                    final int height = STBRPRect.h.get(rects, i);
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
            return new TextureAtlas(id, packerSize, packerSize, mipmapLevel, regionMap);
        }
    }

    public TextureRegion getRegion(Identifier identifier) {
        return regionMap.get(identifier);
    }
}
