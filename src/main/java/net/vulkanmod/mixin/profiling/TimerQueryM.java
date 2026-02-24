package net.vulkanmod.mixin.profiling;

import net.minecraft.class_7168;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(class_7168.class)
public class TimerQueryM {

    @Overwrite
    public void beginProfile() {
    }

    @Overwrite
    public class_7168.class_7169 endProfile() {
        return null;
    }
}
