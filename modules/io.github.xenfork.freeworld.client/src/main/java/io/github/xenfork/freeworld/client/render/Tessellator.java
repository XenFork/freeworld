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
import io.github.xenfork.freeworld.client.render.gl.GLProgram;
import io.github.xenfork.freeworld.client.render.model.VertexLayouts;
import overrungl.opengl.GL;
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
 * <p>
 * The tessellator is <strong>NOT</strong> thread-safe and can only be used in render thread.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class Tessellator {
    private static final int MAX_VERTEX_COUNT = 60000;
    private static final int MAX_INDEX_COUNT = 90000;
    private static final StructLayout LAYOUT = VertexLayouts.POSITION_COLOR_TEX;
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

    public static void free() {
        final GL gl = GameRenderer.OpenGL.get();
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

    public void emit() {
        // check vertex count
        if ((vertexCount + 1) > MAX_VERTEX_COUNT) {
            flush();
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

    public void indexWithOffset(int offset, int... indices) {
        if (indexCount + indices.length > MAX_INDEX_COUNT) {
            flush();
        }
        for (int i = 0; i < indices.length; i++) {
            indexBuffer.setAtIndex(ValueLayout.JAVA_INT, indexCount + i, indices[i] + offset);
        }
        indexCount += indices.length;
    }

    public void index(int... indices) {
        indexWithOffset(vertexCount, indices);
    }

    public void flush() {
        if (!drawing) throw new IllegalStateException("Do not call Tessellator.flush when not drawing");
        final GL gl = GameRenderer.OpenGL.get();

        final boolean firstFlush = vao == 0;
        if (vao == 0) vao = gl.genVertexArrays();
        if (vbo == 0) vbo = gl.genBuffers();
        if (ebo == 0) ebo = gl.genBuffers();

        gl.bindVertexArray(vao);
        gl.bindBuffer(GL15C.ARRAY_BUFFER, vbo);
        if (firstFlush) {
            gl.bufferData(GL15C.ARRAY_BUFFER, buffer, GL15C.STREAM_DRAW);
            gl.enableVertexAttribArray(GLProgram.INPUT_POSITION);
            gl.enableVertexAttribArray(GLProgram.INPUT_COLOR);
            gl.enableVertexAttribArray(GLProgram.INPUT_UV);
            gl.vertexAttribPointer(GLProgram.INPUT_POSITION, 3, GL10C.FLOAT, false, Math.toIntExact(LAYOUT.byteSize()), MemorySegment.NULL);
            gl.vertexAttribPointer(GLProgram.INPUT_COLOR, 4, GL10C.UNSIGNED_BYTE, true, Math.toIntExact(LAYOUT.byteSize()), MemorySegment.ofAddress(3 * 4));
            gl.vertexAttribPointer(GLProgram.INPUT_UV, 2, GL10C.FLOAT, false, Math.toIntExact(LAYOUT.byteSize()), MemorySegment.ofAddress(3 * 4 + 4));
        } else {
            gl.bufferSubData(GL15C.ARRAY_BUFFER, 0L, LAYOUT.scale(0L, vertexCount), buffer);
        }
        gl.bindBuffer(GL15C.ARRAY_BUFFER, 0);
        gl.bindBuffer(GL15C.ELEMENT_ARRAY_BUFFER, ebo);
        if (firstFlush) {
            gl.bufferData(GL15C.ELEMENT_ARRAY_BUFFER, indexBuffer, GL15C.STREAM_DRAW);
        } else {
            gl.bufferSubData(GL15C.ELEMENT_ARRAY_BUFFER, 0L, ValueLayout.JAVA_INT.scale(0L, indexCount), indexBuffer);
        }
        gl.drawElements(drawMode.value(), indexCount, GL10C.UNSIGNED_INT, MemorySegment.NULL);
        gl.bindVertexArray(0);

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

    public void end() {
        if (!drawing) throw new IllegalStateException("Do not call Tessellator.end when not drawing");
        flush();
        drawing = false;
    }
}
