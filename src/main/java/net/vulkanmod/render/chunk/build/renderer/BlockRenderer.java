package net.vulkanmod.render.chunk.build.renderer;

import net.fabricmc.fabric.api.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.class_1087;
import net.minecraft.class_11515;
import net.minecraft.class_1920;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_4588;
import net.minecraft.class_4696;
import net.minecraft.class_6575;
import net.vulkanmod.Initializer;
import net.vulkanmod.render.chunk.build.frapi.mesh.MutableQuadViewImpl;
import net.vulkanmod.render.chunk.build.frapi.render.AbstractBlockRenderContext;
import net.vulkanmod.render.chunk.build.light.LightPipeline;
import net.vulkanmod.render.chunk.build.light.data.QuadLightData;
import net.vulkanmod.render.chunk.build.thread.BuilderResources;
import net.vulkanmod.render.chunk.cull.QuadFacing;
import net.vulkanmod.render.model.quad.QuadUtils;
import net.vulkanmod.render.model.quad.ModelQuadView;
import net.vulkanmod.render.vertex.TerrainBufferBuilder;
import net.vulkanmod.render.vertex.TerrainBuilder;
import net.vulkanmod.render.vertex.TerrainRenderType;
import net.vulkanmod.render.vertex.format.I32_SNorm;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.joml.Vector3f;

public class BlockRenderer extends AbstractBlockRenderContext {
    private Vector3f pos;

    private BuilderResources resources;
    private TerrainBuilder terrainBuilder;

    final boolean backFaceCulling = Initializer.CONFIG.backFaceCulling;

    private TerrainRenderType renderType;

    public void setResources(BuilderResources resources) {
        this.resources = resources;
    }

    public BlockRenderer(LightPipeline flatLightPipeline, LightPipeline smoothLightPipeline) {
        super();
        this.setupLightPipelines(flatLightPipeline, smoothLightPipeline);

        this.random = new class_6575(42L);
    }

    public void renderBlock(class_2680 blockState, class_2338 blockPos, Vector3f pos) {
        this.pos = pos;
        this.blockPos = blockPos;
        this.blockState = blockState;
        this.random.method_43052(blockState.method_26190(blockPos));

        TerrainRenderType renderType = TerrainRenderType.get(class_4696.method_23679(blockState));
        renderType = TerrainRenderType.getRemapped(renderType);
        this.renderType = renderType;
        this.terrainBuilder = this.resources.builderPack.builder(renderType);
        this.terrainBuilder.setBlockAttributes(blockState);

        class_1087 model = class_310.method_1551().method_1541().method_3349(blockState);

        class_1920 renderRegion = this.renderRegion;
        class_243 offset = blockState.method_26226(blockPos);
        pos.add((float) offset.field_1352, (float) offset.field_1351, (float) offset.field_1350);

        this.prepareForBlock(blockState, blockPos, blockState.method_26213() == 0);

        model.emitQuads(this.getEmitter(), renderRegion, blockPos, blockState, this.random, this::isFaceCulled);
    }

    @Override
    protected class_4588 getVertexConsumer(class_11515 layer) {
        return null;
    }

    protected void endRenderQuad(MutableQuadViewImpl quad) {
        final TriState aoMode = quad.ambientOcclusion();
        final boolean ao = this.useAO && (aoMode == TriState.TRUE || (aoMode == TriState.DEFAULT && this.defaultAO));
        final boolean emissive = quad.emissive();
        final boolean vanillaShade = quad.shadeMode() == ShadeMode.VANILLA;

        TerrainBuilder terrainBuilder = getBufferBuilder(quad.renderLayer());

        LightPipeline lightPipeline = ao ? this.smoothLightPipeline : this.flatLightPipeline;

        tintQuad(quad);
        shadeQuad(quad, lightPipeline, emissive, vanillaShade);
        bufferQuad(terrainBuilder, this.pos, quad, this.quadLightData);
    }

    private TerrainBuilder getBufferBuilder(class_11515 layer) {
        if (layer == null) {
            return this.terrainBuilder;
        } else {
            TerrainRenderType renderType = TerrainRenderType.get(layer);
            renderType = TerrainRenderType.getRemapped(renderType);
            TerrainBuilder bufferBuilder = this.resources.builderPack.builder(renderType);
            bufferBuilder.setBlockAttributes(this.blockState);

            return bufferBuilder;
        }
    }

    public void bufferQuad(TerrainBuilder terrainBuilder, Vector3f pos, ModelQuadView quad, QuadLightData quadLightData) {
        QuadFacing quadFacing = quad.getQuadFacing();

        if (renderType == TerrainRenderType.TRANSLUCENT || !this.backFaceCulling) {
            quadFacing = QuadFacing.UNDEFINED;
        }

        TerrainBufferBuilder bufferBuilder = terrainBuilder.getBufferBuilder(quadFacing.ordinal());

        class_2382 normal = quad.getFacingDirection().method_62675();
        int packedNormal = I32_SNorm.packNormal(normal.method_10263(), normal.method_10264(), normal.method_10260());

        float[] brightnessArr = quadLightData.br;
        int[] lights = quadLightData.lm;

        // Rotate triangles if needed to fix AO anisotropy
        int idx = QuadUtils.getIterationStartIdx(brightnessArr, lights);

        bufferBuilder.ensureCapacity();

        for (byte i = 0; i < 4; ++i) {
            final float x = pos.x() + quad.getX(idx);
            final float y = pos.y() + quad.getY(idx);
            final float z = pos.z() + quad.getZ(idx);

            final int quadColor = quad.getColor(idx);

            int color = ColorUtil.ARGB.toRGBA(quadColor);

            final int light = lights[idx];
            final float u = quad.getU(idx);
            final float v = quad.getV(idx);

            bufferBuilder.vertex(x, y, z, color, u, v, light, packedNormal);

            idx = (idx + 1) & 0b11;
        }

    }

}

