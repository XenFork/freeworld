/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.world.entity;

import freeworld.client.FreeworldClient;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.math.Matrix4f;
import freeworld.world.entity.Entity;

/**
 * @author squid233
 * @since 0.1.0
 */
public abstract class EntityRenderer<T extends Entity> {
    protected final FreeworldClient context;

    protected EntityRenderer(FreeworldClient context) {
        this.context = context;
    }

    public interface Factory<T extends Entity> {
        EntityRenderer<T> create(FreeworldClient context);
    }

    public abstract void render(GLStateMgr gl, double partialTick, Matrix4f positionMatrix, T entity);
}
