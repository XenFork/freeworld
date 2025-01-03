/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2025  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.gl;

import overrungl.opengl.GL;
import overrungl.opengl.GLLoadFunc;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class GLStateMgr extends GL {
    private int arrayBufferBinding = 0;
    private boolean blend = false;
    private int blendSrcRGB = GL_ONE;
    private int blendSrcAlpha = GL_ONE;
    private int blendDstRGB = GL_ZERO;
    private int blendDstAlpha = GL_ZERO;
    private boolean cullFace = false;
    private int currentProgram = 0;
    private int depthFunc = GL_LESS;
    private boolean depthTest = false;
    private float lineWidth = 1.0f;
    private boolean polygonOffsetFill = false;
    private float polygonOffsetFactor = 0.0f;
    private float polygonOffsetUnits = 0.0f;
    private int textureBinding2D = 0;
    private int vertexArrayBinding = 0;

    public GLStateMgr(GLLoadFunc function) {
        super(function);
    }

    public void setArrayBufferBinding(int arrayBufferBinding) {
        if (this.arrayBufferBinding != arrayBufferBinding) {
            this.arrayBufferBinding = arrayBufferBinding;
            BindBuffer(GL_ARRAY_BUFFER, arrayBufferBinding);
        }
    }

    public int getArrayBufferBinding() {
        return arrayBufferBinding;
    }

    public void setEnableBlend() {
        if (!this.blend) {
            this.blend = true;
            Enable(GL_BLEND);
        }
    }

    public void setDisableBlend() {
        if (this.blend) {
            this.blend = false;
            Disable(GL_BLEND);
        }
    }

    public boolean getBlend() {
        return blend;
    }

    public void setBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        if (this.blendSrcRGB != srcRGB ||
            this.blendDstRGB != dstRGB ||
            this.blendSrcAlpha != srcAlpha ||
            this.blendDstAlpha != dstAlpha) {
            this.blendSrcRGB = srcRGB;
            this.blendDstRGB = dstRGB;
            this.blendSrcAlpha = srcAlpha;
            this.blendDstAlpha = dstAlpha;
            BlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
        }
    }

    public void setBlendFunc(int sfactor, int dfactor) {
        setBlendFuncSeparate(sfactor, dfactor, sfactor, dfactor);
    }

    public int getBlendSrcRGB() {
        return blendSrcRGB;
    }

    public int getBlendSrcAlpha() {
        return blendSrcAlpha;
    }

    public int getBlendDstRGB() {
        return blendDstRGB;
    }

    public int getBlendDstAlpha() {
        return blendDstAlpha;
    }

    public void setEnableCullFace() {
        if (!this.cullFace) {
            this.cullFace = true;
            Enable(GL_CULL_FACE);
        }
    }

    public void setDisableCullFace() {
        if (this.cullFace) {
            this.cullFace = false;
            Disable(GL_CULL_FACE);
        }
    }

    public boolean getCullFace() {
        return cullFace;
    }

    public void setCurrentProgram(int currentProgram) {
        if (this.currentProgram != currentProgram) {
            this.currentProgram = currentProgram;
            UseProgram(currentProgram);
        }
    }

    public int getCurrentProgram() {
        return currentProgram;
    }

    public void setDepthFunc(int depthFunc) {
        if (this.depthFunc != depthFunc) {
            this.depthFunc = depthFunc;
            DepthFunc(depthFunc);
        }
    }

    public int getDepthFunc() {
        return depthFunc;
    }

    public void setEnableDepthTest() {
        if (!this.depthTest) {
            this.depthTest = true;
            Enable(GL_DEPTH_TEST);
        }
    }

    public void setDisableDepthTest() {
        if (this.depthTest) {
            this.depthTest = false;
            Disable(GL_DEPTH_TEST);
        }
    }

    public boolean getDepthTest() {
        return depthTest;
    }

    public void setLineWidth(float width) {
        if (this.lineWidth != width) {
            this.lineWidth = width;
            LineWidth(width);
        }
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setEnablePolygonOffsetFill() {
        if (!this.polygonOffsetFill) {
            this.polygonOffsetFill = true;
            Enable(GL_POLYGON_OFFSET_FILL);
        }
    }

    public void setDisablePolygonOffsetFill() {
        if (this.polygonOffsetFill) {
            this.polygonOffsetFill = false;
            Disable(GL_POLYGON_OFFSET_FILL);
        }
    }

    public boolean getPolygonOffsetFill() {
        return polygonOffsetFill;
    }

    public void setPolygonOffset(float factor, float units) {
        if (this.polygonOffsetFactor != factor ||
            this.polygonOffsetUnits != units) {
            this.polygonOffsetFactor = factor;
            this.polygonOffsetUnits = units;
            PolygonOffset(factor, units);
        }
    }

    public float getPolygonOffsetFactor() {
        return polygonOffsetFactor;
    }

    public float getPolygonOffsetUnits() {
        return polygonOffsetUnits;
    }

    public void setTextureBinding2D(int textureBinding2D) {
        if (this.textureBinding2D != textureBinding2D) {
            this.textureBinding2D = textureBinding2D;
            BindTexture(GL_TEXTURE_2D, textureBinding2D);
        }
    }

    public int getTextureBinding2D() {
        return textureBinding2D;
    }

    public void setVertexArrayBinding(int vertexArrayBinding) {
        if (this.vertexArrayBinding != vertexArrayBinding) {
            this.vertexArrayBinding = vertexArrayBinding;
            BindVertexArray(vertexArrayBinding);
        }
    }

    public int getVertexArrayBinding() {
        return vertexArrayBinding;
    }
}
