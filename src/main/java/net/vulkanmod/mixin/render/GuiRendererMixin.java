package net.vulkanmod.mixin.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_10799;
import net.minecraft.class_11228;
import net.minecraft.class_11231;
import net.minecraft.class_11241;
import net.minecraft.class_11245;
import net.minecraft.class_11246;
import net.vulkanmod.render.engine.VkRenderPass;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(class_11228.class)
public abstract class GuiRendererMixin {

    @Shadow @Final private class_11246 renderState;
    @Shadow private @Nullable GpuTextureView itemsAtlasView;

//    // Debug
//    @Redirect(method = "method_71055", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/TrackingItemStackRenderState;isAnimated()Z"))
//    private boolean forceRender(TrackingItemStackRenderState instance) {
//        return true;
//    }

    @Overwrite
    private void submitBlitFromItemAtlas(class_11245 guiItemRenderState, float u, float v, int size, int atlasSize) {
        v = 1.0f - v;
        float u1 = u + (float)size / atlasSize;
        float v1 = v + (float)(size) / atlasSize;
        this.renderState
                .method_71996(
                        new class_11241(
                                class_10799.field_59968,
                                class_11231.method_70900(this.itemsAtlasView, RenderSystem.getSamplerCache().method_75297(FilterMode.NEAREST)),
                                guiItemRenderState.method_72120(),
                                guiItemRenderState.method_72122(),
                                guiItemRenderState.method_72123(),
                                guiItemRenderState.method_72122() + 16,
                                guiItemRenderState.method_72123() + 16,
                                u,
                                u1,
                                v,
                                v1,
                                -1,
                                guiItemRenderState.method_72124(),
                                null
                        )
                );
    }

    @Redirect(method = "executeDraw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;setIndexBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V"))
    private void removeIndexBuffer(RenderPass instance, GpuBuffer gpuBuffer, VertexFormat.class_5595 indexType) {
        // This draw method forces quad index buffer, not allowing other draw modes
        // Not binding it here will allow for a proper index  selection in lower level methods
    }

    @Redirect(method = "executeDraw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;drawIndexed(IIII)V"))
    private void useVertexCount(RenderPass renderPass, int baseVertex, int firstIndex, int indexCount, int instanceCount) {
        // For the same reason here we need to use vertexCount instead of indexCount

        VkRenderPass vkRenderPass = (VkRenderPass) renderPass;
        if (vkRenderPass.getPipeline().getVertexFormatMode() != VertexFormat.class_5596.field_27379) {
            int vertexCount = indexCount * 2 / 3;
            renderPass.drawIndexed(baseVertex, 0, vertexCount, 1);
        }
        else {
            renderPass.drawIndexed(baseVertex, 0, indexCount, 1);
        }
    }

}
