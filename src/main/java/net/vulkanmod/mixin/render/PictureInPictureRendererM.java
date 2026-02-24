package net.vulkanmod.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.class_10799;
import net.minecraft.class_11231;
import net.minecraft.class_11239;
import net.minecraft.class_11241;
import net.minecraft.class_11246;
import net.minecraft.class_11256;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(class_11239.class)
public class PictureInPictureRendererM<T extends class_11256> {

    @Shadow
    private @Nullable GpuTextureView textureView;

    @Overwrite
    public void blitTexture(T pictureInPictureRenderState, class_11246 guiRenderState) {
        guiRenderState.method_71996(
                new class_11241(
                        class_10799.field_59968,
                        class_11231.method_70900(this.textureView, RenderSystem.getSamplerCache().method_75297(FilterMode.NEAREST)),
                        pictureInPictureRenderState.method_72127(),
                        pictureInPictureRenderState.comp_4122(),
                        pictureInPictureRenderState.comp_4123(),
                        pictureInPictureRenderState.comp_4124(),
                        pictureInPictureRenderState.comp_4125(),
                        0.0F,
                        1.0F,
                        0.0F,
                        1.0F,
                        -1,
                        pictureInPictureRenderState.comp_4128(),
                        null
                )
        );
    }
}
