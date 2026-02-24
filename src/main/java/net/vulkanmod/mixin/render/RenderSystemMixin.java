package net.vulkanmod.mixin.render;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.class_11282;
import net.minecraft.class_12136;
import net.minecraft.class_12289;
import net.vulkanmod.render.engine.VkGpuDevice;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import static com.mojang.blaze3d.systems.RenderSystem.*;

@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin {
    @Shadow private static @Nullable Thread renderThread;

    @Shadow private static @Nullable GpuDevice DEVICE;
    @Shadow private static @Nullable class_11282 dynamicUniforms;
    @Shadow private static class_12136 samplerCache;
    @Shadow private static String apiDescription;

    @Overwrite(remap = false)
    public static void initRenderer(long l, int i, boolean bl, class_12289 shaderSource, boolean bl2) {
        renderThread.setPriority(Thread.NORM_PRIORITY + 2);

        VRenderSystem.initRenderer();

        DEVICE = new VkGpuDevice(l, i, bl, shaderSource, bl2);
        apiDescription = getDevice().getImplementationInformation();

        Renderer.initRenderer();

        dynamicUniforms = new class_11282();
        samplerCache.method_75292();
    }

}
