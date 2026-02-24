package net.vulkanmod.mixin.render.vertex;

import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5611;
import net.minecraft.class_765;
import net.minecraft.class_777;
import net.vulkanmod.mixin.matrix.PoseAccessor;
import net.vulkanmod.render.util.MathUtil;
import net.vulkanmod.render.vertex.format.I32_SNorm;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(class_4588.class)
public interface VertexConsumerM {

    @Shadow void addVertex(float f, float g, float h, int i, float j, float k, int l, int m, float n, float o,
                   float p);

    @Overwrite
    default void putBulkData(class_4587.class_4665 pose, class_777 bakedQuad, float[] brightness, float r, float g, float b, float a,
                            int[] lights, int overlay) {
        Vector3fc vector3fc = bakedQuad.comp_3723().method_68072();
        Matrix4f matrix4f = pose.method_23761();
        boolean trustedNormals = ((PoseAccessor)(Object)pose).trustedNormals();
        int packedNormal = MathUtil.packTransformedNorm(pose.method_23762(), trustedNormals, vector3fc.x(), vector3fc.y(), vector3fc.z());

        int lightEmission = bakedQuad.comp_3726();

        for (int l = 0; l < 4; l++) {
            Vector3fc quadPos = bakedQuad.method_76648(l);
            long packedUV = bakedQuad.method_76649(l);
            float br = brightness[l];
            int color = ColorUtil.RGBA.pack(r * br, g * br, b * br, a);
            int light = class_765.method_62228(lights[l], lightEmission);

            float x = quadPos.x();
            float y = quadPos.y();
            float z = quadPos.z();
            float tx = MathUtil.transformX(matrix4f, x, y, z);
            float ty = MathUtil.transformY(matrix4f, x, y, z);
            float tz = MathUtil.transformZ(matrix4f, x, y, z);

            float u = class_5611.method_76641(packedUV);
            float v = class_5611.method_76642(packedUV);
            this.addVertex(tx, ty, tz, color, u, v, overlay, light, I32_SNorm.unpackX(packedNormal), I32_SNorm.unpackY(packedNormal), I32_SNorm.unpackZ(packedNormal));
        }
    }
}
