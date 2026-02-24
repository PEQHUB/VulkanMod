package net.vulkanmod.render.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_9801;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.texture.VTextureSelector;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class DrawUtil {

    public static void blitQuad() {
        blitQuad(0.0f, 1.0f, 1.0f, 0.0f);
    }

    public static void drawTexQuad(class_287 builder, float x0, float y0, float x1, float y1, float z,
                                   float u0, float v0, float u1, float v1) {
        class_289 tesselator = class_289.method_1348();
        class_287 bufferBuilder = tesselator.method_60827(VertexFormat.class_5596.field_27382, class_290.field_1585);
        bufferBuilder.method_22912(x0, y0, z).method_22913(0.0F, 1.0F);
        bufferBuilder.method_22912(x1, y0, z).method_22913(1.0F, 1.0F);
        bufferBuilder.method_22912(x1, y1, z).method_22913(1.0F, 0.0F);
        bufferBuilder.method_22912(x0, y1, z).method_22913(0.0F, 0.0F);

        class_9801 meshData = bufferBuilder.method_60800();

        Renderer.getDrawer().draw(meshData.method_60818(), VertexFormat.class_5596.field_27382, meshData.method_60822().comp_749(), meshData.method_60822().comp_750());

    }

    public static void blitQuad(float x0, float y0, float x1, float y1) {
        class_289 tesselator = class_289.method_1348();
        class_287 bufferBuilder = tesselator.method_60827(VertexFormat.class_5596.field_27382, class_290.field_1585);
        bufferBuilder.method_22912(x0, y0, 0.0f).method_22913(0.0F, 1.0F);
        bufferBuilder.method_22912(x1, y0, 0.0f).method_22913(1.0F, 1.0F);
        bufferBuilder.method_22912(x1, y1, 0.0f).method_22913(1.0F, 0.0F);
        bufferBuilder.method_22912(x0, y1, 0.0f).method_22913(0.0F, 0.0F);

        class_9801 meshData = bufferBuilder.method_60800();

        Renderer.getDrawer().draw(meshData.method_60818(), VertexFormat.class_5596.field_27382, meshData.method_60822().comp_749(), meshData.method_60822().comp_750());

    }

    public static void drawFramebuffer(GraphicsPipeline pipeline, VulkanImage attachment) {
        Renderer.getInstance().bindGraphicsPipeline(pipeline);
        VTextureSelector.bindTexture(attachment);

        Matrix4f projection = new Matrix4f().setOrtho(0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, true);
        Matrix4fStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushMatrix();
        poseStack.identity();
        VRenderSystem.applyMVP(poseStack, projection);
        poseStack.popMatrix();

        Renderer.getInstance().uploadAndBindUBOs(pipeline);

        blitQuad(0.0f, 0.0f, 1.0f, 1.0f);
    }
}
