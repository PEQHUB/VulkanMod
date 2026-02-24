package net.vulkanmod.mixin.render.target;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.class_10799;
import net.minecraft.class_276;
import net.vulkanmod.render.engine.VkFbo;
import net.vulkanmod.render.engine.VkGpuTexture;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.util.OptionalInt;

@Mixin(class_276.class)
public abstract class RenderTargetMixin {

    @Shadow public int width;
    @Shadow public int height;

    @Shadow @Nullable protected GpuTexture colorTexture;
    @Shadow @Nullable protected GpuTexture depthTexture;
    @Shadow @Nullable protected GpuTextureView colorTextureView;

    @Overwrite
    public void blitAndBlendToTexture(GpuTextureView gpuTextureView) {
        RenderSystem.assertOnRenderThread();

        VkFbo fbo = ((VkGpuTexture) this.colorTexture).getFbo(this.depthTexture);
        if (fbo.needsClear()) {
            return;
        }

        try (RenderPass renderPass = RenderSystem.getDevice()
                                                 .createCommandEncoder()
                                                 .createRenderPass(() -> "Blit render target", gpuTextureView, OptionalInt.empty())) {
            renderPass.setPipeline(class_10799.field_56840);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.bindTexture("InSampler", this.colorTextureView, RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST));
            renderPass.draw(0, 3);
        }
    }

}
