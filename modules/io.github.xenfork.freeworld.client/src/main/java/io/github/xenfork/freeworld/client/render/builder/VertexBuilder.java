/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.render.builder;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static io.github.xenfork.freeworld.client.util.Conversions.colorToInt;

/**
 * @author squid233
 * @since 0.1.0
 */
public interface VertexBuilder {
    void reset();

    VertexBuilder indicesWithOffset(int offset, int... indices);

    VertexBuilder indices(int... indices);

    VertexBuilder position(float x, float y, float z);

    VertexBuilder color(int red, int green, int blue, int alpha);

    default VertexBuilder color(int red, int green, int blue) {
        return color(red, green, blue, 0xff);
    }

    default VertexBuilder color(float red, float green, float blue, float alpha) {
        return color(colorToInt(red), colorToInt(green), colorToInt(blue), colorToInt(alpha));
    }

    default VertexBuilder color(float red, float green, float blue) {
        return color(colorToInt(red), colorToInt(green), colorToInt(blue), 0xff);
    }

    VertexBuilder texCoord(float u, float v);

    void emit();

    int vertexCount();

    int indexCount();

    MemorySegment vertexData();

    MemorySegment indexData();

    MemorySegment vertexDataSlice();

    default MemorySegment indexDataSlice() {
        return indexData().asSlice(0L, ValueLayout.JAVA_INT.scale(0L, indexCount()));
    }

    boolean shouldReallocateVertexData();

    boolean shouldReallocateIndexData();
}
