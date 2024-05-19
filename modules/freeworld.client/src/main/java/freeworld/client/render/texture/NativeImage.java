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

import freeworld.file.BuiltinFiles;
import freeworld.util.Logging;
import org.slf4j.Logger;
import overrun.marshal.Unmarshal;
import overrungl.stb.STBImage;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * A data class that represents a native image.
 * <p>
 * You should re-associate the memory segment with an arena and a cleanup action.
 *
 * @param width   the width
 * @param height  the height
 * @param segment the segment
 * @param failed  {@code true} if failed
 * @author squid233
 * @since 0.1.0
 */
public record NativeImage(int width, int height, MemorySegment segment, boolean failed) {
    private static final Logger logger = Logging.caller();

    public static NativeImage load(Arena arena, MemorySegment segment, String path) {
        if (Unmarshal.isNullPointer(segment)) {
            return fail();
        }
        final MemorySegment px = arena.allocate(ValueLayout.JAVA_INT);
        final MemorySegment py = arena.allocate(ValueLayout.JAVA_INT);
        final MemorySegment pc = arena.allocate(ValueLayout.JAVA_INT);
        final STBImage stbImage = STBImage.INSTANCE;
        final MemorySegment result = stbImage.loadFromMemory(segment, px, py, pc, STBImage.RGB_ALPHA);
        if (Unmarshal.isNullPointer(result)) {
            logger.error("Failed to load image from {}: {}", path, stbImage.failureReason());
            return fail();
        }
        return new NativeImage(
            px.get(ValueLayout.JAVA_INT, 0L),
            py.get(ValueLayout.JAVA_INT, 0L),
            result.reinterpret(arena, stbImage::free),
            false
        );
    }

    public static NativeImage load(Arena arena, String path) {
        return load(arena, BuiltinFiles.loadBinary(arena, BuiltinFiles.load(path), path), path);
    }

    public static NativeImage fail() {
        final class Holder {
            private static final NativeImage FAILED;

            static {
                final MemorySegment segment = Arena.global().allocate(16 * 16 * 4);
                for (int y = 0; y < 16; y++) {
                    for (int x = 0; x < 16; x++) {
                        segment.setAtIndex(ValueLayout.JAVA_INT, y * 16 + x, (x < 8 ^ y < 8) ? 0xff000000 : 0xffff00ff);
                    }
                }
                FAILED = new NativeImage(16, 16, segment, true);
            }
        }
        return Holder.FAILED;
    }
}
