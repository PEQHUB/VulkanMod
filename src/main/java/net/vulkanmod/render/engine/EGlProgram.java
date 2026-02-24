package net.vulkanmod.render.engine;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.opengl.*;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.class_284;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.shader.descriptor.UBO;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class EGlProgram {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static Set<String> BUILT_IN_UNIFORMS = Sets.<String>newHashSet("Projection", "Lighting", "Fog", "Globals");
    public static EGlProgram INVALID_PROGRAM = new EGlProgram(-1, "invalid");
    private final Map<String, class_284> uniformsByName = new HashMap();
    private final int programId;
    private final String debugLabel;

    public EGlProgram(int i, String string) {
        this.programId = i;
        this.debugLabel = string;
    }

    public void setupUniforms(Pipeline pipeline, List<RenderPipeline.UniformDescription> uniformDescriptions, List<String> samplers) {
        int i = 0;
        int j = 0;

        for (RenderPipeline.UniformDescription uniformDescription : uniformDescriptions) {
            String name = uniformDescription.name();

            class_284 uniform = switch (uniformDescription.type()) {
                case field_60031 -> {
                    UBO ubo = pipeline.getUBO(name);

                    if (ubo == null) {
                        yield null;
                    }

                    int binding = ubo.binding;
                    yield new class_284.class_11272(binding);
                }
                case field_60032 -> {
                    int binding = i++;
                    yield new class_284.class_11273(binding, 0, Objects.requireNonNull(uniformDescription.textureFormat()));
                }
            };

            this.uniformsByName.put(name, uniform);
        }

        for (String samplerName : samplers) {
            var imageDescriptor = pipeline.getImageDescriptor(samplerName);
            int binding = imageDescriptor.getBinding();
            int imageIdx = imageDescriptor.imageIdx;
            this.uniformsByName.put(samplerName, new class_284.class_11271(binding, imageIdx));
        }

    }

    @Nullable
    public class_284 getUniform(String string) {
        RenderSystem.assertOnRenderThread();
        return this.uniformsByName.get(string);
    }

    public int getProgramId() {
        return this.programId;
    }

    public String toString() {
        return this.debugLabel;
    }

    public String getDebugLabel() {
        return this.debugLabel;
    }

    public Map<String, class_284> getUniforms() {
        return this.uniformsByName;
    }

}
