package net.vulkanmod.mixin.vertex;

import net.minecraft.class_2350;
import net.minecraft.class_4583;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.vulkanmod.interfaces.ExtendedVertexBuilder;
import net.vulkanmod.render.vertex.format.I32_SNorm;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class VertexMultiConsumersM {

    @Mixin(targets = "com/mojang/blaze3d/vertex/VertexMultiConsumer$Double")
    public static class DoubleM implements ExtendedVertexBuilder {
        @Shadow @Final private class_4588 first;
        @Shadow @Final private class_4588 second;

        @Unique
        private ExtendedVertexBuilder firstExt;
        @Unique
        private ExtendedVertexBuilder secondExt;

        @Unique
        private boolean canUseFastVertex = false;

        @Override
        public boolean canUseFastVertex() {
            return this.canUseFastVertex;
        }

        @Inject(method = "<init>", at = @At("RETURN"))
        private void checkDelegates(class_4588 vertexConsumer, class_4588 vertexConsumer2, CallbackInfo ci) {
            this.canUseFastVertex = (ExtendedVertexBuilder.of(this.first) != null)
                    && (ExtendedVertexBuilder.of(this.second) != null);

            if (this.canUseFastVertex) {
                this.firstExt = ExtendedVertexBuilder.of(this.first);
                this.secondExt = ExtendedVertexBuilder.of(this.second);
            }
        }

        @Override
        public void vertex(float x, float y, float z, int packedColor, float u, float v, int overlay, int light, int packedNormal) {
            this.firstExt.vertex(x, y, z, packedColor, u, v, overlay, light, packedNormal);
            this.secondExt.vertex(x, y, z, packedColor, u, v, overlay, light, packedNormal);
        }
    }

    @Mixin(targets = "com/mojang/blaze3d/vertex/VertexMultiConsumer$Multiple")
    public static class MultipleM implements ExtendedVertexBuilder {
        @Shadow @Final private class_4588[] delegates;

        @Unique
        private boolean canUseFastVertex = false;

        @Override
        public boolean canUseFastVertex() {
            return this.canUseFastVertex;
        }

        @Inject(method = "<init>", at = @At("RETURN"))
        private void checkDelegates(class_4588[] vertexConsumers, CallbackInfo ci) {
            for (class_4588 delegate : this.delegates) {
                if (ExtendedVertexBuilder.of(delegate) == null) {
                    this.canUseFastVertex = false;
                    return;
                }
            }

            this.canUseFastVertex = true;
        }

        @Override
        public void vertex(float x, float y, float z, int packedColor, float u, float v, int overlay, int light, int packedNormal) {
            for (class_4588 vertexConsumer : this.delegates) {
                ExtendedVertexBuilder extendedVertexBuilder = (ExtendedVertexBuilder) vertexConsumer;

                extendedVertexBuilder.vertex(x, y, z, packedColor, u, v, overlay, light, packedNormal);
            }
        }
    }

    @Mixin(class_4583.class)
    public static abstract class SheetDecalM implements ExtendedVertexBuilder {
        @Shadow @Final private class_4588 delegate;
        @Shadow @Final private Matrix3f normalInversePose;
        @Shadow @Final private Matrix4f cameraInversePose;
        @Shadow @Final private float textureScale;

        @Unique
        private boolean canUseFastVertex = false;

        private Vector3f normal = new Vector3f();
        private Vector4f position = new Vector4f();

        @Override
        public boolean canUseFastVertex() {
            return this.canUseFastVertex;
        }

        @Inject(method = "<init>", at = @At("RETURN"))
        private void checkDelegates(class_4588 vertexConsumer, class_4587.class_4665 pose, float f, CallbackInfo ci) {
            this.canUseFastVertex = (ExtendedVertexBuilder.of(this.delegate) != null);
        }

        @Override
        public void vertex(float x, float y, float z, int packedColor, float u, float v, int overlay, int light, int packedNormal) {
            float nx = I32_SNorm.unpackX(packedNormal);
            float ny = I32_SNorm.unpackY(packedNormal);
            float nz = I32_SNorm.unpackZ(packedNormal);

            normal.set(nx, ny, nz);
            position.set(x, y , z, 1.0f);

            this.normalInversePose.transform(normal);
            class_2350 direction = class_2350.method_10147(normal.x(), normal.y(), normal.z());
            this.cameraInversePose.transform(position);
            position.rotateY(3.1415927F);
            position.rotateX(-1.5707964F);
            position.rotate(direction.method_23224());
            float f = -position.x() * this.textureScale;
            float g = -position.y() * this.textureScale;

            final int color = 0xFFFFFFFF;
            this.delegate.method_23919(x, y, z, color, f, g, overlay, light, nx, ny, nz);
        }
    }
}
