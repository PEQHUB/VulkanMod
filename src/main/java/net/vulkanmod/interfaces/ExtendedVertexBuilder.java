package net.vulkanmod.interfaces;

import net.minecraft.class_4588;

public interface ExtendedVertexBuilder {

    static ExtendedVertexBuilder of(class_4588 vertexConsumer) {
        if (vertexConsumer instanceof ExtendedVertexBuilder) {
            return (ExtendedVertexBuilder) vertexConsumer;
        }

        return null;
    }

    default boolean canUseFastVertex() {
        return true;
    }

    void vertex(float x, float y, float z, int packedColor, float u, float v, int overlay, int light, int packedNormal);

    // Used for particles
    default void vertex(float x, float y, float z, float u, float v, int packedColor, int light) {}
}
