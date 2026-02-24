package net.vulkanmod.mixin.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.class_12245;
import net.minecraft.class_12246;
import net.minecraft.class_12247;
import net.minecraft.class_12250;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_12247.class)
public interface RenderSetupAccessor {

    @Accessor("pipeline")
    RenderPipeline pipeline();

    @Accessor("layeringTransform")
    class_12245 layeringTransform();

    @Accessor("outputTarget")
    class_12246 outputTarget();

    @Accessor("textureTransform")
    class_12250 textureTransform();
}
