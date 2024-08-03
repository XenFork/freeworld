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

import freeworld.client.render.gl.GLDrawMode;
import freeworld.util.Logging;
import org.slf4j.Logger;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.List;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BufferBuilder implements VertexBuilder {
    private static final Logger logger = Logging.caller();
    private GLDrawMode drawMode;
    private VertexLayout vertexLayout;
    private MemorySegment vertexData;
    private MemorySegment indexData;
    private boolean building = false;
    private int maxIndexCount;
    private long vertexDataOffset = 0L;
    private int vertexCount = 0;
    private int indexCount = 0;
    private VertexLayoutElement currentElement;
    private int currentElementIndex = 0;

    public BufferBuilder(long initialCapacity, int indexCount) {
        if (initialCapacity <= 0) throw new IllegalArgumentException("initialCapacity <= 0: " + initialCapacity);
        if (indexCount <= 0) throw new IllegalArgumentException("indexCount <= 0: " + indexCount);

        this.vertexData = Arena.ofAuto().allocate(initialCapacity);
        this.indexData = Arena.ofAuto().allocate(ValueLayout.JAVA_INT, indexCount);
        this.maxIndexCount = indexCount;
    }

    public record BufferData(
        MemorySegment vertexData,
        MemorySegment indexData,
        DrawParameter drawParameter
    ) {
    }

    public record DrawParameter(
        GLDrawMode drawMode,
        int indexCount
    ) {
    }

    public void begin(GLDrawMode drawMode, VertexLayout layout) {
        if (building) {
            throw new IllegalStateException("Already building");
        }
        this.building = true;
        this.drawMode = drawMode;
        this.vertexLayout = layout;
        this.currentElement = layout.elements().getFirst();
    }

    public BufferData end() {
        if (!building) {
            throw new IllegalStateException("Not building");
        }
        building = false;
        BufferData buffer = build();
        reset();
        return buffer;
    }

    private BufferData build() {
        return new BufferData(
            vertexData.asSlice(0L, vertexDataOffset),
            indexData.asSlice(0L, ValueLayout.JAVA_INT.scale(0L, indexCount)),
            new DrawParameter(
                drawMode,
                indexCount
            )
        );
    }

    private void reset() {
        vertexDataOffset = 0L;
        vertexCount = 0;
        indexCount = 0;
        currentElement = null;
        currentElementIndex = 0;
    }

    @Override
    public BufferBuilder indicesWithOffset(int offset, int... indices) {
        final int length = indices.length;
        if (indexCount + length > maxIndexCount) {
            logger.debug("Exceeds max index count: {} + {} > {}; expanding", indexCount, length, maxIndexCount);
            maxIndexCount = maxIndexCount * 3 / 2;
            MemorySegment prevData = indexData;
            indexData = Arena.ofAuto().allocate(ValueLayout.JAVA_INT, maxIndexCount).copyFrom(prevData);
        }
        for (int i = 0; i < indices.length; i++) {
            indexData.setAtIndex(ValueLayout.JAVA_INT, indexCount + i, indices[i] + offset);
        }
        indexCount += length;
        return this;
    }

    @Override
    public BufferBuilder indices(int... indices) {
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
    }
}
