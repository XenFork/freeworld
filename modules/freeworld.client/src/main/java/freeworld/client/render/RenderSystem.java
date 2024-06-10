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
import freeworld.math.Matrix4f;
import freeworld.math.Matrix4fStack;
import freeworld.util.Logging;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.function.UnaryOperator;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class RenderSystem {
    private static final Logger logger = Logging.caller();
    private static GLStateMgr stateMgr = null;
    private static GLProgram currentProgram = null;
    private static final Matrix4fStack projectionMatrixStack = new Matrix4fStack(32);
    private static final Matrix4fStack viewMatrixStack = new Matrix4fStack(32);
    private static final Matrix4fStack modelMatrixStack = new Matrix4fStack(32);

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

    public static void setProjectionMatrix(UnaryOperator<Matrix4f> matrix) {
        projectionMatrixStack.withCurr(matrix);
    }

    public static Matrix4fStack projectionMatrixStack() {
        return projectionMatrixStack;
    }

    public static void setViewMatrix(UnaryOperator<Matrix4f> matrix) {
        viewMatrixStack.withCurr(matrix);
    }

    public static Matrix4fStack viewMatrixStack() {
        return viewMatrixStack;
    }

    public static void setProjectionViewMatrix(UnaryOperator<Matrix4f> projection, UnaryOperator<Matrix4f> view) {
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
        return projectionMatrixStack.curr().mul(viewMatrixStack.curr());
    }

    public static void setModelMatrix(UnaryOperator<Matrix4f> matrix) {
        modelMatrixStack.withCurr(matrix);
        updateModelMatrix();
    }

    public static void updateModelMatrix() {
        if (currentProgram != null && currentProgram.hasUniform(GLProgram.UNIFORM_MODEL_MATRIX)) {
            currentProgram.getUniform(GLProgram.UNIFORM_MODEL_MATRIX).set(modelMatrixStack.curr());
            currentProgram.uploadUniforms(stateMgr);
        }
    }

    public static Matrix4fStack modelMatrixStack() {
        return modelMatrixStack;
    }

    public static void updateMatrices() {
        updateProjectionViewMatrix();
        updateModelMatrix();
    }

    public static void pushMatrices() {
        projectionMatrixStack.push();
        viewMatrixStack.push();
        modelMatrixStack.push();
    }

    public static void popMatrices() {
        projectionMatrixStack.pop();
        viewMatrixStack.pop();
        modelMatrixStack.pop();
    }

    public static MatricesScope matricesScope() {
        pushMatrices();
        return MatricesScope.INSTANCE;
    }

    public static final class MatricesScope implements AutoCloseable {
        public static final MatricesScope INSTANCE = new MatricesScope();

        private MatricesScope() {
        }

        @Override
        public void close() {
            popMatrices();
        }
    }
}
