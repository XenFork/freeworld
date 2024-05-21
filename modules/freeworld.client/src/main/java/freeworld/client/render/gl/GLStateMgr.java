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

import freeworld.client.Freeworld;
import overrun.marshal.DirectAccess;
import overrun.marshal.gen.Skip;
import overrungl.opengl.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public abstract class GLStateMgr implements
    GL10C, GL11C, GL14C, GL15C,
    GL20C,
    GL30C,
    GL41C,
    DirectAccess {
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
    public int arrayBufferBinding() {
        return arrayBufferBinding;
    }

    @Skip
    public void enableBlend() {
        if (!this.blend) {
            this.blend = true;
            enable(BLEND);
        }
    }

    @Skip
    public void disableBlend() {
        if (this.blend) {
            this.blend = false;
            disable(BLEND);
        }
    }

    @Skip
    public boolean blend() {
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
    public int blendSrcRGB() {
        return blendSrcRGB;
    }

    @Skip
    public int blendSrcAlpha() {
        return blendSrcAlpha;
    }

    @Skip
    public int blendDstRGB() {
        return blendDstRGB;
    }

    @Skip
    public int blendDstAlpha() {
        return blendDstAlpha;
    }

    @Skip
    public void enableCullFace() {
        if (!this.cullFace) {
            this.cullFace = true;
            enable(CULL_FACE);
        }
    }

    @Skip
    public void disableCullFace() {
        if (this.cullFace) {
            this.cullFace = false;
            disable(CULL_FACE);
        }
    }

    @Skip
    public boolean cullFace() {
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
    public int currentProgram() {
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
    public int depthFunc() {
        return depthFunc;
    }

    @Skip
    public void enableDepthTest() {
        if (!this.depthTest) {
            this.depthTest = true;
            enable(DEPTH_TEST);
        }
    }

    @Skip
    public void disableDepthTest() {
        if (this.depthTest) {
            this.depthTest = false;
            disable(DEPTH_TEST);
        }
    }

    @Skip
    public boolean depthTest() {
        return depthTest;
    }

    @Skip
    public void setTextureBinding2D(int textureBinding2D) {
        if (this.textureBinding2D != textureBinding2D) {
            this.textureBinding2D = textureBinding2D;
            bindTexture(TEXTURE_2D, textureBinding2D);
        }
    }

    @Skip
    public int textureBinding2D() {
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
    public int vertexArrayBinding() {
        return vertexArrayBinding;
    }

    @Skip
    public GLFlags flags() {
        return Freeworld.getInstance().glFlags();
    }
}
