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

import freeworld.client.FreeworldClient;
import freeworld.client.render.GameRenderer;
import freeworld.client.render.RenderSystem;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.vertex.BufferBuilder;
import freeworld.client.render.vertex.VertexLayout;
import freeworld.client.render.vertex.VertexLayouts;
import freeworld.client.render.world.chunk.ChunkCompiler;
import freeworld.client.render.world.WorldRenderer;
import freeworld.world.World;
import freeworld.world.chunk.Chunk;
import overrungl.opengl.GL15C;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.foreign.MemorySegment;
import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class ClientChunk extends Chunk implements AutoCloseable {
    private static final Cleaner CLEANER = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    private final State state;
    private final Flux<BufferBuilder.BufferData> dataFlux;
    private Disposable subscribed;
    /**
     * Is this chunk changed?
     */
    private boolean dirty = true;
    private int indexCount = 0;
    private long vertexDataSize = 0L;
    private long indexDataSize = 0L;

    public ClientChunk(World world, WorldRenderer worldRenderer, int x, int y, int z) {
        super(world, x, y, z);
        final GameRenderer gameRenderer = worldRenderer.gameRenderer();
        // Get OpenGL context directly
        this.state = new State(RenderSystem.stateManager());
        this.cleanable = CLEANER.register(this, state);
        this.dataFlux = worldRenderer.vertexBuilderPool()
            .withPoolable(vertexBuilder -> Mono.fromSupplier(() -> {
                final Chunk chunk = world().getOrCreateChunk(x(), y(), z());
                if (chunk != null) {
                    copyFrom(chunk);
                }
                return ChunkCompiler.compile(
                    vertexBuilder,
                    gameRenderer.blockRenderer(),
                    gameRenderer.blockModelManager(),
                    this
                );
            }))
            .onBackpressureBuffer()
            .subscribeOn(worldRenderer.scheduler());
    }

    private static final class State implements Runnable {
        private final GLStateMgr gl;
        private int vao = 0;
        private int vbo = 0;
        private int ebo = 0;
        private final AtomicReference<BufferBuilder.BufferData> dataRef = new AtomicReference<>();

        private State(GLStateMgr gl) {
            this.gl = gl;
        }

        @Override
        public void run() {
            gl.deleteVertexArrays(vao);
            gl.deleteBuffers(vbo, ebo);
            dataRef.set(null);
        }
    }

    public void compile() {
        if (!dirty) {
            return;
        }
        if (subscribed != null) {
            subscribed.dispose();
        }
        subscribed = dataFlux.subscribe(state.dataRef::set, throwable ->
            FreeworldClient.getInstance().execute(() -> {
                throw new RuntimeException(throwable);
            })
        );
        dirty = false;
    }

    public boolean shouldBuildBuffer() {
        return state.dataRef.get() != null;
    }

    public void buildBuffer(GLStateMgr gl) {
        final BufferBuilder.BufferData data = state.dataRef.get();
        if (data != null) {
            buildBuffer(gl, data);
            state.dataRef.set(null);
        }
    }

    public void render(GLStateMgr gl) {
        if (state.vao != 0) {
            gl.setVertexArrayBinding(state.vao);
            gl.drawElements(GLStateMgr.TRIANGLES, indexCount, GLStateMgr.UNSIGNED_INT, MemorySegment.NULL);
        }
    }

    private void buildBuffer(GLStateMgr gl, BufferBuilder.BufferData data) {
        indexCount = data.drawParameter().indexCount();

        final MemorySegment vertexData = data.vertexData();
        final MemorySegment indexData = data.indexData();

        if (state.vao == 0) state.vao = gl.genVertexArrays();
        if (state.vbo == 0) state.vbo = gl.genBuffers();
        if (state.ebo == 0) state.ebo = gl.genBuffers();
        gl.setVertexArrayBinding(state.vao);
        gl.setArrayBufferBinding(state.vbo);
        if (vertexData.byteSize() > vertexDataSize) {
            vertexDataSize = vertexData.byteSize();
            gl.bufferData(GL15C.ARRAY_BUFFER, vertexData, GL15C.DYNAMIC_DRAW);
            final VertexLayout layout = VertexLayouts.POSITION_COLOR_TEXTURE; // TODO: 2024/8/3 squid233: RenderLayer
            layout.specifyAttribPointers(gl);
        } else {
            gl.bufferSubData(GL15C.ARRAY_BUFFER, 0L, vertexData);
        }
        gl.bindBuffer(GL15C.ELEMENT_ARRAY_BUFFER, state.ebo);
        if (indexData.byteSize() > indexDataSize) {
            indexDataSize = indexData.byteSize();
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
    public void close() {
        subscribed.dispose();
        cleanable.clean();
    }
}
