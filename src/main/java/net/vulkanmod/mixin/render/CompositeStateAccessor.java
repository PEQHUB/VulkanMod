package net.vulkanmod.mixin.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSetup.class)
public interface CompositeStateAccessor {

    @Accessor("pipeline")
    RenderPipeline getPipeline();

    @Accessor("textureTransform")
    TextureTransform getTextureTransform();

    @Accessor("outputTarget")
    OutputTarget getOutputTarget();

    @Accessor("outlineProperty")
    RenderSetup.OutlineProperty getOutlineProperty();
}
