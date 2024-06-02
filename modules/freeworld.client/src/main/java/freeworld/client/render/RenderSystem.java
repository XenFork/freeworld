/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render;

import freeworld.client.render.gl.GLProgram;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.util.Logging;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.slf4j.Logger;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class RenderSystem {
    private static final Logger logger = Logging.caller();
    private static GLStateMgr stateMgr = null;
    private static GLProgram currentProgram = null;
    private static final Matrix4fStack projectionMatrix = new Matrix4fStack(32);
    private static final Matrix4fStack viewMatrix = new Matrix4fStack(32);
    private static final Matrix4fStack modelMatrix = new Matrix4fStack(32);
    private static final Matrix4f projectionViewMatrix = new Matrix4f();

    public static void initialize(GLStateMgr gl) {
        logger.info("Initializing render system");
        stateMgr = gl;
    }

    public static void bindProgram(@Nullable GLProgram program) {
        currentProgram = program;
        if (program != null) {
            program.use(stateMgr);
        }
    }

    public static GLProgram currentProgram() {
        return currentProgram;
    }

    public static void setProjectionMatrix(Matrix4fc matrix) {
        projectionMatrix.set(matrix);
    }

    public static Matrix4fStack projectionMatrix() {
        return projectionMatrix;
    }

    public static void setViewMatrix(Matrix4fc matrix) {
        viewMatrix.set(matrix);
    }

    public static Matrix4fStack viewMatrix() {
        return viewMatrix;
    }

    public static void setProjectionViewMatrix(Matrix4fc projection, Matrix4fc view) {
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

    public static Matrix4fc projectionViewMatrix() {
        return projectionMatrix.mul(viewMatrix, projectionViewMatrix);
    }

    public static void setModelMatrix(Matrix4fc matrix) {
        modelMatrix.set(matrix);
        updateModelMatrix();
    }

    public static void updateModelMatrix() {
        if (currentProgram != null && currentProgram.hasUniform(GLProgram.UNIFORM_MODEL_MATRIX)) {
            currentProgram.getUniform(GLProgram.UNIFORM_MODEL_MATRIX).set(modelMatrix);
            currentProgram.uploadUniforms(stateMgr);
        }
    }

    public static Matrix4fStack modelMatrix() {
        return modelMatrix;
    }

    public static void updateMatrices() {
        updateProjectionViewMatrix();
        updateModelMatrix();
    }

    public static void pushMatrices() {
        projectionMatrix.pushMatrix();
        viewMatrix.pushMatrix();
        modelMatrix.pushMatrix();
    }

    public static void popMatrices() {
        projectionMatrix.popMatrix();
        viewMatrix.popMatrix();
        modelMatrix.popMatrix();
    }
}
