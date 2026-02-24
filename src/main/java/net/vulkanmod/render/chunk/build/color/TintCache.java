package net.vulkanmod.render.chunk.build.color;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.class_1163;
import net.minecraft.class_1959;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import net.minecraft.class_6539;
import net.vulkanmod.render.chunk.build.biome.BiomeData;

public class TintCache {
    private static final int SECTION_WIDTH = 16;
    private static final int BOUNDARY_WIDTH = 16;
    private static final int LAYER_COUNT = SECTION_WIDTH + (BOUNDARY_WIDTH * 2);

    private final Reference2ReferenceOpenHashMap<class_6539, Layer[]> layers;

    private BiomeData biomeData;
    private int blendRadius, totalWidth;
    private int secX, secY, secZ;
    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;

    private int dataSize;
    private int[] temp;

    public TintCache() {
        this.layers = new Reference2ReferenceOpenHashMap<>();

        // Default resolvers
        this.layers.put(class_1163.field_5664, allocateLayers());
        this.layers.put(class_1163.field_5665, allocateLayers());
        this.layers.put(class_1163.field_5666, allocateLayers());
    }

    public void init(BiomeData biomeData, int blendRadius, int secX, int secY, int secZ) {
        this.biomeData = biomeData;
        this.blendRadius = class_310.method_1551().field_1690.method_41805().method_41753();
        this.totalWidth = (blendRadius * 2) + 16;

        this.secX = secX;
        this.secY = secY;
        this.secZ = secZ;

        this.minX = (secX << 4) - blendRadius;
        this.minZ = (secZ << 4) - blendRadius;
        this.maxX = (secX << 4) + 15 + blendRadius;
        this.maxZ = (secZ << 4) + 15 + blendRadius;

        this.minY = (secY << 4) - 2;
        this.maxY = this.minY + 15 + 4;

        int size = totalWidth * totalWidth;

        if (size != this.dataSize) {
            this.dataSize = size;

            for (Layer[] layers : layers.values()) {
                for (Layer layer : layers) {
                    layer.allocate(size);
                }
            }

            this.temp = new int[size];
        }
        else {
            for (Layer[] layers : layers.values()) {
                for (Layer layer : layers) {
                    layer.invalidate();
                }
            }
        }
    }

    public int getColor(class_2338 blockPos, class_6539 colorResolver) {
        int relY = blockPos.method_10264() - this.minY;

        if (!this.layers.containsKey(colorResolver)) {
            addResolver(colorResolver);
        }

        Layer layer = this.layers.get(colorResolver)[relY];

        if (layer.invalidated) {
            calculateLayer(layer, colorResolver, relY);
        }

        int[] values = layer.getValues();

        int relX = blockPos.method_10263() - this.minX;
        int relZ = blockPos.method_10260() - this.minZ;

        int idx = this.totalWidth * (relZ) + (relX);
        return values[idx];
    }

    private void addResolver(class_6539 colorResolver) {
        Layer[] layers1 = allocateLayers();

        for (Layer layer : layers1) {
            layer.allocate(this.dataSize);
        }

        this.layers.put(colorResolver, layers1);
    }

    private Layer[] allocateLayers() {
        Layer[] layers = new Layer[LAYER_COUNT];

        for (int i = 0; i < LAYER_COUNT; i++) {
            layers[i] = new Layer();
        }

        return layers;
    }

    private void calculateLayer(Layer layer, class_6539 colorResolver, int y) {
        int absY = minY + y;

        int[] values = layer.values;

        for (int absZ = minZ; absZ <= maxZ; absZ++) {
            for (int absX = minX; absX <= maxX; absX++) {
                class_1959 biome = this.biomeData.getBiome(absX, absY, absZ);

                final int idx = (absX - minX) + (absZ - minZ) * totalWidth;
                values[idx] = colorResolver.getColor(biome, absX, absZ);
            }
        }

        if (blendRadius > 0) {
            this.applyBlur(values);
        }

        layer.invalidated = false;
    }

    private void applyBlur(int[] buffer) {
        int value = buffer[0];
        boolean needsBlur = false;
        for (int i = 1; i < buffer.length; ++i) {
            if (value != buffer[i]) {
                needsBlur = true;
                break;
            }
        }

        if (needsBlur)
            BoxBlur.blur(buffer, temp, SECTION_WIDTH, blendRadius);
    }

    static class Layer {
        private boolean invalidated = true;
        private int[] values;

        void allocate(int size) {
            this.values = new int[size];
            invalidate();
        }

        void invalidate() {
            this.invalidated = true;
        }

        public int[] getValues() {
            return this.values;
        }
    }
}
