/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.vertex;

import freeworld.math.Matrix4f;
import freeworld.math.Vector3f;
import freeworld.math.Vector4f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static freeworld.client.util.ColorUtil.colorToInt;

/**
 * @author squid233
 * @since 0.1.0
 */
public interface VertexBuilder {
    @Deprecated
    void reset();

    @Deprecated
    VertexBuilder indicesWithOffset(int offset, int... indices);

    @Deprecated
    VertexBuilder indices(int... indices);

    default VertexBuilder position(float x, float y, float z) {
        putFloat(0L, x);
        putFloat(4L, y);
        putFloat(8L, z);
        nextElement();
        return this;
    }

    default VertexBuilder position(Matrix4f positionMatrix, float x, float y, float z) {
        Vector4f v = new Vector4f(x, y, z, 1).mul(positionMatrix);
        return position(v.x(), v.y(), v.z());
    }

    default VertexBuilder position(Matrix4f positionMatrix, Vector3f v) {
        return position(positionMatrix, v.x(), v.y(), v.z());
    }

    default VertexBuilder color(int red, int green, int blue, int alpha) {
        putByte(0L, (byte) red);
        putByte(1L, (byte) green);
        putByte(2L, (byte) blue);
        putByte(3L, (byte) alpha);
        nextElement();
        return this;
    }

    default VertexBuilder color(int red, int green, int blue) {
        return color(red, green, blue, 0xff);
    }

    default VertexBuilder color(float red, float green, float blue, float alpha) {
        return color(colorToInt(red), colorToInt(green), colorToInt(blue), colorToInt(alpha));
    }

    default VertexBuilder color(float red, float green, float blue) {
        return color(colorToInt(red), colorToInt(green), colorToInt(blue), 0xff);
    }

    default VertexBuilder texCoord(float u, float v) {
        putFloat(0L, u);
        putFloat(4L, v);
        nextElement();
        return this;
    }

    void putByte(long offset, byte b);

    void putFloat(long offset, float f);

    void nextElement();

    void emit();

    @Deprecated
    int vertexCount();

    @Deprecated
    int indexCount();

    @Deprecated
    MemorySegment vertexData();

    @Deprecated
    MemorySegment indexData();

    @Deprecated
    MemorySegment vertexDataSlice();

    @Deprecated
    default MemorySegment indexDataSlice() {
        return indexData().asSlice(0L, ValueLayout.JAVA_INT.scale(0L, indexCount()));
    }

    @Deprecated
    boolean shouldReallocateVertexData();

    @Deprecated
    boolean shouldReallocateIndexData();

    @Deprecated
    VertexLayout vertexLayout();
}
