/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.builder;

import freeworld.client.render.model.vertex.VertexLayout;
import freeworld.math.Matrix4f;
import freeworld.math.Vector3f;
import freeworld.math.Vector4f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static freeworld.client.util.Conversions.colorToInt;

/**
 * @author squid233
 * @since 0.1.0
 */
public interface VertexBuilder {
    void reset();

    VertexBuilder indicesWithOffset(int offset, int... indices);

    VertexBuilder indices(int... indices);

    default VertexBuilder position(float x, float y, float z) {
        nextElement(x);
        nextElement(y);
        nextElement(z);
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
        nextElement((byte) red);
        nextElement((byte) green);
        nextElement((byte) blue);
        nextElement((byte) alpha);
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
        nextElement(u);
        nextElement(v);
        return this;
    }

    void nextElement(byte b);

    void nextElement(float f);

    void nextElement(int i);

    void nextPadding(int size);

    void emit();

    int vertexCount();

    int indexCount();

    MemorySegment vertexData();

    MemorySegment indexData();

    default MemorySegment vertexDataSlice() {
        return vertexData().asSlice(0L, (long) vertexLayout().stride() * vertexCount());
    }

    default MemorySegment indexDataSlice() {
        return indexData().asSlice(0L, ValueLayout.JAVA_INT.scale(0L, indexCount()));
    }

    boolean shouldReallocateVertexData();

    boolean shouldReallocateIndexData();

    VertexLayout vertexLayout();
}
