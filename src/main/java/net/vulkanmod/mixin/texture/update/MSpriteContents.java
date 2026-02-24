package net.vulkanmod.mixin.texture.update;

import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.class_7764;
import net.vulkanmod.render.texture.SpriteUpdateUtil;
import net.vulkanmod.vulkan.texture.VTextureSelector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_7764.class_12298.class)
public class MSpriteContents {

    @Shadow private int subFrame;
    @Shadow private int frame;
    @Shadow @Final private class_7764.class_5790 animationInfo;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void checkUpload(CallbackInfo ci) {
//        if (!SpriteUpdateUtil.doUploadFrame()) {
//            // Update animations frames even if no upload is scheduled
//            ++this.subFrame;
//            SpriteContents.FrameInfo frameInfo = this.animationInfo.frames.get(this.frame);
//            if (this.subFrame >= frameInfo.time) {
//                this.frame = (this.frame + 1) % this.animationInfo.frames.size();
//                this.subFrame = 0;
//            }
//
//            ci.cancel();
//        }
//        else {
//            SpriteUpdateUtil.addTransitionedLayout(VTextureSelector.getBoundTexture());
//        }
    }
}
