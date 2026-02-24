package net.vulkanmod.mixin.wayland;

import net.minecraft.class_1041;
import net.minecraft.class_155;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_3262;
import net.minecraft.class_3268;
import net.minecraft.class_8518;
import net.vulkanmod.config.Platform;
import net.vulkanmod.config.video.VideoModeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(class_310.class)
public class MinecraftMixin {

    @Shadow @Final private class_1041 window;
    @Shadow @Final public class_315 options;

    @Shadow @Final private class_3268 vanillaPackResources;

    /**
     * @author
     * @reason Only KWin supports setting the Icon on Wayland AFAIK
     */
    @Redirect(method="<init>", at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/platform/Window;setIcon(Lnet/minecraft/server/packs/PackResources;Lcom/mojang/blaze3d/platform/IconSet;)V"))
    private void bypassWaylandIcon(class_1041 instance, class_3262 packResources, class_8518 iconSet) throws IOException {
        if (!Platform.isWayLand())
        {
            this.window.method_4491(this.vanillaPackResources, class_155.method_16673().comp_4031() ? class_8518.field_44650 : class_8518.field_44651);
        }
    }
}
