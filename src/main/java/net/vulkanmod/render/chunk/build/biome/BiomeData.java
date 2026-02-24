package net.vulkanmod.render.chunk.build.biome;

import net.minecraft.class_1937;
import net.minecraft.class_1959;
import net.minecraft.class_1972;
import net.minecraft.class_2826;
import net.minecraft.class_3532;
import net.minecraft.class_4540;
import net.minecraft.class_7924;
import net.vulkanmod.render.chunk.build.RenderRegion;
import org.joml.Vector3f;

public class BiomeData {
    private static final int ZOOM_LENGTH = 4;
    private static final int BIOMES_PER_SECTION = 4 * 4 * 4;
    private static final int SIZE = RenderRegion.SIZE * BIOMES_PER_SECTION;

    class_1959[] biomes = new class_1959[SIZE];
    private final long biomeZoomSeed;

    int secX, secY, secZ;

    // Cached cell offsets
    Vector3f[] offsets = new Vector3f[SIZE];

    public BiomeData(long biomeZoomSeed, int secX, int secY, int secZ) {
        this.biomeZoomSeed = biomeZoomSeed;
        this.secX = secX;
        this.secY = secY;
        this.secZ = secZ;
    }

    public void getBiomeData(class_1937 level, class_2826 chunkSection, int secX, int secY, int secZ) {
        class_1959 defaultValue = level.method_30349()
                                  .method_30530(class_7924.field_41236)
                                  .method_46747(class_1972.field_9451)
                                  .comp_349();

        int baseIdx = getRelativeSectionIdx(secX, secY, secZ);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    int relIdx = getRelativeIdx(x, y, z);
                    int idx = baseIdx + relIdx;

                    if (chunkSection != null) {
                        biomes[idx] = chunkSection.method_38293(x, y, z)
                                                  .comp_349();
                    }
                    else {
                        biomes[idx] = defaultValue;
                    }

                }
            }
        }

    }

    public class_1959 getBiome(int blockX, int blockY, int blockZ) {
        int x = blockX - 2;
        int y = blockY - 2;
        int z = blockZ - 2;

        int zoomX = x >> 2;
        int zoomY = y >> 2;
        int zoomZ = z >> 2;

        float fracZoomX = (x & 3) * 0.25f;
        float fracZoomY = (y & 3) * 0.25f;
        float fracZoomZ = (z & 3) * 0.25f;

        int closestCellIdx = 0;
        double closestDistance = Double.POSITIVE_INFINITY;

        for (int i = 0; i < 8; ++i) {
            boolean dirX = (i & 4) != 0;
            boolean dirY = (i & 2) != 0;
            boolean dirZ = (i & 1) != 0;

            int cellX = dirX ? zoomX + 1 : zoomX;
            int cellY = dirY ? zoomY + 1 : zoomY;
            int cellZ = dirZ ? zoomZ + 1 : zoomZ;

            float fCellX = dirX ? fracZoomX - 1.0f : fracZoomX;
            float fCellY = dirY ? fracZoomY - 1.0f : fracZoomY;
            float fCellZ = dirZ ? fracZoomZ - 1.0f : fracZoomZ;

            int baseSectionIdx = getSectionIdx(cellX >> 2, cellY >> 2, cellZ >> 2);
            int cellIdx = baseSectionIdx + getRelativeIdx(cellX & 3, cellY & 3, cellZ & 3);

            Vector3f offset = getOffset(baseSectionIdx, cellX, cellY, cellZ);
            float distance = class_3532.method_27285(fCellX + offset.x()) + class_3532.method_27285(fCellY + offset.y()) + class_3532.method_27285(fCellZ + offset.z());

            if (closestDistance > distance) {
                closestCellIdx = cellIdx;
                closestDistance = distance;
            }
        }

        return this.biomes[closestCellIdx];
    }

    private int getSectionIdx(int secX, int secY, int secZ) {
        return getRelativeSectionIdx(secX - this.secX, secY - this.secY, secZ - this.secZ);
    }

    private Vector3f getOffset(int baseIndex, int cellX, int cellY, int cellZ) {
        int relCellX = cellX & 3;
        int relCellY = cellY & 3;
        int relCellZ = cellZ & 3;
        int idx = baseIndex + getRelativeIdx(relCellX, relCellY, relCellZ);

        if (this.offsets[idx] == null) {
            this.offsets[idx] = computeCellOffset(this.biomeZoomSeed, cellX, cellY, cellZ);
        }

        return this.offsets[idx];
    }

    private static Vector3f computeCellOffset(long l, int cellX, int cellY, int cellZ) {
        long seed;
        seed = class_4540.method_22372(l, cellX);
        seed = class_4540.method_22372(seed, cellY);
        seed = class_4540.method_22372(seed, cellZ);
        seed = class_4540.method_22372(seed, cellX);
        seed = class_4540.method_22372(seed, cellY);
        seed = class_4540.method_22372(seed, cellZ);

        float xOffset = getFiddle(seed);
        seed = class_4540.method_22372(seed, l);
        float yOffset = getFiddle(seed);
        seed = class_4540.method_22372(seed, l);
        float zOffset = getFiddle(seed);

        return new Vector3f(xOffset, yOffset, zOffset);
    }

    private static float getFiddle(long l) {
        float d = Math.floorMod(l >> 24, 1024) * (1.0f / 1024.0f);
        return (d - 0.5f) * 0.9f;
    }

    private static int getRelativeSectionIdx(int x, int y, int z) {
        return ((x * RenderRegion.WIDTH * RenderRegion.WIDTH) + (y * RenderRegion.WIDTH) + z) * BIOMES_PER_SECTION;
    }

    private static int getRelativeIdx(int x, int y, int z) {
        return (x * ZOOM_LENGTH * ZOOM_LENGTH) + (y * ZOOM_LENGTH) + z;
    }

}
