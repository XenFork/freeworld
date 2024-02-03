/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client;

import io.github.xenfork.freeworld.client.render.RenderThread;
import io.github.xenfork.freeworld.util.Logging;
import org.slf4j.Logger;
import overrun.marshal.Unmarshal;
import overrungl.glfw.GLFW;
import overrungl.glfw.GLFWCallbacks;
import overrungl.glfw.GLFWErrorCallback;
import overrungl.glfw.GLFWVidMode;
import overrungl.util.value.Pair;

import java.lang.foreign.MemorySegment;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client logic
 *
 * @author squid233
 * @since 0.1.0
 */
public final class Freeworld implements AutoCloseable {
    private static final Freeworld INSTANCE = new Freeworld();
    private static final Logger logger = Logging.caller();
    private final AtomicBoolean windowOpen = new AtomicBoolean();
    private final GLFW glfw;
    private MemorySegment window;
    private int framebufferWidth;
    private int framebufferHeight;
    private final AtomicBoolean framebufferResized = new AtomicBoolean();

    private Freeworld() {
        this.glfw = GLFW.INSTANCE;
    }

    public void start() {
        logger.info("Starting client");

        GLFWErrorCallback.createLog(logger::error).set();

        if (!glfw.init()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        glfw.defaultWindowHints();
        glfw.windowHint(GLFW.OPENGL_PROFILE, GLFW.OPENGL_CORE_PROFILE);
        glfw.windowHint(GLFW.OPENGL_FORWARD_COMPAT, true);
        glfw.windowHint(GLFW.CONTEXT_VERSION_MAJOR, 3);
        glfw.windowHint(GLFW.CONTEXT_VERSION_MINOR, 3);
        glfw.windowHint(GLFW.VISIBLE, false);
        window = glfw.createWindow(854, 480, "freeworld", MemorySegment.NULL, MemorySegment.NULL);
        if (Unmarshal.isNullPointer(window)) {
            throw new IllegalStateException("Failed to create GLFW window");
        }
        windowOpen.setPlain(true);

        glfw.setFramebufferSizeCallback(window, (_, width, height) -> {
            framebufferWidth = width;
            framebufferHeight = height;
            framebufferResized.setRelease(true);
        });

        final Pair.OfInt framebufferSize = glfw.getFramebufferSize(window);
        framebufferWidth = framebufferSize.x();
        framebufferHeight = framebufferSize.y();
        framebufferResized.setPlain(true);

        // center window
        final GLFWVidMode.Value videoMode = glfw.getVideoMode(glfw.getPrimaryMonitor());
        if (videoMode != null) {
            final Pair.OfInt windowSize = glfw.getWindowSize(window);
            glfw.setWindowPos(window,
                (videoMode.width() - windowSize.x()) / 2,
                (videoMode.height() - windowSize.y()) / 2);
        }

        final RenderThread renderThread = new RenderThread(this, "Render Thread");
        renderThread.setUncaughtExceptionHandler((t, e) -> {
            logger.error("Exception thrown in {}", t, e);
            glfw.setWindowShouldClose(window, true);
            windowOpen.setOpaque(false);
        });
        renderThread.start();

        glfw.showWindow(window);

        run();

        try {
            renderThread.join(Duration.ofSeconds(10));
        } catch (InterruptedException e) {
            logger.error("Render thread interrupted", e);
        }

        logger.info("Closing client");
    }

    public void run() {
        while (!glfw.windowShouldClose(window)) {
            glfw.pollEvents();
        }
        windowOpen.setOpaque(false);
    }

    @Override
    public void close() {
        if (!Unmarshal.isNullPointer(window)) {
            GLFWCallbacks.free(window);
            glfw.destroyWindow(window);
        }
        glfw.terminate();
        glfw.setErrorCallback(null);
    }

    public AtomicBoolean windowOpen() {
        return windowOpen;
    }

    public GLFW glfw() {
        return glfw;
    }

    public MemorySegment window() {
        return window;
    }

    public int framebufferWidth() {
        return framebufferWidth;
    }

    public int framebufferHeight() {
        return framebufferHeight;
    }

    public AtomicBoolean framebufferResized() {
        return framebufferResized;
    }

    public static Freeworld getInstance() {
        return INSTANCE;
    }
}
