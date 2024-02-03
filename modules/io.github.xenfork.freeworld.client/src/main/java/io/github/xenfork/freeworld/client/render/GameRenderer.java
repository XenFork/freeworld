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
import io.github.xenfork.freeworld.client.texture.Texture;
import io.github.xenfork.freeworld.core.Identifier;
import io.github.xenfork.freeworld.util.Logging;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import overrungl.opengl.GL;
import overrungl.opengl.GL10C;
import overrungl.opengl.GLFlags;

/**
 * The game renderer.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class GameRenderer implements AutoCloseable {
    private static final Logger logger = Logging.caller();
    /**
     * The OpenGL flags, which is only available in render thread.
     */
    public static final ScopedValue<GLFlags> OpenGLFlags = ScopedValue.newInstance();
    /**
     * The OpenGL context, which is only available in render thread.
     */
    public static final ScopedValue<GL> OpenGL = ScopedValue.newInstance();
    private final Freeworld client;
    private GLProgram positionColorProgram;
    private GLProgram positionColorTexProgram;
    private int framebufferWidth;
    private int framebufferHeight;
    private final Matrix4f projectionView = new Matrix4f();
    private final Matrix4f matrix = new Matrix4f();
    private Texture texture;

    public GameRenderer(Freeworld client) {
        this.client = client;
    }

    public void init() {
        logger.info("Initializing game renderer");

        final GL gl = OpenGL.get();

        initGLPrograms();

        gl.clearColor(0.4f, 0.6f, 0.9f, 1.0f);

        texture = Texture.load(Identifier.ofBuiltin("block/grass_block.png"));
    }

    private void initGLPrograms() {
        positionColorProgram = initBootstrapProgram("init/position_color");
        positionColorTexProgram = initBootstrapProgram("init/position_color_tex");
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

        texture.bind();
        positionColorTexProgram.use();
        projectionView.setPerspective(
            ((float) Math.toRadians(90.0)),
            framebufferHeight > 0 ? (float) framebufferWidth / framebufferHeight : 0.00001f,
            0.01f,
            1000.0f
        ).translate(0f, 0f, -2f);
        final double v = System.nanoTime() * 1.0e-9d;
        matrix.rotationZ((float) Math.toRadians(Math.sin(v) * 90f))
            .rotateY((float) Math.toRadians(Math.cos(v) * 90f));
        positionColorTexProgram.getUniform(GLProgram.UNIFORM_PROJECTION_VIEW_MATRIX).set(projectionView);
        positionColorTexProgram.getUniform(GLProgram.UNIFORM_MODEL_MATRIX).set(matrix);
        positionColorTexProgram.uploadUniforms();
        final Tessellator t = Tessellator.getInstance();
        t.begin(GLDrawMode.TRIANGLES);
        // +x
        t.index(0, 1, 2, 2, 3, 0);
        t.position(0.5f, 0.5f, 0.5f).color(1f, 1f, 1f).texCoord(0f, 0f).emit();
        t.position(0.5f, -0.5f, 0.5f).color(1f, 1f, 1f).texCoord(0f, 1f).emit();
        t.position(0.5f, -0.5f, -0.5f).color(1f, 1f, 1f).texCoord(1f, 1f).emit();
        t.position(0.5f, 0.5f, -0.5f).color(1f, 1f, 1f).texCoord(1f, 0f).emit();
        // +z
        t.index(0, 1, 2, 2, 3, 0);
        t.position(-0.5f, 0.5f, 0.5f).color(1f, 1f, 1f).texCoord(0f, 0f).emit();
        t.position(-0.5f, -0.5f, 0.5f).color(1f, 1f, 1f).texCoord(0f, 1f).emit();
        t.position(0.5f, -0.5f, 0.5f).color(1f, 1f, 1f).texCoord(1f, 1f).emit();
        t.position(0.5f, 0.5f, 0.5f).color(1f, 1f, 1f).texCoord(1f, 0f).emit();
        t.end();
        gl.useProgram(0);
        gl.bindTexture(GL10C.TEXTURE_2D, 0);

        client.glfw().swapBuffers(client.window());
    }

    @Override
    public void close() {
        logger.info("Closing game renderer");

        if (texture != null) texture.close();

        if (positionColorProgram != null) positionColorProgram.close();
        if (positionColorTexProgram != null) positionColorTexProgram.close();

        Tessellator.free();
    }

    public GLProgram positionColorProgram() {
        return positionColorProgram;
    }

    public GLProgram positionColorTexProgram() {
        return positionColorTexProgram;
    }

    public int framebufferWidth() {
        return framebufferWidth;
    }

    public int framebufferHeight() {
        return framebufferHeight;
    }
}
