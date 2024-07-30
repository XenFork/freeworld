/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client;

import freeworld.client.event.CursorPosEvent;
import overrungl.glfw.GLFW;

import java.lang.foreign.MemorySegment;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class MouseInput {
    private final GLFW glfw = GLFW.INSTANCE;
    private final MemorySegment window;
    private double cursorX;
    private double cursorY;
    private double cursorDeltaX;
    private double cursorDeltaY;
    private boolean disabled = false;

    public MouseInput(MemorySegment window) {
        this.window = window;
        glfw.setCursorPosCallback(window, (_, x, y) -> {
            cursorDeltaX = x - cursorX;
            cursorDeltaY = y - cursorY;
            if (disabled) {
                CursorPosEvent.DISABLED.publish(new CursorPosEvent(x, y, cursorDeltaX, cursorDeltaY));
            }
            cursorX = x;
            cursorY = y;
        });
    }

    public void enable() {
        disabled = false;
        glfw.setInputMode(window, GLFW.CURSOR, GLFW.CURSOR_NORMAL);
    }

    public void disable() {
        disabled = true;
        glfw.setInputMode(window, GLFW.CURSOR, GLFW.CURSOR_DISABLED);
    }

    public double cursorX() {
        return cursorX;
    }

    public double cursorY() {
        return cursorY;
    }

    public double cursorDeltaX() {
        return cursorDeltaX;
    }

    public double cursorDeltaY() {
        return cursorDeltaY;
    }

    public boolean disabled() {
        return disabled;
    }
}
