/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.world.chunk;

import freeworld.client.render.gl.GLResource;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.model.VertexLayout;
import freeworld.client.render.world.ChunkVertexData;
import freeworld.client.render.world.WorldRenderer;
import freeworld.world.World;
import freeworld.world.chunk.Chunk;
import overrungl.opengl.GL15C;

import java.lang.foreign.MemorySegment;
import java.lang.ref.Cleaner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class ClientChunk extends Chunk implements GLResource {
    private static final Cleaner CLEANER = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    private final State state;
    public Future<ChunkVertexData> future = null;
    /**
     * Is this chunk changed?
     */
    public boolean dirty = true;
    private int indexCount = 0;
    private boolean allAir = false;

    public ClientChunk(World world, WorldRenderer worldRenderer, int x, int y, int z) {
        super(world, x, y, z);
        // Get OpenGL context directly
        this.state = new State(worldRenderer.gameRenderer().client().gl());
        this.cleanable = CLEANER.register(this, state);
    }

    private static final class State implements Runnable {
        private final GLStateMgr gl;
        private int vao = 0;
        private int vbo = 0;
        private int ebo = 0;

        private State(GLStateMgr gl) {
            this.gl = gl;
        }

        @Override
        public void run() {
            gl.deleteVertexArrays(vao);
            gl.deleteBuffers(vbo, ebo);
        }
    }

    public void render(GLStateMgr gl) {
        try {
            if (future != null && future.state() == Future.State.SUCCESS) {
                final ChunkVertexData data = future.get();

                indexCount = data.indexCount();
                if (indexCount == 0) {
                    future = null;
                    allAir = true;
                    return;
                } else {
                    allAir = false;
                }

                final MemorySegment vertexData = data.vertexData();
                final MemorySegment indexData = data.indexData();

                if (state.vao == 0) state.vao = gl.genVertexArrays();
                if (state.vbo == 0) state.vbo = gl.genBuffers();
                if (state.ebo == 0) state.ebo = gl.genBuffers();
                gl.setVertexArrayBinding(state.vao);
                gl.setArrayBufferBinding(state.vbo);
                if (data.shouldReallocateVertexData()) {
                    gl.bufferData(GL15C.ARRAY_BUFFER, vertexData, GL15C.DYNAMIC_DRAW);
                    final VertexLayout layout = data.vertexLayout();
                    layout.enableAttribs(gl);
                    layout.specifyAttribPointers(gl);
                } else {
                    gl.bufferSubData(GL15C.ARRAY_BUFFER, 0L, vertexData);
                }
                gl.bindBuffer(GL15C.ELEMENT_ARRAY_BUFFER, state.ebo);
                if (data.shouldReallocateIndexData()) {
                    gl.bufferData(GL15C.ELEMENT_ARRAY_BUFFER, indexData, GL15C.DYNAMIC_DRAW);
                } else {
                    gl.bufferSubData(GL15C.ELEMENT_ARRAY_BUFFER, 0L, indexData);
                }
                future = null;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        if (state.vao != 0 && !allAir) {
            gl.setVertexArrayBinding(state.vao);
            gl.drawElements(GLStateMgr.TRIANGLES, indexCount, GLStateMgr.UNSIGNED_INT, MemorySegment.NULL);
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        dirty = true;
    }

    @Override
    public void close(GLStateMgr gl) {
        cleanable.clean();
    }
}
