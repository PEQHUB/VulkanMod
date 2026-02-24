package net.vulkanmod.mixin.render.frapi;

import net.minecraft.class_10444;
import net.minecraft.class_11687;
import net.minecraft.class_11788;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4618;
import net.vulkanmod.render.chunk.build.frapi.accessor.AccessBatchingRenderCommandQueue;
import net.vulkanmod.render.chunk.build.frapi.render.ItemRenderContext;
import net.vulkanmod.render.chunk.build.frapi.render.MeshItemCommand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_11687.class)
public class ItemFeatureRendererM {

    @Shadow @Final private class_4587 poseStack;

    @Unique private final ItemRenderContext itemRenderContext = new ItemRenderContext();

    @Inject(method = "render", at = @At("RETURN"))
    private void onReturnRender(class_11788 queue, class_4597.class_4598 vertexConsumers, class_4618 outlineVertexConsumers, CallbackInfo ci) {
        for (MeshItemCommand itemCommand : ((AccessBatchingRenderCommandQueue) queue).getMeshItemCommands()) {
            poseStack.method_22903();
            poseStack.method_23760().method_66521(itemCommand.positionMatrix());

            itemRenderContext.renderModel(itemCommand.displayContext(), poseStack, vertexConsumers, itemCommand.lightCoords(), itemCommand.overlayCoords(), itemCommand.tintLayers(), itemCommand.quads(), itemCommand.mesh(), itemCommand.renderLayer(), itemCommand.glintType(), false);

            if (itemCommand.outlineColor() != 0) {
                outlineVertexConsumers.method_23286(itemCommand.outlineColor());
                itemRenderContext.renderModel(itemCommand.displayContext(), poseStack, outlineVertexConsumers, itemCommand.lightCoords(), itemCommand.overlayCoords(), itemCommand.tintLayers(), itemCommand.quads(), itemCommand.mesh(), itemCommand.renderLayer(), class_10444.class_10445.field_55341, true);
            }

            poseStack.method_22909();
        }
    }
}
