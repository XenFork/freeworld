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
import freeworld.client.render.gl.GLVertexArrayObject;
import freeworld.client.render.vertex.BufferBuilder;
import freeworld.client.render.world.WorldRenderer;
import freeworld.client.render.world.chunk.ChunkCompiler;
import freeworld.world.World;
import freeworld.world.chunk.Chunk;
import overrungl.opengl.GL15C;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        private GLVertexArrayObject vertexArrayObject;
        private final AtomicReference<BufferBuilder.BufferData> dataRef = new AtomicReference<>();

        private State(GLStateMgr gl) {
            this.gl = gl;
        }

        @Override
        public void run() {
            if (vertexArrayObject != null) {
                vertexArrayObject.close(gl);
            }
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
        if (state.vertexArrayObject != null) {
            state.vertexArrayObject.bind(gl);
            state.vertexArrayObject.draw(gl);
        }
    }

    private void buildBuffer(GLStateMgr gl, BufferBuilder.BufferData data) {
        if (state.vertexArrayObject == null) {
            state.vertexArrayObject = new GLVertexArrayObject(gl, GL15C.DYNAMIC_DRAW);
        }
        state.vertexArrayObject.specify(gl, data);
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
