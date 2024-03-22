/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.render;

import io.github.xenfork.freeworld.client.render.builder.DefaultVertexBuilder;
import io.github.xenfork.freeworld.client.render.builder.VertexBuilder;
import io.github.xenfork.freeworld.client.render.gl.GLDrawMode;
import io.github.xenfork.freeworld.client.render.gl.GLResource;
import io.github.xenfork.freeworld.client.render.gl.GLStateMgr;
import io.github.xenfork.freeworld.client.render.model.VertexLayout;
import io.github.xenfork.freeworld.client.render.model.VertexLayouts;
import overrungl.opengl.GL10C;
import overrungl.opengl.GL15C;

import java.lang.foreign.MemorySegment;

/**
 * A tessellator that allows rendering things dynamically.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class Tessellator implements GLResource, VertexBuilder {
    private static final int MAX_VERTEX_COUNT = 60000;
    private static final int MAX_INDEX_COUNT = 90000;
    private static final VertexLayout VERTEX_LAYOUT = VertexLayouts.POSITION_COLOR_TEX;
    private final VertexBuilder vertexBuilder = new DefaultVertexBuilder(VERTEX_LAYOUT, MAX_VERTEX_COUNT, MAX_INDEX_COUNT);
    private boolean drawing = false;
    private GLDrawMode drawMode = GLDrawMode.TRIANGLES;
    private int vao = 0;
    private int vbo = 0;
    private int ebo = 0;

    @Override
    public Tessellator position(float x, float y, float z) {
        vertexBuilder.position(x, y, z);
        return this;
    }

    @Override
    public Tessellator color(int red, int green, int blue, int alpha) {
        vertexBuilder.color(red, green, blue, alpha);
        return this;
    }

    @Override
    public Tessellator color(float red, float green, float blue, float alpha) {
        vertexBuilder.color(red, green, blue, alpha);
        return this;
    }

    @Override
    public Tessellator color(int red, int green, int blue) {
        vertexBuilder.color(red, green, blue);
        return this;
    }

    @Override
    public Tessellator color(float red, float green, float blue) {
        vertexBuilder.color(red, green, blue);
        return this;
    }

    @Override
    public Tessellator texCoord(float u, float v) {
        vertexBuilder.texCoord(u, v);
        return this;
    }

    @Override
    public void emit() {
        vertexBuilder.emit();
    }

    @Override
    public Tessellator indicesWithOffset(int offset, int... indices) {
        vertexBuilder.indicesWithOffset(offset, indices);
        return this;
    }

    @Override
    public Tessellator indices(int... indices) {
        vertexBuilder.indices(indices);
        return this;
    }

    public void flush(GLStateMgr gl) {
        if (!drawing) throw new IllegalStateException("Do not call Tessellator.flush when not drawing");

        final boolean firstFlush = vao == 0;
        if (vao == 0) vao = gl.genVertexArrays();
        if (vbo == 0) vbo = gl.genBuffers();
        if (ebo == 0) ebo = gl.genBuffers();

        final MemorySegment vertexData = vertexBuilder.vertexDataSlice();
        final MemorySegment indexData = vertexBuilder.indexDataSlice();
        final int indexCount = vertexBuilder.indexCount();
        gl.setVertexArrayBinding(vao);
        gl.setArrayBufferBinding(vbo);
        if (firstFlush || vertexBuilder.shouldReallocateVertexData()) {
            gl.bufferData(GL15C.ARRAY_BUFFER, vertexData, GL15C.STREAM_DRAW);
            VERTEX_LAYOUT.enableAttribs(gl);
            VERTEX_LAYOUT.specifyAttribPointers(gl);
        } else {
            gl.bufferSubData(GL15C.ARRAY_BUFFER, 0L, vertexData);
        }
        gl.bindBuffer(GL15C.ELEMENT_ARRAY_BUFFER, ebo);
        if (firstFlush || vertexBuilder.shouldReallocateIndexData()) {
            gl.bufferData(GL15C.ELEMENT_ARRAY_BUFFER, indexData, GL15C.STREAM_DRAW);
        } else {
            gl.bufferSubData(GL15C.ELEMENT_ARRAY_BUFFER, 0L, indexData);
        }
        gl.drawElements(drawMode.value(), indexCount, GL10C.UNSIGNED_INT, MemorySegment.NULL);
    }

    public void begin(GLDrawMode drawMode) {
        if (drawing) throw new IllegalStateException("Do not call Tessellator.begin while drawing");
        vertexBuilder.reset();
        drawing = true;
        this.drawMode = drawMode;
    }

    public void end(GLStateMgr gl) {
        if (!drawing) throw new IllegalStateException("Do not call Tessellator.end when not drawing");
        flush(gl);
        drawing = false;
    }

    @Override
    public void reset() {
        vertexBuilder.reset();
    }

    @Override
    public int vertexCount() {
        return vertexBuilder.vertexCount();
    }

    @Override
    public int indexCount() {
        return vertexBuilder.indexCount();
    }

    @Override
    public MemorySegment vertexData() {
        return vertexBuilder.vertexData();
    }

    @Override
    public MemorySegment indexData() {
        return vertexBuilder.indexData();
    }

    @Override
    public MemorySegment vertexDataSlice() {
        return vertexBuilder.vertexDataSlice();
    }

    @Override
    public boolean shouldReallocateVertexData() {
        return vertexBuilder.shouldReallocateVertexData();
    }

    @Override
    public boolean shouldReallocateIndexData() {
        return vertexBuilder.shouldReallocateIndexData();
    }

    @Override
    public void close(GLStateMgr gl) {
        gl.deleteVertexArrays(vao);
        gl.deleteBuffers(vbo, ebo);
    }
}
