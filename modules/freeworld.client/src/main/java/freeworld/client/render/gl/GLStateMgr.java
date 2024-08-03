/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.gl;

import freeworld.client.FreeworldClient;
import overrun.marshal.gen.Skip;
import overrungl.opengl.GL;
import overrungl.opengl.GLFlags;

/**
 * @author squid233
 * @since 0.1.0
 */
public abstract class GLStateMgr implements GL {
    private int arrayBufferBinding = 0;
    private boolean blend = false;
    private int blendSrcRGB = ONE;
    private int blendSrcAlpha = ONE;
    private int blendDstRGB = ZERO;
    private int blendDstAlpha = ZERO;
    private boolean cullFace = false;
    private int currentProgram = 0;
    private int depthFunc = LESS;
    private boolean depthTest = false;
    private float lineWidth = 1.0f;
    private boolean polygonOffsetFill = false;
    private float polygonOffsetFactor = 0.0f;
    private float polygonOffsetUnits = 0.0f;
    private int textureBinding2D = 0;
    private int vertexArrayBinding = 0;

    @Skip
    public void setArrayBufferBinding(int arrayBufferBinding) {
        if (this.arrayBufferBinding != arrayBufferBinding) {
            this.arrayBufferBinding = arrayBufferBinding;
            bindBuffer(ARRAY_BUFFER, arrayBufferBinding);
        }
    }

    @Skip
    public int getArrayBufferBinding() {
        return arrayBufferBinding;
    }

    @Skip
    public void setEnableBlend() {
        if (!this.blend) {
            this.blend = true;
            enable(BLEND);
        }
    }

    @Skip
    public void setDisableBlend() {
        if (this.blend) {
            this.blend = false;
            disable(BLEND);
        }
    }

    @Skip
    public boolean getBlend() {
        return blend;
    }

    @Skip
    public void setBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        if (this.blendSrcRGB != srcRGB ||
            this.blendDstRGB != dstRGB ||
            this.blendSrcAlpha != srcAlpha ||
            this.blendDstAlpha != dstAlpha) {
            this.blendSrcRGB = srcRGB;
            this.blendDstRGB = dstRGB;
            this.blendSrcAlpha = srcAlpha;
            this.blendDstAlpha = dstAlpha;
            blendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
        }
    }

    @Skip
    public void setBlendFunc(int sfactor, int dfactor) {
        setBlendFuncSeparate(sfactor, dfactor, sfactor, dfactor);
    }

    @Skip
    public int getBlendSrcRGB() {
        return blendSrcRGB;
    }

    @Skip
    public int getBlendSrcAlpha() {
        return blendSrcAlpha;
    }

    @Skip
    public int getBlendDstRGB() {
        return blendDstRGB;
    }

    @Skip
    public int getBlendDstAlpha() {
        return blendDstAlpha;
    }

    @Skip
    public void setEnableCullFace() {
        if (!this.cullFace) {
            this.cullFace = true;
            enable(CULL_FACE);
        }
    }

    @Skip
    public void setDisableCullFace() {
        if (this.cullFace) {
            this.cullFace = false;
            disable(CULL_FACE);
        }
    }

    @Skip
    public boolean getCullFace() {
        return cullFace;
    }

    @Skip
    public void setCurrentProgram(int currentProgram) {
        if (this.currentProgram != currentProgram) {
            this.currentProgram = currentProgram;
            useProgram(currentProgram);
        }
    }

    @Skip
    public int getCurrentProgram() {
        return currentProgram;
    }

    @Skip
    public void setDepthFunc(int depthFunc) {
        if (this.depthFunc != depthFunc) {
            this.depthFunc = depthFunc;
            depthFunc(depthFunc);
        }
    }

    @Skip
    public int getDepthFunc() {
        return depthFunc;
    }

    @Skip
    public void setEnableDepthTest() {
        if (!this.depthTest) {
            this.depthTest = true;
            enable(DEPTH_TEST);
        }
    }

    @Skip
    public void setDisableDepthTest() {
        if (this.depthTest) {
            this.depthTest = false;
            disable(DEPTH_TEST);
        }
    }

    @Skip
    public boolean getDepthTest() {
        return depthTest;
    }

    @Skip
    public void setLineWidth(float width) {
        if (this.lineWidth != width) {
            this.lineWidth = width;
            lineWidth(width);
        }
    }

    @Skip
    public float getLineWidth() {
        return lineWidth;
    }

    @Skip
    public void setEnablePolygonOffsetFill() {
        if (!this.polygonOffsetFill) {
            this.polygonOffsetFill = true;
            enable(POLYGON_OFFSET_FILL);
        }
    }

    @Skip
    public void setDisablePolygonOffsetFill() {
        if (this.polygonOffsetFill) {
            this.polygonOffsetFill = false;
            disable(POLYGON_OFFSET_FILL);
        }
    }

    @Skip
    public boolean getPolygonOffsetFill() {
        return polygonOffsetFill;
    }

    @Skip
    public void setPolygonOffset(float factor, float units) {
        if (this.polygonOffsetFactor != factor ||
            this.polygonOffsetUnits != units) {
            this.polygonOffsetFactor = factor;
            this.polygonOffsetUnits = units;
            polygonOffset(factor, units);
        }
    }

    @Skip
    public float getPolygonOffsetFactor() {
        return polygonOffsetFactor;
    }

    @Skip
    public float getPolygonOffsetUnits() {
        return polygonOffsetUnits;
    }

    @Skip
    public void setTextureBinding2D(int textureBinding2D) {
        if (this.textureBinding2D != textureBinding2D) {
            this.textureBinding2D = textureBinding2D;
            bindTexture(TEXTURE_2D, textureBinding2D);
        }
    }

    @Skip
    public int getTextureBinding2D() {
        return textureBinding2D;
    }

    @Skip
    public void setVertexArrayBinding(int vertexArrayBinding) {
        if (this.vertexArrayBinding != vertexArrayBinding) {
            this.vertexArrayBinding = vertexArrayBinding;
            bindVertexArray(vertexArrayBinding);
        }
    }

    @Skip
    public int getVertexArrayBinding() {
        return vertexArrayBinding;
    }

    @Skip
    public GLFlags getFlags() {
        return FreeworldClient.getInstance().glFlags();
    }
}
