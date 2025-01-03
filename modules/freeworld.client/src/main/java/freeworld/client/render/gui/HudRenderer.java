/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2025  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.gui;

import freeworld.client.FreeworldClient;
import freeworld.client.render.GameRenderer;
import freeworld.client.render.RenderSystem;
import freeworld.client.render.Tessellator;
import freeworld.client.render.gl.GLDrawMode;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.texture.TextureAtlas;
import freeworld.client.render.texture.TextureManager;
import freeworld.client.render.vertex.BufferBuilder;
import freeworld.client.render.vertex.VertexLayouts;
import freeworld.client.util.Color;
import freeworld.client.util.GameClientVersion;
import freeworld.math.Matrix4f;
import freeworld.math.Vector2d;
import freeworld.math.Vector3d;
import freeworld.math.Vector3i;
import freeworld.registry.Registries;
import freeworld.util.GameCoreVersion;
import freeworld.util.Identifier;
import freeworld.util.math.ChunkPos;
import freeworld.world.block.BlockType;
import freeworld.world.entity.player.PlayerEntity;

import static overrungl.opengl.GL10.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class HudRenderer {
    public static final Identifier CROSSING_TEXTURE = Identifier.ofBuiltin("gui/crossing");
    public static final Identifier HOT_BAR_TEXTURE = Identifier.ofBuiltin("gui/hotbar");
    public static final Identifier HOT_BAR_SELECTED_TEXTURE = Identifier.ofBuiltin("gui/hotbar_selected");
    private final FreeworldClient client;
    private final GameRenderer gameRenderer;
    private int width;
    private int height;

    public HudRenderer(GameRenderer gameRenderer) {
        this.client = gameRenderer.client();
        this.gameRenderer = gameRenderer;
        this.width = client.scaledFramebufferWidth();
        this.height = client.scaledFramebufferHeight();
    }

    public void update(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void render(GuiGraphics graphics, GLStateMgr gl, double partialTick) {
        RenderSystem.setProjectionViewMatrix(Matrix4f.setOrtho(0.0f, width, 0.0f, height, -300.0f, 300.0f),
            Matrix4f.identity());
        RenderSystem.setModelMatrix(Matrix4f.identity());

        RenderSystem.useProgram(GameRenderer.positionColorTexProgram());

        // gui
        renderCrossing(graphics, gl);

        // hot-bar
        renderHotBar(graphics, gl);

        // debug
        if (client.debugHudEnabled()) {
            renderDebugHud(gl);
        }
    }

    private void renderDebugHud(GLStateMgr gl) {
        gl.setBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.bindTexture2D(gameRenderer.unifont().texture());
        RenderSystem.useProgram(GameRenderer.renderTypeTextProgram());
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.buffer();
        buffer.begin(GLDrawMode.TRIANGLES, VertexLayouts.TEXT);

        PlayerEntity player = client.player();
        Vector3d position = player.position();
        Vector2d rotation = player.rotation();
        Vector3i blockPos = player.blockPos();
        Vector3i chunkPos = player.chunkPos();
        Vector3d velocity = player.velocity();
        Vector3d acceleration = player.acceleration();
        gameRenderer.textRenderer().renderText(buffer,
            gameRenderer.unifont(),
            """
                freeworld (core %s, client %s)

                Position: %f %f %f / %f %f
                Block: %d %d %d [%d %d %d in %d %d %d]
                Velocity: %f %f %f
                Acceleration: %f %f %f""".formatted(
                GameCoreVersion.get().version(),
                GameClientVersion.get().version(),
                position.x(),
                position.y(),
                position.z(),
                rotation.y(),
                rotation.x(),
                blockPos.x(),
                blockPos.y(),
                blockPos.z(),
                ChunkPos.toBlockPosInChunk(blockPos.x()),
                ChunkPos.toBlockPosInChunk(blockPos.y()),
                ChunkPos.toBlockPosInChunk(blockPos.z()),
                chunkPos.x(),
                chunkPos.y(),
                chunkPos.z(),
                velocity.x(),
                velocity.y(),
                velocity.z(),
                acceleration.x(),
                acceleration.y(),
                acceleration.z()
            ),
            0,
            height - gameRenderer.unifont().lineHeight(),
            Color.WHITE,
            true);

        t.draw(gl);
    }

    private void renderCrossing(GuiGraphics graphics, GLStateMgr gl) {
        TextureAtlas atlas = gameRenderer.textureManager().getTexture(TextureManager.GUI_ATLAS);
        RenderSystem.bindTexture2D(atlas);
        gl.setBlendFuncSeparate(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.buffer();
        buffer.begin(GLDrawMode.TRIANGLES, VertexLayouts.POSITION_COLOR_TEXTURE);
        graphics.drawSprite(
            buffer,
            atlas.getRegion(CROSSING_TEXTURE),
            width * 0.5f,
            height * 0.5f,
            0.5f,
            0.5f
        );
        t.draw(gl);
    }

    private void renderHotBar(GuiGraphics graphics, GLStateMgr gl) {
        final TextureAtlas atlas = gameRenderer.textureManager().getTexture(TextureManager.GUI_ATLAS);
        RenderSystem.bindTexture2D(atlas);
        gl.setBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.buffer();
        buffer.begin(GLDrawMode.TRIANGLES, VertexLayouts.POSITION_COLOR_TEXTURE);
        graphics.drawSprite(
            buffer,
            atlas.getRegion(HOT_BAR_TEXTURE),
            width * 0.5f,
            1.0f,
            0.5f,
            0.0f
        );
        graphics.drawSprite(
            buffer,
            atlas.getRegion(HOT_BAR_SELECTED_TEXTURE),
            hotBarSelectorX(gameRenderer.client().player().selectedHotBar()),
            0.0f,
            0.0f,
            0.0f
        );
        t.draw(gl);

        gl.setEnableDepthTest();
        renderHotBarItems(gl);
    }

    private void renderHotBarItems(GLStateMgr gl) {
        RenderSystem.bindTexture2D(gameRenderer.textureManager().getTexture(TextureManager.BLOCK_ATLAS));
        final FreeworldClient client = gameRenderer.client();
        final Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.buffer();
        buffer.begin(GLDrawMode.TRIANGLES, VertexLayouts.POSITION_COLOR_TEXTURE);
        for (int i = 0; i < 10; i++) {
            BlockType item = client.player().getHotBarItem(i);
            gameRenderer.blockRenderer().renderBlockModel(buffer,
                gameRenderer.blockModelManager().get(Registries.BLOCK_TYPE.getId(item)),
                Matrix4f.translation(width * 0.5f + (i - 5) * 20 + 3, 8, 10)
                    .rotateX((float) Math.toRadians(30.0))
                    .rotateY((float) Math.toRadians(45.0))
                    .scale(10),
                0,
                0,
                0,
                _ -> false);
        }
        tessellator.draw(gl);
    }

    private float hotBarSelectorX(int selection) {
        return width * 0.5f + (selection - 5) * 20.0f - 2.0f;
    }

    public void tick() {
    }
}
