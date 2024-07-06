/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.screen;

import freeworld.client.Freeworld;
import freeworld.client.render.RenderSystem;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.gui.GuiGraphics;
import freeworld.math.Matrix4f;

/**
 * @author squid233
 * @since 0.1.0
 */
public class Screen {
    protected final Freeworld client;
    protected final Screen parent;
    protected float width = 0;
    protected float height = 0;

    public Screen(Freeworld client, Screen parent) {
        this.client = client;
        this.parent = parent;
    }

    // process

    public void init(float width, float height) {
        this.width = width;
        this.height = height;
        onInit();
    }

    protected void onInit() {
    }

    public void onResize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    protected void setupMatrix() {
        RenderSystem.setProjectionMatrix(_ -> Matrix4f.setOrtho(0.0f, width, 0.0f, height, -300.0f, 300.0f));
    }

    protected void drawBackground(GuiGraphics graphics, double partialTick) {
        graphics.fillRect(0.0f, 0.0f, width, height, 0.0f, 0.0f, 0.0f, 0.5f);
    }

    public void render(GuiGraphics graphics, GLStateMgr gl, double partialTick) {
        setupMatrix();
    }

    public void close() {
        dispose();
        client.openScreen(parent);
    }

    public void dispose() {
    }

    // events

    public void onKeyPressed(int key) {
    }

    public boolean escapeCanClose() {
        return true;
    }
}
