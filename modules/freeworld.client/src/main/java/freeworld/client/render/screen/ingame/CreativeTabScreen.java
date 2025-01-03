/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2025  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.screen.ingame;

import freeworld.client.render.GameRenderer;
import freeworld.client.render.RenderSystem;
import freeworld.client.render.Tessellator;
import freeworld.client.render.gl.GLDrawMode;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.gui.GuiGraphics;
import freeworld.client.render.screen.Screen;
import freeworld.client.render.texture.Texture2D;
import freeworld.client.render.vertex.BufferBuilder;
import freeworld.client.render.vertex.VertexLayouts;
import freeworld.util.Identifier;

import static overrungl.glfw.GLFW.GLFW_KEY_E;

/**
 * @author squid233
 * @since 0.1.0
 */
public class CreativeTabScreen extends Screen {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.ofBuiltin("gui/screen/creative_tab/background");

    @Override
    public void render(GuiGraphics graphics, GLStateMgr gl, double partialTick) {
        super.render(graphics, gl, partialTick);
        drawBackground(graphics, gl, partialTick);
        Texture2D texture = client.gameRenderer().textureManager().getOrLoad(gl, BACKGROUND_TEXTURE);
        RenderSystem.useProgram(GameRenderer.positionColorTexProgram());
        RenderSystem.bindTexture2D(texture);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.buffer();
        buffer.begin(GLDrawMode.TRIANGLES, VertexLayouts.POSITION_COLOR_TEXTURE);
        graphics.drawSprite(
            buffer,
            texture,
            width * 0.5f,
            height * 0.5f,
            0.5f,
            0.5f
        );
        t.draw(gl);
    }

    @Override
    public void onKeyPressed(int key) {
        super.onKeyPressed(key);
        if (key == GLFW_KEY_E) {
            client.setScreen(null);
        }
    }
}
