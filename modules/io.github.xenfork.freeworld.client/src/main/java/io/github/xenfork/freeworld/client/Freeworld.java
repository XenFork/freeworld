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

import io.github.xenfork.freeworld.client.render.Camera;
import io.github.xenfork.freeworld.client.render.RenderThread;
import io.github.xenfork.freeworld.core.registry.BuiltinRegistries;
import io.github.xenfork.freeworld.util.Logging;
import io.github.xenfork.freeworld.util.Timer;
import io.github.xenfork.freeworld.world.World;
import io.github.xenfork.freeworld.world.block.BlockTypes;
import org.slf4j.Logger;
import overrun.marshal.Unmarshal;
import overrungl.glfw.*;
import overrungl.util.value.Pair;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;
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
    private final Timer timer = new Timer(Timer.DEFAULT_TPS);
    private final Camera camera = new Camera();
    private double cursorX;
    private double cursorY;
    private double cursorDeltaX;
    private double cursorDeltaY;
    private boolean fullscreen = false;
    private int oldWindowX = 0;
    private int oldWindowY = 0;
    private int oldWindowW = 0;
    private int oldWindowH = 0;
    private World world;

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
        window = glfw.createWindow(854, 480, "freeworld", FreeworldConfig.options.get().fullscreen ? glfw.getPrimaryMonitor() : MemorySegment.NULL, MemorySegment.NULL);
        if (Unmarshal.isNullPointer(window)) {
            throw new IllegalStateException("Failed to create GLFW window");
        }
        windowOpen.setPlain(true);

        glfw.setFramebufferSizeCallback(window, (_, width, height) -> {
            framebufferWidth = width;
            framebufferHeight = height;
            framebufferResized.setRelease(true);
        });
        glfw.setCursorPosCallback(window, (_, xpos, ypos) -> onCursorPos(xpos, ypos));
        glfw.setKeyCallback(window, (_, key, _, action, _) -> {
            if (action == GLFW.RELEASE && key == GLFW.KEY_F11) {
                if (fullscreen) {
                    glfw.setWindowMonitor(window, MemorySegment.NULL, oldWindowX, oldWindowY, oldWindowW, oldWindowH, GLFW.DONT_CARE);
                    fullscreen = false;
                } else {
                    final MemorySegment primaryMonitor = glfw.getPrimaryMonitor();
                    final GLFWVidMode.Value videoMode = glfw.getVideoMode(primaryMonitor);
                    if (videoMode != null) {
                        final Pair.OfInt pos = glfw.getWindowPos(window);
                        final Pair.OfInt size = glfw.getFramebufferSize(window);
                        oldWindowX = pos.x();
                        oldWindowY = pos.y();
                        oldWindowW = size.x();
                        oldWindowH = size.y();
                        glfw.setWindowMonitor(window, primaryMonitor, 0, 0, videoMode.width(), videoMode.height(), videoMode.refreshRate());
                        fullscreen = true;
                    }
                }
            }
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

        BlockTypes.bootstrap();
        BuiltinRegistries.BLOCK_TYPE.freeze();

        camera.setPosition(1.5, 16.0, 1.5);

        world = new World("world", "world");

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

    private void onCursorPos(double x, double y) {
        cursorDeltaX = x - cursorX;
        cursorDeltaY = y - cursorY;
        if (glfw.getMouseButton(window, GLFW.MOUSE_BUTTON_RIGHT) == GLFW.PRESS) {
            camera.rotate(-cursorDeltaY * 0.7, -cursorDeltaX * 0.7);
        }
        cursorX = x;
        cursorY = y;
    }

    private void tick() {
        camera.preUpdate();
        final double speed = 0.5;
        double xo = 0.0;
        double yo = 0.0;
        double zo = 0.0;
        if (glfw.getKey(window, GLFW.KEY_W) == GLFW.PRESS) zo -= 1.0;
        if (glfw.getKey(window, GLFW.KEY_S) == GLFW.PRESS) zo += 1.0;
        if (glfw.getKey(window, GLFW.KEY_A) == GLFW.PRESS) xo -= 1.0;
        if (glfw.getKey(window, GLFW.KEY_D) == GLFW.PRESS) xo += 1.0;
        if (glfw.getKey(window, GLFW.KEY_LEFT_SHIFT) == GLFW.PRESS) yo -= 1.0;
        if (glfw.getKey(window, GLFW.KEY_SPACE) == GLFW.PRESS) yo += 1.0;
        camera.moveRelative(xo, yo, zo, speed);
    }

    public void run() {
        timer.update();
        while (!glfw.windowShouldClose(window)) {
            glfw.pollEvents();
            VarHandle.releaseFence();
            timer.update();
            for (int i = 0, c = timer.tickCount(); i < c; i++) {
                tick();
            }
        }
        windowOpen.setOpaque(false);
    }

    @Override
    public void close() {
        FreeworldConfig.closeAll();
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

    public Timer timer() {
        return timer;
    }

    public Camera camera() {
        return camera;
    }

    public World world() {
        return world;
    }

    public static Freeworld getInstance() {
        return INSTANCE;
    }
}
