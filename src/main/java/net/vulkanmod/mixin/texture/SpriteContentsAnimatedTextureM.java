package net.vulkanmod.mixin.texture;

import net.minecraft.class_7764;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(class_7764.class_5790.class)
public class SpriteContentsAnimatedTextureM {

    @ModifyArg(method = "createAnimationState", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuDevice;createTexture(Ljava/util/function/Supplier;ILcom/mojang/blaze3d/textures/TextureFormat;IIII)Lcom/mojang/blaze3d/textures/GpuTexture;"), index = 6)
    private int fixMipLevels(int mipLevels) {
        return mipLevels - 1;
    }
}
