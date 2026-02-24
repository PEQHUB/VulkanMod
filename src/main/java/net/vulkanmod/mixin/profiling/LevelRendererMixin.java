package net.vulkanmod.mixin.profiling;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.class_11658;
import net.minecraft.class_243;
import net.minecraft.class_3695;
import net.minecraft.class_4063;
import net.minecraft.class_761;
import net.minecraft.class_9925;
import net.vulkanmod.render.profiling.Profiler;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_761.class)
public class LevelRendererMixin {

    @Inject(method = "method_62205", at = @At("HEAD"))
    private void pushProfiler(int i, class_4063 cloudStatus, float f, class_243 vec3, long l, float g, CallbackInfo ci) {
        Profiler profiler = Profiler.getMainProfiler();
        profiler.push("Clouds");
    }

    @Inject(method = "method_62205", at = @At("RETURN"))
    private void popProfiler(int i, class_4063 cloudStatus, float f, class_243 vec3, long l, float g, CallbackInfo ci) {
        Profiler profiler = Profiler.getMainProfiler();
        profiler.pop();
    }

    // TODO: fix
    @Inject(method = "method_62213", at = @At(value = "HEAD"))
    private void pushProfiler3(GpuBufferSlice gpuBufferSlice, class_9925 resourceHandle,
                               class_9925 resourceHandle2, CallbackInfo ci) {
        Profiler profiler = Profiler.getMainProfiler();
        profiler.push("Particles");
    }

    @Inject(method = "method_62213", at = @At(value = "RETURN"))
    private void popProfiler3(GpuBufferSlice gpuBufferSlice, class_9925 resourceHandle,
                              class_9925 resourceHandle2, CallbackInfo ci) {
        Profiler profiler = Profiler.getMainProfiler();
        profiler.pop();
    }

    @Inject(method = "method_62214",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/LevelRenderer;submitEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/state/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"))
    private void profilerTerrain2(GpuBufferSlice gpuBufferSlice, class_11658 levelRenderState,
                                  class_3695 profilerFiller, Matrix4f matrix4f, class_9925 resourceHandle,
                                  class_9925 resourceHandle2, boolean bl, class_9925 resourceHandle3,
                                  class_9925 resourceHandle4, CallbackInfo ci) {
        Profiler profiler = Profiler.getMainProfiler();
        profiler.pop();
        profiler.push("entities");
    }


}
