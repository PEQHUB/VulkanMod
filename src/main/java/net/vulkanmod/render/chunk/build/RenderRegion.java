package net.vulkanmod.render.chunk.build;

import net.minecraft.class_1920;
import net.minecraft.class_1937;
import net.minecraft.class_1944;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_2804;
import net.minecraft.class_2841;
import net.minecraft.class_2891;
import net.minecraft.class_310;
import net.minecraft.class_3568;
import net.minecraft.class_3610;
import net.minecraft.class_4076;
import net.minecraft.class_6539;
import net.vulkanmod.render.chunk.build.biome.BiomeData;
import net.vulkanmod.render.chunk.build.color.TintCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class RenderRegion implements class_1920 {
    public static final int WIDTH = 3;
    public static final int SIZE = WIDTH * WIDTH * WIDTH;

    public static final int BOUNDARY_BLOCK_WIDTH = 2;
    public static final int REGION_BLOCK_WIDTH = 16 + BOUNDARY_BLOCK_WIDTH * 2;
    public static final int BLOCK_COUNT = REGION_BLOCK_WIDTH * REGION_BLOCK_WIDTH * REGION_BLOCK_WIDTH;

    public static final class_2680 AIR_BLOCK_STATE = class_2246.field_10124.method_9564();

    private final int minSecX, minSecY, minSecZ;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;
    private final class_1937 level;
    private final int blendRadius;

    private final class_2841<class_2680>[] blockDataContainers;
    private final class_2680[] blockData;
    private final class_2804[][] lightData;

    private BiomeData biomeData;
    private TintCache tintCache;

    private final Map<class_2338, class_2586> blockEntityMap;

    private final Function<class_2338, class_2680> blockStateGetter;

    RenderRegion(class_1937 level, int x, int y, int z, class_2841<class_2680>[] blockData, class_2804[][] lightData,
                 BiomeData biomeData, Map<class_2338, class_2586> blockEntityMap) {
        this.level = level;

        this.minSecX = x - 1;
        this.minSecY = y - 1;
        this.minSecZ = z - 1;
        this.minX = (minSecX << 4) + 16 - BOUNDARY_BLOCK_WIDTH;
        this.minZ = (minSecZ << 4) + 16 - BOUNDARY_BLOCK_WIDTH;
        this.minY = (minSecY << 4) + 16 - BOUNDARY_BLOCK_WIDTH;
        this.maxX = minX + REGION_BLOCK_WIDTH;
        this.maxZ = minZ + REGION_BLOCK_WIDTH;
        this.maxY = minY + REGION_BLOCK_WIDTH;

        this.blockDataContainers = blockData;
        this.lightData = lightData;
        this.biomeData = biomeData;
        this.blockEntityMap = blockEntityMap;

        this.blockData = new class_2680[BLOCK_COUNT];

        this.blockStateGetter = level.method_27982() ? this::debugBlockState : this::defaultBlockState;

        this.blendRadius = class_310.method_1551().field_1690.method_41805().method_41753();
    }

    public void loadBlockStates() {
        Arrays.fill(blockData, class_2246.field_10124.method_9564());

        for (int x = 0; x <= 2; ++x) {
            for (int z = 0; z <= 2; ++z) {
                for (int y = 0; y <= 2; ++y) {
                    final int idx = getSectionIdx(x, y, z);

                    class_2841<class_2680> container = blockDataContainers[idx];

                    if (container == null)
                        continue;

                    int absBlockX = (x + minSecX) << 4;
                    int absBlockY = (y + minSecY) << 4;
                    int absBlockZ = (z + minSecZ) << 4;

                    int tMinX = Math.max(minX, absBlockX);
                    int tMinY = Math.max(minY, absBlockY);
                    int tMinZ = Math.max(minZ, absBlockZ);

                    int tMaxX = Math.min(maxX, absBlockX + 16);
                    int tMaxY = Math.min(maxY, absBlockY + 16);
                    int tMaxZ = Math.min(maxZ, absBlockZ + 16);

                    loadSectionBlockStates(container, blockData,
                                           tMinX, tMinY, tMinZ, tMaxX, tMaxY, tMaxZ);

                }
            }
        }
    }

    void loadSectionBlockStates(class_2841<class_2680> container, class_2680[] blockStates,
                                int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

        for (int y = minY; y < maxY; ++y) {
            for (int z = minZ; z < maxZ; ++z) {
                for (int x = minX; x < maxX; ++x) {
                    final int idx = getBlockIdx(x - this.minX, y - this.minY, z - this.minZ);

                    blockStates[idx] = container != null ?
                            container.method_12321(x & 15, y & 15, z & 15)
                            : class_2246.field_10124.method_9564();
                }
            }
        }
    }

    public void initTintCache(TintCache tintCache) {
        this.tintCache = tintCache;
        this.tintCache.init(biomeData, blendRadius, minSecX + 1, minSecY + 1, minSecZ + 1);
    }

    public class_2680 method_8320(class_2338 blockPos) {
        return blockStateGetter.apply(blockPos);
    }

    public class_3610 method_8316(class_2338 blockPos) {
        return this.method_8320(blockPos).method_26227();
    }

    public float method_24852(class_2350 direction, boolean bl) {
        return this.level.method_24852(direction, bl);
    }

    public class_3568 method_22336() {
        return this.level.method_22336();
    }

    public int method_8314(class_1944 lightLayer, class_2338 blockPos) {
        if (outsideRegion(blockPos.method_10263(), blockPos.method_10264(), blockPos.method_10260())) {
            return 0;
        }

        int secX = class_4076.method_18675(blockPos.method_10263()) - this.minSecX;
        int secY = class_4076.method_18675(blockPos.method_10264()) - this.minSecY;
        int secZ = class_4076.method_18675(blockPos.method_10260()) - this.minSecZ;

        class_2804 dataLayer = this.lightData[getSectionIdx(secX, secY, secZ)][lightLayer.ordinal()];
        return dataLayer == null ? 0 : dataLayer.method_12139(blockPos.method_10263() & 15, blockPos.method_10264() & 15, blockPos.method_10260() & 15);
    }

    public int method_22335(class_2338 blockPos, int i) {
        if (outsideRegion(blockPos.method_10263(), blockPos.method_10264(), blockPos.method_10260())) {
            return 0;
        }

        int secX = class_4076.method_18675(blockPos.method_10263()) - this.minSecX;
        int secY = class_4076.method_18675(blockPos.method_10264()) - this.minSecY;
        int secZ = class_4076.method_18675(blockPos.method_10260()) - this.minSecZ;

        class_2804[] dataLayers = this.lightData[getSectionIdx(secX, secY, secZ)];
        class_2804 skyLightLayer = dataLayers[class_1944.field_9284.ordinal()];
        class_2804 blockLightLayer = dataLayers[class_1944.field_9282.ordinal()];

        int relX = blockPos.method_10263() & 15;
        int relY = blockPos.method_10264() & 15;
        int relZ = blockPos.method_10260() & 15;

        int skyLight = skyLightLayer == null ? 0 : skyLightLayer.method_12139(relX, relY, relZ) - i;
        int blockLight = blockLightLayer == null ? 0 : blockLightLayer.method_12139(relX, relY, relZ);
        return Math.max(skyLight, blockLight);
    }

    @Nullable
    public class_2586 method_8321(@NotNull class_2338 blockPos) {
        return this.blockEntityMap.get(blockPos);
    }

    public int method_23752(class_2338 blockPos, class_6539 colorResolver) {
        return tintCache.getColor(blockPos, colorResolver);
    }

    public int method_31607() {
        return this.level.method_31607();
    }

    public int method_31605() {
        return this.level.method_31605();
    }

    public int getSectionIdx(int secX, int secY, int secZ) {
        return WIDTH * ((WIDTH * secY) + secZ) + secX;
    }

    public int getBlockIdx(int x, int y, int z) {
        return REGION_BLOCK_WIDTH * ((REGION_BLOCK_WIDTH * y) + z) + x;
    }

    public boolean outsideRegion(int x, int y, int z) {
        return x < minX || x >= maxX || y < minY || y >= maxY || z < minZ || z >= maxZ;
    }

    public class_2680 defaultBlockState(class_2338 blockPos) {
        int x = blockPos.method_10263();
        int y = blockPos.method_10264();
        int z = blockPos.method_10260();

        if (outsideRegion(x, y, z)) {
            return AIR_BLOCK_STATE;
        }

        x -= minX;
        y -= minY;
        z -= minZ;

        return blockData[getBlockIdx(x, y, z)];
    }

    public class_2680 debugBlockState(class_2338 blockPos) {
        int x = blockPos.method_10263();
        int y = blockPos.method_10264();
        int z = blockPos.method_10260();

        class_2680 blockState = null;
        if (y == 60) {
            blockState = class_2246.field_10499.method_9564();
        }
        else if (y == 70) {
            blockState = class_2891.method_12578(x, z);
        }

        return blockState == null ? class_2246.field_10124.method_9564() : blockState;
    }
}
