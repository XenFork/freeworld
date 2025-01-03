/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2025  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client;

import freeworld.client.event.CursorPosEvent;

import java.lang.foreign.MemorySegment;

import static overrungl.glfw.GLFW.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class MouseInput {
    private final MemorySegment window;
    private double cursorX;
    private double cursorY;
    private double cursorDeltaX;
    private double cursorDeltaY;
    private boolean disabled = false;

    public MouseInput(MemorySegment window) {
        this.window = window;
        glfwSetCursorPosCallback(window, (_, x, y) -> {
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
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    public void disable() {
        disabled = true;
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
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
