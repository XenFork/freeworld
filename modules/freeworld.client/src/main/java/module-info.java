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
module freeworld.client {
    exports freeworld.client;
    exports freeworld.client.main;
    exports freeworld.client.render;
    exports freeworld.client.render.builder;
    exports freeworld.client.render.gl;
    exports freeworld.client.render.model;
    exports freeworld.client.render.texture;
    exports freeworld.client.render.world;
    exports freeworld.client.util;
    exports freeworld.client.world.chunk;

    requires transitive freeworld.core;
    requires io.github.overrun.marshal;
    requires overrungl.glfw;
    requires overrungl.opengl;
    requires overrungl.stb;
    requires org.apache.commons.pool2;
    requires static org.jetbrains.annotations;
    requires org.reactivestreams;
    requires reactor.core;
    requires reactor.pool;
}
