/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.world.chunk;

import io.github.xenfork.freeworld.client.render.gl.GLResource;
import io.github.xenfork.freeworld.client.render.gl.GLStateMgr;
import io.github.xenfork.freeworld.client.render.model.VertexLayout;
import io.github.xenfork.freeworld.client.render.world.ChunkVertexData;
import io.github.xenfork.freeworld.world.World;
import io.github.xenfork.freeworld.world.chunk.Chunk;
import overrungl.opengl.GL15C;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class ClientChunk extends Chunk implements GLResource {
    public final AtomicReference<Future<ChunkVertexData>> future = new AtomicReference<>();
    public final AtomicBoolean submitted = new AtomicBoolean();
    public final AtomicBoolean shouldRecompile = new AtomicBoolean(true);
    private int indexCount = 0;
    private int vao = 0;
    private int vbo = 0;
    private int ebo = 0;

    public ClientChunk(World world, int x, int y, int z) {
        super(world, x, y, z);
    }

    public void render(GLStateMgr gl) {
        if (shouldRecompile.get() && submitted.get()) {
            try {
                final ChunkVertexData data = future.get().get();
                try (Arena _ = data.arena()) {
                    final MemorySegment vertexData = data.vertexData();
                    final MemorySegment indexData = data.indexData();
                    indexCount = data.indexCount();
                    if (vao == 0) vao = gl.genVertexArrays();
                    if (vbo == 0) vbo = gl.genBuffers();
                    if (ebo == 0) ebo = gl.genBuffers();
                    gl.setVertexArrayBinding(vao);
                    gl.setArrayBufferBinding(vbo);
                    if (data.shouldReallocateVertexData()) {
                        gl.bufferData(GL15C.ARRAY_BUFFER, vertexData, GL15C.DYNAMIC_DRAW);
                        final VertexLayout layout = data.vertexLayout();
                        layout.enableAttribs(gl);
                        layout.specifyAttribPointers(gl);
                    } else {
                        gl.bufferSubData(GL15C.ARRAY_BUFFER, 0L, vertexData);
                    }
                    gl.bindBuffer(GL15C.ELEMENT_ARRAY_BUFFER, ebo);
                    if (data.shouldReallocateIndexData()) {
                        gl.bufferData(GL15C.ELEMENT_ARRAY_BUFFER, indexData, GL15C.DYNAMIC_DRAW);
                    } else {
                        gl.bufferSubData(GL15C.ELEMENT_ARRAY_BUFFER, 0L, indexData);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            shouldRecompile.set(false);
            submitted.set(false);
            future.set(null);
        }
        if (vao != 0) {
            gl.setVertexArrayBinding(vao);
            gl.drawElements(GLStateMgr.TRIANGLES, indexCount, GLStateMgr.UNSIGNED_INT, MemorySegment.NULL);
        }
    }

    @Override
    public void close(GLStateMgr gl) {
        gl.deleteVertexArrays(vao);
        gl.deleteBuffers(vbo, ebo);
    }
}
