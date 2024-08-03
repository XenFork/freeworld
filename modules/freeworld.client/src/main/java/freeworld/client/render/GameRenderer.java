/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render;

import freeworld.client.FreeworldClient;
import freeworld.client.render.gl.*;
import freeworld.client.render.gui.GuiGraphics;
import freeworld.client.render.gui.HudRenderer;
import freeworld.client.render.model.block.BlockModel;
import freeworld.client.render.model.block.BlockModelFace;
import freeworld.client.render.model.block.BlockModelManager;
import freeworld.client.render.model.block.BlockModelPart;
import freeworld.client.render.screen.Screen;
import freeworld.client.render.text.TextRenderer;
import freeworld.client.render.text.Unifont;
import freeworld.client.render.texture.TextureAtlas;
import freeworld.client.render.texture.TextureManager;
import freeworld.client.render.vertex.BufferBuilder;
import freeworld.client.render.vertex.VertexLayout;
import freeworld.client.render.vertex.VertexLayouts;
import freeworld.client.render.world.WorldRenderer;
import freeworld.client.render.world.block.BlockRenderer;
import freeworld.client.render.world.entity.EntityRenderer;
import freeworld.client.render.world.entity.EntityRenderers;
import freeworld.math.Matrix4f;
import freeworld.math.Vector3i;
import freeworld.util.Direction;
import freeworld.util.Identifier;
import freeworld.util.Logging;
import freeworld.util.math.Lined;
import freeworld.world.World;
import freeworld.world.block.BlockHitResult;
import freeworld.world.entity.Entity;
import freeworld.world.entity.EntityType;
import org.slf4j.Logger;
import overrungl.opengl.GL;
import overrungl.opengl.GL10C;

import java.util.*;

/**
 * The game renderer.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class GameRenderer implements GLResource {
    private static final Logger logger = Logging.caller();
    private final FreeworldClient client;
    private static GLProgram positionColorProgram;
    private static GLProgram positionColorTexProgram;
    private static GLProgram renderTypeTextProgram;
    private BlockModelManager blockModelManager;
    private TextureManager textureManager;
    private GuiGraphics guiGraphics;
    private HudRenderer hudRenderer;
    private BlockRenderer blockRenderer;
    private WorldRenderer worldRenderer;
    private TextRenderer textRenderer;
    private Unifont unifont;
    private Map<EntityType<?>, EntityRenderer<?>> entityRendererMap;
    private BlockHitResult hitResult = new BlockHitResult(true, null, Vector3i.ZERO, Direction.SOUTH);
    private final List<GLVertexArrayObject> layoutVAOs = new ArrayList<>();

    public GameRenderer(FreeworldClient client) {
        this.client = client;
    }

    public void init(GLStateMgr gl) {
        logger.info("Initializing game renderer");

        initGLPrograms(gl);

        gl.clearColor(0.4f, 0.6f, 0.9f, 1.0f);

        blockModelManager = new BlockModelManager();
        blockModelManager.bootstrap();

        textureManager = new TextureManager();

        initBlockAtlas(gl);

        final TextureAtlas guiAtlas = TextureAtlas.load(gl, Set.of(
            HudRenderer.CROSSING_TEXTURE,
            HudRenderer.HOT_BAR_TEXTURE,
            HudRenderer.HOT_BAR_SELECTED_TEXTURE
        ), 0);
        textureManager.addTexture(TextureManager.GUI_ATLAS, guiAtlas);
        logAtlas(guiAtlas, TextureManager.GUI_ATLAS);

        blockRenderer = new BlockRenderer(this);
        worldRenderer = new WorldRenderer(this, client.world());

        guiGraphics = new GuiGraphics(this);
        hudRenderer = new HudRenderer(this);

        unifont = new Unifont();
        unifont.load(gl, Unifont.IDENTIFIER.withPathSuffix(".hex.gz"));
        textureManager.addTexture(Unifont.IDENTIFIER, unifont.texture());

        textRenderer = new TextRenderer();

        entityRendererMap = EntityRenderers.loadRenderers(client);
    }

    private void initBlockAtlas(GLStateMgr gl) {
        final var registry = blockModelManager.registry();

        // scan textures
        final Set<Identifier> set = new HashSet<>(registry.size());
        for (var e : registry) {
            final BlockModel model = e.getValue();
            for (BlockModelPart part : model.parts()) {
                for (BlockModelFace face : part.faces().values()) {
                    set.add(model.textureDefinitions().get(face.textureKey()));
                }
            }
        }

        final TextureAtlas blockAtlas = TextureAtlas.load(gl, set, 4);
        textureManager.addTexture(TextureManager.BLOCK_ATLAS, blockAtlas);
        logAtlas(blockAtlas, TextureManager.BLOCK_ATLAS);
    }

    private void logAtlas(TextureAtlas atlas, Identifier identifier) {
        logger.info("Created {}x{}x{} {}", atlas.width(), atlas.height(), atlas.mipmapLevel(), identifier);
    }

    private static void initGLPrograms(GLStateMgr gl) {
        positionColorProgram = initBuiltinProgram(gl, "init/position_color", VertexLayouts.POSITION_COLOR);
        positionColorTexProgram = initBuiltinProgram(gl, "init/position_color_tex", VertexLayouts.POSITION_COLOR_TEXTURE);
        renderTypeTextProgram = initBuiltinProgram(gl, "core/render_type_text", VertexLayouts.TEXT);
    }

    private static GLProgram initBuiltinProgram(GLStateMgr gl, String path, VertexLayout layout) {
        return new GLProgram(gl, Identifier.ofBuiltin(path), layout);
    }

    public void render(GLStateMgr gl, double partialTick) {
        gl.clear(GL10C.COLOR_BUFFER_BIT | GL10C.DEPTH_BUFFER_BIT);

        if (worldRenderer != null) {
            renderWorld(gl, partialTick);
        }

        gl.clear(GL10C.DEPTH_BUFFER_BIT);
        if (client.world() != null) {
            renderHud(gl, partialTick);
        }
        renderScreen(guiGraphics, gl, partialTick);
    }

    private void renderWorld(GLStateMgr gl, double partialTick) {
        gl.setDisableBlend();
        gl.setEnableCullFace();
        gl.setEnableDepthTest();
        gl.setDepthFunc(GL10C.LEQUAL);

        final Camera camera = client.camera();
        final Entity player = client.player();
        camera.moveToEntity(player, partialTick);
        RenderSystem.setProjectionViewMatrix(Matrix4f.setPerspective(
            (float) Math.toRadians(70.0),
            (float) client.framebufferWidth() / client.framebufferHeight(),
            0.01f,
            1000.0f
        ), camera.updateViewMatrix());
        RenderSystem.setModelMatrix(Matrix4f.identity());

        worldRenderer.compileChunks(player);

        hitResult = worldRenderer.selectBlock(player);

        if (!hitResult.missed()) {
            gl.setEnablePolygonOffsetFill();
            gl.setPolygonOffset(1.0f, 1.0f);
            gl.setLineWidth(2.0f);
        }
        RenderSystem.useProgram(positionColorTexProgram);
        RenderSystem.bindTexture2D(textureManager.getTexture(TextureManager.BLOCK_ATLAS));
        worldRenderer.renderChunks(gl, player);
        if (!hitResult.missed()) {
            gl.setDisablePolygonOffsetFill();
            gl.setLineWidth(1.0f);
        }

        if (!hitResult.missed()) {
            var lines = hitResult.blockType().outlineShape().toLines(Direction.LIST);
            Matrix4f mat = Matrix4f.translation(hitResult.position().toVector3f());
            RenderSystem.bindTexture2D(null);
            RenderSystem.useProgram(positionColorProgram);
            final Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.buffer();
            buffer.begin(GLDrawMode.LINES, VertexLayouts.POSITION_COLOR);
            for (Lined line : lines) {
                buffer.indices(0, 1);
                buffer.position(mat, line.from().toVector3f()).color(0, 0, 0).emit();
                buffer.position(mat, line.to().toVector3f()).color(0, 0, 0).emit();
            }
            tessellator.draw(gl);
        }

        renderWorldEntities(gl, partialTick);
    }

    private void renderWorldEntities(GLStateMgr gl, double partialTick) {
        RenderSystem.useProgram(positionColorProgram);
        World.forInChunkRange(client.player(), WorldRenderer.RENDER_RADIUS, (x, y, z) -> {
            for (Entity entity : client.world().getOrCreateChunk(x, y, z).entities()) {
                var renderer = getEntityRenderer(entity);
                if (renderer != null) {
                    renderer.render(gl,
                        partialTick,
                        Matrix4f.translation(entity.interpolatedPosition(partialTick).toVector3f())
                            .rotateY((float) Math.toRadians(entity.rotation().y())),
                        entity);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T extends Entity> EntityRenderer<T> getEntityRenderer(T entity) {
        return (EntityRenderer<T>) entityRendererMap.get(entity.type());
    }

    private void renderHud(GLStateMgr gl, double partialTick) {
        gl.setDisableCullFace();
        gl.setDisableDepthTest();
        gl.setEnableBlend();
        gl.setBlendFunc(GL10C.SRC_ALPHA, GL10C.ONE_MINUS_SRC_ALPHA);
        hudRenderer.update(client.scaledFramebufferWidth(), client.scaledFramebufferHeight());
        hudRenderer.render(guiGraphics, gl, partialTick);
    }

    private void renderScreen(GuiGraphics graphics, GLStateMgr gl, double partialTick) {
        final Screen screen = client.screen();
        if (screen != null) {
            gl.setDisableCullFace();
            gl.setDisableDepthTest();
            gl.setEnableBlend();
            gl.setBlendFunc(GL10C.SRC_ALPHA, GL10C.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setProjectionViewMatrix(Matrix4f.setOrtho(0.0f,
                    client.scaledFramebufferWidth(),
                    0.0f,
                    client.scaledFramebufferHeight(),
                    -300.0f,
                    300.0f),
                Matrix4f.identity());
            RenderSystem.setModelMatrix(Matrix4f.identity());
            screen.render(graphics, gl, partialTick);
        }
    }

    public void tick() {
        hudRenderer.tick();
    }

    @Override
    public void close(GLStateMgr gl) {
        logger.info("Closing game renderer");

        if (worldRenderer != null) worldRenderer.close(gl);
        if (textureManager != null) textureManager.close(gl);

        if (positionColorProgram != null) positionColorProgram.close(gl);
        if (positionColorTexProgram != null) positionColorTexProgram.close(gl);
        if (renderTypeTextProgram != null) renderTypeTextProgram.close(gl);

        for (GLVertexArrayObject vao : layoutVAOs) {
            vao.close(gl);
        }

        BufferRenderer.reset(gl);
    }

    public GLVertexArrayObject createVaoForLayout(GLStateMgr gl) {
        GLVertexArrayObject vertexArrayObject = new GLVertexArrayObject(gl, GL.DYNAMIC_DRAW);
        layoutVAOs.add(vertexArrayObject);
        return vertexArrayObject;
    }

    public FreeworldClient client() {
        return client;
    }

    public static GLProgram positionColorProgram() {
        return positionColorProgram;
    }

    public static GLProgram positionColorTexProgram() {
        return positionColorTexProgram;
    }

    public static GLProgram renderTypeTextProgram() {
        return renderTypeTextProgram;
    }

    public BlockModelManager blockModelManager() {
        return blockModelManager;
    }

    public TextureManager textureManager() {
        return textureManager;
    }

    public BlockRenderer blockRenderer() {
        return blockRenderer;
    }

    public TextRenderer textRenderer() {
        return textRenderer;
    }

    public Unifont unifont() {
        return unifont;
    }

    public BlockHitResult hitResult() {
        return hitResult;
    }
}
