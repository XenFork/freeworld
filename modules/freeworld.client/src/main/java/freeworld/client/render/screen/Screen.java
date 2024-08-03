/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.screen;

import freeworld.client.FreeworldClient;
import freeworld.client.render.Tessellator;
import freeworld.client.render.gl.GLDrawMode;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.gui.GuiGraphics;
import freeworld.client.render.vertex.BufferBuilder;
import freeworld.client.render.vertex.VertexLayouts;

/**
 * @author squid233
 * @since 0.1.0
 */
public class Screen {
    protected FreeworldClient client;
    protected int width = 0;
    protected int height = 0;

    public Screen() {
    }

    // process

    public final void init(FreeworldClient client, int width, int height) {
        this.client = client;
        this.width = width;
        this.height = height;
        onInit();
    }

    protected void onInit() {
    }

    public void onResize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    protected void drawBackground(GuiGraphics graphics, GLStateMgr gl, double partialTick) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.buffer();
        buffer.begin(GLDrawMode.TRIANGLES, VertexLayouts.POSITION_COLOR);
        graphics.fillRect(buffer, 0.0f, 0.0f, width, height, 0.0f, 0.0f, 0.0f, 0.5f);
        t.draw(gl);
    }

    public void render(GuiGraphics graphics, GLStateMgr gl, double partialTick) {
    }

    public void onClose() {
    }

    // events

    public void onKeyPressed(int key) {
    }

    public boolean escapeCanClose() {
        return true;
    }
}
