/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render;

import freeworld.client.render.gl.GLProgram;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.texture.Texture;
import freeworld.math.Matrix4f;
import freeworld.util.Logging;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class RenderSystem {
    private static final Logger logger = Logging.caller();
    private static GLStateMgr stateMgr = null;
    private static GLProgram currentProgram = null;
    private static Texture textureBinding2D = null;
    private static Matrix4f projectionMatrix = Matrix4f.IDENTITY;
    private static Matrix4f viewMatrix = Matrix4f.IDENTITY;
    private static Matrix4f modelMatrix = Matrix4f.IDENTITY;

    public static void initialize(GLStateMgr gl) {
        logger.info("Initializing render system");
        stateMgr = gl;
    }

    public static void useProgram(@Nullable GLProgram program) {
        currentProgram = program;
        if (program != null) {
            program.use(stateMgr);
        } else {
            stateMgr.setCurrentProgram(0);
        }
    }

    public static GLProgram currentProgram() {
        return currentProgram;
    }

    public static void bindTexture2D(Texture texture) {
        textureBinding2D = texture;
        if (texture != null) {
            texture.bind(stateMgr);
        } else {
            stateMgr.setTextureBinding2D(0);
        }
    }

    public static Texture textureBinding2D() {
        return textureBinding2D;
    }

    public static void setProjectionMatrix(Matrix4f matrix) {
        projectionMatrix = matrix;
    }

    public static Matrix4f projectionMatrix() {
        return projectionMatrix;
    }

    public static void setViewMatrix(Matrix4f matrix) {
        viewMatrix = matrix;
    }

    public static Matrix4f viewMatrix() {
        return viewMatrix;
    }

    public static void setProjectionViewMatrix(Matrix4f projection, Matrix4f view) {
        setProjectionMatrix(projection);
        setViewMatrix(view);
        updateProjectionViewMatrix();
    }

    public static void updateProjectionViewMatrix() {
        if (currentProgram != null && currentProgram.hasUniform(GLProgram.UNIFORM_PROJECTION_VIEW_MATRIX)) {
            currentProgram.getUniform(GLProgram.UNIFORM_PROJECTION_VIEW_MATRIX).set(projectionViewMatrix());
            currentProgram.uploadUniforms(stateMgr);
        }
    }

    public static Matrix4f projectionViewMatrix() {
        return projectionMatrix.mul(viewMatrix);
    }

    public static void setModelMatrix(Matrix4f matrix) {
        modelMatrix = matrix;
        updateModelMatrix();
    }

    public static void updateModelMatrix() {
        if (currentProgram != null && currentProgram.hasUniform(GLProgram.UNIFORM_MODEL_MATRIX)) {
            currentProgram.getUniform(GLProgram.UNIFORM_MODEL_MATRIX).set(modelMatrix);
            currentProgram.uploadUniforms(stateMgr);
        }
    }

    public static Matrix4f modelMatrix() {
        return modelMatrix;
    }

    public static void updateMatrices() {
        updateProjectionViewMatrix();
        updateModelMatrix();
    }

    @ApiStatus.Internal
    public static GLStateMgr stateManager() {
        return stateMgr;
    }
}
