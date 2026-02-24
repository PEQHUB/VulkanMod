package net.vulkanmod.mixin.render.frapi;

import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer;
import net.minecraft.class_1087;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_778;
import net.minecraft.class_9891;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(class_778.class)
abstract class ModelBlockRendererMixin {
    @Overwrite
    public static void renderModel(class_4587.class_4665 entry, class_4588 vertexConsumer, class_1087 model, float red, float green, float blue, int light, int overlay) {
        FabricBlockModelRenderer.render(entry, layer -> vertexConsumer, model, red, green, blue, light, overlay, class_9891.field_52611, class_2338.field_10980, class_2246.field_10124.method_9564());
    }
}
