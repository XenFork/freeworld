/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client;

import freeworld.client.event.CursorPosEvent;
import freeworld.client.render.BufferRenderer;
import freeworld.client.render.Camera;
import freeworld.client.render.GameRenderer;
import freeworld.client.render.RenderSystem;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.screen.Screen;
import freeworld.client.render.screen.ingame.CreativeTabScreen;
import freeworld.client.render.screen.ingame.PauseScreen;
import freeworld.client.render.world.entity.EntityRenderers;
import freeworld.world.block.BlockHitResult;
import freeworld.client.render.world.WorldRenderer;
import freeworld.math.Vector2d;
import freeworld.math.Vector3d;
import freeworld.math.Vector3i;
import freeworld.util.Direction;
import freeworld.util.Logging;
import freeworld.util.Timer;
import freeworld.util.math.MathUtil;
import freeworld.world.World;
import freeworld.world.block.BlockType;
import freeworld.world.block.BlockTypes;
import freeworld.world.entity.EntityTypes;
import freeworld.world.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import overrun.marshal.Unmarshal;
import overrungl.OverrunGL;
import overrungl.glfw.GLFW;
import overrungl.glfw.GLFWCallbacks;
import overrungl.glfw.GLFWErrorCallback;
import overrungl.glfw.GLFWVidMode;
import overrungl.opengl.GLFlags;
import overrungl.opengl.GLLoader;
import overrungl.util.value.Pair;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandles;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Client logic
 *
 * @author squid233
 * @since 0.1.0
 */
public final class FreeworldClient implements Executor, AutoCloseable {
    private static final FreeworldClient INSTANCE = new FreeworldClient();
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
    private MouseInput mouseInput;
    private GameRenderer gameRenderer;
    private World world;
    private PlayerEntity player;
    @Nullable
    private Screen screen = null;
    private final float guiScale = 2;
    private int blockDestroyTimer = 0;
    private int blockPlaceTimer = 0;
    private int gameTick = 0;
    private int spaceTick = 0;
    private boolean debugHudEnabled = false;
    private final Queue<Runnable> queue = new LinkedBlockingQueue<>();

    private FreeworldClient() {
        this.glfw = GLFW.INSTANCE;
    }

    public void start() {
        logger.info("Starting client");

        OverrunGL.setApiLogger(logger::error);
        GLFWErrorCallback.createLog(logger::error).set();

        if (!glfw.init()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        glfw.defaultWindowHints();

        // center window
        final GLFWVidMode videoMode = glfw.getVideoMode(glfw.getPrimaryMonitor());
        if (videoMode != null) {
            glfw.windowHint(GLFW.POSITION_X, (videoMode.width() - INIT_WINDOW_WIDTH) / 2);
            glfw.windowHint(GLFW.POSITION_Y, (videoMode.height() - INIT_WINDOW_HEIGHT) / 2);
        }

        window = glfw.createWindow(INIT_WINDOW_WIDTH, INIT_WINDOW_HEIGHT, "freeworld", MemorySegment.NULL, MemorySegment.NULL);
        if (Unmarshal.isNullPointer(window)) {
            throw new IllegalStateException("Failed to create GLFW window");
        }

        mouseInput = new MouseInput(this, window);
        CursorPosEvent.DISABLED.subscribe(this::onCursorPosDisabled);
        glfw.setKeyCallback(window, (_, key, scancode, action, mods) -> onKey(key, scancode, action, mods));
        glfw.setFramebufferSizeCallback(window, (_, width, height) -> onResize(width, height));
        glfw.setScrollCallback(window, (_, scrollX, scrollY) -> onScroll(scrollX, scrollY));

        final Pair.OfInt framebufferSize = glfw.getFramebufferSize(window);
        framebufferWidth = framebufferSize.x();
        framebufferHeight = framebufferSize.y();

        if (glfw.rawMouseMotionSupported()) {
            glfw.setInputMode(window, GLFW.RAW_MOUSE_MOTION, GLFW.TRUE);
        }

        BlockTypes.bootstrap();
        EntityTypes.bootstrap();
        world = new World("New world", new Random().nextLong());
        player = world.createEntity(EntityTypes.PLAYER, new Vector3d(0.0, 64.0, 0.0));

        World.forChunksInRange(player, WorldRenderer.RENDER_RADIUS, (x, y, z) -> world.getOrCreateChunk(x, y, z));

        initGL();
        run();
    }

    private void onKey(int key, int scancode, int action, int mods) {
        switch (action) {
            case GLFW.PRESS -> {
                switch (key) {
                    case GLFW.KEY_ESCAPE -> {
                        if (screen != null) {
                            if (screen.escapeCanClose()) {
                                setScreen(null);
                            }
                        } else if (world != null) {
                            setScreen(new PauseScreen());
                        }
                    }
                    default -> {
                        if (screen == null) {
                            if (world != null) {
                                switch (key) {
                                    case GLFW.KEY_1 -> player.selectHotBar(0);
                                    case GLFW.KEY_2 -> player.selectHotBar(1);
                                    case GLFW.KEY_3 -> player.selectHotBar(2);
                                    case GLFW.KEY_4 -> player.selectHotBar(3);
                                    case GLFW.KEY_5 -> player.selectHotBar(4);
                                    case GLFW.KEY_6 -> player.selectHotBar(5);
                                    case GLFW.KEY_7 -> player.selectHotBar(6);
                                    case GLFW.KEY_8 -> player.selectHotBar(7);
                                    case GLFW.KEY_9 -> player.selectHotBar(8);
                                    case GLFW.KEY_0 -> player.selectHotBar(9);
                                    case GLFW.KEY_E -> setScreen(new CreativeTabScreen());
                                    case GLFW.KEY_SPACE -> {
                                        if (gameTick - spaceTick < 5) {
                                            player.flying = !player.flying();
                                        }
                                        spaceTick = gameTick;
                                    }
                                    case GLFW.KEY_F3 -> debugHudEnabled = !debugHudEnabled;
                                }
                            }
                        } else {
                            screen.onKeyPressed(key);
                        }
                    }
                }
                if (world != null) {
                    if (screen == null) {
                        mouseInput.disable();
                    } else {
                        mouseInput.enable();
                    }
                } else {
                    mouseInput.enable();
                }
            }
        }
    }

    private void onResize(int width, int height) {
        framebufferWidth = width;
        framebufferHeight = height;
        gl.viewport(0, 0, width, height);

        if (screen != null) {
            screen.onResize(scaledFramebufferWidth(), scaledFramebufferHeight());
        }
    }

    private void onCursorPosDisabled(CursorPosEvent event) {
        final double pitch = -event.deltaY() * MOUSE_SENSITIVITY;
        final double yaw = -event.deltaX() * MOUSE_SENSITIVITY;
        final double updateX = Math.clamp(player.rotation().x() + pitch, -90.0, 90.0);
        double updateY = player.rotation().y() + yaw;

        if (updateY < 0.0) {
            updateY += 360.0;
        } else if (updateY >= 360.0) {
            updateY -= 360.0;
        }

        player.rotation = new Vector2d(updateX, updateY);
    }

    private void onScroll(double x, double y) {
        if (screen == null) {
            if (world != null) {
                int hotBarSelection = player.selectedHotBar();
                if (y < 0.0) {
                    hotBarSelection++;
                } else if (y > 0.0) {
                    hotBarSelection--;
                }
                if (hotBarSelection > 9) {
                    hotBarSelection = 0;
                } else if (hotBarSelection < 0) {
                    hotBarSelection = 9;
                }
                player.selectHotBar(hotBarSelection);
            }
        }
    }

    private void worldInput() {
        double speed;
        if (player.flying()) {
            speed = 0.5; // TODO: this value is only for test
        } else {
            if (player.onGround()) {
                speed = 0.1;
            } else {
                speed = 0.02;
            }
        }
        if (glfw.getKey(window, GLFW.KEY_LEFT_CONTROL) == GLFW.PRESS) speed *= 2.0;
        double xo = 0.0;
        double yo = 0.0;
        double zo = 0.0;
        if (glfw.getKey(window, GLFW.KEY_W) == GLFW.PRESS) zo -= 1.0;
        if (glfw.getKey(window, GLFW.KEY_S) == GLFW.PRESS) zo += 1.0;
        if (glfw.getKey(window, GLFW.KEY_A) == GLFW.PRESS) xo -= 1.0;
        if (glfw.getKey(window, GLFW.KEY_D) == GLFW.PRESS) xo += 1.0;
        if ((player.onGround() || player.flying()) && glfw.getKey(window, GLFW.KEY_SPACE) == GLFW.PRESS) {
            yo += 1.0;
        }
        if (player.flying() && glfw.getKey(window, GLFW.KEY_LEFT_SHIFT) == GLFW.PRESS) {
            yo -= 1.0;
        }
        player.acceleration = MathUtil.moveRelative(xo, yo * (player.flying() ? speed : 0.5), zo, player.rotation().y(), speed);

        if (blockDestroyTimer >= 2) {
            final BlockHitResult hitResult = gameRenderer.hitResult();
            if (!hitResult.missed() &&
                glfw.getMouseButton(window, GLFW.MOUSE_BUTTON_LEFT) == GLFW.PRESS) {
                Vector3i position = hitResult.position();
                world.setBlock(position.x(), position.y(), position.z(), BlockTypes.AIR);
                blockDestroyTimer = 0;
            }
        }
        if (blockPlaceTimer >= 2) {
            final BlockHitResult hitResult = gameRenderer.hitResult();
            if (!hitResult.missed() &&
                glfw.getMouseButton(window, GLFW.MOUSE_BUTTON_RIGHT) == GLFW.PRESS) {
                final Direction face = hitResult.face();
                final BlockType type = player.getHandItem();
                if (!type.air()) {
                    Vector3i axis = face.axis();
                    Vector3i position = hitResult.position();
                    Vector3i add = position.add(axis);
                    if (world.getBlock(position).replaceable() ||
                        world.getBlock(add).replaceable()) {
                        world.setBlock(add.x(), add.y(), add.z(), type);
                    }
                }
                blockPlaceTimer = 0;
            }
        }
        blockDestroyTimer++;
        blockPlaceTimer++;

        if (glfw.getKey(window, GLFW.KEY_G) == GLFW.PRESS) {
            world.createEntity(EntityTypes.CUBE, player.position());
        }
    }

    private void tick() {
        if (world != null) {
            if (screen == null) {
                worldInput();
            }
            world.tick();
        }
        gameRenderer.tick();

        gameTick++;
    }

    private void initGL() {
        glfw.makeContextCurrent(window);
        glFlags = GLLoader.loadFlags(glfw::getProcAddress);
        gl = GLLoader.loadContext(MethodHandles.lookup(), glFlags, GLStateMgr.class);

        RenderSystem.initialize(gl);

        EntityRenderers.bootstrap();

        gameRenderer = new GameRenderer(this);
        gameRenderer.init(gl);

        setScreen(new PauseScreen());
    }

    public void run() {
        timer.update();
        while (!glfw.windowShouldClose(window)) {
            Runnable task;
            while ((task = queue.poll()) != null) {
                task.run();
            }
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
        logger.info("Closing client");
        gameRenderer.close(gl);
        if (!Unmarshal.isNullPointer(window)) {
            GLFWCallbacks.free(window);
            glfw.destroyWindow(window);
        }
        glfw.terminate();
        glfw.setErrorCallback(null);
    }

    public void setScreen(@Nullable Screen screen) {
        if (this.screen != null) {
            this.screen.onClose();
        }
        this.screen = screen;
        BufferRenderer.reset(gl);
        if (screen != null) {
            screen.init(this, scaledFramebufferWidth(), scaledFramebufferHeight());
        }
    }

    @Override
    public void execute(@NotNull Runnable command) {
        queue.offer(command);
    }

    public @Nullable Screen screen() {
        return screen;
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

    public int scaledFramebufferWidth() {
        return (int) (framebufferWidth / guiScale);
    }

    public int scaledFramebufferHeight() {
        return (int) (framebufferHeight / guiScale);
    }

    public Timer timer() {
        return timer;
    }

    public Camera camera() {
        return camera;
    }

    public MouseInput mouseInput() {
        return mouseInput;
    }

    public GameRenderer gameRenderer() {
        return gameRenderer;
    }

    public World world() {
        return world;
    }

    public PlayerEntity player() {
        return player;
    }

    public float guiScale() {
        return guiScale;
    }

    public boolean debugHudEnabled() {
        return debugHudEnabled;
    }

    public static FreeworldClient getInstance() {
        return INSTANCE;
    }
}
