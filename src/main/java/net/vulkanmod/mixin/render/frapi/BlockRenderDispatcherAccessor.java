package net.vulkanmod.mixin.render.frapi;

import net.minecraft.class_324;
import net.minecraft.class_776;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_776.class)
public interface BlockRenderDispatcherAccessor {
    @Accessor("blockColors")
    class_324 getBlockColors();
}
