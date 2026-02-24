package net.vulkanmod.render.model.quad;

import net.minecraft.class_2350;
import net.vulkanmod.render.chunk.cull.QuadFacing;

public interface ModelQuadView {

    int getFlags();

    float getX(int idx);

    float getY(int idx);

    float getZ(int idx);

    int getColor(int idx);

    float getU(int idx);

    float getV(int idx);

    int getColorIndex();

    class_2350 getFacingDirection();

    class_2350 lightFace();

    QuadFacing getQuadFacing();

    int getNormal();

    default boolean isTinted() {
        return this.getColorIndex() != -1;
    }


}
