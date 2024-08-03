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
import freeworld.client.render.vertex.BufferBuilder;

/**
 * A tessellator that allows rendering things dynamically.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class Tessellator {
    private static final int INITIAL_CAPACITY = 1024 * 1024 * 2;
    private static final int MAX_INDEX_COUNT = 90000;
    private final BufferBuilder bufferBuilder = new BufferBuilder(INITIAL_CAPACITY, MAX_INDEX_COUNT);

    public Tessellator() {
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

    public void draw(GLStateMgr gl) {
        BufferRenderer.drawWithCurrentProgram(gl, bufferBuilder.end());
    }
}
