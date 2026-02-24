package net.vulkanmod.mixin.render.clouds;

import net.minecraft.class_243;
import net.minecraft.class_3300;
import net.minecraft.class_4063;
import net.minecraft.class_638;
import net.minecraft.class_761;
import net.minecraft.class_9909;
import net.minecraft.class_9916;
import net.minecraft.class_9960;
import net.minecraft.client.renderer.*;
import net.vulkanmod.render.profiling.Profiler;
import net.vulkanmod.render.sky.CloudRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_761.class)
public abstract class LevelRendererM {

    @Shadow private int ticks;
    @Shadow private @Nullable class_638 level;
    @Shadow @Final private class_9960 targets;

    @Unique private CloudRenderer cloudRenderer;

    @Inject(method = "addCloudsPass", at = @At("HEAD"), cancellable = true)
    public void addCloudsPass(class_9909 frameGraphBuilder, class_4063 cloudStatus, class_243 camPos, long gameTime, float partialTicks,
                              int cloudColor, float cloudHeight, CallbackInfo ci) {
        if (this.cloudRenderer == null) {
            this.cloudRenderer = new CloudRenderer();
        }

        class_9916 framePass = frameGraphBuilder.method_61911("clouds");
        if (this.targets.field_53096 != null) {
            this.targets.field_53096 = framePass.method_61933(this.targets.field_53096);
        } else {
            this.targets.field_53091 = framePass.method_61933(this.targets.field_53091);
        }

        framePass.method_61929(() -> {
            Profiler profiler = Profiler.getMainProfiler();
            profiler.push("Clouds");

            this.cloudRenderer.renderClouds(cloudHeight, cloudColor,
                                            camPos.method_10216(), camPos.method_10214(), camPos.method_10215(),
                                            gameTime, partialTicks);

            profiler.pop();
        });

        ci.cancel();
    }

    @Inject(method = "allChanged", at = @At("RETURN"))
    private void onAllChanged(CallbackInfo ci) {
        if (this.cloudRenderer != null) {
            this.cloudRenderer.resetBuffer();
        }
    }

    @Inject(method = "onResourceManagerReload", at = @At("RETURN"))
    private void onReload(class_3300 resourceManager, CallbackInfo ci) {
        if (this.cloudRenderer != null) {
            this.cloudRenderer.loadTexture();
        }
    }

}
