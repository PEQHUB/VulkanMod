package net.vulkanmod.mixin.render.vertex;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5611;
import net.minecraft.class_765;
import net.minecraft.class_777;
import net.vulkanmod.interfaces.ExtendedVertexBuilder;
import net.vulkanmod.mixin.matrix.PoseAccessor;
import net.vulkanmod.render.util.MathUtil;
import net.vulkanmod.render.vertex.format.I32_SNorm;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.*;

@Mixin(class_287.class)
public abstract class BufferBuilderM
        implements class_4588, ExtendedVertexBuilder {

    @Shadow private boolean fastFormat;
    @Shadow private boolean fullFormat;
    @Shadow private VertexFormat format;

    @Shadow protected abstract long beginVertex();

    @Shadow private int elementsToFill;
    @Shadow @Final private int initialElementsToFill;

    @Shadow protected abstract long beginElement(VertexFormatElement vertexFormatElement);

    private long ptr;

    public void vertex(float x, float y, float z, int packedColor, float u, float v, int overlay, int light, int packedNormal) {
        this.ptr = this.beginVertex();

        if (this.format == class_290.field_1580) {
            MemoryUtil.memPutFloat(ptr + 0, x);
            MemoryUtil.memPutFloat(ptr + 4, y);
            MemoryUtil.memPutFloat(ptr + 8, z);

            MemoryUtil.memPutInt(ptr + 12, packedColor);

            MemoryUtil.memPutFloat(ptr + 16, u);
            MemoryUtil.memPutFloat(ptr + 20, v);

            MemoryUtil.memPutInt(ptr + 24, overlay);

            MemoryUtil.memPutInt(ptr + 28, light);
            MemoryUtil.memPutInt(ptr + 32, packedNormal);

        }
        else {
            this.elementsToFill = this.initialElementsToFill;

            this.position(x, y, z);
            this.fastColor(packedColor);
            this.fastUv(u, v);
            this.fastOverlay(overlay);
            this.light(light);
            this.fastNormal(packedNormal);

//            throw new RuntimeException("unaccepted format: " + this.format);
        }

    }

    public void vertex(float x, float y, float z, float u, float v, int packedColor, int light) {
        this.ptr = this.beginVertex();

        MemoryUtil.memPutFloat(ptr + 0, x);
        MemoryUtil.memPutFloat(ptr + 4, y);
        MemoryUtil.memPutFloat(ptr + 8, z);

        MemoryUtil.memPutFloat(ptr + 12, u);
        MemoryUtil.memPutFloat(ptr + 16, v);

        MemoryUtil.memPutInt(ptr + 20, packedColor);

        MemoryUtil.memPutInt(ptr + 24, light);
    }

    public void position(float x, float y, float z) {
        MemoryUtil.memPutFloat(ptr + 0, x);
        MemoryUtil.memPutFloat(ptr + 4, y);
        MemoryUtil.memPutFloat(ptr + 8, z);
    }

    public void fastColor(int packedColor) {
        long ptr = this.beginElement(VertexFormatElement.COLOR);
        if (ptr != -1L) {
            MemoryUtil.memPutInt(ptr, packedColor);
        }
    }

    public void fastUv(float u, float v) {
        long ptr = this.beginElement(VertexFormatElement.UV0);
        if (ptr != -1L) {
            MemoryUtil.memPutFloat(ptr, u);
            MemoryUtil.memPutFloat(ptr + 4, v);
        }
    }

    public void fastOverlay(int o) {
        long ptr = this.beginElement(VertexFormatElement.UV1);
        if (ptr != -1L) {
            MemoryUtil.memPutInt(ptr, o);
        }
    }

    public void light(int l) {
        long ptr = this.beginElement(VertexFormatElement.UV2);
        if (ptr != -1L) {
            MemoryUtil.memPutInt(ptr, l);
        }
    }

    public void fastNormal(int packedNormal) {
        long ptr = this.beginElement(VertexFormatElement.NORMAL);
        if (ptr != -1L) {
            MemoryUtil.memPutInt(ptr, packedNormal);
        }
    }

    /**
     * @author
     */
    @Overwrite
    public void method_23919(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
        if (this.fastFormat) {
            long ptr = this.beginVertex();
            MemoryUtil.memPutFloat(ptr + 0, x);
            MemoryUtil.memPutFloat(ptr + 4, y);
            MemoryUtil.memPutFloat(ptr + 8, z);

            MemoryUtil.memPutInt(ptr + 12, color);

            MemoryUtil.memPutFloat(ptr + 16, u);
            MemoryUtil.memPutFloat(ptr + 20, v);

            byte i;
            if (this.fullFormat) {
                MemoryUtil.memPutInt(ptr + 24, overlay);
                i = 28;
            } else {
                i = 24;
            }

            MemoryUtil.memPutInt(ptr + i, light);

            int temp = I32_SNorm.packNormal(normalX, normalY, normalZ);
            MemoryUtil.memPutInt(ptr + i + 4, temp);
        } else {
            class_4588.super.method_23919(x, y, z, color, u, v, overlay, light, normalX, normalY, normalZ);
        }
    }

    @Override
    public class_4588 method_1336(int r, int g, int b, int a) {
        long m = this.beginElement(VertexFormatElement.COLOR);
        if (m != -1L) {
            int color = packRgba(r, g, b, a);
            MemoryUtil.memPutInt(m, color);
        }

        return this;
    }

    private final float[] brightness = new float[4];
    private final int[] lights = new int[4];

    @Override
    public void method_22919(class_4587.class_4665 pose, class_777 bakedQuad, float r, float g, float b, float a, int light, int overlay) {
        brightness[0] = 1.0f; brightness[1] = 1.0f; brightness[2] = 1.0f; brightness[3] = 1.0f;
        lights[0] = light; lights[1] = light; lights[2] = light; lights[3] = light;

        this.method_22920(pose, bakedQuad, brightness, r, g, b, a, lights, overlay);
    }

    @Override
    public void method_22920(class_4587.class_4665 pose, class_777 bakedQuad, float[] brightness, float r, float g, float b, float a,
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
            this.vertex(tx, ty, tz, color, u, v, overlay, light, packedNormal);
        }
    }

    private static int packRgba(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (b & 0xFF) << 16 | (g & 0xFF) << 8 | (r & 0xFF);
    }

}
