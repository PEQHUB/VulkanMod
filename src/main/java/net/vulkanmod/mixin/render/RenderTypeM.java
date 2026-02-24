package net.vulkanmod.mixin.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_11219;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_276;
import net.minecraft.class_9801;
import net.vulkanmod.interfaces.ExtendedRenderType;
import net.vulkanmod.render.engine.VkCommandEncoder;
import net.vulkanmod.render.engine.VkRenderPass;
import net.vulkanmod.render.vertex.TerrainRenderType;
import net.vulkanmod.vulkan.Renderer;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;

@Mixin(class_1921.class)
public class RenderTypeM implements ExtendedRenderType {
    @Unique
    TerrainRenderType terrainRenderType;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void inj(String string, class_12247 renderSetup, CallbackInfo ci) {
        terrainRenderType = switch (string) {
            case "solid" -> TerrainRenderType.SOLID;
            case "cutout" -> TerrainRenderType.CUTOUT;
            case "translucent" -> TerrainRenderType.TRANSLUCENT;
            case "tripwire" -> TerrainRenderType.TRIPWIRE;
            default -> null;
        };
    }

    @Override
    public TerrainRenderType getTerrainRenderType() {
        return terrainRenderType;
    }

    @Shadow @Final private class_12247 state;
    @Shadow @Final protected String name;

    @Overwrite
    public void draw(class_9801 meshData) {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        final var renderSetupAccessor = (RenderSetupAccessor) (Object) this.state;
        Consumer<Matrix4fStack> consumer = renderSetupAccessor.layeringTransform().method_75918();
        if (consumer != null) {
            matrix4fStack.pushMatrix();
            consumer.accept(matrix4fStack);
        }

        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms()
                                                    .method_71106(RenderSystem.getModelViewMatrix(),
                                                                    new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
                                                                    new Vector3f(),
                                                                    renderSetupAccessor.textureTransform().method_76030());

        Map<String, class_12247.class_12337> map = this.state.method_75926();

        GpuBuffer gpuBuffer = renderSetupAccessor.pipeline().getVertexFormat().uploadImmediateVertexBuffer(meshData.method_60818());
        GpuBuffer gpuBuffer2;
        VertexFormat.class_5595 indexType;
        if (meshData.method_60821() == null) {
            RenderSystem.class_5590 autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(meshData.method_60822().comp_752());
            gpuBuffer2 = autoStorageIndexBuffer.method_68274(meshData.method_60822().comp_751());
            indexType = autoStorageIndexBuffer.method_31924();
        } else {
            gpuBuffer2 = renderSetupAccessor.pipeline().getVertexFormat().uploadImmediateIndexBuffer(meshData.method_60821());
            indexType = meshData.method_60822().comp_753();
        }

        class_276 renderTarget = renderSetupAccessor.outputTarget().method_75921();
        GpuTextureView gpuTextureView = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : renderTarget.method_71639();
        GpuTextureView gpuTextureView2 = renderTarget.field_1478 ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : renderTarget.method_71640()) : null;

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Immediate draw for " + this.name, gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty())) {
            renderPass.setPipeline(renderSetupAccessor.pipeline());
            class_11219 scissorState = RenderSystem.getScissorStateForRenderTypeDraws();
            if (scissorState.method_72091()) {
                renderPass.enableScissor(scissorState.method_72092(), scissorState.method_72093(), scissorState.method_72094(), scissorState.method_72095());
            }

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setVertexBuffer(0, gpuBuffer);

            for(Map.Entry<String, class_12247.class_12337> entry : map.entrySet()) {
                renderPass.bindTexture(entry.getKey(), entry.getValue().comp_5226(), entry.getValue().comp_5227());
            }

            renderPass.setIndexBuffer(gpuBuffer2, indexType);
//                renderPass.drawIndexed(0, 0, meshData.drawState().indexCount(), 1);

            VkCommandEncoder commandEncoder = (VkCommandEncoder) RenderSystem.getDevice().createCommandEncoder();
            commandEncoder.trySetup((VkRenderPass) renderPass);

            Renderer.getDrawer().draw(meshData.method_60818(), meshData.method_60821(), meshData.method_60822().comp_752(), meshData.method_60822().comp_749(), meshData.method_60822().comp_750());
        }

        if (meshData != null) {
            meshData.close();
        }

        if (consumer != null) {
            matrix4fStack.popMatrix();
        }

    }
}
