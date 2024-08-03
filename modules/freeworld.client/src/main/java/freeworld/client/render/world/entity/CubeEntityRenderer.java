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
import freeworld.client.render.GameRenderer;
import freeworld.client.render.RenderSystem;
import freeworld.client.render.Tessellator;
import freeworld.client.render.gl.GLDrawMode;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.vertex.BufferBuilder;
import freeworld.client.render.vertex.VertexLayouts;
import freeworld.math.Matrix4f;
import freeworld.world.entity.CubeEntity;

/**
 * @author squid233
 * @since 0.1.0
 */
public class CubeEntityRenderer extends EntityRenderer<CubeEntity> {
    public CubeEntityRenderer(FreeworldClient client) {
        super(client);
    }

    @Override
    public void render(GLStateMgr gl, double partialTick, Matrix4f positionMatrix, CubeEntity entity) {
        RenderSystem.useProgram(GameRenderer.positionColorProgram());
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.buffer();
        buffer.begin(GLDrawMode.TRIANGLES, VertexLayouts.POSITION_COLOR);
        float x0 = -0.5f;
        float y0 = 0.0f;
        float z0 = -0.5f;
        float x1 = 0.5f;
        float y1 = 1.0f;
        float z1 = 0.5f;

        // -x
        buffer.indices(0, 1, 2, 2, 3, 0);
        buffer.position(positionMatrix, x0, y1, z0).color(0, 255, 255).emit();
        buffer.position(positionMatrix, x0, y0, z0).color(0, 255, 255).emit();
        buffer.position(positionMatrix, x0, y0, z1).color(0, 255, 255).emit();
        buffer.position(positionMatrix, x0, y1, z1).color(0, 255, 255).emit();

        // +x
        buffer.indices(0, 1, 2, 2, 3, 0);
        buffer.position(positionMatrix, x1, y1, z1).color(255, 0, 0).emit();
        buffer.position(positionMatrix, x1, y0, z1).color(255, 0, 0).emit();
        buffer.position(positionMatrix, x1, y0, z0).color(255, 0, 0).emit();
        buffer.position(positionMatrix, x1, y1, z0).color(255, 0, 0).emit();

        // -y
        buffer.indices(0, 1, 2, 2, 3, 0);
        buffer.position(positionMatrix, x0, y0, z1).color(255, 0, 220).emit();
        buffer.position(positionMatrix, x0, y0, z0).color(255, 0, 220).emit();
        buffer.position(positionMatrix, x1, y0, z0).color(255, 0, 220).emit();
        buffer.position(positionMatrix, x1, y0, z1).color(255, 0, 220).emit();

        // +y
        buffer.indices(0, 1, 2, 2, 3, 0);
        buffer.position(positionMatrix, x0, y1, z0).color(0, 255, 33).emit();
        buffer.position(positionMatrix, x0, y1, z1).color(0, 255, 33).emit();
        buffer.position(positionMatrix, x1, y1, z1).color(0, 255, 33).emit();
        buffer.position(positionMatrix, x1, y1, z0).color(0, 255, 33).emit();

        // -z
        buffer.indices(0, 1, 2, 2, 3, 0);
        buffer.position(positionMatrix, x1, y1, z0).color(255, 216, 0).emit();
        buffer.position(positionMatrix, x1, y0, z0).color(255, 216, 0).emit();
        buffer.position(positionMatrix, x0, y0, z0).color(255, 216, 0).emit();
        buffer.position(positionMatrix, x0, y1, z0).color(255, 216, 0).emit();

        // +z
        buffer.indices(0, 1, 2, 2, 3, 0);
        buffer.position(positionMatrix, x0, y1, z1).color(0, 148, 255).emit();
        buffer.position(positionMatrix, x0, y0, z1).color(0, 148, 255).emit();
        buffer.position(positionMatrix, x1, y0, z1).color(0, 148, 255).emit();
        buffer.position(positionMatrix, x1, y1, z1).color(0, 148, 255).emit();

        t.draw(gl);
    }
}
