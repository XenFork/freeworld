/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render;

import freeworld.client.render.gl.GLDrawMode;
import freeworld.client.render.gl.GLResource;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.gl.GLVertexArrayObject;
import freeworld.client.render.vertex.BufferBuilder;
import freeworld.client.render.vertex.VertexLayout;
import freeworld.client.render.vertex.VertexLayouts;
import overrungl.opengl.GL15C;

/**
 * A tessellator that allows rendering things dynamically.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class Tessellator implements GLResource {
    private static final int INITIAL_CAPACITY = 1024 * 1024 * 2;
    private static final int MAX_INDEX_COUNT = 90000;
    private static final VertexLayout VERTEX_LAYOUT = VertexLayouts.POSITION_COLOR_TEXTURE;
    private final BufferBuilder bufferBuilder = new BufferBuilder(INITIAL_CAPACITY, MAX_INDEX_COUNT);
    private boolean drawing = false;
    private GLDrawMode drawMode = GLDrawMode.TRIANGLES;
    private GLVertexArrayObject vertexArrayObject = null;

    private Tessellator() {
    }

    public static Tessellator getInstance() {
        final class Holder {
            private static final Tessellator INSTANCE = new Tessellator();
        }
        return Holder.INSTANCE;
    }

    public BufferBuilder buffer() {
        return bufferBuilder;
    }

    private void draw(GLStateMgr gl) {
        BufferBuilder.BufferData buffer = bufferBuilder.end();
        BufferBuilder.DrawParameter drawParameter = buffer.drawParameter();

        if (vertexArrayObject == null) {
            vertexArrayObject = new GLVertexArrayObject(gl, GL15C.STREAM_DRAW);
        }
        vertexArrayObject.specify(gl, buffer);
        vertexArrayObject.draw(gl);
        drawMode = drawParameter.drawMode();
    }

    @Deprecated
    public void flush(GLStateMgr gl) {
        if (!drawing) throw new IllegalStateException("Do not call Tessellator.flush when not drawing");

        draw(gl);
        bufferBuilder.begin(drawMode, VERTEX_LAYOUT);
    }

    @Deprecated
    public void begin(GLDrawMode drawMode) {
        if (drawing) throw new IllegalStateException("Do not call Tessellator.begin while drawing");
        bufferBuilder.begin(drawMode, VERTEX_LAYOUT);
        drawing = true;
    }

    @Deprecated
    public void end(GLStateMgr gl) {
        if (!drawing) throw new IllegalStateException("Do not call Tessellator.end when not drawing");
        draw(gl);
        drawing = false;
    }

    @Override
    public void close(GLStateMgr gl) {
        vertexArrayObject.close(gl);
    }
}
