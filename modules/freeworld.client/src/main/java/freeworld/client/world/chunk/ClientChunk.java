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
import freeworld.client.render.world.ChunkCompiler;
import freeworld.client.render.world.ChunkVertexData;
import freeworld.client.render.world.WorldRenderer;
import freeworld.world.World;
import freeworld.world.chunk.Chunk;
import overrungl.opengl.GL15C;
import reactor.core.publisher.Mono;

import java.lang.foreign.MemorySegment;
import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class ClientChunk extends Chunk implements GLResource {
    private static final Cleaner CLEANER = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    private final State state;
    private final WorldRenderer worldRenderer;
    private final AtomicReference<ChunkVertexData> dataRef = new AtomicReference<>();
    /**
     * Is this chunk changed?
     */
    private boolean dirty = true;
    private int indexCount = 0;

    public ClientChunk(World world, WorldRenderer worldRenderer, int x, int y, int z) {
        super(world, x, y, z);
        // Get OpenGL context directly
        this.state = new State(worldRenderer.gameRenderer().client().gl());
        this.cleanable = CLEANER.register(this, state);
        this.worldRenderer = worldRenderer;
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

    public void compile() {
        if (!dirty) {
            return;
        }
        final Chunk chunk = world().getOrCreateChunk(x(), y(), z());
        if (chunk != null) {
            copyFrom(chunk);
        }
        Mono.fromCallable(() -> ChunkCompiler.compile(worldRenderer, this))
            .subscribeOn(worldRenderer.scheduler())
            .subscribe(dataRef::set);
        dirty = false;
    }

    public void render(GLStateMgr gl) {
        final ChunkVertexData data = dataRef.get();
        if (data != null) {
            buildBuffer(gl, data);
            dataRef.set(null);
        }
        if (state.vao != 0) {
            gl.setVertexArrayBinding(state.vao);
            gl.drawElements(GLStateMgr.TRIANGLES, indexCount, GLStateMgr.UNSIGNED_INT, MemorySegment.NULL);
        }
    }

    private void buildBuffer(GLStateMgr gl, ChunkVertexData data) {
        indexCount = data.indexCount();

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
