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
import io.github.xenfork.freeworld.client.render.GameRenderer;
import io.github.xenfork.freeworld.client.render.gl.GLStateMgr;
import io.github.xenfork.freeworld.core.registry.BuiltinRegistries;
import io.github.xenfork.freeworld.util.Logging;
import io.github.xenfork.freeworld.util.Timer;
import io.github.xenfork.freeworld.world.World;
import io.github.xenfork.freeworld.world.block.BlockTypes;
import io.github.xenfork.freeworld.world.entity.Entity;
import io.github.xenfork.freeworld.world.entity.EntityTypes;
import org.joml.Vector2d;
import org.slf4j.Logger;
import overrun.marshal.Unmarshal;
import overrungl.glfw.GLFW;
import overrungl.glfw.GLFWCallbacks;
import overrungl.glfw.GLFWErrorCallback;
import overrungl.glfw.GLFWVidMode;
import overrungl.opengl.GLFlags;
import overrungl.opengl.GLLoader;
import overrungl.util.value.Pair;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandles;
import java.util.UUID;

/**
 * Client logic
 *
 * @author squid233
 * @since 0.1.0
 */
public final class Freeworld implements AutoCloseable {
    private static final Freeworld INSTANCE = new Freeworld();
    private static final Logger logger = Logging.caller();
    private static final int INIT_WINDOW_WIDTH = 854;
    private static final int INIT_WINDOW_HEIGHT = 480;
    private static final double MOUSE_SENSITIVITY = 0.15;
    private final GLFW glfw;
    private GLFlags glFlags;
    private GLStateMgr gl;
    private MemorySegment window;
    private int framebufferWidth;
    private int framebufferHeight;
    private final Timer timer = new Timer(Timer.DEFAULT_TPS);
    private final Camera camera = new Camera();
    private double cursorX;
    private double cursorY;
    private double cursorDeltaX;
    private double cursorDeltaY;
    private boolean disableCursor = false;
    private GameRenderer gameRenderer;
    private World world;
    private Entity player;

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

        // center window
        final GLFWVidMode.Value videoMode = glfw.getVideoMode(glfw.getPrimaryMonitor());
        if (videoMode != null) {
            glfw.windowHint(GLFW.POSITION_X, (videoMode.width() - INIT_WINDOW_WIDTH) / 2);
            glfw.windowHint(GLFW.POSITION_Y, (videoMode.height() - INIT_WINDOW_HEIGHT) / 2);
        }

        window = glfw.createWindow(INIT_WINDOW_WIDTH, INIT_WINDOW_HEIGHT, "freeworld ~: toggle camera", MemorySegment.NULL, MemorySegment.NULL);
        if (Unmarshal.isNullPointer(window)) {
            throw new IllegalStateException("Failed to create GLFW window");
        }

        glfw.setKeyCallback(window, (_, key, scancode, action, mods) -> onKey(key, scancode, action, mods));
        glfw.setFramebufferSizeCallback(window, (_, width, height) -> onResize(width, height));
        glfw.setCursorPosCallback(window, (_, xpos, ypos) -> onCursorPos(xpos, ypos));

        final Pair.OfInt framebufferSize = glfw.getFramebufferSize(window);
        framebufferWidth = framebufferSize.x();
        framebufferHeight = framebufferSize.y();

        if (glfw.rawMouseMotionSupported()) {
            glfw.setInputMode(window, GLFW.RAW_MOUSE_MOTION, GLFW.TRUE);
        }

        BlockTypes.bootstrap();
        BuiltinRegistries.BLOCK_TYPE.freeze();
        EntityTypes.bootstrap();
        BuiltinRegistries.ENTITY_TYPE.freeze();

        world = new World("New world");
        player = new Entity(world, UUID.randomUUID(), EntityTypes.PLAYER);
        player.position().position().set(1.5, 16.0, 1.5);
        world.entities.add(player);

        initGL();
        run();

        logger.info("Closing client");
    }

    private void onKey(int key, int scancode, int action, int mods) {
        switch (action) {
            case GLFW.RELEASE -> {
                switch (key) {
                    case GLFW.KEY_GRAVE_ACCENT -> {
                        disableCursor = !disableCursor;
                        glfw.setInputMode(window, GLFW.CURSOR, disableCursor ? GLFW.CURSOR_DISABLED : GLFW.CURSOR_NORMAL);
                    }
                }
            }
        }
    }

    private void onResize(int width, int height) {
        framebufferWidth = width;
        framebufferHeight = height;
        gl.viewport(0, 0, width, height);
    }

    private void onCursorPos(double x, double y) {
        cursorDeltaX = x - cursorX;
        cursorDeltaY = y - cursorY;
        if (disableCursor) {
            final double pitch = -cursorDeltaY * MOUSE_SENSITIVITY;
            final double yaw = -cursorDeltaX * MOUSE_SENSITIVITY;
            final Vector2d rotation = player.rotation().rotation();
            final double updateX = Math.clamp(rotation.x() + pitch, -90.0, 90.0);
            double updateY = rotation.y() + yaw;

            if (updateY < 0.0) {
                updateY += 360.0;
            } else if (updateY >= 360.0) {
                updateY -= 360.0;
            }

            rotation.set(updateX, updateY);
        }
        cursorX = x;
        cursorY = y;
    }

    private void tick() {
        camera.preUpdate();
        final double speed = 0.3;
        double xo = 0.0;
        double yo = 0.0;
        double zo = 0.0;
        if (glfw.getKey(window, GLFW.KEY_W) == GLFW.PRESS) zo -= 1.0;
        if (glfw.getKey(window, GLFW.KEY_S) == GLFW.PRESS) zo += 1.0;
        if (glfw.getKey(window, GLFW.KEY_A) == GLFW.PRESS) xo -= 1.0;
        if (glfw.getKey(window, GLFW.KEY_D) == GLFW.PRESS) xo += 1.0;
        if (glfw.getKey(window, GLFW.KEY_LEFT_SHIFT) == GLFW.PRESS) yo -= 1.0;
        if (glfw.getKey(window, GLFW.KEY_SPACE) == GLFW.PRESS) yo += 1.0;
        player.acceleration().acceleration().set(xo, yo, zo).mul(speed);
        world.tick();
    }

    private void initGL() {
        glfw.makeContextCurrent(window);
        glFlags = GLLoader.loadFlags(glfw::getProcAddress);
        gl = GLLoader.loadContext(MethodHandles.lookup(), glFlags, GLStateMgr.class);

        gameRenderer = new GameRenderer(this);
        gameRenderer.init(gl);
    }

    public void run() {
        timer.update();
        while (!glfw.windowShouldClose(window)) {
            glfw.pollEvents();
            timer.update();
            for (int i = 0, c = timer.tickCount(); i < c; i++) {
                tick();
            }
            gameRenderer.render(gl, timer.partialTick());
            glfw.swapBuffers(window);
        }
    }

    @Override
    public void close() {
        gameRenderer.close(gl);
        if (!Unmarshal.isNullPointer(window)) {
            GLFWCallbacks.free(window);
            glfw.destroyWindow(window);
        }
        glfw.terminate();
        glfw.setErrorCallback(null);
    }

    public GLFW glfw() {
        return glfw;
    }

    public GLFlags glFlags() {
        return glFlags;
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

    public Timer timer() {
        return timer;
    }

    public Camera camera() {
        return camera;
    }

    public World world() {
        return world;
    }

    public Entity player() {
        return player;
    }

    public static Freeworld getInstance() {
        return INSTANCE;
    }
}
