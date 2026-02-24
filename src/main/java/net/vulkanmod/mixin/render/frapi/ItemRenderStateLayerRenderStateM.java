package net.vulkanmod.mixin.render.frapi;

import net.fabricmc.fabric.api.renderer.v1.render.FabricLayerRenderState;
import net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter;
import net.minecraft.class_10444;
import net.minecraft.class_11659;
import net.minecraft.class_1921;
import net.minecraft.class_4587;
import net.minecraft.class_777;
import net.minecraft.class_811;
import net.vulkanmod.render.chunk.build.frapi.accessor.AccessLayerRenderState;
import net.vulkanmod.render.chunk.build.frapi.accessor.AccessRenderCommandQueue;
import net.vulkanmod.render.chunk.build.frapi.mesh.MutableMeshImpl;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = class_10444.class_10446.class)
abstract class ItemStackRenderStateLayerRenderStateM implements FabricLayerRenderState, AccessLayerRenderState {
    @Unique
    private final MutableMeshImpl mutableMesh = new MutableMeshImpl();

    @Unique
    @Nullable
    private ItemRenderTypeGetter renderTypeGetter = null;

    @Inject(method = "clear()V", at = @At("RETURN"))
    private void onReturnClear(CallbackInfo ci) {
        mutableMesh.clear();
        renderTypeGetter = null;
    }

    @Redirect(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;III[ILjava/util/List;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V"))
    private void submitItemProxy(class_11659 commandQueue, class_4587 matrices, class_811 displayContext, int light, int overlay, int outlineColor, int[] tints, List<class_777> quads, class_1921 layer, class_10444.class_10445 glint) {
        if (mutableMesh.size() > 0 && commandQueue instanceof AccessRenderCommandQueue access) {
            // We don't have to copy the mesh here because vanilla doesn't copy the tint array or quad list either.
            access.submitItem(matrices, displayContext, light, overlay, outlineColor, tints, quads, layer, glint, mutableMesh, renderTypeGetter);
        } else {
            commandQueue.method_73480(matrices, displayContext, light, overlay, outlineColor, tints, quads, layer, glint);
        }
    }

    @Override
    public MutableMeshImpl getMutableMesh() {
        return mutableMesh;
    }

    @Override
    public void setRenderTypeGetter(ItemRenderTypeGetter renderTypeGetter) {
        this.renderTypeGetter = renderTypeGetter;
    }
}
