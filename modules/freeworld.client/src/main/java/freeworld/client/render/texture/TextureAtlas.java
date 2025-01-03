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
import freeworld.util.Identifier;
import freeworld.util.Logging;
import org.slf4j.Logger;
import overrungl.stb.STBRPContext;
import overrungl.stb.STBRPNode;
import overrungl.stb.STBRPRect;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static overrungl.opengl.GL10.*;
import static overrungl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;
import static overrungl.stb.STBRectPack.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class TextureAtlas extends Texture2D {
    private static final Logger logger = Logging.caller();
    private final Map<Identifier, TextureRegion> regionMap;
    private final Map<Identifier, Identifier> aliasMap;

    private TextureAtlas(int id, int width, int height, int mipmapLevel, Map<Identifier, TextureRegion> regionMap) {
        super(id, width, height, mipmapLevel);
        this.regionMap = regionMap;
        this.aliasMap = new HashMap<>();
    }

    public static TextureAtlas load(GLStateMgr gl, Set<Identifier> identifierSet, int initMipmapLevel) {
        var identifierList = List.copyOf(identifierSet);
        final int numIds = identifierList.size();
        try (Arena arena = Arena.ofConfined()) {
            final Map<Identifier, NativeImage> imageMap = HashMap.newHashMap(numIds);
            identifierList.forEach(identifier -> {
                boolean missing = MISSING.equals(identifier);
                final NativeImage load = missing ?
                    NativeImage.fail() :
                    NativeImage.load(arena, "assets/" + identifier.withPath(s -> "texture/" + s + ".png").toResourcePath(), ImageFormats.RGBA);
                imageMap.put(identifier, load);
                if (load.failed() && !missing) {
                    logger.error("Failed to load texture {}", identifier);
                }
            });

            final STBRPContext context = STBRPContext.alloc(arena);
            final STBRPNode nodes = STBRPNode.alloc(arena, numIds);
            final STBRPRect rects = STBRPRect.alloc(arena, numIds);
            int mipmapLevel = initMipmapLevel;
            for (int i = 0; i < numIds; i++) {
                final NativeImage image = imageMap.get(identifierList.get(i));
                final int width = image.width();
                final int height = image.height();
                if (mipmapLevel != 0 &&
                    (!isPowerOfTwo(width) || !isPowerOfTwo(height))) {
                    mipmapLevel = 0;
                } else if (mipmapLevel > 0) {
                    mipmapLevel = Math.min(Integer.numberOfTrailingZeros(width), Integer.numberOfTrailingZeros(height));
                }
                rects.idAt(i, i)
                    .wAt(i, width)
                    .hAt(i, height);
            }

            int packerSize = 256;
            do {
                stbrp_init_target(context, packerSize, packerSize, nodes, numIds);
                stbrp_setup_heuristic(context, STBRP_HEURISTIC_Skyline_BF_sortHeight);
                packerSize *= 2;
            } while (stbrp_pack_rects(context, rects, numIds) == 0);
            packerSize /= 2;

            final Map<Identifier, TextureRegion> regionMap = HashMap.newHashMap(numIds);
            final int id = gl.GenTextures();
            final TextureAtlas atlas = new TextureAtlas(id, packerSize, packerSize, mipmapLevel, regionMap);
            RenderSystem.bindTexture2D(atlas);
            gl.TexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, mipmapLevel > 0 ? GL_NEAREST_MIPMAP_NEAREST : GL_NEAREST);
            gl.TexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl.TexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, mipmapLevel);
            gl.TexImage2D(GL_TEXTURE_2D,
                0,
                GL_RGBA,
                packerSize,
                packerSize,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                MemorySegment.NULL);
            for (int i = 0; i < numIds; i++) {
                if (rects.was_packedAt(i) != 0) {
                    final Identifier identifier = identifierList.get(rects.idAt(i));
                    final int xo = rects.xAt(i);
                    final int yo = rects.yAt(i);
                    final int width = rects.wAt(i);
                    final int height = rects.hAt(i);
                    regionMap.put(identifier, new TextureRegion(atlas, xo, yo, width, height));
                    NativeImage nativeImage = imageMap.get(identifier);
                    gl.TexSubImage2D(GL_TEXTURE_2D,
                        0,
                        xo,
                        yo,
                        width,
                        height,
                        nativeImage.formats().format().glEnum(),
                        GL_UNSIGNED_BYTE,
                        nativeImage.segment());
                }
            }
            if (mipmapLevel > 0) {
                gl.GenerateMipmap(GL_TEXTURE_2D);
            }
            return atlas;
        }
    }

    public void addAlias(Identifier original, Identifier alias) {
        aliasMap.put(alias, original);
    }

    public TextureRegion getRegion(Identifier identifier) {
        final TextureRegion region = regionMap.get(identifier);
        if (region != null) {
            return region;
        }
        return regionMap.get(aliasMap.get(identifier));
    }
}
