package net.vulkanmod.mixin.texture.update;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.vulkanmod.render.texture.SpriteUpdateUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteContents.AnimationState.class)
public class MSpriteContents {

    @Inject(method = "drawToAtlas", at = @At("HEAD"), cancellable = true)
    private void checkUpload(RenderPass renderPass, GpuBufferSlice gpuBufferSlice, CallbackInfo ci) {
        if (!SpriteUpdateUtil.doUploadFrame()) {
            ci.cancel();
        }
    }
}
