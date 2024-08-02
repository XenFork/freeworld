/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.text;

import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.texture.ImageFormats;
import freeworld.client.render.texture.ImagedTexture;
import freeworld.client.render.texture.NativeImage;
import freeworld.client.render.texture.Texture2D;
import freeworld.util.Identifier;
import freeworld.util.file.BuiltinFiles;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Unifont implements Font {
    public static final Identifier IDENTIFIER = Identifier.ofBuiltin("font/unifont");
    private static final int CODEPOINT_COUNT = 0xffff;
    private static final int TEXTURE_SIZE = 4096;
    private static final int GRID_SIZE = 16;
    private static final int GRID = TEXTURE_SIZE / GRID_SIZE;
    private final Map<Integer, Integer> glyphWidths = HashMap.newHashMap(CODEPOINT_COUNT);
    private Texture2D texture;

    public void load(GLStateMgr gl, Identifier identifier) {
        String path = "assets/" + identifier.toResourcePath();
        List<String[]> lines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(Objects.requireNonNull(BuiltinFiles.load(path)))))) {
            lines = reader.lines().map(s -> s.split(":", 2)).toList();
        } catch (Exception e) {
            throw new RuntimeException("failed to load font " + identifier + " from path " + path, e);
        }
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(ValueLayout.JAVA_BYTE, TEXTURE_SIZE * TEXTURE_SIZE);
            for (String[] line : lines) {
                String data = line[1];
                int codePoint = Integer.parseInt(line[0], 16);
                boolean halfWidth = data.length() <= 32;
                int stride = halfWidth ? 2 : 4;
                int width = halfWidth ? 8 : 16;
                final int height = GRID_SIZE;
                int x = codePoint % GRID * GRID_SIZE;
                int y = codePoint / GRID * GRID_SIZE;
                glyphWidths.put(codePoint, width);
                for (int y1 = 0; y1 < height; y1++) {
                    int dataIndex = y1 * stride;
                    int hexData = Integer.parseInt(data.substring(dataIndex, dataIndex + stride), 16);
                    for (int x1 = 0; x1 < width; x1++) {
                        int bit = (hexData >> (width - 1 - x1)) & 1;
                        segment.setAtIndex(ValueLayout.JAVA_BYTE, (long) (y + y1) * TEXTURE_SIZE + (x + x1), (byte) (bit != 0 ? 0xff : 0));
                    }
                }
            }
            texture = ImagedTexture.of(gl, NativeImage.of(4096, 4096, segment, ImageFormats.UNIFONT));
        }
    }

    public Texture2D texture() {
        return texture;
    }

    @Override
    public int width(int codePoint) {
        return glyphWidths.computeIfAbsent(codePoint, _ -> 16) / 2;
    }

    @Override
    public int height(int codePoint) {
        return 8;
    }

    @Override
    public int lineHeight() {
        return 8;
    }

    @Override
    public float u0(int codePoint) {
        return (float) ((codePoint % GRID) * GRID_SIZE) / texture().width();
    }

    @Override
    public float v0(int codePoint) {
        return (float) (codePoint / GRID * GRID_SIZE) / texture().height();
    }

    @Override
    public float u1(int codePoint) {
        return u0(codePoint) + (float) width(codePoint) * 2 / texture().width();
    }

    @Override
    public float v1(int codePoint) {
        return v0(codePoint) + (float) height(codePoint) * 2 / texture().height();
    }
}
