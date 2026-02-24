package net.vulkanmod.mixin.window;

import net.minecraft.class_1041;
import net.minecraft.class_3678;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_1041.class)
public interface WindowAccessor {

    @Accessor
    class_3678 getEventHandler();
}
