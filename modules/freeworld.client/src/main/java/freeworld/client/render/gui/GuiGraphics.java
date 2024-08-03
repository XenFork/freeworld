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
import freeworld.client.render.texture.Texture2D;
import freeworld.client.render.texture.TextureRegion;
import freeworld.client.render.vertex.VertexBuilder;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class GuiGraphics {
    private final GameRenderer gameRenderer;

    public GuiGraphics(GameRenderer gameRenderer) {
        this.gameRenderer = gameRenderer;
    }

    public void drawSprite(VertexBuilder builder, float x, float y, float width, float height, float anchorX, float anchorY, float u0, float u1, float v0, float v1) {
        final float lWidth = width * anchorX;
        final float rWidth = width * (1.0f - anchorX);
        final float bHeight = height * anchorY;
        final float tHeight = height * (1.0f - anchorY);
        builder.indices(0, 1, 2, 2, 3, 0);
        builder.position(x - lWidth, y + tHeight, 0).color(1.0f, 1.0f, 1.0f).texCoord(u0, v0).emit();
        builder.position(x - lWidth, y - bHeight, 0).color(1.0f, 1.0f, 1.0f).texCoord(u0, v1).emit();
        builder.position(x + rWidth, y - bHeight, 0).color(1.0f, 1.0f, 1.0f).texCoord(u1, v1).emit();
        builder.position(x + rWidth, y + tHeight, 0).color(1.0f, 1.0f, 1.0f).texCoord(u1, v0).emit();
    }

    public void drawSprite(VertexBuilder builder, Texture2D texture, float x, float y, float anchorX, float anchorY) {
        drawSprite(builder, x, y, texture.width(), texture.height(), anchorX, anchorY, 0.0f, 1.0f, 0.0f, 1.0f);
    }

    public void drawSprite(VertexBuilder builder, Texture2D texture, float x, float y) {
        drawSprite(builder, texture, x, y, 0.0f, 0.0f);
    }

    public void drawSprite(VertexBuilder builder, TextureRegion textureRegion, float x, float y, float anchorX, float anchorY) {
        if (textureRegion == null) {
            drawSprite(builder, gameRenderer.textureManager().getOrLoad(RenderSystem.stateManager(), Texture2D.MISSING), x, y, anchorX, anchorY);
            return;
        }
        drawSprite(builder, x, y, textureRegion.width(), textureRegion.height(), anchorX, anchorY, textureRegion.u0(), textureRegion.u1(), textureRegion.v0(), textureRegion.v1());
    }

    public void drawSprite(VertexBuilder builder, TextureRegion textureRegion, float x, float y) {
        drawSprite(builder, textureRegion, x, y, 0.0f, 0.0f);
    }

    public void fillRect(VertexBuilder builder, float startX, float startY, float endX, float endY, float red, float green, float blue, float alpha) {
        builder.indices(0, 1, 2, 2, 3, 0);
        builder.position(startX, endY, 0.0f).color(red, green, blue, alpha).emit();
        builder.position(startX, startY, 0.0f).color(red, green, blue, alpha).emit();
        builder.position(endX, startY, 0.0f).color(red, green, blue, alpha).emit();
        builder.position(endX, endY, 0.0f).color(red, green, blue, alpha).emit();
    }
}
