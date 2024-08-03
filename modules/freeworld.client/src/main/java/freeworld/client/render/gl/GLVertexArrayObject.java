/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
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
import overrungl.opengl.GL;
import overrungl.opengl.GL15C;

import java.lang.foreign.MemorySegment;

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
        this.vao = gl.genVertexArrays();
        this.vbo = gl.genBuffers();
        this.ebo = gl.genBuffers();
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
        gl.drawElements(drawMode.value(), indexCount, GL.UNSIGNED_INT, MemorySegment.NULL);
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
            gl.bufferData(GL15C.ARRAY_BUFFER, vertexData, usage);
            if (!layout.equals(this.layout)) {
                if (this.layout != null) {
                    this.layout.disableAttributes(gl);
                }
                layout.specifyAttributes(gl);
                this.layout = layout;
            }
        } else {
            gl.bufferSubData(GL15C.ARRAY_BUFFER, 0L, vertexData);
        }
    }

    private void specifyIndexData(GLStateMgr gl, MemorySegment indexData) {
        gl.bindBuffer(GL15C.ELEMENT_ARRAY_BUFFER, ebo);
        if (indexData.byteSize() > eboSize) {
            eboSize = indexData.byteSize();
            gl.bufferData(GL15C.ELEMENT_ARRAY_BUFFER, indexData, usage);
        } else {
            gl.bufferSubData(GL15C.ELEMENT_ARRAY_BUFFER, 0L, indexData);
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
        gl.deleteVertexArrays(vao);
        gl.deleteBuffers(vbo, ebo);
    }
}
