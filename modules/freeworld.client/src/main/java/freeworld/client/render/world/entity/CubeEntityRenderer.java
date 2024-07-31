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
import freeworld.client.render.RenderSystem;
import freeworld.client.render.Tessellator;
import freeworld.client.render.gl.GLDrawMode;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.math.Matrix4f;
import freeworld.world.entity.CubeEntity;

/**
 * @author squid233
 * @since 0.1.0
 */
public class CubeEntityRenderer extends EntityRenderer<CubeEntity> {
    public CubeEntityRenderer(Freeworld client) {
        super(client);
    }

    @Override
    public void render(GLStateMgr gl, double partialTick, Matrix4f positionMatrix, CubeEntity entity) {
        RenderSystem.useProgram(client.gameRenderer().positionColorProgram());
        Tessellator t = Tessellator.getInstance();
        t.begin(GLDrawMode.TRIANGLES);
        float x0 = -0.5f;
        float y0 = 0.0f;
        float z0 = -0.5f;
        float x1 = 0.5f;
        float y1 = 1.0f;
        float z1 = 0.5f;

        // -x
        t.indices(0, 1, 2, 2, 3, 0);
        t.position(positionMatrix, x0, y1, z0).color(0, 255, 255).texCoord(0, 0).emit();
        t.position(positionMatrix, x0, y0, z0).color(0, 255, 255).texCoord(0, 0).emit();
        t.position(positionMatrix, x0, y0, z1).color(0, 255, 255).texCoord(0, 0).emit();
        t.position(positionMatrix, x0, y1, z1).color(0, 255, 255).texCoord(0, 0).emit();

        // +x
        t.indices(0, 1, 2, 2, 3, 0);
        t.position(positionMatrix, x1, y1, z1).color(255, 0, 0).texCoord(0, 0).emit();
        t.position(positionMatrix, x1, y0, z1).color(255, 0, 0).texCoord(0, 0).emit();
        t.position(positionMatrix, x1, y0, z0).color(255, 0, 0).texCoord(0, 0).emit();
        t.position(positionMatrix, x1, y1, z0).color(255, 0, 0).texCoord(0, 0).emit();

        // -y
        t.indices(0, 1, 2, 2, 3, 0);
        t.position(positionMatrix, x0, y0, z1).color(255, 0, 220).texCoord(0, 0).emit();
        t.position(positionMatrix, x0, y0, z0).color(255, 0, 220).texCoord(0, 0).emit();
        t.position(positionMatrix, x1, y0, z0).color(255, 0, 220).texCoord(0, 0).emit();
        t.position(positionMatrix, x1, y0, z1).color(255, 0, 220).texCoord(0, 0).emit();

        // +y
        t.indices(0, 1, 2, 2, 3, 0);
        t.position(positionMatrix, x0, y1, z0).color(0, 255, 33).texCoord(0, 0).emit();
        t.position(positionMatrix, x0, y1, z1).color(0, 255, 33).texCoord(0, 0).emit();
        t.position(positionMatrix, x1, y1, z1).color(0, 255, 33).texCoord(0, 0).emit();
        t.position(positionMatrix, x1, y1, z0).color(0, 255, 33).texCoord(0, 0).emit();

        // -z
        t.indices(0, 1, 2, 2, 3, 0);
        t.position(positionMatrix, x1, y1, z0).color(255, 216, 0).texCoord(0, 0).emit();
        t.position(positionMatrix, x1, y0, z0).color(255, 216, 0).texCoord(0, 0).emit();
        t.position(positionMatrix, x0, y0, z0).color(255, 216, 0).texCoord(0, 0).emit();
        t.position(positionMatrix, x0, y1, z0).color(255, 216, 0).texCoord(0, 0).emit();

        // +z
        t.indices(0, 1, 2, 2, 3, 0);
        t.position(positionMatrix, x0, y1, z1).color(0, 148, 255).texCoord(0, 0).emit();
        t.position(positionMatrix, x0, y0, z1).color(0, 148, 255).texCoord(0, 0).emit();
        t.position(positionMatrix, x1, y0, z1).color(0, 148, 255).texCoord(0, 0).emit();
        t.position(positionMatrix, x1, y1, z1).color(0, 148, 255).texCoord(0, 0).emit();

        t.end(gl);
    }
}
