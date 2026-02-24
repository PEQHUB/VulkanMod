package net.vulkanmod.mixin.texture.update;

import net.minecraft.class_310;
import net.minecraft.class_757;
import net.vulkanmod.render.texture.ImageUploadHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_757.class)
public abstract class GameRendererM {

    @Shadow
    @Final
    class_310 minecraft;

//    @Inject(method = "render", at = @At("HEAD"))
//    private void onRender(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
//        if (this.minecraft.noRender || !(bl && this.minecraft.level != null && this.minecraft.isGameLoadFinished())) {
//            ImageUploadHelper.INSTANCE.submitCommands();
//        }
//    }
}
