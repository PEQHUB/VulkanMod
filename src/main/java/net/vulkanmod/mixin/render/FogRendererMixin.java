package net.vulkanmod.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_7285;
import net.minecraft.class_758;
import net.minecraft.class_9779;
import net.vulkanmod.vulkan.VRenderSystem;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_758.class)
public class FogRendererMixin {

    @Inject(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private void onSetupFog(class_4184 camera, int i, class_9779 deltaTracker, float f, class_638 clientLevel,
                            CallbackInfoReturnable<Vector4f> cir, @Local class_7285 fogData, @Local Vector4f fogColor) {
        VRenderSystem.fogData = fogData;
        VRenderSystem.setShaderFogColor(fogColor.x(), fogColor.y(), fogColor.z(), fogColor.w());
    }
}
