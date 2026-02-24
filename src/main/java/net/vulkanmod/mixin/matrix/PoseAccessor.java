package net.vulkanmod.mixin.matrix;

import net.minecraft.class_4587;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_4587.class_4665.class)
public interface PoseAccessor {

    @Accessor("trustedNormals")
    boolean trustedNormals();
}
