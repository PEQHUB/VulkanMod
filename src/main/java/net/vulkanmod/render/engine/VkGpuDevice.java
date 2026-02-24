package net.vulkanmod.render.engine;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.*;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.*;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10141;
import net.minecraft.class_10149;
import net.minecraft.class_10867;
import net.minecraft.class_10868;
import net.minecraft.class_12137;
import net.minecraft.class_12289;
import net.minecraft.class_2960;
import net.minecraft.class_5913;
import net.minecraft.class_5944;
import net.vulkanmod.Initializer;
import net.vulkanmod.gl.VkGlTexture;
import net.vulkanmod.interfaces.shader.ExtendedRenderPipeline;
import net.vulkanmod.render.shader.ShaderLoadUtil;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.shader.converter.GLSLParser;
import net.vulkanmod.vulkan.shader.converter.Lexer;
import net.vulkanmod.vulkan.shader.descriptor.UBO;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VK10;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@SuppressWarnings("NullableProblems")
public class VkGpuDevice implements GpuDevice {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final VkCommandEncoder encoder;
    private final VkDebugLabel debugLabels;
    private final int maxSupportedTextureSize;
    private final int uniformOffsetAlignment;
    private final class_12289 defaultShaderSource;
    private final Map<RenderPipeline, class_10867> pipelineCache = new IdentityHashMap<>();
    private final Map<ShaderCompilationKey, class_10141> shaderCache = new HashMap<>();
    private final Set<String> enabledExtensions = new HashSet<>();

    private final Map<ShaderCompilationKey, String> shaderSrcCache = new HashMap<>();

    public VkGpuDevice(long l, int i, boolean bl, class_12289 shaderSource, boolean bl2) {
        this.debugLabels = VkDebugLabel.create(bl2, this.enabledExtensions);
        this.maxSupportedTextureSize = VRenderSystem.maxSupportedTextureSize();
        this.uniformOffsetAlignment = (int) DeviceManager.deviceProperties.limits().minUniformBufferOffsetAlignment();
        this.defaultShaderSource = shaderSource;

        this.encoder = new VkCommandEncoder(this);
    }

    public VkDebugLabel debugLabels() {
        return this.debugLabels;
    }

    @Override
    public CommandEncoder createCommandEncoder() {
        return this.encoder;
    }

    @Override
    public class_12137 createSampler(AddressMode addressMode, AddressMode addressMode2, FilterMode filterMode,
                                    FilterMode filterMode2, int maxAnisotropy, OptionalDouble maxLod) {
        return new VkSampler(addressMode, addressMode2, filterMode, filterMode2, maxAnisotropy, maxLod);
    }

    @Override
    public GpuTexture createTexture(@Nullable Supplier<String> supplier, int usage, TextureFormat textureFormat, int width, int height, int layers, int mipLevels) {
        return this.createTexture(this.debugLabels.exists() && supplier != null ? supplier.get() : null, usage, textureFormat, width, height, layers, mipLevels);
    }

    @Override
    public GpuTexture createTexture(@Nullable String string, int usage, TextureFormat textureFormat, int width, int height, int layers, int mipLevels) {
        if (mipLevels < 1) {
            throw new IllegalArgumentException("mipLevels must be at least 1");
        } else {
            int id = VkGlTexture.genTextureId();
            if (string == null) {
                string = String.valueOf(id);
            }

            int format = VkGpuTexture.vkFormat(textureFormat);
            int viewType = VkGpuTexture.vkImageViewType(usage);
            boolean depthFormat = VulkanImage.isDepthFormat(format);
            int attachmentUsage = depthFormat ? VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT : VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;

            VulkanImage texture = VulkanImage.builder(width, height)
                                             .setName(string)
                                             .setFormat(format)
                                             .setArrayLayers(layers)
                                             .setMipLevels(mipLevels)
                                             .addUsage(attachmentUsage)
                                             .setViewType(viewType)
                                             .createVulkanImage();

            VkGlTexture vGlTexture = VkGlTexture.getTexture(id);
            vGlTexture.setVulkanImage(texture);
            VkGlTexture.bindTexture(id);

            VkGpuTexture glTexture = new VkGpuTexture(usage, string, textureFormat, width, height, layers, mipLevels, id, vGlTexture);
            this.debugLabels.applyLabel(glTexture);
            return glTexture;
        }
    }

    public VkGpuTexture gpuTextureFromVulkanImage(VulkanImage image) {
        int id = VkGlTexture.genTextureId();
        VkGlTexture glTexture = VkGlTexture.getTexture(id);
        glTexture.setVulkanImage(image);
        TextureFormat textureFormat = VkGpuTexture.textureFormat(image.format);
        VkGpuTexture gpuTexture = new VkGpuTexture(0, image.name, textureFormat, image.width, image.height, 1, image.mipLevels, id, glTexture);
        this.debugLabels.applyLabel(gpuTexture);
        return gpuTexture;
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture gpuTexture) {
        return this.createTextureView(gpuTexture, 0, gpuTexture.getMipLevels());
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture gpuTexture, int startLevel, int levels) {
        if (gpuTexture.isClosed()) {
            throw new IllegalArgumentException("Can't create texture view with closed texture");
        } else if (startLevel >= 0 && startLevel + levels <= gpuTexture.getMipLevels()) {

            // Try to convert gpuTexture to VkGpuTexture in case it's not
            if (gpuTexture.getClass() != VkGpuTexture.class) {
                gpuTexture = VkGpuTexture.fromGlTexture((class_10868) gpuTexture);
            }

            return new VkTextureView((VkGpuTexture) gpuTexture, startLevel, levels);
        } else {
            throw new IllegalArgumentException(
                    levels + " mip levels starting from " + startLevel + " would be out of range for texture with only " + gpuTexture.getMipLevels() + " mip levels"
            );
        }
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> supplier, @GpuBuffer.class_12113 int usage, long size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size must be greater than zero");
        } else {
            return new VkGpuBuffer(this.debugLabels, supplier, usage, size);
        }
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> supplier, int usage, ByteBuffer byteBuffer) {
        if (!byteBuffer.hasRemaining()) {
            throw new IllegalArgumentException("Buffer source must not be empty");
        } else {
            VkGpuBuffer glBuffer = new VkGpuBuffer(this.debugLabels, supplier, usage, byteBuffer.remaining());
            this.encoder.writeToBuffer(glBuffer.slice(), byteBuffer);
            return glBuffer;
        }
    }

    @Override
    public String getImplementationInformation() {
        return "Vulkan " + Vulkan.getDevice().vkVersion + ", " + Vulkan.getDevice().vendorIdString;
    }

    @Override
    public List<String> getLastDebugMessages() {
        return Collections.emptyList();
    }

    @Override
    public boolean isDebuggingEnabled() {
        return false;
    }

    @Override
    public String getRenderer() {
        return "VulkanMod %s".formatted(Initializer.getVersion()) ;
    }

    @Override
    public String getVendor() {
        return Vulkan.getDevice().vendorIdString;
    }

    @Override
    public String getBackendName() {
        return "Vulkan";
    }

    @Override
    public String getVersion() {
        return Vulkan.getDevice().vkVersion;
    }

    private static int getMaxSupportedTextureSize() {
        int i = GlStateManager._getInteger(3379);

        for (int j = Math.max(32768, i); j >= 1024; j >>= 1) {
            GlStateManager._texImage2D(32868, 0, 6408, j, j, 0, 6408, 5121, null);
            int k = GlStateManager._getTexLevelParameter(32868, 0, 4096);
            if (k != 0) {
                return j;
            }
        }

        int jx = Math.max(i, 1024);
        LOGGER.info("Failed to determine maximum texture size by probing, trying GL_MAX_TEXTURE_SIZE = {}", jx);
        return jx;
    }

    @Override
    public int getMaxTextureSize() {
        return this.maxSupportedTextureSize;
    }

    @Override
    public int getUniformOffsetAlignment() {
        return this.uniformOffsetAlignment;
    }

    @Override
    public void clearPipelineCache() {
        for (class_10867 glRenderPipeline : this.pipelineCache.values()) {
            if (glRenderPipeline.comp_3802() != class_5944.field_57864) {
                glRenderPipeline.comp_3802().close();
            }
        }

        this.pipelineCache.clear();

        for (class_10141 glShaderModule : this.shaderCache.values()) {
            if (glShaderModule != class_10141.field_57880) {
                glShaderModule.close();
            }
        }

        this.shaderCache.clear();
    }

    @Override
    public List<String> getEnabledExtensions() {
        return new ArrayList(this.enabledExtensions);
    }

    @Override
    public int getMaxSupportedAnisotropy() {
        // TODO query max supported anisotropy
        return 16;
    }

    @Override
    public void close() {
        this.clearPipelineCache();
    }

    protected class_10141 getOrCompileShader(
            class_2960 resourceLocation, ShaderType shaderType, class_10149 shaderDefines, BiFunction<class_2960, ShaderType, String> biFunction
    ) {
        ShaderCompilationKey shaderCompilationKey = new ShaderCompilationKey(resourceLocation, shaderType, shaderDefines);
        return this.shaderCache.computeIfAbsent(shaderCompilationKey, shaderCompilationKey2 -> this.compileShader(shaderCompilationKey, biFunction));
    }

    protected String getCachedShaderSrc(class_2960 resourceLocation, ShaderType shaderType, class_10149 shaderDefines, class_12289 shaderSourceGetter) {
        ShaderCompilationKey shaderCompilationKey = new ShaderCompilationKey(resourceLocation, shaderType, shaderDefines);

        return this.shaderSrcCache.computeIfAbsent(shaderCompilationKey, compilationKey -> {
            String shaderExtension = switch (shaderType) {
                case VERTEX -> ".vsh";
                case FRAGMENT -> ".fsh";
            };

            String shaderName = resourceLocation.method_12832() + shaderExtension;

            if (ShaderLoadUtil.REMAPPED_SHADERS.contains(shaderName)) {
                String src = ShaderLoadUtil.getShaderSource(resourceLocation, shaderType);

                if (src == null) {
                    throw new RuntimeException("shader: (%s) not found.".formatted(src));
                }

                return src;
            }

            return shaderSourceGetter.get(compilationKey.id, compilationKey.type);
        });
    }

    public CompiledRenderPipeline precompilePipeline(RenderPipeline renderPipeline, @Nullable class_12289 shaderSourceGetter) {
        shaderSourceGetter = shaderSourceGetter == null ? this.defaultShaderSource : shaderSourceGetter;

        try {
            compilePipeline(renderPipeline, shaderSourceGetter);
        } catch (Exception e) {
            throw new RuntimeException("Caught exception compiling pipeline: %s".formatted(renderPipeline.toString()), e);
        }


        return new VkRenderPipeline(renderPipeline);
    }

    public void compilePipeline(RenderPipeline renderPipeline) {
        this.compilePipeline(renderPipeline, this.defaultShaderSource);
    }

    private class_10141 compileShader(ShaderCompilationKey shaderCompilationKey, BiFunction<class_2960, ShaderType, String> biFunction) {
        String string = biFunction.apply(shaderCompilationKey.id, shaderCompilationKey.type);
        if (string == null) {
            LOGGER.error("Couldn't find source for {} shader ({})", shaderCompilationKey.type, shaderCompilationKey.id);
            return class_10141.field_57880;
        } else {
            String string2 = class_5913.method_62880(string, shaderCompilationKey.defines);
            int i = GlStateManager.glCreateShader(GlConst.toGl(shaderCompilationKey.type));
            GlStateManager.glShaderSource(i, string2);
            GlStateManager.glCompileShader(i);
            if (GlStateManager.glGetShaderi(i, 35713) == 0) {
                String string3 = StringUtils.trim(GlStateManager.glGetShaderInfoLog(i, 32768));
                LOGGER.error("Couldn't compile {} shader ({}): {}", shaderCompilationKey.type.getName(), shaderCompilationKey.id, string3);
                return class_10141.field_57880;
            } else {
                class_10141 glShaderModule = new class_10141(i, shaderCompilationKey.id, shaderCompilationKey.type);
                this.debugLabels.applyLabel(glShaderModule);
                return glShaderModule;
            }
        }
    }

    private void compilePipeline(RenderPipeline renderPipeline, class_12289 shaderSrcGetter) {
        String locationPath = renderPipeline.getLocation().method_12832();

        String configName;
        if (locationPath.contains("core")) {
            configName = locationPath.split("/")[1];
        } else {
            configName = locationPath;
        }

        Pipeline.Builder builder = new Pipeline.Builder(renderPipeline.getVertexFormat(), configName);
        GraphicsPipeline pipeline;
        ExtendedRenderPipeline extPipeline = ExtendedRenderPipeline.of(renderPipeline);

        class_2960 vertexShaderLocation = renderPipeline.getVertexShader();
        class_2960 fragmentShaderLocation = renderPipeline.getFragmentShader();

        class_10149 shaderDefines = renderPipeline.getShaderDefines();

        String vshSrc = this.getCachedShaderSrc(vertexShaderLocation, ShaderType.VERTEX, shaderDefines, shaderSrcGetter);
        String fshSrc = this.getCachedShaderSrc(fragmentShaderLocation, ShaderType.FRAGMENT, shaderDefines, shaderSrcGetter);

        vshSrc = class_5913.method_62880(vshSrc, shaderDefines);
        fshSrc = class_5913.method_62880(fshSrc, shaderDefines);

        Lexer lexer = new Lexer(vshSrc);
        GLSLParser parser = new GLSLParser();
        parser.setVertexFormat(renderPipeline.getVertexFormat());

        try {
            parser.parse(lexer, GLSLParser.Stage.VERTEX);

            lexer = new Lexer(fshSrc);
            parser.parse(lexer, GLSLParser.Stage.FRAGMENT);
        } catch (Exception e) {
            throw new RuntimeException("Caught exception while parsing: %s".formatted(renderPipeline.toString()), e);
        }

        UBO[] ubos = parser.createUBOs();

        String vshProcessed = parser.getOutput(GLSLParser.Stage.VERTEX);
        String fshProcessed = parser.getOutput(GLSLParser.Stage.FRAGMENT);

        builder.setUniforms(List.of(ubos), parser.getSamplerList());
        builder.compileShaders(configName, vshProcessed, fshProcessed);

        try {
            pipeline = builder.createGraphicsPipeline();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Exception while compiling pipeline %s".formatted(renderPipeline));
        }

        EGlProgram eGlProgram = new EGlProgram(1, configName);
        eGlProgram.setupUniforms(pipeline, renderPipeline.getUniforms(), renderPipeline.getSamplers());
        extPipeline.setProgram(eGlProgram);

        extPipeline.setPipeline(pipeline);
    }

    @Environment(EnvType.CLIENT)
    record ShaderCompilationKey(class_2960 id, ShaderType type, class_10149 defines) {

        public String toString() {
            String string = this.id + " (" + this.type + ")";
            return !this.defines.method_62930() ? string + " with " + this.defines : string;
        }
    }

    private static class VkRenderPipeline implements CompiledRenderPipeline {
        final RenderPipeline renderPipeline;

        public VkRenderPipeline(RenderPipeline renderPipeline) {
            this.renderPipeline = renderPipeline;
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }
}
