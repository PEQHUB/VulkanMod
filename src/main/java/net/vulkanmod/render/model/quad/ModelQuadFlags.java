package net.vulkanmod.render.model.quad;

import net.minecraft.class_2350;

public class ModelQuadFlags {
    /**
     * Indicates that the quad does not fully cover the given face for the model.
     */
    public static final int IS_PARTIAL = 0b001;

    /**
     * Indicates that the quad is parallel to its light face.
     */
    public static final int IS_PARALLEL = 0b010;

    /**
     * Indicates that the quad is aligned to the block grid.
     * This flag is only set if {@link #IS_PARALLEL} is set.
     */
    public static final int IS_ALIGNED = 0b100;

    public static boolean contains(int flags, int mask) {
        return (flags & mask) != 0;
    }

    public static int getQuadFlags(ModelQuadView quad, class_2350 face) {
        float minX = 32.0F;
        float minY = 32.0F;
        float minZ = 32.0F;

        float maxX = -32.0F;
        float maxY = -32.0F;
        float maxZ = -32.0F;

        for (int i = 0; i < 4; ++i) {
            float x = quad.getX(i);
            float y = quad.getY(i);
            float z = quad.getZ(i);

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }

        boolean partial = switch (face.method_10166()) {
            case field_11048 -> minY >= 0.0001f || minZ >= 0.0001f || maxY <= 0.9999F || maxZ <= 0.9999F;
            case field_11052 -> minX >= 0.0001f || minZ >= 0.0001f || maxX <= 0.9999F || maxZ <= 0.9999F;
            case field_11051 -> minX >= 0.0001f || minY >= 0.0001f || maxX <= 0.9999F || maxY <= 0.9999F;
        };

        boolean parallel = switch(face.method_10166()) {
            case field_11048 -> minX == maxX;
            case field_11052 -> minY == maxY;
            case field_11051 -> minZ == maxZ;
        };

        boolean aligned = parallel && switch (face) {
            case field_11033 -> minY < 0.0001f;
            case field_11036 -> maxY > 0.9999F;
            case field_11043 -> minZ < 0.0001f;
            case field_11035 -> maxZ > 0.9999F;
            case field_11039 -> minX < 0.0001f;
            case field_11034 -> maxX > 0.9999F;
        };

        int flags = 0;

        if (partial) {
            flags |= IS_PARTIAL;
        }

        if (parallel) {
            flags |= IS_PARALLEL;
        }

        if (aligned) {
            flags |= IS_ALIGNED;
        }

        return flags;
    }


}
