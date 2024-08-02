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
    private MemorySegment vertexData;
    private MemorySegment indexData;
    private boolean shouldReallocateVertexData = true;
    private boolean shouldReallocateIndexData = true;
    private int maxIndexCount;
    private long prevVertexDataOffset = 0L;
    private long vertexDataOffset = 0L;
    private int prevIndexCount = 0;
    private int vertexCount = 0;
    private int indexCount = 0;
    private VertexLayoutElement currentElement;
    private int currentElementIndex = 0;

    public DefaultVertexBuilder(VertexLayout layout, int vertexCount, int indexCount) {
        Objects.requireNonNull(layout);
        if (vertexCount <= 0) throw new IllegalArgumentException("vertexCount <= 0: " + vertexCount);
        if (indexCount <= 0) throw new IllegalArgumentException("indexCount <= 0: " + indexCount);

        this.vertexLayout = layout;
        this.vertexData = Arena.ofAuto().allocate((long) layout.stride() * vertexCount);
        this.indexData = Arena.ofAuto().allocate(ValueLayout.JAVA_INT, indexCount);
        this.maxIndexCount = indexCount;
    }

    @Override
    public void reset() {
        prevVertexDataOffset = vertexDataOffset;
        vertexDataOffset = 0L;
        prevIndexCount = indexCount;
        vertexCount = 0;
        indexCount = 0;
        // TODO: 2024/8/3 squid233: begin(VertexLayout)
//        currentElement = null;
        currentElement = vertexLayout.elements().getFirst();
        currentElementIndex = 0;
    }

    @Override
    public DefaultVertexBuilder indicesWithOffset(int offset, int... indices) {
        final int length = indices.length;
        if (indexCount + length > maxIndexCount) {
            logger.debug("Exceeds max index count: {} + {} > {}; expanding", indexCount, length, maxIndexCount);
            maxIndexCount = maxIndexCount * 3 / 2;
            MemorySegment prevData = indexData;
            indexData = Arena.ofAuto().allocate(ValueLayout.JAVA_INT, maxIndexCount).copyFrom(prevData);
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

    private void grow() {
        grow(vertexLayout.stride());
    }

    private void grow(long size) {
        long byteSize = vertexData.byteSize();
        if (vertexDataOffset + size > byteSize) {
            logger.debug("Exceeds max vertex data size: {}; expanding", byteSize);
            MemorySegment prevData = vertexData;
            vertexData = Arena.ofAuto().allocate(byteSize * 3 / 2).copyFrom(prevData);
            shouldReallocateVertexData = true;
        }
    }

    @Override
    public void putByte(long offset, byte b) {
        vertexData.set(ValueLayout.JAVA_BYTE, vertexDataOffset + offset, b);
    }

    @Override
    public void putFloat(long offset, float f) {
        vertexData.set(ValueLayout.JAVA_FLOAT, vertexDataOffset + offset, f);
    }

    @Override
    public void nextElement() {
        List<VertexLayoutElement> formats = vertexLayout.elements();
        currentElementIndex = (currentElementIndex + 1) % formats.size();
        vertexDataOffset += this.currentElement.byteSize();
        VertexLayoutElement element = formats.get(currentElementIndex);
        this.currentElement = element;
        if (element.format() == VertexFormat.PADDING) {
            nextElement();
        }
    }

    @Override
    public void emit() {
        if (currentElementIndex != 0) {
            throw new IllegalStateException("Not filled all elements");
        }
        vertexCount++;
        grow();
        if (vertexDataOffset > prevVertexDataOffset) {
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
    public MemorySegment vertexDataSlice() {
        return vertexData.asSlice(0L, vertexDataOffset);
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
