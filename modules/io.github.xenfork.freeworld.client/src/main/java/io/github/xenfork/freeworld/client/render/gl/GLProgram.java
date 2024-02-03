/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.render.gl;

import com.google.gson.*;
import io.github.xenfork.freeworld.client.render.GameRenderer;
import io.github.xenfork.freeworld.core.Identifier;
import io.github.xenfork.freeworld.file.BuiltinFiles;
import io.github.xenfork.freeworld.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import overrungl.opengl.GL;
import overrungl.opengl.GL10C;
import overrungl.opengl.GL20C;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The OpenGL program.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class GLProgram implements AutoCloseable {
    public static final int INPUT_POSITION = 0;
    public static final int INPUT_COLOR = 1;
    private static final Logger logger = Logging.caller();
    private static final Gson GSON = new GsonBuilder()
        .disableJdkUnsafe()
        .create();
    private final int id;
    private final Identifier identifier;

    private GLProgram(int id, Identifier identifier) {
        this.id = id;
        this.identifier = identifier;
    }

    @Nullable
    public static GLProgram load(@NotNull Identifier identifier) {
        Objects.requireNonNull(identifier);

        final GL gl = GameRenderer.OpenGL.get();
        final int id = gl.createProgram();
        final boolean success = loadFromJson(id, identifier);
        if (success) {
            final GLProgram program = new GLProgram(id, identifier);
            logger.debug("Created {}", program);
            return program;
        }
        gl.deleteProgram(id);
        return null;
    }

    private static boolean loadFromJson(int id, Identifier identifier) {
        final String path = identifier.toResourcePath(Identifier.ROOT_ASSETS,
            Identifier.RES_SHADER,
            Identifier.EXT_JSON);
        final BufferedReader reader = BuiltinFiles.readTextAsReader(BuiltinFiles.load(path));
        if (reader == null) {
            logger.error("Failed to load GLProgram {} from file {}", identifier, path);
            return false;
        }

        final Identifier vshId;
        final Identifier fshId;
        final Map<String, Integer> inputMap;

        // JSON stuff
        try (reader) {
            final JsonElement jsonElement = JsonParser.parseReader(reader);
            if (!jsonElement.isJsonObject()) {
                malformedJson(identifier, path, "not an JSON object");
                return false;
            }
            final JsonObject jsonObject = jsonElement.getAsJsonObject();

            // shaders
            vshId = getShaderId(identifier, path, jsonObject, "vertex");
            if (vshId == null) return false;
            fshId = getShaderId(identifier, path, jsonObject, "fragment");
            if (fshId == null) return false;

            // input
            final JsonElement inputElement = jsonObject.get("input");
            if (!inputElement.isJsonObject()) {
                malformedJson(identifier, path, "input is not an JSON object");
                return false;
            }
            final JsonObject input = inputElement.getAsJsonObject();
            inputMap = HashMap.newHashMap(input.size());

            for (var entry : input.entrySet()) {
                final String name = entry.getKey();
                final JsonElement value = entry.getValue();
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                    malformedJson(identifier, path, STR."input.\{name} is not a number");
                    return false;
                }
                inputMap.put(name, value.getAsInt());
            }
        } catch (IOException e) {
            logger.error("Failed to load GLProgram {} from file {}", identifier, path, e);
            return false;
        }

        // OpenGL stuff
        final GL gl = GameRenderer.OpenGL.get();

        final String vshPath = vshId.toResourcePath(Identifier.ROOT_ASSETS, Identifier.RES_SHADER, null);
        final String vshSrc = BuiltinFiles.readText(BuiltinFiles.load(vshPath), vshPath);
        if (vshSrc == null) {
            return false;
        }
        final int vsh = compileShader(GL.VERTEX_SHADER, "vertex", vshSrc);
        if (vsh == -1) {
            return false;
        }

        final String fshPath = fshId.toResourcePath(Identifier.ROOT_ASSETS, Identifier.RES_SHADER, null);
        final String fshSrc = BuiltinFiles.readText(BuiltinFiles.load(fshPath), fshPath);
        if (fshSrc == null) {
            gl.deleteShader(vsh);
            return false;
        }
        final int fsh = compileShader(GL.FRAGMENT_SHADER, "fragment", fshSrc);
        if (fsh == -1) {
            gl.deleteShader(fsh);
            return false;
        }

        inputMap.forEach((name, index) -> gl.bindAttribLocation(id, index, name));
        gl.attachShader(id, vsh);
        gl.attachShader(id, fsh);
        gl.linkProgram(id);
        try {
            if (gl.getProgramiv(id, GL20C.LINK_STATUS) == GL10C.FALSE) {
                logger.error("Failed to link GLProgram {} ({}): {}", identifier, id, gl.getProgramInfoLog(id));
                return false;
            }
        } finally {
            gl.detachShader(id, vsh);
            gl.detachShader(id, fsh);
            gl.deleteShader(vsh);
            gl.deleteShader(fsh);
        }

        return true;
    }

    private static Identifier getShaderId(Identifier identifier, String path, JsonObject jsonObject, String name) {
        final JsonElement jsonElement = jsonObject.get(name);
        if (!jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isString()) {
            malformedJson(identifier, path, STR."\{name} is not a string");
            return null;
        }
        final String asString = jsonElement.getAsString();
        final Identifier id = Identifier.ofSafe(asString);
        if (id == null) {
            malformedJson(identifier, path, STR."\{name} shader is invalid: \{asString}");
            return null;
        }
        return id;
    }

    private static void malformedJson(Identifier identifier, String file, String msg) {
        logger.error("Failed to load GLProgram {}: Malformed JSON from file {}: {}", identifier, file, msg);
    }

    private static int compileShader(int type, String name, String src) {
        final GL gl = GameRenderer.OpenGL.get();
        final int shader = gl.createShader(type);
        gl.shaderSource(shader, src);
        gl.compileShader(shader);
        if (gl.getShaderiv(shader, GL20C.COMPILE_STATUS) == GL10C.FALSE) {
            logger.error("Failed to compile {} shader: {}", name, gl.getShaderInfoLog(shader));
            gl.deleteShader(shader);
            return -1;
        }
        return shader;
    }

    @Override
    public void close() {
        final GL gl = GameRenderer.OpenGL.get();
        gl.deleteProgram(id);
    }

    @Override
    public String toString() {
        return STR."GLProgram \{identifier()} (\{id()})";
    }

    public int id() {
        return id;
    }

    public Identifier identifier() {
        return identifier;
    }
}
