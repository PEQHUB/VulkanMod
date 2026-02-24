package net.vulkanmod.interfaces.biome;

import net.minecraft.class_4543;

public interface BiomeManagerExtended {

    static BiomeManagerExtended of(class_4543 biomeManager) {
        return (BiomeManagerExtended) biomeManager;
    }

    long getBiomeZoomSeed();

}
