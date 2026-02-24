package net.vulkanmod.mixin.render.frapi;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.minecraft.class_1087;
import net.minecraft.class_1920;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_776;
import net.minecraft.class_778;
import net.minecraft.class_9891;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_776.class)
abstract class BlockRenderDispatcherMixin {

    @Shadow
    @Final
    private class_778 modelRenderer;

    @Inject(method = "renderBreakingTexture", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;getBlockModel(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/block/model/BlockStateModel;", shift = At.Shift.AFTER), cancellable = true)
    private void afterGetModel(class_2680 blockState, class_2338 blockPos, class_1920 world, class_4587 matrixStack, class_4588 vertexConsumer, CallbackInfo ci, @Local class_1087 model) {
        modelRenderer.render(world, model, blockState, blockPos, matrixStack, layer -> vertexConsumer, true, blockState.method_26190(blockPos), class_4608.field_21444);
        ci.cancel();
    }

    @Redirect(method = "renderSingleBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/renderer/block/model/BlockStateModel;FFFII)V"))
    private void renderProxy(class_4587.class_4665 entry, class_4588 vertexConsumer, class_1087 model, float red, float green, float blue, int light, int overlay, class_2680 state, class_4587 matrices, class_4597 vertexConsumers, int light1, int overlay1) {
        FabricBlockModelRenderer.render(entry, RenderLayerHelper.entityDelegate(vertexConsumers), model, red, green, blue, light, overlay, class_9891.field_52611, class_2338.field_10980, state);
    }
}
