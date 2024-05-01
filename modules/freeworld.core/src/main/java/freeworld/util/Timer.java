/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.util;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Timer {
    public static final double DEFAULT_TPS = 20.0;
    private static final int MAX_TPS = 100;
    private final double tickPerSecond;
    private long currentTime = System.nanoTime();
    private double partialTick = 0.0;
    private double accumTick = 0.0;
    private int tickCount = 0;

    public Timer(double tickPerSecond) {
        this.tickPerSecond = tickPerSecond;
    }

    public void update() {
        final long previousTime = currentTime;
        currentTime = System.nanoTime();
        long elapsedTime = currentTime - previousTime;

        elapsedTime = Math.clamp(elapsedTime, 0, 1_000_000_000L);
        accumTick += elapsedTime * 1.0e-9 * tickPerSecond;
        tickCount = Math.clamp((int) accumTick, 0, MAX_TPS);
        accumTick -= tickCount;
        partialTick = accumTick;
    }

    public double partialTick() {
        return partialTick;
    }

    public int tickCount() {
        return tickCount;
    }
}
