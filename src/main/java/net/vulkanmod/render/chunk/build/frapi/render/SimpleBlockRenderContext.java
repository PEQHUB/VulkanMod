package net.vulkanmod.render.chunk.build.frapi.render;
/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.minecraft.class_1087;
import net.minecraft.class_11515;
import net.minecraft.class_1920;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4696;
import net.minecraft.class_5819;
import net.minecraft.class_765;
import net.minecraft.class_9848;
import net.vulkanmod.render.chunk.build.frapi.helper.ColorHelper;
import net.vulkanmod.render.chunk.build.frapi.mesh.MutableQuadViewImpl;
import org.jetbrains.annotations.Nullable;

public class SimpleBlockRenderContext extends AbstractRenderContext {
    public static final ThreadLocal<SimpleBlockRenderContext> POOL = ThreadLocal.withInitial(SimpleBlockRenderContext::new);

    private final class_5819 random = class_5819.method_43047();

    private BlockVertexConsumerProvider vertexConsumers;
    private class_11515 defaultRenderLayer;
    private float red;
    private float green;
    private float blue;
    private int light;

    @Nullable
    private class_11515 lastRenderLayer;
    @Nullable
    private class_4588 lastVertexConsumer;

    @Override
    protected void bufferQuad(MutableQuadViewImpl quad) {
        final class_11515 quadRenderLayer = quad.renderLayer();
        final class_11515 renderLayer = quadRenderLayer == null ? defaultRenderLayer : quadRenderLayer;
        final class_4588 vertexConsumer;

        if (renderLayer == lastRenderLayer) {
            vertexConsumer = lastVertexConsumer;
        } else {
            lastVertexConsumer = vertexConsumer = vertexConsumers.getBuffer(renderLayer);
            lastRenderLayer = renderLayer;
        }

        tintQuad(quad);
        shadeQuad(quad, quad.emissive());
        bufferQuad(quad, vertexConsumer);
    }

    private void tintQuad(MutableQuadViewImpl quad) {
        if (quad.tintIndex() != -1) {
            final float red = this.red;
            final float green = this.green;
            final float blue = this.blue;

            for (int i = 0; i < 4; i++) {
                quad.color(i, class_9848.method_64602(quad.color(i), red, green, blue));
            }
        }
    }

    private void shadeQuad(MutableQuadViewImpl quad, boolean emissive) {
        if (emissive) {
            for (int i = 0; i < 4; i++) {
                quad.lightmap(i, class_765.field_32767);
            }
        } else {
            final int light = this.light;

            for (int i = 0; i < 4; i++) {
                quad.lightmap(i, ColorHelper.maxLight(quad.lightmap(i), light));
            }
        }
    }

    public void bufferModel(class_4587.class_4665 entry, BlockVertexConsumerProvider vertexConsumers, class_1087 model, float red, float green, float blue, int light, int overlay, class_1920 blockView, class_2338 pos, class_2680 state) {
        matrices = entry;
        this.overlay = overlay;

        this.vertexConsumers = vertexConsumers;
        this.defaultRenderLayer = class_4696.method_23679(state);
        this.red = class_3532.method_15363(red, 0, 1);
        this.green = class_3532.method_15363(green, 0, 1);
        this.blue = class_3532.method_15363(blue, 0, 1);
        this.light = light;

        random.method_43052(42L);

        model.emitQuads(getEmitter(), blockView, pos, state, random, cullFace -> false);

        matrices = null;
        this.vertexConsumers = null;
        lastRenderLayer = null;
        lastVertexConsumer = null;
    }
}

