package net.vulkanmod.config.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.class_11231;
import net.minecraft.class_11244;
import net.minecraft.class_4588;
import net.minecraft.class_8030;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record PolygonRenderState (
        RenderPipeline pipeline,
        class_11231 textureSetup,
        Matrix3x2f pose,
        float[][] vertices,
        int col,
        @Nullable class_8030 scissorArea,
        @Nullable class_8030 bounds
) implements class_11244 {

    public PolygonRenderState(
            RenderPipeline renderPipeline,
            class_11231 textureSetup,
            Matrix3x2f pose,
            float[][] vertices,
            int color,
            @Nullable class_8030 screenRectangle
    ) {
        this(renderPipeline, textureSetup, pose, vertices, color, screenRectangle,
             getBounds(vertices, pose, screenRectangle));
    }

    @Override
    public void method_70917(class_4588 vertexConsumer) {
        for (float[] vertex : vertices) {
            float x = vertex[0];
            float y = vertex[1];
            vertexConsumer.method_70815(this.pose(), x, y)
                          .method_39415(this.col);
        }
    }

    @Nullable
    private static class_8030 getBounds(float[][] vertices, Matrix3x2f matrix3x2f, @Nullable class_8030 screenRectangle) {
        float x0 = vertices[0][0];
        float x1 = vertices[0][0];
        float y0 = vertices[0][1];
        float y1 = vertices[0][1];

        for (float[] vertex : vertices) {
            float x = vertex[0];
            float y = vertex[1];

            if (x < x0) {
                x0 = x;
            }
            if (x > x1) {
                x1 = x;
            }

            if (y < y0) {
                y0 = y;
            }
            if (y > y1) {
                y1 = y;
            }
        }

        class_8030 screenRectangle2 = new class_8030((int) x0, (int) y0, (int) (x1 - x0), (int) (y1 - y0)).method_71523(matrix3x2f);
        return screenRectangle != null ? screenRectangle.method_49701(screenRectangle2) : screenRectangle2;
    }
}

