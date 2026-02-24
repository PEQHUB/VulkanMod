package net.vulkanmod.mixin.util;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.class_1011;
import net.minecraft.class_276;
import net.minecraft.class_318;
import net.minecraft.class_9848;
import net.vulkanmod.render.engine.VkGpuTexture;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.lwjgl.vulkan.VK10;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Consumer;

@Mixin(class_318.class)
public class ScreenshotMixin {

    @Overwrite
    public static void takeScreenshot(class_276 renderTarget, int mipLevel, Consumer<class_1011> consumer) {
        int width = renderTarget.field_1482;
        int height = renderTarget.field_1481;
        GpuTexture gpuTexture = renderTarget.method_30277();
        if (gpuTexture == null) {
            throw new IllegalStateException("Tried to capture screenshot of an incomplete framebuffer");
        } else {
            // Need to submit and wait cmds if screenshot was requested
            // before the end of the frame
            Renderer.getInstance().flushCmds();

            int pixelSize = TextureFormat.RGBA8.pixelSize();
            GpuBuffer gpuBuffer = RenderSystem.getDevice()
                                              .createBuffer(() -> "Screenshot buffer", 9, width * height * pixelSize);
            CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
            RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(gpuTexture, gpuBuffer, 0, () -> {
                try (GpuBuffer.MappedView readView = commandEncoder.mapBuffer(gpuBuffer, true, false)) {
                    class_1011 nativeImage = new class_1011(width, height, false);

                    var colorAttachment = ((VkGpuTexture) Renderer.getInstance()
                                                                  .getMainPass()
                                                                  .getColorAttachment());
                    boolean isBgraFormat = (colorAttachment.getVulkanImage().format == VK10.VK_FORMAT_B8G8R8A8_UNORM);

                    int size = mipLevel * mipLevel;

                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {

                            if (mipLevel == 1) {
                                int color = readView.data().getInt((x + y * width) * pixelSize);

                                if (isBgraFormat) {
                                    color = ColorUtil.BGRAtoRGBA(color);
                                }

                                nativeImage.method_4305(x, y, color | 0xFF000000);
                            } else {
                                int red = 0;
                                int green = 0;
                                int blue = 0;

                                for (int x1 = 0; x1 < mipLevel; x1++) {
                                    for (int y1 = 0; y1 < mipLevel; y1++) {
                                        int color = readView.data().getInt(((x + x1) + (y + y1) * width) * pixelSize);

                                        if (isBgraFormat) {
                                            color = ColorUtil.BGRAtoRGBA(color);
                                        }

                                        red += class_9848.method_61327(color);
                                        green += class_9848.method_61329(color);
                                        blue += class_9848.method_61331(color);
                                    }
                                }

                                nativeImage.method_4305(x, y, class_9848.method_61324(255, red / size, green / size, blue / size));
                            }
                        }
                    }

                    consumer.accept(nativeImage);
                }

                gpuBuffer.close();
            }, 0);
        }
    }

}
