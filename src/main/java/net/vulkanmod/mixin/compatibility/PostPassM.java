package net.vulkanmod.mixin.compatibility;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.class_283;
import net.minecraft.class_9925;
import net.vulkanmod.render.engine.*;
import net.vulkanmod.vulkan.Renderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(class_283.class)
public abstract class PostPassM {
    @Shadow @Final private List<class_283.class_9971> inputs;

    @Inject(method = "method_67884", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuDevice;createCommandEncoder()Lcom/mojang/blaze3d/systems/CommandEncoder;"))
    private void transitionLayouts(class_9925 resourceHandle, GpuBufferSlice gpuBufferSlice, Map map,
                                   CallbackInfo ci) {
        Renderer.getInstance().endRenderPass();

        for (var input : this.inputs) {
            VkGpuTexture gpuTexture = (VkGpuTexture) input.method_71128(map).texture();

            if (gpuTexture.needsClear()) {
                gpuTexture.getFbo(null).bind();
            }

            gpuTexture.getVulkanImage().readOnlyLayout();
        }
    }

}
