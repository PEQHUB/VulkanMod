package net.vulkanmod.mixin.voxel;

import net.minecraft.class_249;
import net.minecraft.class_251;
import net.minecraft.class_265;
import net.minecraft.world.phys.shapes.*;
import net.vulkanmod.interfaces.VoxelShapeExtended;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_265.class)
public class VoxelShapeMixin implements VoxelShapeExtended {
    @Shadow @Final protected class_251 shape;

    int co;

    @SuppressWarnings("UnreachableCode")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void initCornerOcclusion(class_251 discreteVoxelShape, CallbackInfo ci) {
        var disShape = this.shape;

        // TODO: lithium subclasses
        // lithium is using its own classes for simple cube shapes
        class_265 shape = (class_265)((Object)this);
        if(!(shape instanceof class_249) || disShape == null) {
            this.co = 0;
            return;
        }

        int xSize = Math.max(disShape.method_1050(), 1);
        int ySize = Math.max(disShape.method_1047(), 1);
        int zSize = Math.max(disShape.method_1048(), 1);

        int co = 0;
        int s = 0;
        for (int y1 = 0; y1 <= 1; y1++) {
            for (int z1 = 0; z1 <= 1; z1++) {
                for (int x1 = 0; x1 <= 1; x1++) {

                    final int x2 = x1 * (xSize - 1), y2 = y1 * (ySize - 1), z2 = z1 * (zSize - 1);
                    co |= (disShape.method_1063(x2, y2, z2) ? 1 : 0) << s;

                    s++;
                }
            }
        }

        this.co = co;
    }

    public int getCornerOcclusion() {
        return this.co;
    }
}
