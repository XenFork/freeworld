/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.render;

import io.github.xenfork.freeworld.client.Freeworld;
import io.github.xenfork.freeworld.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import overrungl.glfw.GLFW;
import overrungl.opengl.GL;
import overrungl.opengl.GLFlags;
import overrungl.opengl.GLLoader;

import java.util.Objects;

/**
 * The render thread.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class RenderThread extends Thread {
    private static final Logger logger = Logging.caller();
    private final Freeworld client;

    public RenderThread(Freeworld client, @NotNull String name) {
        super(name);
        this.client = client;
    }

    @Override
    public void run() {
        logger.info("Starting render thread");

        final GLFW glfw = client.glfw();
        glfw.makeContextCurrent(client.window());

        final GLFlags glFlags = GLLoader.loadFlags(glfw::getProcAddress);
        final GL gl = Objects.requireNonNull(GLLoader.load(glFlags), "Failed to load OpenGL context");

        ScopedValue
            .where(GameRenderer.OpenGL, gl)
            .where(GameRenderer.OpenGLExt, GLLoader.loadExtension(glFlags))
            .run(() -> {
                try (GameRenderer gameRenderer = new GameRenderer(client, glFlags)) {
                    gameRenderer.init();
                    while (client.windowOpen()) {
                        gameRenderer.render();
                    }
                }
            });

        logger.info("Closing render thread");
    }
}
