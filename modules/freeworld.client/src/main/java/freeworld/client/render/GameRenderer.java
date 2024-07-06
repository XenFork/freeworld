/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render;

import freeworld.client.Freeworld;
import freeworld.client.render.gl.GLDrawMode;
import freeworld.client.render.gl.GLProgram;
import freeworld.client.render.gl.GLResource;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.gui.GuiGraphics;
import freeworld.client.render.gui.HudRenderer;
import freeworld.client.render.model.block.BlockModel;
import freeworld.client.render.model.block.BlockModelFace;
import freeworld.client.render.model.block.BlockModelPart;
import freeworld.client.render.model.vertex.VertexLayout;
import freeworld.client.render.model.vertex.VertexLayouts;
import freeworld.client.render.screen.Screen;
import freeworld.client.render.texture.TextureAtlas;
import freeworld.client.render.texture.TextureManager;
import freeworld.client.render.world.BlockRenderer;
import freeworld.client.render.world.HitResult;
import freeworld.client.render.world.WorldRenderer;
import freeworld.client.world.chunk.ClientChunk;
import freeworld.core.Identifier;
import freeworld.core.ModelResourcePath;
import freeworld.core.math.AABBox;
import freeworld.math.Matrix4f;
import freeworld.util.Direction;
import freeworld.util.Logging;
import freeworld.world.entity.Entity;
import org.slf4j.Logger;
import overrungl.opengl.GL10C;

import java.util.ArrayList;
import java.util.List;

/**
 * The game renderer.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class GameRenderer implements GLResource {
    private static final Logger logger = Logging.caller();
    private final Freeworld client;
    private GLProgram positionColorProgram;
    private GLProgram positionColorTexProgram;
    private TextureManager textureManager;
    private GuiGraphics guiGraphics;
    private HudRenderer hudRenderer;
    private BlockRenderer blockRenderer;
    private WorldRenderer worldRenderer;
    private HitResult hitResult = new HitResult(true, null, 0, 0, 0, Direction.SOUTH);

    public GameRenderer(Freeworld client) {
        this.client = client;
    }

    public void init(GLStateMgr gl) {
        logger.info("Initializing game renderer");

        initGLPrograms(gl);

        gl.clearColor(0.4f, 0.6f, 0.9f, 1.0f);

        textureManager = new TextureManager();

        initBlockAtlas(gl);

        final TextureAtlas guiAtlas = TextureAtlas.load(gl, List.of(
            HudRenderer.CROSSING_TEXTURE,
            HudRenderer.HOT_BAR_TEXTURE,
            HudRenderer.HOT_BAR_SELECTED_TEXTURE
        ), 0);
        textureManager.addTexture(TextureManager.GUI_ATLAS, guiAtlas);
        logAtlas(guiAtlas, TextureManager.GUI_ATLAS);

        blockRenderer = new BlockRenderer(textureManager);
        worldRenderer = new WorldRenderer(this, client.world());

        guiGraphics = new GuiGraphics(gl, this);
        hudRenderer = new HudRenderer(this);
    }

    private void initBlockAtlas(GLStateMgr gl) {
        final var registry = client.blockModelManager().registry();

        // scan textures
        final List<Identifier> list = new ArrayList<>(registry.size());
        for (var e : registry) {
            final BlockModel model = e.getValue();
            for (BlockModelPart part : model.parts()) {
                for (BlockModelFace face : part.faces().values()) {
                    final ModelResourcePath path = face.texture();
                    switch (path.type()) {
                        case DIRECT -> list.add(path.identifier());
                        case VARIABLE -> list.add(model.textureDefinitions().get(path.identifier()));
                    }
                }
            }
        }

        final TextureAtlas blockAtlas = TextureAtlas.load(gl, list, 4);
        textureManager.addTexture(TextureManager.BLOCK_ATLAS, blockAtlas);
        logAtlas(blockAtlas, TextureManager.BLOCK_ATLAS);
    }

    private void logAtlas(TextureAtlas atlas, Identifier identifier) {
        logger.info("Created {}x{}x{} {}", atlas.width(), atlas.height(), atlas.mipmapLevel(), identifier);
    }

    private void initGLPrograms(GLStateMgr gl) {
        positionColorProgram = initBootstrapProgram(gl, "init/position_color", VertexLayouts.POSITION_COLOR);
        positionColorTexProgram = initBootstrapProgram(gl, "init/position_color_tex", VertexLayouts.POSITION_COLOR_TEX);
    }

    private GLProgram initBootstrapProgram(GLStateMgr gl, String path, VertexLayout layout) {
        final Identifier identifier = Identifier.ofBuiltin(path);
        final GLProgram program = GLProgram.load(gl, identifier, layout);
        if (program == null) {
            throw new IllegalStateException(STR."Failed to initialize bootstrap GLProgram \{identifier}");
        }
        return program;
    }

    public void render(GLStateMgr gl, double partialTick) {
        gl.clear(GL10C.COLOR_BUFFER_BIT | GL10C.DEPTH_BUFFER_BIT);

        gl.disableBlend();
        gl.enableCullFace();
        gl.enableDepthTest();
        gl.setDepthFunc(GL10C.LEQUAL);

        try (var _ = RenderSystem.matricesScope()) {
            final Camera camera = client.camera();
            final Entity player = client.player();
            camera.moveToEntity(player);
            camera.updateLerp(partialTick);
            RenderSystem.setProjectionViewMatrix(_ -> Matrix4f.setPerspective(
                (float) Math.toRadians(70.0),
                (float) client.framebufferWidth() / client.framebufferHeight(),
                0.01f,
                1000.0f
            ), _ -> camera.updateViewMatrix());
            RenderSystem.setModelMatrix(_ -> Matrix4f.IDENTITY);

            RenderSystem.useProgram(positionColorTexProgram);
            RenderSystem.updateMatrices();

            final List<ClientChunk> chunks = worldRenderer.renderingChunks(player);
            worldRenderer.compileChunks(chunks);

            RenderSystem.bindTexture2D(textureManager.getTexture(TextureManager.BLOCK_ATLAS));
            worldRenderer.renderChunks(gl, chunks);

            hitResult = worldRenderer.selectBlock(player);
            if (!hitResult.missed()) {
                final AABBox box = hitResult.blockType().outlineShape().move(hitResult.x(), hitResult.y(), hitResult.z());
                final float minX = (float) box.minX();
                final float minY = (float) box.minY();
                final float minZ = (float) box.minZ();
                final float maxX = (float) box.maxX();
                final float maxY = (float) box.maxY();
                final float maxZ = (float) box.maxZ();
                final float offset = 0.005f;
                RenderSystem.bindTexture2D(null);
                RenderSystem.useProgram(positionColorProgram);
                RenderSystem.updateMatrices();
                final Tessellator tessellator = Tessellator.getInstance();
                tessellator.begin(GLDrawMode.LINES);
                tessellator.color(0, 0, 0);
                // -x
                tessellator.indices(0, 1, 0, 2, 1, 3, 2, 3);
                // +x
                tessellator.indices(4, 5, 4, 6, 5, 7, 6, 7);
                // -z
                tessellator.indices(0, 4, 2, 6);
                // +z
                tessellator.indices(1, 5, 3, 7);
                tessellator.position(minX - offset, minY - offset, minZ - offset).emit();
                tessellator.position(minX - offset, minY - offset, maxZ + offset).emit();
                tessellator.position(minX - offset, maxY + offset, minZ - offset).emit();
                tessellator.position(minX - offset, maxY + offset, maxZ + offset).emit();
                tessellator.position(maxX + offset, minY - offset, minZ - offset).emit();
                tessellator.position(maxX + offset, minY - offset, maxZ + offset).emit();
                tessellator.position(maxX + offset, maxY + offset, minZ - offset).emit();
                tessellator.position(maxX + offset, maxY + offset, maxZ + offset).emit();
                tessellator.end(gl);
            }
        }

        gl.clear(GL10C.DEPTH_BUFFER_BIT);

        gl.disableCullFace();
        gl.disableDepthTest();
        gl.enableBlend();
        gl.setBlendFunc(GL10C.SRC_ALPHA, GL10C.ONE_MINUS_SRC_ALPHA);
        try (var _ = RenderSystem.matricesScope()) {
            hudRenderer.update(client.framebufferWidth(), client.framebufferHeight());
            hudRenderer.render(guiGraphics, gl, partialTick);
        }

        gl.disableCullFace();
        gl.disableDepthTest();
        gl.enableBlend();
        gl.setBlendFunc(GL10C.SRC_ALPHA, GL10C.ONE_MINUS_SRC_ALPHA);
        try (var _ = RenderSystem.matricesScope()) {
            renderScreen(guiGraphics, gl, partialTick);
        }
    }

    private void renderScreen(GuiGraphics graphics, GLStateMgr gl, double partialTick) {
        final Screen screen = client.screen();
        if (screen != null) {
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

        Tessellator.getInstance().close(gl);
    }

    public Freeworld client() {
        return client;
    }

    public GLProgram positionColorProgram() {
        return positionColorProgram;
    }

    public GLProgram positionColorTexProgram() {
        return positionColorTexProgram;
    }

    public TextureManager textureManager() {
        return textureManager;
    }

    public BlockRenderer blockRenderer() {
        return blockRenderer;
    }

    public HitResult hitResult() {
        return hitResult;
    }
}
