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

import freeworld.client.render.GameRenderer;
import freeworld.client.render.RenderSystem;
import freeworld.client.render.Tessellator;
import freeworld.client.render.gl.GLDrawMode;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.texture.Texture;
import freeworld.client.render.texture.TextureRegion;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class GuiGraphics {
    private final GLStateMgr gl;
    private final GameRenderer gameRenderer;
    private Texture texture = null;

    public GuiGraphics(GLStateMgr gl, GameRenderer gameRenderer) {
        this.gl = gl;
        this.gameRenderer = gameRenderer;
    }

    public void beginDraw() {
        texture = RenderSystem.textureBinding2D();
        Tessellator.getInstance().begin(GLDrawMode.TRIANGLES);
    }

    public void endDraw() {
        Tessellator.getInstance().end(gl);
    }

    public void drawSprite(Texture texture, float x, float y, float anchorX, float anchorY) {
        final Tessellator tessellator = Tessellator.getInstance();
        if (this.texture != texture) {
            tessellator.flush(gl);
            this.texture = texture;
            RenderSystem.bindTexture2D(texture);
        }
        final float lWidth = texture.width() * anchorX;
        final float rWidth = texture.width() * (1.0f - anchorX);
        final float bHeight = texture.height() * anchorY;
        final float tHeight = texture.height() * (1.0f - anchorY);
        tessellator.color(1.0f, 1.0f, 1.0f);
        tessellator.indices(0, 1, 2, 2, 3, 0);
        tessellator.texCoord(0.0f, 0.0f).position(x - lWidth, y + tHeight, 0).emit();
        tessellator.texCoord(0.0f, 1.0f).position(x - lWidth, y - bHeight, 0).emit();
        tessellator.texCoord(1.0f, 1.0f).position(x + rWidth, y - bHeight, 0).emit();
        tessellator.texCoord(1.0f, 0.0f).position(x + rWidth, y + tHeight, 0).emit();
    }

    public void drawSprite(TextureRegion textureRegion, float x, float y, float anchorX, float anchorY) {
        final Tessellator tessellator = Tessellator.getInstance();
        if (textureRegion == null) {
            drawSprite(
                gameRenderer.textureManager().getTexture(Texture.MISSING),
                x,
                y,
                anchorX,
                anchorY
            );
            return;
        }
        if (texture != textureRegion.atlas()) {
            tessellator.flush(gl);
            texture = textureRegion.atlas();
            RenderSystem.bindTexture2D(texture);
        }
        final float lWidth = textureRegion.width() * anchorX;
        final float rWidth = textureRegion.width() * (1.0f - anchorX);
        final float bHeight = textureRegion.height() * anchorY;
        final float tHeight = textureRegion.height() * (1.0f - anchorY);
        final float u0 = textureRegion.u0();
        final float u1 = textureRegion.u1();
        final float v0 = textureRegion.v0();
        final float v1 = textureRegion.v1();
        tessellator.color(1.0f, 1.0f, 1.0f);
        tessellator.indices(0, 1, 2, 2, 3, 0);
        tessellator.texCoord(u0, v0).position(x - lWidth, y + tHeight, 0).emit();
        tessellator.texCoord(u0, v1).position(x - lWidth, y - bHeight, 0).emit();
        tessellator.texCoord(u1, v1).position(x + rWidth, y - bHeight, 0).emit();
        tessellator.texCoord(u1, v0).position(x + rWidth, y + tHeight, 0).emit();
    }

    public void drawSprite(TextureRegion textureRegion, float x, float y) {
        drawSprite(textureRegion, x, y, 0.0f, 0.0f);
    }
}
