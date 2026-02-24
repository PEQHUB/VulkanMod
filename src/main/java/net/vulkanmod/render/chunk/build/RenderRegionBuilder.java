package net.vulkanmod.render.chunk.build;

import it.unimi.dsi.fastutil.longs.Long2ReferenceLinkedOpenHashMap;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_1944;
import net.minecraft.class_2680;
import net.minecraft.class_2804;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_2841;
import net.minecraft.class_4076;
import net.vulkanmod.interfaces.biome.BiomeManagerExtended;
import net.vulkanmod.render.chunk.build.biome.BiomeData;

public class RenderRegionBuilder {
    private static final class_2804 DEFAULT_SKY_LIGHT_DATA_LAYER = new class_2804(15);
    private static final class_2804 DEFAULT_BLOCK_LIGHT_DATA_LAYER = new class_2804(0);

    private static final int MAX_CACHE_ENTRIES = 256;
    private final Long2ReferenceLinkedOpenHashMap<class_2818> levelChunkCache = new Long2ReferenceLinkedOpenHashMap<>(MAX_CACHE_ENTRIES);

    public RenderRegion createRegion(class_1937 level, int secX, int secY, int secZ) {
        class_2818 levelChunk = getLevelChunk(level, secX, secZ);
        var sections = levelChunk.method_12006();
        class_2826 section = sections[level.method_31603(secY)];

        if (section == null || section.method_38292())
            return null;

        var blockEntityMap = levelChunk.method_12214();

        int minSecX = secX - 1;
        int minSecZ = secZ - 1;
        int minSecY = secY - 1;
        int maxSecX = secX + 1;
        int maxSecZ = secZ + 1;
        int maxSecY = secY + 1;

        class_2841<class_2680>[] blockData = new class_2841[RenderRegion.SIZE];

        class_2804[][] lightData = new class_2804[RenderRegion.SIZE][2 /* Light types */];

        long biomeZoomSeed = BiomeManagerExtended.of(level.method_22385()).getBiomeZoomSeed();
        BiomeData biomeData = new BiomeData(biomeZoomSeed, minSecX, minSecY, minSecZ);

        final int minHeightSec = level.method_31607() >> 4;
        for (int x = minSecX; x <= maxSecX; ++x) {
            for (int z = minSecZ; z <= maxSecZ; ++z) {
                class_2818 levelChunk1 = getLevelChunk(level, x, z);
                sections = levelChunk1.method_12006();

                for (int y = minSecY; y <= maxSecY; ++y) {
                    int sectionIdx = y - minHeightSec;
                    section = sectionIdx >= 0 && sectionIdx < sections.length ? sections[sectionIdx] : null;

                    final int relX = (x - minSecX), relY = (y - minSecY), relZ = (z - minSecZ);
                    final int idx = (relY * RenderRegion.WIDTH + relZ) * RenderRegion.WIDTH + relX;

                    class_2841<class_2680> values = section == null || section.method_38292() ? null : section.method_12265().method_39957();

                    blockData[idx] = values;

                    class_4076 pos = class_4076.method_18676(x, y, z);
                    class_2804[] dataLayers = getSectionDataLayers(level, pos);

                    lightData[idx] = dataLayers;

                    biomeData.getBiomeData(level, section, relX, relY, relZ);
                }
            }
        }

        return new RenderRegion(level, secX, secY, secZ, blockData, lightData, biomeData, blockEntityMap);
    }

    private class_2804[] getSectionDataLayers(class_1937 level, class_4076 pos) {
        class_2804[] dataLayers = new class_2804[2];

        class_2804 blockDataLayer;
        blockDataLayer = level.method_22336().method_15562(class_1944.field_9282).method_15544(pos);

        if (blockDataLayer == null)
            blockDataLayer = DEFAULT_BLOCK_LIGHT_DATA_LAYER;

        dataLayers[class_1944.field_9282.ordinal()] = blockDataLayer;

        class_2804 skyDataLayer;
        if (level.method_8597().comp_642()) {
            skyDataLayer = level.method_22336().method_15562(class_1944.field_9284).method_15544(pos);

            if (skyDataLayer == null)
                skyDataLayer = DEFAULT_SKY_LIGHT_DATA_LAYER;
        } else
            skyDataLayer = null;

        dataLayers[class_1944.field_9284.ordinal()] = skyDataLayer;

        return dataLayers;
    }

    private class_2818 getLevelChunk(class_1937 level, int x, int z) {
        long l = class_1923.method_8331(x, z);
        class_2818 chunk = this.levelChunkCache.getAndMoveToFirst(l);

        if (chunk == null) {
            chunk = level.method_8497(x, z);

            while (levelChunkCache.size() >= MAX_CACHE_ENTRIES) {
                levelChunkCache.removeLast();
            }

            this.levelChunkCache.putAndMoveToFirst(l, chunk);
        }

        return chunk;
    }

    public void remove(int x, int z) {
        levelChunkCache.remove(class_1923.method_8331(x, z));
    }

    public void clear() {
        this.levelChunkCache.clear();
    }
}
