/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.util.math;

import freeworld.math.SimplexNoise;

/**
 * Original code from <a href="https://cmaher.github.io/posts/working-with-simplex-noise/">Christian Maher</a>
 *
 * @author squid233
 * @since 0.1.0
 */
public final class SimplexNoiseUtil {
    public static float sumOctave(int numIterations, float x, float y, float z, float w, float persistence, float scale, float low, float high) {
        float maxAmp = 0;
        float amp = 1;
        float freq = scale;
        float noise = 0;

        // add successively smaller, higher-frequency terms
        for (int i = 0; i < numIterations; ++i) {
            noise += SimplexNoise.noise(x * freq, y * freq, z * freq, w * freq) * amp;
            maxAmp += amp;
            amp *= persistence;
            freq *= 2;
        }

        // take the average value of the iterations
        noise /= maxAmp;

        // normalize the result
        noise = noise * (high - low) * 0.5f + (high + low) * 0.5f;

        return noise;
    }
}
