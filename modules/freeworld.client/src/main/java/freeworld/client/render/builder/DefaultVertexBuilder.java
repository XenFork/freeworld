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

import freeworld.client.render.model.vertex.VertexFormat;
import freeworld.client.render.model.vertex.VertexLayout;
import freeworld.math.Matrix4f;
import freeworld.util.Logging;
import org.slf4j.Logger;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.List;
import java.util.Objects;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class DefaultVertexBuilder implements VertexBuilder {
    private static final Logger logger = Logging.caller();
    private final VertexLayout vertexLayout;
    private Arena vertexArena;
    private Arena indexArena;
    private MemorySegment vertexData;
    private MemorySegment indexData;
    private boolean shouldReallocateVertexData = true;
    private boolean shouldReallocateIndexData = true;
    private int maxVertexCount;
    private int maxIndexCount;
    private int prevVertexCount = 0;
    private int prevIndexCount = 0;
    private int vertexCount = 0;
    private int indexCount = 0;
    private final int elementCount;
    private int currentElement = 0;
    private int currentElementOffset = 0;
    private final MemorySegment elementsBuffer;
    @Deprecated
    private float x = 0f, y = 0f, z = 0f;
    @Deprecated
    private int r = 0xff, g = 0xff, b = 0xff, a = 0xff;
    @Deprecated
    private float u = 0f, v = 0f;

    public DefaultVertexBuilder(VertexLayout layout, int vertexCount, int indexCount) {
        Objects.requireNonNull(layout);
        if (vertexCount <= 0) throw new IllegalArgumentException(STR."vertexCount <= 0: \{vertexCount}");
        if (indexCount <= 0) throw new IllegalArgumentException(STR."indexCount <= 0: \{indexCount}");

        final List<VertexFormat> formats = layout.formats();

        this.vertexLayout = layout;
        this.vertexArena = Arena.ofAuto();
        this.indexArena = Arena.ofAuto();
        this.vertexData = vertexArena.allocate((long) layout.stride() * vertexCount);
        this.indexData = indexArena.allocate(ValueLayout.JAVA_INT, indexCount);
        this.maxVertexCount = vertexCount;
        this.maxIndexCount = indexCount;

        int elementCount = 0;
        for (VertexFormat format : formats) {
            elementCount += format.elementCount();
        }
        this.elementCount = elementCount;
        this.elementsBuffer = Arena.ofAuto().allocate(layout.stride());
    }

    @Override
    public void reset() {
        prevVertexCount = vertexCount;
        prevIndexCount = indexCount;
        vertexCount = 0;
        indexCount = 0;
        currentElement = 0;
        currentElementOffset = 0;
    }

    @Override
    public DefaultVertexBuilder indicesWithOffset(int offset, int... indices) {
        final int length = indices.length;
        if (indexCount + length > maxIndexCount) {
            logger.debug("Exceeds max index count: {} + {} > {}; expanding", indexCount, length, maxIndexCount);
            maxIndexCount = maxIndexCount * 3 / 2;
            indexArena = Arena.ofAuto();
            indexData = indexArena.allocate(ValueLayout.JAVA_INT, maxIndexCount);
            shouldReallocateIndexData = true;
        }
        for (int i = 0; i < indices.length; i++) {
            indexData.setAtIndex(ValueLayout.JAVA_INT, indexCount + i, indices[i] + offset);
        }
        indexCount += length;
        if (indexCount > prevIndexCount) {
            shouldReallocateIndexData = true;
        }
        return this;
    }

    @Override
    public DefaultVertexBuilder indices(int... indices) {
        return indicesWithOffset(vertexCount, indices);
    }

    @Override
    public DefaultVertexBuilder position(float x, float y, float z) {
        VertexBuilder.super.position(x, y, z);
        return this;
    }

    @Override
    public DefaultVertexBuilder position(Matrix4f positionMatrix, float x, float y, float z) {
        VertexBuilder.super.position(positionMatrix, x, y, z);
        return this;
    }

    @Override
    public DefaultVertexBuilder color(int red, int green, int blue, int alpha) {
        VertexBuilder.super.color(red, green, blue, alpha);
        return this;
    }

    @Override
    public DefaultVertexBuilder color(int red, int green, int blue) {
        VertexBuilder.super.color(red, green, blue);
        return this;
    }

    @Override
    public DefaultVertexBuilder color(float red, float green, float blue, float alpha) {
        VertexBuilder.super.color(red, green, blue, alpha);
        return this;
    }

    @Override
    public DefaultVertexBuilder color(float red, float green, float blue) {
        VertexBuilder.super.color(red, green, blue);
        return this;
    }

    @Override
    public DefaultVertexBuilder texCoord(float u, float v) {
        VertexBuilder.super.texCoord(u, v);
        return this;
    }

    private void checkElement() {
        if (currentElementOffset >= elementsBuffer.byteSize()) {
            throw new IllegalStateException("Element out of bounds: " + currentElementOffset + " >= " + elementsBuffer.byteSize());
        }
    }

    private void increaseElement(int count, int byteSize) {
        currentElement += count;
        currentElementOffset += byteSize;
        if (currentElement >= elementCount) {
            currentElement = 0;
            currentElementOffset = 0;
        }
    }

    @Override
    public void nextElement(byte b) {
        checkElement();
        elementsBuffer.set(ValueLayout.JAVA_BYTE, currentElementOffset, b);
        increaseElement(1, 1);
    }

    @Override
    public void nextElement(float f) {
        checkElement();
        elementsBuffer.set(ValueLayout.JAVA_FLOAT, currentElementOffset, f);
        increaseElement(1, 4);
    }

    @Override
    public void nextElement(int i) {
        checkElement();
        elementsBuffer.set(ValueLayout.JAVA_INT, currentElementOffset, i);
        increaseElement(1, 4);
    }

    @Override
    public void nextPadding(int size) {
        checkElement();
        elementsBuffer.asSlice(currentElementOffset, size).fill((byte) 0);
        increaseElement(size, size);
    }

    @Override
    public void emit() {
        if (currentElement % elementCount != 0) {
            throw new IllegalStateException("Incomplete element with layout " + vertexLayout);
        }
        if (vertexCount + 1 > maxVertexCount) {
            logger.debug("Exceeds max vertex count: {}; expanding", maxVertexCount);
            maxVertexCount = maxVertexCount * 3 / 2;
            vertexArena = Arena.ofAuto();
            vertexData = vertexArena.allocate((long) vertexLayout.stride() * maxVertexCount);
            shouldReallocateVertexData = true;
        }
        MemorySegment.copy(elementsBuffer, 0L, vertexData, (long) vertexLayout.stride() * vertexCount, elementsBuffer.byteSize());
        vertexCount++;
        if (vertexCount > prevVertexCount) {
            shouldReallocateVertexData = true;
        }
    }

    @Override
    public int vertexCount() {
        return vertexCount;
    }

    @Override
    public int indexCount() {
        return indexCount;
    }

    @Override
    public MemorySegment vertexData() {
        return vertexData;
    }

    @Override
    public MemorySegment indexData() {
        return indexData;
    }

    @Override
    public boolean shouldReallocateVertexData() {
        return shouldReallocateVertexData;
    }

    @Override
    public boolean shouldReallocateIndexData() {
        return shouldReallocateIndexData;
    }

    @Override
    public VertexLayout vertexLayout() {
        return vertexLayout;
    }
}
