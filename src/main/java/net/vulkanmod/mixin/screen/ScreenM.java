package net.vulkanmod.mixin.screen;

import net.minecraft.class_437;
import net.vulkanmod.vulkan.Renderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_437.class)
public class ScreenM {

    @Inject(method = "renderBlurredBackground", at = @At("RETURN"))
    private void clearDepth(CallbackInfo ci) {
        // Workaround to fix hardcoded z value on PostPass blit shader,
        // that conflicts with Vulkan depth range [0.0, 1.0]
        Renderer.clearAttachments(256);
    }
}
