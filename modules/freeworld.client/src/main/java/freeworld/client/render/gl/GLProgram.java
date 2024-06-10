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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import freeworld.client.render.model.VertexLayout;
import freeworld.core.Identifier;
import freeworld.file.BuiltinFiles;
import freeworld.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import overrungl.opengl.GL;
import overrungl.opengl.GL10C;
import overrungl.opengl.GL20C;

import java.io.BufferedReader;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The OpenGL program.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class GLProgram implements GLResource {
    public static final String UNIFORM_PROJECTION_VIEW_MATRIX = "ProjectionViewMatrix";
    public static final String UNIFORM_MODEL_MATRIX = "ModelMatrix";
    public static final String UNIFORM_COLOR_MODULATOR = "ColorModulator";
    private static final Logger logger = Logging.caller();
    private final int id;
    private final Identifier identifier;
    private final VertexLayout vertexLayout;
    private final Map<String, GLUniform> uniformMap;
    private final Arena uniformArena;

    private GLProgram(int id, Identifier identifier, VertexLayout vertexLayout, Map<String, GLUniform> uniformMap, Arena uniformArena) {
        this.id = id;
        this.identifier = identifier;
        this.vertexLayout = vertexLayout;
        this.uniformMap = uniformMap;
        this.uniformArena = uniformArena;
    }

    @Nullable
    public static GLProgram load(GLStateMgr gl, @NotNull Identifier identifier, @NotNull VertexLayout vertexLayout) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(vertexLayout);

        final GLProgram program = loadFromJson(gl, identifier, vertexLayout);
        if (program != null) {
            logger.debug("Created {}", program);
            return program;
        }
        return null;
    }

    private static GLProgram loadFromJson(GLStateMgr gl, Identifier identifier, VertexLayout vertexLayout) {
        final String path = identifier.toResourcePath(Identifier.ROOT_ASSETS,
            Identifier.RES_SHADER,
            Identifier.EXT_JSON);
        final BufferedReader reader = BuiltinFiles.readTextAsReader(BuiltinFiles.load(path));
        if (reader == null) {
            logger.error("Failed to load GLProgram {} from file {}", identifier, path);
            return null;
        }

        final Identifier vshId;
        final Identifier fshId;
        final boolean hasUniform;
        final Map<String, GLUniformType> uniformTypeMap;
        final Map<String, JsonArray> uniformValueMap;

        // JSON stuff
        try (reader) {
            final JsonElement jsonElement = JsonParser.parseReader(reader);
            if (!jsonElement.isJsonObject()) {
                malformedJson(identifier, path, "not a JSON object");
                return null;
            }
            final JsonObject jsonObject = jsonElement.getAsJsonObject();

            // shaders
            vshId = getShaderId(identifier, path, jsonObject, "vertex");
            if (vshId == null) return null;
            fshId = getShaderId(identifier, path, jsonObject, "fragment");
            if (fshId == null) return null;

            // uniform
            if (jsonObject.has("uniform")) {
                final JsonElement uniformElement = jsonObject.get("uniform");
                if (!uniformElement.isJsonObject()) {
                    malformedJson(identifier, path, "uniform is not a JSON object");
                    return null;
                }
                final JsonObject uniform = uniformElement.getAsJsonObject();
                uniformTypeMap = HashMap.newHashMap(uniform.size());
                uniformValueMap = HashMap.newHashMap(uniform.size());
                for (var entry : uniform.entrySet()) {
                    final String name = entry.getKey();
                    final JsonElement valueElement = entry.getValue();
                    if (!valueElement.isJsonObject()) {
                        malformedJson(identifier, path, STR."uniform.\{name} is not a JSON object");
                        return null;
                    }
                    final JsonObject valueObject = valueElement.getAsJsonObject();
                    final JsonElement typeElement = valueObject.get("type");
                    if (!typeElement.isJsonPrimitive() || !typeElement.getAsJsonPrimitive().isString()) {
                        malformedJson(identifier, path, STR."uniform.\{name}.type is not a string");
                        return null;
                    }
                    final String type = typeElement.getAsString();
                    final GLUniformType uniformType = GLUniformType.fromString(type);
                    if (uniformType == null) {
                        malformedJson(identifier, path, STR."uniform.\{name}.type is an invalid type: \{type}");
                        return null;
                    }
                    uniformTypeMap.put(name, uniformType);
                    if (valueObject.has("value")) {
                        final JsonElement uniformValueElement = valueObject.get("value");
                        if (!uniformValueElement.isJsonArray()) {
                            malformedJson(identifier, path, STR."uniform.\{name}.value is not an array");
                            return null;
                        }
                        final JsonArray valueArray = uniformValueElement.getAsJsonArray();
                        uniformValueMap.put(name, valueArray);
                    }
                }
                hasUniform = true;
            } else {
                hasUniform = false;
                uniformTypeMap = Map.of();
                uniformValueMap = Map.of();
            }
        } catch (Exception e) {
            logger.error("Failed to load GLProgram {} from file {}", identifier, path, e);
            return null;
        }

        // OpenGL stuff

        final String vshPath = vshId.toResourcePath(Identifier.ROOT_ASSETS, Identifier.RES_SHADER, null);
        final String vshSrc = BuiltinFiles.readText(BuiltinFiles.load(vshPath), vshPath);
        if (vshSrc == null) {
            return null;
        }
        final int vsh = compileShader(gl, GL.VERTEX_SHADER, "vertex", vshSrc);
        if (vsh == -1) {
            return null;
        }

        final String fshPath = fshId.toResourcePath(Identifier.ROOT_ASSETS, Identifier.RES_SHADER, null);
        final String fshSrc = BuiltinFiles.readText(BuiltinFiles.load(fshPath), fshPath);
        if (fshSrc == null) {
            gl.deleteShader(vsh);
            return null;
        }
        final int fsh = compileShader(gl, GL.FRAGMENT_SHADER, "fragment", fshSrc);
        if (fsh == -1) {
            gl.deleteShader(fsh);
            return null;
        }

        final int id = gl.createProgram();
        vertexLayout.bindLocations(gl, id);
        gl.attachShader(id, vsh);
        gl.attachShader(id, fsh);
        gl.linkProgram(id);
        try {
            if (gl.getProgramiv(id, GL20C.LINK_STATUS) == GL10C.FALSE) {
                logger.error("Failed to link GLProgram {} ({}): {}", identifier, id, gl.getProgramInfoLog(id));
                gl.deleteProgram(id);
                return null;
            }
        } finally {
            gl.detachShader(id, vsh);
            gl.detachShader(id, fsh);
            gl.deleteShader(vsh);
            gl.deleteShader(fsh);
        }

        final Map<String, GLUniform> uniformMap = hasUniform ? HashMap.newHashMap(uniformTypeMap.size()) : Map.of();
        final Arena uniformArena = hasUniform ? Arena.ofConfined() : null;

        final GLProgram program = new GLProgram(id, identifier, vertexLayout, uniformMap, uniformArena);

        if (hasUniform) {
            try {
                for (var entry : uniformTypeMap.entrySet()) {
                    final String name = entry.getKey();
                    final int location = gl.getUniformLocation(id, name);
                    if (location == -1) {
                        logger.warn("Unknown uniform {} in {}; ignoring.", name, program);
                        continue;
                    }
                    final GLUniformType type = entry.getValue();
                    final GLUniform uniform = new GLUniform(id, type, location, uniformArena);
                    uniformMap.put(name, uniform);

                    final JsonArray array = uniformValueMap.get(name);
                    if (array != null) {
                        final MemorySegment value = uniform.value;
                        switch (type) {
                            case INT -> value.set(ValueLayout.JAVA_INT, 0L, array.get(0).getAsInt());
                            case VEC4 -> {
                                for (int i = 0; i < 4; i++) {
                                    value.setAtIndex(ValueLayout.JAVA_FLOAT, i, array.get(i).getAsFloat());
                                }
                            }
                            case MAT4 -> {
                                for (int i = 0; i < 16; i++) {
                                    value.setAtIndex(ValueLayout.JAVA_FLOAT, i, array.get(i).getAsFloat());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                gl.deleteProgram(id);
                if (uniformArena != null) {
                    uniformArena.close();
                }
                throw new RuntimeException(e);
            }
        }

        return program;
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

    private static int compileShader(GLStateMgr gl, int type, String name, String src) {
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

    public void use(GLStateMgr gl) {
        gl.setCurrentProgram(id());
    }

    public void uploadUniforms(GLStateMgr gl) {
        for (GLUniform uniform : uniformMap.values()) {
            uniform.upload(gl);
        }
    }

    public GLUniform getUniform(String name) {
        return uniformMap.get(name);
    }

    public boolean hasUniform(String name) {
        return uniformMap.containsKey(name);
    }

    @Override
    public void close(GLStateMgr gl) {
        gl.deleteProgram(id);
        if (uniformArena != null) {
            uniformArena.close();
        }
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

    public VertexLayout vertexLayout() {
        return vertexLayout;
    }
}
