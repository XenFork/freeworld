/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.builder;

import freeworld.client.render.model.VertexFormat;
import freeworld.client.render.model.VertexLayout;
import freeworld.util.Logging;
import org.slf4j.Logger;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
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
    private final VarHandle vhPosition;
    private final VarHandle vhColor;
    private final VarHandle vhTexCoord;
    private int prevVertexCount = 0;
    private int prevIndexCount = 0;
    private int vertexCount = 0;
    private int indexCount = 0;
    private float x = 0f, y = 0f, z = 0f;
    private int r = 0xff, g = 0xff, b = 0xff, a = 0xff;
    private float u = 0f, v = 0f;

    public DefaultVertexBuilder(VertexLayout layout, int vertexCount, int indexCount) {
        Objects.requireNonNull(layout);
        if (vertexCount <= 0) throw new IllegalArgumentException(STR."vertexCount <= 0: \{vertexCount}");
        if (indexCount <= 0) throw new IllegalArgumentException(STR."indexCount <= 0: \{indexCount}");

        final StructLayout structLayout = layout.layout();
        final List<VertexFormat> formats = layout.formats();

        this.vertexLayout = layout;
        this.vertexArena = Arena.ofAuto();
        this.indexArena = Arena.ofAuto();
        this.vertexData = vertexArena.allocate(structLayout, vertexCount);
        this.indexData = indexArena.allocate(ValueLayout.JAVA_INT, indexCount);
        this.maxVertexCount = vertexCount;
        this.maxIndexCount = indexCount;

        if (formats.contains(VertexFormat.POSITION)) {
            this.vhPosition = structLayout.arrayElementVarHandle(PathElement.groupElement(VertexFormat.POSITION.name()), PathElement.sequenceElement());
        } else {
            throw new IllegalArgumentException(STR."Invalid vertex layout: \{layout}");
        }
        if (formats.contains(VertexFormat.COLOR)) {
            this.vhColor = structLayout.arrayElementVarHandle(PathElement.groupElement(VertexFormat.COLOR.name()), PathElement.sequenceElement());
        } else {
            this.vhColor = null;
        }
        if (formats.contains(VertexFormat.UV)) {
            this.vhTexCoord = structLayout.arrayElementVarHandle(PathElement.groupElement(VertexFormat.UV.name()), PathElement.sequenceElement());
        } else {
            this.vhTexCoord = null;
        }
    }

    @Override
    public void reset() {
        prevVertexCount = vertexCount;
        prevIndexCount = indexCount;
        vertexCount = 0;
        indexCount = 0;
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
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public DefaultVertexBuilder color(int red, int green, int blue, int alpha) {
        this.r = red;
        this.g = green;
        this.b = blue;
        this.a = alpha;
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
        this.u = u;
        this.v = v;
        return this;
    }

    @Override
    public void emit() {
        if (vertexCount + 1 > maxVertexCount) {
            logger.debug("Exceeds max vertex count: {}; expanding", maxVertexCount);
            maxVertexCount = maxVertexCount * 3 / 2;
            vertexArena = Arena.ofAuto();
            vertexData = vertexArena.allocate(vertexLayout.layout(), maxVertexCount);
            shouldReallocateVertexData = true;
        }
        final long count = vertexCount;
        vhPosition.set(vertexData, 0L, count, 0L, x);
        vhPosition.set(vertexData, 0L, count, 1L, y);
        vhPosition.set(vertexData, 0L, count, 2L, z);
        if (vhColor != null) {
            vhColor.set(vertexData, 0L, count, 0L, (byte) r);
            vhColor.set(vertexData, 0L, count, 1L, (byte) g);
            vhColor.set(vertexData, 0L, count, 2L, (byte) b);
            vhColor.set(vertexData, 0L, count, 3L, (byte) a);
        }
        if (vhTexCoord != null) {
            vhTexCoord.set(vertexData, 0L, count, 0L, u);
            vhTexCoord.set(vertexData, 0L, count, 1L, v);
        }
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
    public MemorySegment vertexDataSlice() {
        return vertexData().asSlice(0L, vertexLayout.layout().scale(0L, vertexCount()));
    }

    @Override
    public boolean shouldReallocateVertexData() {
        return shouldReallocateVertexData;
    }

    @Override
    public boolean shouldReallocateIndexData() {
        return shouldReallocateIndexData;
    }

    public VertexLayout vertexLayout() {
        return vertexLayout;
    }
}
