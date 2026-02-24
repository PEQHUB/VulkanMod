package net.vulkanmod.render.chunk.cull;

import net.minecraft.class_2350;
import net.minecraft.class_3532;
import net.vulkanmod.render.vertex.format.I32_SNorm;
import org.joml.Vector3f;

public enum QuadFacing {
    X_POS,
    X_NEG,
    Y_POS,
    Y_NEG,
    Z_POS,
    Z_NEG,
    UNDEFINED;

    public static final QuadFacing[] VALUES = QuadFacing.values();
    public static final int COUNT = VALUES.length;

    public static QuadFacing fromDirection(class_2350 direction) {
        return switch (direction) {
            case field_11033 -> Y_NEG;
            case field_11036 -> Y_POS;
            case field_11043 -> Z_NEG;
            case field_11035 -> Z_POS;
            case field_11039 -> X_NEG;
            case field_11034 -> X_POS;
        };
    }

    public static QuadFacing fromNormal(int packedNormal) {
        final float x = I32_SNorm.unpackX(packedNormal);
        final float y = I32_SNorm.unpackY(packedNormal);
        final float z = I32_SNorm.unpackZ(packedNormal);

        return fromNormal(x, y, z);
    }

    public static QuadFacing fromNormal(Vector3f normal) {
        return fromNormal(normal.x(), normal.y(), normal.z());
    }

    public static QuadFacing fromNormal(float x, float y, float z) {
        final float absX = Math.abs(x);
        final float absY = Math.abs(y);
        final float absZ = Math.abs(z);

        float sum = absX + absY + absZ;

        if (class_3532.method_15347(sum, 1.0f)) {
            if (class_3532.method_15347(absX, 1.0f)) {
                return x > 0.0f ? QuadFacing.X_POS : QuadFacing.X_NEG;
            }

            if (class_3532.method_15347(absY, 1.0f)) {
                return y > 0.0f ? QuadFacing.Y_POS : QuadFacing.Y_NEG;
            }

            if (class_3532.method_15347(absZ, 1.0f)) {
                return z > 0.0f ? QuadFacing.Z_POS : QuadFacing.Z_NEG;
            }
        }

        return QuadFacing.UNDEFINED;
    }
}
