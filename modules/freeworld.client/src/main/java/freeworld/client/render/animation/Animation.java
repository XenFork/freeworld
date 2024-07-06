/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.animation;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Animation<T> {
    private final Generator<T> generator;
    private int frameTick = 0;
    private T previous;
    private T current;
    private T start;
    private T end;
    private int ticks = 1;

    public Animation(T initialValue, Generator<T> generator) {
        this.generator = generator;
        this.previous = initialValue;
        this.current = initialValue;
        this.start = initialValue;
        this.end = initialValue;
    }

    @FunctionalInterface
    public interface Generator<T> {
        T generate(T start, T end, double progress);
    }

    public void reset(T end, int ticks) {
        this.frameTick = 0;
        this.start = current;
        this.end = end;
        this.ticks = ticks;
    }

    public void tick() {
        previous = current;
        current = generator.generate(start, end, (double) frameTick / ticks);
        if (frameTick < ticks) {
            frameTick++;
        }
    }

    public T previous() {
        return previous;
    }

    public T current() {
        return current;
    }
}
