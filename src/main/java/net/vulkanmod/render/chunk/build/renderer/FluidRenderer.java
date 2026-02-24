package net.vulkanmod.render.chunk.build.renderer;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRendering;
import net.minecraft.class_1058;
import net.minecraft.class_1920;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3486;
import net.minecraft.class_3532;
import net.minecraft.class_3610;
import net.minecraft.class_3611;
import net.minecraft.class_3612;
import net.minecraft.class_4588;
import net.minecraft.class_4696;
import net.vulkanmod.render.chunk.build.light.LightPipeline;
import net.vulkanmod.render.chunk.build.light.data.QuadLightData;
import net.vulkanmod.render.chunk.build.thread.BuilderResources;
import net.vulkanmod.render.chunk.cull.QuadFacing;
import net.vulkanmod.render.chunk.util.Util;
import net.vulkanmod.render.model.quad.ModelQuad;
import net.vulkanmod.render.model.quad.ModelQuadFlags;
import net.vulkanmod.render.model.quad.QuadUtils;
import net.vulkanmod.render.vertex.TerrainBufferBuilder;
import net.vulkanmod.render.vertex.TerrainRenderType;
import net.vulkanmod.render.vertex.format.I32_SNorm;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.joml.Vector3f;

public class FluidRenderer implements FluidRendering.DefaultRenderer {
    private static final float MAX_FLUID_HEIGHT = 0.8888889F;

    private final class_2338.class_2339 mBlockPos = new class_2338.class_2339();

    private final ModelQuad modelQuad = new ModelQuad();

    BuilderResources resources;

    private final LightPipeline smoothLightPipeline;
    private final LightPipeline flatLightPipeline;

    private final int[] quadColors = new int[4];

    public FluidRenderer(LightPipeline flatLightPipeline, LightPipeline smoothLightPipeline) {
        this.smoothLightPipeline = smoothLightPipeline;
        this.flatLightPipeline = flatLightPipeline;
    }

    public void setResources(BuilderResources resources) {
        this.resources = resources;
    }

    public void renderLiquid(class_2680 blockState, class_3610 fluidState, class_2338 blockPos) {
        FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluidState.method_15772());

        TerrainRenderType renderType = TerrainRenderType.get(class_4696.method_23680(fluidState));
        renderType = TerrainRenderType.getRemapped(renderType);
        TerrainBufferBuilder bufferBuilder = this.resources.builderPack.builder(renderType).getBufferBuilder(QuadFacing.UNDEFINED.ordinal());

        // Fallback to water/lava in case there's no handler
        if (handler == null) {
            boolean isLava = fluidState.method_15767(class_3486.field_15518);
            handler = FluidRenderHandlerRegistry.INSTANCE.get(isLava ? class_3612.field_15908 : class_3612.field_15910);
        }

        FluidRendering.render(handler, this.resources.getRegion(),blockPos, bufferBuilder, blockState, fluidState, this);
    }

    private boolean isFaceOccludedByState(class_1922 blockGetter, float h, class_2350 direction, class_2338 blockPos, class_2680 blockState) {
        mBlockPos.method_10101(blockPos).method_10081(class_2350.field_11033.method_62675());

        if (blockState.method_26225()) {
            class_265 occlusionShape = blockState.method_26201();

            if (occlusionShape == class_259.method_1077()) {
                return direction != class_2350.field_11036;
            } else if (occlusionShape.method_1110()) {
                return false;
            }

            class_265 voxelShape = class_259.method_1081(0.0, 0.0, 0.0, 1.0, h, 1.0);
            return class_259.method_1083(voxelShape, occlusionShape, direction);
        } else {
            return false;
        }
    }

    public static boolean shouldRenderFace(class_1920 blockAndTintGetter, class_2338 blockPos, class_3610 fluidState, class_2680 blockState, class_2350 direction, class_2680 adjBlockState) {

        if (adjBlockState.method_26227().method_15772().method_15780(fluidState.method_15772()))
            return false;

        // self-occlusion by waterlogging
        if (blockState.method_26225()) {
            return !blockState.method_26206(blockAndTintGetter, blockPos, direction);
        }

        return true;
    }

    public class_2680 getAdjBlockState(class_1920 blockAndTintGetter, int x, int y, int z, class_2350 dir) {
        mBlockPos.method_10103(x + dir.method_10148(), y + dir.method_10164(), z + dir.method_10165());
        return blockAndTintGetter.method_8320(mBlockPos);
    }

    public void render(FluidRenderHandler handler, class_1920 world, class_2338 pos, class_4588 vertexConsumer, class_2680 blockState, class_3610 fluidState) {
        render(handler, blockState, fluidState, pos, (TerrainBufferBuilder) vertexConsumer);
    }

    public void render(FluidRenderHandler handler, class_2680 blockState, class_3610 fluidState, class_2338 blockPos, TerrainBufferBuilder bufferBuilder) {
        class_1920 region = this.resources.getRegion();

        int color = handler.getFluidColor(region, blockPos, fluidState);
        class_1058[] sprites = handler.getFluidSprites(region, blockPos, fluidState);

        float r = ColorUtil.ARGB.unpackR(color);
        float g = ColorUtil.ARGB.unpackG(color);
        float b = ColorUtil.ARGB.unpackB(color);

        final int posX = blockPos.method_10263();
        final int posY = blockPos.method_10264();
        final int posZ = blockPos.method_10260();

        boolean useAO = blockState.method_26213() == 0 && class_310.method_1588();
        LightPipeline lightPipeline = useAO ? this.smoothLightPipeline : this.flatLightPipeline;

        class_2680 downState = getAdjBlockState(region, posX, posY, posZ, class_2350.field_11033);
        class_2680 upState = getAdjBlockState(region, posX, posY, posZ, class_2350.field_11036);
        class_2680 northState = getAdjBlockState(region, posX, posY, posZ, class_2350.field_11043);
        class_2680 southState = getAdjBlockState(region, posX, posY, posZ, class_2350.field_11035);
        class_2680 westState = getAdjBlockState(region, posX, posY, posZ, class_2350.field_11039);
        class_2680 eastState = getAdjBlockState(region, posX, posY, posZ, class_2350.field_11034);

//        boolean rUf = !isNeighborSameFluid(fluidState, upFluid);
        boolean rUf = shouldRenderFace(region, blockPos, fluidState, blockState, class_2350.field_11036, upState);
        boolean rDf = shouldRenderFace(region, blockPos, fluidState, blockState, class_2350.field_11033, downState)
                && !isFaceOccludedByState(region, MAX_FLUID_HEIGHT, class_2350.field_11033, blockPos, downState);
        boolean rNf = shouldRenderFace(region, blockPos, fluidState, blockState, class_2350.field_11043, northState);
        boolean rSf = shouldRenderFace(region, blockPos, fluidState, blockState, class_2350.field_11035, southState);
        boolean rWf = shouldRenderFace(region, blockPos, fluidState, blockState, class_2350.field_11039, westState);
        boolean rEf = shouldRenderFace(region, blockPos, fluidState, blockState, class_2350.field_11034, eastState);

        if (!(rUf || rDf || rEf || rWf || rNf || rSf))
            return;

        float brightnessUp = region.method_24852(class_2350.field_11036, true);

        class_3611 fluid = fluidState.method_15772();
        float height = this.getHeight(region, fluid, blockPos, blockState);
        float neHeight;
        float nwHeight;
        float seHeight;
        float swHeight;
        if (height >= 1.0F) {
            neHeight = 1.0F;
            nwHeight = 1.0F;
            seHeight = 1.0F;
            swHeight = 1.0F;
        } else {
            float s = this.getHeight(region, fluid, mBlockPos.method_10101(blockPos).method_10081(class_2350.field_11043.method_62675()), northState);
            float t = this.getHeight(region, fluid, mBlockPos.method_10101(blockPos).method_10081(class_2350.field_11035.method_62675()), southState);
            float u = this.getHeight(region, fluid, mBlockPos.method_10101(blockPos).method_10081(class_2350.field_11034.method_62675()), eastState);
            float v = this.getHeight(region, fluid, mBlockPos.method_10101(blockPos).method_10081(class_2350.field_11039.method_62675()), westState);
            neHeight = this.calculateAverageHeight(region, fluid, height, s, u, mBlockPos.method_10101(blockPos).method_10081(class_2350.field_11043.method_62675()).method_10081(class_2350.field_11034.method_62675()));
            nwHeight = this.calculateAverageHeight(region, fluid, height, s, v, mBlockPos.method_10101(blockPos).method_10081(class_2350.field_11043.method_62675()).method_10081(class_2350.field_11039.method_62675()));
            seHeight = this.calculateAverageHeight(region, fluid, height, t, u, mBlockPos.method_10101(blockPos).method_10081(class_2350.field_11035.method_62675()).method_10081(class_2350.field_11034.method_62675()));
            swHeight = this.calculateAverageHeight(region, fluid, height, t, v, mBlockPos.method_10101(blockPos).method_10081(class_2350.field_11035.method_62675()).method_10081(class_2350.field_11039.method_62675()));
        }

        float x0 = (posX & 15);
        float y0 = (posY & 15);
        float z0 = (posZ & 15);
//            float x = 0.001F;
        float y = rDf ? 0.001F : 0.0F;

        modelQuad.setFlags(0);

        if (rUf && !isFaceOccludedByState(region, Math.min(Math.min(nwHeight, swHeight), Math.min(seHeight, neHeight)), class_2350.field_11036, blockPos, upState)) {
            float u0, u1, u2, u3;
            float v0, v1, v2, v3;

            nwHeight -= 0.001F;
            swHeight -= 0.001F;
            seHeight -= 0.001F;
            neHeight -= 0.001F;
            class_243 vec3 = fluidState.method_15758(region, blockPos);
            class_1058 sprite;

            if (vec3.field_1352 == 0.0 && vec3.field_1350 == 0.0) {
                sprite = sprites[0];
                u0 = sprite.method_4580(0.0F);
                v0 = sprite.method_4570(0.0F);
                u1 = u0;
                v1 = sprite.method_4570(1.0F);
                u2 = sprite.method_4580(1.0F);
                v2 = v1;
                u3 = u2;
                v3 = v0;
            } else {
                sprite = sprites[1];
                float ah = (float) class_3532.method_15349(vec3.field_1350, vec3.field_1352) - 1.5707964F;
                float ai = class_3532.method_15374(ah) * 0.25F;
                float aj = class_3532.method_15362(ah) * 0.25F;

                u0 = sprite.method_4580(0.5F + (-aj - ai));
                v0 = sprite.method_4570(0.5F - aj + ai);
                u1 = sprite.method_4580(0.5F - aj + ai);
                v1 = sprite.method_4570(0.5F + aj + ai);
                u2 = sprite.method_4580(0.5F + aj + ai);
                v2 = sprite.method_4570(0.5F + (aj - ai));
                u3 = sprite.method_4580(0.5F + (aj - ai));
                v3 = sprite.method_4570(0.5F + (-aj - ai));
            }

            float brightness = brightnessUp;

            setVertex(modelQuad, 0, 0.0f, nwHeight, 0.0f, u0, v0);
            setVertex(modelQuad, 1, 0.0f, swHeight, 1.0f, u1, v1);
            setVertex(modelQuad, 2, 1.0f, seHeight, 1.0f, u2, v2);
            setVertex(modelQuad, 3, 1.0f, neHeight, 0.0f, u3, v3);

            updateQuad(this.modelQuad, blockPos, lightPipeline, class_2350.field_11036);
            updateColor(r, g, b, brightness);

            putQuad(modelQuad, bufferBuilder, x0, y0, z0, false);

            if (fluidState.method_15756(region, blockPos.method_10084())) {
                putQuad(modelQuad, bufferBuilder, x0, y0, z0, true);
            }

        }

        if (rDf) {
            float u0, u1, v0, v1;

            u0 = sprites[0].method_4594();
            u1 = sprites[0].method_4577();
            v0 = sprites[0].method_4593();
            v1 = sprites[0].method_4575();

            float brightness = region.method_24852(class_2350.field_11033, true);

            setVertex(modelQuad, 0, 0.0f, y, 1.0f, u0, v1);
            setVertex(modelQuad, 1, 0.0f, y, 0.0f, u0, v0);
            setVertex(modelQuad, 2, 1.0f, y, 0.0f, u1, v0);
            setVertex(modelQuad, 3, 1.0f, y, 1.0f, u1, v1);

            updateQuad(this.modelQuad, blockPos, lightPipeline, class_2350.field_11033);
            updateColor(r, g, b, brightness);

            putQuad(modelQuad, bufferBuilder, x0, y0, z0, false);
        }

        modelQuad.setFlags(ModelQuadFlags.IS_PARALLEL | ModelQuadFlags.IS_ALIGNED);

        for (class_2350 direction : Util.XZ_DIRECTIONS) {
            float h1;
            float h2;

            float x1;
            float z1;
            float x2;
            float z2;

            final float E = 0.001f;
            final float E2 = 0.999f;

            class_2680 adjState;
            switch (direction) {
                case field_11043 -> {
                    if (!rNf)
                        continue;

                    h1 = nwHeight;
                    h2 = neHeight;
                    x1 = 0.0f;
                    x2 = 1.0f;
                    z1 = E;
                    z2 = E;

                    adjState = northState;
                }
                case field_11035 -> {
                    if (!rSf)
                        continue;

                    h1 = seHeight;
                    h2 = swHeight;
                    x1 = 1.0f;
                    x2 = 0.0f;
                    z1 = E2;
                    z2 = E2;

                    adjState = southState;
                }
                case field_11039 -> {
                    if (!rWf)
                        continue;

                    h1 = swHeight;
                    h2 = nwHeight;
                    x1 = E;
                    x2 = E;
                    z1 = 1.0f;
                    z2 = 0.0f;

                    adjState = westState;
                }
                case field_11034 -> {
                    if (!rEf)
                        continue;

                    h1 = neHeight;
                    h2 = seHeight;
                    x1 = E2;
                    x2 = E2;
                    z1 = 0.0f;
                    z2 = 1.0f;

                    adjState = eastState;
                }

                default -> {
                    continue;
                }
            }

            if (isFaceOccludedByState(region, Math.max(h1, h2), direction, blockPos, adjState))
                continue;

            class_1058 sprite = sprites[1];
            boolean isOverlay = false;

            if (sprites.length > 2) {
                if (FluidRenderHandlerRegistry.INSTANCE.isBlockTransparent(adjState.method_26204())) {
                    sprite = sprites[2];
                    isOverlay = true;
                }
            }

            float u0 = sprite.method_4580(0.0F);
            float u1 = sprite.method_4580(0.5F);
            float v0 = sprite.method_4570((1.0F - h1) * 0.5F);
            float v1 = sprite.method_4570((1.0F - h2) * 0.5F);
            float v2 = sprite.method_4570(0.5F);

            float brightness = region.method_24852(direction, true);

            setVertex(modelQuad, 0, x2, h2, z2, u1, v1);
            setVertex(modelQuad, 1, x2, y, z2, u1, v2);
            setVertex(modelQuad, 2, x1, y, z1, u0, v2);
            setVertex(modelQuad, 3, x1, h1, z1, u0, v0);

            updateQuad(this.modelQuad, blockPos, lightPipeline, direction);
            updateColor(r, g, b, brightness);

            putQuad(modelQuad, bufferBuilder, x0, y0, z0, false);

            if (!isOverlay) {
                putQuad(modelQuad, bufferBuilder, x0, y0, z0, true);
            }

        }
    }

    private float calculateAverageHeight(class_1920 blockAndTintGetter, class_3611 fluid, float f, float g, float h, class_2338 blockPos) {
        if (!(h >= 1.0F) && !(g >= 1.0F)) {
            float[] fs = new float[2];
            if (h > 0.0F || g > 0.0F) {
                float i = this.getHeight(blockAndTintGetter, fluid, blockPos);
                if (i >= 1.0F) {
                    return 1.0F;
                }

                this.addWeightedHeight(fs, i);
            }

            this.addWeightedHeight(fs, f);
            this.addWeightedHeight(fs, h);
            this.addWeightedHeight(fs, g);
            return fs[0] / fs[1];
        } else {
            return 1.0F;
        }
    }

    private void addWeightedHeight(float[] fs, float f) {
        if (f >= 0.8F) {
            fs[0] += f * 10.0F;
            fs[1] += 10.0F;
        } else if (f >= 0.0F) {
            fs[0] += f;
            fs[1]++;
        }

    }

    private float getHeight(class_1920 blockAndTintGetter, class_3611 fluid, class_2338 blockPos) {
        class_2680 blockState = blockAndTintGetter.method_8320(blockPos);
        return this.getHeight(blockAndTintGetter, fluid, blockPos, blockState);
    }

    private float getHeight(class_1920 blockAndTintGetter, class_3611 fluid, class_2338 blockPos, class_2680 adjBlockState) {
        class_3610 adjFluidState = adjBlockState.method_26227();
        if (fluid.method_15780(adjFluidState.method_15772())) {
            class_2680 blockState2 = blockAndTintGetter.method_8320(blockPos.method_10081(class_2350.field_11036.method_62675()));
            return fluid.method_15780(blockState2.method_26227().method_15772()) ? 1.0F : adjFluidState.method_20785();
        } else {
            return !adjBlockState.method_51367() ? 0.0F : -1.0f;
        }
    }

    private int calculateNormal(ModelQuad quad) {
        // TODO
        Vector3f normal = new Vector3f(quad.getX(1), quad.getY(1), quad.getZ(1))
                .cross(quad.getX(3), quad.getY(3), quad.getZ(3));
        normal.normalize();

        return I32_SNorm.packNormal(normal.x(), normal.y(), normal.z());
    }

    private void putQuad(ModelQuad quad, TerrainBufferBuilder bufferBuilder, float xOffset, float yOffset, float zOffset, boolean flip) {
        QuadLightData quadLightData = resources.quadLightData;

        // Rotate triangles if needed to fix AO anisotropy
        int k = QuadUtils.getIterationStartIdx(quadLightData.br);

        bufferBuilder.ensureCapacity();

        int i;
        for (int j = 0; j < 4; j++) {
            i = k;

            final float x = xOffset + quad.getX(i);
            final float y = yOffset + quad.getY(i);
            final float z = zOffset + quad.getZ(i);

            bufferBuilder.vertex(x, y, z, this.quadColors[i], quad.getU(i), quad.getV(i), quadLightData.lm[i], 0);

            k += (flip ? -1 : +1);
            k &= 0b11;
        }

    }

    private void setVertex(ModelQuad quad, int i, float x, float y, float z, float u, float v) {
        quad.setX(i, x);
        quad.setY(i, y);
        quad.setZ(i, z);
        quad.setU(i, u);
        quad.setV(i, v);
    }

    private void updateQuad(ModelQuad quad, class_2338 blockPos, LightPipeline lightPipeline, class_2350 dir) {
        lightPipeline.calculate(quad, blockPos, resources.quadLightData, null, dir, false);
    }

    private void updateColor(float r, float g, float b, float brightness) {
        QuadLightData quadLightData = resources.quadLightData;

        for (int i = 0; i < 4; i++) {
            float br = quadLightData.br[i] * brightness;
            float r1 = r * br;
            float g1 = g * br;
            float b1 = b * br;

            this.quadColors[i] = ColorUtil.RGBA.pack(r1, g1, b1, 1.0f);
        }
    }
}
