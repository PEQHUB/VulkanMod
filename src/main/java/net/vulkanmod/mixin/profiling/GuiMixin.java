package net.vulkanmod.mixin.profiling;

import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_340;
import net.minecraft.class_9779;
import net.vulkanmod.render.profiling.ProfilerOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_329.class)
public class GuiMixin {

    @Shadow @Final private class_340 debugOverlay;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void createProfilerOverlay(class_310 minecraft, CallbackInfo ci) {
        ProfilerOverlay.createInstance(minecraft);
    }

    @Inject(method = "render", at = @At(value = "RETURN"))
    private void renderProfilerOverlay(class_332 guiGraphics, class_9779 deltaTracker, CallbackInfo ci) {
        if (ProfilerOverlay.shouldRender && !this.debugOverlay.method_53536())
            ProfilerOverlay.INSTANCE.render(guiGraphics);
    }
}
