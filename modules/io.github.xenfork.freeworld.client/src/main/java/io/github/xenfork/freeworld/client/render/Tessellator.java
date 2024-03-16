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

import io.github.xenfork.freeworld.client.render.gl.GLDrawMode;
import io.github.xenfork.freeworld.client.render.gl.GLStateMgr;
import io.github.xenfork.freeworld.client.render.model.VertexLayout;
import io.github.xenfork.freeworld.client.render.model.VertexLayouts;
import overrungl.opengl.GL10C;
import overrungl.opengl.GL15C;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

import static io.github.xenfork.freeworld.client.util.Conversions.colorToInt;

/**
 * A tessellator that allows rendering things dynamically.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class Tessellator {
    private static final int MAX_VERTEX_COUNT = 60000;
    private static final int MAX_INDEX_COUNT = 90000;
    private static final VertexLayout VERTEX_LAYOUT = VertexLayouts.POSITION_COLOR_TEX;
    private static final StructLayout LAYOUT = VERTEX_LAYOUT.layout();
    private static final VarHandle X = LAYOUT.arrayElementVarHandle(
        PathElement.groupElement(VertexLayouts.NAME_POSITION),
        PathElement.sequenceElement(0L)
    );
    private static final VarHandle Y = LAYOUT.arrayElementVarHandle(
        PathElement.groupElement(VertexLayouts.NAME_POSITION),
        PathElement.sequenceElement(1L)
    );
    private static final VarHandle Z = LAYOUT.arrayElementVarHandle(
        PathElement.groupElement(VertexLayouts.NAME_POSITION),
        PathElement.sequenceElement(2L)
    );
    private static final VarHandle R = LAYOUT.arrayElementVarHandle(
        PathElement.groupElement(VertexLayouts.NAME_COLOR),
        PathElement.sequenceElement(0L)
    );
    private static final VarHandle G = LAYOUT.arrayElementVarHandle(
        PathElement.groupElement(VertexLayouts.NAME_COLOR),
        PathElement.sequenceElement(1L)
    );
    private static final VarHandle B = LAYOUT.arrayElementVarHandle(
        PathElement.groupElement(VertexLayouts.NAME_COLOR),
        PathElement.sequenceElement(2L)
    );
    private static final VarHandle A = LAYOUT.arrayElementVarHandle(
        PathElement.groupElement(VertexLayouts.NAME_COLOR),
        PathElement.sequenceElement(3L)
    );
    private static final VarHandle U = LAYOUT.arrayElementVarHandle(
        PathElement.groupElement(VertexLayouts.NAME_UV),
        PathElement.sequenceElement(0L)
    );
    private static final VarHandle V = LAYOUT.arrayElementVarHandle(
        PathElement.groupElement(VertexLayouts.NAME_UV),
        PathElement.sequenceElement(1L)
    );
    private final MemorySegment buffer;
    private final MemorySegment indexBuffer;
    private int vertexCount = 0;
    private int indexCount = 0;
    private boolean drawing = false;
    private GLDrawMode drawMode = GLDrawMode.TRIANGLES;
    private int vao = 0;
    private int vbo = 0;
    private int ebo = 0;
    private float x = 0f;
    private float y = 0f;
    private float z = 0f;
    private int red = 0;
    private int green = 0;
    private int blue = 0;
    private int alpha = 0xff;
    private float u = 0f;
    private float v = 0f;

    private Tessellator() {
        final Arena arena = Arena.ofAuto();
        buffer = arena.allocate(LAYOUT, MAX_VERTEX_COUNT);
        indexBuffer = arena.allocate(ValueLayout.JAVA_INT, MAX_INDEX_COUNT);
    }

    /**
     * @author squid233
     * @since 0.1.0
     */
    private static final class Holder {
        private static final Tessellator INSTANCE = new Tessellator();
    }

    public static Tessellator getInstance() {
        return Holder.INSTANCE;
    }

    public static void free(GLStateMgr gl) {
        gl.deleteVertexArrays(Holder.INSTANCE.vao);
        gl.deleteBuffers(Holder.INSTANCE.vbo, Holder.INSTANCE.ebo);
    }

    public Tessellator position(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Tessellator color(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        return this;
    }

    public Tessellator color(float red, float green, float blue, float alpha) {
        return color(colorToInt(red), colorToInt(green), colorToInt(blue), colorToInt(alpha));
    }

    public Tessellator color(int red, int green, int blue) {
        return color(red, green, blue, 0xff);
    }

    public Tessellator color(float red, float green, float blue) {
        return color(red, green, blue, 1f);
    }

    public Tessellator texCoord(float u, float v) {
        this.u = u;
        this.v = v;
        return this;
    }

    public void emit(GLStateMgr gl) {
        // check vertex count
        if ((vertexCount + 1) > MAX_VERTEX_COUNT) {
            flush(gl);
        }
        final long longVertexCount = vertexCount;
        X.set(buffer, 0L, longVertexCount, x);
        Y.set(buffer, 0L, longVertexCount, y);
        Z.set(buffer, 0L, longVertexCount, z);
        R.set(buffer, 0L, longVertexCount, (byte) (red & 0xff));
        G.set(buffer, 0L, longVertexCount, (byte) (green & 0xff));
        B.set(buffer, 0L, longVertexCount, (byte) (blue & 0xff));
        A.set(buffer, 0L, longVertexCount, (byte) (alpha & 0xff));
        U.set(buffer, 0L, longVertexCount, u);
        V.set(buffer, 0L, longVertexCount, v);
        vertexCount++;
    }

    public void indexWithOffset(GLStateMgr gl, int offset, int... indices) {
        if (indexCount + indices.length > MAX_INDEX_COUNT) {
            flush(gl);
        }
        for (int i = 0; i < indices.length; i++) {
            indexBuffer.setAtIndex(ValueLayout.JAVA_INT, indexCount + i, indices[i] + offset);
        }
        indexCount += indices.length;
    }

    public void index(GLStateMgr gl, int... indices) {
        indexWithOffset(gl, vertexCount, indices);
    }

    public void flush(GLStateMgr gl) {
        if (!drawing) throw new IllegalStateException("Do not call Tessellator.flush when not drawing");

        final boolean firstFlush = vao == 0;
        if (vao == 0) vao = gl.genVertexArrays();
        if (vbo == 0) vbo = gl.genBuffers();
        if (ebo == 0) ebo = gl.genBuffers();

        gl.setVertexArrayBinding(vao);
        gl.setArrayBufferBinding(vbo);
        if (firstFlush) {
            gl.bufferData(GL15C.ARRAY_BUFFER, buffer, GL15C.STREAM_DRAW);
            VERTEX_LAYOUT.enableAttribs(gl);
            VERTEX_LAYOUT.specifyAttribPointers(gl);
        } else {
            gl.bufferSubData(GL15C.ARRAY_BUFFER, 0L, LAYOUT.scale(0L, vertexCount), buffer);
        }
        gl.bindBuffer(GL15C.ELEMENT_ARRAY_BUFFER, ebo);
        if (firstFlush) {
            gl.bufferData(GL15C.ELEMENT_ARRAY_BUFFER, indexBuffer, GL15C.STREAM_DRAW);
        } else {
            gl.bufferSubData(GL15C.ELEMENT_ARRAY_BUFFER, 0L, ValueLayout.JAVA_INT.scale(0L, indexCount), indexBuffer);
        }
        gl.drawElements(drawMode.value(), indexCount, GL10C.UNSIGNED_INT, MemorySegment.NULL);

        vertexCount = 0;
        indexCount = 0;
    }

    public void begin(GLDrawMode drawMode) {
        if (drawing) throw new IllegalStateException("Do not call Tessellator.begin while drawing");
        vertexCount = 0;
        indexCount = 0;
        drawing = true;
        this.drawMode = drawMode;
    }

    public void end(GLStateMgr gl) {
        if (!drawing) throw new IllegalStateException("Do not call Tessellator.end when not drawing");
        flush(gl);
        drawing = false;
    }
}
