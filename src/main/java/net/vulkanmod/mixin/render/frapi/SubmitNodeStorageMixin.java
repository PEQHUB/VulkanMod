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

package net.vulkanmod.mixin.render.frapi;

import net.fabricmc.fabric.api.renderer.v1.mesh.MeshView;

import net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter;
import net.minecraft.class_10444;
import net.minecraft.class_11659;
import net.minecraft.class_11661;
import net.minecraft.class_11785;
import net.minecraft.class_4587;
import net.minecraft.class_777;
import net.minecraft.class_811;
import net.vulkanmod.render.chunk.build.frapi.accessor.AccessRenderCommandQueue;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(class_11661.class)
abstract class SubmitNodeStorageMixin implements class_11659, AccessRenderCommandQueue {
    @Override
    public void submitItem(
            class_4587 matrices,
            class_811 displayContext,
            int light,
            int overlay,
            int outlineColors,
            int[] tintLayers,
            List<class_777> quads,
            net.minecraft.class_1921 renderLayer,
            class_10444.class_10445 glintType,
            MeshView mesh,
            ItemRenderTypeGetter renderTypeGetter
    ) {
        class_11785 queue = method_73529(0);

        if (queue instanceof AccessRenderCommandQueue access) {
            access.submitItem(matrices, displayContext, light, overlay, outlineColors, tintLayers, quads, renderLayer, glintType, mesh, renderTypeGetter);
        } else {
            queue.method_73480(matrices, displayContext, light, overlay, outlineColors, tintLayers, quads, renderLayer, glintType);
        }
    }
}
