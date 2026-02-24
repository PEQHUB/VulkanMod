package net.vulkanmod.mixin.render.particle;

import net.minecraft.class_11940;
import net.minecraft.class_4604;
import net.vulkanmod.render.chunk.RenderSection;
import net.vulkanmod.render.chunk.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(class_11940.class)
public class QuadParticleGroupM {

    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/Frustum;pointInFrustum(DDD)Z"))
    private boolean particleWithinSections(class_4604 instance, double x, double y, double z) {
        return !cull(WorldRenderer.getInstance(), x, y, z) && instance.method_74404(x,y, z);
    }

    @Unique
    private static boolean cull(WorldRenderer worldRenderer, double x, double y, double z) {
        RenderSection section = worldRenderer.getSectionGrid().getSectionAtBlockPos((int) x, (int) y, (int) z);
        return section != null && section.getLastFrame() != worldRenderer.getLastFrame();
    }
}
