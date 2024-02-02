/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.render.gl;

import io.github.xenfork.freeworld.client.render.GameRenderer;
import io.github.xenfork.freeworld.core.Identifier;
import io.github.xenfork.freeworld.util.Logging;
import org.slf4j.Logger;
import overrungl.opengl.GL;

/**
 * The OpenGL program.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class GLProgram implements AutoCloseable {
    private static final Logger logger = Logging.caller();
    private final int id;
    private final Identifier identifier;

    private GLProgram(int id, Identifier identifier) {
        this.id = id;
        this.identifier = identifier;
    }

    public static GLProgram load(Identifier identifier) {
        final GL gl = GameRenderer.OpenGL.get();
        final int id = gl.createProgram();
        loadFromJson(id, identifier);

        final GLProgram program = new GLProgram(id, identifier);
        logger.debug("Created {}", program);
        return program;
    }

    private static void loadFromJson(int id, Identifier identifier) {
    }

    @Override
    public void close() {
        final GL gl = GameRenderer.OpenGL.get();
        gl.deleteProgram(id);
    }

    @Override
    public String toString() {
        return STR."GLProgram \{identifier()} (\{id()})";
    }

    public int id() {
        return id;
    }

    public Identifier identifier() {
        return identifier;
    }
}
