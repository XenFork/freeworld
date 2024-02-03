/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.render;

import io.github.xenfork.freeworld.client.Freeworld;
import io.github.xenfork.freeworld.client.render.gl.GLDrawMode;
import io.github.xenfork.freeworld.client.render.gl.GLProgram;
import io.github.xenfork.freeworld.core.Identifier;
import io.github.xenfork.freeworld.util.Logging;
import org.slf4j.Logger;
import overrungl.opengl.GL;
import overrungl.opengl.GL10C;
import overrungl.opengl.GLFlags;
import overrungl.opengl.ext.GLExtension;

/**
 * The game renderer.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class GameRenderer implements AutoCloseable {
    private static final Logger logger = Logging.caller();
    /**
     * The OpenGL context, which is only available in render thread.
     */
    public static final ScopedValue<GL> OpenGL = ScopedValue.newInstance();
    /**
     * The OpenGL extension context, which is only available in render thread.
     */
    public static final ScopedValue<GLExtension> OpenGLExt = ScopedValue.newInstance();
    private final Freeworld client;
    private final GLFlags glFlags;
    private GLProgram positionColorProgram;
    private int framebufferWidth;
    private int framebufferHeight;

    public GameRenderer(Freeworld client, GLFlags glFlags) {
        this.client = client;
        this.glFlags = glFlags;
    }

    public void init() {
        logger.info("Initializing game renderer");

        final GL gl = OpenGL.get();
        gl.clearColor(0.4f, 0.6f, 0.9f, 1.0f);

        initGLPrograms();
    }

    private void initGLPrograms() {
        positionColorProgram = initBootstrapProgram("init/position_color");
    }

    private GLProgram initBootstrapProgram(String path) {
        final Identifier identifier = Identifier.ofBuiltin(path);
        final GLProgram program = GLProgram.load(identifier);
        if (program == null) {
            throw new IllegalStateException(STR."Failed to initialize bootstrap GLProgram \{identifier}");
        }
        return program;
    }

    public void render() {
        final GL gl = OpenGL.get();
        if (client.framebufferResized().getAcquire()) {
            framebufferWidth = client.framebufferWidth();
            framebufferHeight = client.framebufferHeight();
            gl.viewport(0, 0, framebufferWidth, framebufferHeight);
            client.framebufferResized().setOpaque(false);
        }

        gl.clear(GL10C.COLOR_BUFFER_BIT | GL10C.DEPTH_BUFFER_BIT);

        positionColorProgram.use();
        final Tessellator t = Tessellator.getInstance();
        t.begin(GLDrawMode.TRIANGLES);
        t.index(0, 1, 2, 2, 3, 0);
        t.position(-0.5f, 0.5f, 0f).color(1f, 0f, 0f).emit();
        t.position(-0.5f, -0.5f, 0f).color(0f, 1f, 0f).emit();
        t.position(0.5f, -0.5f, 0f).color(0f, 0f, 1f).emit();
        t.position(0.5f, 0.5f, 0f).color(1f, 1f, 1f).emit();
        t.end();
        gl.useProgram(0);

        client.glfw().swapBuffers(client.window());
    }

    @Override
    public void close() {
        logger.info("Closing game renderer");

        if (positionColorProgram != null) positionColorProgram.close();

        Tessellator.free();
    }

    public GLFlags glFlags() {
        return glFlags;
    }

    public GLProgram positionColorProgram() {
        return positionColorProgram;
    }

    public int framebufferWidth() {
        return framebufferWidth;
    }

    public int framebufferHeight() {
        return framebufferHeight;
    }
}
