package net.vulkanmod.mixin.vertex;

import net.minecraft.class_4588;
import net.minecraft.class_4618;
import net.vulkanmod.interfaces.ExtendedVertexBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_4618.class_4586.class)
public class EntityOutlineGeneratorM implements ExtendedVertexBuilder {

    private ExtendedVertexBuilder extDelegate;
    private boolean canUseFastVertex = false;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void getExtBuilder(class_4588 vertexConsumer, int i, CallbackInfo ci) {
        if (vertexConsumer instanceof ExtendedVertexBuilder) {
            this.extDelegate = (ExtendedVertexBuilder) vertexConsumer;
            this.canUseFastVertex = true;
        }
    }

    @Override
    public boolean canUseFastVertex() {
        return this.canUseFastVertex;
    }

    @Override
    public void vertex(float x, float y, float z, int packedColor, float u, float v, int overlay, int light, int packedNormal) {
        this.extDelegate.vertex(x, y, z, packedColor, u, v, overlay, light, packedNormal);
    }
}
