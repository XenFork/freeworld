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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import freeworld.client.render.vertex.VertexLayout;
import freeworld.util.Identifier;
import freeworld.util.Logging;
import freeworld.util.file.BuiltinFiles;
import org.jetbrains.annotations.NotNull;
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
    private static final Logger logger = Logging.caller();
    private final int id;
    private final Identifier identifier;
    private final VertexLayout vertexLayout;
    private final Map<String, GLUniform> uniformMap;
    private final Arena uniformArena;
    public final GLUniform projectionViewMatrixUniform;
    public final GLUniform modelMatrixUniform;
    public final GLUniform colorModulatorUniform;

    public GLProgram(GLStateMgr gl, @NotNull Identifier identifier, @NotNull VertexLayout vertexLayout) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(vertexLayout);
        this.identifier = identifier;
        this.vertexLayout = vertexLayout;

        final String path = "assets/" + identifier.withPath(s -> "shader/" + s + ".json").toResourcePath();
        final BufferedReader reader = BuiltinFiles.readTextAsReader(BuiltinFiles.load(path));
        if (reader == null) {
            throw exception(identifier, path);
        }

        final Identifier vshId;
        final Identifier fshId;
        final boolean hasUniform;
        final Map<String, GLUniformType> uniformTypeMap;
        final Map<String, JsonArray> uniformValueMap;

        // JSON stuff
        final JsonObject jsonObject;
        try (reader) {
            jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            throw exception(identifier, path, e);
        }

        // shaders
        vshId = getShaderId(identifier, path, jsonObject, "vertex");
        fshId = getShaderId(identifier, path, jsonObject, "fragment");

        // uniform
        if (jsonObject.has("uniform")) {
            final JsonObject uniform = jsonObject.get("uniform").getAsJsonObject();
            uniformTypeMap = HashMap.newHashMap(uniform.size());
            uniformValueMap = HashMap.newHashMap(uniform.size());
            for (var entry : uniform.entrySet()) {
                final String name = entry.getKey();
                final JsonObject valueObject = entry.getValue().getAsJsonObject();
                final String type = valueObject.get("type").getAsString();
                final GLUniformType uniformType = GLUniformType.fromString(type);
                if (uniformType == null) {
                    throw malformedJson(identifier, path, "uniform." + name + ".type is an invalid type: " + type);
                }
                uniformTypeMap.put(name, uniformType);
                if (valueObject.has("value")) {
                    uniformValueMap.put(name, valueObject.get("value").getAsJsonArray());
                }
            }
            hasUniform = true;
        } else {
            hasUniform = false;
            uniformTypeMap = Map.of();
            uniformValueMap = Map.of();
        }

        // OpenGL stuff

        final String vshPath = "assets/" + vshId.withPathPrefix("shader/").toResourcePath();
        final String vshSrc = BuiltinFiles.readText(BuiltinFiles.load(vshPath), vshPath);
        if (vshSrc == null) {
            throw exception(identifier, "failed to load vertex shader");
        }
        final int vsh = compileShader(gl, GL.VERTEX_SHADER, "vertex", vshSrc);
        if (vsh == -1) {
            throw exception(identifier, "failed to compile vertex shader");
        }

        final String fshPath = "assets/" + fshId.withPathPrefix("shader/").toResourcePath();
        final String fshSrc = BuiltinFiles.readText(BuiltinFiles.load(fshPath), fshPath);
        if (fshSrc == null) {
            gl.deleteShader(vsh);
            throw exception(identifier, "failed to load fragment shader");
        }
        final int fsh = compileShader(gl, GL.FRAGMENT_SHADER, "fragment", fshSrc);
        if (fsh == -1) {
            gl.deleteShader(vsh);
            gl.deleteShader(fsh);
            throw exception(identifier, "failed to compile fragment shader");
        }

        this.id = gl.createProgram();
        vertexLayout.bindLocations(gl, id);
        gl.attachShader(id, vsh);
        gl.attachShader(id, fsh);
        gl.linkProgram(id);
        try {
            if (gl.getProgramiv(id, GL20C.LINK_STATUS) == GL10C.FALSE) {
                String log = gl.getProgramInfoLog(id);
                gl.deleteProgram(id);
                throw exception(identifier, "failed to link GLProgram " + identifier + ": " + log);
            }
        } finally {
            gl.detachShader(id, vsh);
            gl.detachShader(id, fsh);
            gl.deleteShader(vsh);
            gl.deleteShader(fsh);
        }

        this.uniformMap = hasUniform ? HashMap.newHashMap(uniformTypeMap.size()) : Map.of();
        this.uniformArena = hasUniform ? Arena.ofConfined() : null;

        if (hasUniform) {
            try {
                for (var entry : uniformTypeMap.entrySet()) {
                    final String name = entry.getKey();
                    final int location = gl.getUniformLocation(id, name);
                    if (location == -1) {
                        logger.warn("Unknown uniform {} in {}; ignoring.", name, this);
                        continue;
                    }
                    final GLUniformType type = entry.getValue();
                    final GLUniform uniform = new GLUniform(this, type, location, uniformArena);
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

        projectionViewMatrixUniform = getUniform("ProjectionViewMatrix");
        modelMatrixUniform = getUniform("ModelMatrix");
        colorModulatorUniform = getUniform("ColorModulator");
    }

    private static Identifier getShaderId(Identifier identifier, String path, JsonObject jsonObject, String name) {
        final String asString = jsonObject.get(name).getAsString();
        final Identifier id = Identifier.of(asString);
        if (id == null) {
            throw malformedJson(identifier, path, name + " shader is invalid: " + asString);
        }
        return id;
    }

    private static IllegalStateException malformedJson(Identifier identifier, String file, String msg) {
        return exception(identifier, "malformed JSON from file " + file + ": " + msg);
    }

    private static IllegalStateException exception(Identifier identifier, String msg) {
        return new IllegalStateException("Failed to load GLProgram " + identifier + ": " + msg);
    }

    private static IllegalStateException exception(Identifier identifier, String msg, Throwable cause) {
        return new IllegalStateException("Failed to load GLProgram " + identifier + ": " + msg, cause);
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

    public void bind(GLStateMgr gl) {
        gl.setCurrentProgram(id());

        for (GLUniform uniform : uniformMap.values()) {
            uniform.specify(gl);
        }
    }

    public void unbind(GLStateMgr gl) {
        gl.setCurrentProgram(0);
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
        return "GLProgram " + identifier + " (" + id + ")";
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
