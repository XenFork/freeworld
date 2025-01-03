/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2025  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.gl;

import freeworld.client.render.BufferRenderer;
import freeworld.client.render.RenderSystem;
import freeworld.client.render.vertex.BufferBuilder;
import freeworld.client.render.vertex.VertexLayout;
import freeworld.math.Matrix4f;

import java.lang.foreign.MemorySegment;

import static overrungl.opengl.GL10.GL_UNSIGNED_INT;
import static overrungl.opengl.GL15.GL_ARRAY_BUFFER;
import static overrungl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class GLVertexArrayObject implements GLResource {
    private final int usage;
    private final int vao;
    private final int vbo;
    private final int ebo;
    private long vboSize;
    private long eboSize;
    private VertexLayout layout;
    private GLDrawMode drawMode;
    private int indexCount;

    public GLVertexArrayObject(GLStateMgr gl, int usage) {
        this.usage = usage;
        this.vao = gl.GenVertexArrays();
        this.vbo = gl.GenBuffers();
        this.ebo = gl.GenBuffers();
    }

    public void bind(GLStateMgr gl) {
        BufferRenderer.resetVertexArrayObject();
        gl.setVertexArrayBinding(vao);
    }

    public static void unbind(GLStateMgr gl) {
        BufferRenderer.resetVertexArrayObject();
        gl.setVertexArrayBinding(0);
    }

    public void draw(GLStateMgr gl) {
        gl.DrawElements(drawMode.value(), indexCount, GL_UNSIGNED_INT, MemorySegment.NULL);
    }

    public void draw(GLStateMgr gl, Matrix4f projectionViewMatrix, Matrix4f modelMatrix, GLProgram program) {
        if (program.projectionViewMatrixUniform != null) {
            program.projectionViewMatrixUniform.set(projectionViewMatrix);
        }
        if (program.modelMatrixUniform != null) {
            program.modelMatrixUniform.set(modelMatrix);
        }
        if (program.colorModulatorUniform != null) {
            program.colorModulatorUniform.set(RenderSystem.colorModulator().toVector4f());
        }
        program.bind(gl);
        draw(gl);
        program.unbind(gl);
    }

    public void specify(GLStateMgr gl, BufferBuilder.BufferData bufferData) {
        BufferBuilder.DrawParameter drawParameter = bufferData.drawParameter();
        gl.setVertexArrayBinding(vao);
        specifyVertexData(gl, bufferData.vertexData(), drawParameter.vertexLayout());
        specifyIndexData(gl, bufferData.indexData());
        this.drawMode = drawParameter.drawMode();
        this.indexCount = drawParameter.indexCount();
    }

    private void specifyVertexData(GLStateMgr gl, MemorySegment vertexData, VertexLayout layout) {
        gl.setArrayBufferBinding(vbo);
        if (vertexData.byteSize() > vboSize) {
            vboSize = vertexData.byteSize();
            gl.BufferData(GL_ARRAY_BUFFER, vertexData, usage);
            if (!layout.equals(this.layout)) {
                if (this.layout != null) {
                    this.layout.disableAttributes(gl);
                }
                layout.specifyAttributes(gl);
                this.layout = layout;
            }
        } else {
            gl.BufferSubData(GL_ARRAY_BUFFER, 0L, vertexData.byteSize(), vertexData);
        }
    }

    private void specifyIndexData(GLStateMgr gl, MemorySegment indexData) {
        gl.BindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        if (indexData.byteSize() > eboSize) {
            eboSize = indexData.byteSize();
            gl.BufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, usage);
        } else {
            gl.BufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0L, indexData.byteSize(), indexData);
        }
    }

    public int vao() {
        return vao;
    }

    public int vbo() {
        return vbo;
    }

    public int ebo() {
        return ebo;
    }

    @Override
    public void close(GLStateMgr gl) {
        gl.DeleteVertexArrays(vao);
        gl.DeleteBuffers(vbo);
        gl.DeleteBuffers(ebo);
    }
}
