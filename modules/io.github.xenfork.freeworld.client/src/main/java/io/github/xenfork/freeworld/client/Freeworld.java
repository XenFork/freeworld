/*
 * freeworld
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package io.github.xenfork.freeworld.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import overrun.marshal.Unmarshal;
import overrungl.glfw.GLFW;
import overrungl.glfw.GLFWCallbacks;
import overrungl.glfw.GLFWErrorCallback;
import overrungl.glfw.GLFWVidMode;
import overrungl.opengl.GL;
import overrungl.opengl.GL10C;
import overrungl.opengl.GLLoader;
import overrungl.util.value.Pair;

import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Client logic
 *
 * @author squid233
 * @since 0.1.0
 */
public final class Freeworld implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Freeworld.class);
    private final GLFW glfw;
    private MemorySegment window;

    public Freeworld() {
        this.glfw = GLFW.INSTANCE;
    }

    public void start() {
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

        // center window
        final GLFWVidMode.Value videoMode = glfw.getVideoMode(glfw.getPrimaryMonitor());
        if (videoMode != null) {
            final Pair.OfInt size = glfw.getWindowSize(window);
            glfw.setWindowPos(window,
                (videoMode.width() - size.x()) / 2,
                (videoMode.height() - size.y()) / 2);
        }

        glfw.makeContextCurrent(window);
        final GL gl = Objects.requireNonNull(GLLoader.load(GLLoader.loadFlags(glfw::getProcAddress)), "Failed to load OpenGL context");
        initGL(gl);

        glfw.showWindow(window);

        run(gl);
    }

    public void initGL(GL gl) {
        gl.clearColor(0.4f, 0.6f, 0.9f, 1.0f);
    }

    public void run(GL gl) {
        while (!glfw.windowShouldClose(window)) {
            glfw.pollEvents();
            render(gl);
        }
    }

    public void render(GL gl) {
        gl.clear(GL10C.COLOR_BUFFER_BIT | GL10C.DEPTH_BUFFER_BIT);
        glfw.swapBuffers(window);
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
}
