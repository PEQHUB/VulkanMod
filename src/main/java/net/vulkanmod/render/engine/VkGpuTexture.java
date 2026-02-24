package net.vulkanmod.render.engine;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10868;
import net.vulkanmod.gl.VkGlTexture;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VK10;

@Environment(EnvType.CLIENT)
public class VkGpuTexture extends class_10868 {
    private static final Reference2ReferenceOpenHashMap<class_10868, VkGpuTexture> glToVkMap = new Reference2ReferenceOpenHashMap<>();

    protected VkGlTexture glTexture;
    protected final int field_57882;
    private final Int2ReferenceMap<VkFbo> fboCache = new Int2ReferenceOpenHashMap<>();
    protected boolean field_57883;

    VkTextureView fboView;
    boolean needsClear = false;
    int clearColor = 0;
    float depthClearValue = 1.0f;

    protected VkGpuTexture(int usage, String string, TextureFormat textureFormat, int width, int height, int layers, int mipLevel, int id, VkGlTexture glTexture) {
        super(usage, string, textureFormat, width, height, layers, mipLevel, id);
        this.field_57882 = id;
        this.glTexture = glTexture;
    }

    @Override
    public void close() {
        if (!this.field_57883) {
            this.field_57883 = true;
            GlStateManager._deleteTexture(this.field_57882);
        }
    }

    @Override
    public boolean isClosed() {
        return this.field_57883;
    }

    public int method_68427() {
        return this.field_57882;
    }

    public void setClearColor(int clearColor) {
        this.needsClear = true;
        this.clearColor = clearColor;
    }

    public void setDepthClearValue(float depthClearValue) {
        this.needsClear = true;
        this.depthClearValue = depthClearValue;
    }

    public boolean needsClear() {
        return needsClear;
    }

    public VkFbo getFbo(@Nullable GpuTexture depthAttachment) {
        int depthAttachmentId = depthAttachment == null ? 0 : ((VkGpuTexture)depthAttachment).field_57882;

        if (this.fboView == null) {
            VkGpuDevice gpuDevice = (VkGpuDevice) RenderSystem.getDevice();
            this.fboView = (VkTextureView) gpuDevice.createTextureView(this, 0, this.getMipLevels());
        }

        return this.fboCache.computeIfAbsent(depthAttachmentId, j -> new VkFbo(this.fboView, (VkGpuTexture) depthAttachment));
    }

    public VulkanImage getVulkanImage() {
        return glTexture.getVulkanImage();
    }

    public static VkGpuTexture fromGlTexture(class_10868 glTexture) {
        return glToVkMap.computeIfAbsent(glTexture, glTexture1 -> {
            var name = glTexture.getLabel();
            int id = glTexture.method_68427();
            VkGlTexture vglTexture = VkGlTexture.getTexture(id);
            VkGpuTexture gpuTexture = new VkGpuTexture(0, name, glTexture.getFormat(),
                                                       glTexture.getWidth(0), glTexture.getHeight(0),
                                                       1, glTexture.getMipLevels(),
                                                       glTexture.method_68427(), vglTexture);

            return gpuTexture;
        });
    }

    public static TextureFormat textureFormat(int format) {
        return switch (format) {
            case VK10.VK_FORMAT_R8G8B8A8_UNORM, VK10.VK_FORMAT_B8G8R8A8_UNORM -> TextureFormat.RGBA8;
            case VK10.VK_FORMAT_R8_UNORM -> TextureFormat.RED8;
            case VK10.VK_FORMAT_D32_SFLOAT -> TextureFormat.DEPTH32;
            default -> null;
        };
    }

    public static int vkFormat(TextureFormat textureFormat) {
        return switch (textureFormat) {
            case RGBA8 -> VK10.VK_FORMAT_R8G8B8A8_UNORM;
            case RED8 -> VK10.VK_FORMAT_R8_UNORM;
            case RED8I -> VK10.VK_FORMAT_R8_SINT;
            case DEPTH32 -> VK10.VK_FORMAT_D32_SFLOAT;
        };
    }

    public static int vkImageViewType(int usage) {
        int viewType;
        if ((usage & GpuTexture.USAGE_CUBEMAP_COMPATIBLE) != 0) {
            viewType = VK10.VK_IMAGE_VIEW_TYPE_CUBE;
        }
        else {
            viewType = VK10.VK_IMAGE_VIEW_TYPE_2D;
        }

        return viewType;
    }
}

