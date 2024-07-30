/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.world.entity;

import freeworld.world.World;

import java.util.UUID;

/**
 * @author squid233
 * @since 0.1.0
 */
public class PlayerEntity extends Entity {
    public PlayerEntity(World world, UUID uuid) {
        super(world, uuid, EntityTypes.PLAYER);
    }
}
