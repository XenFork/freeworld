/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.render.world;

import io.github.xenfork.freeworld.client.render.GameRenderer;
import io.github.xenfork.freeworld.client.render.Tessellator;
import io.github.xenfork.freeworld.client.texture.TextureAtlas;
import io.github.xenfork.freeworld.client.texture.TextureManager;
import io.github.xenfork.freeworld.client.texture.TextureRegion;
import io.github.xenfork.freeworld.core.Identifier;
import io.github.xenfork.freeworld.core.registry.BuiltinRegistries;
import io.github.xenfork.freeworld.world.block.BlockState;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BlockRenderer {
    private final GameRenderer gameRenderer;

    public BlockRenderer(GameRenderer gameRenderer) {
        this.gameRenderer = gameRenderer;
    }

    public void renderBlock(Tessellator t, BlockState blockState, int x, int y, int z) {
        final TextureAtlas texture = (TextureAtlas) gameRenderer.textureManager().getTexture(TextureManager.BLOCK_ATLAS);
        final TextureRegion region = texture.getRegion(BuiltinRegistries.BLOCK_TYPE.getId(blockState.blockType()).toResourceId("texture/block", Identifier.EXT_PNG));
        if (region == null) {
            return;
        }

        final float x0 = x;
        final float y0 = y;
        final float z0 = z;
        final float x1 = x0 + 1f;
        final float y1 = y0 + 1f;
        final float z1 = z0 + 1f;
        final float u0 = region.u0(texture.width());
        final float u1 = region.u1(texture.width());
        final float v0 = region.v0(texture.height());
        final float v1 = region.v1(texture.height());
        // -x
        t.index(0, 1, 2, 2, 3, 0);
        t.position(x0, y1, z0).color(1f, 1f, 1f).texCoord(u0, v0).emit();
        t.position(x0, y0, z0).color(1f, 1f, 1f).texCoord(u0, v1).emit();
        t.position(x0, y0, z1).color(1f, 1f, 1f).texCoord(u1, v1).emit();
        t.position(x0, y1, z1).color(1f, 1f, 1f).texCoord(u1, v0).emit();
        // +x
        t.index(0, 1, 2, 2, 3, 0);
        t.position(x1, y1, z1).color(1f, 1f, 1f).texCoord(u0, v0).emit();
        t.position(x1, y0, z1).color(1f, 1f, 1f).texCoord(u0, v1).emit();
        t.position(x1, y0, z0).color(1f, 1f, 1f).texCoord(u1, v1).emit();
        t.position(x1, y1, z0).color(1f, 1f, 1f).texCoord(u1, v0).emit();
        // -y
        t.index(0, 1, 2, 2, 3, 0);
        t.position(x0, y0, z1).color(1f, 1f, 1f).texCoord(u0, v0).emit();
        t.position(x0, y0, z0).color(1f, 1f, 1f).texCoord(u0, v1).emit();
        t.position(x1, y0, z0).color(1f, 1f, 1f).texCoord(u1, v1).emit();
        t.position(x1, y0, z1).color(1f, 1f, 1f).texCoord(u1, v0).emit();
        // +y
        t.index(0, 1, 2, 2, 3, 0);
        t.position(x0, y1, z0).color(1f, 1f, 1f).texCoord(u0, v0).emit();
        t.position(x0, y1, z1).color(1f, 1f, 1f).texCoord(u0, v1).emit();
        t.position(x1, y1, z1).color(1f, 1f, 1f).texCoord(u1, v1).emit();
        t.position(x1, y1, z0).color(1f, 1f, 1f).texCoord(u1, v0).emit();
        // -z
        t.index(0, 1, 2, 2, 3, 0);
        t.position(x1, y1, z0).color(1f, 1f, 1f).texCoord(u0, v0).emit();
        t.position(x1, y0, z0).color(1f, 1f, 1f).texCoord(u0, v1).emit();
        t.position(x0, y0, z0).color(1f, 1f, 1f).texCoord(u1, v1).emit();
        t.position(x0, y1, z0).color(1f, 1f, 1f).texCoord(u1, v0).emit();
        // +z
        t.index(0, 1, 2, 2, 3, 0);
        t.position(x0, y1, z1).color(1f, 1f, 1f).texCoord(u0, v0).emit();
        t.position(x0, y0, z1).color(1f, 1f, 1f).texCoord(u0, v1).emit();
        t.position(x1, y0, z1).color(1f, 1f, 1f).texCoord(u1, v1).emit();
        t.position(x1, y1, z1).color(1f, 1f, 1f).texCoord(u1, v0).emit();
    }
}
