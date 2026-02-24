package net.vulkanmod.mixin.screen;

import net.minecraft.class_2561;
import net.minecraft.class_315;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.vulkanmod.config.gui.VOptionScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_429.class)
public class OptionsScreenM extends class_437 {

    @Shadow @Final private class_437 lastScreen;

    @Shadow @Final private class_315 options;

    protected OptionsScreenM(class_2561 title) {
        super(title);
    }

    @Inject(method = "method_19828", at = @At("HEAD"), cancellable = true)
    private void injectVideoOptionScreen(CallbackInfoReturnable<class_437> cir) {
        cir.setReturnValue(new VOptionScreen(class_2561.method_43470("Video Setting"), this));
    }
}
