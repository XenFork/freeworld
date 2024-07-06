/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.math;

import java.util.Arrays;
import java.util.function.UnaryOperator;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Matrix4fStack implements AutoCloseable {
    private final Matrix4f[] matrices;
    private int curr = -1;

    public Matrix4fStack(int stackSize) {
        this.matrices = new Matrix4f[stackSize];
        Arrays.fill(this.matrices, Matrix4f.IDENTITY);
    }

    public Matrix4fStack push() {
        if (curr + 1 >= matrices.length) {
            throw new IllegalStateException(STR."Stack exceeds size \{matrices.length}");
        }
        curr++;
        if (curr > 0) {
            matrices[curr] = matrices[curr - 1];
        } else if (curr == 0) {
            matrices[curr] = Matrix4f.IDENTITY;
        }
        return this;
    }

    public void pop() {
        if (curr - 1 < -1) {
            throw new IllegalStateException("Stack underflow");
        }
        curr--;
    }

    public Matrix4f curr() {
        return matrices[curr];
    }

    public void setCurr(Matrix4f m) {
        matrices[curr] = m;
    }

    public void withCurr(UnaryOperator<Matrix4f> function) {
        setCurr(function.apply(curr()));
    }

    @Override
    public void close() {
        pop();
    }
}
