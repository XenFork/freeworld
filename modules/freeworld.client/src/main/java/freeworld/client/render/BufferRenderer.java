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

import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.gl.GLVertexArrayObject;
import freeworld.client.render.vertex.BufferBuilder;
import freeworld.client.render.vertex.VertexLayout;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BufferRenderer {
    private static GLVertexArrayObject vertexArrayObject;

    private static void bind(GLStateMgr gl, GLVertexArrayObject vao) {
        if (vertexArrayObject != vao) {
            vertexArrayObject = vao;
            vao.bind(gl);
        }
    }

    public static void reset(GLStateMgr gl) {
        if (vertexArrayObject != null) {
            resetVertexArrayObject();
            GLVertexArrayObject.unbind(gl);
        }
    }

    public static void resetVertexArrayObject() {
        vertexArrayObject = null;
    }

    public static void draw(GLStateMgr gl, BufferBuilder.BufferData bufferData) {
        GLVertexArrayObject vao = specify(gl, bufferData);
        if (vao != null) {
            vao.draw(gl);
        }
    }

    public static void drawWithCurrentProgram(GLStateMgr gl, BufferBuilder.BufferData bufferData) {
        GLVertexArrayObject vao = specify(gl, bufferData);
        if (vao != null) {
            vao.draw(gl, RenderSystem.projectionViewMatrix(), RenderSystem.modelMatrix(), RenderSystem.currentProgram());
        }
    }

    private static GLVertexArrayObject specify(GLStateMgr gl, BufferBuilder.BufferData bufferData) {
        if (bufferData.isEmpty()) {
            return null;
        }
        GLVertexArrayObject vao = bind(gl, bufferData.drawParameter().vertexLayout());
        vao.specify(gl, bufferData);
        return vao;
    }

    private static GLVertexArrayObject bind(GLStateMgr gl, VertexLayout layout) {
        GLVertexArrayObject vao = layout.vertexArrayObject(gl);
        bind(gl, vao);
        return vao;
    }
}
