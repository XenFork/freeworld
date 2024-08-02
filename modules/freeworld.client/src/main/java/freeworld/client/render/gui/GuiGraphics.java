/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.gui;

import freeworld.client.render.GameRenderer;
import freeworld.client.render.RenderSystem;
import freeworld.client.render.Tessellator;
import freeworld.client.render.gl.GLDrawMode;
import freeworld.client.render.gl.GLProgram;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.texture.Texture2D;
import freeworld.client.render.texture.TextureRegion;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class GuiGraphics {
    private final GLStateMgr gl;
    private final GameRenderer gameRenderer;
    private final Tessellator tessellator = Tessellator.getInstance();
    private GLProgram program = null;
    private Texture2D texture = null;

    public GuiGraphics(GLStateMgr gl, GameRenderer gameRenderer) {
        this.gl = gl;
        this.gameRenderer = gameRenderer;
    }

    private void updateProgram(GLProgram program) {
        if (this.program != program) {
            tessellator.flush(gl);
            this.program = program;
            RenderSystem.useProgram(program);
        }
    }

    private void updateTexture(Texture2D texture) {
        if (this.texture != texture) {
            tessellator.flush(gl);
            this.texture = texture;
            RenderSystem.bindTexture2D(texture);
        }
    }

    public void beginDraw(GLDrawMode drawMode) {
        program = RenderSystem.currentProgram();
        texture = RenderSystem.textureBinding2D();
        RenderSystem.updateMatrices();
        tessellator.begin(drawMode);
    }

    public void beginDraw() {
        beginDraw(GLDrawMode.TRIANGLES);
    }

    public void endDraw() {
        tessellator.end(gl);
    }

    public void drawSprite(Texture2D texture, float x, float y, float width, float height, float anchorX, float anchorY, float u0, float u1, float v0, float v1) {
        updateProgram(gameRenderer.positionColorTexProgram());
        updateTexture(texture);
        final float lWidth = width * anchorX;
        final float rWidth = width * (1.0f - anchorX);
        final float bHeight = height * anchorY;
        final float tHeight = height * (1.0f - anchorY);
        tessellator.indices(0, 1, 2, 2, 3, 0);
        tessellator.position(x - lWidth, y + tHeight, 0).color(1.0f, 1.0f, 1.0f).texCoord(u0, v0).emit();
        tessellator.position(x - lWidth, y - bHeight, 0).color(1.0f, 1.0f, 1.0f).texCoord(u0, v1).emit();
        tessellator.position(x + rWidth, y - bHeight, 0).color(1.0f, 1.0f, 1.0f).texCoord(u1, v1).emit();
        tessellator.position(x + rWidth, y + tHeight, 0).color(1.0f, 1.0f, 1.0f).texCoord(u1, v0).emit();
    }

    public void drawSprite(Texture2D texture, float x, float y, float anchorX, float anchorY) {
        drawSprite(texture, x, y, texture.width(), texture.height(), anchorX, anchorY, 0.0f, 1.0f, 0.0f, 1.0f);
    }

    public void drawSprite(Texture2D texture, float x, float y) {
        drawSprite(texture, x, y, 0.0f, 0.0f);
    }

    public void drawSprite(TextureRegion textureRegion, float x, float y, float anchorX, float anchorY) {
        if (textureRegion == null) {
            drawSprite(gameRenderer.textureManager().getOrLoad(gl, Texture2D.MISSING), x, y, anchorX, anchorY);
            return;
        }
        drawSprite(textureRegion.atlas(), x, y, textureRegion.width(), textureRegion.height(), anchorX, anchorY, textureRegion.u0(), textureRegion.u1(), textureRegion.v0(), textureRegion.v1());
    }

    public void drawSprite(TextureRegion textureRegion, float x, float y) {
        drawSprite(textureRegion, x, y, 0.0f, 0.0f);
    }

    public void fillRect(float startX, float startY, float endX, float endY, float red, float green, float blue, float alpha) {
        updateProgram(gameRenderer.positionColorProgram());
        updateTexture(null);
        tessellator.indices(0, 1, 2, 2, 3, 0);
        tessellator.position(startX, endY, 0.0f).color(red, green, blue, alpha).texCoord(0f, 0f).emit();
        tessellator.position(startX, startY, 0.0f).color(red, green, blue, alpha).texCoord(0f, 0f).emit();
        tessellator.position(endX, startY, 0.0f).color(red, green, blue, alpha).texCoord(0f, 0f).emit();
        tessellator.position(endX, endY, 0.0f).color(red, green, blue, alpha).texCoord(0f, 0f).emit();
    }
}
