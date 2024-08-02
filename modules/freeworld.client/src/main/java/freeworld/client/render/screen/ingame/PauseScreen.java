/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.screen.ingame;

import freeworld.client.FreeworldClient;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.gui.GuiGraphics;
import freeworld.client.render.screen.Screen;

/**
 * @author squid233
 * @since 0.1.0
 */
public class PauseScreen extends Screen {
    public PauseScreen(FreeworldClient client) {
        super(client);
    }

    @Override
    public void render(GuiGraphics graphics, GLStateMgr gl, double partialTick) {
        super.render(graphics, gl, partialTick);
        graphics.beginDraw();
        drawBackground(graphics, partialTick);
        graphics.endDraw();
    }
}
