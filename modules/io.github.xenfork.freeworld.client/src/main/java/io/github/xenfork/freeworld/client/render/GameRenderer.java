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
import io.github.xenfork.freeworld.client.render.model.VertexLayout;
import io.github.xenfork.freeworld.client.render.model.VertexLayouts;
import io.github.xenfork.freeworld.client.render.world.BlockRenderer;
import io.github.xenfork.freeworld.client.texture.TextureAtlas;
import io.github.xenfork.freeworld.client.texture.TextureManager;
import io.github.xenfork.freeworld.core.Identifier;
import io.github.xenfork.freeworld.util.Logging;
import io.github.xenfork.freeworld.world.block.BlockTypes;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import overrungl.opengl.GL;
import overrungl.opengl.GL10C;
import overrungl.opengl.GLFlags;

import java.util.List;

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
    public static final Identifier TEX_DIRT = Identifier.ofBuiltin("texture/block/dirt.png");
    public static final Identifier TEX_GRASS_BLOCK = Identifier.ofBuiltin("texture/block/grass_block.png");
    public static final Identifier TEX_STONE = Identifier.ofBuiltin("texture/block/stone.png");
    private TextureAtlas texture;
    private TextureManager textureManager;
    private BlockRenderer blockRenderer;

    public GameRenderer(Freeworld client) {
        this.client = client;
    }

    public void init() {
        logger.info("Initializing game renderer");

        final GL gl = OpenGL.get();

        initGLPrograms();

        gl.clearColor(0.4f, 0.6f, 0.9f, 1.0f);

        textureManager = new TextureManager();

        texture = TextureAtlas.load(List.of(TEX_DIRT, TEX_GRASS_BLOCK, TEX_STONE));
        textureManager.addTexture(TextureManager.BLOCK_ATLAS, texture);
        logger.info("Created {}x{}x{} {}", texture.width(), texture.height(), texture.mipmapLevel(), TextureManager.BLOCK_ATLAS);

        blockRenderer = new BlockRenderer(this);
    }

    private void initGLPrograms() {
        positionColorProgram = initBootstrapProgram("init/position_color", VertexLayouts.POSITION_COLOR);
        positionColorTexProgram = initBootstrapProgram("init/position_color_tex", VertexLayouts.POSITION_COLOR_TEX);
    }

    private GLProgram initBootstrapProgram(String path, VertexLayout layout) {
        final Identifier identifier = Identifier.ofBuiltin(path);
        final GLProgram program = GLProgram.load(identifier, layout);
        if (program == null) {
            throw new IllegalStateException(STR."Failed to initialize bootstrap GLProgram \{identifier}");
        }
        return program;
    }

    public void render(double partialTick) {
        final GL gl = OpenGL.get();
        if (client.framebufferResized().getAcquire()) {
            framebufferWidth = client.framebufferWidth();
            framebufferHeight = client.framebufferHeight();
            gl.viewport(0, 0, framebufferWidth, framebufferHeight);
            client.framebufferResized().setOpaque(false);
        }

        gl.clear(GL10C.COLOR_BUFFER_BIT | GL10C.DEPTH_BUFFER_BIT);

        gl.enable(GL10C.CULL_FACE);
        gl.enable(GL10C.DEPTH_TEST);
        gl.depthFunc(GL10C.LEQUAL);
        texture.bind();
        positionColorTexProgram.use();
        projectionView.setPerspective(
            ((float) Math.toRadians(90.0)),
            framebufferHeight > 0 ? (float) framebufferWidth / framebufferHeight : 0.00001f,
            0.01f,
            1000.0f
        );
        final Camera camera = client.camera();
        camera.updateLerp(partialTick);
        camera.updateViewMatrix();
        projectionView.mul(camera.viewMatrix());
        positionColorTexProgram.getUniform(GLProgram.UNIFORM_PROJECTION_VIEW_MATRIX).set(projectionView);
        positionColorTexProgram.getUniform(GLProgram.UNIFORM_MODEL_MATRIX).set(matrix);
        positionColorTexProgram.uploadUniforms();
        final Tessellator t = Tessellator.getInstance();
        t.begin(GLDrawMode.TRIANGLES);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                blockRenderer.renderBlock(t, BlockTypes.GRASS_BLOCK.defaultBlockState(), x, 2, z);
                blockRenderer.renderBlock(t, BlockTypes.DIRT.defaultBlockState(), x, 1, z);
                blockRenderer.renderBlock(t, BlockTypes.STONE.defaultBlockState(), x, 0, z);
            }
        }
        t.end();
        gl.useProgram(0);
        gl.bindTexture(GL10C.TEXTURE_2D, 0);
        gl.disable(GL10C.CULL_FACE);
        gl.disable(GL10C.DEPTH_TEST);

        client.glfw().swapBuffers(client.window());
    }

    @Override
    public void close() {
        logger.info("Closing game renderer");

        if (textureManager != null) textureManager.close();

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

    public TextureManager textureManager() {
        return textureManager;
    }
}
