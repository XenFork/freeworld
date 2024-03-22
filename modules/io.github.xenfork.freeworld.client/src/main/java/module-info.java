/*
 * freeworld
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

/**
 * freeworld client
 *
 * @author squid233
 * @since 0.1.0
 */
module io.github.xenfork.freeworld.client {
    exports io.github.xenfork.freeworld.client;
    exports io.github.xenfork.freeworld.client.main;
    exports io.github.xenfork.freeworld.client.render;
    exports io.github.xenfork.freeworld.client.render.builder;
    exports io.github.xenfork.freeworld.client.render.gl;
    exports io.github.xenfork.freeworld.client.render.model;
    exports io.github.xenfork.freeworld.client.render.texture;
    exports io.github.xenfork.freeworld.client.render.world;
    exports io.github.xenfork.freeworld.client.util;
    exports io.github.xenfork.freeworld.client.world.chunk;

    requires transitive io.github.xenfork.freeworld.core;
    requires io.github.overrun.marshal;
    requires overrungl.joml;
    requires overrungl.glfw;
    requires overrungl.opengl;
    requires overrungl.stb;
    requires static org.jetbrains.annotations;
}
