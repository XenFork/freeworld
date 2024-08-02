/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.world.entity.player;

import freeworld.world.World;
import freeworld.world.block.BlockType;
import freeworld.world.block.BlockTypes;
import freeworld.world.entity.Entity;
import freeworld.world.entity.EntityTypes;

/**
 * @author squid233
 * @since 0.1.0
 */
public class PlayerEntity extends Entity {
    private final BlockType[] hotBar = {
        BlockTypes.STONE,
        BlockTypes.DIRT,
        BlockTypes.GRASS_BLOCK,
        BlockTypes.STONE_SLAB,
        BlockTypes.OAK_LOG,
        BlockTypes.COAL_ORE,
        BlockTypes.AIR,
        BlockTypes.AIR,
        BlockTypes.AIR,
        BlockTypes.AIR
    };
    private int selectedHotBar = 0;

    public PlayerEntity(World world) {
        super(EntityTypes.PLAYER, world);
    }

    public void selectHotBar(int index) {
        selectedHotBar = Math.clamp(index, 0, 9);
    }

    public int selectedHotBar() {
        return selectedHotBar;
    }

    public BlockType getHotBarItem(int index) {
        return hotBar[Math.clamp(index, 0, 9)];
    }

    public BlockType getHandItem() {
        return hotBar[selectedHotBar];
    }
}
