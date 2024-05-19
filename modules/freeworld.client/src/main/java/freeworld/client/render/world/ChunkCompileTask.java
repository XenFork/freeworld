/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.world;

import freeworld.world.entity.Entity;
import freeworld.world.entity.component.PositionComponent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class ChunkCompileTask extends FutureTask<ChunkVertexData> implements Comparable<ChunkCompileTask> {
    private final Entity player;
    private final int x;
    private final int y;
    private final int z;

    public ChunkCompileTask(@NotNull Callable<ChunkVertexData> callable, Entity player, int x, int y, int z) {
        super(callable);
        this.player = player;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int compareTo(@NotNull ChunkCompileTask o) {
        if (player.hasComponent(PositionComponent.ID)) {
            return Double.compare(distanceSquared(), o.distanceSquared());
        }
        return 0;
    }

    private double distanceSquared() {
        final Vector3d value = player.position().value();
        return value.distanceSquared(x, y, z);
    }
}
