/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.gui;

import freeworld.client.Freeworld;
import freeworld.client.render.GameRenderer;
import freeworld.client.render.RenderSystem;
import freeworld.client.render.Tessellator;
import freeworld.client.render.gl.GLDrawMode;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.texture.TextureAtlas;
import freeworld.client.render.texture.TextureManager;
import freeworld.util.Identifier;
import freeworld.registry.Registries;
import freeworld.math.Matrix4f;
import freeworld.world.block.BlockType;
import overrungl.opengl.GL10C;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class HudRenderer {
    public static final Identifier CROSSING_TEXTURE = Identifier.ofBuiltin("gui/crossing");
    public static final Identifier HOT_BAR_TEXTURE = Identifier.ofBuiltin("gui/hotbar");
    public static final Identifier HOT_BAR_SELECTED_TEXTURE = Identifier.ofBuiltin("gui/hotbar_selected");
    private final GameRenderer gameRenderer;
    private int width = 0;
    private int height = 0;

    public HudRenderer(GameRenderer gameRenderer) {
        this.gameRenderer = gameRenderer;
        Freeworld client = gameRenderer.client();
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
        RenderSystem.setModelMatrix(Matrix4f.translation(width * 0.5f, height * 0.5f, 0.0f));

        RenderSystem.useProgram(gameRenderer.positionColorTexProgram());
        RenderSystem.updateMatrices();

        // gui
        renderCrossing(graphics, gl);

        // hot-bar
        renderHotBar(graphics, gl, partialTick);
    }

    private void renderCrossing(GuiGraphics graphics, GLStateMgr gl) {
        gl.setBlendFuncSeparate(GL10C.ONE_MINUS_DST_COLOR, GL10C.ONE_MINUS_SRC_ALPHA, GL10C.ONE, GL10C.ZERO);
        graphics.beginDraw();
        graphics.drawSprite(
            gameRenderer.textureManager()
                .<TextureAtlas>getTexture(TextureManager.GUI_ATLAS)
                .getRegion(CROSSING_TEXTURE),
            0.0f,
            0.0f,
            0.5f,
            0.5f
        );
        graphics.endDraw();
    }

    private void renderHotBar(GuiGraphics graphics, GLStateMgr gl, double partialTick) {
        final TextureAtlas atlas = gameRenderer.textureManager().getTexture(TextureManager.GUI_ATLAS);
        graphics.beginDraw();
        gl.setBlendFunc(GL10C.SRC_ALPHA, GL10C.ONE_MINUS_SRC_ALPHA);
        graphics.drawSprite(
            atlas.getRegion(HOT_BAR_TEXTURE),
            0.0f,
            -height * 0.5f + 1,
            0.5f,
            0.0f
        );
        graphics.drawSprite(
            atlas.getRegion(HOT_BAR_SELECTED_TEXTURE),
            hotBarSelectorX(gameRenderer.client().player().selectedHotBar()),
            -height * 0.5f,
            0.0f,
            0.0f
        );
        graphics.endDraw();

        gl.enableDepthTest();
        renderHotBarItems(gl);
    }

    private void renderHotBarItems(GLStateMgr gl) {
        RenderSystem.bindTexture2D(gameRenderer.textureManager().getTexture(TextureManager.BLOCK_ATLAS));
        final Freeworld client = gameRenderer.client();
        final Tessellator tessellator = Tessellator.getInstance();
        for (int i = 0; i < 10; i++) {
            BlockType item = client.player().getHotBarItem(i);
            tessellator.begin(GLDrawMode.TRIANGLES);
            gameRenderer.blockRenderer().renderBlockModel(tessellator,
                client.blockModelManager().get(Registries.BLOCK_TYPE.getId(item)),
                Matrix4f.translation((i - 5) * 20 + 3, -height * 0.5f + 8, 100)
                    .rotateX((float) Math.toRadians(30.0))
                    .rotateY((float) Math.toRadians(45.0))
                    .scale(10),
                0,
                0,
                0,
                _ -> false);
            tessellator.end(gl);
        }
    }

    private float hotBarSelectorX(int selection) {
        return (selection - 5) * 20.0f - 2.0f;
    }

    public void tick() {
    }
}
