/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.screen.ingame;

import freeworld.client.Freeworld;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.gui.GuiGraphics;
import freeworld.client.render.screen.Screen;

/**
 * @author squid233
 * @since 0.1.0
 */
public class PauseScreen extends Screen {
    public PauseScreen(Freeworld client, Screen parent) {
        super(client, parent);
    }

    @Override
    public void render(GuiGraphics graphics, GLStateMgr gl, double partialTick) {
        super.render(graphics, gl, partialTick);
        graphics.beginDraw();
        drawBackground(graphics, partialTick);
        graphics.endDraw();
    }
}
