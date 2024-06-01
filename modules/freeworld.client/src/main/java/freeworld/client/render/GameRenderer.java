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

import freeworld.client.render.gl.GLDrawMode;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.texture.TextureRegion;
import freeworld.client.render.world.HitResult;
import freeworld.client.render.world.WorldRenderer;
import freeworld.client.Freeworld;
import freeworld.client.render.gl.GLProgram;
import freeworld.client.render.gl.GLResource;
import freeworld.client.render.model.VertexLayout;
import freeworld.client.render.model.VertexLayouts;
import freeworld.client.render.texture.TextureAtlas;
import freeworld.client.render.texture.TextureManager;
import freeworld.client.render.world.BlockRenderer;
import freeworld.client.world.chunk.ClientChunk;
import freeworld.core.Identifier;
import freeworld.core.math.AABBox;
import freeworld.util.Direction;
import freeworld.util.Logging;
import freeworld.world.entity.Entity;
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
    private static final Identifier TEX_CROSSING = Identifier.ofBuiltin("texture/gui/crossing.png");
    private static final Identifier TEX_HOT_BAR = Identifier.ofBuiltin("texture/gui/hotbar.png");
    private static final Identifier TEX_HOT_BAR_SELECTED = Identifier.ofBuiltin("texture/gui/hotbar_selected.png");
    private final float guiScale = 2;
    private TextureManager textureManager;
    private TextureAtlas blockAtlas;
    private TextureAtlas guiAtlas;
    private BlockRenderer blockRenderer;
    private WorldRenderer worldRenderer;
    private Tessellator tessellator;
    private HitResult hitResult = new HitResult(true, null, 0, 0, 0, Direction.SOUTH);

    public GameRenderer(Freeworld client) {
        this.client = client;
    }

    public void init(GLStateMgr gl) {
        logger.info("Initializing game renderer");

        initGLPrograms(gl);

        gl.clearColor(0.4f, 0.6f, 0.9f, 1.0f);

        textureManager = new TextureManager();

        blockAtlas = TextureAtlas.load(gl, List.of(TEX_DIRT, TEX_GRASS_BLOCK, TEX_STONE), 4);
        textureManager.addTexture(TextureManager.BLOCK_ATLAS, blockAtlas);
        logger.info("Created {}x{}x{} {}", blockAtlas.width(), blockAtlas.height(), blockAtlas.mipmapLevel(), TextureManager.BLOCK_ATLAS);

        guiAtlas = TextureAtlas.load(gl, List.of(TEX_CROSSING, TEX_HOT_BAR, TEX_HOT_BAR_SELECTED), 0);
        textureManager.addTexture(TextureManager.GUI_ATLAS, guiAtlas);
        logger.info("Created {}x{}x{} {}", guiAtlas.width(), guiAtlas.height(), guiAtlas.mipmapLevel(), TextureManager.GUI_ATLAS);

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

        gl.disableBlend();
        gl.enableCullFace();
        gl.enableDepthTest();
        gl.setDepthFunc(GL10C.LEQUAL);
        blockAtlas.bind(gl);
        projectionViewMatrix.setPerspective(
            (float) Math.toRadians(70.0),
            (float) client.framebufferWidth() / client.framebufferHeight(),
            0.01f,
            1000.0f
        );
        final Camera camera = client.camera();
        final Entity player = client.player();
        camera.moveToEntity(player);
        camera.updateLerp(partialTick);
        camera.updateViewMatrix();
        projectionViewMatrix.mul(camera.viewMatrix());
        modelMatrix.identity();
        positionColorTexProgram.use(gl);
        positionColorTexProgram.getUniform(GLProgram.UNIFORM_PROJECTION_VIEW_MATRIX).set(projectionViewMatrix);
        positionColorTexProgram.getUniform(GLProgram.UNIFORM_MODEL_MATRIX).set(modelMatrix);
        positionColorTexProgram.uploadUniforms(gl);


        final List<ClientChunk> chunks = worldRenderer.renderingChunks(player);
        worldRenderer.compileChunks(chunks);
        worldRenderer.renderChunks(gl, chunks);

        hitResult = worldRenderer.selectBlock(player);
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
        final float screenWidth = width / guiScale;
        final float screenHeight = height / guiScale;
        projectionViewMatrix.setOrtho(0.0f, screenWidth, 0.0f, screenHeight, -100.0f, 100.0f);
        modelMatrix.translation(screenWidth * 0.5f, screenHeight * 0.5f, 0.0f);

        gl.clear(GL10C.DEPTH_BUFFER_BIT);

        gl.enableBlend();
        gl.setBlendFuncSeparate(GL10C.ONE_MINUS_DST_COLOR, GL10C.ONE_MINUS_SRC_ALPHA, GL10C.ONE, GL10C.ZERO);
        gl.disableCullFace();
        gl.disableDepthTest();
        guiAtlas.bind(gl);
        positionColorTexProgram.use(gl);
        positionColorTexProgram.getUniform(GLProgram.UNIFORM_PROJECTION_VIEW_MATRIX).set(projectionViewMatrix);
        positionColorTexProgram.getUniform(GLProgram.UNIFORM_MODEL_MATRIX).set(modelMatrix);
        positionColorTexProgram.uploadUniforms(gl);
        tessellator.begin(GLDrawMode.TRIANGLES);
        renderCrossing();
        tessellator.end(gl);

        gl.setBlendFunc(GL10C.SRC_ALPHA, GL10C.ONE_MINUS_SRC_ALPHA);
        tessellator.begin(GLDrawMode.TRIANGLES);
        renderHotBar(screenHeight);
        renderHotBarSelected(screenHeight);
        tessellator.end(gl);
    }

    private void renderGuiSprite(Identifier identifier, float x, float y, float anchorX, float anchorY) {
        final TextureRegion region = guiAtlas.getRegion(identifier);
        final int width = guiAtlas.width();
        final int height = guiAtlas.height();
        final float lWidth = region.width() * anchorX;
        final float rWidth = region.width() * (1.0f - anchorX);
        final float bHeight = region.height() * anchorY;
        final float tHeight = region.height() * (1.0f - anchorY);
        final float u0 = region.u0(width);
        final float u1 = region.u1(width);
        final float v0 = region.v0(height);
        final float v1 = region.v1(height);
        tessellator.color(1.0f, 1.0f, 1.0f);
        tessellator.indices(0, 1, 2, 2, 3, 0);
        tessellator.texCoord(u0, v0).position(x - lWidth, y + tHeight, 0).emit();
        tessellator.texCoord(u0, v1).position(x - lWidth, y - bHeight, 0).emit();
        tessellator.texCoord(u1, v1).position(x + rWidth, y - bHeight, 0).emit();
        tessellator.texCoord(u1, v0).position(x + rWidth, y + tHeight, 0).emit();
    }

    private void renderCrossing() {
        renderGuiSprite(TEX_CROSSING, 0.0f, 0.0f, 0.5f, 0.5f);
    }

    private void renderHotBar(float screenHeight) {
        renderGuiSprite(TEX_HOT_BAR, 0.0f, -screenHeight * 0.5f, 0.5f, 0.0f);
    }

    private void renderHotBarSelected(float screenHeight) {
        renderGuiSprite(TEX_HOT_BAR_SELECTED, (client.hotBarSelection() - 5) * 22.0f - 2.0f, -screenHeight * 0.5f, 0.0f, 0.0f);
    }

    @Override
    public void close(GLStateMgr gl) {
        logger.info("Closing game renderer");

        if (worldRenderer != null) worldRenderer.close(gl);
        if (textureManager != null) textureManager.close(gl);

        if (positionColorProgram != null) positionColorProgram.close(gl);
        if (positionColorTexProgram != null) positionColorTexProgram.close(gl);

        if (tessellator != null) tessellator.close(gl);
    }

    public Freeworld client() {
        return client;
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

    public HitResult hitResult() {
        return hitResult;
    }
}
