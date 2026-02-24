package net.vulkanmod.render.chunk.build.light.data;

import java.util.Arrays;
import net.minecraft.class_1920;
import net.minecraft.class_2338;
import net.minecraft.class_4076;

/**
 * A light data cache which uses a flat-array to store the light data for the blocks in a given chunk and its direct
 * neighbors. This is considerably faster than using a hash table to lookup values for a given block position and
 * can be re-used to avoid allocations.
 */
public class ArrayLightDataCache extends LightDataAccess {
    private static final int NEIGHBOR_BLOCK_RADIUS = 2;
    private static final int BLOCK_LENGTH = 16 + (NEIGHBOR_BLOCK_RADIUS * 2);

    private final int[] light;

    private int xOffset, yOffset, zOffset;

    public ArrayLightDataCache() {
        super();
        this.light = new int[BLOCK_LENGTH * BLOCK_LENGTH * BLOCK_LENGTH];
    }

    public void reset(class_1920 blockAndTintGetter, int x, int y, int z) {
        this.region = blockAndTintGetter;

        this.xOffset = x - NEIGHBOR_BLOCK_RADIUS;
        this.yOffset = y - NEIGHBOR_BLOCK_RADIUS;
        this.zOffset = z - NEIGHBOR_BLOCK_RADIUS;

        Arrays.fill(this.light, 0);
    }

    public void reset(class_1920 blockAndTintGetter, class_2338 origin) {
        this.region = blockAndTintGetter;

        this.xOffset = origin.method_10263() - NEIGHBOR_BLOCK_RADIUS;
        this.yOffset = origin.method_10264() - NEIGHBOR_BLOCK_RADIUS;
        this.zOffset = origin.method_10260() - NEIGHBOR_BLOCK_RADIUS;

        Arrays.fill(this.light, 0);
    }

    public void reset(class_4076 origin) {
        this.xOffset = origin.method_19527() - NEIGHBOR_BLOCK_RADIUS;
        this.yOffset = origin.method_19528() - NEIGHBOR_BLOCK_RADIUS;
        this.zOffset = origin.method_19529() - NEIGHBOR_BLOCK_RADIUS;

        Arrays.fill(this.light, 0);
    }

    public void reset(class_2338 origin) {
        this.xOffset = origin.method_10263() - NEIGHBOR_BLOCK_RADIUS;
        this.yOffset = origin.method_10264() - NEIGHBOR_BLOCK_RADIUS;
        this.zOffset = origin.method_10260() - NEIGHBOR_BLOCK_RADIUS;

        Arrays.fill(this.light, 0);
    }

    private int index(int x, int y, int z) {
        int x2 = x - this.xOffset;
        int y2 = y - this.yOffset;
        int z2 = z - this.zOffset;

        return (z2 * BLOCK_LENGTH * BLOCK_LENGTH) + (y2 * BLOCK_LENGTH) + x2;
    }

    @Override
    public int get(int x, int y, int z) {
        int l = this.index(x, y, z);

        int word = this.light[l];

        if (word != 0) {
            return word;
        }

        return this.light[l] = this.compute(x, y, z);
    }
}