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
import freeworld.client.render.screen.Screen;
import freeworld.client.render.texture.TextureAtlas;
import freeworld.client.render.texture.TextureManager;
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
import overrungl.opengl.GL10C;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private BlockHitResult hitResult = new BlockHitResult(true, null, Vector3i.ZERO, Direction.SOUTH);
    private Map<EntityType<?>, EntityRenderer<?>> rendererMap;

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

        rendererMap = EntityRenderers.loadRenderers(client);
    }

    private void initBlockAtlas(GLStateMgr gl) {
        final var registry = client.blockModelManager().registry();

        // scan textures
        final List<Identifier> list = new ArrayList<>(registry.size());
        for (var e : registry) {
            final BlockModel model = e.getValue();
            for (BlockModelPart part : model.parts()) {
                for (BlockModelFace face : part.faces().values()) {
                    list.add(model.textureDefinitions().get(face.textureKey()));
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

        if (worldRenderer != null) {
            renderWorld(gl, partialTick);
        }

        gl.clear(GL10C.DEPTH_BUFFER_BIT);
        if (client.world() != null) {
            renderHud(gl, partialTick);
        }
        renderGui(gl, partialTick);
    }

    private void renderWorld(GLStateMgr gl, double partialTick) {
        gl.disableBlend();
        gl.enableCullFace();
        gl.enableDepthTest();
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

        RenderSystem.useProgram(positionColorTexProgram);
        RenderSystem.updateMatrices();

        worldRenderer.compileChunks(player);

        hitResult = worldRenderer.selectBlock(player);

        RenderSystem.bindTexture2D(textureManager.getTexture(TextureManager.BLOCK_ATLAS));
        if (!hitResult.missed()) {
            gl.enablePolygonOffsetFill();
            gl.setPolygonOffset(1.0f, 1.0f);
            gl.setLineWidth(2.0f);
        }
        worldRenderer.renderChunks(gl, player);
        if (!hitResult.missed()) {
            gl.disablePolygonOffsetFill();
            gl.setLineWidth(1.0f);
        }

        if (!hitResult.missed()) {
            var lines = hitResult.blockType().outlineShape().toLines(Direction.LIST);
            Matrix4f mat = Matrix4f.translation(hitResult.position().toVector3f());
            RenderSystem.bindTexture2D(null);
            RenderSystem.useProgram(positionColorProgram);
            RenderSystem.updateMatrices();
            final Tessellator tessellator = Tessellator.getInstance();
            tessellator.begin(GLDrawMode.LINES);
            for (Lined line : lines) {
                tessellator.indices(0, 1);
                tessellator.position(mat, line.from().toVector3f()).color(0, 0, 0).texCoord(0f, 0f).emit();
                tessellator.position(mat, line.to().toVector3f()).color(0, 0, 0).texCoord(0f, 0f).emit();
            }
            tessellator.end(gl);
        }

        renderWorldEntities(gl, partialTick);
    }

    private void renderWorldEntities(GLStateMgr gl, double partialTick) {
        RenderSystem.useProgram(positionColorProgram);
        RenderSystem.updateMatrices();
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
        return (EntityRenderer<T>) rendererMap.get(entity.type());
    }

    private void renderHud(GLStateMgr gl, double partialTick) {
        gl.disableCullFace();
        gl.disableDepthTest();
        gl.enableBlend();
        gl.setBlendFunc(GL10C.SRC_ALPHA, GL10C.ONE_MINUS_SRC_ALPHA);
        hudRenderer.update(client.scaledFramebufferWidth(), client.scaledFramebufferHeight());
        hudRenderer.render(guiGraphics, gl, partialTick);
    }

    private void renderGui(GLStateMgr gl, double partialTick) {
        gl.disableCullFace();
        gl.disableDepthTest();
        gl.enableBlend();
        gl.setBlendFunc(GL10C.SRC_ALPHA, GL10C.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setProjectionViewMatrix(Matrix4f.setOrtho(0.0f,
                client.scaledFramebufferWidth(),
                0.0f,
                client.scaledFramebufferHeight(),
                -300.0f,
                300.0f),
            Matrix4f.identity());
        RenderSystem.setModelMatrix(Matrix4f.identity());
        renderScreen(guiGraphics, gl, partialTick);
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

    public BlockHitResult hitResult() {
        return hitResult;
    }
}
