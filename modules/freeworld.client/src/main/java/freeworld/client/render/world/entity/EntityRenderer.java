/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.world.entity;

import freeworld.client.Freeworld;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.math.Matrix4f;
import freeworld.world.entity.Entity;

/**
 * @author squid233
 * @since 0.1.0
 */
public abstract class EntityRenderer<T extends Entity> {
    protected final Freeworld client;

    protected EntityRenderer(Freeworld client) {
        this.client = client;
    }

    public interface Factory<T extends Entity> {
        EntityRenderer<T> create(Freeworld client);
    }

    public abstract void render(GLStateMgr gl, double partialTick, Matrix4f positionMatrix, T entity);
}
