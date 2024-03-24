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
import io.github.xenfork.freeworld.client.render.gl.GLResource;
import io.github.xenfork.freeworld.client.render.gl.GLStateMgr;
import io.github.xenfork.freeworld.client.render.model.VertexLayout;
import io.github.xenfork.freeworld.client.render.model.VertexLayouts;
import io.github.xenfork.freeworld.client.render.texture.TextureAtlas;
import io.github.xenfork.freeworld.client.render.texture.TextureManager;
import io.github.xenfork.freeworld.client.render.world.BlockRenderer;
import io.github.xenfork.freeworld.client.render.world.HitResult;
import io.github.xenfork.freeworld.client.render.world.WorldRenderer;
import io.github.xenfork.freeworld.core.Identifier;
import io.github.xenfork.freeworld.core.math.AABBox;
import io.github.xenfork.freeworld.util.Logging;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import overrungl.opengl.GL10C;

import java.util.List;

/**
 * The game renderer.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class GameRenderer implements GLResource {
    private static final Logger logger = Logging.caller();
    private final Freeworld client;
    private GLProgram positionColorProgram;
    private GLProgram positionColorTexProgram;
    private final Matrix4f projectionViewMatrix = new Matrix4f();
    private final Matrix4f modelMatrix = new Matrix4f();
    public static final Identifier TEX_DIRT = Identifier.ofBuiltin("texture/block/dirt.png");
    public static final Identifier TEX_GRASS_BLOCK = Identifier.ofBuiltin("texture/block/grass_block.png");
    public static final Identifier TEX_STONE = Identifier.ofBuiltin("texture/block/stone.png");
    private TextureAtlas texture;
    private TextureManager textureManager;
    private BlockRenderer blockRenderer;
    private WorldRenderer worldRenderer;
    private Tessellator tessellator;

    public GameRenderer(Freeworld client) {
        this.client = client;
    }

    public void init(GLStateMgr gl) {
        logger.info("Initializing game renderer");

        initGLPrograms(gl);

        gl.clearColor(0.4f, 0.6f, 0.9f, 1.0f);

        textureManager = new TextureManager();

        texture = TextureAtlas.load(gl, List.of(TEX_DIRT, TEX_GRASS_BLOCK, TEX_STONE));
        textureManager.addTexture(TextureManager.BLOCK_ATLAS, texture);
        logger.info("Created {}x{}x{} {}", texture.width(), texture.height(), texture.mipmapLevel(), TextureManager.BLOCK_ATLAS);

        blockRenderer = new BlockRenderer(this);
        worldRenderer = new WorldRenderer(this, client.world());

        tessellator = new Tessellator();
    }

    private void initGLPrograms(GLStateMgr gl) {
        positionColorProgram = initBootstrapProgram(gl, "init/position_color", VertexLayouts.POSITION_COLOR);
        positionColorTexProgram = initBootstrapProgram(gl, "init/position_color_tex", VertexLayouts.POSITION_COLOR_TEX);
    }

    private GLProgram initBootstrapProgram(GLStateMgr gl, String path, VertexLayout layout) {
        final Identifier identifier = Identifier.ofBuiltin(path);
        final GLProgram program = GLProgram.load(gl, identifier, layout);
        if (program == null) {
            throw new IllegalStateException(STR."Failed to initialize bootstrap GLProgram \{identifier}");
        }
        return program;
    }

    public void render(GLStateMgr gl, double partialTick) {
        gl.clear(GL10C.COLOR_BUFFER_BIT | GL10C.DEPTH_BUFFER_BIT);

        gl.enableCullFace();
        gl.enableDepthTest();
        gl.setDepthFunc(GL10C.LEQUAL);
        texture.bind(gl);
        projectionViewMatrix.setPerspective(
            (float) Math.toRadians(70.0),
            (float) client.framebufferWidth() / client.framebufferHeight(),
            0.01f,
            1000.0f
        );
        final Camera camera = client.camera();
        camera.moveToEntity(client.player());
        camera.updateLerp(partialTick);
        camera.updateViewMatrix();
        projectionViewMatrix.mul(camera.viewMatrix());
        modelMatrix.identity();
        positionColorTexProgram.use(gl);
        positionColorTexProgram.getUniform(GLProgram.UNIFORM_PROJECTION_VIEW_MATRIX).set(projectionViewMatrix);
        positionColorTexProgram.getUniform(GLProgram.UNIFORM_MODEL_MATRIX).set(modelMatrix);
        positionColorTexProgram.uploadUniforms(gl);
        worldRenderer.compileChunks();
        worldRenderer.renderChunks(gl);

        final HitResult hitResult = worldRenderer.selectBlock();
        if (!hitResult.missed()) {
            final AABBox box = hitResult.blockType().outlineShape().move(hitResult.x(), hitResult.y(), hitResult.z());
            final float minX = (float) box.minX();
            final float minY = (float) box.minY();
            final float minZ = (float) box.minZ();
            final float maxX = (float) box.maxX();
            final float maxY = (float) box.maxY();
            final float maxZ = (float) box.maxZ();
            final float offset = 0.005f;
            gl.setTextureBinding2D(0);
            positionColorProgram.use(gl);
            positionColorProgram.getUniform(GLProgram.UNIFORM_PROJECTION_VIEW_MATRIX).set(projectionViewMatrix);
            positionColorProgram.getUniform(GLProgram.UNIFORM_MODEL_MATRIX).set(modelMatrix);
            positionColorProgram.uploadUniforms(gl);
            tessellator.begin(GLDrawMode.LINES);
            tessellator.color(0, 0, 0);
            // -x
            tessellator.indices(0, 1, 0, 2, 1, 3, 2, 3);
            // +x
            tessellator.indices(4, 5, 4, 6, 5, 7, 6, 7);
            // -z
            tessellator.indices(0, 4, 2, 6);
            // +z
            tessellator.indices(1, 5, 3, 7);
            tessellator.position(minX - offset, minY - offset, minZ - offset).emit();
            tessellator.position(minX - offset, minY - offset, maxZ + offset).emit();
            tessellator.position(minX - offset, maxY + offset, minZ - offset).emit();
            tessellator.position(minX - offset, maxY + offset, maxZ + offset).emit();
            tessellator.position(maxX + offset, minY - offset, minZ - offset).emit();
            tessellator.position(maxX + offset, minY - offset, maxZ + offset).emit();
            tessellator.position(maxX + offset, maxY + offset, minZ - offset).emit();
            tessellator.position(maxX + offset, maxY + offset, maxZ + offset).emit();
            tessellator.end(gl);
        }

        renderGui(gl, partialTick);
    }

    private void renderGui(GLStateMgr gl, double partialTick) {
        final int width = client.framebufferWidth();
        final int height = client.framebufferHeight();

        gl.clear(GL10C.DEPTH_BUFFER_BIT);

        gl.disableCullFace();
        gl.disableDepthTest();
        gl.setTextureBinding2D(0);
        positionColorProgram.use(gl);
        projectionViewMatrix.setOrtho(0.0f, width, 0.0f, height, -100.0f, 100.0f);
        modelMatrix.translation(width * 0.5f, height * 0.5f, 0.0f);
        positionColorProgram.getUniform(GLProgram.UNIFORM_PROJECTION_VIEW_MATRIX).set(projectionViewMatrix);
        positionColorProgram.getUniform(GLProgram.UNIFORM_MODEL_MATRIX).set(modelMatrix);
        positionColorProgram.uploadUniforms(gl);
        tessellator.begin(GLDrawMode.TRIANGLES);
        tessellator.color(1.0f, 1.0f, 1.0f);
        tessellator.indices(0, 1, 2, 2, 3, 0);
        tessellator.position(-8, 1, 0).emit();
        tessellator.position(-8, -1, 0).emit();
        tessellator.position(8, -1, 0).emit();
        tessellator.position(8, 1, 0).emit();
        tessellator.indices(0, 1, 2, 2, 3, 0);
        tessellator.position(1, 8, 0).emit();
        tessellator.position(1, -8, 0).emit();
        tessellator.position(-1, -8, 0).emit();
        tessellator.position(-1, 8, 0).emit();
        tessellator.end(gl);
    }

    @Override
    public void close(GLStateMgr gl) {
        logger.info("Closing game renderer");

        if (textureManager != null) textureManager.close(gl);
        if (worldRenderer != null) worldRenderer.close(gl);

        if (positionColorProgram != null) positionColorProgram.close(gl);
        if (positionColorTexProgram != null) positionColorTexProgram.close(gl);

        if (tessellator != null) tessellator.close(gl);
    }

    public GLProgram positionColorProgram() {
        return positionColorProgram;
    }

    public GLProgram positionColorTexProgram() {
        return positionColorTexProgram;
    }

    public TextureManager textureManager() {
        return textureManager;
    }

    public BlockRenderer blockRenderer() {
        return blockRenderer;
    }

    public Matrix4f projectionViewMatrix() {
        return projectionViewMatrix;
    }

    public Matrix4f modelMatrix() {
        return modelMatrix;
    }
}
