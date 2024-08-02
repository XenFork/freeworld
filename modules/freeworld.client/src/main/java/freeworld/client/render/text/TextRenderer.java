/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.text;

import freeworld.client.render.vertex.VertexBuilder;
import freeworld.client.util.Color;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class TextRenderer {
    public void renderText(VertexBuilder builder, Font font, String text, int x, int y, Color color, boolean allowNewLine) {
        int count = text.codePointCount(0, text.length());
        for (int i = 0; i < count; i++) {
            int codePoint = text.codePointAt(i);
            if (codePoint == '\n' && allowNewLine) {
                x = 0;
                y -= font.lineHeight();
                continue;
            }
            int width = font.width(codePoint);
            int height = font.height(codePoint);
            float x0 = x;
            float y0 = y;
            float x1 = x0 + width;
            float y1 = y0 + height;
            int red = color.red();
            int green = color.green();
            int blue = color.blue();
            int alpha = color.alpha();
            float u0 = font.u0(codePoint);
            float v0 = font.v0(codePoint);
            float u1 = font.u1(codePoint);
            float v1 = font.v1(codePoint);
            builder.indices(0, 1, 2, 2, 3, 0);
            builder.position(x0, y1, 0.0f).color(red, green, blue, alpha).texCoord(u0, v0).emit();
            builder.position(x0, y0, 0.0f).color(red, green, blue, alpha).texCoord(u0, v1).emit();
            builder.position(x1, y0, 0.0f).color(red, green, blue, alpha).texCoord(u1, v1).emit();
            builder.position(x1, y1, 0.0f).color(red, green, blue, alpha).texCoord(u1, v0).emit();
            x += width;
        }
    }
}
