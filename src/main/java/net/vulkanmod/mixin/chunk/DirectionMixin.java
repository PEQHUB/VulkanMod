package net.vulkanmod.mixin.chunk;

import net.minecraft.class_2350;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(class_2350.class)
public class DirectionMixin {

    @Shadow @Final private static class_2350[] BY_3D_DATA;

    @Shadow @Final private int oppositeIndex;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public class_2350 getOpposite() {
        return BY_3D_DATA[this.oppositeIndex];
    }
}
