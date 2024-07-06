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
import freeworld.client.render.animation.Animation;
import freeworld.client.render.gl.GLDrawMode;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.texture.TextureAtlas;
import freeworld.client.render.texture.TextureManager;
import freeworld.core.Identifier;
import freeworld.core.registry.BuiltinRegistries;
import freeworld.math.Maths;
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
    private final Animation<Float> hotBarSelectorAnimation = new Animation<>(
        hotBarSelectorX(0),
        (start, end, progress) -> (float) Maths.lerp(start, end, progress)
    );
    private int prevHotBarSelection = 0;
    private float width = 0f;
    private float height = 0f;

    public HudRenderer(GameRenderer gameRenderer) {
        this.gameRenderer = gameRenderer;
    }

    public void update(int width, int height) {
        final float guiScale = gameRenderer.client().guiScale();
        this.width = width / guiScale;
        this.height = height / guiScale;
    }

    public void render(GuiGraphics graphics, GLStateMgr gl, double partialTick) {
        RenderSystem.setProjectionMatrix(_ -> Matrix4f
            .setOrtho(0.0f, width, 0.0f, height, -300.0f, 300.0f));
        RenderSystem.setModelMatrix(_ -> Matrix4f
            .translation(width * 0.5f, height * 0.5f, 0.0f));

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
            (float) Maths.lerp(hotBarSelectorAnimation.previous(), hotBarSelectorAnimation.current(), partialTick),
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
        int i = 0;
        for (BlockType blockType : client.hotBar()) {
            try (var _ = RenderSystem.modelMatrixStack().push()) {
                int finalI = i;
                RenderSystem.setModelMatrix(mat -> mat
                    .translate((finalI - 5) * 20 + 3, 0, 0)
                    .translate(0, -height * 0.5f + 8, 100)
                    .rotateX((float) Math.toRadians(30.0))
                    .rotateY((float) Math.toRadians(45.0))
                    .scale(10));
                tessellator.begin(GLDrawMode.TRIANGLES);
                gameRenderer.blockRenderer().renderBlockModel(tessellator, client.blockModelManager().get(BuiltinRegistries.BLOCK_TYPE.getId(blockType)), 0, 0, 0, _ -> false);
                tessellator.end(gl);
                i++;
            }
        }
    }

    private float hotBarSelectorX(int selection) {
        return (selection - 5) * 20.0f - 2.0f;
    }

    public void tick() {
        final int selection = gameRenderer.client().hotBarSelection();
        if (prevHotBarSelection != selection) {
            hotBarSelectorAnimation.reset(
                hotBarSelectorX(selection),
                1
            );
            prevHotBarSelection = selection;
        }
        hotBarSelectorAnimation.tick();
    }
}
